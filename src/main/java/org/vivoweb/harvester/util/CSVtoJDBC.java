/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, James Pence, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, James Pence, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import org.h2.tools.Csv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;

/**
 * This Class takes the data from a csv file and places it into a database
 *
 */
public class CSVtoJDBC {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(CSVtoJDBC.class);
	/**
	 * CSV to export from
	 */
	private String csvFile;
	/**
	 * DBconnection to import to
	 */
	private Connection output;
	/**
	 * Table names to Import
	 */
	private String tableName;
	/**
	 * Library style Constructor
	 * @param filename CSV to input from
	 * @param output The database connection for the output
	 */
	public CSVtoJDBC(String filename, Connection output) {
		this.csvFile = filename;
		this.output = output;
	}
	/**
	 * Command line Constructor
	 * @param args command line arguments
	 * @throws IOException error creating task
	 * @throws ClassNotFoundException error loading driver
	 * @throws SQLException error with connection
	 */
	public CSVtoJDBC(String[] args) throws IOException, ClassNotFoundException, SQLException {
		this(new ArgList(getParser(), args));
	}

	/**
	 * ArgList Constructor
	 * @param argList option set of parsed args
	 * @throws IOException error with recordhandler
	 * @throws SQLException error with connection
	 */
	public CSVtoJDBC(ArgList argList) throws IOException, SQLException {
		this.csvFile = argList.get("inputFile");
		String jdbcDriverClass = argList.get("d");
		try {
			Class.forName(jdbcDriverClass);
		} catch(ClassNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		}
		
		// Setting the database connection parameters
		String connLine = argList.get("c");
		String username = argList.get("u");
		String password = argList.get("p");
		this.output = DriverManager.getConnection(connLine, username, password);
		this.tableName = argList.get("t");
	}
	
	/**
	 * move CSV data into a recordHandler
	 * @throws SQLException error connecting
	 * @throws IOException error outputting to recordhandler
	 */
	public void execute() throws SQLException, IOException {
		ResultSet rs = Csv.getInstance().read(this.csvFile, null, null);
        ResultSetMetaData meta = rs.getMetaData();
        Statement cursor = this.output.createStatement();
        int rowID = 0;
        StringBuilder createTable = new StringBuilder("CREATE TABLE ");
        createTable.append(this.tableName);
        createTable.append("( ROWID int, ");
        StringBuilder columnNames = new StringBuilder("( ROWID, ");
    	for(int i = 0; i < meta.getColumnCount(); i++){
    		createTable.append("\n");
    		createTable.append(meta.getColumnLabel(i + 1));
    		createTable.append((i==(meta.getColumnCount()-1) )?" TEXT )":" TEXT ,");
    		
    		columnNames.append(meta.getColumnLabel(i + 1));
    		columnNames.append((i==(meta.getColumnCount()-1) )?" )":", ");
		} 
    	log.info("Create table command: \n" + createTable.toString());
        cursor.execute(createTable.toString());
        while(rs.next()){
        	
        	StringBuilder insertCommand =  new StringBuilder("INSERT INTO ");
        	insertCommand.append(this.tableName);
        	insertCommand.append(" ");
        	insertCommand.append(columnNames.toString() );
        	insertCommand.append("\nVALUES (");
        	insertCommand.append(rowID);
        	insertCommand.append(", '");
        	for(int i = 0; i < meta.getColumnCount(); i++){
//				System.out.println(meta.getColumnLabel(i + 1) + ": " + rs.getString(i + 1));
        		insertCommand.append(rs.getString(i + 1));
        		insertCommand.append((i==(meta.getColumnCount()-1) )?"')":"', '");
			}       
    		log.info("Insert command: \n" + insertCommand.toString());	
			cursor.executeUpdate(insertCommand.toString());
			rowID++;
        }
        

	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("CSVtoJDBC");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputFile").withParameter(true, "FILENAME").setDescription("csv file to be read into the database").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("driver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class for output database").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("connection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string for output database").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("username").withParameter(true, "USERNAME").setDescription("database username for output database").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("password").withParameter(true, "PASSWORD").setDescription("database password for output database").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tableName").withParameters(true, "TABLE_NAME").setDescription("a single database table name").setRequired(true));
		return parser;
	}

	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String[] args) {
		InitLog.initLogger(CSVtoJDBC.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new CSVtoJDBC(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
}
