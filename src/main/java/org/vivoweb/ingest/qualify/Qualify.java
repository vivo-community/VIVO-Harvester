/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.qualify;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.xml.sax.SAXException;
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
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Qualify.class);
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
	 * The model name
	 */
	private String modelName;
	/**
	 * Is this to use Regex to match the string
	 */
	private boolean regex;
	
	/**
	 * Constructor
	 * @param jenaModel the JENA model to run qualifications on
	 * @param dataType the data predicate
	 * @param matchString the string to match
	 * @param newValue the value to replace it with
	 * @param withModelName the model name to connect to (will override jena config)
	 * @param isRegex is this to use Regex to match the string
	 * @throws IOException error connecting to model
	 */
	public Qualify(JenaConnect jenaModel, String dataType, String matchString, String newValue, String withModelName, boolean isRegex) throws IOException {
		if(withModelName != null) {
			this.model = new JenaConnect(jenaModel, withModelName);
		} else {
			this.model = jenaModel;
		}
		this.dataPredicate = dataType;
		this.matchTerm = matchString;
		this.newVal = newValue;
		this.modelName = withModelName;
		this.regex = isRegex;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Qualify(ArgList argList) throws IOException {
		if(!(argList.has("r") ^ argList.has("t"))) {
			throw new IllegalArgumentException("Must provide one of --regex or --text, but not both");
		}
		this.modelName = argList.get("n");
		setModel(argList.get("j"));
		this.dataPredicate = argList.get("d");
		this.regex = argList.has("r");
		this.matchTerm = (this.regex ? argList.get("r") : argList.get("t"));
		this.newVal = argList.get("v");
	}
	
	/**
	 * Setter for model
	 * @param configFileName the config file that describes the model to set
	 * @throws IOException error connecting to model
	 */
	private void setModel(String configFileName) throws IOException {
		try {
			// connect to proper model, if specified on command line
			if(this.modelName != null) {
				log.trace("Using " + this.modelName + " for input Model");
				this.model = new JenaConnect(JenaConnect.parseConfig(configFileName), this.modelName);
			} else {
				this.model = JenaConnect.parseConfig(configFileName);
			}
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
	 * Executes the task
	 */
	public void executeTask() {
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
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jenaConfig").setDescription("config file for jena model").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("modelName").setDescription("specify model to connect to. this requires you specify a jenaConfig and will override the jena config modelname").withParameter(true, "MODEL_NAME").setDefaultValue("staging"));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dataType").setDescription("data type (rdf predicate)").withParameter(true, "RDF_PREDICATE"));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("regexMatch").setDescription("match this regex expression").withParameter(true, "REGEX"));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("textMatch").setDescription("match this exact text string").withParameter(true, "MATCH_STRING"));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("value").setDescription("replace matching record data with this value").withParameter(true, "REPLACE_VALUE"));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		log.info(getParser().getAppName()+": Start");
		try {
			new Qualify(new ArgList(getParser(), args)).executeTask();
		} catch(IllegalArgumentException e) {
			log.fatal(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
