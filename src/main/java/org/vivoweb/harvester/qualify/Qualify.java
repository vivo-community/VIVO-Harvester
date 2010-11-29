/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.HashSet;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
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
	private JenaConnect model;
	/**
	 * The data predicate
	 */
	private String dataPredicate;
	/**
	 * The string to match
	 */
	private String matchTerm;
	/**
	 * The value to replace it with
	 */
	private String newVal;
	/**
	 * Is this to use Regex to match the string
	 */
	private boolean regex;
	/**
	 * the namespace you want removed
	 */
	private String namespace;
	
	/**
	 * Constructor
	 * @param jenaModel the JENA model to run qualifications on
	 * @param dataType the data predicate
	 * @param matchString the string to match
	 * @param newValue the value to replace it with
	 * @param isRegex is this to use Regex to match the string
	 * @param rmNameSpace remove statements with predicates in this namespace
	 */
	public Qualify(JenaConnect jenaModel, String dataType, String matchString, String newValue, boolean isRegex, String rmNameSpace) {
		this.model = jenaModel;
		this.dataPredicate = dataType;
		this.matchTerm = matchString;
		this.newVal = newValue;
		this.regex = isRegex;
		this.namespace = rmNameSpace;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Qualify(ArgList argList) throws IOException {
		try {
			if(!((argList.has("r") ^ argList.has("t") ^ argList.has("p")))) {
				throw new IllegalArgumentException("Must provide one of --regex, --text, or --namespace, but not more than 1");
			}
			this.model = JenaConnect.parseConfig(argList.get("j"), argList.getProperties("J"));
			this.dataPredicate = argList.get("d");
			this.regex = argList.has("r");
			this.matchTerm = (this.regex ? argList.get("r") : argList.get("t"));
			this.newVal = argList.get("v");
			this.namespace = argList.get("p");
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		} catch(IOException e) {
			throw new IOException(e.getMessage(), e);
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
	 * Remove all predicates in a given namespace
	 * @param ns the namespace to remove all statements from
	 */
	private void rmNamespace(String ns) {
		String predicateQuery =	"SELECT ?s " +
				"WHERE " +
				"{ " +
				"?s ?p ?o .  " +
				"FILTER regex(str(?p), \"" + ns + "\" ) " + 
				"}";
		log.debug(predicateQuery);
		
		ResultSet propList = this.model.executeSelectQuery(predicateQuery);
		HashSet<Property> propArray = new HashSet<Property>();
		
		while (propList.hasNext()) {
			QuerySolution solution = propList.next();
			propArray.add(solution.getResource("p").as(Property.class));
		}
		
		for(Property p : propArray) {
			this.model.getJenaModel().removeAll(null, p, null);
		}
		
		this.model.getJenaModel().commit();
		log.info("Removed " + propArray.size() + " unique properties");
	}
	
	/**
	 * Executes the task
	 */
	public void executeTask() {
		if(!this.namespace.isEmpty()){
			log.info("Running remove namespace for " + this.namespace);
			rmNamespace(this.namespace);
		}
		
		if(this.regex) {
			log.info("Running Regex replace '" + this.dataPredicate + "': '" + this.matchTerm + "' with '" + this.newVal + "'");
			regexReplace(this.dataPredicate, this.matchTerm, this.newVal);
		} else {
			log.info("Running text replace '" + this.dataPredicate + "': '" + this.matchTerm + "' with '" + this.newVal + "'");
			strReplace(this.dataPredicate, this.matchTerm, this.newVal);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Qualify");
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jenaConfig").setDescription("config file for jena model").withParameter(true, "CONFIG_FILE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('J').setLongOpt("jenaOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dataType").setDescription("data type (rdf predicate)").withParameter(true, "RDF_PREDICATE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("regexMatch").setDescription("match this regex expression").withParameter(true, "REGEX").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("textMatch").setDescription("match this exact text string").withParameter(true, "MATCH_STRING").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("value").setDescription("replace matching record data with this value").withParameter(true, "REPLACE_VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("rmNamespace").setDescription("remove all statements where the predicate is of the given namespace").withParameter(true, "RDF_NAMESPACE").setRequired(false));
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
			new Qualify(new ArgList(getParser(), args)).executeTask();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
