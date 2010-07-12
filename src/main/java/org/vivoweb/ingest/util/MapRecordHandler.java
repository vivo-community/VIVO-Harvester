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
 * Record Handler that uses a Java Map to store records in memory
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MapRecordHandler extends RecordHandler {
	/**
	 * The map to store records in 
	 */
	Map<String,String> map;
	
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
	
	/**
	 * Iterator for MapRecordHandler
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private class MapRecordIterator implements Iterator<Record> {
		/**
		 * Iterator for the keys in the map
		 */
		private Iterator<String> keyIter;
		
		/**
		 * Default Constructor
		 */
		protected MapRecordIterator() {
			this.keyIter = MapRecordHandler.this.map.keySet().iterator();
		}
		
		@Override
		public boolean hasNext() {
			return this.keyIter.hasNext();
		}
		
		@Override
		public Record next() {
			String key = this.keyIter.next();
			String data = MapRecordHandler.this.map.get(key);
			return new Record(key,data);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		//No params to set
	}
	
}
