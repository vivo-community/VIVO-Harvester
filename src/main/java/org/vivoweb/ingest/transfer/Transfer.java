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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;
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
	private String dumpModel;
	/**
	 * keep model after transfer
	 */
	private boolean retainModel;
	
	/**
	 * Constructor
	 * @param in input Model
	 * @param out output Model
	 * @param inName input model name
	 * @param outName output model name
	 * @param file dump to file option
	 * @param keep keep transferred data option
	 */
	public Transfer(Model in, Model out, String inName, String outName, String file, boolean keep) {
	  this.input = in;
	  this.output = out;
	  this.inputModelName = inName;
	  this.outputModelName = outName;
	  this.dumpModel = file;
	  this.retainModel = keep;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Transfer(ArgList argList) throws IOException {
		//Require some args
		if ((!argList.has("o") && !argList.has("O") && !argList.has("d")) || !argList.has("i")) {
			throw new IllegalArgumentException("Must provide one of -o or -O, or -d in addition to -i");
		}
		String inConfig = argList.get("i");
		String outConfig = argList.get("o");
		try {
			//connect to proper model, if specified on command line
			if (this.inputModelName != null) {
				log.trace("Using  " + this.inputModelName + " for input Model");
				this.input = (new JenaConnect(JenaConnect.parseConfig(inConfig),this.inputModelName)).getJenaModel();
			} else {
				this.input = JenaConnect.parseConfig(inConfig).getJenaModel();
			}
			if (argList.has("o") || argList.has("O")) {
				//connect to proper model, if specified on command line
				if (this.outputModelName != null) {
					log.trace("Using  " + this.outputModelName + " for output Model");
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
			this.dumpModel = argList.get("d");
		}
		
		//empty model
		this.retainModel = argList.has("k");
	}
	
	/**
	 * Copy data from input to output
	 */
	private void transfer() {
		
		if (this.output != null) { 
			this.output.add(this.input);
		}
		
		//output to file, if requested
		if (this.dumpModel != null) { 
			log.trace("Outputting RDF to " + this.dumpModel);
			try {
				this.input.write(new FileOutputStream(this.dumpModel));
				//this.input.write(System.out);
			} catch (FileNotFoundException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				//TODO Nicholas: Fix Jena error
				//do nothing; currently bad xml will cause jena to throw error
			}	
		}
		
		//empty model
		if (!this.retainModel) {
			this.input.removeAll();
		}
	}
	
	/**
	 * Executes the task
	 */
	public void executeTask() {
		transfer();
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Transfer");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("input").withParameter(true, "MODEL_NAME").setDescription("model name for input (overrides config file)").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("output").withParameter(true, "MODEL_NAME").setDescription("model name for output (overrides config file)").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("dump file").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('k').setLongOpt("keep-transfered-model").withParameter(false, "cheese").setDescription("If set, this will not clear the input model after transfer is complete"));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		log.info("Transfer: Start");
		try {
			new Transfer(new ArgList(getParser(), args)).executeTask();
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
