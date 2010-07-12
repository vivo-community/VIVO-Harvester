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
 * Task Interface
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class Task {
	
	/**
	 * Has this task been initialized with its parameters
	 */
	private boolean ready = false;
	
	/**
	 * Uses given parameters to initialize the task and prepare for execution
	 * @param params mapping of parameter name to parameter value
	 * @throws IOException xml parsing error
	 * @throws SAXException xml parsing error
	 * @throws ParserConfigurationException xml parsing error
	 */
	public void setParams(Map<String,String> params) throws ParserConfigurationException, SAXException, IOException {
		acceptParams(params);
		this.ready = true;
	}
	
	/**
	 * Uses given parameters to initialize the task and prepare for execution
	 * @param params mapping of parameter name to parameter value
	 * @throws IOException xml parsing error
	 * @throws SAXException xml parsing error
	 * @throws ParserConfigurationException xml parsing error
	 */
	protected abstract void acceptParams(Map<String,String> params) throws ParserConfigurationException, SAXException, IOException;
	
	/**
	 * Checks if task's parameters have been initialized, and executes the task
	 * @throws NumberFormatException non-numerical config value encountered where numerical value expected
	 */
	public void executeTask() throws NumberFormatException {
		if(!this.ready) {
			throw new IllegalStateException("You must set the parameters first!");
		}
		runTask();
	}
	
	/**
	 * Checks if task's parameters have been initialized, and executes the task
	 * @throws NumberFormatException non-numerical config value encountered where numerical value expected
	 */
	protected abstract void runTask() throws NumberFormatException;
	
	/**
	 * Get a specified parameter
	 * @param params the param list to retrieve from
	 * @param paramName the parameter to retrieve
	 * @param required is this parameter required?
	 * @return the value for the parameter
	 * @throws IllegalArgumentException parameter is required and does not exist
	 */
	protected String getParam(Map<String,String> params, String paramName, boolean required) throws IllegalArgumentException {
		if(!params.containsKey(paramName)) {
			if(required) {
				throw new IllegalArgumentException("param missing: "+paramName);
			}
			return null;
		}
		return params.remove(paramName);
	}
	
	/**
	 * Main Method
	 * @param args command line arguments
	 * @throws IOException xml parsing error
	 * @throws SAXException xml parsing error
	 * @throws ParserConfigurationException xml parsing error
	 */
	public static void main(String... args) throws ParserConfigurationException, SAXException, IOException {
		if(args.length != 1) {
			throw new IllegalArgumentException("Usage: Task [path/to/config/file.xml]");
		}
		Task t = TaskConfigParser.parseTaskConfig(args[0]);
		t.executeTask();
	}
	
	/**
	 * Config Parser for Tasks
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private static class TaskConfigParser extends DefaultHandler {
		
		/**
		 * The task we are building
		 */
		private Task task;
		/**
		 * The param list from config file
		 */
		private Map<String,String> params;
		/**
		 * Class name for the RecordHandler
		 */
		private String type;
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
		protected TaskConfigParser() {
			this.params = new HashMap<String,String>();
			this.tempVal = "";
			this.type = "Unset!";
		}
		
		/**
		 * Parses a configuration file describing a Task
		 * @param filename the name of the file to parse
		 * @return the Task described by the config file
		 * @throws IOException xml parsing error
		 * @throws SAXException xml parsing error
		 * @throws ParserConfigurationException xml parsing error
		 */
		public static Task parseTaskConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
			return new TaskConfigParser().parseConfig(filename);
		}
		
		/**
		 * Parses a configuration file describing a Task
		 * @param filename the name of the file to parse
		 * @return the Task described by the config file
		 * @throws IOException xml parsing error
		 * @throws SAXException xml parsing error
		 * @throws ParserConfigurationException xml parsing error
		 */
		private Task parseConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(VFS.getManager().resolveFile(new File("."), filename).getContent().getInputStream(), this); // parse the file and also register this class for call backs
			return this.task;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.tempVal = "";
			this.tempParamID = "";
			if(qName.equalsIgnoreCase("Task")) {
				this.type = attributes.getValue("type");
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
				try {
					Class<?> className = Class.forName(this.type);
					Object tempTask = className.newInstance();
					if(!(tempTask instanceof Task)) {
						throw new SAXException("Class must extend Task");
					}
					this.task = (Task)tempTask;
					this.task.setParams(this.params);
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
				} catch(ParserConfigurationException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IOException e) {
					throw new SAXException(e.getMessage(),e);
				}
			} else if(qName.equalsIgnoreCase("Param")) {
				this.params.put(this.tempParamID, this.tempVal);
			} else {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
	}
	
}
