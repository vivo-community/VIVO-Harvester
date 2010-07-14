package org.vivoweb.ingest.fetch;

import static java.util.Arrays.asList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;

/**
 * Testing if JDBC table data can be used for JDBCFetch
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JDBCTest extends Task {	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(JDBCFetch.class);
	/**
	 * Table information
	 */
//	private Map<String,Map<String,String>> tables;
	/**
	 * Statement processor for the database
	 */
	private Statement cursor;
	
	/**
	 * Constructor
	 * @param dbConn connection to the database
	 * @throws SQLException error talking with database
	 */
	public JDBCTest(Connection dbConn) throws SQLException {
		this.cursor = dbConn.createStatement();
	}
	
	/**
	 * Temp to see if we can read the data from the database rather than config files
	 * @throws SQLException error talking with database
	 */
	public void getTableData() throws SQLException {
		List<String> tableNames = new LinkedList<String>();
		DatabaseMetaData metaData = this.cursor.getConnection().getMetaData();
		String catalog = this.cursor.getConnection().getCatalog();
		//Get Table Names
		String[] tableTypes = {"TABLE"};
		ResultSet tableData = metaData.getTables(null, null, "%", tableTypes);
		while(tableData.next()) {
			tableNames.add(tableData.getString("TABLE_NAME"));
		}
		
		//For each Table
		for(String tableName : tableNames) {
			System.out.println("====================================");
			System.out.println("tableName: "+tableName);
			System.out.println("------------------------------------");
			ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, null, tableName);
			while(primaryKeys.next()) {
				String name = primaryKeys.getString("COLUMN_NAME");
//				String dataType = primaryKeys.getString("TYPE_NAME");
//				int size = primaryKeys.getInt("COLUMN_SIZE");
				System.out.println("primary key: "+name);
			}
			ResultSet columnData = metaData.getColumns(catalog, null, tableName, "%");
			while(columnData.next()) {
				String name = columnData.getString("COLUMN_NAME");
				String dataType = columnData.getString("TYPE_NAME");
				int size = columnData.getInt("COLUMN_SIZE");
				System.out.println("column name: "+name+" - "+dataType+"["+size+"]");
			}
			ResultSet foreignKeys = metaData.getExportedKeys(catalog, null, tableName);
			while(foreignKeys.next()) {
				String foreignTable = foreignKeys.getString("FKTABLE_NAME");
				String foreignColumnName = foreignKeys.getString("FKCOLUMN_NAME");
				int foreignSequence = foreignKeys.getInt("KEY_SEQ");
				System.out.println("foreign key: "+foreignSequence+" - "+foreignTable+"["+foreignColumnName+"]");
			}
			System.out.println("====================================");
			System.out.println();
		}
	}
	
	protected static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("d", "driver")).withRequiredArg().describedAs("jdbc driver class");
		parser.acceptsAll(asList("c", "connection")).withRequiredArg().describedAs("jdbc connection string");
		parser.acceptsAll(asList("u", "username")).withRequiredArg().describedAs("database username");
		parser.acceptsAll(asList("p", "password")).withRequiredArg().describedAs("database password");
		parser.acceptsAll(asList("o", "rh", "output")).withRequiredArg().describedAs("RecordHandler config file path");
		return parser;
	}
	
	public static JDBCTest getInstance(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
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
		Connection dbConn;
		try {
			dbConn = DriverManager.getConnection("jdbc:"+connType+"://"+host+":"+port+"/"+dbName, username, password);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
		try {
			return new JDBCTest(dbConn);
		} catch(SQLException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public void executeTask() {
		try {
			getTableData();
		} catch(SQLException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	public static void main(String... args) {
		if(args.length == 1) {
			Task.main(args);
		}
		OptionSet o = getParser().parse(args);
		checkNeededArgs(o,"d","c","u","p","o");
		if(o.has("d")) {
			//
		}
	}
}
