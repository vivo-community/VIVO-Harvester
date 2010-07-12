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
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class Record {
	private String id;
	private String data;
	
	/**
	 * Constructor
	 * @param recID records id
	 * @param recData records data
	 */
	public Record(String recID, String recData) {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of constructor "Record.Record" to Protected.
		//FIXME This code was marked as may cause compile errors by UCDetector.
		setID(recID);
		setData(recData);
	}
	
	/**
	 * @param newID id to be used
	 */
	private void setID(String newID) {
		this.id = newID;
	}
	
	/**
	 * @return the records id
	 */
	public String getID() {
		return this.id;
	}
	
	/**
	 * @param newData data to be used
	 */
	private void setData(String newData) {
		this.data = newData;
	}
	
	/**
	 * @return the records data
	 */
	public String getData() {
		return this.data;
	}
	
}
