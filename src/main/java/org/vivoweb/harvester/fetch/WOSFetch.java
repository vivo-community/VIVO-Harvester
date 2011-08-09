/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.XPathTool;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Fetches SOAP-XML data from a SOAP compatible site placing the data in the supplied file.
 */
public class WOSFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(WOSFetch.class);
	/**
	 * RecordHandler to put data in.
	 */
	private RecordHandler outputRH;
	
	/**
	 * URL to send Authorization message to
	 */
	private URL authUrl;
	
	/**
	 * URL to send Authorization message to
	 */
	private URL searchUrl;

	/**
	 * Inputstream with SOAP style XML message to get authorization
	 */
	private InputStream authFile;

	/**
	 * Inputstream with SOAP style XML message to perform the search
	 */
	private InputStream searchFile;
	/**
	 * String of XML gathered from the inputStream
	 */
	private String xmlString;
	
	/**
	 * The authentication sessionID
	 */
	private String sessionID;
	
	/**
	 * Constructor
	 * @param url address to connect with
	 * @param outputRH 
	 * @param xmlAuthFile 
	 * @param xmlSearchFile 
	 * @throws IOException error talking with database
	 */
	public WOSFetch(URL authurl,URL searchurl, RecordHandler outputRH, String xmlAuthFile, String xmlSearchFile) throws IOException {
		this(authurl,searchurl, outputRH, FileAide.getInputStream( xmlAuthFile ), FileAide.getInputStream( xmlSearchFile ) );
	}
	
	
	/**
	 * Command line Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private WOSFetch(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Arglist Constructor
	 * @param args option set of parsed args
	 * @throws IOException error creating task
	 */
	private WOSFetch(ArgList args) throws IOException {
		this(
			new URL(args.get("u")), 
			new URL(args.get("c")), 
			RecordHandler.parseConfig(args.get("o"), args.getValueMap("O")),
			FileAide.getInputStream(args.get("a")),
			FileAide.getInputStream(args.get("s"))
		);
	}
	
	/**
	 * Library style Constructor
	 * @param authorizationUrl 
	 * @param searchUrl 
	 * @param output The stream to the output file
	 * @param xmlAuthStream 
	 * @param xmlSearchStream 
	 * @throws IOException problem with opening url connection
	 */
	public WOSFetch(URL authorizationUrl, URL searchUrl, RecordHandler output, InputStream xmlAuthStream, InputStream xmlSearchStream) throws IOException {
		
		this.outputRH = output;
		this.authUrl = authorizationUrl;
		this.authFile = xmlAuthStream;
		this.searchUrl = searchUrl;
		this.searchFile = xmlSearchStream;
		
		log.debug("Checking for NULL values");
		if(this.outputRH == null) {
			log.debug("Outputfile = null");
			log.error("Must provide output file!");
		}else{
			log.debug("Outputfile = " + this.outputRH.toString());
		}
		
		if(this.authFile == null) {
			log.debug("Authorization = null");
			log.error("Must provide authorization message file!");
		}else{
			log.debug("Authorization = " + this.authFile.toString());
		}
		
		if(this.searchFile == null) {
			log.debug("Search = null");
			log.error("Must provide Search message file!");
		}else{
			log.debug("Search = " + this.searchFile.toString());
		}
		
		if(this.authUrl == null) {
			log.debug("URL = null");
			log.error("Must provide authorization site url!");
		}else{
			log.debug("URL = "+ this.authUrl.toString());
		}
		
		if(this.searchUrl == null) {
			log.debug("URL = null");
			log.error("Must provide Search site url!");
		}else{
			log.debug("URL = "+ this.searchUrl.toString());
		}
		
		if(this.sessionID == null) {
			log.debug("SessionID = null");
			this.sessionID = "";
		}else{
			log.debug("SessionID = " + this.sessionID);
		}
		
	}
	private String documentToString(Document document){
		StreamResult result =null;
		try {
		Transformer	transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		result = new StreamResult(new StringWriter());
		DOMSource domSource = new DOMSource(document);
		transformer.transform(domSource, result);
		} catch(TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	return result.getWriter().toString();
	}
	
	private String getnextQuery(String previousQuery){
		String nextQuery = "";
		try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		Document searchDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(previousQuery.getBytes()) );
		log.debug("searchDoc:");
		log.debug(documentToString(searchDoc));
		
		NodeList firstrecordNodes = searchDoc.getElementsByTagName("firstRecord");
		log.debug("Node length = " + firstrecordNodes.getLength());
		Node firstnode = firstrecordNodes.item(0);
		int firstrecord = Integer.parseInt(firstnode.getTextContent());
		log.debug("firstrecord = " + firstrecord);
		
		NodeList countNodes = searchDoc.getElementsByTagName("count");
		log.debug("Node length = " + countNodes.getLength());
		Node countnode = countNodes.item(0);
		int count = Integer.parseInt(countnode.getTextContent());
		log.debug("count= " + count);
		
		firstnode.setTextContent(""+(firstrecord + count));

		log.debug("searchDoc:");
		nextQuery = documentToString(searchDoc);
		
		} catch(SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextQuery;
		
	}
	
	
	/**
	 * Executes the task
	 * @throws IOException error processing record handler or jdbc connection
	 */
	public void execute() throws IOException {
		String searchQuery = IOUtils.toString(this.searchFile);
		ByteArrayOutputStream authResponse = new ByteArrayOutputStream();
		{
			SOAPFetch soapfetch = new SOAPFetch(this.authUrl,authResponse,this.authFile,"");
			soapfetch.execute();
		}
		
		String authCode = XPathTool.getXpathStreamResult(new ByteArrayInputStream(authResponse.toByteArray()), "//return");
		
		ByteArrayOutputStream searchResponse = new ByteArrayOutputStream();
		{
			SOAPFetch soapfetch = new SOAPFetch(this.searchUrl,searchResponse,new ByteArrayInputStream(searchQuery.getBytes()),authCode);
			soapfetch.execute();
		}
		
		String recFound = XPathTool.getXpathStreamResult(new ByteArrayInputStream(searchResponse.toByteArray()), "//recordsFound");
		String searchCount = XPathTool.getXpathStreamResult(new ByteArrayInputStream(searchQuery.getBytes()), "//retrieveParameters/count");
		log.debug("Search count = \"" + searchCount + "\"");
		log.debug("Records Found = \"" + recFound + "\"");
		int recordsFound = Integer.parseInt(recFound);
		int searchCnt = Integer.parseInt(searchCount);
		log.debug("Records left = " + (recordsFound - searchCnt));
		if((recordsFound - searchCnt) > 0){
			getnextQuery(searchQuery);
		}
		

	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("WOSFetch");
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("authurl").withParameter(true, "URL").setDescription("The URL which will receive the AUTHMESSAGE.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("searchconnection").withParameter(true, "URL").setDescription("The URL which will receive the AUTHMESSAGE.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("searchmessage").withParameter(true, "SEARCHMESSAGE").setDescription("The SEARCHMESSAGE file path.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "OUTPUT_FILE").setDescription("XML result file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("authenticationmessage").withParameter(true, "AUTHMESSAGE").setDescription("The authentication session ID").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new WOSFetch(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
