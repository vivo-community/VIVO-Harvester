/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.cli.fetch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.cli.util.InitLog;
import org.vivoweb.harvester.cli.util.args.ArgDef;
import org.vivoweb.harvester.cli.util.args.ArgList;
import org.vivoweb.harvester.cli.util.args.ArgParser;
import org.vivoweb.harvester.util.recordhandler.RecordHandlerFactory;

/**
 * Fetches rdf data from a JDBC database placing the data in the supplied record handler.
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JDBCFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JDBCFetch.class);
	
	/**
	 * Split the values of a comma separated list mapping
	 * @param list the original mapping
	 * @return the split mapping
	 */
	private static Map<String, List<String>> splitCommaList(Map<String, String> list) {
		Map<String, List<String>> splitList = new HashMap<String, List<String>>();
		for(String tableName : list.keySet()) {
			splitList.put(tableName, Arrays.asList(list.get(tableName).split("\\s?,\\s?")));
		}
		return splitList;
	}
	
	/**
	 * Split the values of comma separated ~ maps mapping
	 * @param list the original mapping
	 * @return the split mappings
	 */
	private static Map<String, Map<String, String>> splitTildeMap(Map<String, String> list) {
		Map<String, List<String>> splitList = splitCommaList(list);
		Map<String, Map<String, String>> splitMaps = new HashMap<String, Map<String, String>>();
		for(String tableName : splitList.keySet()) {
			if(!splitMaps.containsKey(tableName)) {
				splitMaps.put(tableName, new HashMap<String, String>());
			}
			for(String relLine : splitList.get(tableName)) {
				String[] relPair = relLine.split("\\s?~\\s?", 2);
				if(relPair.length != 2) {
					throw new IllegalArgumentException("Bad Relation Line: " + relLine);
				}
				splitMaps.get(tableName).put(relPair[0], relPair[1]);
			}
		}
		return splitMaps;
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("JDBCFetch");
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("driver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("connection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("username").withParameter(true, "USERNAME").setDescription("database username").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("password").withParameter(true, "PASSWORD").setDescription("database password").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tableName").withParameters(true, "TABLE_NAME").setDescription("a single database table name [have multiple -t for more table names]").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("validTableType").withParameters(true, "TABLE_TYPE").setDescription("a single table type (TABLE, VIEW, etc) [have multiple -v for more table types]").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('Q').setLongOpt("query").withParameterValueMap("TABLE_NAME", "SQL_QUERY").setDescription("use SQL_QUERY to select from TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("id").withParameterValueMap("TABLE_NAME", "ID_FIELD_LIST").setDescription("use columns in ID_FIELD_LIST[comma separated] as identifier for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('F').setLongOpt("fields").withParameterValueMap("TABLE_NAME", "FIELD_LIST").setDescription("fetch columns in FIELD_LIST[comma separated] for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('R').setLongOpt("relations").withParameterValueMap("TABLE_NAME", "RELATION_PAIR_LIST").setDescription("fetch columns in RELATION_PAIR_LIST[comma separated] for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('W').setLongOpt("whereClause").withParameterValueMap("TABLE_NAME", "CLAUSE_LIST").setDescription("filter TABLE_NAME records based on conditions in CLAUSE_LIST[comma separated]").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('T').setLongOpt("tableFromClause").withParameterValueMap("TABLE_NAME", "TABLE_LIST").setDescription("add tables to use in from clauses for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("delimiterPrefix").withParameter(true, "DELIMITER").setDescription("Prefix each field in the query with this character").setDefaultValue("").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("delimiterSuffix").withParameter(true, "DELIMITER").setDescription("Suffix each field in the query with this character").setDefaultValue("").setRequired(false));
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
			log.info(getParser().getAppName() + ": Start");
			ArgList argList = getParser().parse(args);
			org.vivoweb.harvester.fetch.JDBCFetch jdbcFetch = new org.vivoweb.harvester.fetch.JDBCFetch(
				argList.get("d"),
				argList.get("c"),
				argList.get("u"),
				argList.get("p"), 
				RecordHandlerFactory.parseConfig(argList.get("o"), argList.getValueMap("O")), 
				argList.get("c")+"/", 
				argList.get("delimiterPrefix"), 
				argList.get("delimiterSuffix"), 
				new TreeSet<String>(argList.getAll("t")), 
				(argList.has("T")?argList.getValueMap("T"):null), 
				(argList.has("F")?splitCommaList(argList.getValueMap("F")):null), 
				(argList.has("I")?splitCommaList(argList.getValueMap("I")):null), 
				(argList.has("W")?splitCommaList(argList.getValueMap("W")):null), 
				(argList.has("R")?splitTildeMap(argList.getValueMap("R")):null), 
				(argList.has("Q")?argList.getValueMap("Q"):null),
				argList.getAll("v").toArray(new String[]{})
			);
			jdbcFetch.execute();
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
