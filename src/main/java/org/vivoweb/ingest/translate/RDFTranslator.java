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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * RDF Tranlator
 * This class translates RDF from its current schema into 
 * the VIVO ontology.  It uses RDFworkflows to manipulate the
 * data
 * 
 * TODO:  Identify additional parameters required for translation
 * TODO:  Identify the method of invoking an RDF workflow in Jena
 * TODO:  Create a sample RDF workflow
 * 
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 *
 */
public class RDFTranslator extends Translator{
	
	private File translationWorkFlow;
	
	
	public RDFTranslator(){
	}
	
	public RDFTranslator(File transWF){
		this.setTranslationWorkFlow(transWF);
		
	}
	
	
	public void setTranslationWorkFlow(File transWF){
		this.translationWorkFlow = transWF;
	}
	
	
	public void execute(){
		//checking for valid input parameters
		if ((this.translationWorkFlow !=null && this.translationWorkFlow.isFile()) && this.inStream != null && this.outStream != null) {
			log.info("Translation: Start");
		
			try{			
				Model wfModel = ModelFactory.createDefaultModel();
				
				wfModel.read(new FileReader(this.translationWorkFlow), "http://vivoweb.org/harvester/rdfTranslation/", "RDF/XML"); ///working here
				
				Model mainModel = ModelFactory.createDefaultModel();
				
				mainModel.read(this.inStream, "http://vivoweb.org/harvester/rdfTranslation/", "RDF/XML");
				
				processWorkFlow(wfModel, mainModel);
			}
			catch (Exception e){
				log.error(e.getMessage());
			}
			
			
			log.info("Translation: End");
		}
		else {
			log.error("Translation unable to start: Not all Parameters Set" );
			throw new IllegalArgumentException("Unable to translate, system not configured");
		}
	}
	
	//TODO make this work
	public void processWorkFlow(Model workflow, Model toBeModified){
	}
	
	
	/**
	 * The main method is for execution through the command line.  
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		if (args.length < 3 || args.length > 4){
			log.error("Invalid Arguments: RDFTranslate requires at least 1 arguments.  The system was supplied with " + args.length);
			//throw new IllegalArgumentException();
		}
		else {
			if (args[0].equals("-f")){
				
				try {
					RDFTranslator rdfTrans = new RDFTranslator();
									
					//read in workflow rdf
					rdfTrans.setTranslationWorkFlow(new File(args[1]));
					
					//read in the jena model rdf to inputstream
					rdfTrans.setInStream(new FileInputStream(new File(args[2])));
					
					//set the outstream as System.out
					rdfTrans.setOutStream(System.out);
					
					rdfTrans.execute();
				}
				catch (Exception e){
					log.error(e.getMessage());  //TODO make this more robust (better information for debugging problems)
				}
				
			}
			else if (args[0].equals("-rh")){
				//pull in rdf workflow
				//pull in the record handler for the jena model
				//execute
			}
			else{
				log.error("Invalid Arguments: Translate option " + args[0] + " not handled.");
				//throw new IllegalArgumentException();
			}
			
		}
	}

}
