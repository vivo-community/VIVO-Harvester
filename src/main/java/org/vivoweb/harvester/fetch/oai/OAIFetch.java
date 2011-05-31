/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.fetch.oai;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.IterableAide;
import org.vivoweb.harvester.util.XMLAide;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.ctc.wstx.io.EBCDICCodec;

/**
 * Class for harvesting from OAI Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
@SuppressWarnings("unused")
public class OAIFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(OAIFetch.class);
//	/**
//	 * The website address of the OAI Repository without the protocol prefix (No http://)
//	 */
//	private String strAddress;
//	/**
//	 * The start date for the range of records to pull, format is YYYY-MM-DD<br>
//	 * If time is required, format is YYYY-MM-DDTHH:MM:SS:MSZ<br>
//	 * Some repositories do not support millisecond resolution.<br>
//	 * Example 2010-01-15T13:45:12:50Z<br>
//	 */
//	private String strStartDate;
//	/**
//	 * The end date for the range of records to pull, format is YYYY-MM-DD<br>
//	 * If time is required, format is YYYY-MM-DDTHH:MM:SS:MSZ<br>
//	 * Some repositories do not support millisecond resolution.<br>
//	 * Example 2010-01-15T13:45:12:50Z<br>
//	 */
//	private String strEndDate;
	/**
	 * The output stream to send the harvested XML to
	 */
	private RecordHandler rhOutput;
	/**
	 * The full request url
	 */
	private String requestURL;
	
	/**
	 * Constuctor
	 * @param address The website address of the repository, without http://
	 * @param output The recordhander to write to
	 */
	public OAIFetch(String address, RecordHandler output) {
		this(address, "0001-01-01", "8000-01-01", output);
	}
	
	/**
	 * Constructor
	 * @param address The website address of the repository, without http://
	 * @param startDate The date at which to begin fetching records, format and time resolution depends on repository.
	 * @param endDate The date at which to stop fetching records, format and time resolution depends on repository.
	 * @param output The recordhandler to write to
	 */
	public OAIFetch(String address, String startDate, String endDate, RecordHandler output) {
		this.rhOutput = output;
        StringBuffer requestSb =  new StringBuffer(address.toString());
        requestSb.append("?verb=ListRecords");
        this.requestURL = requestSb.toString();
	}
	
	/**
	 * Get the url for fetching a date range
	 * @param startDate start date
	 * @param endDate end date
	 * @throws IOException error getting recrords
	 */
	public void fetchRange(String startDate, String endDate) throws IOException {
		log.info("Fetching records from OAI Repository");
		RawWriteRun(this.requestURL, startDate, endDate, "oai_dc", "", this.rhOutput);
	}
	
//	/**
//	 * Executes the task
//	 * @param resumptionToken the token for the oai repository to continue the same search
//	 * @throws IOException error getting recrords
//	 */
//	private void fetchResume(String resumptionToken) throws IOException {
//		StringBuilder requestSb = new StringBuilder(this.requestURL);
//        requestSb.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, "UTF-8"));
//        return requestSb.toString();
//		RawWriteRun(requestSb.toString(), null, null, "oai_dc", "", this.rhOutput);
//	}
//    
//    /**
//     * Get the oai:resumptionToken from the response
//     * 
//     * @return the oai:resumptionToken value
//     * @throws TransformerException
//     * @throws NoSuchFieldException
//     */
//    private String getResumptionToken() throws TransformerException, NoSuchFieldException {
//        String schemaLocation = getSchema();
//        if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
//            return getSingleString("/oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken");
//        } else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
//            return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:resumptionToken");
//        } else {
//            throw new NoSuchFieldException(schemaLocation);
//        }
//    }
	
	/**
	 * Modified version of org.oclc.oai.harvester2.app.RawWrite.run()
	 * @param baseURL the server baseurl
	 * @param from from date
	 * @param until until date
	 * @param metadataPrefix metadata prefix code
	 * @param setSpec ??
	 * @param out output recordhandler
	 * @throws IOException error
	 */
	@SuppressWarnings("null")
	private static void RawWriteRun(String baseURL, String from, String until, String metadataPrefix, String setSpec, RecordHandler out) throws IOException {
		try {
			OAIHarvester listRecords = null;// = new OAIHarvester(OAIHarvester.getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
//			ListRecords listRecords = new ListRecords(baseURL, from, until, setSpec, metadataPrefix);
	        while(listRecords != null) {
				NodeList errors = null;// = listRecords.getErrors();
				if(errors != null && errors.getLength() > 0) {
					int length = errors.getLength();
					for(int i = 0; i < length; ++i) {
						Node item = errors.item(i);
						log.error("Error processing node:\n"+item);
					}
					break;
				}
				NodeList nodes = null;// = listRecords.getNodeList("/oai20:OAI-PMH/oai20:ListRecords/oai20:record");
				System.out.println(nodes.getLength());
				for(Node n : IterableAide.adapt(nodes)) {
		            String nodeId = XPathAPI.selectSingleNode(n, "./oai20:header/oai20:identifier/text()", OAIHarvester.namespaceNode).getNodeValue();
			        StringBuilder nodeSb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<harvest>\n");
					Source input = new DOMSource(n);
			        StringWriter sw = new StringWriter();
			        Result output = new StreamResult(sw);
			        try {
			        	Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
			            idTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			            idTransformer.transform(input, output);
			        } catch (TransformerException e) {
			        	throw new Error(e);
			        }
			        nodeSb.append(sw.toString());
			        nodeSb.append("\n</harvest>\n");
		            System.out.println(nodeSb);
		            out.addRecord(nodeId, nodeSb.toString(), OAIFetch.class);
				}
				String resumptionToken = listRecords.getResumptionToken();
				System.out.println("resumptionToken: " + resumptionToken);
				if(resumptionToken == null || resumptionToken.length() == 0) {
					listRecords = null;
				} else {
					listRecords = null;//new ListRecords(baseURL, resumptionToken);
				}
			}
//		} catch(ParserConfigurationException e) {
//			throw new IOException(e);
//		} catch(SAXException e) {
//			throw new IOException(e);
		} catch(TransformerException e) {
			throw new IOException(e);
//		} catch(NoSuchFieldException e) {
//			throw new IOException(e);
		}
	}
}