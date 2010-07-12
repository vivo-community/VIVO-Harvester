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
package org.vivoweb.ingest.fetch;
/**
 * @author Dale Scheppler Dscheppler@ctrip.ufl.edu
 *
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;
/**
 * @author Dale Scheppler
 * 
 * Wrapper for Fetch sub-functions and commandline parser.
 */
public class Fetch 

{

	private static Log log = LogFactory.getLog(Fetch.class);								//Initialize the Log4J log Factory.
	/**
	 * @author Dale Scheppler
	 * @param args - The command line parameters input by the user at runtime.
	 */
	public static void main(String[] args)
	{
		log.debug("Initializing Fetch.");													//Log that we're starting a fetch.
		if(args.length == 1)																//We only take one argument.
		{
		for(String strArguments: args)
			{
			if(strArguments.equals("OAI"))													//If they typed "OAI"
			{
				try
				{
				System.out.println("Trying to read OAI Configuration file.");				//Log that we're checking for a config file for OAI
				OAIFetch(readConfig("config/OAIHarvestConfig.txt"));						//Run OAI Fetch using the existing configuration file.
				}
				catch(IllegalArgumentException e)
				{
					log.fatal("", e);														//If there is a problem, throw the exception to the logger
				}
			}
			else if(strArguments.equals("NIH"))												//If they typed "NIH"
			{
				//@TODO Finish the OAI fetch and add configuration options here.
				NIHFetch();																	//Run the OAI Fetch
			}
			else if(strArguments.equals("PubMed"))											//If they typed "PubMed"
			{
				//@TODO Finish putting in the stuff for reading the configuration file here.
				PubMedFetch();																//Run the PubMed Fetch
			}
			else if(strArguments.equals("RDB"))												//If they typed RDB
			{
				//@TODO Finish the RDB Fetch and add configuration options here.
				RDBFetch();																	//Run the RDB (Relational Database) Fetch
			}
			else																			//Otherwise
			{
				log.fatal("Fetch initialized with inappropriate argument.");				//Log that they input an improper commandline argument.				
				System.out.println("Invalid Argument. Valid arguments are NIH, OAI, PubMed, or RDB.");
			}
			}
		}
		else																				//Otherwise
		{
			log.fatal("Fetch attempted to run with incorrect number of arguments.");		//Log that they tried to put in too many or too few arguments.
			System.out.println("Incorrect Number of Arguments, valid arguments are OAI, NIH, PubMed, or RDB.");
		}
		log.debug("Fetch Complete.");														//Log that fetch is shutting down.

	}
	/**
	 * @author Dale Scheppler
	 * @param hmConfigMap - A hashmap of the configuration data from the configuration file.<br>
	 * The configuration file can be read and a hashmap returned using Fetch.readconfig
	 */
	public static void OAIFetch(HashMap<String, String> hmConfigMap )
	{
		log.debug("Initializing OAI Fetch.");												//Log that we're running an OAI Fetch
		checkConfig(hmConfigMap, OAIHarvest.arrRequiredParamaters);							//Check that the configuration paramaters are correct as defined in the OAIHarvest class.
		try {
			OAIHarvest.execute(hmConfigMap.get("address"), hmConfigMap.get("startDate"), hmConfigMap.get("endDate"), new FileOutputStream(hmConfigMap.get("filename")));		//Attempt to run the OAI Harvest.
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error during OAI Fetch: ", e);										//Catch and log any errors.
		}
		log.debug("OAI Fetch Complete.");													//Log when complete.
	}
	public static void PubMedFetch()
	{
//		FileOutputStream fos;
//		try {
//			fos = new FileOutputStream("temp.txt");
//			PubmedSOAPFetch bob = new PubmedSOAPFetch("dscheppler@ichp.ufl.edu", "somewhere", fos);
//			bob.fetchPubMedEnv(bob.ESearchEnv(bob.queryByAffiliation("ufl.edu"), 1000));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
			Task.main("config/PubmedFetchTask.xml");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	/**
	 * Look ma, I'm a stub!
	 */
	public static void NIHFetch()
	{
		System.out.println("We would be running NIH Fetch here");
	}
	/**
	 * Look ma, I'm a stub!
	 */
	public static void RDBFetch()
	{
		System.out.println("We would be running RDB Fetch here");
	}
/**
 * Reads a configuration file in key:value format and returns a hashmap of the results.
 * @author Dale Scheppler and Chris Haines
 * @param strFilename - The file name and path of the configuration file
 * @return hashMap[String, String] - A hashMap the data in the configuration file.
 * @throws IllegalArgumentException If configuration file is not in correct format
 */
	public static HashMap<String, String> readConfig(String strFilename) throws IllegalArgumentException {
		HashMap<String, String> hmConfigMap = new HashMap<String, String>();											//Create the hashmap
		try {
			FileInputStream fisConfigFile = new FileInputStream(strFilename);											//Create the File Input Stream
			BufferedReader brConfigFile = new BufferedReader(															//Create a buffered reader
					new InputStreamReader(fisConfigFile));
			String strLine;																								//Initialize a string variable
			while ((strLine = brConfigFile.readLine()) != null) {														//Until we hit the end of the file
				String[] strParams = strLine.split(":",2);																//Grab the line and split it on the colon
				if(strParams.length != 2){																				//If we end up with more or less than two pieces
					throw new IllegalArgumentException("Invalid configuration file format. Entries must be key:value.");//Throw an error
				}
				hmConfigMap.put(strParams[0], strParams[1]);															//Add the key:value pair to the hashmap.
				
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			log.fatal("File Not Found: " + strFilename, e1);
		} catch (IOException e) {
			e.printStackTrace();
			log.fatal("IO Exception reading configuration file: "+strFilename, e);
		}
		if(hmConfigMap.isEmpty())
		{
			throw new IllegalArgumentException("Failed to read configuration file. File does not exist or is blank.");
		}
		return hmConfigMap;

	}
	/**
	 * Insert twilight zone theme song here.
	 */
	private Fetch()
	{
		System.out.println("This space intentionally left blank.");
	}
	/**
	 * Checks the configuration file hashmap against the list of required paramaters in the various fetch subclasses.
	 * @author Dale Scheppler and Chris Haines
	 * @param hmConfigMap - The hashMap to be checked.
	 * @param arrParameters - The parameters that are required by the subclass.
	 */
	private static void checkConfig(HashMap<String, String> hmConfigMap, String[] arrParameters)
	{
		for(String Param:arrParameters)
		{
			if(!hmConfigMap.containsKey(Param))
			{
				throw new IllegalArgumentException ("Missing parameter \"" + Param + "\" in configuration file");
			}
		}
	}
}
