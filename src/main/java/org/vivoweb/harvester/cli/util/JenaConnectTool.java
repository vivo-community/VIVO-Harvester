package org.vivoweb.harvester.cli.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.cli.util.args.ArgDef;
import org.vivoweb.harvester.cli.util.args.ArgList;
import org.vivoweb.harvester.cli.util.args.ArgParser;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.JenaConnectFactory;

/**
 * Run SPARQL Queries against your Jena Models from the commandline
 * @author Christopher Haines
 */
public class JenaConnectTool {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JenaConnectTool.class);
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("JenaConnect");
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jena").withParameter(true, "CONFIG_FILE").setDescription("config file for jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('J').setLongOpt("jenaOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('q').setLongOpt("query").withParameter(true, "SPARQL_QUERY").setDescription("sparql query to execute").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('Q').setLongOpt("queryResultFormat").withParameter(true, "RESULT_FORMAT").setDescription("the format to return the results in ('RS_RDF',etc for select queries / 'RDF/XML',etc for construct/describe queries)").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dataset").setDescription("execute query against dataset rather than model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("truncate").setDescription("empty the jena model").setRequired(false));
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
			ArgList argList = getParser().parse(args);
			JenaConnect jc = JenaConnectFactory.parseConfig(argList.get("j"), argList.getValueMap("J"));
			if(jc == null) {
				throw new IllegalArgumentException("Must specify a jena model");
			}
			if(argList.has("t")) {
				if(argList.has("q") || argList.has("Q")) {
					throw new IllegalArgumentException("Cannot Execute Query and Truncate");
				}
				jc.truncate();
			} else if(argList.has("q")) {
				jc.executeQuery(argList.get("q"), argList.get("Q"), argList.has("d"));
			} else {
				throw new IllegalArgumentException("No Operation Specified");
			}
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.err.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			error = e;
		} finally {
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
