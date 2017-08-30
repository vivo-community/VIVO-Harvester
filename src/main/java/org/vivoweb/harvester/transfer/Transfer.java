/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.transfer;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * Transfer data from one Jena model to another
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class Transfer {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Transfer.class);
	/**
	 * Model to read records from
	 */
	private JenaConnect input;
	/**
	 * Model to write records to
	 */
	private JenaConnect output;
	/**
	 * dump model option
	 */
	private String dumpFile;
	/**
	 * input rdf file
	 */
	private String inRDF;
	/**
	 * the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or "TTL") and "N3".
	 * null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML"
	 */
	private String inRDFlang;
	/**
	 * input record handler
	 */
	private RecordHandler inRH;
	/**
	 * namespace for relative uri resolution
	 */
	private String namespace;
	/**
	 * remove rather than add
	 */
	private boolean removeMode;
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error parsing options
	 * @throws UsageException user requested usage message
	 */
	private Transfer(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	private Transfer(ArgList argList) throws IOException {
		// setup input model
		this.input = JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I"));
		
		// setup output
		this.output = JenaConnect.parseConfig(argList.get("o"), argList.getValueMap("O"));
		
		// load any specified rdf file data
		this.inRDF = argList.get("r");
		this.inRDFlang = argList.get("R");
		if (this.inRDFlang == null || this.inRDFlang.isEmpty()) {
			this.inRDFlang = "RDF/XML";
		}
		
		// load data from recordhandler
		this.inRH = RecordHandler.parseConfig(argList.get("s"), argList.getValueMap("S"));
		
		// output to file, if requested
		this.dumpFile = argList.get("d");
		
		// get namespace
		this.namespace = argList.get("n");
		 
		// remove mode
		this.removeMode = argList.has("m");
		
		// Require output args
		if(this.output == null && this.dumpFile == null) {
			throw new IllegalArgumentException("Must provide an output {-o, -O, or -d}");
		}
		
		// Check for weird flag combinations
		if(this.removeMode && this.output == null) {
			throw new IllegalArgumentException("Removing from nothing (no output model) is pointless");
		}
	}
	
	/**
	 * Copy data from input to output
	 * @throws IOException error
	 */
	private void execute() throws IOException {
		if(this.output == null) {
			this.output = new MemJenaConnect();
			log.debug("No Output Specified, Using In-Memory Jena Model As Temporary Model");
		}
		 
		if(this.removeMode) {
			if(this.input != null) {
				this.output.removeRdfFromJC(this.input);
			}
			if(this.inRDF != null) {
				this.output.removeRdfFromFile(this.inRDF, this.namespace, this.inRDFlang);
			}
			if(this.inRH != null) {
				int processed = this.output.removeRdfFromRH(this.inRH, this.namespace, this.inRDFlang);
				log.info(getParser().getAppName() + ": Records Removed: "+ processed);
			}
		} else {
			if(this.input != null) {
				this.output.loadRdfFromJC(this.input);
			}
			if(this.inRDF != null) {
				log.debug(getParser().getAppName() + ": language: "+ this.inRDFlang);
				this.output.loadRdfFromFile(this.inRDF, this.namespace, this.inRDFlang);
			}
			if(this.inRH != null) {
				log.debug(getParser().getAppName() + ": language: "+ this.inRDFlang);
				log.debug(getParser().getAppName() + ": namespace: "+ this.namespace);
				int processed = this.output.loadRdfFromRH(this.inRH, this.namespace, this.inRDFlang);
				log.info(getParser().getAppName() + ": Records loaded: "+ processed);				 
			}
		}
		
		
		if(this.dumpFile != null) {
			this.output.exportRdfToFile(this.dumpFile);
		}
		
		if(this.input != null) {
			this.input.sync();
		}
		if(this.output != null) {
			this.output.sync();
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Transfer");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for jena model to load into output model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rdf").withParameter(true, "RDF_FILE").setDescription("rdf filename to load into output model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('R').setLongOpt("rdfLang").withParameter(true, "LANGUAGE").setDescription("rdf language of rdf file").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("recordHandler").withParameter(true, "RECORD_HANDLER").setDescription("record handler to load into output model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("recordHandlerOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "URI_BASE").setDescription("use URI_BASE when importing relative uris").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("modeRemove").setDescription("remove from output model rather than add").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("filename into which output model should be dumped (as rdf/xml)").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new Transfer(args).execute();
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
