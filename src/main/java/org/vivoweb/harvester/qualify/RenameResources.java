/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * Changes the namespace for all matching uris
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class RenameResources {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RenameResources.class);
	/**
	 * The resource uri to which old uri will be renamed
	 */
	private String newUri;
	/**
	 * The resource uri to be renamed to the new uri
	 */
	private String[] oldUris;
	/**
	 * The jena model
	 */
	private JenaConnect jena;
	
	/**
	 * Commandline Constructor
	 * @param args commandline arguments
	 * @throws IOException error parsing args
	 */
	public RenameResources(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Arg Parser Constructor
	 * @param argList parsed commandling arguments
	 * @throws IOException error connecting to jena model
	 */
	public RenameResources(ArgList argList) throws IOException {
		this(JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")), argList.get("n"), argList.getAll("u").toArray(new String[]{}));
	}
	
	/**
	 * Constructor
	 * @param jena the jena model
	 * @param newUri resource uri to which old uri will be renamed
	 * @param oldUris The resource uri to be renamed to the new uri
	 */
	public RenameResources(JenaConnect jena, String newUri, String... oldUris) {
		this.jena = jena;
		this.newUri = newUri;
		this.oldUris = oldUris;
	}
	
	/**
	 * Run the Merge
	 */
	public void execute() {
		for(String sec : this.oldUris) {
			ResourceUtils.renameResource(this.jena.getJenaModel().getResource(sec), this.newUri);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("RenameResources");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		
		// Params
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("new-uri").withParameter(true, "RESOURCE_URI").setDescription("The resource uri to which old uri will be renamed").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("old-uri").withParameters(true, "RESOURCE_URI").setDescription("The resource uri to be renamed to the new uri").setRequired(true));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new RenameResources(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info(getParser().getAppName() + ": End");
		}
	}
}
