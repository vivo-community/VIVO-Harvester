/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * Changes the namespace for all matching uris
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ChangeNamespace {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ChangeNamespace.class);
	/**
	 * The model to change uris in
	 */
	private final JenaConnect model;
	/**
	 * The old namespace
	 */
	private final String oldNamespace;
	/**
	 * The new namespace
	 */
	private final String newNamespace;
	/**
	 * The model in which to search for previously used uris
	 */
	private final JenaConnect vivo;
	/**
	 * Log error messages for changed nodes
	 */
	private final boolean errorLogging;
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	private ChangeNamespace(String[] args) throws IOException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error reading config
	 */
	private ChangeNamespace(ArgList argList) throws IOException {
		this(
			JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")), 
			JenaConnect.parseConfig(argList.get("v"), argList.getValueMap("V")), 
			argList.get("u"), 
			argList.get("n"), 
			argList.has("e")
		);
	}
	
	/**
	 * Constructor
	 * @param model model to change uris in
	 * @param vivo model in which to search for previously used uris
	 * @param oldName old namespace
	 * @param newName new namespace
	 * @param errorLog log error messages for changed nodes
	 */
	public ChangeNamespace(JenaConnect model, JenaConnect vivo, String oldName, String newName, boolean errorLog) {
		if(model == null) {
			throw new IllegalArgumentException("No input model provided! Must provide an input model");
		}
		this.model = model;
		if(vivo == null) {
			throw new IllegalArgumentException("No vivo model provided! Must provide a vivo model");
		}
		this.vivo = vivo;
		this.oldNamespace = oldName;
		this.newNamespace = newName;
		this.errorLogging = errorLog;
		
		this.model.printParameters();
		this.vivo.printParameters();
	}
	
	/**
	 * Gets an unused URI in the the given namespace for the given models
	 * @param namespace the namespace
	 * @param models models to check in
	 * @return the uri
	 * @throws IOException error connecting
	 */
	public static String getUnusedURI(String namespace, JenaConnect... models) throws IOException {
		if((namespace == null) || namespace.equals("")) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		String uri = null;
		Random random = new Random();
		while(uri == null) {
			uri = namespace + "n" + random.nextInt(Integer.MAX_VALUE);
			log.trace("evaluating uri <" + uri + ">");
			for(JenaConnect model : models) {
				boolean modelContains = model.containsURI(uri);
				log.trace("model <" + model.getModelName() + "> contains this uri?: " + modelContains);
				if(modelContains) {
					uri = null;
					break;
				}
			}
		}
		log.debug("Using new URI: <" + uri + ">");
		return uri;
	}
	
	/**
	 * Changes the namespace for all matching uris
	 * @param model the model to change namespaces for
	 * @param vivo the model to search for uris in
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @param errorLog log error messages for changed nodes
	 * @throws IOException error connecting
	 */
	public static void changeNS(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, boolean errorLog) throws IOException {
		if((oldNamespace == null) || oldNamespace.trim().equals("")) {
			throw new IllegalArgumentException("old namespace cannot be empty");
		}
		if((newNamespace == null) || newNamespace.trim().equals("")) {
			throw new IllegalArgumentException("new namespace cannot be empty");
		}
		if(oldNamespace.trim().equals(newNamespace.trim())) {
			log.trace("namespaces are equal, nothing to change");
			return;
		}
		batchRename(model, vivo, oldNamespace.trim(), newNamespace.trim(), errorLog);
	}
	
	/**
	 * Rename unmatched resources from a given namespace in the given model to another (vivo) model
	 * @param model the model to change namespaces for
	 * @param vivo the model to search for uris in
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @param errorLog log error messages for changed nodes
	 * @throws IOException error connecting
	 */
	private static void batchRename(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, boolean errorLog) throws IOException {
		//Grab all resources matching namespaces needing changed
		String subjectQuery = "" + 
		"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + 
		"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" + 
		"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> \n" + 
		"PREFIX owl:   <http://www.w3.org/2002/07/owl#> \n" + 
		"PREFIX swrl:  <http://www.w3.org/2003/11/swrl#> \n" + 
		"PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#> \n" + 
		"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" + 
		"PREFIX bibo: <http://purl.org/ontology/bibo/> \n" + 
		"PREFIX dcelem: <http://purl.org/dc/elements/1.1/> \n" + 
		"PREFIX dcterms: <http://purl.org/dc/terms/> \n" + 
		"PREFIX event: <http://purl.org/NET/c4dm/event.owl#> \n" + 
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" + 
		"PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#> \n" + 
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n" + 
		"PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> \n" + 
		"PREFIX core: <http://vivoweb.org/ontology/core#> \n" + 
		"\n" + 
		"SELECT ?sub \n" + 
		"WHERE {\n" + 
		"\t" + "?sub ?p ?o . \n" + 
		"\t" + "FILTER regex(str(?sub), \"" + oldNamespace + "\" ) \n" + "} ORDER BY ?sub";
		log.debug("Change Query:\n" + subjectQuery);
		
		Set<String> changeArray = new TreeSet<String>();
		ResultSet results = model.executeSelectQuery(subjectQuery,true,false);
		for(QuerySolution solution : IterableAdaptor.adapt(results)) {
			String renameURI = solution.getResource("sub").getURI();
			changeArray.add(renameURI);
		}
		int total = changeArray.size();
		int count = 0;
		for(String sub : changeArray) {
			count++;
			Resource res = model.getJenaModel().getResource(sub);
			float percent = Math.round(10000f * count / total) / 100f;
			log.trace("(" + count + "/" + total + ": " + percent + "%): Finding unused URI for resource <" + res + ">");
			String uri = getUnusedURI(newNamespace, vivo, model);
			if(errorLog) {
				log.warn("Resource <" + res.getURI() + "> was found and renamed to new uri <" + uri + ">!");
			}
			renameResource(res, uri);
		}
		log.info("Changed namespace for " + changeArray.size() + " rdf nodes");
	}
	
	
    /**
     * <p>Answer a new resource that occupies the same position in the graph as the current
     * resource <code>old</code>, but that has the given URI.  In the process, the existing
     * statements referring to <code>old</code> are removed.  Since Jena does not allow the
     * identity of a resource to change, this is the closest approximation to a rename operation
     * that works.
     * </p>
     * <p><strong>Notes:</strong> This method does minimal checking, so renaming a resource
     * to its own URI is unpredictable.  Furthermore, it is a general and simple approach, and
     * in given applications it may be possible to do this operation more efficiently. Finally,
     * if <code>res</code> is a property, existing statements that use the property will not
     * be renamed, nor will occurrences of <code>res</code> in other models.
     * </p>
     * @param old An existing resource in a given model
     * @param uri A new URI for resource old, or <code>null</code> to rename old to a bNode
     * @return A new resource that occupies the same position in the graph as old, but which
     * has the new given URI.
     */
    private static Resource renameResource(final Resource old, final String uri) {
       	 // Let's work directly with the Graph. Faster if old is attached to a InfModel (~2 times).
       	 // Otherwise, we would create more or less many fine grained removals and additions of
       	 // statements on model which may cause to refresh the backing reasoner frequently, thus,
       	 // introducing a lot of update work. With this implementation this happens at most once.
       	 // It could be solved without the need to work directly with the graph if we had a bulk
       	 // update feature over Model/InfModel/OntModel.
       	 // Some tests have shown that even if there is just one triple involving the resource
       	 // to be renamed and the model has a reasonable size (~10000 triples) then it is still
       	 // faster (~30%) -- including the final rebind() -- than working at the level of model.
       	 final Node resAsNode = old.asNode();
       	 final Model model = old.getModel();
       	 final Graph graph = model.getGraph(), rawGraph;
       	 if (graph instanceof InfGraph) 
       	     rawGraph = ((InfGraph) graph).getRawGraph();
       	 else 
       	     rawGraph = graph;
    
       	 final Set<Triple> reflexiveTriples = new HashSet<Triple>();
    
       	 // list the statements that mention old as a subject
       	 ExtendedIterator<Triple> i = rawGraph.find(resAsNode, null, null);
    
       	 // List the statements that mention old as an object and filter reflexive triples.
       	 // Latter ones are found twice in each find method, thus, we need to make sure to
       	 // keep only one.
       	 i = i.andThen(rawGraph.find(null, null, resAsNode)).filterKeep(new Filter<Triple>() {
       		 @Override public boolean accept(final Triple o) {
       			 if (o.getSubject().equals(o.getObject())) {
       				 reflexiveTriples.add(o);
       				 return false;
       			 }
       			 return true;
       		 }
       	 });
    
       	 // create a new resource node to replace old
       	 final Resource newRes = model.createResource(uri);
       	 final Node newResAsNode = newRes.asNode();
    
       	 Triple t;
       	 Node subj, obj;
       	 while (i.hasNext())
       	 {
       		 t = i.next();
    
       		 // first, create a new triple to refer to newRes instead of old
       		 subj = (t.getSubject().equals(resAsNode))? newResAsNode : t.getSubject();
       		 obj = (t.getObject().equals(resAsNode))? newResAsNode : t.getObject();
       		 rawGraph.add(Triple.create(subj, t.getPredicate(), obj));
    
       		 // second, remove existing triple
       		 i.remove();
       	 }
    
       	 // finally, move reflexive triples (if any) <-- cannot do this in former loop as it
       	 // causes ConcurrentModificationException
       	 for (final Triple rt : reflexiveTriples)
       	 {
       		 rawGraph.delete(rt);
       		 rawGraph.add(Triple.create(newResAsNode, rt.getPredicate(), newResAsNode));
       	 }
    
       	 // Did we work in the back of the InfGraph? If so, we need to rebind raw data (more or less expensive)!
       	 if (rawGraph != graph) ((InfGraph) graph).rebind();
    
       	 return newRes;
    }
	
	/**
	 * Change namespace
	 * @throws IOException error connecting
	 */
	public void execute() throws IOException {
		changeNS(this.model, this.vivo, this.oldNamespace, this.newNamespace, this.errorLogging);
		this.model.sync();
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ChangeNamespace");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoModel").withParameter(true, "CONFIG_FILE").setDescription("config file for vivo jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivo jena model config using VALUE").setRequired(false));
		
		// Params
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("oldNamespace").withParameter(true, "OLD_NAMESPACE").setDescription("The old namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("newNamespace").withParameter(true, "NEW_NAMESPACE").setDescription("The new namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("errorLogging").setDescription("Log error messages for each record changed").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new ChangeNamespace(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
