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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
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
	 * use this database state file as the input database
	 */
	private InputStream inFile;
	/**
	 * output the state of the input database in this file
	 */
	private OutputStream outFile;
	
	/**
	 * Constructor
	 * @param inputConn database to input from
	 * @param inputFile use this database state file as the input database
	 * @param outputConn database to output to
	 * @param outputFile output the state of the input database in this file
	 * @param tableNames list of tables to export (null exports all)
	 * @param tableTypes list of table types to export (null exports 'TABLE' type only)
	 * @param dbUnitFeatures map of DBUnit features to boolean strings "true"/"false"
	 * @throws IOException error resolving file
	 * @throws DatabaseUnitException error connecting to database
	 */
	public DatabaseClone(Connection inputConn, String inputFile, Connection outputConn, String outputFile, String[] tableNames, String[] tableTypes, Map<String, String> dbUnitFeatures) throws IOException, DatabaseUnitException {
		if(inputConn != null) {
			this.db1 = new DatabaseConnection(inputConn);
		} else {
			this.db1 = null;
		}
		this.inFile = FileAide.getInputStream(inputFile);
		if(outputConn != null) {
			this.db2 = new DatabaseConnection(outputConn);
		} else {
			this.db2 = null;
		}
		this.outFile = FileAide.getOutputStream(outputFile, true);
		this.tables = tableNames;
		this.tableTypes = tableTypes;
		this.dbUnitFeatures = dbUnitFeatures;
		// Add value check info
		if((this.inFile == null) ^ (this.db1 != null)) {
			throw new IllegalArgumentException("Must provide one of input database or input file");
		}
		if((this.outFile == null && this.db2 == null)) {
			throw new IllegalArgumentException("Must provide an output database and/or output file");
		}
		if(this.inFile != null && this.outFile != null) {
			if(this.db2 == null) {
				throw new IllegalArgumentException("This tool should not be used to copy a database state file, please provide an output database");
			}
			log.warn("This tool should not be used to copy a database state file, ignoring outputFile");
			this.outFile = null;
		}
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error connecting to database
	 * @throws DatabaseUnitException error connecting to database
	 */
	public DatabaseClone(String[] args) throws IOException, ClassNotFoundException, SQLException, DatabaseUnitException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList option set of parsed args
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error connecting to database
	 * @throws IOException error resolving file
	 * @throws DatabaseUnitException error connecting to database
	 */
	private DatabaseClone(ArgList argList) throws ClassNotFoundException, SQLException, IOException, DatabaseUnitException {
		this(
			initDBConn("input", argList.get("inputDriver"), argList.get("inputConnection"), argList.get("inputUsername"), argList.get("inputPassword")),
			argList.get("inputFile"),
			initDBConn("output", argList.get("outputDriver"), argList.get("outputConnection"), argList.get("outputUsername"), argList.get("outputPassword")),
			argList.get("outputFile"),
			argList.getAll("tableName").toArray(new String[]{}),
			argList.getAll("validTableType").toArray(new String[]{}),
			argList.getValueMap("DBUnitFeature")
		);
	}
	
	/**
	 * Initialize a Database Connection
	 * @param dbname the name for this database
	 * @param driver the jdbc driver
	 * @param connLine the jdbc connection line
	 * @param user the username
	 * @param pass the password
	 * @return the database connection
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error connecting to database
	 */
	private static Connection initDBConn(String dbname, String driver, String connLine, String user, String pass) throws ClassNotFoundException, SQLException {
		if(driver == null) {
			log.debug("No "+dbname+"Driver provided, not using "+dbname+" database");
			return null;
		}
		if(connLine == null) {
			log.debug("No "+dbname+"Connection provided, not using "+dbname+" database");
			return null;
		}
		if(user == null) {
			log.debug("No "+dbname+"Username provided, not using "+dbname+" database");
			return null;
		}
		if(pass == null) {
			log.debug("No "+dbname+"Password provided, not using "+dbname+" database");
			return null;
		}
		Class.forName(driver);
		return DriverManager.getConnection(connLine, user, pass);
	}
	
	/**
	 * Clone a database
	 * @throws SQLException error connecting
	 * @throws DatabaseUnitException error
	 * @throws IOException error resolving connections
	 */
	public void execute() throws SQLException, DatabaseUnitException, IOException {
		if(this.tableTypes == null || this.tableTypes.length == 0) {
			this.tableTypes = new String[]{"TABLE"};
		}
		IDataSet data = getDataSet();
		if(this.db2 != null) {
			log.info("Preparing Output Database");
			Connection db1conn = this.db1.getConnection();
			Connection db2conn = this.db2.getConnection();
			Map<Integer,Map<String, String>> inputDbTypes = getDbTypes(db1conn, "input");
			Map<Integer,Map<String, String>> outputDbTypes = getDbTypes(db2conn, "output");
			ResultSet tableData = db2conn.getMetaData().getTables(db2conn.getCatalog(), null, "%", this.tableTypes);
			while(tableData.next()) {
				String db2tableName = tableData.getString("TABLE_NAME");
				for(String db1table : data.getTableNames()) {
					if(db1table.trim().equalsIgnoreCase(db2tableName.trim())) {
						log.debug("Droping table '"+db2tableName+"' from output database");
						String sql = "DROP TABLE "+db2tableName;
						log.trace("Drop Table SQL Query:\n"+sql);
						db2conn.createStatement().executeUpdate(sql);
					}
				}
			}
			for(String table : data.getTableNames()) {
				// get record set
				log.debug("Creating table '"+table+"' in output database");
				ResultSet columnRS = db1conn.getMetaData().getColumns(null, null, table, null);
				int count = 0;
				StringBuilder createTableSB = new StringBuilder();
				createTableSB.append("CREATE TABLE "+table+" (");
				while(columnRS.next()) {
					if(columnRS.getString("TABLE_NAME").equals(table)) {
						String colName = columnRS.getString("COLUMN_NAME");
						log.debug("Getting column information for '"+colName+"'");
						Integer typeCode = Integer.valueOf(columnRS.getInt("DATA_TYPE"));
						int size = columnRS.getInt("COLUMN_SIZE");
						if(!outputDbTypes.containsKey(typeCode)) {
							if(typeCode.intValue() == Types.BIT) {
								typeCode = Integer.valueOf(Types.BOOLEAN);
								size = 0;
							} else {
								if(inputDbTypes.containsKey(typeCode)) {
									log.warn("Output database does not support datatype '"+inputDbTypes.get(typeCode).get("TYPE_NAME")+"': using VARCHAR");
								} else {
									log.error("Unknown datatype code '"+typeCode+"': using VARCHAR");
								}
								typeCode = Integer.valueOf(12);
							}
						} else {
							log.trace("typeCode: "+typeCode);
						}
						Map<String, String> map = outputDbTypes.get(typeCode);
						String typeName = map.get("TYPE_NAME");
						String params = map.get("CREATE_PARAMS");
						if(StringUtils.isBlank(params)) {
							params = map.get("PARAMS");
						}
						boolean needParam = (StringUtils.isNotBlank(params) && (size != 0));
						log.trace("column '"+colName+"': "+typeCode+" => '"+typeName+((needParam)?"("+size+")":"")+"'");
						if(count != 0) {
							createTableSB.append(',');
						}
						createTableSB.append("\n  ");
						createTableSB.append(colName);
						createTableSB.append(" ");
						createTableSB.append(typeName);
						if(needParam) {
							createTableSB.append("(");
							createTableSB.append(size);
							createTableSB.append(")");
						}
						count++;
					}
				}
				createTableSB.append("\n)");
				log.trace("Create Table SQL Query:\n"+createTableSB);
				db2conn.createStatement().executeUpdate(createTableSB.toString());
			}
			log.info("Dumping Dataset To Output");
			DatabaseOperation.INSERT.execute(this.db2, data);
			log.info("Dataset Output Complete");
		}
		if(this.outFile != null) {
			FlatDtdDataSet.write(data, this.outFile);
		}
	}
	
	/**
	 * Get the dataset
	 * @return the dataset
	 * @throws IOException error writting to flat file
	 * @throws DataSetException error building dataset
	 * @throws SQLException error communicating with database
	 */
	private IDataSet getDataSet() throws DataSetException, IOException, SQLException {
		IDataSet data;
		if(this.db1 != null) {
			DatabaseConfig config = this.db1.getConfig();
			config.setProperty("http://www.dbunit.org/properties/tableType", this.tableTypes);
			if(this.dbUnitFeatures != null) {
				for(String feature : this.dbUnitFeatures.keySet()) {
					Boolean b = Boolean.valueOf(this.dbUnitFeatures.get(feature));
					log.debug("Setting '"+feature+"' to "+b);
					config.setProperty(feature, b);
				}
			}
			if(this.tables != null && this.tables.length > 0) {
				// partial database export
				log.info("Constructing Dataset Based on Given Tables");
			} else {
				// full database export
				log.info("Constructing Full Database Dataset");
				Set<String> tableSet = new HashSet<String>();
				ResultSet tableRS = this.db1.getConnection().getMetaData().getTables(null, null, null, this.tableTypes);
				while(tableRS.next()) {
					tableSet.add(tableRS.getString("TABLE_NAME"));
				}
				this.tables = tableSet.toArray(new String[]{});
			}
			QueryDataSet partialDataSet = new QueryDataSet(this.db1);
			for(String table : this.tables) {
				log.debug("Adding table '"+table+"' to dataset");
				String sql = "SELECT * FROM "+table;
				log.trace("Query for table '"+table+"':\n"+sql);
				partialDataSet.addTable(table, sql);
			}
			data = partialDataSet;
		} else if(this.inFile != null) {
			if(this.dbUnitFeatures != null) {
				for(String feature : this.dbUnitFeatures.keySet()) {
					log.warn("feature '"+feature+"' not supported for input files");
				}
			}
			data = new FlatDtdDataSet(this.inFile);
		} else {
			throw new IllegalStateException("inputFile or input database should be initialized!");
		}
		return data;
	}

	/**
	 * Get the database type information for this connection
	 * @param db the database connection
	 * @param dbName the name for this database
	 * @return the type information
	 * @throws SQLException error getting information
	 */
	private Map<Integer, Map<String, String>> getDbTypes(Connection db, String dbName) throws SQLException {
		Map<Integer,Map<String, String>> dbTypes = new HashMap<Integer, Map<String, String>>();
		ResultSet dbTypeInfo = db.getMetaData().getTypeInfo();
		ResultSetMetaData rsmd = dbTypeInfo.getMetaData();
		log.debug("Building type code mappings for "+dbName+" database");
		while(dbTypeInfo.next()) {
			Integer typeCode = Integer.valueOf(dbTypeInfo.getInt("DATA_TYPE"));
//			String typeName = dbTypeInfo.getString("TYPE_NAME");
			Map<String, String> map = null;
			if(!dbTypes.containsKey(typeCode)) {
				log.trace("Adding mapping information for typecode "+typeCode+":");
				map = new HashMap<String, String>();
			} else {
				log.trace("Already contained mapping information for typecode: "+typeCode);
			}
			for(int x = 1; x <= rsmd.getColumnCount(); x++) {
				String colVal = dbTypeInfo.getString(x);
				String colName = rsmd.getColumnName(x);
				log.trace("'"+colName+"' => "+(colVal == null?"":"'")+colVal+(colVal == null?"":"'"));
				if(map != null) {
					map.put(colName, colVal);
				}
			}
			if(map != null) {
				dbTypes.put(typeCode, map);
			}
		}
		return dbTypes;
	}

	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("DatabaseClone");
		parser.addArgument(new ArgDef().setLongOpt("inputDriver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class for input database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("inputConnection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string for input database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("inputUsername").withParameter(true, "USERNAME").setDescription("database username for input database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("inputPassword").withParameter(true, "PASSWORD").setDescription("database password for input database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("inputFile").withParameter(true, "FILE_PATH").setDescription("use this database state file as the input database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("outputDriver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class for output database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("outputConnection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string for output database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("outputUsername").withParameter(true, "USERNAME").setDescription("database username for output database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("outputPassword").withParameter(true, "PASSWORD").setDescription("database password for output database").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("outputFile").withParameter(true, "FILE_PATH").setDescription("output the state of the input database in this file").setRequired(false));
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
			new DatabaseClone(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error("Error in execution", e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
