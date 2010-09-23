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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.SpecialEntities;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.sparql.util.StringUtils;

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
	private Map<String,List<String>> idFields = null;
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
	 * list of conditions
	 */
	private Map<String,List<String>> whereClauses;
	/**
	 * mapping of extra tables for the from section
	 */
	private Map<String,String> fromClauses;
	/**
	 * Namespace for RDF made from this database
	 */
	private String uriNS;
	/**
	 * Prefix each field in query with this
	 */
	private String queryPre;
	/**
	 * Suffix each field in query with this
	 */
	private String querySuf;
	
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
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tableName").withParameters(true, "TABLE_NAME").setDescription("a single database table name [have multiple -t for more table names]").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("id").withParameterProperties("TABLE_NAME", "ID_FIELD_LIST").setDescription("use columns in ID_FIELD_LIST[comma separated] as identifier for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('F').setLongOpt("fields").withParameterProperties("TABLE_NAME", "FIELD_LIST").setDescription("fetch columns in FIELD_LIST[comma separated] for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('R').setLongOpt("relations").withParameterProperties("TABLE_NAME", "RELATION_PAIR_LIST").setDescription("fetch columns in RELATION_PAIR_LIST[comma separated] for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('W').setLongOpt("whereClause").withParameterProperties("TABLE_NAME", "CLAUSE_LIST").setDescription("filter TABLE_NAME records based on conditions in CLAUSE_LIST[comma separated]").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('T').setLongOpt("tableFromClause").withParameterProperties("TABLE_NAME", "TABLE_LIST").setDescription("add tables to use in from clasuse for TABLE_NAME").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("delimiterPrefix").withParameter(true, "DELIMITER").setDescription("Prefix each field in the query with this character").setDefaultValue("").setRequired(false));
		parser.addArgument(new ArgDef().setLongOpt("delimiterSuffix").withParameter(true, "DELIMITER").setDescription("Suffix each field in the query with this character").setDefaultValue("").setRequired(false));
		return parser;
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
		this.queryPre = opts.get("delimiterPrefix");
		this.querySuf = opts.get("delimiterSuffix");
		
		if(opts.has("t")) {
			this.tableNames = opts.getAll("t");
		}
		
		if(opts.has("T")) {
			if(!opts.has("t")) {
				throw new IllegalArgumentException("Cannot specify fromClauses without tableName");
			}
			this.fromClauses = new HashMap<String, String>();
			Properties froms = opts.getProperties("T");
			for(String tableName : froms.stringPropertyNames()) {
				this.fromClauses.put(tableName.trim(), froms.getProperty(tableName).trim());
			}
		}
		
		if(opts.has("F")) {
			if(!opts.has("t")) {
				throw new IllegalArgumentException("Cannot specify fields without tableName");
			}
			this.dataFields = new HashMap<String,List<String>>();
			Properties fields = opts.getProperties("F");
			for(String tableName : fields.stringPropertyNames()) {
				if(!this.dataFields.containsKey(tableName.trim())) {
					this.dataFields.put(tableName, new LinkedList<String>());
				}
				for(String fieldLine : fields.get(tableName.trim()).toString().split(",")) {
					this.dataFields.get(tableName).add(fieldLine.trim());
					log.debug("field: '"+fieldLine.trim()+"'");
				}
			}
		}
		
		if(opts.has("I")) {
			if(!opts.has("t")) {
				throw new IllegalArgumentException("Cannot specify id without tableName");
			}
			this.idFields = new HashMap<String,List<String>>();
			Properties ids = opts.getProperties("I");
			for(Object table : ids.keySet()) {
				String tableName = table.toString().trim();
				this.idFields.put(tableName, Arrays.asList(ids.get(tableName).toString().trim().split(",")));
			}
		}
		
		if (opts.has("W")) {
			if(!opts.has("t")) {
				throw new IllegalArgumentException("Cannot specify whereClauses without tableName");
			}
			this.whereClauses = new HashMap<String,List<String>>();
			Properties wheres = opts.getProperties("W");
			for(Object table : wheres.keySet()) {
				String tableName = table.toString().trim();
				if(!this.whereClauses.containsKey(tableName)) {
					this.whereClauses.put(tableName, new LinkedList<String>());
				}
				for(String whereLine : wheres.get(table).toString().split(",")) {
					this.whereClauses.get(tableName).add(whereLine.trim());
				}
			}
		}
		
		if (opts.has("R")) {
			if(!opts.has("t")) {
				throw new IllegalArgumentException("Cannot specify relations without tableName");
			}
			this.relations = new HashMap<String,Map<String,String>>();
			Properties rels = opts.getProperties("R");
			for(Object table : rels.keySet()) {
				String tableName = table.toString().trim();
				if(!this.relations.containsKey(tableName)) {
					this.relations.put(tableName, new HashMap<String, String>());
				}
				for(String relLine : rels.get(table).toString().split(",")) {
					String[] relPair = relLine.split("~", 2);
					if(relPair.length != 2) {
						throw new IllegalArgumentException("Bad Relation Line: "+relLine);
					}
					this.relations.get(tableName).put(relPair[0].trim(), relPair[1].trim());
				}
			}
		}
		
		Connection dbConn;
		try {
//			System.out.println("dbDriver: '"+jdbcDriverClass+"'");
//			System.out.println("ConnLine: '"+connLine+"'");
//			System.out.println("UserName: '"+username+"'");
//			System.out.println("PassWord: '"+password+"'");
			dbConn = DriverManager.getConnection(connLine, username, password);
			this.cursor = dbConn.createStatement();
			this.rh = RecordHandler.parseConfig(opts.get("o"), opts.getProperties("O"));
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
		this.uriNS = connLine+"/";
	}
	
	/**
	 * Get the field prefix
	 * @return the field prefix
	 */
	private String getFieldPrefix() {
		if(this.queryPre == null) {
			this.queryPre = "";
		}
		return this.queryPre;
	}

	/**
	 * Set the field prefix
	 * @param fieldPrefix the field prefix to use
	 */
	public void setFieldPrefix(String fieldPrefix) {
		this.queryPre = fieldPrefix;
	}
	
	/**
	 * Get the field suffix
	 * @return the field suffix
	 */
	private String getFieldSuffix() {
		if(this.querySuf == null) {
			this.querySuf = "";
		}
		return this.querySuf;
	}

	/**
	 * Set the field suffix
	 * @param fieldSuffix the field suffix to use
	 */
	public void setFieldSuffix(String fieldSuffix) {
		this.querySuf = fieldSuffix;
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
				if((getIDFields(tableName).size() > 1 || !getIDFields(tableName).contains(colName)) && !getRelationFields(tableName).containsKey(colName)) {
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
	 * Get the where clauses for a table from the database
	 * @param tableName the table to get the where clauses for
	 * @return the where clauses
	 * @throws SQLException error connecting to DB
	 */
	private List<String> getWhereClauses(String tableName) throws SQLException {
		if(this.whereClauses == null) {
			this.whereClauses = new HashMap<String, List<String>>();
		}
		if(!this.whereClauses.containsKey(tableName)) {
			this.whereClauses.put(tableName, new LinkedList<String>());
		}
		return this.whereClauses.get(tableName);
	}
	
	/**
	 * Get the id field list for a table from the database
	 * @param tableName the table to get the id field list for
	 * @return the id field list
	 * @throws SQLException error connecting to DB
	 */
	private List<String> getIDFields(String tableName) throws SQLException {
		if(this.idFields == null) {
			this.idFields = new HashMap<String,List<String>>();
		}
		if(!this.idFields.containsKey(tableName)) {
			this.idFields.put(tableName, new LinkedList<String>());
			ResultSet primaryKeys = this.cursor.getConnection().getMetaData().getPrimaryKeys(this.cursor.getConnection().getCatalog(), null, tableName);
			while(primaryKeys.next()) {
				this.idFields.get(tableName).add(primaryKeys.getString("COLUMN_NAME"));
			}
		}
		if(this.idFields.get(tableName).isEmpty()) {
			throw new IllegalArgumentException("ID fields for table '"+tableName+"' were not provided and no primary keys are present... please provide an ID field set for this table");
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
		boolean multiTable = this.fromClauses.containsKey(tableName);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for(String dataField : getDataFields(tableName)) {
			sb.append(getFieldPrefix());
			if(multiTable) {
				sb.append(tableName);
				sb.append(".");
			}
			sb.append(dataField);
			sb.append(getFieldSuffix());
			sb.append(", ");
		}
		for(String relField : getRelationFields(tableName).keySet()) {
			sb.append(getFieldPrefix());
			if(multiTable) {
				sb.append(tableName);
				sb.append(".");
			}
			sb.append(relField);
			sb.append(getFieldSuffix());
			sb.append(", ");
		}
		for(String idField : getIDFields(tableName)) {
			sb.append(getFieldPrefix());
			if(multiTable) {
				sb.append(tableName);
				sb.append(".");
			}
			sb.append(idField);
			sb.append(getFieldSuffix());
			sb.append(", ");
		}
		sb.delete(sb.lastIndexOf(", "),sb.length());
		sb.append(" FROM ");
		sb.append(tableName);
		if(this.fromClauses.containsKey(tableName)) {
			sb.append(", ");
			sb.append(this.fromClauses.get(tableName));
		}

		if (getWhereClauses(tableName).size() > 0) {
			sb.append(" WHERE ");
			sb.append(StringUtils.join(" AND ", getWhereClauses(tableName)));
		}
		log.info("SQL Query: " + sb.toString());
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
	 * Builds a table's type description namespace
	 * @param tableName the table to build the namespace for
	 * @return the namespace
	 */
	private String buildTableType(String tableName) {
		return this.uriNS+"types#"+tableName;
	}

	/**
	 * Executes the task
	 */
	public void execute() {
		log.info("Fetch: Start");
		//For each Table
		try {
			for(String tableName : getTableNames()) {
				StringBuilder sb = new StringBuilder();
				//For each Record
				for(ResultSet rs = this.cursor.executeQuery(buildSelect(tableName)); rs.next(); ) {
					StringBuilder recID = new StringBuilder();
					recID.append("id");
					for(String idField : getIDFields(tableName)) {
						recID.append("_-_");
						recID.append(SpecialEntities.xmlEncode(rs.getString(idField).trim()));
					}
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
					
					
					//insert type value
					sb.append("    <rdf:type rdf:resource=\"");
					sb.append(buildTableType(tableName));	
					sb.append("\"/>\n");
					
					//DataFields
					for(String dataField : getDataFields(tableName)) {
						//Field BEGIN
						String field = tableNS+":"+dataField.replaceAll(" ", "_");
						sb.append("    <");
						sb.append(field);
						sb.append(">");
						
						//insert field value
						if (rs.getString(dataField) != null) {
							sb.append(SpecialEntities.xmlEncode(rs.getString(dataField).trim()));	
						}
						
						//Field END
						sb.append("</");
						sb.append(field);
						sb.append(">\n");
					}
					
					//Relation Fields
					for(String relationField : getRelationFields(tableName).keySet()) {
						//Field BEGIN
						sb.append("    <");
						sb.append(tableNS);
						sb.append(":");
						sb.append(relationField.replaceAll(" ", "_"));
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
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			new JDBCFetch(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(),e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(),e);
		}
	}
}
