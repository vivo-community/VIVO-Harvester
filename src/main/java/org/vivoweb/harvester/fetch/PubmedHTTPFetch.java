/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
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
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(PubmedHTTPFetch.class);
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
	 * @throws IOException error finding latest record
	 */
	public PubmedHTTPFetch(String emailAddress, OutputStream outStream) throws IOException {
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
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public PubmedHTTPFetch(String[] args) throws IOException {
		this(new ArgList(getParser("PubmedHTTPFetch"), args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public PubmedHTTPFetch(ArgList argList) throws IOException {
		super(argList, database, new XMLRecordOutputStream("PubmedArticle", "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<[pP][mM][iI][dD].*?>(.*?)</[pP][mM][iI][dD]>.*?", null, PubmedHTTPFetch.class));
	}
	
	@Override
	public String[] runESearch(String term, boolean logMessage) throws IOException {
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
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		}
		return env;
	}
	
	@Override
	public void fetchRecords(String WebEnv, String QueryKey, String retStart, String numRecords) throws IOException {
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
		int retEnd = Integer.parseInt(retStart) + Integer.parseInt(numRecords);
		log.info("Fetching " + retStart + " to " + retEnd + " records from search");
		try {
			sanitizeXML(getURLContents(urlSb.toString()));
		} catch(MalformedURLException e) {
			throw new IOException("Query URL incorrectly formatted", e);
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
	 * @throws IOException Unable to write XML to record
	 */
	private void sanitizeXML(String strInput) throws IOException {
		//used to remove header from xml
		String headerRegEx = "<\\?xml.*?PubmedArticleSet>";
		//used to remove footer from xml
		String footerRegEx = "</PubmedArticleSet>";
		log.debug("Sanitizing Output");
		log.debug("XML File Length - Pre Sanitize: " + strInput.length());
		//		System.out.println("===============================\n          PRE-SANITIZE         \n===============================\n");
		//		System.out.println(strInput);
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle").replaceAll(headerRegEx, "").replaceAll(footerRegEx, "");
		log.debug("XML File Length - Post Sanitze: " + newS.length());
		//		System.out.println("===============================\n          POST-SANITIZE        \n===============================\n");
		//		System.out.println(newS);
		log.debug("Sanitization Complete");
		log.trace("Writing to output");
		getOsWriter().write(newS);
		//file close statements.  Warning, not closing the file will leave incomplete xml files and break the translate method
		getOsWriter().write("\n");
		getOsWriter().flush();
		log.trace("Writing complete");
	}
	
	@Override
	protected int getLatestRecord() throws IOException {
		return Integer.parseInt(runESearch("1:8000[dp]", false)[3]);
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(PubmedHTTPFetch.class, args, getParser("PubmedHTTPFetch"));
			log.info("PubmedHTTPFetch: Start");
			new PubmedHTTPFetch(args).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser("PubmedHTTPFetch").getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info("PubmedHTTPFetch: End");
		}
	}
}