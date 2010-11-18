/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.transfer;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.xml.sax.SAXException;

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
	 * clear model after transfer
	 */
	private boolean clearModel;
	/**
	 * empty model before transfer
	 */
	private boolean emptyModel;
	/**
	 * input rdf file
	 */
	private String inRDF;
	/**
	 * the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML"
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
	 * Constructor
	 * @param in input Model
	 * @param out output Model
	 * @param outputFile dump to file option
	 * @param clear clear transferred data option
	 * @param empty empty model before transfer
	 */
	public Transfer(JenaConnect in, JenaConnect out, String outputFile, boolean clear, boolean empty) {
		if(in == null) {
			throw new IllegalArgumentException("Must provide non-null input jena connect");
		}
		this.input = in;
		this.output = out;
		this.dumpFile = outputFile;
		this.clearModel = clear;
		this.emptyModel = empty;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Transfer(ArgList argList) throws IOException {
		// Require input args
		if(!argList.has("r") && !argList.has("i") && !argList.has("h")) {
			throw new IllegalArgumentException("Must provide input via -i, -r, or -h");
		}
		// Require output args
		if(!argList.has("o") && !argList.has("O") && !argList.has("d")) {
			throw new IllegalArgumentException("Must provide one of -o, -O, or -d");
		}
		
		try {
			// setup input model
			if(argList.has("i")) {
				this.input = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("i")), argList.getProperties("I"));
			} else {
				this.input = null;
			}
			
			// setup output
			if(argList.has("o")) {
				this.output = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("o")), argList.getProperties("O"));
			} else {
				this.output = null;
			}
			
			// load any specified rdf file data
			if(argList.has("r")) {
				this.inRDF = argList.get("r");
				this.inRDFlang = argList.get("R");
			} else {
				this.inRDF = null;
				this.inRDFlang = null;
			}
			
			// load data from recordhandler
			if(argList.has("h")) {
				this.inRH = RecordHandler.parseConfig(argList.get("h"), argList.getProperties("H"));
			} else {
				this.inRH = null;
			}
			
			// output to file, if requested
			if(argList.has("d")) {
				this.dumpFile = argList.get("d");
			} else {
				this.dumpFile = null;
			}
			
			// get namespace
			if(argList.has("n")) {
				this.namespace = argList.get("n");
			} else {
				this.namespace = null;
			}
			
			// empty model
			this.clearModel = argList.has("w");
			this.emptyModel = argList.has("e");
			
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Copy data from input to output
	 */
	private void execute() {
		boolean newInput = false;
		if(this.dumpFile != null && this.input == null) {
			newInput = true;
		}
		if(this.emptyModel) {
			if(this.input == null) {
				newInput = true;
			} else {
				emptyJC(this.input);
			}
		}
		if(newInput && this.input == null) {
			this.input = new JenaConnect();
		}
		boolean inputIsOutput = false;
		if(this.input == null && this.output != null) {
			inputIsOutput = true;
			this.input = this.output;
		} else if(this.input != null && this.output == null) {
			inputIsOutput = true;
			this.output = this.input;
		}
		
		if(this.inRDF != null) {
			dumpFileToJC(this.inRDF, this.input, this.inRDFlang);
		}
		
		if(this.inRH != null) {
			dumpRHToJC(this.inRH, this.input);
		}
		
		if(this.dumpFile != null) {
			try {
				this.input.exportRDF(this.dumpFile);
			} catch(FileSystemException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		if(!inputIsOutput) {
			this.output.importRDF(this.input);
		}
		
		// empty model
		if(this.clearModel && !inputIsOutput) {
			emptyJC(this.input);
		}
		
		// close the models;
		if(this.input != null) {
			this.input.close();
		}
		if(this.output != null && !inputIsOutput) {
			this.output.close();
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Transfer");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rdf").withParameter(true, "RDF_FILE").setDescription("rdf filename for input").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('R').setLongOpt("rdfLang").withParameter(true, "LANGUAGE").setDescription("rdf language").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('h').setLongOpt("recordHandler").withParameter(true, "RECORD_HANDLER").setDescription("record handler for input").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('H').setLongOpt("recordHandlerOverride").withParameterProperties("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "URI_BASE").setDescription("use URI_BASE when importing relative uris").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("filename for output").setRequired(false));
		
		// options
		parser.addArgument(new ArgDef().setShortOption('w').setLongOpt("wipe-transfered-model").withParameter(false, "WIPE_FLAG").setDescription("If set, this will clear the input model after transfer is complete"));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("empty-input-model").withParameter(false, "EMPTY_FLAG").setDescription("If set, this will clear the input model before transfer is started").setRequired(false));
		return parser;
	}
	
	/**
	 * dumps the contents of a file into a jena model
	 * @param fileName the file to dump
	 * @param jc the jena model
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML"
	 */
	private void dumpFileToJC(String fileName, JenaConnect jc, String language) {
		try {
			log.info("Loading RDF from " + fileName);
			jc.loadRDF(VFS.getManager().resolveFile(new File("."), fileName).getContent().getInputStream(), this.namespace, language);
		} catch(FileSystemException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * dumps the contents of a record handler into a jena model
	 * @param rh the record handler to dump
	 * @param jc the jena model
	 */
	private void dumpRHToJC(RecordHandler rh, JenaConnect jc) {
		// Read in records that need processing
		log.info("Loading Records from RecordHandler");
		int processCount = jc.importRDF(rh, this.namespace);
		log.info("Loaded " + processCount + " records");
	}
	
	/**
	 * maybe be deprecated
	 * copies contents of a jena model into another jena model
	 * @param in the input model
	 * @param out the output model
	 */
	/*private void transferJCToJC(JenaConnect in, JenaConnect out) {
		out.getJenaModel().add(in.getJenaModel());
	}*/
	
	/**
	 * clears all statements from a jena model
	 * @param jc the jena model
	 */
	private void emptyJC(JenaConnect jc) {
		log.trace("Emptying Model");
		jc.getJenaModel().removeAll();
	}
	
	/**
	 * maybe be deprecated
	 * dumps the contents of a jena model into a file
	 * @param jc the jena model
	 * @param fileName the file to dump into
	 */
	/*private void dumpJCToFile(JenaConnect jc, String fileName) {
		log.info("Outputting RDF to " + fileName);
		try {
			jc.exportRDF(VFS.getManager().resolveFile(new File("."), fileName).getContent().getOutputStream(false));
		} catch(FileSystemException e) {
			log.error(e.getMessage(), e);
		}
	}*/
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(Transfer.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new Transfer(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(IOException e) {
			log.error(e.getMessage(), e);
			// System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
