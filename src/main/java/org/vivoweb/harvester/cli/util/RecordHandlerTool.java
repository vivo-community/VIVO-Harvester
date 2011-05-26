package org.vivoweb.harvester.cli.util;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.cli.util.args.ArgDef;
import org.vivoweb.harvester.cli.util.args.ArgList;
import org.vivoweb.harvester.cli.util.args.ArgParser;
import org.vivoweb.harvester.util.recordhandler.Record;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;
import org.vivoweb.harvester.util.recordhandler.RecordHandlerFactory;

/**
 * Inspect and modify RecordHandlers from the commandline
 * @author Christopher Haines
 */
public class RecordHandlerTool {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RecordHandlerTool.class);
	
	/**
	 * Run from commandline
	 * @param args the commandline args
	 * @throws IOException error parsing args
	 */
	public static void run(String... args) throws IOException {
		ArgList argList = getParser().parse(args);
		RecordHandler rh = RecordHandlerFactory.parseConfig(argList.get("i"), argList.getValueMap("I"));
		boolean list = argList.has("l");
		String recordId = argList.get("r");
		String value = argList.get("v");
		if(rh == null) {
			throw new IllegalArgumentException("Must provide a source record handler");
		}
		if(value != null && recordId == null) {
			throw new IllegalArgumentException("Cannot set a value when no record id specified");
		}
		if(list && recordId != null) {
			throw new IllegalArgumentException("Cannot list contents when specifying a record id");
		}
		if(list) {
			for(Record r : rh) {
				System.out.println(r.getID());
			}
		}
		if(recordId != null) {
			Record r = rh.getRecord(recordId);
			if(value != null) {
				r.setData(value, RecordHandler.class);
			} else {
				System.out.println(r.getData());
			}
		}
	}
	
	/**
	 * Get the OptionParser
	 * @return the OptionParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("RecordHandler");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").setDescription("CONFIG_FILE configuration filename for input recordhandler").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of input recordhanlder config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("recordId").withParameter(true, "RECORD_ID").setDescription("the record id to use").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("value").withParameter(true, "RECORD_VALUE").setDescription("set the value of RECORD_ID to be RECORD_VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("list").setDescription("list the ids contained in this recordhandler").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline args
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			run(args);
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
