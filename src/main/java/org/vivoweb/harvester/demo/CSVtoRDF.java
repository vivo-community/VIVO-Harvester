/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.demo;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.JDBCFetch;
import org.vivoweb.harvester.util.CSVtoJDBC;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * @author jrpence
 *
 */
public class CSVtoRDF {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JDBCFetch.class);
	/** */
	private CSVtoJDBC toDatabase;
	/** */
	private JDBCFetch fromDatabase;
	
	/**
	 * @param argList ArgList of command line arguments
	 * @throws IOException exception thrown if there is a problem with parsing the configs
	 */
	private CSVtoRDF(ArgList argList) throws IOException {
		this(argList.get("i"),RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")),argList.get("n"));
	}

	/**
	 * @param args array of command line arguments
	 * @throws IOException exception thrown if there is a problem with parsing the configs
	 * @throws UsageException user requested usage message
	 */
	private CSVtoRDF(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}

	/**
	 * @param CSVfilename Path and filename of the CSVfile
	 * @param output destination recordHandler
	 * @param uriNameSpace Name space to be used for the rdf elements 
	 * @throws IOException Exception for file access problems
	 */
	public CSVtoRDF(String CSVfilename, RecordHandler output, String uriNameSpace) throws IOException {
		this(FileAide.getInputStream(CSVfilename),output,uriNameSpace);
	}

	/**
	 * @param CSVfilestream An input stream of CSV data
	 * @param output destination recordHandler
	 * @param uriNameSpace Name space to be used for the rdf elements
	 * @throws IOException error
	 */
	public CSVtoRDF(InputStream CSVfilestream, RecordHandler output, String uriNameSpace) throws IOException {
		String driver = "org.h2.Driver";
		String connLine = "jdbc:h2:mem:TempCSVtoRDF";
		String user = "sa";
		String pass = "";
		String tablename = "csv";
		this.toDatabase = new CSVtoJDBC(CSVfilestream, driver, connLine, user, pass, tablename);
		this.fromDatabase = new JDBCFetch(driver, connLine, user, pass, output, uriNameSpace);
	}
	
	/**
	 * @throws IOException If there is an I/O problem during either execute
	 * 
	 */
	public void execute() throws IOException {
			this.toDatabase.execute();
			this.fromDatabase.execute();
	}
	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("CSVtoRDF");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "FILENAME").setDescription("csv file to be read into the Recordhandler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "NAMESPACE").setDescription("").setRequired(false));
		return parser;
	}
	
	/**
	 * @param args array of command line arguments
	 */
	public static void main(String[] args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new CSVtoRDF(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
}
