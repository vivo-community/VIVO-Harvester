/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.fetch;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.RecordHandler;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;

/**
 * Fetch from JDBC into RDF/XML
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JDBCFetch extends Task {
	
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(JDBCFetch.class);
	/**
	 * Record Handler to write records to
	 */
	private RecordHandler rh;
	/**
	 * Table information
	 */
	private Map<String,Map<String,String>> tables;
	/**
	 * Statement processor for the database
	 */
	private Statement cursor;
	/**
	 * Namespace for RDF made from this database
	 */
	private String uriNS;
	
	/**
	 * Constructor
	 * @param output RecordHandler to write records to 
	 * @param dbConn database connection to read from
	 * @param tableInfo information about tables to read
	 * @param uriNameSpace base string for uri generation
	 * @throws SQLException error connecting to database
	 */
	public JDBCFetch(RecordHandler output, Connection dbConn, Map<String,Map<String,String>> tableInfo, String uriNameSpace) throws SQLException {
		this.rh = output;
		this.tables = tableInfo;
		this.cursor = dbConn.createStatement();
		this.uriNS = uriNameSpace;
		for(String tableName : this.tables.keySet()) {
			checkTableExists(tableName);
			checkTableConfigured(tableName);
		}
	}

	public static JDBCFetch getInstance(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
		String repositoryConfig = getParam(params, "repositoryConfig", true);
		RecordHandler output = RecordHandler.parseConfig(repositoryConfig);
		output.setOverwriteDefault(true);
		String jdbcDriverClass = getParam(params, "jdbcDriverClass", true);
		try {
			Class.forName(jdbcDriverClass);
		} catch(ClassNotFoundException e) {
			throw new IOException(e.getMessage(),e);
		}
		String connType = getParam(params, "connType", true);
		String host = getParam(params, "host", true);
		String port = getParam(params, "port", true);
		String dbName = getParam(params, "dbName", true);
		String username = getParam(params, "username", true);
		String password = getParam(params, "password", true);
		String uriNameSpace = "http://"+connType+"."+host+"/"+dbName+"/";
		Connection dbConn;
		try {
			dbConn = DriverManager.getConnection("jdbc:"+connType+"://"+host+":"+port+"/"+dbName, username, password);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
		Map<String,Map<String,String>> tableInfo = new HashMap<String,Map<String,String>>();
		for(String tableData : params.keySet()) {
			String[] temp = tableData.split("\\.", 2);
			if(temp.length == 2) {
				if(!tableInfo.containsKey(temp[0])) {
					tableInfo.put(temp[0], new HashMap<String,String>());
				}
				tableInfo.get(temp[0]).put(temp[1], params.get(tableData));
			}
		}
		try {
			return new JDBCFetch(output,dbConn,tableInfo,uriNameSpace);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	/**
	 * Checks if the table exists
	 * @param tableName the name of the table to check for
	 * @throws SQLException the table does not exist
	 */
	private void checkTableExists(String tableName) throws SQLException {
		boolean a;
		try {
			// ANSI SQL way.  Works in PostgreSQL, MSSQL, MySQL
			this.cursor.execute("select case when exists((select * from information_schema.tables where table_name = '"+tableName+"')) then 1 else 0 end");
			a = this.cursor.getResultSet().getBoolean(1);
		} catch(SQLException e) {
			try {
				// Other RDBMS. Graceful degradation
				a = true;
				this.cursor.execute("select 1 from "+tableName+" where 1 = 0");
			} catch(SQLException e1) {
				a = false;
			}
		}
		if(!a) {
			throw new SQLException("Database Does Not Contain Table: "+tableName);
		}
	}
	
	/**
	 * Get the data field information for a table from the parameter list
	 * @param tableName the table to get the data field information for
	 * @return the data field list
	 */
	private String[] getDataFields(String tableName) {
		return this.tables.get(tableName).get("dataFieldList").split("\\s?,\\s?");
	}
	
	/**
	 * Get the relation field information for a table from the parameter list
	 * @param tableName the table to get the relation field information for
	 * @return the relation field mapping
	 */
	private Map<String,String> getRelationFields(String tableName) {
		Map<String,String> relations = new HashMap<String,String>();
		String relationList = this.tables.get(tableName).get("relationFieldList");
		if(relationList != null){
			for(String relation : relationList.split("\\s?,\\s?")) {
				String[] temp = relation.split("\\s?:\\s?", 2);
				if(temp.length != 2) {
					throw new IllegalArgumentException();
				}
				relations.put(temp[0], temp[1]);
			}
		}
		return relations;
	}
	
	/**
	 * Get the id field information for a table from the parameter list
	 * @param tableName the table to get the id field information for
	 * @return the id field name
	 */
	private String getIDField(String tableName) {
		return this.tables.get(tableName).get("idField");
	}
	
	/**
	 * Builds a select statement against the table using configured fields
	 * @param tableName the table to build the select statement for
	 * @return the select statement
	 */
	private String buildSelect(String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for(String dataField : getDataFields(tableName)) {
			sb.append(dataField);
			sb.append(", ");
		}
		for(String relField : getRelationFields(tableName).keySet()) {
			sb.append(relField);
			sb.append(", ");
		}
		sb.append(getIDField(tableName));
		sb.append(" FROM ");
		sb.append(tableName);
		return sb.toString();
	}
	
	/**
	 * Builds a table's record namespace
	 * @param tableName the table to build the namespace for
	 * @return the namespace
	 */
	private String buildTableRecordNS(String tableName) {
		return this.uriNS+tableName+"/";
	}
	
	/**
	 * Builds a table's field description namespace
	 * @param tableName the table to build the namespace for
	 * @return the namespace
	 */
	private String buildTableFieldNS(String tableName) {
		return this.uriNS+"fields/"+tableName+"/";
	}
	
	/**
	 * Checks if a table is properly configured
	 * @param tableName the name of the table to check
	 * @throws SQLException the table is incorrectly configured
	 */
	private void checkTableConfigured(String tableName) throws SQLException {
		try {
			this.cursor.execute(buildSelect(tableName));
		} catch(SQLException e) {
			throw new SQLException("Table '"+tableName+"' Is Not Structured Correctly",e);
		}
	}
	
	@Override
	public void executeTask() throws NumberFormatException {
		//For each Table
		for(String tableName : this.tables.keySet()) {
			StringBuilder sb = new StringBuilder();
			try {
				//For each Record
				for(ResultSet rs = this.cursor.executeQuery(buildSelect(tableName)); rs.next(); ) {
					String recID = "id-"+rs.getString(getIDField(tableName)).trim();
					log.trace("Creating RDF for "+tableName+": "+recID);
					//Build RDF BEGIN
					//Header info
					String tableNS = "db-"+tableName;
					sb = new StringBuilder();
					sb.append("<?xml version=\"1.0\"?>\n");
					sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
					sb.append("         xmlns:");
					sb.append(tableNS);
					sb.append("=\"");
					sb.append(buildTableFieldNS(tableName));
					sb.append("\"\n");
					sb.append("         xml:base=\"");
					sb.append(buildTableRecordNS(tableName));
					sb.append("\">\n");
					
					//Record info BEGIN
					sb.append("  <rdf:Description rdf:ID=\"");
					sb.append(recID);
					sb.append("\">\n");
					for(String dataField : getDataFields(tableName)) {
						//Field BEGIN
						String field = tableNS+":"+dataField;
						sb.append("    <");
						sb.append(field);
						sb.append(">");
						
						//insert field value
						sb.append(rs.getString(dataField).trim());
						
						//Field END
						sb.append("</");
						sb.append(field);
						sb.append(">\n");
					}
					Map<String,String> relations = getRelationFields(tableName);
					for(String relationField : relations.keySet()) {
						//Field BEGIN
						sb.append("    <");
						sb.append(tableNS);
						sb.append(":");
						sb.append(relationField);
						sb.append(" rdf:resource=\"");
						sb.append(this.buildTableRecordNS(relations.get(relationField)));
						
						//insert field value
						sb.append("id-"+rs.getString(relationField).trim());
						
						//Field END
						sb.append("\"/>\n");
					}
					//Record info END
					sb.append("  </rdf:Description>\n");
					
					//Footer info
					sb.append("</rdf:RDF>");
					//Build RDF END
					
					//Write RDF to RecordHandler
//					System.out.println(sb.toString());
					log.trace("Adding record for "+tableName+": "+recID);
					this.rh.addRecord(tableName+"_"+recID,sb.toString());
				}
			} catch(SQLException e) {
				log.error(e.getMessage(),e);
				log.debug(sb.toString());
				break;
			} catch(IOException e) {
				log.error(e.getMessage(),e);
				break;
			}
		}
	}
	
}
