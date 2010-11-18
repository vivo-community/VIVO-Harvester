/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.update;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.diff.Diff;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.ibm.icu.util.Calendar;

/**
 *
 */
@SuppressWarnings("unused")
public class Update {

	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Update.class);
	/**
	 * Models to read records from
	 */
	private JenaConnect previousJC;
	/**
	 * Models to read records from
	 */
	private JenaConnect incomingJC;
	/**
	 * Model to write records to
	 */
	private JenaConnect vivoJC;
	/**
	 * keep the subtractions
	 */
	private boolean keepSubtractions;
	/**
	 * keep the additions
	 */
	private boolean keepAdditions;
	/**
	 * wipe the incoming model
	 */
	private boolean wipeIncomingModel;
	
	
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
		try {
			// setup incomingJC
			if(argList.has("i")) {
				this.incomingJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("i")), argList.getProperties("I"));
			} else {
				this.incomingJC = null;
			}
			
			// setup previousJC
			if(argList.has("p")) {
				this.previousJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("p")), argList.getProperties("P"));
			} else {
				this.previousJC = null;
			}
			
			// setup vivoJC
			if(argList.has("v")) {
				this.vivoJC = JenaConnect.parseConfig(VFS.getManager().resolveFile(new File("."), argList.get("v")), argList.getProperties("V"));
			} else {
				this.vivoJC = null;
			}
			
			this.wipeIncomingModel = argList.has("w");
			
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
		ArgParser parser = new ArgParser("Update");
		
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("previousModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("previousModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("incomingModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("incomingModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoModel").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		// switches
		// keepSubtractions (don't subtract)
		
		// keepAdditions
		
		// delete the incoming model
		parser.addArgument(new ArgDef().setShortOption('w').setLongOpt("wipe-incoming-model").withParameter(false, "WIPE_FLAG").setDescription("If set, this will clear the input model after transfer is complete"));
				
		return parser;
	}
	
	/**
	 * Execute the task
	 */
	public void execute() {
		try {
			JenaConnect subJC = new JenaConnect("jdbc:h2:"+File.createTempFile("update_Subtractions", ".xml").getAbsolutePath()+";MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "subModel");
			JenaConnect addJC = new JenaConnect("jdbc:h2:"+File.createTempFile("update_Additions", ".xml").getAbsolutePath()+";MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "addModel");
			
			if (this.previousJC == null){
				System.out.println("previous is null");
			}
			
			ByteArrayOutputStream baos;
			
			//run diff for subtractions previousJC - incomingJC  (IF BOOL NOT SET)
			log.info("Finding Subtractions");
			Diff.diff(this.previousJC, this.incomingJC,subJC,null);
			baos = new ByteArrayOutputStream();
			subJC.exportRDF(baos);
			baos.flush();
			log.debug("Subtraction RDF:\n"+baos.toString());
			subJC.exportRDF("logs/update_Subtractions.rdf.xml");
				
			//run diff for additions incomingJC - previous jc
			log.info("Finding Additions");
			Diff.diff(this.incomingJC, this.previousJC, addJC, null);
			baos = new ByteArrayOutputStream();
			addJC.exportRDF(baos);
			baos.flush();
			log.debug("Addition RDF:\n"+baos.toString());
			addJC.exportRDF("logs/update_Additions.rdf.xml");
		
			//if applyToVIVO
			if (this.vivoJC != null){
				log.info("Removing Subtractions from VIVO");
				this.vivoJC.getJenaModel().remove(subJC.getJenaModel());
				log.info("Inputing Additions to VIVO");
				this.vivoJC.getJenaModel().add(addJC.getJenaModel());
			}
			
			log.info("Removing Subtractions from harvester Model");
			this.previousJC.getJenaModel().remove(subJC.getJenaModel());
			log.info("Inputing Additions to harvester Model");
			this.previousJC.getJenaModel().add(addJC.getJenaModel());
			
			if (this.wipeIncomingModel){
				log.info("Wiping Input Model");
				this.incomingJC.getJenaModel().removeAll();
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
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
			new Update(new ArgList(getParser(), args)).execute();
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
