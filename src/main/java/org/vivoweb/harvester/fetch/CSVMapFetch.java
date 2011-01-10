/*******************************************************************************
 * Copyright (c) 2010 Eliza Chan All rights reserved. This program and the accompanying materials are made available
 * under the terms of the new BSD license which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html Contributors: Eliza Chan
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.RecordHandler;
import de.fuberlin.wiwiss.d2r.D2rProcessor;

/**
 * Fetches rdf data using D2RMap
 * @author Eliza Chan (elc2013@med.cornell.edu)
 */
public class CSVMapFetch {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(CSVMapFetch.class);
	/**
	 * Record Handler to write records to
	 */
	private RecordHandler rh;
	/**
	 * D2RMap config file path
	 */
	private String d2rConfigPath;
	/**
	 * D2RMap output file
	 */
	private String d2rOutputFile;
	/**
	 * D2RMap working directory
	 */
	private String d2rWDir;
	
	/**
	 * Constructor
	 * @param output RecordHandler to write data to
	 */
	public CSVMapFetch(RecordHandler output) {
		this.rh = output;
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("D2RMapFetch");
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		// d2RMap specific
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("d2rMapConfigFile").withParameter(true, "D2RMAP_CONFIG_FILE").setDescription("D2RMap config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("d2rMapOutputFile").withParameter(true, "D2RMAP_OUTPUT_FILE").setDescription("D2RMap output file").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("d2rMapWorkingDirectory").withParameter(true, "D2RMAP_WORKING_DIRECTORY").setDescription("D2RMap working directory").setRequired(true));
		return parser;
	}
	
	/**
	 * Constructor
	 * @param opts option set of parsed args
	 * @throws IOException error creating task
	 */
	public CSVMapFetch(ArgList opts) throws IOException {
		this.d2rConfigPath = opts.get("u");
		this.d2rOutputFile = opts.get("s");
		this.d2rWDir = opts.get("a");
		this.rh = RecordHandler.parseConfig(opts.get("o"), opts.getValueMap("O"));
	}
	
	/**
	 * Executes the task
	 */
	public void execute() {
		log.info("Fetch: Start");
		D2rProcessor proc = new D2rProcessor();
		proc.harvesterInit();
		try {
			String output = proc.processCsvMap("RDF/XML", this.d2rWDir, this.d2rConfigPath);
			this.rh.addRecord(this.d2rOutputFile, output, this.getClass());
				
			} 
		catch(Exception e) {
			System.err.println("CSVMapFetch errors: " + e);
		}

		log.info("Fetch: End");
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new CSVMapFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(), e);
		}
	}
}
