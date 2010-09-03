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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.HtmlEntities;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.xml.sax.SAXException;

/**
 * Fetches rdf data from a JDBC database
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JDBCFetch {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(JDBCFetch.class);
	/**
	 * Record Handler to write records to
	 */
	private RecordHandler rh;
	/**
	 * Statement processor for the database
	 */
	private Statement cursor;
	/**
	 * Mapping of tablename to idField name
	 */
	private Map<String,String> idFields = null;
	/**
	 * Mapping of tablename to mapping of fieldname to tablename
	 */
	private Map<String,Map<String,String>> relations = null;
	/**
	 * Mapping of tablename to list of datafields
	 */
	private Map<String,List<String>> dataFields = null;
	/**
	 * list of tablenames
	 */
	private List<String> tableNames = null;
	/**
	 * Namespace for RDF made from this database
	 */
	private String uriNS;
	
	/**
	 * Constructor
	 * @param dbConn connection to the database
	 * @param output RecordHandler to write data to
	 * @param uriNameSpace namespace base for rdf records
	 * @throws SQLException error talking with database
	 */
	public JDBCFetch(Connection dbConn, RecordHandler output, String uriNameSpace) throws SQLException {
		this.cursor = dbConn.createStatement();
		this.rh = output;
		this.uriNS = uriNameSpace;
	}
	
	/**
	 * Constructor
	 * @param opts option set of parsed args
	 * @throws IOException error creating task
	 */
	public JDBCFetch(ArgList opts) throws IOException {
		String jdbcDriverClass = opts.get("d");
		try {
			Class.forName(jdbcDriverClass);
		} catch(ClassNotFoundException e) {
			throw new IOException(e.getMessage(),e);
		}
		String connLine = opts.get("c");
		String username = opts.get("u");
		String password = opts.get("p");
		Connection dbConn;
		try {
			dbConn = DriverManager.getConnection(connLine, username, password);
			this.cursor = dbConn.createStatement();
			this.rh = RecordHandler.parseConfig(opts.get("o"));
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
//		this.uriNS = "http://"+host+"/"+connType+"/"+dbName+"/";
		this.uriNS = connLine+"/";
	}
	
	/**
	 * Get the data field information for a table from the database
	 * @param tableName the table to get the data field information for
	 * @return the data field list
	 * @throws SQLException error connecting to DB
	 */
	private List<String> getDataFields(String tableName) throws SQLException {
		if(this.dataFields == null) {
			this.dataFields = new HashMap<String,List<String>>();
		}
		if(!this.dataFields.containsKey(tableName)) {
			this.dataFields.put(tableName, new LinkedList<String>());
			ResultSet columnData = this.cursor.getConnection().getMetaData().getColumns(this.cursor.getConnection().getCatalog(), null, tableName, "%");
			while(columnData.next()) {
				String colName = columnData.getString("COLUMN_NAME");
				if(!getIDField(tableName).equalsIgnoreCase(colName) && !getRelationFields(tableName).containsKey(colName)) {
					this.dataFields.get(tableName).add(colName);
				}
			}
		}
		return this.dataFields.get(tableName);
	}
	
	/**
	 * Get the relation field information for a table from the database
	 * @param tableName the table to get the relation field information for
	 * @return the relation field mapping
	 * @throws SQLException error connecting to DB
	 */
	private Map<String,String> getRelationFields(String tableName) throws SQLException {
		if(this.relations == null) {
			this.relations = new HashMap<String,Map<String,String>>();
		}
		if(!this.relations.containsKey(tableName)) {
			this.relations.put(tableName, new HashMap<String,String>());
			ResultSet foreignKeys = this.cursor.getConnection().getMetaData().getImportedKeys(this.cursor.getConnection().getCatalog(), null, tableName);
			while(foreignKeys.next()) {
//				StringBuilder sb = new StringBuilder();
//				for(int x = 1; x <= foreignKeys.getMetaData().getColumnCount(); x++) {
//					sb.append(foreignKeys.getMetaData().getColumnName(x));
//					sb.append(" - ");
//					sb.append(foreignKeys.getString(x));
//					sb.append(" || ");
//				}
//				log.debug(sb.toString());
				this.relations.get(tableName).put(foreignKeys.getString("FKCOLUMN_NAME"), foreignKeys.getString("PKTABLE_NAME"));
			}
		}
		return this.relations.get(tableName);
	}
	
	/**
	 * Get the id field information for a table from the database
	 * @param tableName the table to get the id field information for
	 * @return the id field name
	 * @throws SQLException error connecting to DB
	 */
	private String getIDField(String tableName) throws SQLException {
		if(this.idFields == null) {
			this.idFields = new HashMap<String,String>();
		}
		if(!this.idFields.containsKey(tableName)) {
			ResultSet primaryKeys = this.cursor.getConnection().getMetaData().getPrimaryKeys(this.cursor.getConnection().getCatalog(), null, tableName);
			while(primaryKeys.next()) {
				String name = primaryKeys.getString("COLUMN_NAME");
				this.idFields.put(tableName, name);
			}
		}
		return this.idFields.get(tableName);
	}
	
	/**
	 * Gets the tablenames in database
	 * @return list of tablenames
	 * @throws SQLException error connecting to DB
	 */
	private List<String> getTableNames() throws SQLException {
		if(this.tableNames == null) {
			this.tableNames = new LinkedList<String>();
			String[] tableTypes = {"TABLE"};
			ResultSet tableData = this.cursor.getConnection().getMetaData().getTables(this.cursor.getConnection().getCatalog(), null, "%", tableTypes);
			while(tableData.next()) {
				this.tableNames.add(tableData.getString("TABLE_NAME"));
			}
		}
		return this.tableNames;
	}
	
	/**
	 * Builds a select statement against the table using configured fields
	 * @param tableName the table to build the select statement for
	 * @return the select statement
	 * @throws SQLException error connecting to db
	 */
	private String buildSelect(String tableName) throws SQLException {
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
		return this.uriNS+tableName;
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
	 * Executes the task
	 */
	public void execute() {
		log.info("Fetch: Start");
		//For each Table
		try {
			for(String tableName : this.getTableNames()) {
				StringBuilder sb = new StringBuilder();
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
						sb.append(HtmlEntities.htmlEncode(rs.getString(dataField).trim()));
						
						//Field END
						sb.append("</");
						sb.append(field);
						sb.append(">\n");
					}
					for(String relationField : getRelationFields(tableName).keySet()) {
						//Field BEGIN
						sb.append("    <");
						sb.append(tableNS);
						sb.append(":");
						sb.append(relationField);
						sb.append(" rdf:resource=\"");
						sb.append(this.buildTableRecordNS(getRelationFields(tableName).get(relationField)));
						
						//insert field value
						sb.append("#id-"+rs.getString(relationField).trim());
						
						//Field END
						sb.append("\"/>\n");
					}
					//Record info END
					sb.append("  </rdf:Description>\n");
					
					//Footer info
					sb.append("</rdf:RDF>");
					//Build RDF END
					
					//Write RDF to RecordHandler
					log.trace("Adding record for "+tableName+": "+recID);
					this.rh.addRecord(tableName+"_"+recID,sb.toString(), this.getClass());
				}
			}
		} catch(SQLException e) {
			log.error(e.getMessage(),e);
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		}
		log.info("Fetch: End");
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
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new JDBCFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}
