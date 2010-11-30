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
import org.vivoweb.harvester.util.repo.MemJenaConnect;
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
	 * remove rather than add
	 */
	private boolean removeMode;
	
	/**
	 * Constructor
	 * @param in input Model
	 * @param out output Model
	 * @param outputFile dump to file option
	 * @param clear clear transferred data option
	 * @param empty empty model before transfer
	 * @param removeMode remove rather than add
	 */
	public Transfer(JenaConnect in, JenaConnect out, String outputFile, boolean clear, boolean empty, boolean removeMode) {
		if(in == null) {
			throw new IllegalArgumentException("Must provide non-null input jena connect");
		}
		this.input = in;
		this.output = out;
		this.dumpFile = outputFile;
		this.clearModel = clear;
		this.emptyModel = empty;
		this.removeMode = removeMode;
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error parsing options
	 */
	public Transfer(String... args) throws IOException {
		this(new ArgList(getParser(), args));
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
			}
			
			// setup output
			if(argList.has("o")) {
				this.output = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("o")), argList.getProperties("O"));
			}
			
			// load any specified rdf file data
			this.inRDF = argList.get("r");
			this.inRDFlang = argList.get("R");
			
			// load data from recordhandler
			if(argList.has("h")) {
				this.inRH = RecordHandler.parseConfig(argList.get("h"), argList.getProperties("H"));
			}
			
			// output to file, if requested
			this.dumpFile = argList.get("d");
			
			// get namespace
			this.namespace = argList.get("n");
			
			// empty model
			this.clearModel = argList.has("w");
			this.emptyModel = argList.has("e");
			this.removeMode = argList.has("m");
			
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
			this.input = new MemJenaConnect();
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
			try {
				log.debug("removeMode?: " + this.removeMode);
				if(this.removeMode) {
					log.trace("removing inputfile from output");
					this.output.removeRDF(this.inRDF, this.namespace, this.inRDFlang);
				} else {
					log.trace("adding inputfile to input");
					this.input.loadRDF(this.inRDF, this.namespace, this.inRDFlang);
				}
			} catch(FileSystemException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		if(this.inRH != null) {
			if(this.removeMode) {
				log.info("Remving Records from RecordHandler");
				int processCount = this.output.removeRDF(this.inRH, this.namespace);
				log.info("Removed " + processCount + " records");
			} else {
				log.info("Loading Records from RecordHandler");
				int processCount = this.input.importRDF(this.inRH, this.namespace);
				log.info("Loaded " + processCount + " records");
			}
		}
		
		if(this.dumpFile != null) {
			try {
				log.trace("dumping input to dumpfile");
				this.input.exportRDF(this.dumpFile);
			} catch(FileSystemException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		if(!inputIsOutput) {
			if(this.removeMode) {
				log.trace("removing input from output");
				this.output.removeRDF(this.input);
			} else {
				log.trace("adding input to output");
				this.output.loadRDF(this.input);
			}
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
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("modeRemove").setDescription("remove rather than add").setRequired(false));
		
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
	 * clears all statements from a jena model
	 * @param jc the jena model
	 */
	public static void emptyJC(JenaConnect jc) {
		log.trace("Emptying Model");
		jc.getJenaModel().removeAll();
	}
	
	/**
	 * maybe be deprecated
	 * dumps the contents of a jena model into a file
	 * @param jc the jena model from which the other is removed
	 * @param remove the jena model to remove
	 */
	public static void removeJCFromJS(JenaConnect jc, JenaConnect remove) {
		jc.getJenaModel().remove(remove.getJenaModel());
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(Transfer.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new Transfer(args).execute();
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
