/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
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
	 * The search model
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
	public ChangeNamespace(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param input input model
	 * @param output output model
	 * @param oldName old namespace
	 * @param newName new namespacey
	 * @param errorLog log error messages for changed nodes
	 */
	public ChangeNamespace(JenaConnect input,JenaConnect output, String oldName, String newName, boolean errorLog) {
		this.model = input;
		this.vivo = output;
		this.oldNamespace = oldName;
		this.newNamespace = newName;
		this.errorLogging = errorLog;
		
		this.model.printConnectionParameters();
		this.vivo.printConnectionParameters();
		//TODO Nicholas REMOVE DEBUG STATEMENTS
		//log.info("vivo size: " + this.vivo.size());
		//log.info("input size: " + this.model.size());
		//log.info("INPUT:\n" + this.model.exportRdfToString());
	}
	

	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error reading config
	 */
	public ChangeNamespace(ArgList argList) throws IOException {
		this.model = JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I"));
		this.vivo = JenaConnect.parseConfig(argList.get("v"), argList.getValueMap("V"));
		this.oldNamespace = argList.get("o");
		this.newNamespace = argList.get("n");
		this.errorLogging = argList.has("e");
		//TODO Nicholas REMOVE DEBUG STATEMENTS		
		//log.info("vivo size: " + this.vivo.size());
		//log.info("input size: " + this.model.size());
		//log.info("INPUT:\n" + this.model.exportRdfToString());
	}
	
	/**
	 * Gets an unused URI in the the given namespace for the given models
	 * @param namespace the namespace
	 * @param models models to check in
	 * @return the uri
	 * @throws IllegalArgumentException empty namespace
	 */
	public static String getUnusedURI(String namespace, JenaConnect... models) throws IllegalArgumentException {
		if (namespace == null || namespace.equals("")) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		String uri = null;
		Random random = new Random();
		while (uri == null) {
			uri = namespace + "n" + random.nextInt(Integer.MAX_VALUE);
			log.trace("evaluating uri <"+uri+">");
			for (JenaConnect model : models) {
				boolean modelContains = model.containsURI(uri);
				log.trace("model <"+model.getModelName()+"> contains this uri?: "+modelContains);
				if (modelContains) {
					uri = null;
					break;
				}
			}
		}
		log.debug("Using new URI: <"+uri+">");
		return uri;
	}
	
	/**
	 * Changes the namespace for all matching uris
	 * @param model the model to change namespaces for
	 * @param vivo the model to search for uris in
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @param errorLog log error messages for changed nodes
	 * @throws IllegalArgumentException empty namespace
	 */
	public static void changeNS(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, boolean errorLog) throws IllegalArgumentException {
		if (oldNamespace == null || oldNamespace.trim().equals("")) {
			throw new IllegalArgumentException("old namespace cannot be empty");
		}
		if (newNamespace == null || newNamespace.trim().equals("")) {
			throw new IllegalArgumentException("new namespace cannot be empty");
		}
		if (oldNamespace.trim().equals(newNamespace.trim())) {
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
	 */
	private static void batchRename(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, boolean errorLog) {
		//Grab all resources matching namespaces needing changed
		String subjectQuery =	""+
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
			"\t" + "FILTER regex(str(?sub), \"" + oldNamespace + "\" ) \n" + 
			"}";
		log.debug("Change Query:\n"+subjectQuery);
		
		ResultSet changeList = model.executeSelectQuery(subjectQuery);

		HashSet<String> changeArray = new HashSet<String>();
		for(QuerySolution solution : IterableAdaptor.adapt(changeList)) {
			String renameURI = solution.getResource("sub").getURI();
			changeArray.add(renameURI);
		}
		
		for(String sub : changeArray) {
			Resource res = model.getJenaModel().getResource(sub);
			log.trace("Finding unused URI for resource <" + res + ">");
			String uri = getUnusedURI(newNamespace, vivo, model);
			if(errorLog) {
				log.error("Resource <"+res.getURI()+"> was found and renamed to new uri <"+uri+">!");
			}
			ResourceUtils.renameResource(res, uri);
		}
		log.info("Changed namespace for " + changeArray.size() + " rdf nodes");
	}
	
	/**
	 * Change namespace
	 */
	public void execute() {
		changeNS(this.model, this.vivo, this.oldNamespace, this.newNamespace, this.errorLogging);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ChangeNamespace");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoModel").withParameter(true, "CONFIG_FILE").setDescription("config file for vivo jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivo jena model config using VALUE").setRequired(false));
		
		// Params
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("oldNamespace").withParameter(true, "OLD_NAMESPACE").setDescription("The old namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("newNamespace").withParameter(true, "NEW_NAMESPACE").setDescription("The new namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("errorLogging").setDescription("Log error messages for each record changed").setRequired(false));
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
			new ChangeNamespace(args).execute();
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
