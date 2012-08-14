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
	 * Input alternate destination for items that do not match the expression
	 */
	private String altDest = "";
	
	/**
	 * Destination for items with malformed XML or which generate errors.
	 */
	private String errorDest = "";
	
	/**
	 * xpath expression to filter files
	 */
	private String expression;
		
	/**
	 * Constructor
	 * @param src directory to read files from
	 * @param dest directory to move files from
	 * @param xpath expression to filter
	 */
	public XMLGrep(String src, String dest, String altDest, String errorDest, String value, String name) {
		this.src = src;
		this.dest = dest;
		this.errorDest = errorDest;
		
		if (altDest != null) {
			this.altDest = altDest;
		}
		
		if(value == null) 
		{
			this.expression ="//"+name;
			
		}
		else 
		{
			if(name == null)
			{
				this.expression = "//*[. ='"+ value + "']";
			} 
			else 
			{
				this.expression = "//" + name + "[. = '" + value + "']";
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
		this(argList.get("s"), argList.get("d"), argList.get("a"), argList.get("e"), argList.get("v"), argList.get("n"));
	}
	
	/**
	 * 
	 * @param myFile
	 * @param exp
	 * @return boolean
	 * @throws IOException
	 */
	public boolean findInFile(File myFile, String exp) throws IOException {
		
		String result = null;
		
		try
		{
			result = XPathTool.getXPathResult(myFile.getPath(), exp);
		}
		catch (IOException e)
		{
			log.error("Exception in XPathTool: Malformed XML, or bad Parser Configuration");
			log.debug("Moving offending file: " + myFile.getPath() + " to error destination: " + this.errorDest);
			moveFile(myFile, this.errorDest);
		}
		catch (IllegalArgumentException e)
		{
			log.error("Exception in XPathTool: Invalid XPath Expression.");
		}
			
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
					// If the current file is not a directory skip it
					if (! FileAide.isFolder(file.toString())) {
						if (findInFile(file,this.expression)) {
							//If the current file matches the xpath expression then move it
							moveFile(file,this.dest);
						} else {
							//Check for case where no altDest provided, or altDest = srcDest
							if (this.src.equals(this.altDest) || this.altDest.equals("") )
							{
								// Ignore the file, as we wish to leave it in place.
							}
							//If the current file does not match the xpath expression then
							//check to see if there is an alternate destination defined
							else if (this.altDest != null) {
								//Alternate destination defined so move file to alternate destination
								moveFile(file,this.altDest);
							}
							
						}
					}
				}
			} else if(FileAide.isFile(this.src)) {
				File file = new File(this.src);
				if(findInFile(file,this.expression)) {
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
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("src-dir").withParameter(true, "SRC_DIRECTORY").setDescription("SRC directory to read files from").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dest-dir").withParameter(true, "DEST_DIRECTORY").setDescription("DEST directory to write files to").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("alt-dest").withParameter(true, "ALT_DESTINATION_DIRECTORY").setDescription("Alternate destination for files that failed to match expression").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("err-dest").withParameter(true, "ERROR_DESTINATION_DIR").setDescription("Destination for malformed or exception generating files").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("tag-name").withParameter(true, "TAG_NAME").setDescription("TAG Name to Search for").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("tag-value").withParameter(true, "TAG_VALUE").setDescription("TAG value to Search for").setRequired(false));
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