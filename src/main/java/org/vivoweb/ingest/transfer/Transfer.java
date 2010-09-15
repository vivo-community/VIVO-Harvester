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
package org.vivoweb.ingest.transfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.xml.sax.SAXException;

/**
 * Transfer data from one Jena model to another
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class Transfer {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Transfer.class);
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
	 * keep model after transfer
	 */
	private boolean retainModel;
	/**
	 * empty model before transfer
	 */
	private boolean emptyModel;
	
	/**
	 * Constructor
	 * @param in input Model
	 * @param out output Model
	 * @param file dump to file option
	 * @param keep keep transferred data option
	 * @param empty empty model before transfer
	 */
	public Transfer(JenaConnect in, JenaConnect out, String file, boolean keep, boolean empty) {
	  this.input = in;
	  this.output = out;
	  this.dumpFile = file;
	  this.retainModel = keep;
	  this.emptyModel = empty;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Transfer(ArgList argList) throws IOException {
		//Require input args
		if(!argList.has("r") && !argList.has("i") && !argList.has("h")) {
			throw new IllegalArgumentException("Must provide input via -i, -r, or -h");
		}
		//Require output args
		if (!argList.has("o") && !argList.has("O") && !argList.has("d")) {
			throw new IllegalArgumentException("Must provide one of -o, -O, or -d");
		}
		
		try {
			//setup input model
			if(argList.has("i")) {
				this.input = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("i")), argList.getProperties("I"));
			} else {
				this.input = new JenaConnect();
			}
			
			//load any specified rdf file data
			if(argList.has("r")) {
				String inRDF = argList.get("r");
				log.info("Loading RDF from " + inRDF);
				this.input.loadRDF(VFS.getManager().resolveFile(new File("."), inRDF).getContent().getInputStream());
			}
			
			//load data from recordhandler
			if(argList.has("h")) {
				//Read in records that need processing
				log.info("Loading Records from RecordHandler");
				int processCount = this.input.importRDF(RecordHandler.parseConfig(argList.get("h"), argList.getProperties("H")));
				log.info("Loaded " + processCount + " records");
			}
			
			//setup output
			this.output = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("o")), argList.getProperties("O"));
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		}
		
		//output to file, if requested
		if (argList.has("d")) {
			this.dumpFile = argList.get("d");
		}
		
		//empty model
		this.retainModel = argList.has("k");
		this.emptyModel = argList.has("e");
	}
	
	/**
	 * Copy data from input to output
	 */
	private void transfer() {
		
		if (this.input == null) {
			log.info("Input is empty, nothing to do");
		} else {
		
			if (this.output != null) { 
				if (this.emptyModel) {
					this.output.getJenaModel().removeAll();
				}
				this.output.getJenaModel().add(this.input.getJenaModel());
			}
			
			//output to file, if requested
			if (this.dumpFile != null) {
				if (this.input != null) {
					log.info("Outputting RDF to " + this.dumpFile);
					try {
						this.input.exportRDF(new FileOutputStream(this.dumpFile));
						//this.input.write(System.out);
					} catch (FileNotFoundException e) {
						log.error(e.getMessage(),e);
					} catch (Exception e) {
						log.error(e.getMessage(),e);
						//NOTTODO Nicholas: Fix Jena error
						//do nothing; currently bad xml will cause jena to throw error
					}
				} else {
					log.info("Dump Model option not valid when input is RDF file");
				}
			}
			
			//empty model
			if (!this.retainModel) {
				log.trace("Emptying Model");
				this.input.getJenaModel().removeAll();
			}
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Transfer");
		//Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rdf").withParameter(true, "MODEL_NAME").setDescription("rdf filename for input").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('h').setLongOpt("recordHandler").withParameter(true, "RECORD_HANDLER").setDescription("record handler for input").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('H').setLongOpt("recordHandlerOverride").withParameterProperties("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of recordhandler using VALUE").setRequired(false));
		
		//Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("filename for output").setRequired(false));
		
		//options
		parser.addArgument(new ArgDef().setShortOption('k').setLongOpt("keep-transfered-model").withParameter(false, "KEEP_FLAG").setDescription("If set, this will not clear the input model after transfer is complete"));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("empty-input-model").withParameter(false, "EMPTY_FLAG").setDescription("If set, this will clear the input model before transfer is started").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		log.info("Transfer: Start");
		try {
			new Transfer(new ArgList(getParser(), args)).transfer();
		} catch(IllegalArgumentException e) {
			log.fatal(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(IOException e) {
			log.fatal(e.getMessage());
			System.out.println(getParser().getUsage());	
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
		log.info("Transfer: End");
	}
}
