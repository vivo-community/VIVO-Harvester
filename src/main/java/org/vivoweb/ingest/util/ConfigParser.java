package org.vivoweb.ingest.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.vfs.VFS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Config Parser for Tasks
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ConfigParser extends DefaultHandler {
	/**
	 * The param list from config file
	 */
	private Map<String,String> params;
	/**
	 * Temporary container for cdata
	 */
	private String tempVal;
	/**
	 * Temporary container for parameter id
	 */
	private String tempParamID;
	
	/**
	 * Default Constructor
	 */
	protected ConfigParser() {
		this.params = new HashMap<String,String>();
		this.tempVal = "";
	}
	
	/**
	 * Converts the contents of a config file to commandline arguments
	 * @param filePath path to the config file
	 * @return equivalent commandline argument array
	 * @throws SecurityException violates security manager
	 * @throws IllegalArgumentException illegal arguments for method
	 * @throws IOException error reading config file
	 * @throws SAXException xml parse error
	 * @throws ParserConfigurationException parser config error
	 */
	public static String[] configToArgs(String filePath) throws SecurityException, IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		Map<String, String> params = new ConfigParser().parseConfig(filePath);
		String[] paramArray = {};
		List<String> paramList = new LinkedList<String>();
		for(String key : params.keySet()) {
			String value = params.get(key);
			if(!value.equalsIgnoreCase("false")) {
				paramList.add("--"+key);
				if(!value.equalsIgnoreCase("true")) {
					paramList.add(value);
				}
			}
		}
		paramArray = paramList.toArray(paramArray);
		return paramArray;
	}
	
	/**
	 * Parses a configuration file describing a Task
	 * @param filename the name of the file to parse
	 * @return the Task described by the config file
	 * @throws IOException xml parsing error
	 * @throws SAXException xml parsing error
	 * @throws ParserConfigurationException xml parsing error
	 */
	private Map<String, String> parseConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
		SAXParser sp = spf.newSAXParser(); // get a new instance of parser
		sp.parse(VFS.getManager().resolveFile(new File("."), filename).getContent().getInputStream(), this); // parse the file and also register this class for call backs
		return this.params;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.tempVal = "";
		this.tempParamID = "";
		if(qName.equalsIgnoreCase("Task")) {
			//Do nothing, but keep to prevent falling into else
		} else if(qName.equalsIgnoreCase("Param")) {
			this.tempParamID = attributes.getValue("id");
		} else {
			throw new SAXException("Unknown Tag: "+qName);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.tempVal += new String(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("Task")) {
			//Do nothing, but leave it here so it doesn't fall into else statement
		} else if(qName.equalsIgnoreCase("Param")) {
			this.params.put(this.tempParamID, this.tempVal);
		} else {
			throw new SAXException("Unknown Tag: "+qName);
		}
	}
}