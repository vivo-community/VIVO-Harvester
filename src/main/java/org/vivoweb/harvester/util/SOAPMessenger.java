/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Fetches SOAP-XML data from a SOAP compatible site placing the data in the supplied file.
 */
public class SOAPMessenger {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SOAPMessenger.class);
	/**
	 * File to put XML in.
	 */
	private OutputStream outputFile;
	
	/**
	 * URL to send message to
	 */
	private URL url;

	/**
	 * Connection derived from that URL
	 */
	private URLConnection urlCon;
	
	/**
	 * Inputstream with SOAP style XML message
	 */
	private InputStream inputFile;
	
	/**
	 * String of XML gathered from the inputStream
	 */
	private String xmlString;
	
	/**
	 * The authentication sessionID
	 */
	private String sessionID;
	
	Map<String,String> requestProperties;
	
	/**
	 * Constructor
	 * @param url address to connect with
	 * @param output RecordHandler to write data to
	 * @param xmlFile xml file to POST to the url
	 * @param reqProperties 
	 * @throws IOException error talking with database
	 */
	public SOAPMessenger(URL url, String output, String xmlFile,String sesID,Map<String,String> reqProperties) throws IOException {
		this(url, FileAide.getOutputStream( output ), FileAide.getInputStream( xmlFile ), sesID,  reqProperties);
	}
	
	
	
	/**
	 * Command line Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private SOAPMessenger(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Arglist Constructor
	 * @param args option set of parsed args
	 * @throws IOException error creating task
	 */
	private SOAPMessenger(ArgList args) throws IOException {
		this(
			new URL(args.get("u")), 
			FileAide.getOutputStream(args.get("o")),
			FileAide.getInputStream(args.get("m")),
			args.get("a"),
			args.getValueMap("p")
		);
	}
	
	/**
	 * Library style Constructor
	 * @param url URL to connect with.
	 * @param output The stream to the output file
	 * @param xmlFileStream The stream which points to the Soap Message
	 * @throws IOException problem with opening url connection
	 */
	public SOAPMessenger(URL url, OutputStream output, InputStream xmlFileStream, String sesID,Map<String,String> reqProprties) throws IOException {
		
		this.outputFile = output;
		this.url = url;
		this.inputFile = xmlFileStream;
		this.sessionID = sesID;
		this.requestProperties = reqProprties;
		
		log.debug("Checking for NULL values");
		if(this.outputFile == null) {
			log.debug("Outputfile = null");
			log.error("Must provide output file!");
		}else{
			log.debug("Outputfile = " + this.outputFile.toString());
		}
		
		if(this.inputFile == null) {
			log.debug("Inputfile = null");
			log.error("Must provide message file!");
		}else{
			log.debug("Inputfile = " + this.inputFile.toString());
		}
		
		if(this.url == null) {
			log.debug("URL = null");
			log.error("Must provide url!");
		}else{
			log.debug("URL = "+ this.url.toString());
		}
		
		if(this.sessionID == null) {
			log.debug("SessionID = null");
			this.sessionID = "";
		}else{
			log.debug("SessionID = " + this.sessionID);
		}
		
		if(this.requestProperties == null) {
			log.debug("Request Properties = null");
		}else{
			log.debug("Request Properties:");
			for(String prop : this.requestProperties.keySet()){
				log.debug(prop+" = "+ this.requestProperties.get(prop));
			}
		}
		
	}
	
	/**
	 * @param urlCon
	 * @param message
	 * @throws IOException
	 */
	private void sendMessage(URLConnection urlCon,String message) throws IOException{


	    // specify that we will send output and accept input
		this.urlCon.setDoInput(true);
		this.urlCon.setDoOutput(true);

//		this.urlCon.setConnectTimeout( 20000 );  // long timeout, but not infinite
//		this.urlCon.setReadTimeout( 20000 );

		this.urlCon.setUseCaches (false);
		this.urlCon.setDefaultUseCaches (false);

	    // tell the web server what we are sending
		this.urlCon.setRequestProperty ( "Content-Type", "application/soap+xml; charset=utf-8" );
		if(this.sessionID != ""){
			this.urlCon.setRequestProperty ( "Cookie", "SID=\""+this.sessionID + "\"" );
		}
		if(this.requestProperties != null){
			for(String param : this.requestProperties.keySet()){
				this.urlCon.setRequestProperty ( param, this.requestProperties.get(param) );
			}
		}
		
		log.debug("getting writer for url connection");
	    OutputStreamWriter osWriter = new OutputStreamWriter( this.urlCon.getOutputStream() );
	    
		log.debug("writting to url connection");
	    osWriter.write(this.xmlString);
	    osWriter.close();
	}
	
	/**
	 * @param urlCon
	 * @return
	 * @throws IOException
	 */
	private String readMessage(URLConnection urlCon) throws IOException{
	    InputStreamReader isReader = new InputStreamReader( this.urlCon.getInputStream() );
	
	    StringBuilder buf = new StringBuilder();
	    char[] cbuf = new char[ 2048 ];
	    int num;
	
	    while ( -1 != (num=isReader.read( cbuf )))
	    {
	        buf.append( cbuf, 0, num );
	    }
	
	    return buf.toString();
	}
	
	/**
	 * Executes the task
	 * @throws IOException error processing record handler or jdbc connection
	 */
	public void execute() throws IOException {

		this.xmlString = IOUtils.toString(this.inputFile,"UTF-8");

		log.info("Built message");
		log.debug("Message contents:\n" + this.xmlString);

		log.info("opening the url connection");
		this.urlCon = this.url.openConnection();
		
		sendMessage(this.urlCon,this.xmlString);

	    // reading the response
		log.debug("getting reader for url connection");
		String result = readMessage(this.urlCon);

	    result = formatResult(result);

		log.debug("Response contents:\n" + result);
	    OutputStreamWriter outputWriter = new OutputStreamWriter(this.outputFile);
		log.info("writing response");
	    outputWriter.write(result);
	    outputWriter.close();

	}
	
	/**
	 * Take an XML string and ensure there are line breaks and indentation.
	 * @param inputXml the input XML string
	 * @return the formatted XML string
	 */
	public String formatResult(String inputXml) {
		try {
			StringReader reader = new StringReader(inputXml);
			InputSource source = new InputSource(reader);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(source);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource domSource = new DOMSource(document);
			transformer.transform(domSource, result);
			
			return result.getWriter().toString();
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return inputXml; //log error and then just don't format the result
		}
	}
	
	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SOAPMessenger");
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("url").withParameter(true, "URL").setDescription("The URL which will receive the MESSAGE.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("message").withParameter(true, "MESSAGE").setDescription("The MESSAGE file path.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "OUTPUT_FILE").setDescription("XML result file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("authentication").withParameter(true, "AUTH").setDescription("The authentication session ID").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("requestproperty").withParameterValueMap("PARAM", "VALUE").setDescription("request properties to be associated with the SOAP request").setRequired(false));
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
			new SOAPMessenger(args).execute();
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
