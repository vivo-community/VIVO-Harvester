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
package org.vivoweb.ingest.fetch;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.SpecialEntities;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.sparql.util.StringUtils;

import de.fuberlin.wiwiss.d2r.D2rProcessor;

/**
 * Fetches rdf data from a JDBC database
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class D2RMapFetch {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(D2RMapFetch.class);
	/**
	 * Record Handler to write records to
	 */
	private RecordHandler rh;
	/**
	 * Statement processor for the database
	 */
	private Statement cursor;
	/**
	 * Namespace for RDF made from this database
	 */
	private String uriNS;
	/**
	 * Prefix each field in query with this
	 */
	private String queryPre;
	/**
	 * Suffix each field in query with this
	 */
	private String querySuf;
	/**
	 * D2RMap config file path
	 */
	private String d2rConfigPath;
	/**
	 * D2RMap output file
	 */
	private String d2rOutputFile;
	
	/**
	 * Constructor
	 * @param dbConn connection to the database
	 * @param output RecordHandler to write data to
	 * @param uriNameSpace namespace base for rdf records
	 * @throws SQLException error talking with database
	 */
	public D2RMapFetch(Connection dbConn, RecordHandler output, String uriNameSpace) throws SQLException {
		this.cursor = dbConn.createStatement();
		this.rh = output;
		this.uriNS = uriNameSpace;
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("D2RMapFetch");
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("delimiterPrefix").withParameter(true, "DELIMITER").setDescription("Prefix each field in the query with this character").setDefaultValue("").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("delimiterSuffix").withParameter(true, "DELIMITER").setDescription("Suffix each field in the query with this character").setDefaultValue("").setRequired(false));
		// d2RMap specific
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("d2rMapConfigFile").withParameter(true, "D2RMAP_CONFIG_FILE").setDescription("D2RMap config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("d2rMapOutputFile").withParameter(true, "D2RMAP_OUTPUT_FILE").setDescription("D2RMap output file").setRequired(true));
		return parser;
	}
	
	/**
	 * Constructor
	 * @param opts option set of parsed args
	 * @throws IOException error creating task
	 */
	public D2RMapFetch(ArgList opts) throws IOException {
		this.queryPre = opts.get("delimiterPrefix");
		this.querySuf = opts.get("delimiterSuffix");
		this.d2rConfigPath = opts.get("r");
		this.d2rOutputFile = opts.get("s");
		try {
			this.rh = RecordHandler.parseConfig(opts.get("o"), opts.getProperties("O"));
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	/**
	 * Get the field prefix
	 * @return the field prefix
	 */
	private String getFieldPrefix() {
		if(this.queryPre == null) {
			this.queryPre = "";
		}
		return this.queryPre;
	}

	/**
	 * Set the field prefix
	 * @param fieldPrefix the field prefix to use
	 */
	public void setFieldPrefix(String fieldPrefix) {
		this.queryPre = fieldPrefix;
	}
	
	/**
	 * Get the field suffix
	 * @return the field suffix
	 */
	private String getFieldSuffix() {
		if(this.querySuf == null) {
			this.querySuf = "";
		}
		return this.querySuf;
	}

	/**
	 * Set the field suffix
	 * @param fieldSuffix the field suffix to use
	 */
	public void setFieldSuffix(String fieldSuffix) {
		this.querySuf = fieldSuffix;
	}
	
	/**
	 * Executes the task
	 */
	public void execute() {
		log.info("Fetch: Start");
		D2rProcessor proc = new D2rProcessor();
		proc.harvesterInit();
		try {
			String output = proc.processMap("RDF/XML", this.d2rConfigPath);
			this.rh.addRecord(this.d2rOutputFile, output, this.getClass());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("Fetch: End");
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new D2RMapFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(),e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}

