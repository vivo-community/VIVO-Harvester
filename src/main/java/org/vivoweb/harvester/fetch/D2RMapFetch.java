/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;
import de.fuberlin.wiwiss.d2r.D2rProcessor;

/**
 * Fetches database or csv data using D2RMap
 * @author Eliza Chan (elc2013@med.cornell.edu)
 */
public class D2RMapFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(D2RMapFetch.class);
	/**
	 * Record Handler to write records to
	 */
	private RecordHandler outStore;
	/**
	 * D2RMap config file path
	 */
	private String d2rConfigPath;
	/**
	 * D2RMap working directory
	 */
	private String d2rWDir;
	
	/**
	 * Library style Constructor
	 * @param configPath D2RMap config file path
	 * @param rh Record Handler to write records to
	 * @param workingDir D2RMap working directory
	 */
	public D2RMapFetch(String configPath, RecordHandler rh, String workingDir) {
		this.d2rConfigPath = configPath;
		this.outStore = rh;
		this.d2rWDir = workingDir;
		if(this.outStore == null) {
			throw new IllegalArgumentException("Must provide an output RecordHandler");
		}
	}

	/**
	 * Executes the task
	 */
	public void execute() {
		log.info("Fetch: Start");
		D2rProcessor proc = new D2rProcessor();
		proc.harvesterInit();
		try {
			if(this.d2rConfigPath != null) {
				String output;
				if(this.d2rWDir != null) { // process CSV file
					output = proc.processCsvMap("RDF/XML", this.d2rWDir, this.d2rConfigPath);
				} else { // process data from database
					output = proc.processMap("RDF/XML", this.d2rConfigPath);
				}
				this.outStore.addRecord("id", output, this.getClass());
			}
			
		} catch(Exception e) {
			System.err.println("D2RMapFetch errors: " + e);
		}
		log.info("Fetch: End");
	}
}
