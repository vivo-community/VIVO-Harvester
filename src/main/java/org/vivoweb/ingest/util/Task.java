package org.vivoweb.ingest.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static java.util.Arrays.*;

/**
 * Task Interface
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class Task {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Task.class);
	
	/**
	 * Uses given parameters to build an instance of the Task
	 * @param params mapping of parameter name to parameter value
	 * @return the built Task
	 * @throws IOException xml parsing error
	 * @throws SAXException xml parsing error
	 * @throws ParserConfigurationException xml parsing error
	 */
	@SuppressWarnings("unused")
	public static Task getInstance(Map<String,String> params) throws ParserConfigurationException, SAXException, IOException {
		throw new IllegalStateException("Task cannot be initialized as it is an Abstract Class.  Try instantiating a class that extends Task.");
	}
	
	/**
	 * Checks if task's parameters have been initialized, and executes the task
	 * @throws NumberFormatException non-numerical config value encountered where numerical value expected
	 */
	public abstract void executeTask();
	
	/**
	 * Get a specified parameter
	 * @param params the param list to retrieve from
	 * @param paramName the parameter to retrieve
	 * @param required is this parameter required?
	 * @return the value for the parameter
	 * @throws IllegalArgumentException parameter is required and does not exist
	 */
	protected static String getParam(Map<String,String> params, String paramName, boolean required) throws IllegalArgumentException {
		if(!params.containsKey(paramName)) {
			if(required) {
				throw new IllegalArgumentException("param missing: "+paramName);
			}
			return null;
		}
		return params.remove(paramName);
	}
	
	/**
	 * Checks if the OptionSet contains all the listed arguments
	 * @param ops the optionset to check
	 * @param args the arguments to check for
	 */
	protected static void checkNeededArgs(OptionSet ops, String... args) {
		for(String arg : args) {
			if(!ops.has(arg)) {
				throw new IllegalArgumentException("Missing Argument: "+arg);
			}
		}
	}
	
	/**
	 * Instantiate an instance of taskClass with args from filePath config
	 * @param taskClass subclass of Task to run with args from config
	 * @param filePath path to the config file
	 * @throws NoSuchMethodException method not defined
	 * @throws SecurityException violates security manager
	 * @throws InvocationTargetException cannot invoke using those params
	 * @throws IllegalAccessException  method is not accessible
	 * @throws IllegalArgumentException illegal arguments for method
	 */
	public static void runConfig(Class<? extends Task> taskClass, String filePath) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String[] args = {}; //TODO Chris: Fill this in
		taskClass.getMethod("main", String[].class).invoke(null, (Object)args);
	}
	
	/**
	 * Runs a task based off its config
	 * @param filepath path to config file
	 * @throws IOException xml parsing error
	 * @throws SAXException xml parsing error
	 * @throws ParserConfigurationException xml parsing error
	 */
	public static void runConfig(String filepath) throws ParserConfigurationException, SAXException, IOException {
//		Task t = TaskConfigParser.parseTaskConfig(filepath);
//		t.executeTask();
	}
	
	/**
	 * Get the OptionParser for this Task
	 * @return the OptionParser
	 */
	protected static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("x", "config"), "configuration file").withRequiredArg().describedAs("Config File Path");
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		OptionSet options = getParser().parse(args);
		try {
			if(options.has("config")) {
//				runConfig((String)options.valueOf("config"));
				Map<String, String> t = TaskConfigParser.parseTaskConfig((String)options.valueOf("config"));
				String[] params = {};
				List<String> paramList = new LinkedList<String>();
				for(String key : t.keySet()) {
					//
				}
				t.getClass().getMethod("main", args.getClass()).invoke(null, new Object());
			} else {
				getParser().printHelpOn(System.out);
			}
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
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
		public static Map<String, String> parseTaskConfig(String filename) throws ParserConfigurationException, SAXException, IOException {
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
//					Object tempTask = className.newInstance();
					Method builderMethod = className.getDeclaredMethod("getInstance", Map.class);
					Object tempTask = builderMethod.invoke(null, this.params);
					if(!(tempTask instanceof Task)) {
						throw new SAXException("Class must extend Task");
					}
					this.task = (Task)tempTask;
//					this.task.setParams(this.params);
				} catch(ClassNotFoundException e) {
					throw new SAXException("Unknown Class: "+this.type,e);
				} catch(SecurityException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IllegalArgumentException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IllegalAccessException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(NoSuchMethodException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(InvocationTargetException e) {
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
