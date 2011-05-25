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
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.vfs.FileSystemException;
import org.h2.tools.Csv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class takes the data from a csv file and places it into a database
 * @author James Pence jrpence@ufl.edu
 */
public class CSVtoJDBC {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(CSVtoJDBC.class);
	/**
	 * CSV to read from
	 */
	private InputStream csvStream;
	/**
	 * DBconnection into which to output
	 */
	private Connection output;
	/**
	 * Table name into which to output
	 */
	private String tableName;
	/**
	 * Field names into which to output
	 */
	private List<String> fieldNames;
	
	/**
	 * Library style initialyzer 
	 * @param input CSV inputStream to read from
	 * @param output The database connection for the output
	 * @param tableName table name into which to output
	 */
	public CSVtoJDBC(InputStream input, Connection output, String tableName) {
		this.csvStream = input;
		this.output = output;
		this.tableName = tableName;
		this.fieldNames = new ArrayList<String>();
	}
	
	/**
	 * Library style Constructor
	 * @param filename CSV to read from
	 * @param output The database connection for the output
	 * @param tableName table name into which to output
	 * @throws IOException error establishing connection to file
	 */
	public CSVtoJDBC(String filename, Connection output, String tableName) throws IOException {
		this(FileAide.getInputStream(filename), output, tableName);
	}
	
	/**
	 * Library style Constructor
	 * @param filename CSV to read from
	 * @param jdbcDriverClass jdbc driver class
	 * @param connLine the jdbc connection line
	 * @param username username with which to connect
	 * @param password password with which to connect
	 * @param tableName table name into which to output
	 * @throws IOException error establishing connection to database or file
	 */
	public CSVtoJDBC(String filename, String jdbcDriverClass, String connLine, String username, String password, String tableName) throws IOException {
		this(filename,getConnection(jdbcDriverClass, connLine, username, password),tableName);
	}
	
	/**
	 * Library style Constructor
	 * @param input CSV inputStream to read from
	 * @param jdbcDriverClass jdbc driver class
	 * @param connLine the jdbc connection line
	 * @param username username with which to connect
	 * @param password password with which to connect
	 * @param tableName table name into which to output
	 * @throws IOException error establishing connection to database or file
	 */
	public CSVtoJDBC(InputStream input, String jdbcDriverClass, String connLine, String username, String password, String tableName) throws IOException {
		this(input,getConnection(jdbcDriverClass, connLine, username, password),tableName);
	}
	
	/**
	 * Get a connection
	 * @param jdbcDriverClass the driver
	 * @param connLine the connection string
	 * @param username the username
	 * @param password the password
	 * @return the connection
	 * @throws IOException error connecting
	 */
	private static Connection getConnection(String jdbcDriverClass, String connLine, String username, String password) throws IOException {
		try {
			Class.forName(jdbcDriverClass);
			return DriverManager.getConnection(connLine, username, password);
		} catch(ClassNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Move CSV data into a recordHandler
	 * @throws IOException error reading from database or file
	 */
	public void execute() throws IOException {
		try {
			ResultSet rs = Csv.getInstance().read(new InputStreamReader(this.csvStream), null);
			ResultSetMetaData meta = rs.getMetaData();
			Statement cursor = this.output.createStatement();
			int rowID = 0;
			StringBuilder createTable = new StringBuilder("CREATE TABLE ");
			createTable.append(this.tableName);
			createTable.append("( ROWID int NOT NULL, ");
			this.fieldNames.add("ROWID");
			StringBuilder columnNames = new StringBuilder("( ROWID, ");
			for(int i = 0; i < meta.getColumnCount(); i++) {
				String colLbl = meta.getColumnLabel(i + 1);
				createTable.append("\n");
				createTable.append( colLbl);
				this.fieldNames.add( colLbl);
				createTable.append((i == (meta.getColumnCount() - 1)) ? " TEXT )" : " TEXT ,");
				
				columnNames.append(colLbl);
				columnNames.append((i == (meta.getColumnCount() - 1)) ? " )" : ", ");
			}
			log.debug("Create table command: \n" + createTable.toString());
			cursor.execute(createTable.toString());
			cursor.execute("ALTER TABLE "+this.tableName+" ADD PRIMARY KEY (ROWID)");
			while(rs.next()) {
				
				StringBuilder insertCommand = new StringBuilder("INSERT INTO ");
				insertCommand.append(this.tableName);
				insertCommand.append(" ");
				insertCommand.append(columnNames.toString());
				insertCommand.append("\nVALUES (");
				insertCommand.append(rowID);
				insertCommand.append(", '");
				for(int i = 0; i < meta.getColumnCount(); i++) {
					insertCommand.append(rs.getString(i + 1));
					insertCommand.append((i == (meta.getColumnCount() - 1)) ? "')" : "', '");
				}
				log.debug("Insert command: \n" + insertCommand.toString());
				cursor.executeUpdate(insertCommand.toString());
				rowID++;
			}
		} catch(FileSystemException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the list of fields from the recent CSV
	 * @return the list of fields generated from recent CSV
	 */
	public List<String> getFields(){
		return this.fieldNames;
	}
}
