package org.vivoweb.ingest.fetch;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchJournalsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchJournalsServiceStub.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchJournalsServiceStub.SerialsSet_type0;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.apache.commons.io.output.NullOutputStream;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.XMLRecordOutputStream;
import org.xml.sax.SAXException;

public class NLMJournalFetch extends NIHFetch {

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
	public NLMJournalFetch(String strEmail, String strToolLoc, OutputStream outStream) {
		super(strEmail,strToolLoc,outStream);
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
	public NLMJournalFetch(String strEmail, String strToolLoc, String searchTerm, String maxRecords, String batchSize, OutputStream outStream)
	{
		super(strEmail,strToolLoc,searchTerm,maxRecords,batchSize,outStream);
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	public NLMJournalFetch(ArgList argList) throws IOException {
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
	public void fetchJournals(String WebEnv, String QueryKey, String retStart, String numRecords) {
		EFetchJournalsServiceStub.EFetchRequest req = new EFetchJournalsServiceStub.EFetchRequest();
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
	public void fetchJournals(String[] env) throws IllegalArgumentException {
		if(env.length < 3) {
			throw new IllegalArgumentException("Invalid env. Must contain {WebEnv, QueryKey, number of records found}");
		}
		fetchJournals(env[0], env[1], "0", env[2]);
	}
	
	/**
	 * Performs a PubMed Fetch using a previously defined esearch environment and querykey
	 * @param env {WebEnv, QueryKey, number of records found} - from ESearch
	 * @param retStart record number (out of the total - eg: '1200' out of 15000 records), not the PMID 
	 * @param numRecords The number of records to fetch
	 * @throws IllegalArgumentException env is invalid
	 */
	public void fetchJournals(String[] env, String retStart, String numRecords) throws IllegalArgumentException {
		if(env.length < 2) {
			throw new IllegalArgumentException("Invalid env. Must contain {WebEnv, QueryKey}");
		}
		fetchJournals(env[0], env[1], retStart, numRecords);
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
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "").replaceAll("</?RemoveMe>", "").replaceAll("</Serial>.*?<Serial", "</Serial>\n<Serial");
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
	private void serializeFetchRequest(EFetchJournalsServiceStub.EFetchRequest req) throws RemoteException {
		//Create buffer for raw, pre-sanitized output
		ByteArrayOutputStream buffer=new ByteArrayOutputStream();
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
			//todo change
			recToFetch = 10000;
		} else {
			recToFetch = Integer.parseInt(this.strMaxRecords);
		}
		int intBatchSize = Integer.parseInt(this.strBatchSize); 
		if(recToFetch <= intBatchSize) {
			fetchJournals(runESearch(this.strSearchTerm, recToFetch, "journals"));
		} else {
			String[] env = runESearch(this.strSearchTerm, recToFetch,"journals");
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
				fetchJournals(WebEnv, QueryKey, startRec+"", maxRec+"");
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
			new NLMJournalFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(),e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}

}
