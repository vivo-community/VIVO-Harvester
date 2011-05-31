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
import java.io.UnsupportedEncodingException;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for harvesting from OAI Data Sources
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
@SuppressWarnings("unused")
public class OAIHarvester {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(OAIFetch.class);
	/**
	 * oai namespace base
	 */
	private static final String oains = "http://www.openarchives.org/OAI/";
	/**
	 * oai2.0 namespace
	 */
	public static final String OAI20 = oains+"2.0/ "+oains+"2.0/OAI-PMH.xsd";
	/**
	 * oai1.1 get record namespace
	 */
	public static final String OAI11_GET_RECORD = oains+"1.1/OAI_GetRecord "+oains+"1.1/OAI_GetRecord.xsd";
	/**
	 * oai1.1 identify namespace
	 */
	public static final String OAI11_IDENTIFY = oains+"1.1/OAI_Identify "+oains+"1.1/OAI_Identify.xsd";
	/**
	 * oai1.1 list identifiers namespace
	 */
	public static final String OAI11_LIST_IDENTIFIERS = oains+"1.1/OAI_ListIdentifiers "+oains+"1.1/OAI_ListIdentifiers.xsd";
	/**
	 * oai1.1 list metadata formats namespace
	 */
	public static final String OAI11_LIST_METADATA_FORMATS = oains+"1.1/OAI_ListMetadataFormats "+oains+"1.1/OAI_ListMetadataFormats.xsd";
	/**
	 * oai1.1 list records namespace
	 */
	public static final String OAI11_LIST_RECORDS = oains+"1.1/OAI_ListRecords "+oains+"1.1/OAI_ListRecords.xsd";
	/**
	 * oai1.1 list sets namespace
	 */
	public static final String OAI11_LIST_SETS = oains+"1.1/OAI_ListSets "+oains+"1.1/OAI_ListSets.xsd";
	/**
	 * Element to hold namespace information 
	 */
	protected static Element namespaceNode = null;
	/**
	 * current schema
	 */
	private String schema = null;
	/**
	 * the url to pull from
	 */
	private String baseURL = null;
	/**
	 * RecordHandler to write records to
	 */
	private RecordHandler rh = null;
	/**
	 * XMLAide for searching
	 */
	protected XMLAide xmlS;
	
	static {
		/* Load DOM Document */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			throw new Error(e);
		}
		String oclcHarv = "http://www.oclc.org/research/software/oai/harvester";
		String xmlns = "http://www.w3.org/2000/xmlns/";
		String oai11 = "xmlns:oai11_";
		namespaceNode = builder.getDOMImplementation().createDocument(oclcHarv, "harvester:namespaceHolder", null).getDocumentElement();
		namespaceNode.setAttributeNS(xmlns, "xmlns:harvester", oclcHarv);
		namespaceNode.setAttributeNS(xmlns, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		namespaceNode.setAttributeNS(xmlns, "xmlns:oai20", oains+"2.0/");
		namespaceNode.setAttributeNS(xmlns, oai11 + "GetRecord", oains + "1.1/OAI_GetRecord");
		namespaceNode.setAttributeNS(xmlns, oai11 + "Identify", oains + "1.1/OAI_Identify");
		namespaceNode.setAttributeNS(xmlns, oai11 + "ListIdentifiers", oains + "1.1/OAI_ListIdentifiers");
		namespaceNode.setAttributeNS(xmlns, oai11 + "ListMetadataFormats", oains + "1.1/OAI_ListMetadataFormats");
		namespaceNode.setAttributeNS(xmlns, oai11 + "ListRecords", oains + "1.1/OAI_ListRecords");
		namespaceNode.setAttributeNS(xmlns, oai11 + "ListSets", oains + "1.1/OAI_ListSets");
	}
	
