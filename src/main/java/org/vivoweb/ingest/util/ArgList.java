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
package org.vivoweb.ingest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import static java.util.Arrays.asList;

/**
 * Parses arguments and config files
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ArgList {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ArgList.class);
	/**
	 * The Argument parser
	 */
	private OptionParser parser;
	/**
	 * Argument OptionSet from commandline arguments
	 */
	private OptionSet oCmdSet;
	/**
	 * Argument OptionSet from configuration file
	 */
	private OptionSet oConfSet;
	
	/**
	 * Constructor
	 * @param p parser
	 * @param args commandline args
	 * @param neededArgs the required arguments (do not include arguments with default values)
	 * @throws IllegalArgumentException missing args
	 * @throws IOException error parsing args
	 */
	public ArgList(OptionParser p, String[] args, String... neededArgs) throws IllegalArgumentException, IOException {
		try {
			this.parser = p;
			this.parser.acceptsAll(asList("C", "config")).withRequiredArg().describedAs("XML Configuration File");
			this.parser.acceptsAll(asList("h","?","help")).withRequiredArg().describedAs("Help Message");
			this.oCmdSet = this.parser.parse(args);
			if(this.oCmdSet.has("?")) {
				String usage = getUsage();
				log.info(usage);
				System.out.println(usage);
			} else {
				if(this.oCmdSet.has("C")) {
					this.oConfSet = this.parser.parse(ConfigParser.configToArgs((String)this.oCmdSet.valueOf("C")));
				} else {
					this.oConfSet = null;
				}
				for(String arg : neededArgs) {
					if(!has(arg)) {
						throw new IllegalArgumentException("Missing Argument: "+arg);
					}
				}
			}
		} catch(SecurityException e) {
			throw new IOException(e.getMessage(), e);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the value of the argument
	 * @param arg argument to get
	 * @return the value
	 */
	public String get(String arg) {
		String retVal = null;
		if(this.oCmdSet.has(arg)) {
			retVal = (String)this.oCmdSet.valueOf(arg);
		} else {
			if(this.oConfSet != null && this.oConfSet.has(arg)) {
				retVal = (String)this.oConfSet.valueOf(arg);
			} else {
				try {
					retVal = (String)this.oCmdSet.valueOf(arg);
				} catch (NullPointerException e){
					retVal = null;
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Gets the values of the argument
	 * @param arg argument to get
	 * @return the values
	 */
	public List<String> getAll(String arg) {
		List<String> retVal = null;
		if(this.oCmdSet.has(arg)) {
			retVal = (List<String>) this.oCmdSet.valuesOf(arg);
		} else {
			if(this.oConfSet != null && this.oConfSet.has(arg)) {
				retVal = (List<String>) this.oConfSet.valuesOf(arg);
			} else {
				try {
					retVal = (List<String>) this.oCmdSet.valuesOf(arg);
				} catch (NullPointerException e){
					retVal = null;
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Determines if the arg list has a value for a given argument
	 * @param arg the argument
	 * @return true if a value was provided (from any of command line, config files)
	 */
	public boolean has(String arg) {
		return (this.oCmdSet.has(arg) || (this.oConfSet != null && this.oConfSet.has(arg)));
	}
	
	/**
	 * Get the usage message for this arg list
	 * @return the usage string
	 * @throws IOException error outputing
	 */
	public String getUsage() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		this.parser.printHelpOn(baos);
		baos.flush();
		return baos.toString();
	}
}
