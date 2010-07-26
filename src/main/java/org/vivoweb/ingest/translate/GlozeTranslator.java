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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import joptsimple.OptionParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.ArgList;
import org.vivoweb.ingest.util.Record;
import org.vivoweb.ingest.util.RecordHandler;
import com.hp.gloze.Gloze;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Gloze Tranlator
 * This class translates XML into its own natural RDF ontology
 * using the gloze library. Translation into the VIVO ontology
 * is completed using the RDF Translator.
 * TODO Stephen: Identify additional parameters required for translation
 * TODO Stephen: Identify methods to invoke in the gloze library
 * 
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class GlozeTranslator {
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Log log = LogFactory.getLog(GlozeTranslator.class);
	/**
	 * the file to be translated
	 * FIXME Stephen: remove this and use the incoming stream
	 */
	protected File incomingXMLFile;
	/**
	 * The incoming schema to help gloze translate the xml file
	 */
	protected File incomingSchema;
	/**
	 * the uri base for relative nodes in the xml file
	 */
	protected URI uriBase;
	/**
	 * in stream is the stream containing the file (xml) that we are going to translate
	 */
	private InputStream inStream;
	/**
	 * out stream is the stream that the controller will be handling and were we will dump the translation
	 */
	private OutputStream outStream;
	/**
	 * record handler for incoming records
	 */
	protected RecordHandler inStore;
	/**
	 * record handler for storing records
	 */
	protected RecordHandler outStore;
	
	/**
	 * Default Constructor
	 */
	public GlozeTranslator() {
		this.setURIBase("http://vivoweb.org/glozeTranslation/noURI/");
	}
	
	
	/**
	 * @param argumentList
	 * <ul>
	 *           <li><em>inRecordHandler</em> the incoming record handler when record handlers are due</li>
	 *           <li><em>schema</em> the incoming schema for gloze translation</li>
	 *           <li><em>outRecordHandler</em> the out record handler</li>
	 *           <li><em>uriBase</em> required for gloze translation the unset URIBASE used is
	 *           http://vivoweb.org/glozeTranslation/noURI/</li>
	 *           </ul>
	 */
	public GlozeTranslator(ArgList argumentList){
		// the uri base if not set is http://vivoweb.org/glozeTranslation/noURI/"
		if(argumentList.has("uriBase")) {
			this.setURIBase(argumentList.get("uriBase"));
		}
		// pull in the translation xsl
		if(argumentList.has("xmlSchema")) {
			this.setIncomingSchema(new File(argumentList.get("xmlSchema")));
		}		

		// create record handlers
		try {
			this.inStore = RecordHandler.parseConfig(argumentList.get("input"));
			this.outStore = RecordHandler.parseConfig(argumentList.get("output"));	
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * Setter for xmlFile
	 * @param xmlFile
	 *           the file to translate
	 */
	public void setIncomingXMLFile(File xmlFile) {
		this.incomingXMLFile = xmlFile;
	}
	
	/**
	 * Setter for schema
	 * @param schema
	 *           the schema that gloze can use, but doesn't need to translate the xml
	 */
	public void setIncomingSchema(File schema) {
		this.incomingSchema = schema;
	}
	
	/**
	 * Setter for uriBase
	 * @param base
	 *           the base uri to apply to all relative entities
	 */
	public void setURIBase(String base) {
		try {
			this.uriBase = new URI(base);
		} catch(URISyntaxException e) {
			log.error("", e);
		}
	}
	
	/**
	 * The main translation method for the gloze translation class
	 * setups up the necessary conditions for using the gloze library
	 * then executes its translation class
	 */
	public void translateFile() {
		Gloze gl = new Gloze();
		
		Model outputModel = ModelFactory.createDefaultModel();
		
		try {
			// Create a temporary file to use for translation
			File tempFile = new File("temp");
			FileOutputStream tempWrite = new FileOutputStream(tempFile);
			while(true) {
				int bytedata = this.inStream.read();
				if(bytedata == -1) {
					break;
				}
				tempWrite.write(bytedata);
			}
			this.inStream.close();
			tempWrite.close();
			
			gl.xml_to_rdf(tempFile, new File("test"), this.uriBase, outputModel);
			tempFile.delete();
		} catch(Exception e) {
			log.error("", e);
		}
		
		outputModel.write(this.outStream);
	}
	
	/***
	 * 
	 */
	public void execute() {
		if(this.uriBase != null && this.inStream != null) {
			log.trace("Translation: Start");
			
			try {
				// create a output stream for writing to the out store
				ByteArrayOutputStream buff = new ByteArrayOutputStream();
				// get from the in record and translate
				for(Record r : this.inStore) {
					this.inStream = new ByteArrayInputStream(r.getData().getBytes());
					this.outStream = buff;
					this.translateFile();
					buff.flush();
					this.outStore.addRecord(r.getID(), buff.toString());
					buff.reset();
				}
				buff.close();
			} catch(Exception e) {
				log.error("", e);
			}	
			
			log.trace("Translation: End");
		} else {
			log.error("Invalid Arguments: Gloze Translation requires a URIBase and XMLFile");
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * @return returns OptionParser
	 */
	protected static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("i", "input")).withRequiredArg().describedAs("Input Record Handler");
		parser.acceptsAll(asList("o", "output")).withRequiredArg().describedAs("Output Record Handler");
		parser.acceptsAll(asList("z", "xmlSchema")).withOptionalArg().describedAs("XML Schema");
		parser.acceptsAll(asList("u", "uriBase")).withOptionalArg().describedAs("URI Base");
		return parser;
	}	
	
	/***
	 * 
	 * @param args list of arguments required to execute glozetranslate
	 */
	public static void main(String[] args) {
		try {
			new GlozeTranslator(new ArgList(getParser(), args, "i","o","z","u")).execute();
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
