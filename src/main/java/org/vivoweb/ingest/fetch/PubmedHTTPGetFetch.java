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

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fetch
 * @author Stephen V. Williams swilliams@ichp.ufl.edu
 * @author Dale R. Scheppler dscheppler@ichp.ufl.edu
 * @author Christopher Haines cah@ichp.ufl.edu
 */
public class PubmedHTTPGetFetch extends NIHFetch {
	private static Log log = LogFactory.getLog(PubmedHTTPGetFetch.class);
	private String strEmailAddress;
	private String strToolLocation;
	private OutputStreamWriter xmlWriter;
	
	/**
	 * Query String used to fetch all records
	 */
	public static String FETCHALL = "1:8000[dp]";

	/***
	 * Constructor
	 * @param strEmail Email address of the tool
	 * @param strToolLoc Location of the tool (Eg: UF or Cornell or Pensyltucky U.
	 * @param osOutStream The output stream for the method.
	 */
	public PubmedHTTPGetFetch(String strEmail, String strToolLoc, OutputStream osOutStream)
	{
		this.strEmailAddress = strEmail; // NIH Will email this person if there is a problem
		this.strToolLocation = strToolLoc; // This provides further information to NIH
		this.xmlWriter = new OutputStreamWriter(osOutStream); // Writer to the stream we're getting from the controller.
	}
	
	/**
	 * TODO
	 */
	public void fetchAll()
	{
		int intStartRecord = 1;
		int intStopRecord = getHighestRecordNumber();
		log.info("Beginning fetch of records " + intStartRecord + " to " + intStopRecord + ".");
		ArrayList<Integer> idList = new ArrayList<Integer>();
		int id = intStartRecord;
		while(id <= intStopRecord) {
			idList.add(new Integer(id));
			if(id%100000 == 0) {
				throttle(idList);
				idList.clear();
			}
			id++;
		}
		if(!idList.isEmpty()) {
			throttle(idList);
		}
	}
	
	/**
	 * TODO
	 * @param strAffiliation
	 */
	public void fetchByAffiliation(String strAffiliation)
	{
		fetchByAffiliation(strAffiliation, null);
	}
	
	/**
	 * TODO
	 * @param strAffiliation
	 * @param intMaxRecords
	 */
	public void fetchByAffiliation(String strAffiliation, Integer intMaxRecords)
	{
		log.trace("Searching for all records affiliated with " + strAffiliation);
//		throttle(ESearch(strAffiliation + "[ad]", intMaxRecords));
		fetchPubMedEnv(ESearchEnv(strAffiliation + "[ad]", intMaxRecords));
	}

