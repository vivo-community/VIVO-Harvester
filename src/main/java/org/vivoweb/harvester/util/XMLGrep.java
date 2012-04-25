package org.vivoweb.harvester.util;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;

/**
 * @author kuppuraj, Mayank Saini
 *
 */
public class XMLGrep {
	
	/**
	 * 
	 */
	protected static Logger log = LoggerFactory.getLogger(XMLGrep.class);
	/**
	 * Input src directory
	 */
	private String src;
	/**
	 * Input dest directory
	 */
	private String dest = "";
	
	/**
	 * xpath expression to filter files
	 */
	private String exp;
	
	/**
	 * Constructor
	 * @param src directory to read files from
	 * @param dest directory to move files from
	 * @param xpath expression to filter
	 */
	public XMLGrep(String src, String dest, String value, String name) {
		this.src = src;
		this.dest = dest;

		if(value == null) 
		{
			this.exp = "";
			log.error("Value is required");
			System.exit(0);
		}
		else 
		{
			if(name == null)
			{
				this.exp = "//*[. ='"+ value + "']";
			} 
			else 
			{
				this.exp = "//" + name + "[. = '" + value + "']";
			}
			
		}
		
		
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private XMLGrep(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList arguments
	 */
	private XMLGrep(ArgList argList) {
		this(argList.get("s"), argList.get("d"), argList.get("v"), argList.get("n"));
	}
	
	/**
	 * 
	 * @param myFile
	 * @param expression
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean findInFile(File myFile, String expression) throws IOException {
		
		String result = XPathTool.getXPathResult(myFile.getPath(), expression);
		
		if(result != null && !result.isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param src
	 * @param dest
	 * @param xpathExpression
	 * @throws IOException 
	 */
	@SuppressWarnings("javadoc")
	public static void moveFile(File src, String dest) throws IOException {
		String newpath = "";
		
		if(! FileAide.exists(dest)) {
			FileAide.createFolder(dest);
		}
		
		if(! dest.endsWith("/")) {
			newpath = dest.concat("/").concat(src.getName());
		}
		else {
			newpath = dest.concat(src.getName());
		}
		
		FileAide.createFile(newpath);
		System.out.println("Moved to " + newpath);
		FileAide.setTextContent(newpath, FileAide.getTextContent(src.getPath()));
		FileAide.delete(src.getPath());

	}
	
	/**
	 * Runs the XMLGrep
	 * @throws IOException error executing
	 */
	public void execute() {
		//grepXML(this.src, this.dest, this.exp);
		String newpath = "";
		try {
			if(FileAide.isFolder(this.src)) {
				File dir = new File(this.src);
				File[] files = dir.listFiles();
				for(File file : files) {
					if (findInFile(file,this.exp)) {
						moveFile(file,this.dest);
					}
				}
			} else if(FileAide.isFile(this.src)) {
				File file = new File(this.src);
				if(findInFile(file,this.exp)) {
					moveFile(file,this.dest);
				}
			}
		} catch(IOException e) {
			System.out.println(e);
			log.error(e.getMessage());
		}

	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("XMLGrep");
		// src
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("src-dir").withParameter(true, "SRC_DIRECTORY").setDescription("SRC directory to read files from").setRequired(true));
		// dest
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dest-dir").withParameter(true, "DEST_DIRECTORY").setDescription("DEST directory to write files to").setRequired(true));
		// exp
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("tag-name").withParameter(true, "TAG_NAME").setDescription("TAG Name to Search for").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("tag-value").withParameter(true, "TAG_VALUE").setDescription("TAG value to Search for").setRequired(true));
		
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			String harvLev = System.getProperty("console-log-level");
			System.setProperty("console-log-level", "OFF");
			InitLog.initLogger(args, getParser(), "h");
			if(harvLev == null) {
				System.clearProperty("console-log-level");
			} else {
				System.setProperty("console-log-level", harvLev);
			}
			log.info(getParser().getAppName() + ": Start");
			new XMLGrep(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
}