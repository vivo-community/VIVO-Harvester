package org.vivoweb.test.harvester.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.CSVtoJDBC;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.test.harvester.fetch.JDBCFetchTest;
import junit.framework.TestCase;

/**
 * @author jrpence
 *
 */
public class CSVtoJDBCTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JDBCFetchTest.class);
	/** */
	private Connection conn;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		InitLog.initLogger(CSVtoJDBCTest.class);
		Class.forName("org.h2.Driver");
		this.conn = DriverManager.getConnection("jdbc:h2:mem:TestJDBCFetchDB", "sa", "");
		this.conn.setAutoCommit(false);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if(this.conn != null) {
			this.conn.close();
		}
		this.conn = null;
	}
	
	/**
	 * @throws IOException if the file is missing or not read properly
	 * 
	 */
	public void testCSVtoJDBCStringConnectionString() throws IOException {
		log.info("BEGIN testCSVtoJDBCStringConnectionString");
		new CSVtoJDBC("files/person.csv", this.conn, "Person");
		log.info("END testCSVtoJDBCStringConnectionString");
	}
	
	/**
	 * @throws IOException if the file is missing or not read properly
	 * 
	 */
	public void testCSVtoJDBCStringStringStringStringStringString() throws IOException {
		log.info("BEGIN testCSVtoJDBCStringStringStringStringStringString");
		new CSVtoJDBC("files/person.csv", "org.h2.Driver","jdbc:h2:mem:TestJDBCFetchDB","sa","", "Person");
		log.info("END testCSVtoJDBCStringStringStringStringStringString");
	}
	
	
	/**
	 * @throws IOException if the file is missing or not read properly
	 * 
	 */
	public void testExecute() throws IOException {
		log.info("BEGIN testExecute");
		CSVtoJDBC subject = new CSVtoJDBC("files/person.csv", this.conn, "Person");
		subject.execute();
		try {
			Statement cursor = this.conn.createStatement();
			String sqlQuery = "SELECT * from Person";
			ResultSet results = cursor.executeQuery(sqlQuery);
			ResultSetMetaData resMeta= results.getMetaData();
			{
				StringBuilder line = new StringBuilder();
				for(int x = 1; x <= resMeta.getColumnCount();x++){
					line.append("[");
					line.append(resMeta.getColumnName(x) );
					line.append("]");
				}
				assertEquals("[ROWID][NAME][ID]", line.toString());
				log.info(line.toString());
			}
			String name[] = {"Burton, Joe D","Smith, Jane W","Schwarz, Elizabeth R","Silverstein, Steven"};
			int rowid = 0;
			while(results.next()) {
				StringBuilder line = new StringBuilder();
				for(int x = 1; x <= resMeta.getColumnCount();x++){
					line.append("|");
					line.append(results.getString(x) );
					line.append("|");
				}
				String expected = "|" + rowid++ + "||" + name[rowid-1] + "||" + rowid + "|";
				assertEquals(expected, line.toString());
				log.info(line.toString());
			}
		} catch(SQLException e) {
			throw new IOException(e);
		}
		log.info("END testExecute");
	}
	
}
