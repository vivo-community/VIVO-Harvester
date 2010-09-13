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
package org.vivoweb.ingest.util.args;

import java.io.IOException;
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
	private Map<String,ArgDef> optMap;
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
		this.optMap = new HashMap<String,ArgDef>();
		this.parser = new Options();
		this.options.add(new ArgDef().setShortOption('X').setLongOpt("config").withParameter(true, "CONFIG_FILE").setDescription("XML Configuration File"));
		this.options.add(new ArgDef().setShortOption('?').setLongOpt("help").setDescription("Help Message"));
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
	public Map<String,ArgDef> getOptMap() {
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
		if(arg.getLongOption() == null && arg.getShortOption() == null) {
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
	 */
	public ArgList parse(String[] args) throws IllegalArgumentException, IOException {
		return new ArgList(this, args);
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
						if(arg.isParameterProperties()) {
							ob = ob.hasOptionalArgs(2).withValueSeparator();
						} else {
							ob = ob.hasOptionalArgs();
						}
					}
				} else {
					if (arg.isParameterRequired()) {
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
		formatter.printHelp(this.app, getOptions());
		return "";
	}
}
