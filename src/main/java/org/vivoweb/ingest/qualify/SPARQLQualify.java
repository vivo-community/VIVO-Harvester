/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Qualify data using SPARQL queries
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class SPARQLQualify {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(SPARQLQualify.class);
	/**
	 * Jena Model we are working in
	 */
	private Model model;
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
	 * Constructor
	 * @param jenaModel the JENA model to run qualifications on
	 * @param dataType the data predicate
	 * @param matchString the string to match
	 * @param newValue the value to replace it with
	 * @param isRegex is this to use Regex to match the string
	 */
	public SPARQLQualify(Model jenaModel, String dataType, String matchString, String newValue, boolean isRegex) {
		this.model = jenaModel;
		this.dataPredicate = dataType;
		this.regex = isRegex;
		this.matchTerm = matchString;
		this.newVal = newValue;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public SPARQLQualify(ArgList argList) throws IOException {
		if(!(argList.has("r") ^ argList.has("t"))) {
			throw new IllegalArgumentException("Must provide one of --regex or --text, but not both");
		}
		setModel(argList.get("j"));
		this.dataPredicate = argList.get("d");
		this.regex = argList.has("r");
		this.matchTerm = (this.regex?argList.get("r"):argList.get("t"));
		this.newVal = argList.get("v");
	}
	
	/**
	 * Setter for model
	 * @param configFileName the config file that describes the model to set
	 * @throws IOException error connecting to model
	 */
	private void setModel(String configFileName) throws IOException {
		try {
			this.model = JenaConnect.parseConfig(configFileName).getJenaModel();
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		} catch(IOException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	/**
	 * Replace records exactly matching uri & datatype & oldValue with newValue
	 * @param uri uri to match
	 * @param dataType data type to match
	 * @param oldValue old value to match
	 * @param newValue new value to set
	 */
	private void strReplace(String uri, String dataType, String oldValue, String newValue) {
		log.trace("Running text replace '"+dataType+"':'"+oldValue+"' with '"+newValue+"'");
		// create query string
		String sQuery = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "DELETE { "+uri+" <"+dataType+"> ?value } "
				+ "INSERT { "+uri+" <"+dataType+"> \""+newValue+"\" } "
				+ "WHERE { "+uri+" <"+dataType+"> \""+oldValue+"\" }";
		
		// run update
		UpdateRequest ur = UpdateFactory.create(sQuery);
		UpdateAction.execute(ur, this.model);
	}

	/**
	 * Replace records matching dataType & the regexMatch with newValue
	 * @param dataType data type to match
	 * @param regexMatch regular expresion to match
	 * @param newValue new value
	 */
	private void regexReplace(String dataType, String regexMatch, String newValue) {
		log.trace("Running Regex replace '"+dataType+"':'"+regexMatch+"' with '"+newValue+"'");
		// create query string
		String sQuery = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "Select ?record ?dataField "
				+ "WHERE { "
				+ "  ?record <"+dataType+"> ?dataField . "
				+ "}";
		
		// create query
		Query query = QueryFactory.create(sQuery);
		
		// execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, this.model);
		ResultSet resultSet = qe.execSelect();
		
		// read first result
		String data = null;
		if(resultSet.hasNext()) {
			QuerySolution result = resultSet.next();
			data = result.getLiteral(resultSet.getResultVars().get(1)).getString();
			if(data.matches(regexMatch)) {
				String newData = data.replaceAll(regexMatch, newValue);
				log.trace("matching record found");
				log.debug("data: "+data);
				log.debug("newData: "+newData);
				if(!newData.equals(data)) {
					String record = result.getLiteral(resultSet.getResultVars().get(0)).getString();
					log.debug("Updating record");
					strReplace(record, dataType, data, newData);
				} else {
					log.debug("No update needed");
				}
			}
		}
	}
	
	/**
	 * Executes the task
	 */
	public void executeTask() {
		if(this.regex) {
			regexReplace(this.dataPredicate, this.matchTerm, this.newVal);
		} else {
			strReplace("?uri", this.dataPredicate, this.matchTerm, this.newVal);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SPARQLQualify");
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jenaConfig").setDescription("config file for jena model").withParameter(true, "CONFIG_FILE"));
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
		try {
			new SPARQLQualify(new ArgList(getParser(), args)).executeTask();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}
