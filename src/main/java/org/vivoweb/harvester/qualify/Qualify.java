/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Qualify data using SPARQL queries
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class Qualify {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Qualify.class);
	/**
	 * Jena Model we are working in
	 */
	private final JenaConnect model;
	/**
	 * The data predicate
	 */
	private final String dataPredicate;
	/**
	 * The string to match
	 */
	private final String matchTerm;
	/**
	 * The value to replace it with
	 */
	private final String newVal;
	/**
	 * Is this to use Regex to match the string
	 */
	private final boolean regex;
	/**
	 * the namespace you want removed
	 */
	private final String namespace;
	/**
	 * remove all statements where the predicate is from the given namespace
	 */
	private final boolean cleanPredicates;
	/**
	 * remove all statements where the subject or object is from the given namespace
	 */
	private final boolean cleanResources;
	
	/**
	 * Constructor
	 * @param jenaModel the JENA model to run qualifications on
	 * @param dataType the data predicate
	 * @param matchString the string to match
	 * @param newValue the value to replace it with
	 * @param isRegex is this to use Regex to match the string
	 * @param removeNameSpace remove statements with predicates in this namespace
	 * @param cleanPredicates remove all statements where the predicate is from the given namespace
	 * @param cleanResources remove all statements where the subject or object is from the given namespace
	 */
	public Qualify(JenaConnect jenaModel, String dataType, String matchString, String newValue, boolean isRegex, String removeNameSpace, boolean cleanPredicates, boolean cleanResources) {
		this.model = jenaModel;
		this.dataPredicate = dataType;
		this.matchTerm = matchString;
		this.newVal = newValue;
		this.regex = isRegex;
		this.namespace = removeNameSpace;
		this.cleanPredicates = cleanPredicates;
		this.cleanResources = cleanResources;
		if(this.namespace == null || this.namespace.isEmpty()) {
			if(this.cleanPredicates && this.cleanResources) {
				throw new IllegalArgumentException("Cannot specify cleanPredicates and cleanResources when removeNamepsace is empty");
			}
			if(this.cleanPredicates) {
				throw new IllegalArgumentException("Cannot specify cleanPredicates when removeNamepsace is empty");
			}
			if(this.cleanResources) {
				throw new IllegalArgumentException("Cannot specify cleanResources when removeNamepsace is empty");
			}
		}
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public Qualify(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Qualify(ArgList argList) throws IOException {
		if(!((argList.has("r") ^ argList.has("t") ^ argList.has("p")))) {
			throw new IllegalArgumentException("Must provide one of --regex, --text, or --namespace, but not more than 1");
		}
		this.model = JenaConnect.parseConfig(argList.get("j"), argList.getValueMap("J"));
		this.dataPredicate = argList.get("d");
		this.regex = argList.has("r");
		this.matchTerm = (this.regex ? argList.get("r") : argList.get("t"));
		this.newVal = argList.get("v");
		this.namespace = argList.get("n");
		this.cleanPredicates = argList.has("p");
		this.cleanResources = argList.has("c");
		if(this.namespace == null || this.namespace.isEmpty()) {
			if(this.cleanPredicates && this.cleanResources) {
				throw new IllegalArgumentException("Cannot specify predicate-clean and clean-resources when remove-namepsace is empty");
			}
			if(this.cleanPredicates) {
				throw new IllegalArgumentException("Cannot specify predicate-clean when remove-namepsace is empty");
			}
			if(this.cleanResources) {
				throw new IllegalArgumentException("Cannot specify clean-resources when remove-namepsace is empty");
			}
		}
	}
	
	/**
	 * Replace records exactly matching uri & datatype & oldValue with newValue
	 * @param dataType data type to match
	 * @param oldValue old value to match
	 * @param newValue new value to set
	 */
	private void strReplace(String dataType, String oldValue, String newValue) {
		StmtIterator stmtItr = this.model.getJenaModel().listStatements(null, this.model.getJenaModel().createProperty(dataType), oldValue);
		
		while(stmtItr.hasNext()) {
			Statement stmt = stmtItr.next();
			log.trace("Replacing record");
			log.debug("oldValue: " + oldValue);
			log.debug("newValue: " + newValue);
			this.model.getJenaModel().add(stmt.getSubject(), stmt.getPredicate(), newValue);
			stmt.remove();
		}
	}
	
	/**
	 * Replace records matching dataType & the regexMatch with newValue
	 * @param dataType data type to match
	 * @param regexMatch regular expression to match
	 * @param newValue new value
	 */
	private void regexReplace(String dataType, String regexMatch, String newValue) {
		Property pred = this.model.getJenaModel().createProperty(dataType);
		ResIterator resItr = this.model.getJenaModel().listResourcesWithProperty(pred);
		
		while(resItr.hasNext()) {
			Statement stmt = resItr.next().getRequiredProperty(pred);
			String obj = stmt.getString();
			if(obj.matches(regexMatch)) {
				log.trace("Replacing record");
				log.debug("oldValue: " + obj);
				log.debug("newValue: " + newValue);
				this.model.getJenaModel().add(stmt.getSubject(), stmt.getPredicate(), newValue);
				stmt.remove();
			} else {
				log.debug("no match: " + obj);
			}
		}
	}
	
	/**
	 * Remove all subjects and objects in a given namespace
	 * @param ns the namespace to remove all resources from
	 */
	private void cleanResources(String ns) {
		String subjectQuery =	"SELECT ?s ?p ?o " +
				"WHERE " +
				"{ " +
				"?s ?p ?o .  " +
				"FILTER regex(str(?s), \"" + ns + "\" ) " + 
				"}";
		log.debug(subjectQuery);
		
		ResultSet subjList = this.model.executeSelectQuery(subjectQuery);
		HashSet<Resource> subjArray = new HashSet<Resource>();
		
		while (subjList.hasNext()) {
			subjArray.add(ResourceFactory.createResource(subjList.next().getResource("s").getURI()));
		}
		
		for(Resource s : subjArray) {
			this.model.getJenaModel().removeAll(s, null, null);
		}
		
		log.info("Removed " + subjArray.size() + " unique subjects");
		
		String objectQuery =	"SELECT ?s ?p ?o " +
				"WHERE " +
				"{ " +
				"?s ?p ?o .  " +
				"FILTER regex(str(?o), \"" + ns + "\" ) " + 
				"}";
		log.debug(objectQuery);
		
		ResultSet objList = this.model.executeSelectQuery(objectQuery);
		HashSet<Resource> objArray = new HashSet<Resource>();
		
		while (objList.hasNext()) {
			objArray.add(ResourceFactory.createResource(objList.next().getResource("o").getURI()));
		}
		
		for(Resource o : objArray) {
			this.model.getJenaModel().removeAll(null, null, o);
		}
		
		log.info("Removed " + objArray.size() + " unique objects");
	}
	
	/**
	 * Remove all predicates in a given namespace
	 * @param ns the namespace to remove all predicates from
	 */
	private void cleanPredicates(String ns) {
		String predicateQuery =	"SELECT ?s ?p ?o " +
				"WHERE " +
				"{ " +
				"?s ?p ?o .  " +
				"FILTER regex(str(?p), \"" + ns + "\" ) " + 
				"}";
		log.debug(predicateQuery);
		
		ResultSet propList = this.model.executeSelectQuery(predicateQuery);
		HashSet<Property> propArray = new HashSet<Property>();
		
		while (propList.hasNext()) {
			propArray.add(ResourceFactory.createProperty(propList.next().getResource("p").getURI()));
		}
		
		for(Property p : propArray) {
			this.model.getJenaModel().removeAll(null, p, null);
		}
		
		log.info("Removed " + propArray.size() + " unique properties");
	}
	
	/**
	 * Executes the task
	 */
	public void execute() {
		if(this.namespace != null && !this.namespace.isEmpty()){
			if(this.cleanPredicates) {
				log.info("Running clean predicates for " + this.namespace);
				cleanPredicates(this.namespace);
			}
			if(this.cleanResources) {
				log.info("Running clean resources for " + this.namespace);
				cleanResources(this.namespace);
			}
		}
		
		if(this.matchTerm != null && this.dataPredicate != null && this.newVal != null && !this.matchTerm.isEmpty() && !this.dataPredicate.isEmpty() && !this.newVal.isEmpty()) {
			if(this.regex) {
				log.info("Running Regex replace '" + this.dataPredicate + "': '" + this.matchTerm + "' with '" + this.newVal + "'");
				regexReplace(this.dataPredicate, this.matchTerm, this.newVal);
			} else {
				log.info("Running text replace '" + this.dataPredicate + "': '" + this.matchTerm + "' with '" + this.newVal + "'");
				strReplace(this.dataPredicate, this.matchTerm, this.newVal);
			}
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Qualify");
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jenaConfig").setDescription("config file for jena model").withParameter(true, "CONFIG_FILE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('J').setLongOpt("jenaOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dataType").setDescription("data type (rdf predicate)").withParameter(true, "RDF_PREDICATE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("regexMatch").setDescription("match this regex expression").withParameter(true, "REGEX").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("textMatch").setDescription("match this exact text string").withParameter(true, "MATCH_STRING").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("value").setDescription("replace matching record data with this value").withParameter(true, "REPLACE_VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("remove-namespace").setDescription("remove all statements where the predicate is of the given namespace").withParameter(true, "RDF_NAMESPACE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("predicate-clean").setDescription("remove all statements where the predicate is from the given -n/--remove-namespace").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("clean-resources").setDescription("remove all statements where the subject or object is from the given -n/--remove-namespace").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(Qualify.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new Qualify(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
