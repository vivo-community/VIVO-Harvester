/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.vfs.VFS;
import org.vivoweb.harvester.util.repo.RecordMetaData.RecordMetaDataType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Record Handler Interface
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class RecordHandler implements Iterable<Record> {
	/**
	 * SLF4J Logger
	 */
	// private static Log log = LogFactory.getLog(RecordHandler.class);
	/**
	 * Do we overwrite existing records by default
	 */
	private boolean overwriteDefault = true;
	
	/**
	 * Sets parameters from param list
	 * @param params map of parameters
	 * @throws IllegalArgumentException invalid parameters
	 * @throws IOException error
	 */
	public abstract void setParams(Map<String, String> params) throws IllegalArgumentException, IOException;
	
	/**
	 * Adds a record to the RecordHandler
	 * @param rec record to add
	 * @param creator the creator
	 * @param overwrite when set to true, will automatically overwrite existing records
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public abstract boolean addRecord(Record rec, Class<?> creator, boolean overwrite) throws IOException;
	
	/**
	 * Adds a record to the RecordHandler
	 * @param recID record id to add
	 * @param recData record data to add
	 * @param creator the creator
	 * @param overwrite when set to true, will automatically overwrite existing records
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public boolean addRecord(String recID, String recData, Class<?> creator, boolean overwrite) throws IOException {
		return addRecord(new Record(recID, recData, this), creator, overwrite);
	}
	
	/**
	 * Adds a record to the RecordHandler If overwriteDefault is set to true, will automatically overwrite existing
	 * records
	 * @param rec record to add
	 * @param creator the creator
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public boolean addRecord(Record rec, Class<?> creator) throws IOException {
		return addRecord(rec, creator, isOverwriteDefault());
	}
	
	/**
	 * Adds a record to the RecordHandler If overwriteDefault is set to true, will automatically overwrite existing
	 * records
	 * @param recID record id to add
	 * @param recData record data to add
	 * @param creator the creator
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public boolean addRecord(String recID, String recData, Class<?> creator) throws IOException {
		return addRecord(new Record(recID, recData, this), creator);
	}
	
	/**
	 * Get a record
	 * @param recID record id to get
	 * @return record
	 * @throws IllegalArgumentException record not found
	 * @throws IOException error reading
	 */
	public Record getRecord(String recID) throws IllegalArgumentException, IOException {
		return new Record(recID, getRecordData(recID), this);
	}
	
	/**
	 * Retrieve the data for a given record
	 * @param recID id of record to retrieve
	 * @return data from record
	 * @throws IllegalArgumentException id not found
	 * @throws IOException error reading
	 */
	public abstract String getRecordData(String recID) throws IllegalArgumentException, IOException;
	
	/**
	 * Retrieves all metadata for a given record
	 * @param recID id of record to retrieve metadata for
	 * @return the metadata map
	 * @throws IOException error retrieving record metadata
	 */
	protected abstract SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException;
	
	/**
	 * Get the last RecordMetaData of a given type by a given operator for a given record
	 * @param recID id of record to retrieve metadata for
	 * @param type the type of metadata, null for any type
	 * @param operator the operator to get for, null for any type
	 * @return the last metadata of the specified type
	 * @throws IOException error retrieving record metadata
	 */
	protected RecordMetaData getLastMetaData(String recID, RecordMetaData.RecordMetaDataType type, Class<?> operator) throws IOException {
		for(RecordMetaData rmd : getRecordMetaData(recID)) {
			if((type == null || rmd.getOperation() == type) && (operator == null || rmd.getOperator().equals(operator))) {
				return rmd;
			}
		}
		return null;
	}
	
	/**
	 * Add a metadata record to indicate that the given operator has processed the given record
	 * @param rec record to set processed
	 * @param operator the class performing the processing
	 * @throws IOException error setting processed
	 */
	protected void setProcessed(Record rec, Class<?> operator) throws IOException {
		addMetaData(rec, operator, RecordMetaDataType.processed);
	}
	
	/**
	 * Add a metadata record to indicate that the given operator has written the given record
	 * @param rec record to set written
	 * @param operator the class performing the writing
	 * @throws IOException error setting written
	 */
	protected void setWritten(Record rec, Class<?> operator) throws IOException {
		addMetaData(rec, operator, RecordMetaDataType.written);
	}
	
	/**
	 * Adds a metadata record for the given record
	 * @param rec record to add metadata for
	 * @param operator the class operating on the record
	 * @param type the operation type
	 * @throws IOException error adding meta data
	 */
	protected void addMetaData(Record rec, Class<?> operator, RecordMetaDataType type) throws IOException {
		addMetaData(rec, new RecordMetaData(operator, type, RecordMetaData.md5hex(rec.getData())));
	}
	
	/**
	 * Adds a metadata record
	 * @param rec record to add metadata for
	 * @param rmd the metadata record
	 * @throws IOException error adding meta data
	 */
	protected abstract void addMetaData(Record rec, RecordMetaData rmd) throws IOException;
	
	/**
	 * Deletes all metadata for a record
	 * @param recID record id to delete metadata for
	 * @throws IOException error deleting metadata
	 */
	protected abstract void delMetaData(String recID) throws IOException;
	
	/**
	 * Delete the specified Record
	 * @param recID id of record to delete
	 * @throws IOException i/o error
	 */
	public abstract void delRecord(String recID) throws IOException;
	
	/**
	 * Get a specified parameter
	 * @param params the param list to retrieve from
	 * @param paramName the parameter to retrieve
	 * @param required is this parameter required?
	 * @return the value for the parameter
	 * @throws IllegalArgumentException parameter is required and does not exist
	 */
	protected String getParam(Map<String, String> params, String paramName, boolean required) throws IllegalArgumentException {
		if(!params.containsKey(paramName)) {
			if(required) {
				throw new IllegalArgumentException("param missing: " + paramName);
			}
			return null;
		}
		return params.remove(paramName);
	}
	
	/**
	 * Build RecordHandler based on config file
	 * @param filename filename of config file
	 * @return RecordHandler described by config file
	 * @throws IOException xml config parse error
	 * @throws SAXException xml config parse error
	 * @throws ParserConfigurationException xml config parse error
	 */
	public static RecordHandler parseConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
		return parseConfig(filename, null);
	}
	
	/**
	 * Build RecordHandler based on config file and overrides using the specified parameters
	 * @param filename filename of config file
	 * @param overrideParams the parameters to override the file with
	 * @return RecordHandler described by config file
	 * @throws IOException xml config parse error
	 * @throws SAXException xml config parse error
	 * @throws ParserConfigurationException xml config parse error
	 */
	public static RecordHandler parseConfig(String filename, Properties overrideParams) throws ParserConfigurationException, SAXException, IOException {
		return new RecordHandlerParser().parseConfig(filename, overrideParams);
	}
	
	/**
	 * Setter for overwriteDefault
	 * @param overwrite the new value for overwriteDefault
	 */
	public void setOverwriteDefault(boolean overwrite) {
		this.overwriteDefault = overwrite;
	}
	
	/**
	 * Getter for overwriteDefault
	 * @return the overwriteDefault
	 */
	public boolean isOverwriteDefault() {
		return this.overwriteDefault;
	}
	
	/**
	 * Closes the recordhandler
	 * @throws IOException error closing
	 */
	public abstract void close() throws IOException;
	
	/**
	 * Config Parser for RecordHandlers
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private static class RecordHandlerParser extends DefaultHandler {
		/**
		 * The RecordHandler we are building
		 */
		private RecordHandler rh;
		/**
		 * The param list from config file
		 */
		private Map<String, String> params;
		/**
		 * Class name for the RecordHandler
		 */
		private String type;
		/**
		 * Temporary container for cdata
		 */
		private String tempVal;
		/**
		 * Temporary container for parameter name
		 */
		private String tempParamName;
		
		/**
		 * Default Constructor
		 */
		protected RecordHandlerParser() {
			this.params = new HashMap<String, String>();
			this.tempVal = "";
			this.type = "Unset!";
		}
		
		/**
		 * Parses a configuration file describing a RecordHandler
		 * @param filename the name of the file to parse
		 * @param overrideParams parameters that override the params in the config file
		 * @return the RecordHandler described by the config file
		 * @throws IOException xml parsing error
		 * @throws SAXException xml parsing error
		 * @throws ParserConfigurationException xml parsing error
		 */
		protected RecordHandler parseConfig(String filename, Properties overrideParams) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(VFS.getManager().resolveFile(new File("."), filename).getContent().getInputStream(), this); // parse
			// the
			// file
			// and
			// also
			// register
			// this
			// class
			// for
			// call
			// backs
			if(overrideParams != null) {
				for(String key : overrideParams.stringPropertyNames()) {
					this.params.put(key, overrideParams.getProperty(key));
				}
			}
			try {
				Class<?> className = Class.forName(this.type);
				Object tempRH = className.newInstance();
				if(!(tempRH instanceof RecordHandler)) {
					throw new SAXException("Class must extend RecordHandler");
				}
				this.rh = (RecordHandler)tempRH;
				this.rh.setParams(this.params);
			} catch(ClassNotFoundException e) {
				throw new SAXException("Unknown Class: " + this.type, e);
			} catch(SecurityException e) {
				throw new SAXException(e.getMessage(), e);
			} catch(IllegalArgumentException e) {
				throw new SAXException(e.getMessage(), e);
			} catch(InstantiationException e) {
				throw new SAXException(e.getMessage(), e);
			} catch(IllegalAccessException e) {
				throw new SAXException(e.getMessage(), e);
			} catch(IOException e) {
				throw new SAXException(e.getMessage(), e);
			}
			this.rh.setOverwriteDefault(true);
			return this.rh;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.tempVal = "";
			this.tempParamName = "";
			if(qName.equalsIgnoreCase("RecordHandler")) {
				this.type = attributes.getValue("type");
			} else if(qName.equalsIgnoreCase("Param")) {
				this.tempParamName = attributes.getValue("name");
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal = new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("RecordHandler")) {
				// ignore
			} else if(qName.equalsIgnoreCase("Param")) {
				this.params.put(this.tempParamName, this.tempVal);
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
	}
	
	/**
	 * Has the given record been written since last processed by operator?
	 * @param id the record to check fo
	 * @param operator the class to check for
	 * @return true if written since last processed by operator or if never been processed by operator
	 * @throws IOException error getting meta data
	 */
	public boolean needsProcessed(String id, Class<?> operator) throws IOException {
		RecordMetaData rmdWrite = getLastMetaData(id, RecordMetaDataType.written, null);
		Calendar write = rmdWrite.getDate();
		RecordMetaData rmdProcess = getLastMetaData(id, RecordMetaDataType.processed, operator);
		// log.debug("rmdWrite: "+rmdWrite);
		// log.debug("rmdProcess: "+rmdProcess);
		if(rmdProcess == null) {
			return true;
		}
		Calendar processed = rmdProcess.getDate();
		return (processed.compareTo(write) < 0);
	}
	
	/**
	 * Does the given record contain updated information compared to existing record data
	 * @param rec the record
	 * @return true if need updated or record is new
	 */
	protected boolean needsUpdated(Record rec) {
		// log.debug("Checking if Record "+rec.getID()+" needs updated");
		try {
			RecordMetaData rmd = getLastMetaData(rec.getID(), RecordMetaDataType.written, null);
			// Check if previous written record meta data exists
			if(rmd != null) {
				// Get previous record meta data md5
				String oldMD5 = rmd.getMD5();
				// If md5s same
				String newMD5 = RecordMetaData.md5hex(rec.getData());
				if(newMD5.equals(oldMD5)) {
					// do nothing more
					// log.debug("Record "+rec.getID()+" has not changed... no need to update.");
					return false;
				}
				// log.debug("Record has changed... need to update");
			} else {
				// log.debug("Record never written... need to update");
			}
			return true;
		} catch(IOException e) {
			// error getting metadata file... assume it does not exist
			// TODO Chris: RC2 - perhaps we can test for that assumption?
			// log.debug("Record "+rec.getID()+" has no metadata... need to update.");
			return true;
		}
	}

	/**
	 * Find records with idText in their id
	 * @param idText the text to find
	 * @return list of ids that match
	 */
	public abstract List<String> find(String idText);
}
