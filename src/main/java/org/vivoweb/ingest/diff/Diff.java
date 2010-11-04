/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.diff;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * Set math to find difference (subtraction) of one model from another
 * @author Stephen Williams
 */
public class Diff {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Diff.class);
	/**
	 * Models to read records from
	 */
	private JenaConnect minuendJC;
	/**
	 * Models to read records from
	 */
	private JenaConnect subtrahendJC;
	/**
	 * Model to write records to
	 */
	private JenaConnect output;
	/**
	 * dump model option
	 */
	private String dumpFile;
	
	
	public Diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC){
		this.minuendJC = mJC;
		this.subtrahendJC = sJC;
		this.output = oJC;
	}
	
	public Diff(JenaConnect mJC, JenaConnect sJC, String dF){
		this.minuendJC = mJC;
		this.subtrahendJC = sJC;
		this.dumpFile = dF;
	}
	
	
	/**
	 * Constructor
	 * @param argList parsed commandline arguments
	 * @throws IOException error reading config files
	 */
	public Diff(ArgList argList) throws IOException {
		// Require inputs args
		if(!argList.has("s") || !argList.has("m")) {
			throw new IllegalArgumentException("Must provide input via -s and -m");
		}
		// Require output args
		if(!argList.has("o") && !argList.has("d")) {
			throw new IllegalArgumentException("Must provide one of -o or -d");
		}
		
		try {
			// setup input model
			if(argList.has("s")) {
				this.subtrahendJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("s")), argList.getProperties("S"));
			} else {
				this.subtrahendJC = null;
			}
			
			// setup subtrahend Model (b)
			if(argList.has("m")) {
				this.minuendJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("m")), argList.getProperties("M"));
			} else {
				this.minuendJC = null;
			}
			
			// setup output
			if(argList.has("o")) {
				this.output = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("o")), argList.getProperties("O"));
			} else {
				this.output = null;
			}
			
			// output to file, if requested
			if(argList.has("d")) {
				this.dumpFile = argList.get("d");
			} else {
				this.dumpFile = null;
			}
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Diff");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("minuend").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('M').setLongOpt("minuendOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("subtrahend").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("subtrahendOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("filename for output").setRequired(false));
		
		return parser;
	}
	
	public static void diff(JenaConnect mJC,JenaConnect sJC,JenaConnect oJC, String dF){
		/*
		 * c - b = a minuend - subtrahend = difference ie minuend.diff(subtrahend) = differenece c.diff(b) = a
		 */
		Model diffModel = ModelFactory.createDefaultModel();
		Model minuendModel = mJC.getJenaModel();
		Model subtrahendModel = sJC.getJenaModel();
		
		diffModel = minuendModel.difference(subtrahendModel);
		
		
		try {
			if(dF != null) {
				RDFWriter fasterWriter = diffModel.getWriter("RDF/XML");
				fasterWriter.setProperty("showXmlDeclaration", "true");
				fasterWriter.setProperty("allowBadURIs", "true");
				fasterWriter.setProperty("relativeURIs", "");
				OutputStreamWriter osw = new OutputStreamWriter(VFS.getManager().resolveFile(new File("."), dF).getContent().getOutputStream(false), Charset.availableCharsets().get("UTF-8"));
				fasterWriter.write(diffModel, osw, "");
				log.debug("RDF/XML Data was exported");
			} 
			if (oJC != null){
				oJC.getJenaModel().add(diffModel);
			}
		} catch(Exception e) {
			log.fatal(e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	public void execute() {
		diff(this.minuendJC, this.subtrahendJC, this.output, this.dumpFile);		
	}
	
	/**
	 * Main Method
	 * @param args commandline arguments
	 */
	public static void main(String[] args) {
		log.info(getParser().getAppName() + ": Start");
		try {
			new Diff(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.fatal(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
			// System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(), e);
		}
		log.info(getParser().getAppName() + ": End");
	}
}