	/**
	 * Get the oai:resumptionToken from the response
	 * @return the oai:resumptionToken value
	 * @throws IOException error
	 */
	public String getResumptionToken() throws IOException {
		try {
			if(this.schema.indexOf(OAI20) != -1) {
				return this.xmlS.getXObject("/oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken").str();
			} else if(this.schema.indexOf(OAI11_LIST_RECORDS) != -1) {
				return this.xmlS.getXObject("/oai11_ListRecords:ListRecords/oai11_ListRecords:resumptionToken").str();
			} else {
				throw new IOException(this.schema);
			}
		} catch(TransformerException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Construct the query portion of the http request
	 * @param baseURL a
	 * @param from a
	 * @param until a
	 * @param set a
	 * @param metadataPrefix a
	 * @return a String containing the query portion of the http request
	 */
	private static String getRequestURL(String baseURL, String from, String until, String set, String metadataPrefix) {
		StringBuffer requestURL = new StringBuffer(baseURL);
		requestURL.append("?verb=ListRecords");
		if(from != null)
			requestURL.append("&from=").append(from);
		if(until != null)
			requestURL.append("&until=").append(until);
		if(set != null)
			requestURL.append("&set=").append(set);
		requestURL.append("&metadataPrefix=").append(metadataPrefix);
		return requestURL.toString();
	}
	
	/**
	 * Construct the query portion of the http request (resumptionToken version)
	 * @param baseURL a
	 * @param resumptionToken a
	 * @return a
	 * @throws UnsupportedEncodingException a
	 */
	private static String getRequestURL(String baseURL, String resumptionToken) throws UnsupportedEncodingException {
		StringBuffer requestURL = new StringBuffer(baseURL);
		requestURL.append("?verb=ListRecords");
		requestURL.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, "UTF-8"));
		return requestURL.toString();
	}
	
