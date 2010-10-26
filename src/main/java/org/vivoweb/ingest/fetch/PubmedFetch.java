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

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet_type0;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.XMLRecordOutputStream;
import org.xml.sax.SAXException;

/**
 * Module for fetching PubMed Citations using the PubMed SOAP Interface
 * Based on the example code available at the PubMed Website.
 * @author Stephen V. Williams (swilliams@ctrip.ufl.edu)
 * @author Dale R. Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class PubmedFetch extends NIHFetch {
	

	/**
	 * Log4J Logger
	 */
	protected static Log log = LogFactory.getLog(NIHFetch.class);							//Initialize the logger
	
	/**
	 * Constructor
	 * Primary method for running a PubMed SOAP Fetch. The email address and location of the
	 * person responsible for this install of the program is required by PubMed guidelines so
	 * the person can be contacted if there is a problem, such as sending too many queries
	 * too quickly. 
	 * @author Dale Scheppler
	 * @author Chris Haines
	 * @author Stephen Williams
	 * @param strEmail Contact email address of the person responsible for this install of the PubMed Harvester
	 * @param strToolLoc Location of the current tool installation (Eg: UF or Cornell or Pensyltucky U.)
	 * @param outStream The output stream for the method.
	 */
	public PubmedFetch(String strEmail, String strToolLoc, OutputStream outStream) {
		super(strEmail,strToolLoc,outStream);
		this.strMaxRecords = getHighestPMID()+"";
		this.strBatchSize = "1000";
	}
	
	/**
	 * Constructor
	 * Primary method for running a PubMed SOAP Fetch. The email address and location of the
	 * person responsible for this install of the program is required by PubMed guidelines so
	 * the person can be contacted if there is a problem, such as sending too many queries
	 * too quickly. 
	 * @author Dale Scheppler
	 * @author Chris Haines
	 * @param strEmail Contact email address of the person responsible for this install of the PubMed Harvester
	 * @param strToolLoc Location of the current tool installation (Eg: UF or Cornell or Pensyltucky U.)
	 * @param searchTerm query to run on pubmed data
	 * @param maxRecords maximum number of records to fetch
	 * @param batchSize number of records to fetch per batch
	 * @param outStream The output stream for the method.
	 */
	public PubmedFetch(String strEmail, String strToolLoc, String searchTerm, String maxRecords, String batchSize, OutputStream outStream)
	{
		super(strEmail,strToolLoc,searchTerm,maxRecords,batchSize,outStream);
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public PubmedFetch(ArgList argList) throws IOException {
		super(argList);
		String repositoryConfig = argList.get("o");
		RecordHandler rhRecordHandler;
		try {
			rhRecordHandler = RecordHandler.parseConfig(repositoryConfig);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		}
		OutputStream os = new XMLRecordOutputStream("PubmedArticle", "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<PMID>(.*?)</PMID>.*?", rhRecordHandler, this.getClass());
		setXMLWriter(os);
	}
	

	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param WebEnv web environment from an ESearch
	 * @param QueryKey query key from an ESearch
	 * @param retStart record number (out of the total - eg: '1200' out of 15000 records), not the PMID
	 * @param numRecords The number of records to fetch
	 */
	public void fetchPubMed(String WebEnv, String QueryKey, String retStart, String numRecords) {
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		req.setQuery_key(QueryKey);
		req.setWebEnv(WebEnv);
		req.setEmail(this.strEmailAddress);
		req.setTool(this.strToolLocation);
		req.setRetstart(retStart);
		req.setRetmax(numRecords);
		log.info("Fetching records from search");
		try {
			serializeFetchRequest(req);
		}catch(RemoteException e) {
			log.error("Could not run search",e);
		}
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env {WebEnv, QueryKey, number of records found, first PMID} - from ESearch
	 * @throws IllegalArgumentException env is invalid
	 * @author Chris Haines
	 */
	public void fetchPubMed(String[] env) throws IllegalArgumentException {
		if(env.length < 3) {
			throw new IllegalArgumentException("Invalid env. Must contain {WebEnv, QueryKey, number of records found}");
		}
		fetchPubMed(env[0], env[1], "0", env[2]);
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env {WebEnv, QueryKey, number of records found} - from ESearch
	 * @param retStart record number (out of the total - eg: '1200' out of 15000 records), not the PMID 
	 * @param numRecords The number of records to fetch
	 * @throws IllegalArgumentException env is invalid
	 */
	public void fetchPubMed(String[] env, String retStart, String numRecords) throws IllegalArgumentException {
		if(env.length < 2) {
			throw new IllegalArgumentException("Invalid env. Must contain {WebEnv, QueryKey}");
		}
		fetchPubMed(env[0], env[1], retStart, numRecords);
	}
	
	/**
	 * Get highest PubMed article PMID
	 * @return highest PMID
	 * @author Dale Scheppler
	 */
	private int getHighestPMID()
	{
		return Integer.parseInt(runESearch(queryAll(), 1,"pubmed")[3]);
	}
	
	/**
	 * Get highest PubMed article PMID
	 * @return highest PMID
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	public static int getHighestRecordNumber() {
		return new PubmedFetch("", "", queryAll(), "1", "1", new NullOutputStream()).getHighestPMID();
	}
	
	/**
	 * Get query to fetch all records in pubmed
	 * @return query string for all pubmed records
	 */
	public static String queryAll()
	{
		return "1:8000[dp]";
	}
	
	/**
	 * Get query for all records between a date range
	 * @param start start date
	 * @param end end date
	 * @return query string for date range
	 */
	public static String queryAllByDateRange(Calendar start, Calendar end)
	{
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy/M/d");
		return dfm.format(start.getTime())+"[PDAT]:"+dfm.format(end.getTime())+"[PDAT]";		
	}
	
	/**
	 * Get query for all records since a given date
	 * @param date date to fetch since
	 * @return String query to fetch all from given date
	 */
	public static String queryAllSinceDate(Calendar date)
	{
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy/M/d");
		return dfm.format(date.getTime())+"[PDAT]:8000[PDAT]";
	}
	
	/**
	 * Get query string to locate all records matching the given affiliation
	 * Ex: "vivoweb.org" matches records with "vivoweb.org" in the affiliation field
	 * @param strAffiliation The affiliation information
	 * @return A query string that will allow a search by affiliation.
	 */
	public static String queryByAffiliation(String strAffiliation)
	{
		return strAffiliation+"[ad]";
	}
	
	/**
	 * Get query to fetch all records in a given PMID range
	 * @param intStartPMID start PMID
	 * @param intStopPMID end PMID
	 * @return String query to fetch all in range
	 */
	public static String queryByRange(int intStartPMID, int intStopPMID)
	{
		return intStartPMID+":"+intStopPMID+"[uid]";
	}
	
	/**
	 * Sanitizes XML in preparation for writing to output stream
	 * Removes xml namespace attributes, XML wrapper tag, and splits each record on a new line
	 * @param strInput The XML to Sanitize.
	 * @author Chris Haines
	 * @author Stephen Williams
	 */
	private void sanitizeXML(String strInput) {
		log.debug("Sanitizing Output");
		log.debug("XML File Length - Pre Sanitize: " + strInput.length());
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle");
		log.debug("XML File Length - Post Sanitze: " + newS.length());
		log.debug("Sanitization Complete");
		try {
			log.trace("Writing to output");
			this.xmlWriter.write(newS);
			//file close statements.  Warning, not closing the file will leave incomplete xml files and break the translate method
			this.xmlWriter.write("\n");
			this.xmlWriter.flush();
			log.trace("Writing complete");
		} catch(IOException e) {
			log.error("Unable to write XML to file.",e);
		}
	}
	
	/**
	 * Runs, sanitizes, and outputs the results of a EFetch request to the xmlWriter
	 * @param req the request to run and output results
	 * @throws RemoteException error running EFetch
	 */
	private void serializeFetchRequest(EFetchPubmedServiceStub.EFetchRequest req) throws RemoteException {
		//Create buffer for raw, pre-sanitized output
		ByteArrayOutputStream buffer=new ByteArrayOutputStream();
		//Connect to pubmed
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		//Run the EFetch request
		EFetchResult result = service.run_eFetch(req);
		//Get the article set
		PubmedArticleSet_type0 articleSet = result.getPubmedArticleSet();
		XMLStreamWriter writer;
		try {
			//Create a temporary xml writer to our buffer
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
			MTOMAwareXMLSerializer serial = new MTOMAwareXMLSerializer(writer);
			log.debug("Buffering records");
			//Output data
			articleSet.serialize(new QName("RemoveMe"), null, serial);
			serial.flush();
			log.debug("Buffering complete");
			log.debug("buffer size: "+buffer.size());
			//Dump buffer to String
			String iString = buffer.toString("UTF-8");
			//Sanitize string (which writes it to xmlWriter)
			sanitizeXML(iString);
		} catch(XMLStreamException e) {
			log.error("Unable to write to output",e);
		} catch(UnsupportedEncodingException e) {
			log.error("Cannot get xml from buffer",e);
		}
	}
		
	/**
	 * Executes the task
	 */
	public void execute() {
		log.info("Fetch: Start");
		int recToFetch;
		if(this.strMaxRecords.equalsIgnoreCase("all")) {
			recToFetch = getHighestRecordNumber();
		} else {
			recToFetch = Integer.parseInt(this.strMaxRecords);
		}
		int intBatchSize = Integer.parseInt(this.strBatchSize); 
		if(recToFetch <= intBatchSize) {
			fetchPubMed(runESearch(this.strSearchTerm, recToFetch, "pubmed"));
		} else {
			String[] env = runESearch(this.strSearchTerm, recToFetch,"pubmed");
			String WebEnv = env[0];
			String QueryKey = env[1];
			// sanity check for max records
			if (Integer.parseInt(env[2]) < recToFetch) {
				recToFetch = Integer.parseInt(env[2]);
			}
			for(int x = recToFetch; x > 0; x-=intBatchSize) {
				int maxRec = (x<=intBatchSize) ? x : intBatchSize;
				int startRec = recToFetch - x;
				log.debug("maxRec: "+maxRec);
				log.debug("startRec: "+startRec);
				fetchPubMed(WebEnv, QueryKey, startRec+"", maxRec+"");
			}
		}
		log.info("Fetch: End");
	}
		
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new PubmedFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(),e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}