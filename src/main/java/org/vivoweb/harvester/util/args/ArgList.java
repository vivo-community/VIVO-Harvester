/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.args;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parsed arguments from commandline and config files
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class ArgList {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ArgList.class);
	/**
	 * Argument set from commandline arguments
	 */
	private CommandLine oCmdSet;
	/**
	 * Map from configuration file
	 */
	private Map<String, List<String>> confMap;
	/**
	 * Argument Parse Mappings
	 */
	private ArgParser argParser;
	
	/**
	 * Constructor
	 * @param p parser
	 * @param args commandline args
	 * @throws IOException error parsing args
	 * @throws UsageException user requested usage message
	 */
	protected ArgList(ArgParser p, String[] args) throws IOException, UsageException {
		this(p, args, true);
	}
	
	/**
	 * Constructor
	 * @param p parser
	 * @param args commandline args
	 * @param logLines log lines
	 * @throws IOException error parsing args
	 * @throws UsageException user requested usage message
	 */
	protected ArgList(ArgParser p, String[] args, boolean logLines) throws IOException, UsageException {
		try {
			this.argParser = p;
			if(logLines) {
				log.debug("running " + p.getAppName());
				log.debug("command line args: " + getSanitizedArgString(args));
			}
			this.oCmdSet = new PosixParser().parse(this.argParser.getOptions(), args);
			if(this.oCmdSet.hasOption("help")) {
				throw new UsageException();
			}
			String[] confArgs = {""};
			if(this.oCmdSet.hasOption("X")) {
				this.confMap = new ConfigParser().parseConfig(this.oCmdSet.getOptionValue("X"));
				confArgs = new ConfigParser().configToArgs(this.oCmdSet.getOptionValue("X"));
				if(logLines) {
					log.debug("config file args: " + getSanitizedArgString(confArgs));
				}
			} else {
				this.confMap = null;
			}
			for(ArgDef arg : this.argParser.getArgDefs()) {
				if(arg.isRequired()) {
					String argName;
					if(arg.getShortOption() != null) {
						argName = arg.getShortOption().toString();
					} else {
						argName = arg.getLongOption();
					}
					if(!has(argName)) {
						throw new IllegalArgumentException("Missing Argument: " + argName);
					}
				}
			}
		} catch(ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Get the values for the given argdef
	 * @param argdef the argdef
	 * @return the values
	 */
	private List<String> getConfArgValues(ArgDef argdef) {
		List<String> retVal = new ArrayList<String>();
		Character shortOp = argdef.getShortOption();
		if((shortOp != null) && this.confMap.containsKey(shortOp.toString())) {
			retVal.addAll(this.confMap.get(shortOp.toString()));
		}
		String longOp = argdef.getLongOption();
		if((longOp != null) && this.confMap.containsKey(longOp)) {
			retVal.addAll(this.confMap.get(longOp));
		}
		if(retVal.isEmpty() || retVal.size() < 1) {
			return null;
		}
		return retVal;
	}
	
	/**
	 * Get the sanitized string of args
	 * @param args the args
	 * @return the sanitized string
	 */
	private String getSanitizedArgString(String[] args) {
		StringBuilder sb = new StringBuilder();
		String s;
		Set<String> filters = new HashSet<String>();
		filters.add("--inputUsername");
		filters.add("--inputPassword");
		filters.add("--outputUsername");
		filters.add("--outputPassword");
		filters.add("--username");
		filters.add("--password");
		filters.add("-.*dbUser");
		filters.add("-.*dbPass");
		for(int x = 0; x < args.length; x++) {
			if(x != 0) {
				sb.append(" ");
			}
			s = args[x];
			sb.append(s);
			for(String regex : filters) {
				if(s.matches(regex)) {
					sb.append(" ******");
					x++;
					break;
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Gets the value of the argument or default value if not set, null if no default value
	 * @param arg argument to get
	 * @return the value
	 */
	public String get(String arg) {
		ArgDef argdef = this.argParser.getOptMap().get(arg);
		if(argdef == null) {
			throw new IllegalArgumentException("No such parameter: "+arg);
		}
		if(!argdef.hasParameter()) {
			throw new IllegalArgumentException(arg + " has no parameters");
		}
		if(argdef.hasParameters()) {
			throw new IllegalArgumentException(arg + " potentially has more than one value, use getAll()");
		}
		if(argdef.isParameterValueMap()) {
			throw new IllegalArgumentException(arg + " is a value map parameter, use getValueMap()");
		}
		String retVal = null;
		if(this.oCmdSet.hasOption(arg)) {
			retVal = this.oCmdSet.getOptionValue(arg);
		} else {
			List<String> confVals = null;
			if((this.confMap != null) && ((confVals = getConfArgValues(argdef)) != null)) {
				if(confVals.size() > 1) {
					throw new IllegalArgumentException("Invalid config file: contains more than one value for parameter '"+argdef.getOptionString()+"'");
				}
				retVal = confVals.get(0);
			} else {
				retVal = this.argParser.getOptMap().get(arg).getDefaultValue();
			}
		}
		if(retVal != null) {
			retVal = retVal.trim();
		}
		return retVal;
	}
	
	/**
	 * Gets the value map for the argument
	 * @param arg argument to get
	 * @return the value map
	 */
	public Map<String, String> getValueMap(String arg) {
		ArgDef argdef = this.argParser.getOptMap().get(arg);
		if(argdef == null) {
			throw new IllegalArgumentException("No such parameter: "+arg);
		}
		if(!argdef.hasParameter()) {
			throw new IllegalArgumentException(arg + " has no parameters");
		}
		if(!argdef.isParameterValueMap()) {
			if(argdef.hasParameters()) {
				throw new IllegalArgumentException(arg + " is not a value map parameter, use getAll()");
			}
			throw new IllegalArgumentException(arg + " is not a value map parameter, use get()");
		}
		Map<String, String> p = new HashMap<String, String>();
		List<String> confVals = null;
		if((this.confMap != null) && ((confVals = getConfArgValues(argdef)) != null)) {
			for(String confVal : confVals) {
				String[] confValSplit = confVal.split("=", 2);
				if(confValSplit.length != 2) {
					throw new IllegalArgumentException("Invalid config file: contains non-value map (paramName=paramValue) value for parameter '"+argdef.getOptionString()+"'");
				}
				p.put(confValSplit[0].trim(), confValSplit[1].trim());
			}
		}
		Properties props = this.oCmdSet.getOptionProperties(arg);
		for(String prop : props.stringPropertyNames()) {
			p.put(prop.trim(), props.getProperty(prop).trim());
		}
		return p;
	}
	
	/**
	 * Gets the values of the argument (excluding a default value)
	 * @param arg argument to get
	 * @return the values
	 */
	public List<String> getAll(String arg) {
		return getAll(arg, false);
	}
	
	/**
	 * Gets the values of the argument
	 * @param arg argument to get
	 * @param includeDefaultValue should we include a default value if it exists?
	 * @return the values
	 */
	public List<String> getAll(String arg, boolean includeDefaultValue) {
		ArgDef argdef = this.argParser.getOptMap().get(arg);
		if(argdef == null) {
			throw new IllegalArgumentException("No such parameter: "+arg);
		}
		if(!argdef.hasParameter()) {
			throw new IllegalArgumentException(arg + " has no parameters");
		}
		if(!argdef.hasParameters()) {
			if(argdef.isParameterValueMap()) {
				throw new IllegalArgumentException(arg + " is a value map parameter, use getValueMap()");
			}
			throw new IllegalArgumentException(arg + " has only one parameter, use get()");
		}
		List<String> retVal = new LinkedList<String>();
		if(this.oCmdSet.hasOption(arg)) {
			for(String value : this.oCmdSet.getOptionValues(arg)) {
				retVal.add(value.trim());
			}
		}
		List<String> confVals = null;
		if((this.confMap != null) && ((confVals = getConfArgValues(argdef)) != null)) {
			for(String confVal : confVals) {
				retVal.add(confVal.trim());
			}
		}
		if((includeDefaultValue || retVal.isEmpty()) && argdef.hasDefaultValue()) {
			retVal.add(argdef.getDefaultValue().trim());
		}
		return retVal;
	}
	
	/**
	 * Determines if the given argument has been set or has a default value
	 * @param arg the argument
	 * @return true if a value has been set (from any of command line, config files, or default value)
	 */
	public boolean has(String arg) {
		ArgDef argdef = this.argParser.getOptMap().get(arg);
		if(argdef == null) {
			throw new IllegalArgumentException("No such parameter: "+arg);
		}
		if(this.oCmdSet.hasOption(arg)) {
			return true;
		} else if((this.confMap != null) && (getConfArgValues(argdef) != null)) {
			return true;
		} else if(argdef.hasDefaultValue()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Config Parser for Tasks
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private class ConfigParser extends DefaultHandler {
		/**
		 * The param list from config file
		 */
		private Map<String, List<String>> params;
		/**
		 * Temporary container for cdata
		 */
		private String tempVal;
		/**
		 * Temporary container for parameter id
		 */
		private String tempParamName;
		
		/**
		 * Default Constructor
		 */
		protected ConfigParser() {
			this.params = new HashMap<String, List<String>>();
			this.tempVal = "";
		}
		
		/**
		 * Converts the contents of a config file to commandline arguments
		 * @param filePath path to the config file
		 * @return equivalent commandline argument array
		 * @throws IOException error reading config file
		 */
		String[] configToArgs(String filePath) throws IOException {
			Map<String, List<String>> parameters = parseConfig(filePath);
			List<String> paramList = new LinkedList<String>();
			for(String key : parameters.keySet()) {
				for(String value : parameters.get(key)) {
					if(!value.equalsIgnoreCase("false")) {
						paramList.add("--" + key);
						if(!value.equalsIgnoreCase("true")) {
							paramList.add(value);
						}
					}
				}
			}
			return paramList.toArray(new String[]{});
		}
		
		/**
		 * Parses a configuration file describing a Task
		 * @param filename the name of the file to parse
		 * @return the Task described by the config file
		 * @throws IOException xml parsing error
		 */
		Map<String, List<String>> parseConfig(String filename) throws IOException {
			// get a factory
			SAXParserFactory spf = SAXParserFactory.newInstance();
			try {
				// get a new instance of parser
				SAXParser sp = spf.newSAXParser();
				// parse the file and also register this class for call backs
				sp.parse(FileAide.getInputStream(filename), this);
			} catch(ParserConfigurationException e) {
				throw new IOException(e);
			} catch(SAXException e) {
				throw new IOException(e);
			}
			return this.params;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.tempVal = "";
			this.tempParamName = "";
			if(qName.equalsIgnoreCase("Task") || qName.equalsIgnoreCase("Config")) {
				// Do nothing, but keep to prevent falling into else
			} else if(qName.equalsIgnoreCase("Param")) {
				this.tempParamName = attributes.getValue("name");
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal += new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("Task") || qName.equalsIgnoreCase("Config")) {
				// Do nothing, but leave it here so it doesn't fall into else statement
			} else if(qName.equalsIgnoreCase("Param")) {
				if(!this.params.containsKey(this.tempParamName)) {
					this.params.put(this.tempParamName, new LinkedList<String>());
				}
				this.params.get(this.tempParamName).add(this.tempVal);
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
	}
}
