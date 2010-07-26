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

import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import joptsimple.OptionParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.ArgList;
import org.vivoweb.ingest.util.Record;
import org.vivoweb.ingest.util.RecordHandler;

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
	 */
	public XSLTranslator(ArgList argumentList) {
		//set Translation file
		this.setTranslationFile(new File(argumentList.get("xslFile")));
		
		// create record handlers
		try {
			this.inStore = RecordHandler.parseConfig(argumentList.get("input"));
			this.outStore = RecordHandler.parseConfig(argumentList.get("output"));
		} catch (Exception e) {
			log.error(e);		//TODO  catch more specific errors
		}
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
			// create a output stream for writing to the out store
			ByteArrayOutputStream buff = new ByteArrayOutputStream();
			
			// get from the in record and translate
			for(Record r : this.inStore) {
				log.trace("Translating Record " + r.getID());
				
				this.inStream = new ByteArrayInputStream(r.getData().getBytes());
				this.outStream = buff; 
				this.xmlTranslate();
				buff.flush();
				this.outStore.addRecord(r.getID(), buff.toString());
				buff.reset();
			}
			
			buff.close();
		} catch(Exception e) {
			log.error("", e);
		}
		 
		log.trace("Translation: End");
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
			Source xsltSource = new StreamSource(this.translationFile);
			
			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
			
			// the factory pattern supports different XSLT processors
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer trans = transFact.newTransformer(xsltSource);
			
			// this outputs to oStream
			trans.transform(xmlSource, outputResult);
		} catch(Exception e) {
			log.error("Translation Error", e);
		}
	}
	
	/**
	 * Get the OptionParser for this Task
	 * @return the OptionParser
	 */
	protected static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("i", "input")).withRequiredArg().describedAs("Input Record Handler");
		parser.acceptsAll(asList("o", "output")).withRequiredArg().describedAs("Output Record Handler");
		parser.acceptsAll(asList("x", "xslFile")).withRequiredArg().describedAs("XSL File");
		return parser;
	}	
	
	/**
	 * Currently the main method accepts two methods of execution, file translation and record handler translation
	 * The main method actually passes its arg string to another method so that Translator can
	 * use this same method of execution
	 * 
	 * @param args
	 *           (passed directly to parseArgs)
	 */
	public static void main(String... args) {
		try {
			new XSLTranslator(new ArgList(getParser(), args, "i","o","x")).execute();
		} catch(IllegalArgumentException e) {
			try {
				getParser().printHelpOn(System.out);
			} catch(IOException e1) {
				log.fatal(e.getMessage(),e);
			}
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
		
		
		
	}

}
