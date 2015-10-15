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
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.SOAPMessenger;
import org.vivoweb.harvester.util.XPathTool;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
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
	 * URL to send Authorization message to
	 */
	private URL lamrUrl;

	/**
	 * Inputstream with SOAP style XML message to get authorization
	 */
	private InputStream authMessage;

	/**
	 * Inputstream with SOAP style XML message to close session
	 */
	private InputStream closeMessage;

	/**
	 * Inputstream with SOAP style XML message to perform the search
	 */
	private InputStream searchFile;

	/**
	 * Inputstream with SOAP style XML message to perform the search
	 */
	private InputStream lamrFile;
	
	/**
	 * The String version of the xml message
	 */
	private String lamrMessage;
	
	/**
	 * the set of identifiers which are then requested from the LAMR site.
	 */
	private Set<String> lamrSet;
	
	/**
	 * The authentication sessionID
	 */
	private String sessionID;
	
	/**
	 * This string is where the base64 encoded user name and password combination is to be stored
	 */
	private String usernamePassword;
	
	/**
	 * Constructor
	 * @param authurl The location of the authorization site
	 * @param searchurl The location of the search site
	 * @param lamrurl The location of the links article match retrieval site
	 * @param outputRH The record handler used for storing the harvested records
	 * @param xmlSearchFile the file path to the search query message
	 * @param xmlLamrFile the file path to the links article match retrieval message
	 * @param userPass the user name password string to be base 64 encoded
	 * @throws IOException error talking with database
	 */
	public WOSFetch(URL authurl, URL searchurl, URL lamrurl, RecordHandler outputRH,  String xmlSearchFile, String xmlLamrFile, String userPass) throws IOException {
		init(authurl,searchurl,lamrurl, outputRH,null, FileAide.getInputStream( xmlSearchFile ), FileAide.getInputStream( xmlLamrFile ), userPass );
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
		if(args.has("a")){
			init(
				new URL(args.get("u")), 
				new URL(args.get("c")), 
				new URL(args.get("l")), 
				RecordHandler.parseConfig(args.get("o"), args.getValueMap("O")),
				FileAide.getInputStream(args.get("a")),
				FileAide.getInputStream(args.get("s")),
				FileAide.getInputStream(args.get("m")),
				args.get("p")
			);
		}else{
			init(
				new URL(args.get("u")), 
				new URL(args.get("c")), 
				new URL(args.get("l")), 
				RecordHandler.parseConfig(args.get("o"), args.getValueMap("O")),
				null,
				FileAide.getInputStream(args.get("s")),
				FileAide.getInputStream(args.get("m")),
				args.get("p")
			);
		}
	}
	
	/**
	 * Library style Constructor
	 * @param authorizationUrl The location of the authorization site
	 * @param searchUrl The location of the search site
	 * @param lamrhUrl The location of the links article match retrieval site
	 * @param output The record handler used for storing the harvested records
	 * @param xmlAuthStream the stream to the authorization message
	 * @param xmlSearchStream the stream to the search query message
	 * @param xmlLamrStream the stream to the links article match retrieval message
	 * @param usernamePassword the user name password string to be base 64 encoded
	 */
	public WOSFetch(URL authorizationUrl, URL searchUrl, URL lamrhUrl, RecordHandler output,InputStream xmlAuthStream, InputStream xmlSearchStream, InputStream xmlLamrStream,String usernamePassword) {
		init(authorizationUrl, searchUrl, lamrhUrl, output,xmlAuthStream, xmlSearchStream, xmlLamrStream,usernamePassword);
		
	}
	
	/**
	 * The initializing method called on via the constructors.
	 * @param authorizationUrl The location of the authorization site
	 * @param searchUrl The location of the search site
	 * @param lamrUrl The location of the links article match retrieval site
	 * @param output The record handler used for storing the harvested records
	 * @param xmlAuthStream the stream to the authorization message
	 * @param xmlSearchStream the stream to the search query message
	 * @param xmlLamrStream the stream to the links article match retrieval message
	 * @param usernamePassword the user name password string to be base 64 encoded
	 */
	private void init(URL authorizationUrl, URL searchUrl, URL lamrUrl, RecordHandler output,InputStream xmlAuthStream, InputStream xmlSearchStream, InputStream xmlLamrStream, String usernamePassword) {
		String authString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "+
			"xmlns:ns2=\"http://auth.cxf.wokmws.thomsonreuters.com\">"+
			"<soap:Body><ns2:authenticate/></soap:Body></soap:Envelope>";
		
		String closeString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "+
		    "xmlns:ns2=\"http://auth.cxf.wokmws.thomsonreuters.com\">"+
			"<soap:Body><ns2:closeSession/></soap:Body></soap:Envelope>";
		
		this.outputRH = output;
		this.authUrl = authorizationUrl;
		if(usernamePassword == null){
			this.usernamePassword = null;
		}else{
			this.usernamePassword = new Base64().encodeBase64URLSafeString(usernamePassword.getBytes());
		}
		if(xmlAuthStream == null){
			this.authMessage = new ByteArrayInputStream(authString.getBytes());
		}else{
			this.authMessage = xmlAuthStream;
		}
		this.closeMessage = new ByteArrayInputStream(closeString.getBytes());
		this.searchUrl = searchUrl;
		this.searchFile = xmlSearchStream;

		this.lamrUrl = lamrUrl;
		this.lamrFile = xmlLamrStream;
		try {
			this.lamrMessage = IOUtils.toString(this.lamrFile);
//			log.debug("LAMR message\n" + this.lamrMessage);
		} catch(IOException e) {
			e.printStackTrace();
		}
		this.lamrSet = new TreeSet<String>();
		
		log.debug("Checking for NULL values");
		if(this.outputRH == null) {
			log.debug("Outputfile = null");
			log.error("Must provide output file!");
		}else{
			log.debug("Outputfile = " + this.outputRH.toString());
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
	
	/**
	 * @param documentNode a DOM node to be changed into a properly indented string
	 * @return The indented string containing the node and sub-nodes
	 */
	private String nodeToString(Node documentNode){
		StreamResult result =null;
		try {
		Transformer	transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		result = new StreamResult(new StringWriter());
		DOMSource domSource = new DOMSource(documentNode);
		transformer.transform(domSource, result);
		} catch(TransformerException e) {
			e.printStackTrace();
		} catch(TransformerFactoryConfigurationError e1) {
			e1.printStackTrace();
		}
	
	return result.getWriter().toString();
	}
	
	/**
	 * @param previousQuery a WOS soap query xml message
	 * @return the string with the altered first node in the 
	 * @throws IOException thrown if there is an issue parsing the previousQuery string
	 */
	private String getnextQuery(String previousQuery) throws IOException{
		String nextQuery = "";
		try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		Document searchDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(previousQuery.getBytes()) );
		//log.debug("searchDoc:");
		//log.debug(documentToString(searchDoc));
		
		NodeList firstrecordNodes = searchDoc.getElementsByTagName("firstRecord");
		Node firstnode = firstrecordNodes.item(0);
		int firstrecord = Integer.parseInt(firstnode.getTextContent() );
		log.debug("firstrecord = " + firstrecord);
		
		NodeList countNodes = searchDoc.getElementsByTagName("count");
		int count = Integer.parseInt(countNodes.item(0).getTextContent() );
		log.debug("count= " + count);
		int newFirst=firstrecord + count;
		firstnode.setTextContent(Integer.toString(newFirst) );
		log.debug("new First Record= " + newFirst);
//		}
		

		nextQuery = nodeToString(searchDoc);
		//log.debug("newsearchDoc:\n"+nextQuery);
		} catch(SAXException e) {
			e.printStackTrace();
			throw new IOException(e);
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
			throw new IOException(e);
		} 
		return nextQuery;
		
	}
	
	/**
	 * @param responseXML String containing the results from the WOS soap query
	 * @return the number of records found in the string
	 */
	private Map<String,String> extractSearchRecords(String responseXML){
		HashMap<String,String> recordMap = new HashMap<String, String>();
		int numRecords = 0;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true); // never forget this!
				Document responseDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(responseXML.getBytes()) );
				NodeList recordList = responseDoc.getElementsByTagName("records");
				for(int index = 0; index < recordList.getLength(); index++){
					Element currentRecord = (Element)recordList.item(index);
					String identifier = currentRecord.getElementsByTagName("UT").item(0).getTextContent();
					String id = "id_-_" + identifier;
					compileLamrList(identifier);
					Element recordRoot = responseDoc.createElement("Description");
					recordRoot.setAttribute( "ID",  identifier);
					recordRoot.appendChild(currentRecord);
					String data = nodeToString(recordRoot);
					recordMap.put(id, data);
					writeRecord(id, data);
					numRecords++;
				}
			} catch(SAXException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(ParserConfigurationException e) {
				e.printStackTrace();
			}
		log.debug("Extracted "+ numRecords +" records from search");	
		return recordMap;
	}
	
	/**
	 * 
	 */
	private void executeLamrQuery(){
		if(this.lamrSet.isEmpty()){
			log.debug("No LAMR query sent, empty LAMR set.");
			return;
		}
		//compile lamrquery with lamrSet
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		Document lamrDoc = null;
		try {
			lamrDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(this.lamrMessage.getBytes()) );

//			log.debug("LAMR Message :\n"+nodeToString(lamrDoc));
			NodeList mapElements = lamrDoc.getElementsByTagName("map");
			Element lookUp = null;
			for(int index = 0;index < mapElements.getLength(); index++){
				Element currentmap = (Element)mapElements.item(index);
	//			log.debug("Element :\n" + nodeToString(currentmap));
	//			log.debug("Element name = \"" + currentmap.getAttribute("id")+ "\"");
				if(currentmap.getAttribute("id").contentEquals("lookup")){
					log.debug("Found element lookup");
	//				lookUp = (Element)currentmap.getParentNode();
					lookUp = currentmap;
					break;
				}
			}
			if(lookUp == null){
				log.error("No \"lookup\" node in LAMR query message");
			}
//			log.debug("prelookUp = " + nodeToString(lookUp));
	
			for(String currentUT : this.lamrSet){
				Element val = lamrDoc.createElement("val");
				val.setAttribute("name", "ut");
				val.setTextContent(currentUT);
				Element docMap = lamrDoc.createElement("map");
				docMap.setAttribute("name", "doc-"+currentUT);
				docMap.appendChild(val);
				lookUp.appendChild(docMap);
			}
//			log.debug("LAMR Message :\n"+nodeToString(lamrDoc));
			
			//send lamrquery
			Document lamrRespDoc =null;

			ByteArrayOutputStream lamrResponse = new ByteArrayOutputStream();
			{
				SOAPMessenger soapfetch = new SOAPMessenger(this.lamrUrl,lamrResponse,new ByteArrayInputStream(nodeToString(lamrDoc).getBytes()),"",null);
				soapfetch.execute();
			}
			lamrRespDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(lamrResponse.toByteArray()) );
	
