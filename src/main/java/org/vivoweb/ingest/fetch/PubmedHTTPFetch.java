/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.fetch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.repo.XMLRecordOutputStream;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Module for fetching PubMed Citations using the PubMed HTTP Interface Based on the example code available at the
 * PubMed Website.
 * @author Stephen V. Williams (swilliams@ctrip.ufl.edu)
 * @author Dale R. Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class PubmedHTTPFetch extends NIHFetch {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(PubmedHTTPFetch.class);
	/**
	 * The name of the PubMed database
	 */
	private static String database = "pubmed";
	
	/**
	 * Constructor: Primary method for running a PubMed Fetch. The email address of the person responsible for this
	 * install of the program is required by NIH guidelines so the person can be contacted if there is a problem, such
	 * as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param outStream output stream to write to
	 */
	public PubmedHTTPFetch(String emailAddress, OutputStream outStream) {
		super(emailAddress, outStream, database);
		setMaxRecords(getLatestRecord() + "");
	}
	
	/**
	 * Constructor: Primary method for running a PubMed Fetch. The email address of the person responsible for this
	 * install of the program is required by NIH guidelines so the person can be contacted if there is a problem, such
	 * as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param searchTerm query to run on pubmed data
	 * @param maxRecords maximum number of records to fetch
	 * @param batchSize number of records to fetch per batch
	 * @param outStream output stream to write to
	 */
	public PubmedHTTPFetch(String emailAddress, String searchTerm, String maxRecords, String batchSize, OutputStream outStream) {
		super(emailAddress, searchTerm, maxRecords, batchSize, outStream, database);
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public PubmedHTTPFetch(ArgList argList) throws IOException {
		super(argList, database, new XMLRecordOutputStream("PubmedArticle", "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<PMID>(.*?)</PMID>.*?", null, PubmedHTTPFetch.class));
	}
	
	@Override
	public String[] runESearch(String term, boolean logMessage) {
		String[] env = new String[4];
		try {
			StringBuilder urlSb = new StringBuilder();
			urlSb.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?");
			urlSb.append("&db=");
			urlSb.append(database);
			urlSb.append("&tool=");
			urlSb.append(getToolName());
			urlSb.append("&email=");
			urlSb.append(getEmailAddress());
			urlSb.append("&usehistory=y");
			urlSb.append("&retmode=xml");
			urlSb.append("&term=");
			urlSb.append(term);
			if(logMessage) {
//				log.debug(urlSb.toString());
			}
			
			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
			docBuildFactory.setIgnoringComments(true);
			Document doc = docBuildFactory.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(getURLContents(urlSb.toString()).getBytes())));
			env[0] = doc.getElementsByTagName("WebEnv").item(0).getTextContent();
			env[1] = doc.getElementsByTagName("QueryKey").item(0).getTextContent();
			env[2] = doc.getElementsByTagName("Count").item(0).getTextContent();
			env[3] = doc.getElementsByTagName("Id").item(0).getTextContent();
			if(logMessage) {
				log.info("Query resulted in a total of " + env[2] + " records.");
			}
		} catch(MalformedURLException e) {
			log.error(e.getMessage(),e);
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		} catch(SAXException e) {
			log.error(e.getMessage(),e);
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(),e);
		}
		return env;
	}
	
	@Override
	public void fetchRecords(String WebEnv, String QueryKey, String retStart, String numRecords) {
		StringBuilder urlSb = new StringBuilder();
		urlSb.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?");
		urlSb.append("&db=");
		urlSb.append(database);
		urlSb.append("&query_key=");
		urlSb.append(QueryKey);
		urlSb.append("&WebEnv=");
		urlSb.append(WebEnv);
		urlSb.append("&tool=");
		urlSb.append(getToolName());
		urlSb.append("&email=");
		urlSb.append(getEmailAddress());
		urlSb.append("&retmode=xml");
		// set max number of records to return from search
		urlSb.append("&retmax=" + numRecords);
		// set number to start at
		urlSb.append("&retstart=" + retStart);
//		log.debug(urlSb.toString());
		log.info("Fetching records from http search");
		try {
			sanitizeXML(getURLContents(urlSb.toString()));
		} catch(MalformedURLException e) {
			log.error("Query URL incorrectly formatted", e);
		} catch(IOException e) {
			log.error("Unable to read from URL", e);
		}
	}
	
	/**
	 * Get the contents of a url
	 * @param url the url to grab
	 * @return the contents
	 * @throws MalformedURLException invalid url
	 * @throws IOException error reading
	 */
	private String getURLContents(String url) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		String s;
		while((s = br.readLine()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
	
	/**
	 * Sanitizes XML in preparation for writing to output stream Removes xml namespace attributes, XML wrapper tag, and
	 * splits each record on a new line
	 * @param strInput The XML to Sanitize.
	 */
	private void sanitizeXML(String strInput) {
		//used to remove header from xml
		String headerRegEx = "<\\?xml version=\"1.0\"\\?>.*?<!DOCTYPE.*?PubmedArticleSet.*?PUBLIC.*?\"-//NLM//DTD PubMedArticle, 1st January 2010//EN\".*?\"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_.*?.dtd\">.*?<PubmedArticleSet>";
		//used to remove footer from xml
		String footerRegEx = "</PubmedArticleSet>";
		log.debug("Sanitizing Output");
		log.debug("XML File Length - Pre Sanitize: " + strInput.length());
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle").replaceAll(headerRegEx, "").replaceAll(footerRegEx, "");
		log.debug("XML File Length - Post Sanitze: " + newS.length());
		log.debug("Sanitization Complete");
		try {
			log.trace("Writing to output");
			getOsWriter().write(newS);
			//file close statements.  Warning, not closing the file will leave incomplete xml files and break the translate method
			getOsWriter().write("\n");
			getOsWriter().flush();
			log.trace("Writing complete");
		} catch(IOException e) {
			log.error("Unable to write XML to record.", e);
		}
	}
	
	@Override
	protected int getLatestRecord() {
		return Integer.parseInt(runESearch("1:8000[dp]", false)[3]);
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new PubmedHTTPFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(), e);
		}
	}
}