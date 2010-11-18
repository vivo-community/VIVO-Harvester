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
import com.hp.hpl.jena.query.QuerySolution;
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
	 * Log ERROR messages when a new URI is generated
	 */
	private boolean errorOnNewURI;

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
		this.errorOnNewURI = argList.has("e");
		List<String> predicates = argList.getAll("p");
		this.properties = new ArrayList<Property>(predicates.size());
		for(String pred : predicates) {
			this.properties.add(ResourceFactory.createProperty(pred));
		}
	}
	
	/**
	 * Get either a matching uri from the given model or an unused uri
	 * @param current the current resource
	 * @param namespace the namespace to match in
	 * @param properties the propeties to match on
	 * @param vivo the model to match in
	 * @param model models to check for duplicates
	 * @param errorOnNewURI Log ERROR messages when a new URI is generated
	 * @return the uri of the first matched resource or an unused uri if none found
	 */
	public static String getURI(Resource current, String namespace, List<Property> properties, JenaConnect vivo, JenaConnect model, boolean errorOnNewURI) {
		String uri = null;
		String matchURI = null;
		
		if (properties != null && !properties.isEmpty()) {
			matchURI = getMatchingURI(current, namespace, properties, vivo);
		}
		
		if (matchURI != null) {
			uri = matchURI;
		} else {
			uri = getUnusedURI(namespace, vivo, model);
			if(errorOnNewURI) {
				log.error("Generated New Unused URI <"+uri+"> for rdf node <"+current.getURI()+">");
			}
		}
		
		log.debug("Using URI: <"+uri+">");
		return uri;
	}
	
	/**
	 * Gets an unused URI in the the given namespace for the given models
	 * @param namespace the namespace
	 * @param vivo primary model to check in
	 * @param models additional models to check in
	 * @return the uri
	 * @throws IllegalArgumentException empty namespace
	 */
	public static String getUnusedURI(String namespace, JenaConnect vivo, JenaConnect... models) throws IllegalArgumentException {
		if(namespace == null || namespace.equals("")) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		String uri = null;
		Random random = new Random();
		while(uri == null) {
			uri = namespace + "n" + random.nextInt(Integer.MAX_VALUE);
			if(vivo.containsURI(uri)) {
				uri = null;
				log.debug("Collision with existing vivo uri");
			}
			for(JenaConnect model : models) {
				if(model.containsURI(uri)) {
					uri = null;
					log.debug("Collision with existing model uri");
				}
			}
		}
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
		log.debug("Matched URI: <"+uri+">");
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
		if(properties.size() < 1) {
			throw new IllegalArgumentException("No properties! SELECT cannot be created!");
		}
		for(Property p : properties) {
			StmtIterator stmntit = current.listProperties(p);
			if(!stmntit.hasNext()) {
				throw new IllegalArgumentException("Resource <"+current.getURI()+"> does not have property <"+p.getURI()+">! SELECT cannot be created!");
			}
			for(Statement s : IterableAdaptor.adapt(stmntit)) {
				sbQuery.append("\t?uri <");
				sbQuery.append(p.getURI());
				sbQuery.append("> ");
				if(s.getObject().isResource()) {
					sbQuery.append("<");
					sbQuery.append(s.getObject().asResource().getURI());
					sbQuery.append(">");
				} else {
					filters.add("( str(?value"+valueCount+") = \""+s.getObject().asLiteral().getValue().toString()+"\" )");
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
		if(!filters.isEmpty()) {
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
		for(QuerySolution qs : IterableAdaptor.adapt(vivo.executeQuery(sbQuery.toString()))) {
			Resource res = qs.getResource("uri");
			if(res == null) {
				throw new IllegalArgumentException("res is null! SELECT for resource <"+current.getURI()+"> is most likely corrupted!");
			}
			String resns = res.getNameSpace();
			if(resns.equals(namespace)) {
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
	 * @param properties the propeties to match on
	 * @param errorOnNewURI Log ERROR messages when a new URI is generated
	 * @throws IllegalArgumentException empty namespace
	 */
	public static void changeNS(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, List<Property> properties, boolean errorOnNewURI) throws IllegalArgumentException {
		if(oldNamespace == null || oldNamespace.equals("")) {
			throw new IllegalArgumentException("old namespace cannot be empty");
		}
		if(newNamespace == null || newNamespace.equals("")) {
			throw new IllegalArgumentException("new namespace cannot be empty");
		}
		if(oldNamespace.equals(newNamespace)) {
			return;
		}
		ArrayList<String> uriCheck = new ArrayList<String>();
		int count = 0;
		for(Resource res : IterableAdaptor.adapt(model.getJenaModel().listSubjects())) {
			if(oldNamespace.equals(res.getNameSpace())) {
				log.debug("Finding match for <"+res.getURI()+">");
				String uri = null;
				boolean uriFound = false;
				//find valid URI
				while (!uriFound) {
					uri = getURI(res, newNamespace, properties, vivo, model, errorOnNewURI);
					log.trace("urlCheck: "+uriCheck.contains(uri));
					//if matched in VIVO or not previously used
					if (vivo.containsURI(uri) || !uriCheck.contains(uri)) {
						//note URI as being used
						uriCheck.add(uri);
						//use this URI
						uriFound = true;
					}
				}
				ResourceUtils.renameResource(res, uri);
				count++;
			}
		}
		log.info("Changed namespace for "+count+" rdf nodes");
	}
	
	/**
	 * Change namespace
	 */
	private void execute() {
		changeNS(this.model, this.vivo, this.oldNamespace, this.newNamespace, this.properties, this.errorOnNewURI);
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
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("errorOnNewURI").setDescription("Log ERROR messages when a new URI is generated").setRequired(false));
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
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(IOException e) {
			log.error(e.getMessage(), e);
			// System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
