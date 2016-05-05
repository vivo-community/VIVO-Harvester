/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
import org.xml.sax.SAXException;
import ORG.oclc.oai.harvester2.app.RawWrite;

/**
 * Class for harvesting from OAI Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class OAIFetch implements RecordStreamOrigin {
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
	 * The record handler to write records to
	 */
	private RecordHandler rhOutput;
	/**
	 * the base for each instance's xmlRos
	 */
	private static XMLRecordOutputStream xmlRosBase = new XMLRecordOutputStream(new String[]{"record"}, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><harvest>", "</harvest>", ".*?<identifier>(.*?)</identifier>.*?", null);
	
	/**
	 * Constuctor
	 * @param address The website address of the repository, without http://
	 * @param rhOutput The recordhandler to write to
	 */
	public OAIFetch(String address, RecordHandler rhOutput) {
		this(address, "0001-01-01", "8000-01-01", rhOutput);
	}
	
	/**
	 * Constructor
	 * @param args command line arguments
	 * @throws IOException error connecting to record handler
	 * @throws UsageException user requested usage message
	 */
	private OAIFetch(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error connecting to record handler
	 */
	private OAIFetch(ArgList argList) throws IOException {
		this(argList.get("u"), argList.get("s"), argList.get("e"), RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")));
	}
	
	/**
	 * Constructor
	 * @param address The website address of the repository, without http://
	 * @param startDate The date at which to begin fetching records, format and time resolution depends on repository.
	 * @param endDate The date at which to stop fetching records, format and time resolution depends on repository.
	 * @param rhOutput The recordhandler to write to
	 */
	public OAIFetch(String address, String startDate, String endDate, RecordHandler rhOutput) {
		this.strAddress = address;
		this.strStartDate = startDate;
		this.strEndDate = endDate;
		this.rhOutput = rhOutput;
	}
	
	/**
	 * Executes the task
	 * @throws IOException error getting recrords
	 */
	public void execute() throws IOException {
		try {
			XMLRecordOutputStream xmlRos = xmlRosBase.clone();
			xmlRos.setRso(this);
			RawWrite.run("http://" + this.strAddress, this.strStartDate, this.strEndDate, "oai_dc", "", xmlRos);
		} catch(ParserConfigurationException e) {
			throw new IOException(e);
		} catch(SAXException e) {
			throw new IOException(e);
		} catch(TransformerException e) {
			throw new IOException(e);
		} catch(NoSuchFieldException e) {
			throw new IOException(e);
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

	@Override
	public void writeRecord(String id, String data) throws IOException {
		log.trace("Adding record "+id);
		this.rhOutput.addRecord(id, data, getClass());
	}
	
	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new OAIFetch(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
