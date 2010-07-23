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
package org.vivoweb.ingest.transfer;

import static java.util.Arrays.asList;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import joptsimple.OptionParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.ArgList;
import org.vivoweb.ingest.util.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Transfer data from one Jena model to another
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class Transfer {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Transfer.class);
	/**
	 * Model to read records from
	 */
	private Model input;
	/**
	 * Model to write records to
	 */
	private Model output;
	
	/**
	 * Constructor
	 * @param in input Model
	 * @param out output Model
	 */
	public Transfer(Model in, Model out) {
	  this.input = in;
	  this.output = out;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public Transfer(ArgList argList) throws IOException {
		String inConfig = argList.get("i");
		String outConfig = argList.get("o");
		try {
			this.input = JenaConnect.parseConfig(inConfig).getJenaModel();
			this.output = JenaConnect.parseConfig(outConfig).getJenaModel();
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	/**
	 * Copy data from input to output
	 */
	private void transfer() {
		this.output.add(this.input);
	}
	
	/**
	 * Executes the task
	 */
	public void executeTask() {
		transfer();
	}
	
	/**
	 * Get the OptionParser for this Task
	 * @return the OptionParser
	 */
	private static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("i", "input")).withRequiredArg().describedAs("config file for input jena model");
		parser.acceptsAll(asList("o", "output")).withRequiredArg().describedAs("config file for output jena model");
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new Transfer(new ArgList(getParser(), args, "i","o")).executeTask();
		} catch(IllegalArgumentException e) {
			try {
				getParser().printHelpOn(System.out);
			} catch(IOException e1) {
				log.fatal(e.getMessage(),e);
			}
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}
