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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
		if(this.db2 != null) {
			log.info("Preparing Output Database");
			Map<Integer,String> dbTypes = new HashMap<Integer, String>();
			Connection db2conn = this.db2.getConnection();
			ResultSet dbTypeInfo = db2conn.getMetaData().getTypeInfo();
			while(dbTypeInfo.next()) {
				Integer typeCode = Integer.valueOf(dbTypeInfo.getInt("DATA_TYPE"));
				String typeName = dbTypeInfo.getString("TYPE_NAME");
				if(!dbTypes.containsKey(typeCode)) {
					dbTypes.put(typeCode, typeName);
				}
			}
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
				log.trace("Create Table SQL Query:\n"+createTableSB);
				this.db2.getConnection().createStatement().executeUpdate(createTableSB.toString());
			}
			log.info("Dumping Dataset To Output");
			DatabaseOperation.INSERT.execute(this.db2, data);
			log.info("Dataset Output Complete");
		}
		if(this.outFile != null) {
			FlatDtdDataSet.write(data, this.outFile);
		}
	}
}