//			log.debug("LAMR Response :\n"+nodeToString(lamrRespDoc));
//			extract records - A little hacky - message specifics sensitive
//			To ensure no erroneous name spaces rebuilding structure from existing data.
			log.debug("Extracting LAMR Records");
//			 records are in map elements.
			NodeList respMapList = lamrRespDoc.getElementsByTagName("map");
			int recordsFound = 0;
//			cycle through existing map elements;
			for(int index = 0; index < respMapList.getLength();index++){
				Element currentNode = (Element)respMapList.item(index);
//				what we are looking for is found in maps named "WOS"
				if(currentNode.getAttribute("name").contentEquals("WOS")){
//					for output similarity  have the root node be Description
					Element recordRoot = lamrRespDoc.createElement("Description");
					String ut = "";
//					each WOS node has the result formatted as named val nodes.
					NodeList valList = currentNode.getElementsByTagName("val");
					for(int index2 = 0; index2 < valList.getLength(); index2++){
						Element currentVal = (Element)valList.item(index2);
//						Getting Record ID
						if(currentVal.getAttribute("name").contentEquals("ut")){
							ut = currentVal.getTextContent();
							break;
						}
					}
					if(ut != ""){
						recordsFound++;
						recordRoot.setAttribute( "ID",  ut);
						NodeList currentchildren = currentNode.getElementsByTagName("val");
						Element currentDup = lamrRespDoc.createElement("map");
						currentDup.setAttribute("name", "WOS");
						for(int index2 = 0; index2 < currentchildren.getLength(); index2++){
							Element cur = (Element)currentchildren.item(index2);
							Element childNode = lamrRespDoc.createElement(cur.getTagName());
							childNode.setAttribute("name", cur.getAttribute("name"));
							childNode.setTextContent(cur.getTextContent());
							currentDup.appendChild( childNode );
						}
						recordRoot.appendChild(currentDup);
						
						writeRecord("id_-_LAMR_-_" + ut, nodeToString(recordRoot));
					}
				}
			}
			log.debug("Found " + recordsFound + " LAMR Records");
			
		
			//write records
		} catch(SAXException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		}
		this.lamrSet.clear();
	}
	
	/**
	 * @param id The identifying name for the record. (used as a filename in the text file record handler)
	 * @param data A string representing the information to place within the record.
	 * @throws IOException Thrown if there is an issue with the recordhandler back-end
	 */
	public void writeRecord(String id, String data) throws IOException {
		log.trace("Adding Record " + id);
		this.outputRH.addRecord(id, data, this.getClass());
	}
	
	/**
	 * @param id The identifying name for the record. (<UT> attribute)
	 * @param data A string representing the information to place within the record.
	 * @throws IOException Thrown if there is an issue with the recordhandler back-end
	 */
	public void compileLamrList(String id) {
		log.trace("Adding LAMR UT = " + id);
		this.lamrSet.add(id);
		if(this.lamrSet.size() == 50){
			executeLamrQuery();
		}
	}
	
	/**
	 * Executes the task
	 * @throws IOException error processing record handler or jdbc connection
	 */
	public void execute() throws IOException {
		String searchQuery = IOUtils.toString(this.searchFile);
		ByteArrayOutputStream authResponse = new ByteArrayOutputStream();
		{
			HashMap<String,String> reqProp = new HashMap<String, String>();
			if(this.usernamePassword != null){
				reqProp.put("Authorization", "Basic " + this.usernamePassword);
			}
			SOAPMessenger soapfetch = new SOAPMessenger(this.authUrl,authResponse,this.authMessage,"",reqProp);
			soapfetch.execute();
		}
		String authCode = XPathTool.getXpathStreamResult(new ByteArrayInputStream(authResponse.toByteArray()), "//return");
		int recordsFound,lastRec;
		do{
			
			ByteArrayOutputStream searchResponse = new ByteArrayOutputStream();
			{
				SOAPMessenger soapfetch = new SOAPMessenger(this.searchUrl,searchResponse,new ByteArrayInputStream(searchQuery.getBytes()),authCode,null);
				soapfetch.execute();
			}
			String recFound = XPathTool.getXpathStreamResult(new ByteArrayInputStream(searchResponse.toByteArray()), "//recordsFound");
			String searchCount = XPathTool.getXpathStreamResult(new ByteArrayInputStream(searchQuery.getBytes()), "//retrieveParameters/count");
			String firstrecord = XPathTool.getXpathStreamResult(new ByteArrayInputStream(searchQuery.getBytes()), "//retrieveParameters/firstRecord");

			Map<String,String> recordMap = extractSearchRecords(new String(searchResponse.toByteArray(),"UTF-8"));
//			for(Entry<String, String> x : recordMap.entrySet()) {
//				writeRecord(x.getKey(), x.getValue());
//			}
			log.debug("Search count = \"" + searchCount + "\"");
			log.debug("Records Found = \"" + recFound + "\"");
			recordsFound = Integer.parseInt(recFound);
			lastRec = Integer.parseInt(searchCount) + Integer.parseInt(firstrecord);
			log.debug("Records left = " + (recordsFound - lastRec));
			searchQuery = getnextQuery(searchQuery);
			try
			{
				Thread.sleep(100); // do nothing for 100 miliseconds (1000 miliseconds = 1 second)
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			} 
		}while(lastRec < recordsFound);
		executeLamrQuery();
		
		ByteArrayOutputStream closeResponse = new ByteArrayOutputStream();
		{
			SOAPMessenger soapfetch = new SOAPMessenger(this.authUrl,closeResponse,this.closeMessage,authCode,null);
			soapfetch.execute();
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("WOSFetch");
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("authurl").withParameter(true, "URL").setDescription("The URL which will receive the AUTHMESSAGE.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("searchconnection").withParameter(true, "URL").setDescription("The URL which will receive the SEARCHMESSAGE.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("lamrconnection").withParameter(true, "URL").setDescription("The URL which will receive the LAMRMESSAGE.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("searchmessage").withParameter(true, "SEARCHMESSAGE").setDescription("The SEARCHMESSAGE file path.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("authmessage").withParameter(true, "AUTHMESSAGE").setDescription("The AUTHMESSAGE file path.").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("lamrmessage").withParameter(true, "LAMRMESSAGE").setDescription("The LAMRMESSAGE file path.").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("usernamepassword").withParameter(true, "USERNAMEPASSWORD").setDescription("The username and password string to be encoded using base64").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "OUTPUT_FILE").setDescription("XML result file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
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
