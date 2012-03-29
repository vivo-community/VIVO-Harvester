package org.vivoweb.harvester.util;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.translate.XSLTranslator;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * @author drspeedo
 *
 */
public class ImagePreservationDuringPrivacy {

	
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Logger log = LoggerFactory.getLogger(ImagePreservationDuringPrivacy.class);
	/**
	 * record handler for incoming records
	 */
	protected JenaConnect inStore;
	/**
	 * record handler for incoming records
	 */
	protected JenaConnect privateStore;
	/**
	 * record handler for vivo
	 */
	protected JenaConnect vivo;
	
	/**
	 * 
	 * @param inrh
	 * @param vivorh
	 */
	public ImagePreservationDuringPrivacy(JenaConnect inJC, JenaConnect privateJC, JenaConnect vivoJC) {
		this.inStore = inJC;
		this.privateStore = privateJC;
		this.vivo = vivoJC;
	}
	
	/**
	 * Constructor
	 * @param args commandline argument
	 * @throws IllegalArgumentException 
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private ImagePreservationDuringPrivacy(String[] args) throws IllegalArgumentException, IOException, UsageException {
		this(getParser().parse(args));
	}
	
	//TODO: Implement Execution
	/**
	 * 
	 */
	public void execute(){
		
		//union private model and inrecord set and query for ufid / old private flag / new privacy flag
		//new privacy flag comes from the privacy setting of the in record
		//old privacy flag comes from the existence of the UFID in the private model (true = exists)
		
		//loop through recordset
		
			//if old and new privacy flags are not equal begin process
		
				//if new private = true
					//query vivo for image information
					//create new record in private store with ufid, uri, and image information
		
				//if new private = false
					//query private store for image information
					//add image information to incoming model
					//remove record from private store
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	private ImagePreservationDuringPrivacy(ArgList argList) throws IOException {
		this(
			JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")), 
			JenaConnect.parseConfig(argList.get("v"), argList.getValueMap("V")), 
			JenaConnect.parseConfig(argList.get("p"), argList.getValueMap("P"))
			);
	}
	
	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ImagePreservationDuringPrivacy");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJena JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of inputJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoJena-config").withParameter(true, "CONFIG_FILE").setDescription("vivoJena JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivoJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("privateJena-config").withParameter(true, "CONFIG_FILE").setDescription("private entries data JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("privateOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of private entries jena model config using VALUE").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info("ImagePreservationDuringPrivacy: Start");
			new ImagePreservationDuringPrivacy(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info("ImagePreservationDuringPrivacy: End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
