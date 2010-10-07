/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.translate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.Record;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.xml.sax.SAXException;

/**
 * Takes XML Files and uses an XSL file to translate the data into the desired ontology
 * 
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class XSLTranslator {
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Log log = LogFactory.getLog(XSLTranslator.class);
	/**
	 * The translation file is the map that will reconstruct our input stream's document into
	 * the appropriate format
	 */
	private File translationFile;
	/**
	 * in stream is the stream containing the file (xml) that we are going to translate
	 * @TODO possibly remove and switch to passing streams to xmlTranslate
	 */
	protected InputStream inStream;
	/**
	 * out stream is the stream that the controller will be handling and were we will dump the translation
	 * @TODO possibly remove and switch to passing streams to xmlTranslate
	 */
	protected OutputStream outStream;
	/**
	 * record handler for incoming records
	 */
	protected RecordHandler inStore;
	/**
	 * record handler for storing records
	 */
	protected RecordHandler outStore;
	
	/**
	 * Default constructor
	 */
	@SuppressWarnings("unused")
	private XSLTranslator() {
		// empty constructor
	}
	
	/**
	 * Constructor
	 * 
	 * @param argumentList
	 *           <ul>
	 *           <li>translationFile the file that details the translation from the original xml to the target format</li>
	 *           <li>inRecordHandler the files/records that require translation</li>
	 *           <li>outRecordHandler the output record for the translated files</li>
	 *           </ul>
	 * @throws IOException error reading files
	 * @throws SAXException error with parser
	 * @throws ParserConfigurationException error with parser config
	 */
	public XSLTranslator(ArgList argumentList) throws ParserConfigurationException, SAXException, IOException {
		//set Translation file
		this.setTranslationFile(VFS.getManager().resolveFile(new File("."), argumentList.get("xslFile")).getContent().getInputStream());
		
		// create record handlers
		this.inStore = RecordHandler.parseConfig(argumentList.get("input"), argumentList.getProperties("I"));
		this.outStore = RecordHandler.parseConfig(argumentList.get("output"), argumentList.getProperties("O"));
	}
	
	/**
	 * @deprecated
	 * Constructor
	 * Initializing constructor for the translate method
	 * 
	 * @param transFile
	 *           The file that contains the mapping for translation
	 * @param iStream
	 *           the incoming stream that the file is passed into
	 * @param oStream
	 *           the outgoing stream that the translation is passed to
	 */
	@Deprecated
	public XSLTranslator(File transFile, InputStream iStream, OutputStream oStream) {
		this.setTranslationFile(transFile);
		this.inStream = iStream;
		this.outStream = oStream;
	}
	
	
	/***
	 * Set translation file from a file
	 * 
	 * @param transFileStream
	 *           valid type of translation file is xslt
	 * @throws IOException error creating temp file
	 */
	public void setTranslationFile(InputStream transFileStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(transFileStream));
		this.translationFile = File.createTempFile("transFile", "xsl");
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.translationFile));
		String s;
		while((s = br.readLine()) != null) {
			bw.append(s);
			bw.append("\n");
		}
		bw.close();
		br.close();
	}
	
	/***
	 * Set translation file from a file
	 * 
	 * @param transFile
	 *           valid type of translation file is xslt
	 */
	public void setTranslationFile(File transFile) {
		this.translationFile = transFile;
	}
	
	
	/***
	 * checks again for the necessary file and makes sure that they exist
	 */
	public void execute() {
		// checking for valid input parameters
		log.info("Translation: Start");
		
		try {
			// get from the in record and translate
			for(Record r : this.inStore) {
				if (r.needsProcessed(this.getClass())){
					log.info("Translating Record " + r.getID());
					this.inStream = new ByteArrayInputStream(r.getData().getBytes());
					this.outStream = new ByteArrayOutputStream(); 
					this.xmlTranslate();
					this.outStream.flush();
					this.outStore.addRecord(r.getID(), this.outStream.toString(), this.getClass());
					r.setProcessed(this.getClass());
					this.outStream.close();
				} else {
					log.debug("No Translation Needed: " + r.getID());
				}
			}
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
		log.info("Translation: End");
	}
	
	/***
	 * using the javax xml transform factory this method uses the xsl file to translate
	 * XML into the desired format designated in the xsl file.
	 * 
	 */
	private void xmlTranslate() {
		StreamResult outputResult = new StreamResult(this.outStream);
		try {
			// JAXP reads data using the Source interface
			Source xmlSource = new StreamSource(this.inStream);
			Source xslSource = new StreamSource(this.translationFile);
			
			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
			
			// the factory pattern supports different XSLT processors
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer trans = transFact.newTransformer(xslSource);
			
			// this outputs to oStream
			trans.transform(xmlSource, outputResult);
		} catch(Exception e) {
			log.error("Translation Error", e);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("XSLTranslator");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input record handler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterProperties("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of input recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output record handler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('x').setLongOpt("xslFile").withParameter(true, "XSL_FILE").setDescription("xsl file").setRequired(true));
		return parser;
	}	
	
	/**
	 * Currently the main method accepts two methods of execution, file translation and record handler translation
	 * The main method actually passes its arg string to another method so that Translator can
	 * use this same method of execution
	 * 
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new XSLTranslator(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
		
		
		
	}

}
