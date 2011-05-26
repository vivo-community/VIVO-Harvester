/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.recordhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.jenaconnect.TDBJenaConnect;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Record Handler Interface
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class RecordHandlerFactory {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RecordHandlerFactory.class);
	
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
		InputStream confStream = (configFileName == null) ? null : FileAide.getInputStream(configFileName);
		return parseConfig(confStream, overrideParams);
	}
	
	/**
	 * Build a RecordHandler based on the given parameter set
	 * @param params the value map
	 * @return the RecordHandler
	 * @throws IOException error configuring
	 */
	private static RecordHandler build(Map<String, String> params) throws IOException {
		if((params == null) || params.isEmpty()) {
			return null;
		}
		String type = params.get("type");
		if(StringUtils.isBlank(type)) {
			log.warn("No RecordHandler type specified: using binary (JenaRecordHandler in TBDJenaConnect)");
			type = "binary";
		}
		RecordHandler rh;
		if(type.equalsIgnoreCase("binary")) {
			type = "jena";
		}
		if(type.equalsIgnoreCase("jena")) {
			String dir = params.get("dir");
			rh = new JenaRecordHandler(new TDBJenaConnect(dir));
		} else if(type.equalsIgnoreCase("text")) {
			String dir = params.get("dir");
			rh = new TextFileRecordHandler(dir);
		} else if(type.equalsIgnoreCase("map")) {
			rh = new MapRecordHandler();
		} else if(type.equalsIgnoreCase("jdbc")) {
			String driver = params.get("driver");
			String url = params.get("url");
			String username = params.get("username");
			String password = params.get("password");
			String table = params.get("table");
			String field = params.get("field");
			rh = new JDBCRecordHandler(driver, url, username, password, table, field);
		} else {
			throw new IllegalArgumentException("Unknown type: "+type);
		}
		return rh;
	}
	
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
}
