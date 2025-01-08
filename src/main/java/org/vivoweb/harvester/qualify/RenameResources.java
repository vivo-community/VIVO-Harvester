/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;

/**
 * Changes the namespace for all matching uris
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class RenameResources {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RenameResources.class);
	/**
	 * The resource uri to which old uri will be renamed
	 */
	private String newUri;
	/**
	 * The resource uri to be renamed to the new uri
	 */
	private String[] oldUris;
	/**
	 * The jena model
	 */
	private JenaConnect jena;
	
	/**
	 * Commandline Constructor
	 * @param args commandline arguments
	 * @throws IOException error parsing args
	 * @throws UsageException user requested usage message
	 */
	private RenameResources(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Arg Parser Constructor
	 * @param argList parsed commandling arguments
	 * @throws IOException error connecting to jena model
	 */
	private RenameResources(ArgList argList) throws IOException {
		this(JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")), argList.get("n"), argList.getAll("u").toArray(new String[]{}));
	}
	
	/**
	 * Constructor
	 * @param jena the jena model
	 * @param newUri resource uri to which old uri will be renamed
	 * @param oldUris The resource uri to be renamed to the new uri
	 */
	public RenameResources(JenaConnect jena, String newUri, String... oldUris) {
		this.jena = jena;
		this.newUri = newUri;
		this.oldUris = oldUris;
		if(this.jena == null) {
			throw new IllegalArgumentException("Must provide a jena model");
		}
	}
	
	/**
	 * Run the Merge
	 */
	public void execute() {
		for(String sec : this.oldUris) {
			renameResource(this.jena.getJenaModel().getResource(sec), this.newUri);
		}
		this.jena.sync();
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
	 * @author jena team - see org.apache.jena.util.ResourceUtils
	 * @author hainesc - fixed to not cause ConcurrentModificationException in TDB
	 */
	public static Resource renameResource(final Resource old, final String uri) {
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
		if(graph instanceof InfGraph)
			rawGraph = ((InfGraph)graph).getRawGraph();
		else
			rawGraph = graph;
		
		final Set<Triple> reflexiveTriples = new HashSet<Triple>();
		
		// list the statements that mention old as a subject
		ExtendedIterator<Triple> subjectTriples = rawGraph.find(resAsNode, null, null);
		// List the statements that mention old as an object
		ExtendedIterator<Triple> objectTriples = rawGraph.find(null, null, resAsNode);
		// combine these iterators
		ExtendedIterator<Triple> combinedTriples = subjectTriples.andThen(objectTriples);
		// Filter reflexive triples, which are found twice in each find method, thus, we need to make sure to keep only one.
		ExtendedIterator<Triple> filteredTriples = combinedTriples.filterKeep(
			(final Triple o) -> {
				if(o.getSubject().equals(o.getObject())) {
					reflexiveTriples.add(o);
					return false;
				}
				return true;
			}
		);
		
		// create a new resource node to replace old
		final Resource newRes = model.createResource(uri);
		final Node newResAsNode = newRes.asNode();
		
		Set<Triple> removeTriples = new HashSet<Triple>();
		Set<Triple> addTriples = new HashSet<Triple>();
		
		Node subj, obj;
		for(final Triple t : IterableAdaptor.adapt(filteredTriples)) {
			// first, create a new triple to refer to newRes instead of old and mark it for addition
			subj = (t.getSubject().equals(resAsNode)) ? newResAsNode : t.getSubject();
			obj = (t.getObject().equals(resAsNode)) ? newResAsNode : t.getObject();
			//rawGraph.add(Triple.create(subj, t.getPredicate(), obj));
			addTriples.add(Triple.create(subj, t.getPredicate(), obj));
			
			// second, mark existing triple for removal
			//filteredTriples.remove();
			removeTriples.add(t);
		}
		
		// finally, handle reflexive triples (if any)
		for(final Triple t : reflexiveTriples) {
			//rawGraph.delete(rt);
			removeTriples.add(t);
			//rawGraph.add(Triple.create(newResAsNode, rt.getPredicate(), newResAsNode));
			addTriples.add(Triple.create(newResAsNode, t.getPredicate(), newResAsNode));
		}
		
//		// remove all triples marked for removal
//		for(final Triple t : removeTriples) {
//			rawGraph.delete(t);
//		}
//		// add all triples marked for addition
//		for(final Triple t : addTriples) {
//			rawGraph.add(t);
//		}
		// BulkUpdateHandler removed in Jena3, use GraphUtil instead
		//BulkUpdateHandler buh = rawGraph.getBulkUpdateHandler();
		//buh.add(addTriples.iterator());
		//buh.delete(removeTriples.iterator());
		GraphUtil.add(rawGraph, addTriples.iterator());
		GraphUtil.delete(rawGraph, removeTriples.iterator());
		
		// Did we work in the back of the InfGraph? If so, we need to rebind raw data (more or less expensive)!
		if(rawGraph != graph)
			((InfGraph)graph).rebind();
		
		return newRes;
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("RenameResources");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		
		// Params
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("new-uri").withParameter(true, "RESOURCE_URI").setDescription("The resource uri to which old uri will be renamed").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("old-uri").withParameters(true, "RESOURCE_URI").setDescription("The resource uri to be renamed to the new uri").setRequired(true));
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
			new RenameResources(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
