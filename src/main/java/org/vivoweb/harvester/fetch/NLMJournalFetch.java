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

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchJournalsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchJournalsServiceStub.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchJournalsServiceStub.SerialsSet_type0;
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
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;

/**
 * NLMJournalFetch
 */
public class NLMJournalFetch extends NIHFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(NLMJournalFetch.class);
	/**
	 * The name of the NLM Journals Database
	 */
	private static String database = "???NLMJouranals???";
	
	/**
	 * Constructor: Primary method for running a Journal Fetch. The email address of the person responsible for this
	 * install of the program is required by NIH guidelines so the person can be contacted if there is a problem, such
	 * as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param outStream output stream to write to
	 */
	public NLMJournalFetch(String emailAddress, OutputStream outStream) {
		super(emailAddress, outStream, database);
	}
	
	/**
	 * Constructor: Primary method for running a Journal Fetch. The email address of the person responsible for this
	 * install of the program is required by NIH guidelines so the person can be contacted if there is a problem, such
	 * as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param searchTerm query to run on pubmed data
	 * @param maxRecords maximum number of records to fetch
	 * @param batchSize number of records to fetch per batch
	 * @param outStream output stream to write to
	 */
	public NLMJournalFetch(String emailAddress, String searchTerm, String maxRecords, String batchSize, OutputStream outStream) {
		super(emailAddress, searchTerm, maxRecords, batchSize, outStream, database);
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public NLMJournalFetch(String[] args) throws IOException {
		this(new ArgList(getParser("NLMJournalFetch"), args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public NLMJournalFetch(ArgList argList) throws IOException {
		super(argList, database, new XMLRecordOutputStream("Serial", "<?xml version=\"1.0\"?>\n<!DOCTYPE SerialSet PUBLIC \"-//NLM//DTD Serial, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<SerialSet>\n", "\n</SerialSet>", ".*?<NlmUniqueID>(.*?)</NlmUniqueID>.*?", null, NLMJournalFetch.class));
	}
	
	@Override
	public void fetchRecords(String WebEnv, String QueryKey, String retStart, String numRecords) throws IOException {
		EFetchJournalsServiceStub.EFetchRequest req = new EFetchJournalsServiceStub.EFetchRequest();
		req.setQuery_key(QueryKey);
		req.setWebEnv(WebEnv);
		req.setEmail(getEmailAddress());
		req.setTool(getToolName());
		req.setRetstart(retStart);
		req.setRetmax(numRecords);
		log.info("Fetching records from search");
		try {
			serializeFetchRequest(req);
		} catch(RemoteException e) {
			throw new IOException("Could not run search", e);
		}
	}
	
	/**
	 * Runs, sanitizes, and outputs the results of a EFetch request to the xmlWriter
	 * @param req the request to run and output results
	 * @throws RemoteException error running EFetch
	 */
	private void serializeFetchRequest(EFetchJournalsServiceStub.EFetchRequest req) throws RemoteException {
		//Create buffer for raw, pre-sanitized output
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		//Connect to NLM
		EFetchJournalsServiceStub service = new EFetchJournalsServiceStub();
		//Run the EFetch request
		EFetchResult result = service.run_eFetch(req);
		//Get the article set
		SerialsSet_type0 serialSet = result.getSerialsSet();
		XMLStreamWriter writer;
		try {
			//Create a temporary xml writer to our buffer
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
			MTOMAwareXMLSerializer serial = new MTOMAwareXMLSerializer(writer);
			log.debug("Buffering records");
			//Output data
			serialSet.serialize(new QName("RemoveMe"), null, serial);
			serial.flush();
			log.debug("Buffering complete");
			log.debug("buffer size: " + buffer.size());
			//Dump buffer to String
			String iString = buffer.toString("UTF-8");
			//Sanitize string (which writes it to xmlWriter)
			sanitizeXML(iString);
		} catch(XMLStreamException e) {
			log.error("Unable to write to output", e);
		} catch(UnsupportedEncodingException e) {
			log.error("Cannot get xml from buffer", e);
		}
	}
	
	/**
	 * Sanitizes XML in preparation for writing to output stream Removes xml namespace attributes, XML wrapper tag, and
	 * splits each record on a new line
	 * @param strInput The XML to Sanitize.
	 */
	private void sanitizeXML(String strInput) {
		log.debug("Sanitizing Output");
		log.debug("XML File Length - Pre Sanitize: " + strInput.length());
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</Serial>.*?<Serial", "</Serial>\n<Serial");
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
			log.error("Unable to write XML to file.", e);
		}
	}
	
	@Override
	protected int getLatestRecord() throws IOException {
		//FIXME: make this work for NLM Journal Fetch? Is relevant? if not, try to move out of NIHFetch
		return Integer.parseInt(runESearch("1:8000[dp]", false)[3]);
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(args, getParser("NLMJournalFetch"));
			log.info("NLMJournalFetch: Start");
			new NLMJournalFetch(args).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser("NLMJournalFetch").getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info("NLMJournalFetch: End");
		}
	}
	
}
