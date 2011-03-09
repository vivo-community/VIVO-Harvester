/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;

/**
 * Clone a database from one jdbc connection to another
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class DatabaseClone {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(DatabaseClone.class);
	/**
	 * database to export from
	 */
	private IDatabaseConnection db1;
	/**
	 * database to import to
	 */
	private IDatabaseConnection db2;
	/**
	 * list of tables to export (null exports all)
	 */
	private String[] tables;
	/**
	 * list of table types to export (null exports 'TABLE' type only)
	 */
	private String[] tableTypes;
	/**
	 * map of DBUnit features to boolean strings "true"/"false"
	 */
	private Map<String, String> dbUnitFeatures;
	
	/**
	 * Constructor
	 * @param inputConn database to input from
	 * @param outputConn database to output to
	 * @param tableNames list of tables to export (null exports all)
	 * @param tableTypes list of table types to export (null exports 'TABLE' type only)
	 * @param dbUnitFeatures map of DBUnit features to boolean strings "true"/"false"
	 */
	public DatabaseClone(Connection inputConn, Connection outputConn, String[] tableNames, String[] tableTypes, Map<String, String> dbUnitFeatures) {
		this.db1 = new DatabaseConnection(inputConn);
		this.db2 = new DatabaseConnection(outputConn);
		this.tables = tableNames;
		this.tableTypes = tableTypes;
		this.dbUnitFeatures = dbUnitFeatures;
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error connecting to database
	 */
	public DatabaseClone(String[] args) throws IOException, ClassNotFoundException, SQLException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argList option set of parsed args
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error connecting to database
	 */
	public DatabaseClone(ArgList argList) throws ClassNotFoundException, SQLException {
		Class.forName(argList.get("inputDriver"));
		this.db1 = new DatabaseConnection(DriverManager.getConnection(argList.get("inputConnection"), argList.get("inputUsername"), argList.get("inputPassword")));
		Class.forName(argList.get("outputDriver"));
		this.db2 = new DatabaseConnection(DriverManager.getConnection(argList.get("outputConnection"), argList.get("outputUsername"), argList.get("outputPassword")));
		this.tables = argList.getAll("tableName").toArray(new String[]{});
		this.tableTypes = argList.getAll("validTableType").toArray(new String[]{});
		this.dbUnitFeatures = argList.getValueMap("DBUnitFeature");
	}
	
	/**
	 * Clone a database
	 * @throws SQLException error connecting
	 * @throws DatabaseUnitException error
	 */
	public void execute() throws SQLException, DatabaseUnitException {
		if(this.tableTypes == null || this.tableTypes.length == 0) {
			this.tableTypes = new String[]{"TABLE"};
		}
		DatabaseConfig config = this.db1.getConfig();
		config.setProperty("http://www.dbunit.org/properties/tableType", this.tableTypes);
		for(String feature : this.dbUnitFeatures.keySet()) {
			boolean b = Boolean.parseBoolean(this.dbUnitFeatures.get(feature));
			log.debug("Setting '"+feature+"' to "+b);
			config.setFeature(feature, b);
		}
		IDataSet data;
		if(this.tables != null) {
			// partial database export
			log.info("Constructing Dataset Based on Given Tables");
			QueryDataSet partialDataSet = new QueryDataSet(this.db1);
			for(String table : this.tables) {
				log.info("Adding "+table+" to dataset");
				partialDataSet.addTable(table, "SELECT * FROM "+table);
			}
			data = partialDataSet;
		} else {
			// full database export
			log.info("Constructing Full Database Dataset");
			data = this.db1.createDataSet();
		}
		log.info("Preparing Output Database");
		Map<Integer,String> dbTypes = new HashMap<Integer, String>();
		ResultSet dbTypeInfo = this.db2.getConnection().getMetaData().getTypeInfo();
		while(dbTypeInfo.next()) {
			Integer typeCode = Integer.valueOf(dbTypeInfo.getInt("DATA_TYPE"));
			String typeName = dbTypeInfo.getString("TYPE_NAME");
			if(!dbTypes.containsKey(typeCode)) {
				dbTypes.put(typeCode, typeName);
			}
		}
		for(String table : data.getTableNames()) {
			log.debug("Droping table '"+table+"' from output database");
			this.db2.getConnection().createStatement().executeUpdate("DROP TABLE IF EXISTS "+table);
			// get record set
			log.debug("Creating table '"+table+"' in output database");
			ResultSet columnRS = this.db1.getConnection().getMetaData().getColumns(null, null, table, null);
			int count = 0;
			StringBuilder createTableSB = new StringBuilder();
			createTableSB.append("CREATE TABLE "+table+" (");
			while(columnRS.next()) {
				if(columnRS.getString("TABLE_NAME").equals(table)) {
					if(count != 0) {
						createTableSB.append(',');
					}
					createTableSB.append("\n  ");
					createTableSB.append(columnRS.getString("COLUMN_NAME"));
					createTableSB.append(" ");
					createTableSB.append(dbTypes.get(Integer.valueOf(columnRS.getInt("DATA_TYPE"))));
					count++;
				}
			}
			createTableSB.append("\n)");
			log.debug("Create Table SQL Query:\n"+createTableSB);
			this.db2.getConnection().createStatement().executeUpdate(createTableSB.toString());
		}
		log.debug("Dumping Dataset To Output");
		DatabaseOperation.INSERT.execute(this.db2, data);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("DatabaseClone");
		parser.addArgument(new ArgDef().setLongOpt("inputDriver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class for input database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("inputConnection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string for input database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("inputUsername").withParameter(true, "USERNAME").setDescription("database username for input database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("inputPassword").withParameter(true, "PASSWORD").setDescription("database password for input database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("outputDriver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class for output database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("outputConnection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string for output database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("outputUsername").withParameter(true, "USERNAME").setDescription("database username for output database").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("outputPassword").withParameter(true, "PASSWORD").setDescription("database password for output database").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tableName").withParameters(true, "TABLE_NAME").setDescription("a single database table name [have multiple -t flags, one for each table names]").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("validTableType").withParameters(true, "TABLE_TYPE").setDescription("a single table type ('TABLE', 'VIEW', etc) Defaults to just 'TABLE' [have multiple --validTableType flags, one for each table type]").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('D').setLongOpt("DBUnitFeature").withParameterValueMap("FEATURE", "VALUE").setDescription("Use VALUE for the DBUnit FEATURE (should be 'true'/'false')").setRequired(false));
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
			new DatabaseClone(new ArgList(getParser(), args)).execute();
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
