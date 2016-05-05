/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;

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
	 * the recordhandler this record came from
	 */
	private RecordHandler rh;
	
	/**
	 * Constructor
	 * @param recID records id
	 * @param recData records data
	 * @param recordhandler the recordhandler this record came from
	 */
	protected Record(String recID, String recData, RecordHandler recordhandler) {
		this.id = recID;
		this.data = recData;
		this.rh = recordhandler;
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
	 * @param operator the class setting the data
	 * @throws IOException error writing data to recordhandler
	 */
	public void setData(String newData, Class<?> operator) throws IOException {
		this.data = newData;
		this.rh.addRecord(this, operator, true);
	}
	
	/**
	 * Getter for Data
	 * @return the records data
	 */
	public String getData() {
		return this.data;
	}
	
	/**
	 * Set this record as processed by the given operator
	 * @param operator the operator that processed this record
	 * @throws IOException error setting processed
	 */
	public void setProcessed(Class<?> operator) throws IOException {
		this.rh.setProcessed(this, operator);
	}
	
	/**
	 * Has this record been written since last processed by operator?
	 * @param operator the class to check for
	 * @return true if written since last processed by operator or if never been processed by operator
	 */
	public boolean needsProcessed(Class<?> operator) {
		return this.rh.needsProcessed(getID(), operator);
	}
}
