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
package org.vivoweb.harvester.fetch;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet_type0;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;

/**
 * Module for fetching PubMed Citations using the PubMed SOAP Interface
 * Based on the example code available at the PubMed Website.
 * @author Stephen V. Williams (swilliams@ctrip.ufl.edu)
 * @author Dale R. Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class PubmedFetch extends NIHFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(PubmedFetch.class);
	/**
	 * The name of the PubMed database
	 */
	private static String database = "pubmed";
	/**
	 * a base xmlrecordoutputstream
	 */
	private static XMLRecordOutputStream baseXMLROS = new XMLRecordOutputStream("PubmedArticle", "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<PMID>(.*?)</PMID>.*?", null, PubmedFetch.class);
	
	/**
	 * Constructor:
	 * Primary method for running a PubMed Fetch. The email address of the person responsible
	 * for this install of the program is required by NIH guidelines so the person can be
	 * contacted if there is a problem, such as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param outStream output stream to write to
	 * @throws IOException error finding latest record
	 */
	public PubmedFetch(String emailAddress, OutputStream outStream) throws IOException {
		super(emailAddress,outStream,database);
		setMaxRecords(getLatestRecord()+"");
	}
	
	/**
	 * Constructor:
	 * Primary method for running a PubMed Fetch. The email address of the person responsible
	 * for this install of the program is required by NIH guidelines so the person can be
	 * contacted if there is a problem, such as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param searchTerm query to run on pubmed data
	 * @param maxRecords maximum number of records to fetch
	 * @param batchSize number of records to fetch per batch
	 * @param outStream output stream to write to
	 */
	public PubmedFetch(String emailAddress, String searchTerm, String maxRecords, String batchSize, OutputStream outStream) {
		super(emailAddress, searchTerm, maxRecords, batchSize, outStream, database);
	}
	
	/**
	 * Constructor:
	 * Primary method for running a PubMed Fetch. The email address of the person responsible
	 * for this install of the program is required by NIH guidelines so the person can be
	 * contacted if there is a problem, such as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param searchTerm query to run on pubmed data
	 * @param maxRecords maximum number of records to fetch
	 * @param batchSize number of records to fetch per batch
	 * @param rh record handler to write to
	 */
	public PubmedFetch(String emailAddress, String searchTerm, String maxRecords, String batchSize, RecordHandler rh) {
		super(emailAddress, searchTerm, maxRecords, batchSize, baseXMLROS.clone().setRecordHandler(rh), database);
	}
	
	/**
	 * Constructor
	 * @param args commandline argument
	 * @throws IOException error creating task
	 */
	public PubmedFetch(String[] args) throws IOException {
		this(new ArgList(getParser("PubmedFetch"), args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public PubmedFetch(ArgList argList) throws IOException {
		super(argList, database, baseXMLROS.clone());
	}
	
	@Override
	public void fetchRecords(String WebEnv, String QueryKey, String retStart, String numRecords) throws IOException {
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		req.setQuery_key(QueryKey);
		req.setWebEnv(WebEnv);
		req.setEmail(getEmailAddress());
		req.setTool(getToolName());
		req.setRetstart(retStart);
		req.setRetmax(numRecords);
		int retEnd = Integer.parseInt(retStart) + Integer.parseInt(numRecords);
		log.info("Fetching " + retStart + " to " + retEnd + " records from search");
		try {
			serializeFetchRequest(req);
		}catch(RemoteException e) {
			throw new IOException("Could not run search",e);
		}
	}
	
	/**
	 * Runs, sanitizes, and outputs the results of a EFetch request to the xmlWriter
	 * @param req the request to run and output results
	 * @throws IOException Unable to write XML to record
	 */
	private void serializeFetchRequest(EFetchPubmedServiceStub.EFetchRequest req) throws IOException {
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
			throw new IOException("Unable to write to output", e);
		} catch(UnsupportedEncodingException e) {
			throw new IOException("Cannot get xml from buffer", e);
		}
	}
	
	/**
	 * Sanitizes XML in preparation for writing to output stream
	 * Removes xml namespace attributes, XML wrapper tag, and splits each record on a new line
	 * @param strInput The XML to Sanitize.
	 * @throws IOException Unable to write XML to record
	 */
	private void sanitizeXML(String strInput) throws IOException {
		log.debug("Sanitizing Output");
		log.debug("XML File Length - Pre Sanitize: " + strInput.length());
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle");
		log.debug("XML File Length - Post Sanitze: " + newS.length());
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
		InitLog.initLogger(PubmedFetch.class);
		log.info("PubmedFetch: Start");
		try {
			new PubmedFetch(args).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(),e);
			System.out.println(getParser("PubmedFetch").getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
		log.info("PubmedFetch: End");
	}
}