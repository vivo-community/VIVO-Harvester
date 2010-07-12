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
package org.vivoweb.ingest.util;

/**
 * Individual record
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class Record {
	/**
	 * ID of this record
	 */
	private String id;
	/**
	 * Data for this record
	 */
	private String data;
	
	/**
	 * Constructor
	 * @param recID records id
	 * @param recData records data
	 */
	public Record(String recID, String recData) {
		this.id = recID;
		this.data = recData;
	}
	
	/**
	 * Getter for ID
	 * @return the records id
	 */
	public String getID() {
		return this.id;
	}
	
	/**
	 * Setter for Data
	 * @param newData data to be used
	 */
	@SuppressWarnings("unused") //TODO Chris: eventually make this public and automatically update the data in the original record handler
	private void setData(String newData) {
		this.data = newData;
	}
	
	/**
	 * Getter for Data
	 * @return the records data
	 */
	public String getData() {
		return this.data;
	}
	
}
