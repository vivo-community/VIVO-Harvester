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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.vivoweb.ingest.util.Record;
import org.vivoweb.ingest.util.RecordHandler;
import org.vivoweb.ingest.util.Task;

/**
 * Takes XML Files and uses an XSL file to translate the data into the desired ontology
 * 
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class XSLTranslator extends Task {
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
	 */
	protected InputStream inStream;
	/**
	 * out stream is the stream that the controller will be handling and were we will dump the translation
	 */
	protected OutputStream outStream;
	
	/**
	 * Default constructor
	 */
	public XSLTranslator() {
		// empty constructor
	}
	
	/**
	 * Constructor
	 * Initializing constructor for the translate method, it is not required to use this constructor
	 * but it is suggested, since not passing one of the variables would result in a error being thrown
	 * 
	 * @param transFile
	 *           The file that contains the mapping for translation
	 * @param iStream
	 *           the incoming stream that the file is passed into
	 * @param oStream
	 *           the outgoing stream that the translation is passed to
	 */
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
	 * set translation file from a location
	 * 
	 * @param filename
	 *           string of the location of the translation file
	 */
	public void setTranslationFile(String filename) {
		this.translationFile = new File(filename);
	}
	
	@Override
	public void executeTask() {
		// checking for valid input parameters
		if((this.translationFile != null && this.translationFile.isFile()) && this.inStream != null
				&& this.outStream != null) {
			// TODO Stephen: no reason to pass the variables to the method, have them use the designated streams on thier
			// own
			xmlTranslate(this.inStream, this.translationFile, this.outStream);
		} else {
			log.error("Translation unable to start: Not all Parameters Set");
			log.error("Translation File: " + this.translationFile.toString());
			log.error("Translation File truth: " + this.translationFile.isFile());
			log.error("Translation Stream: " + this.inStream.toString());
			throw new IllegalArgumentException("Unable to translate, system not configured");
		}
	}
	
	/***
	 * using the javax xml transform factory this method uses the xsl file to translate
	 * XML into the desired format designated in the xsl file.
	 * 
	 * @param xmlData
	 *           ObjectInputStream of the xml that is about to be translated
	 * @param xsltFile
	 *           File for the definition that should be translated
	 * @param oStream
	 *           The stream to which the data should be written
	 */
	private void xmlTranslate(InputStream xmlData, File xsltFile, OutputStream oStream) {
		StreamResult outputResult = new StreamResult(oStream);
		
		try {
			// JAXP reads data using the Source interface
			Source xmlSource = new StreamSource(xmlData);
			Source xsltSource = new StreamSource(xsltFile);
			
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
	 * Currently the main method accepts two methods of execution, file translation and record handler translation
	 * This is the method that executes those functions. It was put in place so that translator's main method
	 * can also call this method and pass its argument array
	 * 
	 * @param args
	 *           the file to translate and xsl
	 *           <ul>
	 *           <li>functionSwitch possible entries include -f for file and -rh for record handler</li>
	 *           <li>translationFile the file that details the translation from the original xml to the target format</li>
	 *           <li>fileToTranslate the file that requires translation</li>
	 *           <li>inRecordHandler the files/records that require translation</li>
	 *           <li>outRecordHandler the output record for the translated files</li>
	 *           </ul>
	 */
	public void parseArgsExecute(String[] args) {
		if(args.length != 4) {
			log.error("Invalid Arguments: XSLTranslate requires 4. They system was supplied " + args.length);
			throw new IllegalArgumentException();
		}
		log.info("Translation: Start");
		
		if(args[0].equals("-f")) {
			try {
				// set the in/out and translation var
				// TODO Stephen: change setOutStream to allow for a file to be specified
				this.setTranslationFile(new File(args[2]));
				this.inStream = new FileInputStream(new File(args[1]));
				
				log.trace("Translating Record " + args[1].toString());
				
				if( !args[3].equals("") && args[3] != null) {
					this.outStream = new FileOutputStream(new File(args[3]));
				} else {
					this.outStream = System.out;
				}
				
				// execute the program
				this.executeTask();
			} catch(FileNotFoundException e) {
				log.error("", e);
			}
		} else if(args[0].equals("-rh")) {
			try {
				// pull in the translation xsl
				
				this.setTranslationFile(new File(args[2]));
				
				// create record handlers
				RecordHandler inStore = RecordHandler.parseConfig(args[1]);
				RecordHandler outStore = RecordHandler.parseConfig(args[3]);
				
				// create a output stream for writing to the out store
				ByteArrayOutputStream buff = new ByteArrayOutputStream();
				
				// get from the in record and translate
				for(Record r : inStore) {
					log.trace("Translating Record " + r.getID());
					
					this.inStream = new ByteArrayInputStream(r.getData().getBytes());
					this.outStream = buff; 
					this.executeTask();
					buff.flush();
					outStore.addRecord(r.getID(), buff.toString());
					buff.reset();
				}
				
				buff.close();
			} catch(Exception e) {
				log.error("", e);
			}
		} else {
			log.error("Invalid Arguments: Translate option " + args[0] + " not handled.");
			throw new IllegalArgumentException();
		}
		
		log.trace("Translation: End");
		
	}
	
	/**
	 * Get the OptionParser for this Task
	 * @return the OptionParser
	 */
	protected static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("d", "driver")).withRequiredArg().describedAs("jdbc driver class");
		parser.acceptsAll(asList("c", "connection")).withRequiredArg().describedAs("jdbc connection string");
		parser.acceptsAll(asList("u", "username")).withRequiredArg().describedAs("database username");
		parser.acceptsAll(asList("p", "password")).withRequiredArg().describedAs("database password");
		parser.acceptsAll(asList("o", "output")).withRequiredArg().describedAs("RecordHandler config file path");
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
		XSLTranslator xslTrans = new XSLTranslator();
		xslTrans.parseArgsExecute(args);
	}

}
