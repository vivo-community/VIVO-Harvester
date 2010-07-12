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
import org.vivoweb.ingest.util.JDBCRecordHandler;
import org.vivoweb.ingest.util.RecordHandler;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class JDBCFetch extends Task {
	
	private static Log log = LogFactory.getLog(JDBCFetch.class);
	private RecordHandler rh;
	private Connection db;
	private HashMap<String,Map<String,String>> tables;
	private Statement cursor;
	private String uriNS;

	@Override
	protected void acceptParams(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
		String repositoryConfig = getParam(params, "repositoryConfig", true);
		this.rh = RecordHandler.parseConfig(repositoryConfig);
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
		this.uriNS = "http://"+connType+"."+host+"/"+dbName+"/";
		try {
			this.db = DriverManager.getConnection("jdbc:"+connType+"://"+host+":"+port+"/"+dbName, username, password);
			this.cursor = this.db.createStatement();
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
		this.tables = new HashMap<String,Map<String,String>>();
		for(String tableData : params.keySet()) {
			String[] temp = tableData.split("\\.", 2);
			if(temp.length == 2) {
				if(!this.tables.containsKey(temp[0])) {
					this.tables.put(temp[0], new HashMap<String,String>());
				}
				this.tables.get(temp[0]).put(temp[1], params.get(tableData));
			}
		}
		for(String tableName : this.tables.keySet()) {
			checkTableExists(tableName);
			checkTableConfigured(tableName);
		}
	}
	
	private void checkTableExists(String tableName) throws IOException {
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
			throw new IOException("Database Does Not Contain Table: "+tableName);
		}
	}
	
	private String[] getDataFields(String tableName) {
		return this.tables.get(tableName).get("dataFieldList").split("\\s?,\\s?");
	}
	
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
	
	private String getIDField(String tableName) {
		return this.tables.get(tableName).get("idField");
	}
	
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
	
	private String buildTableNS(String tableName) {
		return this.uriNS+tableName+"/";
	}
	
	private String buildTableFieldNS(String tableName) {
		return this.uriNS+"fields/"+tableName+"/";
	}
	
	private void checkTableConfigured(String tableName) throws IOException {
		boolean a = true;
		try {
			this.cursor.execute(buildSelect(tableName));
		} catch(SQLException e) {
			a = false;
		}
		if(!a) {
			throw new IOException("Table '"+tableName+"' Is Not Structured Correctly");
		}
	}
	
	@Override
	protected void runTask() throws NumberFormatException {
		//For each Table
		for(String tableName : this.tables.keySet()) {
			StringBuilder sb = new StringBuilder();
			try {
				//For each Record
				for(ResultSet rs = this.cursor.executeQuery(buildSelect(tableName)); rs.next(); ) {
					String recID = "id-"+rs.getString(getIDField(tableName)).trim();
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
					sb.append(buildTableNS(tableName));
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
						sb.append(this.buildTableNS(relations.get(relationField)));
						
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
