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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;

/**
 * Wrapper for Fetch sub-functions and commandline parser.
 * @author Dale Scheppler Dscheppler@ctrip.ufl.edu
 */
public class Fetch 
{

	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Fetch.class);								//Initialize the Log4J log Factory.
	
	/**
	 * Main Method
	 * @author Dale Scheppler
	 * @param args The command line parameters input by the user at runtime.
	 */
	public static void main(String[] args)
	{
		log.info("Initializing Fetch.");													//Log that we're starting a fetch.
		if(args.length == 1)																//We only take one argument.
		{
			for(String strArguments: args)
			{
				try
				{
					if(strArguments.equalsIgnoreCase("OAI"))													//If they typed "OAI"
					{
						Task.main("config/tasks/OAIFetchTask.xml");						//Run the OAI Fetch
					}
					else if(strArguments.equalsIgnoreCase("NIH"))												//If they typed "NIH"
					{
						//TODO Dale: Finish the NIH fetch
						//Nothing Yet																	//Run the NIH Fetch
					}
					else if(strArguments.equalsIgnoreCase("PubMed"))											//If they typed "PubMed"
					{
						Task.main("config/tasks/PubmedFetchTask.xml");							//Run the PubMed Fetch
					}
					else if(strArguments.equalsIgnoreCase("JDBC"))												//If they typed RDB
					{
						Task.main("config/tasks/JDBCFetchTask.xml");								//Run the JDBC Fetch
					}
					else																			//Otherwise
					{
						log.fatal("Fetch initialized with inappropriate argument.");				//Log that they input an improper commandline argument.				
						System.out.println("Invalid Argument. Valid arguments are NIH, OAI, PubMed, or RDB.");
					}
				} catch(ParserConfigurationException e) {
					log.error(e.getMessage(),e);
				} catch(SAXException e) {
					log.error(e.getMessage(),e);
				} catch(IOException e) {
					log.error(e.getMessage(),e);
				}
			}
		}
		else																				//Otherwise
		{
			log.fatal("Fetch attempted to run with incorrect number of arguments. Usage: Fetch [OAI|NIH|PubMed|JDBC]");		//Log that they tried to put in too many or too few arguments.
		}
		log.info("Fetch Complete.");														//Log that fetch is shutting down.
	}
	
	/**
	 * Default Constructor
	 */
	private Fetch(){
		//No Initiallization
	}
}
