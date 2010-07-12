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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.vfs.VFS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public abstract class RecordHandler implements Iterable<Record> {
	
	private boolean overwriteDefault = false;
	
	/**
	 * @param params map of parameters
	 * @throws IllegalArgumentException invalid parameters
	 * @throws IOException error 
	 */
	public abstract void setParams(Map<String,String> params) throws IllegalArgumentException, IOException;
	
	/**
	 * Adds a record to the RecordHandler
	 * @param rec record to add
	 * @param overwrite when set to true, will automatically overwrite existing records
	 * @throws IOException error adding
	 */
	public abstract void addRecord(Record rec, boolean overwrite) throws IOException;
	
	/**
	 * Adds a record to the RecordHandler
	 * @param recID record id to add
	 * @param recData record data to add
	 * @param overwrite when set to true, will automatically overwrite existing records
	 * @throws IOException error adding
	 */
	public void addRecord(String recID, String recData, boolean overwrite) throws IOException {
		addRecord(new Record(recID, recData), overwrite);
	}
	
	/**
	 * Adds a record to the RecordHandler
	 * If overwriteDefault is set to true, will automatically overwrite existing records
	 * @param rec record to add
	 * @throws IOException error adding
	 */
	public void addRecord(Record rec) throws IOException {
		addRecord(rec, isOverwriteDefault());
	}
	
	/**
	 * Adds a record to the RecordHandler
	 * If overwriteDefault is set to true, will automatically overwrite existing records
	 * @param recID record id to add
	 * @param recData record data to add
	 * @throws IOException error adding
	 */
	public void addRecord(String recID, String recData) throws IOException {
		addRecord(new Record(recID, recData));
	}
	
	/**
	 * @param recID record id to get
	 * @return record
	 * @throws IllegalArgumentException record not found 
	 * @throws IOException error reading
	 */
	public Record getRecord(String recID) throws IllegalArgumentException, IOException {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of method "RecordHandler.getRecord" to Protected.
		//FIXME This code was marked as may cause compile errors by UCDetector.
		return new Record(recID, getRecordData(recID));
	}
	
	/**
	 * @param recID id of record to retrieve
	 * @return data from record
	 * @throws IllegalArgumentException id not found
	 * @throws IOException error reading
	 */
	public abstract String getRecordData(String recID) throws IllegalArgumentException, IOException;
	
	/**
	 * @param recID id of record to delete
	 * @throws IOException i/o error
	 */
	public abstract void delRecord(String recID) throws IOException;
	
	protected String getParam(Map<String,String> params, String paramName, boolean required) {
		if(!params.containsKey(paramName)) {
			if(required) {
				throw new IllegalArgumentException("param missing: "+paramName);
			}
			return null;
		}
		return params.remove(paramName);
	}
	
	/**
	 * @param filename filename of config file
	 * @return RecordHandler described by config file
	 * @throws IOException xml config parse error
	 * @throws SAXException xml config parse error
	 * @throws ParserConfigurationException xml config parse error
	 */
	public static RecordHandler parseConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
		return new RecordHandlerParser().parseRecordHandlerConfig(filename);
	}
	
	/**
	 * @param overwriteDefault the overwriteDefault to set
	 */
	public void setOverwriteDefault(boolean overwriteDefault) {
		this.overwriteDefault = overwriteDefault;
	}

	/**
	 * @return the overwriteDefault
	 */
	public boolean isOverwriteDefault() {
		return this.overwriteDefault;
	}

	private static class RecordHandlerParser extends DefaultHandler {
		
		private RecordHandler rh;
		private Map<String,String> params;
		private String type;
		private String tempVal;
		private String tempParamName;
		
		protected RecordHandlerParser() {
			this.params = new HashMap<String,String>();
			this.tempVal = "";
			this.type = "Unset!";
		}
		
		/**
		 * Parses a configuration file describing a RecordHandler
		 * @param filename the name of the file to parse
		 * @return the RecordHandler described by the config file
		 * @throws IOException xml parsing error
		 * @throws SAXException xml parsing error
		 * @throws ParserConfigurationException xml parsing error
		 */
		public RecordHandler parseRecordHandlerConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
			return new RecordHandlerParser().parseConfig(filename);
		}
		
		private RecordHandler parseConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(VFS.getManager().resolveFile(new File("."), filename).getContent().getInputStream(), this); // parse the file and also register this class for call backs
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
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal = new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("RecordHandler")) {
				try {
					Class<?> className = Class.forName(this.type);
					Object tempRH = className.newInstance();
					if(!(tempRH instanceof RecordHandler)) {
						throw new SAXException("Class must extend RecordHandler");
					}
					this.rh = (RecordHandler)tempRH;
					this.rh.setParams(this.params);
				} catch(ClassNotFoundException e) {
					throw new SAXException("Unknown Class: "+this.type,e);
				} catch(SecurityException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IllegalArgumentException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(InstantiationException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IllegalAccessException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IOException e) {
					throw new SAXException(e.getMessage(),e);
				}
			} else if(qName.equalsIgnoreCase("Param")) {
				this.params.put(this.tempParamName, this.tempVal);
			} else {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
	}
	
}
