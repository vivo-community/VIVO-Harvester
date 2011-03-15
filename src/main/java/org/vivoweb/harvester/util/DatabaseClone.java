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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
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
	private FileObject inFile;
	/**
	 * output the state of the input database in this file
	 */
	private FileObject outFile;
	
	/**
	 * Constructor
	 * @param inputConn database to input from
	 * @param inputFile use this database state file as the input database
	 * @param outputConn database to output to
	 * @param outputFile output the state of the input database in this file
	 * @param tableNames list of tables to export (null exports all)
	 * @param tableTypes list of table types to export (null exports 'TABLE' type only)
	 * @param dbUnitFeatures map of DBUnit features to boolean strings "true"/"false"
	 * @throws FileSystemException error resolving file
	 * @throws DatabaseUnitException error connecting to database
	 */
	public DatabaseClone(Connection inputConn, String inputFile, Connection outputConn, String outputFile, String[] tableNames, String[] tableTypes, Map<String, String> dbUnitFeatures) throws FileSystemException, DatabaseUnitException {
		if(inputConn != null) {
			this.db1 = new DatabaseConnection(inputConn);
		} else {
			this.db1 = null;
		}
		if(inputFile != null) {
			this.inFile = VFS.getManager().resolveFile(new File("."), inputFile);
		} else {
			this.inFile = null;
		}
		if(outputConn != null) {
			this.db2 = new DatabaseConnection(outputConn);
		} else {
			this.db2 = null;
		}
		if(outputFile != null) {
			this.outFile = VFS.getManager().resolveFile(new File("."), outputFile);
		} else {
			this.outFile = null;
		}
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
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argList option set of parsed args
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error connecting to database
	 * @throws FileSystemException error resolving file
	 * @throws DatabaseUnitException error connecting to database
	 */
	public DatabaseClone(ArgList argList) throws ClassNotFoundException, SQLException, FileSystemException, DatabaseUnitException {
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
		IDataSet data;
		if(this.db1 != null) {
			DatabaseConfig config = this.db1.getConfig();
			config.setProperty("http://www.dbunit.org/properties/tableType", this.tableTypes);
			for(String feature : this.dbUnitFeatures.keySet()) {
				Boolean b = Boolean.valueOf(this.dbUnitFeatures.get(feature));
				log.debug("Setting '"+feature+"' to "+b);
				config.setProperty(feature, b);
			}
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
		} else if(this.inFile != null) {
			for(String feature : this.dbUnitFeatures.keySet()) {
				log.debug("feature '"+feature+"' not supported for input files");
			}
			data = new FlatDtdDataSet(this.inFile.getContent().getInputStream());
		} else {
			throw new IllegalStateException("inputFile or input database should be initialized!");
		}
		if(this.db2 != null) {
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
						int size = columnRS.getInt("COLUMN_SIZE");
						if(size != 0) {
							createTableSB.append("(");
							createTableSB.append(size);
							createTableSB.append(")");
						}
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
		if(this.outFile != null) {
			FlatDtdDataSet.write(data, this.outFile.getContent().getOutputStream(true));
		}
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
