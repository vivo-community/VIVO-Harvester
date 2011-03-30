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
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Find nodes with no name and give them a name
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class RenameBlankNodes {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ChangeNamespace.class);
	/**
	 * The model to perform rename in
	 */
	private final Model inModel;
	/**
	 * The part of the namespace between the base and the ID number
	 */
	private final String namespaceEtc;
	/**
	 * dedupModel
	 */
	private final Model dedupModel;
	/**
	 * pattern
	 */
	private final String pattern;
	/**
	 * property
	 */
	private final String property;
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public RenameBlankNodes(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param inModel The model to perform rename in
	 * @param namespaceEtc The part of the namespace between the base and the ID number
	 * @param dedupModel dedupModel
	 * @param pattern pattern
	 * @param property property
	 */
	public RenameBlankNodes(Model inModel, String namespaceEtc, Model dedupModel, String pattern, String property) {
		this.inModel = inModel;
		this.namespaceEtc = namespaceEtc;
		this.dedupModel = dedupModel;
		this.pattern = pattern;
		this.property = property;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error reading config
	 */
	public RenameBlankNodes(ArgList argList) throws IOException {
		this(
			JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")).getJenaModel(),
			argList.get("n"),
			argList.has("d") ? JenaConnect.parseConfig(argList.get("d"), argList.getValueMap("D")).getJenaModel() : null,
			argList.get("t"),
			argList.get("p")
		);
	}
	

	/**
	 * Rename blank nodes
	 */
	public void execute() {
		//todo: complete
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ChangeNamespace");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dedupModel").withParameter(true, "CONFIG_FILE").setDescription("optional: config file for dedup jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('D').setLongOpt("dedupModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of dedup jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespaceEtc").withParameter(true, "NAMESPACE_ETC").setDescription("part of the namespace between the base and the ID number").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("pattern").withParameter(true, "PATTERN").setDescription("pattern").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("property").withParameter(true, "PROPERTY").setDescription("property").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new RenameBlankNodes(args).execute();
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