	/**
	 * Get the OAI errors
	 * @return a NodeList of /oai:OAI-PMH/oai:error elements
	 * @throws IOException error getting errors
	 */
	private NodeList getErrors() throws IOException {
		if(this.schema.equals(OAI20)) {
			try {
				return this.xmlS.getXObject("/oai20:OAI-PMH/oai20:error").nodelist();
			} catch(TransformerException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		return null;
	}
	
	/**
	 * Get the url for fetching a date range
	 * @param startDate start date
	 * @param endDate end date
	 * @throws IOException error getting recrords
	 */
	public void fetchRange(String startDate, String endDate) throws IOException {
		try{
			log.info("Fetching records from OAI Repository");
			harvest(getRequestURL(this.baseURL, startDate, endDate, "", "oai_dc"));
			while(true) {
				NodeList errors = getErrors();
				if(errors != null && errors.getLength() > 0) {
					int length = errors.getLength();
					for(int i = 0; i < length; ++i) {
						Node item = errors.item(i);
						log.error("Error processing node:\n"+item);
					}
					break;
				}
				NodeList nodes = this.xmlS.getXObject("/oai20:OAI-PMH/oai20:ListRecords/oai20:record").nodelist();
				System.out.println(nodes.getLength());
				for(Node n : IterableAide.adapt(nodes)) {
		            String nodeId = XPathAPI.selectSingleNode(n, "./oai20:header/oai20:identifier/text()", namespaceNode).getNodeValue();
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
		            this.rh.addRecord(nodeId, nodeSb.toString(), OAIFetch.class);
				}
				String resumptionToken = getResumptionToken();
				System.out.println("resumptionToken: " + resumptionToken);
				if(resumptionToken == null || resumptionToken.length() == 0) {
					break;
				}
				harvest(getRequestURL(this.baseURL, resumptionToken));
			}
		} catch(TransformerException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Constructor
	 * @param baseURL the base url of the repository
	 * @param out RecordHandler to write records to
	 */
	public OAIHarvester(String baseURL, RecordHandler out) {
		this.baseURL = baseURL;
		this.rh = out;
	}
	
	/**
	 * Preforms the OAI request
	 * @param reqURL a
	 * @throws IOException a
	 * @throws TransformerException a
	 */
	private void harvest(String reqURL) throws IOException, TransformerException {
		log.debug("requestURL=" + reqURL);
		InputStream in = null;
		URL url = new URL(reqURL);
		HttpURLConnection con = null;
		int respCode = 0;
		do {
			con = (HttpURLConnection)url.openConnection();
			con.setRequestProperty("User-Agent", "VIVOHarvester/OAIHarvester");
			con.setRequestProperty("Accept-Encoding", "compress, gzip, identify");
			try {
				respCode = con.getResponseCode();
				log.debug("responseCode=" + respCode);
			} catch(FileNotFoundException e) {
				// assume it's a 503 response
				log.info(reqURL, e);
				respCode = HttpURLConnection.HTTP_UNAVAILABLE;
			}
			
			if(respCode == HttpURLConnection.HTTP_UNAVAILABLE) {
				long retrySecs = con.getHeaderFieldInt("Retry-After", -1);
				if(retrySecs == -1) {
					long now = System.currentTimeMillis();
					long retryDate = con.getHeaderFieldDate("Retry-After", now);
					retrySecs = retryDate - now;
				}
				if(retrySecs == 0) { // Apparently, it's a bad URL
					throw new FileNotFoundException("Bad URL?");
				}
				System.err.println("Server response: Retry-After=" + retrySecs);
				if(retrySecs > 0) {
					try {
						Thread.sleep(retrySecs * 1000);
					} catch(InterruptedException e) {
						log.error(e.getMessage(),e);
					}
				}
			}
		} while(respCode == HttpURLConnection.HTTP_UNAVAILABLE);
		String contEnc = con.getHeaderField("Content-Encoding");
		log.debug("contentEncoding=" + contEnc);
		if(contEnc.equals("compress")) {
			ZipInputStream zis = new ZipInputStream(con.getInputStream());
			zis.getNextEntry();
			in = zis;
		} else if(contEnc.equals("gzip")) {
			in = new GZIPInputStream(con.getInputStream());
		} else if(contEnc.equals("deflate")) {
			in = new InflaterInputStream(con.getInputStream());
		} else {
			in = con.getInputStream();
		}
		
		this.xmlS = new XMLAide(XMLAide.getDocumentNode(in), namespaceNode);
		
		StringTokenizer tokenizer = new StringTokenizer(this.xmlS.getXObject("/*/@xsi:schema").str(), " ");
		StringBuffer sb = new StringBuffer();
		while(tokenizer.hasMoreTokens()) {
			if(sb.length() > 0)
				sb.append(" ");
			sb.append(tokenizer.nextToken());
		}
		this.schema = sb.toString();
	}
	
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
	protected static void RawWriteRun(String baseURL, String from, String until, String metadataPrefix, String setSpec, RecordHandler out) throws IOException {
		try {
			OAIHarvester hv = null;//new OAIHarvester(OAIHarvester.getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
	        while(hv != null) {
				NodeList errors = hv.getErrors();
				if(errors != null && errors.getLength() > 0) {
					int length = errors.getLength();
					for(int i = 0; i < length; ++i) {
						Node item = errors.item(i);
						log.error("Error processing node:\n"+item);
					}
					break;
				}
				NodeList nodes = hv.xmlS.getXObject("/oai20:OAI-PMH/oai20:ListRecords/oai20:record").nodelist();
				System.out.println(nodes.getLength());
				for(Node n : IterableAide.adapt(nodes)) {
		            String nodeId = XPathAPI.selectSingleNode(n, "./oai20:header/oai20:identifier/text()", namespaceNode).getNodeValue();
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
				String resumptionToken = hv.getResumptionToken();
				System.out.println("resumptionToken: " + resumptionToken);
				if(resumptionToken == null || resumptionToken.length() == 0) {
					hv = null;
				} else {
					hv = null;//new OAIHarvester(OAIHarvester.getRequestURL(baseURL, resumptionToken));
				}
			}
		} catch(TransformerException e) {
			throw new IOException(e);
//		} catch(NoSuchFieldException e) {
//			throw new IOException(e);
		}
	}
}
