package org.vivoweb.ingest.fetch;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.XMLRecordOutputStream;
import org.xml.sax.SAXException;

public class NIHFetch {

	/**
	 * Log4J Logger
	 */
	protected static Log log = LogFactory.getLog(PubmedFetch.class);							//Initialize the logger
	/**
	 * Email address pubmed with contact in case of issues
	 */
	protected String strEmailAddress;
	/**
	 * Location information pubmed will use to contact in case of issues
	 */
	protected String strToolLocation;
	/**
	 * Writer for our output stream
	 */
	protected OutputStreamWriter xmlWriter;
	/**
	 * Query to run on pubmed data
	 */
	protected String strSearchTerm;
	/**
	 * Maximum number of records to fetch
	 */
	protected String strMaxRecords;
	/**
	 * Number of records to fetch per batch
	 */
	protected String strBatchSize;
	
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
	public NIHFetch(String strEmail, String strToolLoc, OutputStream outStream) {
		this.strEmailAddress = strEmail; // NIH Will email this person if there is a problem
		this.strToolLocation = strToolLoc; // This provides further information to NIH
		this.strBatchSize = "1000";
		setXMLWriter(outStream);
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
	public NIHFetch(String strEmail, String strToolLoc, String searchTerm, String maxRecords, String batchSize, OutputStream outStream)
	{
		this.strEmailAddress = strEmail; // NIH Will email this person if there is a problem
		this.strToolLocation = strToolLoc; // This provides further information to NIH
		this.strSearchTerm = searchTerm;
		this.strMaxRecords = maxRecords;
		this.strBatchSize = batchSize;
		setXMLWriter(outStream);
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public NIHFetch(ArgList argList) throws IOException {
		this.strEmailAddress = argList.get("m");
		this.strToolLocation = argList.get("l");
		String repositoryConfig = argList.get("o");
		this.strSearchTerm = argList.get("t");
		this.strMaxRecords = argList.get("n");
		this.strBatchSize  = argList.get("b");
		RecordHandler rhRecordHandler;
		try {
			rhRecordHandler = RecordHandler.parseConfig(repositoryConfig);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		}
		OutputStream os = new XMLRecordOutputStream("", "", "", "", rhRecordHandler, this.getClass());
		setXMLWriter(os);
	}
	
	
	/**
	 * Performs an ESearch against PubMed database and returns the query web environment/query key data
	 * @param term The search term to run against pubmed
	 * @param maxNumRecords The maximum number of records to fetch
	 * @return String[] = {WebEnv, QueryKey, number of records found, first PMID} from the search - used by fetchPubMedEnv
	 */
	public String[] runESearch(String term, int maxNumRecords, String database) {
		return runESearch(term, maxNumRecords, 0, database);
	}
	
	/**
	 * Performs an ESearch against PubMed database and returns the query web environment/query key data
	 * @param term The search term to run against pubmed
	 * @param maxNumRecords The maximum number of records to fetch
	 * @param retStart record number (out of the total - eg: '1200' out of 15000 records), not the PMID
	 * @return String[] = {WebEnv, QueryKey, number of records found, first PMID} from the search - used by fetchPubMedEnv
	 * @author Chris Haines
	 * @author Dale Scheppler
	 */
	public String[] runESearch(String term, int maxNumRecords, int retStart, String database)
	{
		String[] env = new String[4];
		try
		{
			// create service connection
			EUtilsServiceStub service = new EUtilsServiceStub();
			// create a new search
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			// set search to pubmed database
			req.setDb(database);
			// set search term
			req.setTerm(term);
			// set max number of records to return from search
			req.setRetMax(maxNumRecords+"");
			// set number to start at
			req.setRetStart(retStart+"");
			// save this search so we can use the returned set
			req.setUsehistory("y");
			// run the search and get result set
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			// save the environment data
			env[0] = res.getWebEnv();
			env[1] = res.getQueryKey();
			env[2] = ""+res.getIdList().getId().length;
			env[3] = res.getIdList().getId()[0];
			
			log.info("Query resulted in a total of " + env[2] + " records.");
		}
		catch (RemoteException e)
		{
			log.error("NIH Fetch ESearchEnv failed with error: ",e);
		}
		return env;
	}
	
	/**
	 * Setter for xmlwriter
	 * @param os outputstream to write to
	 */
	protected void setXMLWriter(OutputStream os) {
		try {
			// Writer to the stream we're getting from the controller.
			this.xmlWriter = new OutputStreamWriter(os, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			log.error("",e);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	protected static ArgParser getParser() {
		ArgParser parser = new ArgParser("PubmedSOAPFetch");
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("email").setDescription("contact email address").withParameter(true, "EMAIL_ADDRESS"));
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("location").setDescription("contact location/institution").withParameter(true, "LOCATION"));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("termSearch").setDescription("term to search against pubmed").withParameter(true, "SEARCH_STRING").setDefaultValue("1:8000[dp]"));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("numRecords").setDescription("maximum records to return").withParameter(true, "NUMBER").setDefaultValue("100"));
		parser.addArgument(new ArgDef().setShortOption('b').setLongOpt("batchSize").setDescription("number of records to fetch per batch").withParameter(true, "NUMBER").setDefaultValue("1000"));
		return parser;
	}
	
	
}
