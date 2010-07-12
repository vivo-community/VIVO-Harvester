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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class MapRecordHandler extends RecordHandler {
	//This code was marked as may cause compile errors by UCDetector.
	//Change visibility of class to Private
	//FIXME This code was marked as may cause compile errors by UCDetector.
	
	Map<String,String> map;
	//This code was marked as may cause compile errors by UCDetector.
	//Change visibility of Field "MapRecordHandler.map" to Private
	//FIXME This code was marked as may cause compile errors by UCDetector.
	
	/**
	 * Default Constructor
	 */
	public MapRecordHandler() {
		this.map = new HashMap<String,String>();
	}
	
	@Override
	public void addRecord(Record rec, boolean overwrite) throws IOException {
		if(!overwrite && this.map.containsKey(rec.getID())) {
			throw new IOException("Record already exists!");
		}
		this.map.put(rec.getID(), rec.getData());
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		this.map.remove(recID);
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		return this.map.get(recID);
	}
	
	@Override
	public Iterator<Record> iterator() {
		return new MapRecordIterator();
	}
	
	private class MapRecordIterator implements Iterator<Record> {
		private Iterator<String> keyIter;
		
		protected MapRecordIterator() {
			this.keyIter = MapRecordHandler.this.map.keySet().iterator();
		}
		
		public boolean hasNext() {
			return this.keyIter.hasNext();
		}
		
		public Record next() {
			String key = this.keyIter.next();
			String data = MapRecordHandler.this.map.get(key);
			return new Record(key,data);
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		
	}
	
}
