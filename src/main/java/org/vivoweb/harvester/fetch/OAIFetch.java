/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
import org.xml.sax.SAXException;
import ORG.oclc.oai.harvester2.app.RawWrite;

/**
 * Class for harvesting from OAI Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class OAIFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(OAIFetch.class);
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
	 * Constructor
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
	 * @param args command line arguments
	 * @throws IOException error connecting to record handler
	 */
	public OAIFetch(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
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
		rhRecordHandler = RecordHandler.parseConfig(repositoryConfig, argList.getValueMap("O"));
		this.osOutStream = new XMLRecordOutputStream("record", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><harvest>", "</harvest>", ".*?<identifier>(.*?)</identifier>.*?", rhRecordHandler, this.getClass());
	}
	
	/**
	 * Executes the task
	 * @throws IOException error getting recrords
	 */
	public void execute() throws IOException {
		try {
			RawWrite.run("http://" + this.strAddress, this.strStartDate, this.strEndDate, "oai_dc", "", this.osOutStream);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		} catch(TransformerException e) {
			throw new IOException(e.getMessage(), e);
		} catch(NoSuchFieldException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("OAIFetch");
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("url").setDescription("repository url without http://").withParameter(true, "URL"));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("start").setDescription("beginning date of date range (YYYY-MM-DD)").withParameter(true, "DATE"));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("end").setDescription("ending date of date range (YYYY-MM-DD)").withParameter(true, "DATE"));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new OAIFetch(args).execute();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info(getParser().getAppName() + ": End");
		}
	}
}