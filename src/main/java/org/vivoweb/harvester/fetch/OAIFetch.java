/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.dlese.dpc.oai.harvester.HarvestMessageHandler;
import org.dlese.dpc.oai.harvester.Harvester;
import org.dlese.dpc.oai.harvester.Hexception;
import org.dlese.dpc.oai.harvester.OAIChangeListener;
import org.dlese.dpc.oai.harvester.OAIErrorException;
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
	
	private String metadataPrefix = "oai_dc";
	/**
	 * the metadataprefix is hardcoded to oai_dc for now until implementations for other metadata types are supported
	 */
	private String strSetSpec; 
	/**
	 * the setSpec (called spec to avoid confusion with a setter method) is set to null until 
	 * it is added as an optional argument
	 */
	/**
	 * The record handler to write records to
	 */
	private RecordHandler rhOutput;

	/**
	 * Pattern to match character references in a string that are not already escaped.
	 * This ensures that numeric or hexadecimal character references (e.g., &#123; or &#x7B;)
	 * are detected when not preceded by an ampersand (&) to avoid double-escaping.
	 */
	private static final Pattern CHAR_REFERENCE_PATTERN =
		Pattern.compile("(?<=^|[^&])(&#(?:[0-9]+|x[0-9a-fA-F]+);)");

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
		this(address, "0001-01-01", "8000-01-01", "", "oai_dc", rhOutput);
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
		this(argList.get("u"), argList.get("s"), argList.get("e"), argList.get("S"), argList.get("m"), RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")));
	}
	
	/**
	 * Constructor
	 * @param address The website address of the repository, without http://
	 * @param startDate The date at which to begin fetching records, format and time resolution depends on repository.
	 * @param endDate The date at which to stop fetching records, format and time resolution depends on repository.
	 * @param rhOutput The recordhandler to write to
	 */
	public OAIFetch(String address, String startDate, String endDate, String setSpec, String metadataPrefix, RecordHandler rhOutput) {
		this.strAddress = address;
		this.strStartDate = startDate;
		this.strEndDate = endDate;
		this.strSetSpec = setSpec;
		if (metadataPrefix == null) {
			this.metadataPrefix = "oai_dc";
		} else {
		    this.metadataPrefix = metadataPrefix;
		}
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
			HarvestMessageHandler msgHandler = null;
			OAIChangeListener oaiChangeListener = null;
			boolean splitBySet = false;
			boolean writeHeaders = false;
			boolean harvestAll = false;
			boolean harvestAllIfNoDeletedRecord = false;
			int timeOutMilliseconds = 0;
			Date from = null;
			Date until = null; 
			
			if (this.strStartDate.endsWith("Z")) {
			  from = setDate(this.strStartDate, "yyyy-MM-dd'T'HH:mm:ss'Z'");
			} else {
			   from = setDate(this.strStartDate, "yyyy-MM-dd");
		    }
			if (this.strEndDate.endsWith("Z")) {
				until = setDate(this.strEndDate, "yyyy-MM-dd'T'HH:mm:ss'Z'");
			} else {
				until = setDate(this.strEndDate, "yyyy-MM-dd");	
			}
			 
			
			String outdir = null;  // set output directory to null to capture resultes in an array or arrays
			String baseURL = new String();
			
			// if we're not sure if the url is http or https try to ping both
			if (! this.strAddress.startsWith("http")) {
				baseURL = "http://" + this.strAddress;
				
				// try to connect via http.  If fails, try https
				boolean okUrl = pingURL(baseURL, 2000);
				if (okUrl) {
			       //
				} else {
					System.err.println("Could not ping "+ baseURL +" trying https");
					baseURL = "https://" + this.strAddress;
					okUrl = pingURL(baseURL, 2000);
					if (okUrl) {
						//
					} else {
						System.err.println("Could not ping "+ baseURL);
						return;
					}
				}
			} else {
				baseURL = this.strAddress;
			}
			System.out.println("baseURL: "+baseURL);
	
				
			
			String[][] results = Harvester.harvest(baseURL, this.metadataPrefix,
				this.strSetSpec, from, until, outdir, splitBySet, msgHandler, oaiChangeListener, writeHeaders, harvestAll, harvestAllIfNoDeletedRecord, timeOutMilliseconds);
			for (String[] strArray: results) {
		    	 
				if (! StringUtils.equalsIgnoreCase(strArray[1], "deleted")) {
		    	   log.trace("Adding record: " + strArray[0]);
				   this.rhOutput.addRecord(strArray[0], strArray[1], this.getClass());
				   Matcher matcher = CHAR_REFERENCE_PATTERN.matcher(strArray[1]);
				   String fullyEscapedData = matcher.replaceAll("&amp;$1").replace("&amp;&#", "&amp;#");
				   this.rhOutput.addRecord(strArray[0], fullyEscapedData, this.getClass());
				}
		    }
		 
		} catch(Hexception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(OAIErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("OAIFetch");
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("url").setDescription("repository url without http://").withParameter(true, "URL").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("start").setDescription("beginning date of date range (YYYY-MM-DD)").withParameter(true, "DATE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("end").setDescription("ending date of date range (YYYY-MM-DD)").withParameter(true, "DATE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("setSpec").setDescription("setSpec").withParameter(true, "SETSPEC").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("metadataPrefix").setDescription("metadataPrefix (oai_dc)").withParameter(true, "METADATAPREFIX").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE").setRequired(false));
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
	
	public static java.util.Date setDate(String s, String fmt) {
	      SimpleDateFormat formatter = new SimpleDateFormat(fmt);
	      
	      java.util.Date newDate = null;
		  try {
			  newDate = formatter.parse(s);
		  } catch(ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	      
	      return newDate;
	}
	
	public static boolean pingURL(String url, int timeout) {
	    //url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("HEAD");
	        int responseCode = connection.getResponseCode();
	        //System.out.println("responseCode: "+ responseCode);
	        if (200 <= responseCode && responseCode <= 399) {
	        	return true;
	        } else {
	        	System.err.println("responseCode: "+ responseCode);
	        	return false;
	        }
	        
	    } catch (IOException exception) {
	        return false;
	    }
	}
}
