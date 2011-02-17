/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static Logger log = LoggerFactory.getLogger(RecordHandler.class);
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
	 * Config Stream Based Factory that overrides parameters
	 * @param configStream the config input stream
	 * @param overrideParams the parameters to override the file with
	 * @return RecordHandler instance
	 * @throws IOException error configuring
	 */
	public static RecordHandler parseConfig(InputStream configStream, Map<String, String> overrideParams) throws IOException {
		Map<String, String> paramList = new RecordHandlerParser().parseConfig(configStream);
		if(overrideParams != null) {
			for(String key : overrideParams.keySet()) {
				paramList.put(key, overrideParams.get(key));
			}
		}
		for(String param : paramList.keySet()) {
			if(!param.equalsIgnoreCase("dbUser") && !param.equalsIgnoreCase("dbPass")) {
				log.debug("'" + param + "' - '" + paramList.get(param) + "'");
			}
		}
		return build(paramList);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFile the vfs config file descriptor
	 * @return RecordHandler instance
	 * @throws IOException error configuring
	 */
	public static RecordHandler parseConfig(FileObject configFile) throws IOException {
		return parseConfig(configFile, null);
	}
	
	/**
	 * Config File Based Factory that overrides parameters
	 * @param configFile the vfs config file descriptor
	 * @param overrideParams the parameters to override the file with
	 * @return RecordHandler instance
	 * @throws IOException error configuring
	 */
	public static RecordHandler parseConfig(FileObject configFile, Map<String, String> overrideParams) throws IOException {
		InputStream confStream = (configFile == null) ? null : configFile.getContent().getInputStream();
		return parseConfig(confStream, overrideParams);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFile the config file descriptor
	 * @return RecordHandler instance
	 * @throws IOException error configuring
	 */
	public static RecordHandler parseConfig(File configFile) throws IOException {
		return parseConfig(configFile, null);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFile the config file descriptor
	 * @param overrideParams the parameters to override the file with
	 * @return RecordHandler instance
	 * @throws IOException error configuring
	 */
	public static RecordHandler parseConfig(File configFile, Map<String, String> overrideParams) throws IOException {
		InputStream confStream = (configFile == null) ? null : VFS.getManager().resolveFile(new File("."), configFile.getAbsolutePath()).getContent().getInputStream();
		return parseConfig(confStream, overrideParams);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFileName the config file path
	 * @return RecordHandler instance
	 * @throws IOException xml parse error
	 */
	public static RecordHandler parseConfig(String configFileName) throws IOException {
		return parseConfig(configFileName, null);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFileName the config file path
	 * @param overrideParams the parameters to override the file with
	 * @return RecordHandler instance
	 * @throws IOException xml parse error
	 */
	public static RecordHandler parseConfig(String configFileName, Map<String, String> overrideParams) throws IOException {
		InputStream confStream = (configFileName == null) ? null : VFS.getManager().resolveFile(new File("."), configFileName).getContent().getInputStream();
		return parseConfig(confStream, overrideParams);
	}
	
	/**
	 * Build a RecordHandler based on the given parameter set
	 * @param params the value map
	 * @return the RecordHandler
	 * @throws IOException error configuring
	 */
	private static RecordHandler build(Map<String, String> params) throws IOException {
		// for(String param : params.keySet()) {
		// log.debug(param+" => "+params.get(param));
		// }
		if(params == null || params.isEmpty()) {
			return null;
		}
		String type = params.get("rhClass");
		if(type == null) {
			log.warn("No RecordHandler class specified: using MapRecordHandler");
			type = MapRecordHandler.class.getCanonicalName();
		}
		RecordHandler rh;
		log.debug("Using class: '" + type + "'");
		try {
			Object tempRH = Class.forName(type).newInstance();
			if(!(tempRH instanceof RecordHandler)) {
				throw new IOException("Class must extend RecordHandler");
			}
			rh = (RecordHandler)tempRH;
			rh.setParams(params);
		} catch(ClassNotFoundException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		} catch(InstantiationException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		} catch(IllegalAccessException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		
		return rh;
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
		 * The param list from config file
		 */
		private Map<String, String> params;
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
		}
		
		/**
		 * Parses a configuration file describing a RecordHandler
		 * @param configStream the config input stream
		 * @return the RecordHandler described by the config file
		 * @throws IOException xml parsing error
		 */
		protected Map<String, String> parseConfig(InputStream configStream) throws IOException {
			if(configStream != null) {
				try {
					// get a factory
					SAXParserFactory spf = SAXParserFactory.newInstance();
					// get a new instance of parser
					SAXParser sp = spf.newSAXParser();
					// parse the stream and also register this class for call backs
					sp.parse(configStream, this);
				} catch(SAXException e) {
					throw new IOException(e.getMessage(), e);
				} catch(ParserConfigurationException e) {
					throw new IOException(e.getMessage(), e);
				}
			}
			return this.params;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.tempVal = "";
			this.tempParamName = "";
			if(qName.equalsIgnoreCase("Param")) {
				this.tempParamName = attributes.getValue("name");
			} else if(!qName.equalsIgnoreCase("RecordHandler")) {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal = new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("Param")) {
				this.params.put(this.tempParamName, this.tempVal);
			} else if(!qName.equalsIgnoreCase("RecordHandler")) {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
	}
	
	/**
	 * Has the given record been written since last processed by operator?
	 * @param id the record to check fo
	 * @param operator the class to check for
	 * @return true if written since last processed by operator or if never been processed by operator
	 */
	public boolean needsProcessed(String id, Class<?> operator) {
		try {
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
		} catch(IOException e) {
			// error getting metadata file... assume it does not exist
			//			log.debug("Record "+id+" has no metadata... need to update.");
			return true;
		}
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
			//			log.debug("Record "+rec.getID()+" has no metadata... need to update.");
			return true;
		}
	}
	
	/**
	 * Find records with idText in their id
	 * @param idText the text to find
	 * @return list of ids that match
	 * @throws IOException error searching
	 */
	public abstract Set<String> find(String idText) throws IOException;
}