	/**
	 * Performs an ESearch against PubMed database using a search term
	 * 
	 * @param term
	 *            - The search term
	 * @param maxNumRecords
	 *            - Maximum number of records to pull, set currently by
	 *            Fetch.throttle.
	 * @return List<Integer> of ids found in the search result
	 * @author chaines
	 */
	private List<Integer> ESearch(String term, Integer maxNumRecords)
	{
		// define the list to hold our ids
		ArrayList<Integer> idList = new ArrayList<Integer>();
		try
		{
			// create service connection
			EUtilsServiceStub service = new EUtilsServiceStub();
			// create a new search
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			// set search to pubmed database
			req.setDb("pubmed");
			// set search term
			req.setTerm(term);
			// set max number of records to return from search
			req.setRetMax(maxNumRecords.toString());
			// run the search and get result set
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			log.trace("Fetching a total of " + res.getIdList().getId().length + " records.");
			// for each id in the list of ids in the search results
			for (String id : res.getIdList().getId())
			{
				try
				{
					// put it in our List
					idList.add(new Integer(id));
				}
				// just in case there is a non-number in the ID list (should not happen)
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
			log.trace(idList.size()+" records found");
		}
		catch (AxisFault f)
		{
			log.error("Failed to initialize service connection");
			f.printStackTrace();
		}
		catch (RemoteException e)
		{
			log.error("Failed to run the search");
			e.printStackTrace();
		}
		// return the list of ids
		return idList;
	}
	
	/**
	 * Performs an ESearch against PubMed database using a search term
	 * 
	 * @param term
	 *            - The search term
	 * @return String[] = {WebEnv, QueryKey, idListLength}
	 * @author chaines
	 */
	public String[] ESearchEnv(String term)
	{
		return ESearchEnv(term, null);
	}
	
	/**
	 * Performs an ESearch against PubMed database using a search term
	 * 
	 * @param term
	 *            - The search term
	 * @param maxNumRecords
	 *            - Maximum number of records to pull, set currently by
	 *            Fetch.throttle.
	 * @return String[] = {WebEnv, QueryKey, idListLength}
	 * @author chaines
	 */
	public String[] ESearchEnv(String term, Integer maxNumRecords)
	{
		// define the array to hold our Environment data
		String[] env = new String[3];
		try
		{
			// create service connection
			EUtilsServiceStub service = new EUtilsServiceStub();
			// create a new search
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			// set search to pubmed database
			req.setDb("pubmed");
			// set search term
			req.setTerm(term);
			if(maxNumRecords != null) {
				// set max number of records to return from search
				req.setRetMax(maxNumRecords.toString());
			}
			// save this search so we can use the returned set
			req.setUsehistory("y");
			// run the search and get result set
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			log.trace("Query resulted in a total of " + res.getIdList().getId().length + " records.");
			// save the environment data
			env[0] = res.getWebEnv();
			env[1] = res.getQueryKey();
			env[2] = ""+res.getIdList().getId().length;
		}
		catch (RemoteException e)
		{
			log.error("Failed to run the search",e);
		}
		return env;
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env =  = {WebEnv, QueryKey, idListLength}
	 * @throws IllegalArgumentException 
	 */
	public void fetchPubMedEnv(String[] env) throws IllegalArgumentException {
		if(env.length != 3) {
			throw new IllegalArgumentException("Invalid WebEnv, QueryKey, and idListLength");
		}
		fetchPubMedEnv(env[0], env[1], "0", env[2]);
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env = {WebEnv, QueryKey, [idListLength]}
	 * @param start = String of record number to start at 
	 * @param numRecords = String of number of records to pull
	 * @throws IllegalArgumentException 
	 */
	public void fetchPubMedEnv(String[] env, String start, String numRecords) throws IllegalArgumentException {
		if(!(env.length == 2 || env.length == 3)) {
			throw new IllegalArgumentException("Invalid WebEnv and QueryKey");
		}
		fetchPubMedEnv(env[0], env[1], start, numRecords);
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param WebEnv
	 * @param QueryKey
	 * @param intStart 
	 * @param maxRecords 
	 */
	public void fetchPubMedEnv(String WebEnv, String QueryKey, String intStart, String maxRecords) {
		log.trace("Fetching records from search");
		StringBuilder urlSb = new StringBuilder();
		urlSb.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&WebEnv=");
		urlSb.append(WebEnv);
		urlSb.append("&query_key=");
		urlSb.append(QueryKey);
		urlSb.append("&rettype=xml&tool=");
		urlSb.append(this.strToolLocation);
		urlSb.append("&email=");
		urlSb.append(this.strEmailAddress);
		urlSb.append("&retstart=");
		urlSb.append(intStart);
		urlSb.append("&retmax=");
		urlSb.append(maxRecords);
		serializeFetchRequest(urlSb.toString());
	}
	
	/**
	 * This method takes in a range of PMIDs and returns MedLine XML to the main
	 * method as an outputstream.
	 * 
	 * @param ids
	 *            Range of PMID you want to pull, in list form
	 */
	private void fetchPubMed(List<Integer> ids) {
		StringBuilder urlSb = new StringBuilder();
		urlSb.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=");
		
		//append ID List
		for(int id = 0; id < ids.size(); id++ ) {
			if(id != 0) {
				urlSb.append(",");
			}
			urlSb.append(ids.get(id));
		}
		urlSb.append("&rettype=xml&tool=");
		urlSb.append(this.strToolLocation);
		urlSb.append("&email=");
		urlSb.append(this.strEmailAddress);
//		log.info("Fetching "+ ids.size() + " records: " + strPMID.toString() + ".");
		if(ids.size() > 0){
			log.info("Fetching "+ ids.size() + " records: " + ids.get(0) + " - " + ids.get(ids.size()-1) + ".");
		}
		serializeFetchRequest(urlSb.toString());
	}
	
	private void serializeFetchRequest(String url) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br;
		try {
				br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
				String s;
				while((s = br.readLine()) != null) {
					sb.append(s);
				}
				sanitizeXML(sb.toString());
			} catch(MalformedURLException e) {
				log.error("Query URL incorrectly formatted", e);
			} catch(IOException e) {
				log.error("Unable to read from URL", e);
			}
	}
	
	/**
	 * This method takes in a range of PMIDs and returns MedLine XML to the main
	 * method as an outputstream.
	 * 
	 * @param id
	 *            PMID you want to pull
	 */
	@SuppressWarnings("unused")
	private void fetchPubMed(int id) {
		StringBuilder urlSb = new StringBuilder();
		urlSb.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=");
		urlSb.append(id);
		urlSb.append("&rettype=xml&tool=");
		urlSb.append(this.strToolLocation);
		urlSb.append("&email=");
		urlSb.append(this.strEmailAddress);
		log.trace("Fetching record: " + id + ".");
		serializeFetchRequest(urlSb.toString());
	}
	
	/**
	 * Ensures that fetch procedures only request a number of times under the PubMed cap.
	 * 10000 between the hours of 9pmEST and 5amEST, 100 otherwise
	 * 
	 * @param ids a List<Integer> of ids to be fetched
	 * @author chaines
	 */
	private void throttle(List<Integer> ids)
	{
		TimeZone est = TimeZone.getTimeZone("EST5EDT");
		Calendar cal = Calendar.getInstance(est);
		int intRetNum = 900; //<< set to -1 to obey time restriction
		while(intRetNum == -1) {
			cal = Calendar.getInstance(est);
			int intHour = cal.get(Calendar.HOUR_OF_DAY);
			int intDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm'-'zzzz' on 'EEEE");
			sdf.setTimeZone(est);
			log.trace("It is currently "+sdf.format(cal.getTime())+".");
			if ((intHour >= 21 || intHour < 5) || (intDayOfWeek == Calendar.SATURDAY || intDayOfWeek == Calendar.SUNDAY))
			{
				intRetNum = 900;
			}
			else
			{
				try {
					Thread.sleep(1*60*1000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		log.trace("Throttle is allowing a maximum fetch of " + intRetNum + " records this batch.");
		// our list to fetch is more than one batch
		if (ids.size() > intRetNum)
		{
			// create List to hold ids to fetch this batch
			ArrayList<Integer> idList = new ArrayList<Integer>();
			int x = intRetNum;
			while (x != 0)
			{
				//shift the top intRetNum records from ids to idList
				idList.add(ids.remove(0));
				x--;
			}
//			System.out.println(idList);
			// fetch this batch
			fetchPubMed(idList);
//			for(Integer id : idList) {
//				fetchPubMed(id);
//			}
//			try
//			{
//				// pause for 1/3 second
//				Thread.sleep(333);
//			}
//			catch (InterruptedException e)
//			{
//				e.printStackTrace();
//			}
			// run throttle again with the remaining ids to be fetched
			throttle(ids);
		}
		else
		{
			// fetch batch
			fetchPubMed(ids);
//			for(Integer id : ids) {
//				fetchPubMed(id);
//			}
		}
	}
	
	/**
	 * 
	 * @param intStartRecord
	 * @param intStopRecord
	 */
	public void fetchAllByRange(int intStartRecord, int intStopRecord)
	{
		int start = intStartRecord;
		int stop = intStopRecord;
		ArrayList<Integer> idList = new ArrayList<Integer>();
		for (int id = start; id <= stop; id++)
		{
			idList.add(new Integer(id));
		}
		try
		{
			throttle(idList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		log.trace("Fetching records that fall within " + intStartRecord+ " and " + intStopRecord);
	}
	
	/**
	 * 
	 * @param intStartMonth
	 * @param intStartDay
	 * @param intStartYear
	 * @param intStopMonth
	 * @param intStopDay
	 * @param intStopYear
	 */
	public void fetchAllByDate(int intStartMonth, int intStartDay, int intStartYear, int intStopMonth, int intStopDay, int intStopYear)
	{
		log.trace("Fetching all records between "+intStartYear+"/"+intStartMonth+"/"+intStartDay+ " and " +intStopYear+"/"+intStopMonth+"/"+intStopDay+ ".");
		fetchPubMedEnv(ESearchEnv("\""+intStartYear+"/"+intStartMonth+"/"+intStartDay+"\""+"[PDAT] : \""+intStopYear+"/"+intStopMonth+"/"+intStopDay+"\"[PDAT]"));		
	}
	
	/**
	 * 
	 * @param intLastRunMonth
	 * @param intLastRunDay
	 * @param intLastRunYear 
	 * @return Calendar timestamp of when this was run
	 */
	public Calendar fetchAllFromLastFetch(int intLastRunMonth, int intLastRunDay, int intLastRunYear)
	{
		Calendar gcToday = Calendar.getInstance();
		int intYear = gcToday.get(Calendar.YEAR);
		int intMonth = gcToday.get(Calendar.MONTH) + 1;
		int intDay = gcToday.get(Calendar.DATE);
		log.trace("Fetching all records created between " + intLastRunYear+"/"+intLastRunMonth+"/"+intLastRunDay + " and now.");
		fetchPubMedEnv(ESearchEnv("\""+intLastRunYear+"/"+intLastRunMonth+"/"+intLastRunDay+"\""+"[PDAT] : \""+(intYear + 5)+"/"+intMonth+"/"+intDay+"\"[PDAT]"));
		return gcToday;
	}
	
	/**
	 * This function simply checks to see what is the highest PubMed article PMID at the time it is called.
	 * The pubmed website might have 2-5 more records past what this one pulls
	 * But this function pulls them up to what they have indexed.
	 * So it's as good a "Highest number" as we're going to get.
	 * 
	 * @return Returns an integer of the highest PMID at the time it is run
	 * @author dscheppler
	 */
	public int getHighestRecordNumber()
	{
		Calendar gcToday = Calendar.getInstance();
		int intYear = gcToday.get(Calendar.YEAR);
		int intMonth = gcToday.get(Calendar.MONTH);
		int intDay = gcToday.get(Calendar.DATE);
		List<Integer> lstResult = ESearch("\""+intYear+"/"+intMonth+"/"+intDay+"\""+"[PDAT] : \""+(intYear + 5)+"/"+12+"/"+31+"\"[PDAT]", 1);
		return lstResult.get(0);
	}
	
	/**
	 * Sanitize Method
	 * Adds the dtd and xml code to the top of the xml file and removes the extranious
	 * xml namespace attributes.  This function is slated for deprecation on mileston 2
	 * 
	 * @param s 
	 * @throws IOException
	 * @author cah
	 * @author swilliams
	 */
	private void sanitizeXML(String s) {
		log.trace("Sanitizing Output");
		
		//used to remove header from xml
		String headerRegEx = "<\\?xml version=\"1.0\"\\?>.*?<!DOCTYPE.*?PubmedArticleSet.*?PUBLIC.*?\"-//NLM//DTD PubMedArticle, 1st January 2010//EN\".*?\"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">.*?<PubmedArticleSet>";
		
		//used to remove footer from xml
		String footerRegEx = "</PubmedArticleSet>";
		
		//System Messages
		String newS = s.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle").replaceAll(headerRegEx, "").replaceAll(footerRegEx, "");
		log.trace("XML File Length - Pre Sanitize: " + s.length());
		log.trace("XML File Length - Post Sanitze: " + newS.length());
		try {
			this.xmlWriter.write(newS);
			//file close statements.  Warning, not closing the file will leave incomplete xml files and break the translate method
			this.xmlWriter.write("\n");
			this.xmlWriter.flush();
		} catch(IOException e) {
			log.error("Unable to write XML to file.",e);
		}
		log.trace("Sanitization Complete");
	}
	
	/**
	 * @throws IOException
	 */
	public void beginXML() throws IOException {
		this.xmlWriter.write("<?xml version=\"1.0\"?>\n");
		this.xmlWriter.write("<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n");
		this.xmlWriter.write("<PubmedArticleSet>\n");
		this.xmlWriter.flush();
	}
	
	/**
	 * @throws IOException
	 */
	public void endXML() throws IOException {
		this.xmlWriter.flush();
		this.xmlWriter.write("</PubmedArticleSet>");
		this.xmlWriter.flush();
		this.xmlWriter.close();
	}
	
	/**
	 * Executes the fetch
	 * 
	 * FIXME eventually Fetch should be initialized with parameters such that it know which of the fetches to run and all
	 * -- that needs to be called is execute()
	 */
	public void execute()
	{
		log.info("Fetch Begin");
		//xml write functions, take in a stream pass it to a writer
		//Header lines for XML files from pubmed
		try {
			beginXML();
			this.fetchAll();
			endXML();
		} catch(IOException e) {
			log.error("",e);
		}
//		this.fetchByAffiliation("ufl.edu", 20);
		log.info("Fetch End");
		// TODO throttling should be done as part of the queries maybe? the current throttle does not work with the idea of
		// -- WebEnv/QueryKey fetching... Will need to research how that will work
	}
	
}