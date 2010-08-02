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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parsed arguments from commandline and config files
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ArgList {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ArgList.class);
	/**
	 * Argument set from commandline arguments
	 */
	private CommandLine oCmdSet;
	/**
	 * Argument set from configuration file
	 */
	private CommandLine oConfSet;
	/**
	 * Argument Parse Mappings
	 */
	private ArgParser argParser;
	
	/**
	 * Constructor
	 * @param p parser
	 * @param args commandline args
	 * @throws IllegalArgumentException missing args
	 * @throws IOException error parsing args
	 */
	public ArgList(ArgParser p, String[] args) throws IllegalArgumentException, IOException {
		try {
			this.argParser = p;
			this.oCmdSet = new PosixParser().parse(this.argParser.getOptions(),args);
			if(this.oCmdSet.hasOption("help")) {
				String usage = this.argParser.getUsage();
				log.info(usage);
				System.out.println(usage);
			} else {
				String[] confArgs = {""};
				if(this.oCmdSet.hasOption("X")) {
					confArgs = new ConfigParser().configToArgs(this.oCmdSet.getOptionValue("X"));
					this.oConfSet = new PosixParser().parse(this.argParser.getOptions(), confArgs);
				} else {
					this.oConfSet = null;
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
							throw new IllegalArgumentException("Missing Argument: "+argName);
						}
					}
				}
			}
		} catch(SecurityException e) {
			throw new IOException(e.getMessage(), e);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		} catch(ParseException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the value of the argument or default value if not set
	 * @param arg argument to get
	 * @return the value
	 */
	public String get(String arg) {
		String retVal;
		if(this.oCmdSet.hasOption(arg)) {
			retVal = this.oCmdSet.getOptionValue(arg);
		} else {
			if(this.oConfSet != null && this.oConfSet.hasOption(arg)) {
				retVal = this.oConfSet.getOptionValue(arg);
			} else {
				retVal = this.argParser.getOptMap().get(arg).getDefaultValue();
			}
		}
		return retVal;
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
		List<String> retVal = new LinkedList<String>();
		if(this.oCmdSet.hasOption(arg)) {
			retVal.addAll(Arrays.asList(this.oCmdSet.getOptionValues(arg)));
		}
		if(this.oConfSet != null && this.oConfSet.hasOption(arg)) {
			retVal.addAll(Arrays.asList(this.oConfSet.getOptionValues(arg)));
		}
		if((includeDefaultValue || retVal.isEmpty()) && this.argParser.getOptMap().get(arg).hasDefaultValue()) {
			retVal.add(this.argParser.getOptMap().get(arg).getDefaultValue());
		}
		return retVal;
	}
	
	/**
	 * Determines if the given argument has been set or has a default value
	 * @param arg the argument
	 * @return true if a value has been set (from any of command line, config files, or default value)
	 */
	public boolean has(String arg) {
		return (this.oCmdSet.hasOption(arg) || (this.oConfSet != null && this.oConfSet.hasOption(arg)) || this.argParser.getOptMap().get(arg).hasDefaultValue());
	}
	
	/**
	 * Config Parser for Tasks
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private class ConfigParser extends DefaultHandler {
		/**
		 * The param list from config file
		 */
		private Map<String,String> params;
		/**
		 * Temporary container for cdata
		 */
		private String tempVal;
		/**
		 * Temporary container for parameter id
		 */
		private String tempParamID;
		
		/**
		 * Default Constructor
		 */
		protected ConfigParser() {
			this.params = new HashMap<String,String>();
			this.tempVal = "";
		}
		
		/**
		 * Converts the contents of a config file to commandline arguments
		 * @param filePath path to the config file
		 * @return equivalent commandline argument array
		 * @throws SecurityException violates security manager
		 * @throws IllegalArgumentException illegal arguments for method
		 * @throws IOException error reading config file
		 * @throws SAXException xml parse error
		 * @throws ParserConfigurationException parser config error
		 */
		public String[] configToArgs(String filePath) throws SecurityException, IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
			Map<String, String> parameters = parseConfig(filePath);
			String[] paramArray = {};
			List<String> paramList = new LinkedList<String>();
			for(String key : parameters.keySet()) {
				String value = parameters.get(key);
				if(!value.equalsIgnoreCase("false")) {
					paramList.add("--"+key);
					if(!value.equalsIgnoreCase("true")) {
						paramList.add(value);
					}
				}
			}
			paramArray = paramList.toArray(paramArray);
			return paramArray;
		}
		
		/**
		 * Parses a configuration file describing a Task
		 * @param filename the name of the file to parse
		 * @return the Task described by the config file
		 * @throws IOException xml parsing error
		 * @throws SAXException xml parsing error
		 * @throws ParserConfigurationException xml parsing error
		 */
		private Map<String, String> parseConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(VFS.getManager().resolveFile(new File("."), filename).getContent().getInputStream(), this); // parse the file and also register this class for call backs
			return this.params;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.tempVal = "";
			this.tempParamID = "";
			if(qName.equalsIgnoreCase("Task")) {
				//Do nothing, but keep to prevent falling into else
			} else if(qName.equalsIgnoreCase("Param")) {
				this.tempParamID = attributes.getValue("id");
			} else {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal += new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("Task")) {
				//Do nothing, but leave it here so it doesn't fall into else statement
			} else if(qName.equalsIgnoreCase("Param")) {
				this.params.put(this.tempParamID, this.tempVal);
			} else {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
	}
}
