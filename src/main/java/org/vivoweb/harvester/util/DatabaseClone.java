package org.vivoweb.harvester.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.dbunit.DatabaseUnitException;
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
	 * Constructor
	 * @param inputConn database to input from
	 * @param outputConn database to output to
	 * @param tableNames list of tables to export (null exports all)
	 */
	public DatabaseClone(Connection inputConn, Connection outputConn, String[] tableNames) {
		this.db1 = new DatabaseConnection(inputConn);
		this.db2 = new DatabaseConnection(outputConn);
		this.tables = tableNames;
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
	}
	
	/**
	 * Clone a database
	 * @throws SQLException error connecting
	 * @throws DatabaseUnitException error
	 */
	public void execute() throws SQLException, DatabaseUnitException {
		IDataSet data;
		if(this.tables != null) {
			// partial database export
			QueryDataSet partialDataSet = new QueryDataSet(this.db1);
			for(String table : this.tables) {
				partialDataSet.addTable(table);
			}
			data = partialDataSet;
		} else {
			// full database export
			data = this.db1.createDataSet();
		}
		DatabaseOperation.CLEAN_INSERT.execute(this.db2, data);
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
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tableName").withParameters(true, "TABLE_NAME").setDescription("a single database table name [have multiple -t for more table names]").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(DatabaseClone.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new DatabaseClone(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
}
