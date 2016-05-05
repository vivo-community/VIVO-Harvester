package org.vivoweb.harvester.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;

/**
 * @author Rene Ziede (rziede@ufl.edu)
 *
 */
public class HarvestLogFormatter {
	
	/**
	 * Logger for debug and trace output to console.
	 */
	protected static Logger log = LoggerFactory.getLogger(HarvestLogFormatter.class);

	/**
	 * Input file types and paths
	 */
	private Map<String, String> inputFiles;
	/**
	 * Log destination root directory
	 */
	private String destinationRootDir;
	
	/**
	 * The name of the harvest which is being logged.
	 */
	private String targetHarvestName;
	
	/**
	 * All entries processed by this tool are under the "Harvest" category.
	 */
	private final String category = "Harvest";
		
	/**
	 * Constructor
	 * @param inFiles input file paths keyed by type.
	 * @param destRootDir destination directory root.
	 * @param targetHarvest name of the targeted harvest.
	 */
	public HarvestLogFormatter( Map<String, String> inFiles, String destRootDir, String targetHarvest ) {
		this.inputFiles = inFiles;
		this.destinationRootDir = destRootDir;
		this.targetHarvestName = targetHarvest;
	}
	
	/**
	 * Constructor
	 * @param args Command Line Arguments
	 * @throws IOException Error creating task 
	 * @throws UsageException Requested usage message 
	 */
	private HarvestLogFormatter(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList arguments
	 */
	private HarvestLogFormatter(ArgList argList) {
		this( argList.getValueMap("i"),
			  argList.get("o"),
			  argList.get("t") );
	}
	
	/**
	 * Runs the HarvestLogFormatter
	 * @throws IOException FileAide
	 */
	public void execute() throws IOException {
			
		log.trace("Begin HarvestLogFormatter execute:");
	
		StringBuilder stringBuilder;
		String fileContents;
		Scanner fileScanner;
		Set<Map.Entry<String,String>> set = this.inputFiles.entrySet();
		
		// Objects and regex expression for parsing file's last-modified date.
		Date logDate = new Date();
		String dateString;
		SimpleDateFormat sdf = new SimpleDateFormat();
		String simpleDateFormatString = "yyyy-MM-dd-'T'HH:mm:ssZ"; 	// 2012-05-04-T17:15:23-04:00
		sdf.applyPattern(simpleDateFormatString);
		
		// For each key/value pair in inputFiles map.
		for (Map.Entry<String,String> me : set ) {
			
			log.trace(me.getKey() + " : " + me.getValue());
	
			stringBuilder = new StringBuilder();
			
			// Check for file existing.
			if( FileAide.exists(me.getValue()) ) {

				//Grab the file's date. BAD JUJU - going around VFS!
				File tempFile = new File(me.getValue());
				logDate = new Date(tempFile.lastModified());
				dateString = sdf.format(logDate);
				
				//Load the file's contents into a Scanner.
				fileContents = FileAide.getTextContent(me.getValue());
				fileScanner = new Scanner(fileContents);
				fileScanner.useDelimiter("\\s\\.|\\s");
				
				while( fileScanner.hasNext() )
				{
					// [DATE], [CATEGORY], [USER], [TYPE], ["S"], ["P"], ["O"]
					stringBuilder.append(dateString + ", ");
					stringBuilder.append(this.category + ", ");
					stringBuilder.append(this.targetHarvestName + ", ");
					stringBuilder.append(me.getKey() + ", ");
					
					if (fileScanner.hasNext()) stringBuilder.append(fileScanner.next() + ", ");
					if (fileScanner.hasNext()) stringBuilder.append(fileScanner.next() + ", ");
					if (fileScanner.hasNext()) stringBuilder.append(fileScanner.next() + "\n");
				}
				
				log.trace(stringBuilder.toString());
				
				// Write contents of stringBuilder to output log.
				//if (!FileAide.exists(this.destinationRootDir))
				//{
				//	FileAide.createFile(this.destinationRootDir);
				//}
				
				String outputPath = this.destinationRootDir + "vivo-triple-log-" + dateString + ".log";
				
				if (!FileAide.exists(outputPath))
				{
					//DEBUG
					log.trace("Creating log file at: " + outputPath);
					FileAide.setTextContent(outputPath, stringBuilder.toString(), true);
					
					//DEBUG
					//log.trace(FileAide.getTextContent(outputPath));
				}
				else
				{
					log.warn("Log file already exists with that name!");					
				}
				
			}
			else
			{
				new IOException("Invalid file path, file not found! " + me.getValue() );
			}
			
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("HarvestLogFormatter");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-files").withParameterValueMap("TYPE", "FILE_PATH").setDescription("Key/value of type and input file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output-dest").withParameter(true, "DEST_DIRECTORY").setDescription("Destination root directory to write output to").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("target-harvest").withParameter(true, "TARGET_HARVEST").setDescription("Name of the targeted harvest").setRequired(true));

		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		
		// TODO: May need to do more here, check other command line apps.		
		try	{
			new HarvestLogFormatter(args).execute();
		} 
		catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			System.out.println(getParser().getUsage());
			error = e;
		} 
		catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} 
		catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			error = e;
		} 
		finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
}
