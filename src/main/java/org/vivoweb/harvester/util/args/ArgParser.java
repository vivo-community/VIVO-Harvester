/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.args;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Argument Description Listing
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ArgParser {
	/**
	 * The application this parser is meant for
	 */
	private String app;
	/**
	 * The argument definitions
	 */
	private Collection<ArgDef> options;
	/**
	 * Mapping of option flags to ArgDefs
	 */
	private Map<String, ArgDef> optMap;
	/**
	 * The Options made from this parser
	 */
	private Options parser;
	
	/**
	 * Default Constructor
	 * @param appName the application this parser is meant for
	 */
	public ArgParser(String appName) {
		this.app = appName;
		this.options = new LinkedList<ArgDef>();
		this.optMap = new HashMap<String, ArgDef>();
		this.parser = new Options();
		addArgument(new ArgDef().setShortOption('X').setLongOpt("config").withParameter(true, "CONFIG_FILE").setDescription("XML Configuration File"));
		addArgument(new ArgDef().setShortOption('h').setLongOpt("help").setDescription("Help Message"));
		addArgument(new ArgDef().setShortOption('w').setLongOpt("wordiness").setDescription("Set the console log level").withParameter(true, "LOG_LEVEL"));
	}
	
	/**
	 * Get the Options
	 * @return the Options
	 */
	protected Options getOptions() {
		if(this.parser == null) {
			createOptions();
		}
		return this.parser;
	}
	
	/**
	 * Get the argument definitions
	 * @return the argument definitions
	 */
	public Collection<ArgDef> getArgDefs() {
		return this.options;
	}
	
	/**
	 * Get the mapping of option flag to ArgDef
	 * @return the mapping
	 */
	public Map<String, ArgDef> getOptMap() {
		return this.optMap;
	}
	
	/**
	 * Get the application name
	 * @return the application name
	 */
	public String getAppName() {
		return this.app;
	}
	
	/**
	 * Add an Argument Definition to this Argument Parser
	 * @param arg the argument to add
	 */
	public void addArgument(ArgDef arg) {
		if((arg.getLongOption() == null) && (arg.getShortOption() == null)) {
			throw new IllegalArgumentException("must define at least a short or long option");
		}
		this.options.add(arg);
		createOptions();
	}
	
	/**
	 * Parse the commandline arguments
	 * @param args the commandline arguments
	 * @return the parsed arglist
	 * @throws IllegalArgumentException bad arguments provided
	 * @throws IOException error parsing args
	 * @throws UsageException user requested usage message
	 */
	public ArgList parse(String[] args) throws IllegalArgumentException, IOException, UsageException {
		return parse(args, true);
	}
	
	/**
	 * Parse the commandline arguments
	 * @param args the commandline arguments
	 * @param logLines log messages?
	 * @return the parsed arglist
	 * @throws IllegalArgumentException bad arguments provided
	 * @throws IOException error parsing args
	 * @throws UsageException user requested usage message
	 */
	public ArgList parse(String[] args, boolean logLines) throws IllegalArgumentException, IOException, UsageException {
		return new ArgList(this, args, logLines);
	}
	
	/**
	 * Create the Options from this ArgParser
	 */
	@SuppressWarnings("static-access")
	private void createOptions() {
		this.parser = new Options();
		for(ArgDef arg : getArgDefs()) {
			OptionBuilder ob = OptionBuilder.isRequired(false);
			if(arg.getLongOption() != null) {
				ob = ob.withLongOpt(arg.getLongOption());
				this.optMap.put(arg.getLongOption(), arg);
			}
			if(arg.getDescription() != null) {
				ob = ob.withDescription(arg.getDescription());
			}
			if(arg.hasParameter()) {
				ob = ob.withArgName(arg.getParameterDescription());
				int num = arg.numParameters();
				if(num == 1) {
					ob = ob.hasArg(arg.isParameterRequired());
				} else if(num == -1) {
					if(arg.isParameterRequired()) {
						ob = ob.hasArgs();
					} else {
						if(arg.isParameterValueMap()) {
							ob = ob.hasOptionalArgs(2).withValueSeparator();
						} else {
							ob = ob.hasOptionalArgs();
						}
					}
				} else {
					if(arg.isParameterRequired()) {
						ob = ob.hasArgs(num);
					} else {
						ob = ob.hasOptionalArgs(num);
					}
				}
			}
			Option o;
			if(arg.getShortOption() != null) {
				o = ob.create(arg.getShortOption().charValue());
				this.optMap.put(arg.getShortOption().toString(), arg);
			} else {
				o = ob.create();
			}
			this.parser.addOption(o);
		}
	}
	
	/**
	 * Get the usage message for this arg list
	 * @return the usage string
	 */
	public String getUsage() {
		HelpFormatter formatter = new HelpFormatter();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, this.app, null, getOptions(), HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, false);
		pw.flush();
		return baos.toString();
	}
}
