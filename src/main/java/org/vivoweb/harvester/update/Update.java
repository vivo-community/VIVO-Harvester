/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.update;

import java.io.File;
import java.io.IOException;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.diff.Diff;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RDBJenaConnect;

/**
 *
 */
public class Update {

	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Update.class);
	/**
	 * Model to read previous records from
	 */
	private JenaConnect previousJC;
	/**
	 * Model to read incoming records from
	 */
	private JenaConnect incomingJC;
	/**
	 * Model to write records to
	 */
	private JenaConnect vivoJC;
	/**
	 * keep the subtractions
	 */
	private boolean ignoreSubtractions;
	/**
	 * keep the additions
	 */
	private boolean ignoreAdditions;
	/**
	 * wipe the incoming model
	 */
	private boolean wipeIncomingModel;
	/**
	 * Dump addition rdf to this file
	 */
	private String dumpAddFile;
	/**
	 * Dump subtraction rdf to this file
	 */
	private String dumpSubFile;
	
	/**
	 * Constructor
	 * @param prev Model to read previous records from
	 * @param in Model to read incoming records from
	 * @param vivo Model to write records to
	 * @param addDumpFile Dump addition rdf to this file
	 * @param subDumpFile Dump subtraction rdf to this file
	 * @param ignoreSub ignore the subtractions
	 * @param ignoreAdd ignore the additions
	 * @param wipeIn wipe the incoming model
	 */
	public Update(JenaConnect prev, JenaConnect in, JenaConnect vivo, String addDumpFile, String subDumpFile, boolean ignoreSub, boolean ignoreAdd, boolean wipeIn) {
		// Require input args
		if(prev == null || in == null) {
			throw new IllegalArgumentException("Must provide input and previous models");
		}
		this.incomingJC = in;
		this.previousJC = prev;
		this.vivoJC = vivo;
		this.dumpAddFile = addDumpFile;
		this.dumpSubFile = subDumpFile;
		this.ignoreAdditions = ignoreAdd;
		this.ignoreSubtractions = ignoreSub;
		this.wipeIncomingModel = wipeIn;
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public Update(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Update(ArgList argList) throws IOException {
		// Require input args
		if(!argList.has("p") || !argList.has("i")) {
			throw new IllegalArgumentException("Must provide input via -i and -p");
		}
		
		// setup incomingJC
		this.incomingJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("i")), argList.getProperties("I"));
		// setup previousJC
		this.previousJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("p")), argList.getProperties("P"));
		
		// setup vivoJC
		if(argList.has("v")) {
			this.vivoJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("v")), argList.getProperties("V"));
		} else {
			this.vivoJC = null;
		}
		
		this.dumpAddFile = argList.get("A");
		this.dumpSubFile = argList.get("S");
		
		this.ignoreAdditions = argList.has("a");
		this.ignoreSubtractions = argList.has("s");
		this.wipeIncomingModel = argList.has("w");
	}
	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Update");
		
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("previousModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("previousModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("incomingModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("incomingModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoModel").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('A').setLongOpt("addition-dump").withParameter(true, "DUMP_FILE").setDescription("dump file for output of addition rdf").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("subtraction-dump").withParameter(true, "DUMP_FILE").setDescription("dump file for output of subtraction rdf").setRequired(false));
		
		// switches
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("addition-ignore").withParameter(false, "NOADD_FLAG").setDescription("If set, this will prevent additions from being applied"));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("subtraction-ignore").withParameter(false, "NOSUB_FLAG").setDescription("If set, this will prevent subtractions from being applied"));
		parser.addArgument(new ArgDef().setShortOption('w').setLongOpt("wipe-incoming-model").withParameter(false, "WIPE_FLAG").setDescription("If set, this will clear the input model after transfer is complete"));
				
		return parser;
	}
	
	/**
	 * Execute the task
	 * @throws IOException error executing
	 * @throws ClassNotFoundException error loading class
	 */
	public void execute() throws IOException, ClassNotFoundException {
		JenaConnect subJC = new RDBJenaConnect("jdbc:h2:"+File.createTempFile("update_Subtractions", ".xml").getAbsolutePath()+";MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "subModel");
		JenaConnect addJC = new RDBJenaConnect("jdbc:h2:"+File.createTempFile("update_Additions", ".xml").getAbsolutePath()+";MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "addModel");
		
		if (this.previousJC == null){
			System.out.println("previous is null");
		}
		
		//run diff for subtractions previousJC - incomingJC  (IF BOOL NOT SET)
		log.info("Finding Subtractions");
		Diff.diff(this.previousJC, this.incomingJC,subJC, this.dumpSubFile);
		log.debug("Subtraction RDF:\n"+subJC.exportRdfToString());
			
		//run diff for additions incomingJC - previous jc
		log.info("Finding Additions");
		Diff.diff(this.incomingJC, this.previousJC, addJC, this.dumpAddFile);
		log.debug("Addition RDF:\n"+addJC.exportRdfToString());
	
		//if applyToVIVO
		if (this.vivoJC != null){
			if(!this.ignoreSubtractions) {
				log.info("Removing Subtractions from VIVO");
				this.vivoJC.removeRdfFromJC(subJC);
			}
			if(!this.ignoreAdditions) {
				log.info("Inputing Additions to VIVO");
				this.vivoJC.loadRdfFromJC(addJC);
			}
		}
		
		if(!this.ignoreSubtractions) {
			log.info("Removing Subtractions from previous harvest Model");
			this.previousJC.removeRdfFromJC(subJC);
		}
		if(!this.ignoreAdditions) {
			log.info("Inputing Additions to previous harvest Model");
			this.previousJC.loadRdfFromJC(addJC);
		}
		
		if (this.wipeIncomingModel){
			log.info("Wiping incoming Model");
			this.incomingJC.truncate();
		}
	}
	
	/**
	 * Main Method
	 * @param args commandline arguments
	 */
	public static void main(String[] args) {
		InitLog.initLogger(Update.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new Update(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}

}
