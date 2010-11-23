/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.util.ResourceUtils;

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
	private JenaConnect model;
	/**
	 * The old namespace
	 */
	private String oldNamespace;
	/**
	 * The new namespace
	 */
	private String newNamespace;
	/**
	 * the propeties to match on
	 */
	private List<Property> properties;
	/**
	 * The search model
	 */
	private JenaConnect vivo;

	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error reading config
	 * @throws SAXException error parsing config
	 * @throws ParserConfigurationException error parsing config 
	 */
	public ChangeNamespace(ArgList argList) throws ParserConfigurationException, SAXException, IOException {
		this.model = JenaConnect.parseConfig(argList.get("i"), argList.getProperties("I"));
		this.vivo = JenaConnect.parseConfig(argList.get("v"), argList.getProperties("V"));
		this.oldNamespace = argList.get("o");
		this.newNamespace = argList.get("n");
		List<String> predicates = argList.getAll("p");
		this.properties = new ArrayList<Property>(predicates.size());
		for (String pred : predicates) {
			this.properties.add(ResourceFactory.createProperty(pred));
		}
	}
	
	/**
	 * Get either a matching uri from the given model or an unused uri
	 * @param current the current resource
	 * @param namespace the namespace to match in
	 * @param properties the propeties to match on
	 * @param uriCheck list of new uris generated during this changenamespace run
	 * @param errorOnNewURI Log ERROR messages when a new URI is generated
	 * @param vivo the model to match in
	 * @param model model to check for duplicates
	 * @return the uri of the first matched resource or an unused uri if none found
	 */
	public static String getURI(Resource current, String namespace, List<Property> properties, ArrayList<String> uriCheck, boolean errorOnNewURI, JenaConnect vivo, JenaConnect model) {
		String uri = null;
		
		if (properties != null && !properties.isEmpty()) {
			uri = getMatchingURI(current, namespace, properties, vivo);
		}
		
		if (uri == null) {
			uri = getUnusedURI(namespace, uriCheck, vivo, model);
			if (errorOnNewURI) {
				log.error("Generated New Unused URI <"+uri+"> for rdf node <"+current.getURI()+">");
			}
		}
		log.debug("Using URI: <"+uri+">");
		return uri;
	}
	
	/**
	 * Gets an unused URI in the the given namespace for the given models
	 * @param namespace the namespace
	 * @param uriCheck list of new uris generated during this changenamespace run
	 * @param models models to check in
	 * @return the uri
	 * @throws IllegalArgumentException empty namespace
	 */
	public static String getUnusedURI(String namespace, ArrayList<String> uriCheck, JenaConnect... models) throws IllegalArgumentException {
		if (namespace == null || namespace.equals("")) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		String uri = null;
		Random random = new Random();
		while (uri == null) {
			uri = namespace + "n" + random.nextInt(Integer.MAX_VALUE);
			log.trace("uriCheck: "+uriCheck.contains(uri));
			if (uriCheck.contains(uri)) {
				uri = null;
			}
			for (JenaConnect model : models) {
				if (model.containsURI(uri)) {
					uri = null;
				}
			}
		}
		uriCheck.add(uri);
		log.debug("Using new URI: <"+uri+">");
		return uri;
	}
	
	/**
	 * Matches the current resource to a resource in the given namespace in the given model based on the given properties
	 * @param current the current resource
	 * @param namespace the namespace to match in
	 * @param properties the propeties to match on
	 * @param vivo the model to match in
	 * @return the uri of the first matched resource or null if none found
	 */
	public static String getMatchingURI(Resource current, String namespace, List<Property> properties, JenaConnect vivo) {
		List<String> uris = getMatchingURIs(current, namespace, properties, vivo);
		String uri = uris.isEmpty()?null:uris.get(0);
		if (uri != null) {
			log.debug("Matched URI: <"+uri+">");
		} else {
			log.debug("No Matched URI");
		}
		return uri;
	}
	
	/**
	 * Matches the current resource to resources in the given namespace in the given model based on the given properties
	 * @param current the current resource
	 * @param namespace the namespace to match in
	 * @param properties the propeties to match on
	 * @param vivo the model to match in
	 * @return the uris of the matched resources (empty set if none found)
	 */
	public static List<String> getMatchingURIs(Resource current, String namespace, List<Property> properties, JenaConnect vivo) {
		StringBuilder sbQuery = new StringBuilder();
		ArrayList<String> filters = new ArrayList<String>();
		int valueCount = 0;
		sbQuery.append("SELECT ?uri\nWHERE\n{");
		log.debug("properties size: "+properties.size());
		if (properties.size() < 1) {
			throw new IllegalArgumentException("No properties! SELECT cannot be created!");
		}
		for (Property p : properties) {
			StmtIterator stmntit = current.listProperties(p);
			if (!stmntit.hasNext()) {
				throw new IllegalArgumentException("Resource <"+current.getURI()+"> does not have property <"+p.getURI()+">! SELECT cannot be created!");
			}
			for (Statement s : IterableAdaptor.adapt(stmntit)) {
				sbQuery.append("\t?uri <");
				sbQuery.append(p.getURI());
				sbQuery.append("> ");
				if (s.getObject().isResource()) {
					sbQuery.append("<");
					sbQuery.append(s.getResource().getURI());
					sbQuery.append(">");
				} else {
					filters.add("( str(?value"+valueCount+") = \""+s.getLiteral().getValue().toString()+"\" )");
					sbQuery.append("?value");
					sbQuery.append(valueCount);
					valueCount++;
				}
				sbQuery.append(" .\n");
			}
		}
//		sbQuery.append("regex( ?uri , \"");
//		sbQuery.append(namespace);
//		sbQuery.append("\" )");
		if (!filters.isEmpty()) {
			sbQuery.append("\tFILTER (");
//			sbQuery.append(" && ");
			sbQuery.append(StringUtils.join(" && ", filters));
			sbQuery.append(")\n");
		}
		sbQuery.append("}");
		ArrayList<String> retVal = new ArrayList<String>();
		int count = 0;
		log.debug("Query:\n"+sbQuery.toString());
		log.debug("namespace: "+namespace);
		for (QuerySolution qs : IterableAdaptor.adapt(vivo.executeQuery(sbQuery.toString()))) {
			Resource res = qs.getResource("uri");
			if (res == null) {
				throw new IllegalArgumentException("res is null! SELECT for resource <"+current.getURI()+"> is most likely corrupted!");
			}
			String resns = res.getNameSpace();
			if (resns.equals(namespace)) {
				String uri = res.getURI();
				retVal.add(uri);
				log.debug("Matched URI["+count+"]: <"+uri+">");
				count++;
			}
		}
		return retVal;
	}
	
	/**
	 * Changes the namespace for all matching uris
	 * @param model the model to change namespaces for
	 * @param vivo the model to search for uris in
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @param properties the properties to match on
	 * @throws IllegalArgumentException empty namespace
	 */
	public static void changeNS(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, List<Property> properties) throws IllegalArgumentException {
		if (oldNamespace == null || oldNamespace.equals("")) {
			throw new IllegalArgumentException("old namespace cannot be empty");
		}
		if (newNamespace == null || newNamespace.equals("")) {
			throw new IllegalArgumentException("new namespace cannot be empty");
		}
		if (oldNamespace.equals(newNamespace)) {
			return;
		}
		ArrayList<String> uriCheck = new ArrayList<String>();
		int count = 0;
		String uri;
		Resource res;
		QuerySolution solution;

		if (properties.size() < 1) {
			throw new IllegalArgumentException("No properties! SELECT cannot be created!");
		}
		
		batchMatch(model, vivo, oldNamespace, newNamespace, properties, count);
		
		batchRename(model, vivo, oldNamespace, newNamespace, uriCheck, count);		
	}

	
	/**
	 * @param model
	 * @param vivo
	 * @param oldNamespace
	 * @param newNamespace
	 * @param uriCheck
	 * @param count
	 */
	private static void batchRename(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, ArrayList<String> uriCheck, int count) {
		String uri;
		Resource res;
		QuerySolution solution;
		//Grab all namespaces needing changed
		log.trace("Begin Change Query Build");
		String subjectQuery =	"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> " +
						"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> " +
						"PREFIX owl:   <http://www.w3.org/2002/07/owl#> " +
						"PREFIX swrl:  <http://www.w3.org/2003/11/swrl#> " +
						"PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#> " +
						"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> " +
						"PREFIX bibo: <http://purl.org/ontology/bibo/> " +
						"PREFIX dcelem: <http://purl.org/dc/elements/1.1/> " +
						"PREFIX dcterms: <http://purl.org/dc/terms/> " +
						"PREFIX event: <http://purl.org/NET/c4dm/event.owl#> " +
						"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
						"PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#> " +
						"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
						"PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> " +
						"PREFIX core: <http://vivoweb.org/ontology/core#> " +
						"SELECT ?sNew " +
						"WHERE " +
						"{ " +
						"?sNew ?p ?o .  " +
						"FILTER regex(str(?sNew), \"" + oldNamespace + "\" ) " + 
						"}";
		log.debug(subjectQuery);
		log.trace("End Change Query Build");
		
		log.trace("Begin Execute Query");
		ResultSet changeList = model.executeQuery(subjectQuery);
		ArrayList<String> changeArray = new ArrayList<String>();
		log.trace("End Execute Query");
		
		log.trace("Begin Rename Changes");
		while (changeList.hasNext()) {
			solution = changeList.next();
			changeArray.add(solution.getResource("sNew").toString());
			count++;
		}
		
		for(int i = 0; i < count; i++) {
			res = model.getJenaModel().getResource(changeArray.get(i));	
			uri = getUnusedURI(newNamespace, uriCheck, vivo, model);
			ResourceUtils.renameResource(res, uri);
		}
		log.info("Changed namespace for "+count+" rdf nodes");
		log.trace("End Rename Changes");
	}

	/**
	 * @param model
	 * @param vivo
	 * @param oldNamespace
	 * @param newNamespace
	 * @param properties
	 * @param count
	 * @return
	 */
	private static void batchMatch(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, List<Property> properties, int count) {
		Resource res;
		QuerySolution solution;
		log.trace("Begin Match Query Build");
		
		//Find all namespace matches
		StringBuilder sQuery =	new StringBuilder("PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
								"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> " +
								"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> " +
								"PREFIX owl:   <http://www.w3.org/2002/07/owl#> " +
								"PREFIX swrl:  <http://www.w3.org/2003/11/swrl#> " +
								"PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#> " +
								"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> " +
								"PREFIX bibo: <http://purl.org/ontology/bibo/> " +
								"PREFIX dcelem: <http://purl.org/dc/elements/1.1/> " +
								"PREFIX dcterms: <http://purl.org/dc/terms/> " +
								"PREFIX event: <http://purl.org/NET/c4dm/event.owl#> " +
								"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
								"PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#> " +
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
								"PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> " +
								"PREFIX core: <http://vivoweb.org/ontology/core#> " +
								"SELECT ?sNew ?sOld  " +
								"WHERE " +
								"{ ");

		int counter = 0;
		for (Property p : properties) {
				sQuery.append("\t?sNew <");
				sQuery.append(p.getURI());
				sQuery.append("> ");
				sQuery.append("?o" + counter );
				sQuery.append(" .\n");
				sQuery.append("\t?sOld <");
				sQuery.append(p.getURI());
				sQuery.append("> ");
				sQuery.append("\t?o" + counter );
				sQuery.append(" .\n");
		}
		
		sQuery.append("FILTER regex(str(?sNew), \"");
		sQuery.append(oldNamespace + "\" ) .");
		sQuery.append("FILTER regex(str(?sOld), \"");
		sQuery.append(newNamespace + "\" ) }");
		
		log.debug(sQuery.toString());
		log.trace("End Match Query Build");
		
		log.trace("Begin Union Model");
		Model unionModel = model.getJenaModel().union(vivo.getJenaModel());
		log.trace("End Union Model");
		
		log.trace("Begin Run Query to ResultSet");
		Query query = QueryFactory.create(sQuery.toString(), Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, unionModel);
		ResultSet matchList = queryExec.execSelect();
		log.trace("End Run Query to ResultSet");
		
		log.trace("Begin Rename Matches");
		ArrayList<String[]> uriArray = new ArrayList<String[]>();
		String matchArray[];
				
		while (matchList.hasNext()) {
			solution = matchList.next();
			matchArray = new String[2];
			matchArray[0] = solution.getResource("sNew").toString();
			matchArray[1] = solution.getResource("sOld").toString();
			uriArray.add(matchArray);
			count++;
		}
		
		for(int i = 0; i < count; i++) {
			matchArray = uriArray.get(i);
			res = model.getJenaModel().getResource(matchArray[0]);		
			ResourceUtils.renameResource(res, matchArray[1]);
		}		
		
		log.info("Matched namespace for "+count+" rdf nodes");
		log.trace("Begin Rename Matches");
		count = 0;
	}
	
	/**
	 * Change namespace
	 */
	private void execute() {
		changeNS(this.model, this.vivo, this.oldNamespace, this.newNamespace, this.properties);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ChangeNamespace");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoModel").withParameter(true, "CONFIG_FILE").setDescription("config file for vivo jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivo jena model config using VALUE").setRequired(false));
		
		// Params
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("oldNamespace").withParameter(true, "OLD_NAMESPACE").setDescription("The old namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("newNamespace").withParameter(true, "NEW_NAMESPACE").setDescription("The new namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("predicate").withParameters(true, "MATCH_PREDICATE").setDescription("Predicate to match on").setRequired(true));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(ChangeNamespace.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new ChangeNamespace(new ArgList(getParser(), args)).execute();
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
