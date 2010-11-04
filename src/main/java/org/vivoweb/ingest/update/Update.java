/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.update;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 *
 */
@SuppressWarnings("unused")
public class Update {

	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Update.class);
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
	 * 
	 */
	private boolean applyToVIVO;
	/**
	 * 
	 */
	private boolean keepSubtractions;
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Diff");
		
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("previousModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("previousModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("incomingModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("incomingModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoModel").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));

		
		// switches
		// applyToVIVO Model (for before provinance)
		// keepSubtractions (don't subtract)
		// deleteIncomingModel
		
		return parser;
	}
	
	/**
	 * Execute the task
	 */
	public void execute() {
		Model subtractions = ModelFactory.createDefaultModel();
		Model additions = ModelFactory.createDefaultModel();
		
		Model previousModel = this.previousJC.getJenaModel();
		Model incomingModel = this.incomingJC.getJenaModel();
		
		//run diff for subtractions previousJC - incomingJC  (IF BOOL NOT SET)
		
		//run diff for additions incomingJC - previous jc
		
		//if applyToVIVO
		//subtract
		//add
		
		//apply to previous too
		
		//delete Incoming Model?
	}
	
	/**
	 * Main Method
	 * @param args commandline arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
