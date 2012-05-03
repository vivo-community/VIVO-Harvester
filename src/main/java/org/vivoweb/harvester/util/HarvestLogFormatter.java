package org.vivoweb.harvester.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;

/**
 * @author Rene Ziede (rziede@ufl.edu)
 *
 */
public class HarvestLogFormatter {
	
	/**
	 * Logger for debug and trace output to console.
	 */
	protected static Logger log = LoggerFactory.getLogger(XMLGrep.class);

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
	 */
	public void execute() {
		
		//StringBuilder sb = new StringBuilder();
		
		//for (Map.Entry<String,String> entry : data.entrySet()) {

		//}
		
		//For each file in inputFiles
			// Create a text file containing a table in the format:
			// [DATE], [CATEGORY], [USER], [TYPE], ["S"], ["P"], ["O"]
			// Write file to output directory as a log.
		
		/* Notes from reformat.sh!
			
			#Get the logdate by spliting filename around 'dot' and getting second column
			ADDLOGDATE=`stat -c %y  $ADDFILENAME  | awk  '{ print $1"_" $2}' | awk -F. '{print $1}'`
			SUBLOGDATE=`stat -c %y  $SUBFILENAME  | awk  '{ print $1"_" $2}' | awk -F. '{print $1}'`
			
			ADDDATEFORLOGFILE=`stat -c %y  $ADDFILENAME  | awk  '{ print $1" " $2}'| awk -F. '{print $1}'`
			SUBDATEFORLOGFILE=`stat -c %y  $SUBFILENAME  | awk  '{ print $1" " $2}'| awk -F. '{print $1}'`
			
			#Since this is can only be harvester logs 
			CATEGORY="HARVEST"
			
			#Get username 

			USERNAME=`echo $HARVEST_NAME | awk -F- '{print $2}'`
			
			#reformat the vivo tripple log file
			
			#reformat the vivo tripple log file
			if [  -f $HARVESTER_INSTALL_DIR$HARVEST_NAME/data/vivo-ntriple-additions.xml ]; then
			
			#if the Ntrippleaddition file exist.
			# Awk desc
			## 1 Add username, logdate, category at the begning of each line and print the original line as comma seperated values
			## 2. Write the output to tocat6/logs/all_harvest_logs/$harvest_name/filename.xml
			
			awk -v username=$USERNAME -v date="$ADDDATEFORLOGFILE" -v category=$CATEGORY ' { print date",",category",",username ", ADD, \"" $1,"\",\""$2"\",\"",$3"\"" }' 
			$HARVESTER_INSTALL_DIR$HARVEST_NAME/data/vivo-ntriple-additions.xml >  $TOMCAT_INSTALL_DIR/logs/all_harvest_logs/$HARVEST_NAME/vivo-ntriple-additions.$ADDLOGDATE.xml
		*/
		
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
