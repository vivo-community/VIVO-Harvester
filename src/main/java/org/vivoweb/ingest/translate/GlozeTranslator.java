/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.translate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.vivoweb.ingest.util.Record;
import org.vivoweb.ingest.util.RecordHandler;

import com.hp.gloze.Gloze;
import com.hp.hpl.jena.rdf.model.*;
import org.vivoweb.ingest.translate.Translator;

/**
 * Gloze Tranlator
 * This class translates XML into its own natural RDF ontology
 * using the gloze library.  Translation into the VIVO ontology
 * is completed using the RDF Translator.
 * 
 * TODO Stephen: Identify additional parameters required for translation
 * TODO Stephen: Identify methods to invoke in the gloze library
 * 
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class GlozeTranslator extends Translator{

	/**
	 * the file to be translated
	 * 
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
	 * @param xmlFile the file to translate
	 */
	public void setIncomingXMLFile(File xmlFile){
		this.incomingXMLFile = xmlFile;
	}
	
	/**
	 * 
	 * @param schema the schema that gloze can use, but doesn't need to translate the xml
	 */
	public void setIncomingSchema(File schema){
		this.incomingSchema = schema;
	}
	
	/**
	 * 
	 * @param base the base uri to apply to all relative entities
	 */
	public void setURIBase(String base){
		try {
			this.uriBase = new URI(base);
		} catch (URISyntaxException e) {
			log.error("",e);
		}
	}
	
	/**
	 * 
	 */
	public GlozeTranslator() {
		this.setURIBase("http://vivoweb.org/glozeTranslation/noURI/");
	}
	
	
	/**
	 * The main translation method for the gloze translation class
	 * setups up the necessary conditions for using the gloze library
	 * then executes its translation class
	 */
	public void translateFile(){
		Gloze gl = new Gloze();
		
		Model outputModel = ModelFactory.createDefaultModel();
		
		try {
			//Create a temporary file to use for translation
			File tempFile = new File("temp");
			FileOutputStream tempWrite = new FileOutputStream(tempFile);
			while(true){
				int bytedata = this.inStream.read();
				if(bytedata == -1){
					break;
				}
				tempWrite.write(bytedata);
			}
			this.inStream.close();
			tempWrite.close();
									
			gl.xml_to_rdf(tempFile, new File("test"), this.uriBase , outputModel);
			tempFile.delete();
		} catch (Exception e) {
			log.error("",e);
		}
		
		outputModel.write(this.outStream);
	}
	
	@Override
	public void execute() {
		if (this.uriBase != null && this.inStream != null ){	
			log.trace("Translation: Start");
			
			translateFile();
			
			log.trace("Translation: End");
		}
		else {
			log.error("Invalid Arguments: Gloze Translation requires a URIBase and XMLFile");
			throw new IllegalArgumentException();
		}
	}
	
	/***
	 * 
	 * @param args the argument list passed to Main
	 * <ul>
	 * <li><em>switch</em> states if you are using the file methods or the record handler</li>
	 * <li><em>inFile</em> the xml file to translate</li>
	 * <li><em>inRecordHandler</em> the incoming record handler when record handlers are due</li>
	 * <li><em>schema</em> the incoming schema for gloze translation</li>
	 * <li><em>outFile</em> the out file </li>
	 * <li><em>outRecordHandler</em> the out record handler</li>
	 * <li><em>uriBase</em> required for gloze translation the unset URIBASE used is http://vivoweb.org/glozeTranslation/noURI/</li>
	 * </ul>
	 */
	public void parseArgsExecute(String[] args){
		if (args.length != 5) {
			  log.error("Invalid Arguments: GlozeTranslate requires 5 arguments.  The system was supplied with " + args.length);
		}
		else {
			
			//File Translation
			if (args[0].equals("-f")) {
				try {
					this.setInStream(new FileInputStream(new File(args[1])));
					if (!args[3].equals("") && args[3] != null){
						this.setOutStream(new FileOutputStream(new File(args[3])));
					} else {
						this.setOutStream(System.out);
					}
					
					//the schema is not required but aids in xml translation
					if (!args[2].equals("") && args[2] != null){
						this.setIncomingXMLFile(new File(args[2]));
					}
					
					//the uri base if not set is http://vivoweb.org/glozeTranslation/noURI/"
					if (args[4].equals("") && args[4] != null) {					
						this.setURIBase(args[4]);
					} 
					
					this.execute();
				}
				catch(Exception e){
					log.error("", e);
				}
			}
			else if (args[0].equals("-rh")) {
				try {
					//the uri base if not set is http://vivoweb.org/glozeTranslation/noURI/"
					if (args[4].equals("") && args[4] != null) {					
						this.setURIBase(args[4]);
					} 
				
					//pull in the translation xsl
					if (!args[2].equals("") && args[2] != null){
						this.setIncomingSchema(new File(args[2]));
					}	
					
					//create record handlers
					RecordHandler inStore = RecordHandler.parseConfig(args[1]);
					RecordHandler outStore;
					if (!args[3].equals("") && args[3] != null){
						outStore = RecordHandler.parseConfig(args[3]);
					} else {
						throw new IllegalArgumentException("Record Handler Execution requires and out bound record handler");
					}
					
					//create a output stream for writing to the out store
					ByteArrayOutputStream buff = new ByteArrayOutputStream();
					
					// get from the in record and translate
					for(Record r : inStore){
						this.setInStream(new ByteArrayInputStream(r.getData().getBytes()));
						this.setOutStream(buff);
						this.execute();
						buff.flush();
						outStore.addRecord(r.getID(),buff.toString());
						buff.reset();
					}
				
					buff.close();
				}
				catch (Exception e) {
					log.error("",e);
				}
			}
			else {
				log.error("Invalid Arguments: Translate option " + args[0] + " not handled.");
			}		
		}
	}
	
	
	/**
	 * 
	 * @param args the string required for translation
	 */
	public static void main(String[] args) {
		GlozeTranslator glTrans = new GlozeTranslator();
		glTrans.parseArgsExecute(args);		
	}
}
