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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.Record;
import org.vivoweb.ingest.util.RecordHandler;

/*****************************************************************************************
 * VIVO Data Translator
 * @author Stephen V. Williams swilliams@ichp.ufl.edu
 * TODO MILESTONE2: create constructor for accepting a parameter list
 * TODO MILESTONE2: create constructor for accepting a parameter array
 * TODO MILESTONE1: test for error handling, failing completely and gently
 * ***************************************************************************************/
public class Translator {
	
	protected static Log log = LogFactory.getLog(Translator.class);
	
	
	/**
	 * in stream is the stream containing the file (xml) that we are going to translate
	 */
	protected InputStream inStream;
	
	/**
	 * out stream is the stream that the controller will be handling and were we will dump the translation
	 */
	protected OutputStream outStream;
	
	
	/***
	 * Empty constructor
	 */
	public Translator(){
		
	}
		
	/***
	 * Initial constructor for the translate method, it is not required to use this constructor
	 * but it is suggested, since not passing one of the variables would result in a error being thrown
	 * 
	 * @param sType Type of the file to be translated (for this exercise its always XML)
	 * @param transFile The file that contains the mapping for translation
	 * @param iStream the incoming stream that the file is passed into
	 * @param oStream the outgoing stream that the translation is passed to
	 */
	public Translator(String sType, InputStream iStream, OutputStream oStream){
		this.setInStream(iStream);
		this.setOutStream(oStream);
	}
		
	
	/***
	 * Sets the input stream 
	 * 
	 * @param iStream the input stream
	 */
	public void setInStream(InputStream iStream) {
		this.inStream = iStream;
	}
	
	 /***
   * Sets the output stream 
   * 
   * @param oStream the output stream
   */
	public void setOutStream(OutputStream oStream) {
	  this.outStream = oStream;
	}
		
	
	/***
	 * This function will utilize the translation file to transform the outputstream
	 * @throws IllegalArgumentException when the system is not properly configured
	 */
	public void execute() throws IllegalArgumentException {
		
		//checking for valid input parameters
		if (this.inStream != null && this.outStream != null) {
		
			log.trace((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance()) + " - Translation: Start");
			System.out.println("Translation: Start");
			
			System.out.println("This is the stub for all translation classes, please instatiate one of them to run an actual translation");
			
			log.trace((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance()) + " - Translation: End");
			System.out.println("Translation: End");
		}
		else {
			log.error((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance()) + " - Translation unable to start: Not all Parameters Set" );
			throw new IllegalArgumentException("Unable to translate, system not configured");
		}		
	}
	
	
	public static void main(String[] args){
		
		//you need more than one argument to run any of the translate functions
		if (args.length <= 1){
			log.error("Invalid Arguments");
		}
		else{
			//XSL Translation (same switches and code as the main method for xsltranslate
			//TODO can we just call the main method for xsl translate?
			if (args[0].startsWith("-xsl")){
				args[0] = args[0].replace("xsl", "");
				XSLTranslator xslTrans = new XSLTranslator();
				xslTrans.parseArgsExecute(args);	
			}
			else if (args[0].startsWith("-gloze")){
				
			}
			else if (args[0].startsWith("-rdf")){
				
			}
			else{
				log.error("The switch " + args[0].toString() + "is not handled by Translator");
			}
			
			
			
		}
		
		
		
	}
	
}
