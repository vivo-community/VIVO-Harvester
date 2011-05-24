/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.diff;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * Set math to find difference (subtraction) of one model from another
 * @author Stephen Williams
 */
public class Diff {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Diff.class);
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
	
	/**
	 * Constructor
	 * @param mJC minuend jenaconnect
	 * @param sJC subtrahend jenaconnect
	 * @param oJC output jenaconnect
	 * @param dF dump file path
	 */
	public Diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, String dF) {
		this.minuendJC = mJC;
		this.subtrahendJC = sJC;
		this.output = oJC;
		this.dumpFile = dF;
		if(this.minuendJC == null) {
			throw new IllegalArgumentException("Must provide a minuend jena model");
		}
		if(this.subtrahendJC == null) {
			throw new IllegalArgumentException("Must provide a subtrahend jena model");
		}
		if(this.output == null && (this.dumpFile == null || this.dumpFile.trim().isEmpty())) {
			throw new IllegalArgumentException("Must provide at least one of an output jena model or a dump file");
		}
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error reading config files
	 */
	private Diff(String[] args) throws IOException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed commandline arguments
	 * @throws IOException error reading config files
	 */
	private Diff(ArgList argList) throws IOException {
		this(
			JenaConnect.parseConfig(argList.get("m"), argList.getValueMap("M")), 
			JenaConnect.parseConfig(argList.get("s"), argList.getValueMap("S")), 
			JenaConnect.parseConfig(argList.get("o"), argList.getValueMap("O")), 
			argList.get("d")
		);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Diff");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("minuend").withParameter(true, "CONFIG_FILE").setDescription("config file for source jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('M').setLongOpt("minuendOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of source jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("subtrahend").withParameter(true, "CONFIG_FILE").setDescription("config file for removemode jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("subtrahendOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of remove jena model config using VALUE").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("filename for output").setRequired(false));
		
		return parser;
	}
	
	/**
	 * Perform diff of mJC and sJC and put result in oJC and/or dF
	 * @param mJC minuend jenaconnect
	 * @param sJC subtrahend jenaconnect
	 * @param oJC output jenaconnect
	 * @param dF dump file path
	 * @throws IOException error accessing file
	 */
	public static void diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, String dF) throws IOException {
		/*
		 * c - b = a
		 * minuend - subtrahend = difference
		 * minuend.diff(subtrahend) = differenece
		 * c.diff(b) = a
		 */
		Model diffModel = ModelFactory.createDefaultModel();
		Model minuendModel = mJC.getJenaModel();
		Model subtrahendModel = sJC.getJenaModel();
		
		diffModel = minuendModel.difference(subtrahendModel);
		
		if(dF != null) {
			RDFWriter fasterWriter = diffModel.getWriter("RDF/XML");
			fasterWriter.setProperty("showXmlDeclaration", "true");
			fasterWriter.setProperty("allowBadURIs", "true");
			fasterWriter.setProperty("relativeURIs", "");
			OutputStreamWriter osw = new OutputStreamWriter(FileAide.getOutputStream(dF), Charset.availableCharsets().get("UTF-8"));
			fasterWriter.write(diffModel, osw, "");
			log.debug("RDF/XML Data was exported");
		}
		if(oJC != null) {
			oJC.getJenaModel().add(diffModel);
		}
	}
	
	/**
	 * Execute the diff
	 * @throws IOException error accessing file
	 */
	public void execute() throws IOException {
		diff(this.minuendJC, this.subtrahendJC, this.output, this.dumpFile);
	}
	
	/**
	 * Main Method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new Diff(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}