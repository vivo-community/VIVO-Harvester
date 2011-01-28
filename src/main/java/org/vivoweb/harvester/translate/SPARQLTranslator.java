/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.translate;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Takes XML Files and uses an XSL file to translate the data into the desired ontology
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class SPARQLTranslator {
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Logger log = LoggerFactory.getLogger(SPARQLTranslator.class);
	/**
	 * The translation file is the map that will reconstruct our input stream's document into the appropriate format
	 */
	// private String sparqlConstruct;
	/**
	 * in stream is the stream containing the file (xml) that we are going to translate
	 * @TODO possibly remove and switch to passing streams to xmlTranslate
	 */
	protected JenaConnect inputJC;
	/**
	 * out stream is the stream that the controller will be handling and were we will dump the translation
	 * @TODO possibly remove and switch to passing streams to xmlTranslate
	 */
	protected JenaConnect outputJC;
	/**
	 * 
	 */
	protected File sparqlFile;
	/**
	 * 
	 */
	protected RecordHandler outputRC;
	
	
	/**
	 * Default constructor
	 */
	@SuppressWarnings("unused")
	private SPARQLTranslator() {
		// empty constructor
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public SPARQLTranslator(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argumentList <ul>
	 * <li>translationFile the file that details the translation from the original xml to the target format</li>
	 * <li>inRecordHandler the files/records that require translation</li>
	 * <li>outRecordHandler the output record for the translated files</li>
	 * </ul>
	 * @throws IOException error reading files
	 */
	public SPARQLTranslator(ArgList argumentList) throws IOException {
		
		// setup input model
		if(argumentList.has("i")) {
			this.inputJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argumentList.get("i")), argumentList.getValueMap("I"));
		} else {
			this.inputJC = null;
		}
		
		// setup output
		if(argumentList.has("o")) {
			this.outputJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argumentList.get("o")), argumentList.getValueMap("O"));
		} else {
			this.outputJC = null;
		}
		
		// load data from recordhandler
		if(argumentList.has("h")) {
			this.outputRC = RecordHandler.parseConfig(argumentList.get("h"), argumentList.getValueMap("H"));
		} else {
			this.outputRC = null;
		}
		
		if(argumentList.has("s")){
			this.sparqlFile = new File(argumentList.get("s"));
		} else {
			this.sparqlFile = null;
		}		
	}
	
	/***
	 * checks again for the necessary file and makes sure that they exist
	 * @throws IOException error accessing file stream
	 */
	public void execute() throws IOException {
		// checking for valid input parameters
		log.info("Translation: Start");
		
		//build Sparl Query
		StringBuilder strQuery = new StringBuilder();
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(this.sparqlFile);
		} catch(FileNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		}
	    // Get the object of DataInputStream
	    DataInputStream in = new DataInputStream(fstream);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    //Read File Line By Line
	    while ((strLine = br.readLine()) != null)   {
	      // Print the content on the console
    		strQuery.append(strLine);	
	    }
	    //Close the input stream
	    in.close();
		
	    log.info(strQuery.toString());
	    ResultSet rs = this.inputJC.executeSelectQuery(strQuery.toString());
		
	    if (!rs.hasNext())
	    {
	    	log.info("Failed to find");
	    }
	    
	    while(rs.hasNext()) {
	    	QuerySolution qs = rs.next();
	    	log.info(qs.toString());
	    }
		
		
		log.info("Translation: End");
	}
	

	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SPARQLTranslator");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input record handler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of input recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output record handler").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("sparqlConstruct").withParameter(true, "SPARQL_CONSTRUCT_FILE").setDescription("the sparql construct to run").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('h').setLongOpt("outputToRH").withParameter(true, "RECORD_HANDLER").setDescription("output the sparql statment to a record handler").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('H').setLongOpt("outputToRHOverride").withParameterValueMap("RH_PARAM", "RECORD_HANDLER").setDescription("override the RH_PARAM of recordhandler using VALUE").setRequired(false));
		return parser;
	}
	
	/**
	 * Currently the main method accepts two methods of execution, file translation and record handler translation The
	 * main method actually passes its arg string to another method so that Translator can use this same method of
	 * execution
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(SPARQLTranslator.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new SPARQLTranslator(args).execute();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
}
