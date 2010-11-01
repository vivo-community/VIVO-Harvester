/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.util.repo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.vivoweb.ingest.util.repo.RecordMetaData.RecordMetaDataType;

/**
 * Record Handler that uses a Java Map to store records in memory
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MapRecordHandler extends RecordHandler {
	/**
	 * The map to store records in
	 */
	Map<String, String> map;
	/**
	 * The map to store metadata in
	 */
	Map<String, SortedSet<RecordMetaData>> metaDataMap;
	
	/**
	 * Default Constructor
	 */
	public MapRecordHandler() {
		this.map = new HashMap<String, String>();
		this.metaDataMap = new HashMap<String, SortedSet<RecordMetaData>>();
	}
	
	@Override
	public boolean addRecord(Record rec, Class<?> creator, boolean overwrite) throws IOException {
		if(!needsUpdated(rec)) {
			return false;
		}
		if(!overwrite && this.map.containsKey(rec.getID())) {
			throw new IOException("Record already exists!");
		}
		this.map.put(rec.getID(), rec.getData());
		addMetaData(rec, creator, RecordMetaDataType.written);
		return true;
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		this.map.remove(recID);
		delMetaData(recID);
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		if(!this.map.containsKey(recID)) {
			throw new IllegalArgumentException("Record " + recID + " does not exist!");
		}
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
			return new Record(key, data, MapRecordHandler.this);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		// No params to set
	}
	
	@Override
	protected void addMetaData(Record rec, RecordMetaData rmd) {
		if(!this.metaDataMap.containsKey(rec.getID())) {
			this.metaDataMap.put(rec.getID(), new TreeSet<RecordMetaData>());
		}
		this.metaDataMap.get(rec.getID()).add(rmd);
	}
	
	@Override
	protected void delMetaData(String recID) throws IOException {
		this.metaDataMap.remove(recID);
	}
	
	@Override
	protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
		SortedSet<RecordMetaData> x = this.metaDataMap.get(recID);
		if(x == null || x.isEmpty()) {
			throw new IOException("No Matching MetaData Found");
		}
		return x;
	}
	
	@Override
	public void close() throws IOException {
		this.map.clear();
		this.metaDataMap.clear();
	}

	@Override
	public List<String> find(String idText) {
		List<String> retVal = new LinkedList<String>();
		for(String id : this.map.keySet()) {
			if(id.contains(idText)) {
				retVal.add(id);
			}
		}
		return retVal;
	}
}
