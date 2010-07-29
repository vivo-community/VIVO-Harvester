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
package org.vivoweb.ingest.fetch;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.XMLRecordOutputStream;
import org.xml.sax.SAXException;
import ORG.oclc.oai.harvester2.app.RawWrite;

/**
 * Class for harvesting from OAI Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class OAIFetch {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(OAIFetch.class);
	/**
	 * The website address of the OAI Repository without the protocol prefix (No http://)
	 */
	private String strAddress;
	/**
	 * The start date for the range of records to pull, format is YYYY-MM-DD<br>
	 * If time is required, format is YYYY-MM-DDTHH:MM:SS:MSZ<br>
	 * Some repositories do not support millisecond resolution.<br>
	 * Example 2010-01-15T13:45:12:50Z<br>
	 */
	private String strStartDate;
	/**
	 * The end date for the range of records to pull, format is YYYY-MM-DD<br>
	 * If time is required, format is YYYY-MM-DDTHH:MM:SS:MSZ<br>
	 * Some repositories do not support millisecond resolution.<br>
	 * Example 2010-01-15T13:45:12:50Z<br>
	 */
	private String strEndDate;
	/**
	 * The output stream to send the harvested XML to
	 */
	private OutputStream osOutStream;
	
	/**
	 * Constuctor
	 * @param address The website address of the repository, without http://
	 * @param outStream The output stream to write to
	 */
	public OAIFetch(String address, OutputStream outStream) {
		this(address, "0001-01-01", "8000-01-01", outStream);
	}
	
	/**
	 * Constuctor
	 * @param address The website address of the repository, without http://
	 * @param startDate The date at which to begin fetching records, format and time resolution depends on repository.
	 * @param endDate The date at which to stop fetching records, format and time resolution depends on repository.
	 * @param outStream The output stream to write to
	 */
	public OAIFetch(String address, String startDate, String endDate, OutputStream outStream) {
		this.strAddress = address;
		this.strStartDate = startDate;
		this.strEndDate = endDate;
		this.osOutStream = outStream;
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error connecting to record handler
	 */
	public OAIFetch(ArgList argList) throws IOException {
		this.strAddress = argList.get("u");
		this.strStartDate = argList.get("s");
		this.strEndDate = argList.get("e");
		String repositoryConfig = argList.get("o");
		RecordHandler rhRecordHandler;
		try {
			rhRecordHandler = RecordHandler.parseConfig(repositoryConfig);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		} catch(IOException e) {
			throw new IOException(e.getMessage(),e);
		}
		this.osOutStream = new XMLRecordOutputStream("record", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><harvest>", "</harvest>", ".*?<identifier>(.*?)</identifier>.*?", rhRecordHandler, this.getClass());
	}
	
	/**
	 * Executes the task
	 */
	public void executeTask() {
		try {
			RawWrite.run("http://" + this.strAddress, this.strStartDate, this.strEndDate, "oai_dc", "", this.osOutStream);
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(),e);
		} catch(SAXException e) {
			log.error(e.getMessage(),e);
		} catch(TransformerException e) {
			log.error(e.getMessage(),e);
		} catch(NoSuchFieldException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("OAIFetch");
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("url").setDescription("repository url").withParameter(true, "URL"));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("start").setDescription("beginning date of date range (MM/DD/YYYY)").withParameter(true, "DATE"));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("end").setDescription("ending date of date range (MM/DD/YYYY)").withParameter(true, "DATE"));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new OAIFetch(new ArgList(getParser(), args)).executeTask();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}