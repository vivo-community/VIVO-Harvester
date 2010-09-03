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
import java.io.ByteArrayInputStream;
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
import org.vivoweb.ingest.util.repo.Record;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Model;

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
	private Model input;
	/**
	 * Model to write records to
	 */
	private Model output;
	/**
	 * input model name
	 */
	private String inputModelName;
	/**
	 * output model name
	 */
	private String outputModelName;
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
	 * @param inName input model name
	 * @param outName output model name
	 * @param file dump to file option
	 * @param keep keep transferred data option
	 * @param empty empty model before transfer
	 */
	public Transfer(Model in, Model out, String inName, String outName, String file, boolean keep, boolean empty) {
	  this.input = in;
	  this.output = out;
	  this.inputModelName = inName;
	  this.outputModelName = outName;
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
		//Require some args
		if ((!argList.has("o") && !argList.has("O") && !argList.has("d")) || !argList.has("i") && !argList.has("r") && !argList.has("h")) {
			throw new IllegalArgumentException("Must provide one of -o or -O, or -d in addition to -i or -r");
		}
		String inConfig = argList.get("i");
		String inRDF = argList.get("r");
		String outConfig = argList.get("o");
		this.inputModelName = argList.get("I");
		this.outputModelName = argList.get("O");
		
		try {
			if (inRDF != null) {
				log.info("Loading RDF " + inRDF + " for input");
				this.input = new JenaConnect(VFS.getManager().resolveFile(new File("."), inRDF).getContent().getInputStream()).getJenaModel();
			} else if (argList.has("h")) {
				//Read in records that need processing
				int processCount = 0;
				RecordHandler rh = RecordHandler.parseConfig(argList.get("h"));
				log.info("Loading Records from RecordHandler as input");
				for (Record r: rh) {
					log.trace("Record " + r.getID() + " added to incoming processing model");
					this.input = new JenaConnect().getJenaModel();
					this.input.read(new ByteArrayInputStream(r.getData().getBytes()), null);
					r.setProcessed(this.getClass());
					processCount += 1;
				}
				log.info("Loaded " + processCount + " records");
			} else {
				//connect to proper model, if specified on command line
				if (this.inputModelName != null) {
					log.info("Using " + this.inputModelName + " for input Model");
					this.input = (new JenaConnect(JenaConnect.parseConfig(inConfig),this.inputModelName)).getJenaModel();
				} else {
					this.input = JenaConnect.parseConfig(inConfig).getJenaModel();
				}
			}
			if (argList.has("o") || argList.has("O")) {
				//connect to proper model, if specified on command line
				if (this.outputModelName != null) {
					log.info("Using " + this.outputModelName + " for output Model");
					this.output = (new JenaConnect(JenaConnect.parseConfig(outConfig),this.outputModelName)).getJenaModel();
				} else {
					this.output = JenaConnect.parseConfig(outConfig).getJenaModel();
				}
			}
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
					this.output.removeAll();
				}
				this.output.add(this.input);
			}
			
			//output to file, if requested
			if (this.dumpFile != null) {
				if (this.input != null) {
					log.info("Outputting RDF to " + this.dumpFile);
					try {
						this.input.write(new FileOutputStream(this.dumpFile));
						//this.input.write(System.out);
					} catch (FileNotFoundException e) {
						//TODO Auto-generated catch block
						log.error(e.getMessage(),e);
					} catch (Exception e) {
						log.error(e.getMessage(),e);
						//TODO Nicholas: Fix Jena error
						//do nothing; currently bad xml will cause jena to throw error
					}
				} else {
					log.info("Dump Model option not valid when input is RDF file");
				}
			}
			
			//empty model
			if (!this.retainModel) {
				log.trace("Emptying Model");
				this.input.removeAll();
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
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("input-model").withParameter(true, "MODEL_NAME").setDescription("model name for input (overrides config file)").setRequired(false).setDefaultValue("staging"));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rdf").withParameter(true, "MODEL_NAME").setDescription("rdf filename for input").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('h').setLongOpt("recordHandler").withParameter(true, "RECORD_HANDLER").setDescription("record handler for input").setRequired(false));
		
		//Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("output-model").withParameter(true, "MODEL_NAME").setDescription("model name for output (overrides config file)").setRequired(false));
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
