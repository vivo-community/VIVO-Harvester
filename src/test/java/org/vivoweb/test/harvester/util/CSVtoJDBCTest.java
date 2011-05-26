/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.test.harvester.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.CSVtoJDBC;
//import org.vivoweb.harvester.util.InitLog;

/**
 * @author jrpence
 */
public class CSVtoJDBCTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(CSVtoJDBCTest.class);
	/** */
	private InputStream sourceStream;
	/** */
	private Connection conn;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String sourcefile ="\"NAME\",\"ID\"\n\"Burton, Joe D\",\"1\"\n\"Smith, Jane W\",\"2\"\n\"Schwarz, Elizabeth R\",\"3\"\n\"Silverstein, Steven\",\"4\"";
		this.sourceStream = new ByteArrayInputStream(sourcefile.getBytes());
//		InitLog.initLogger(null, null);
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
	 */
	public void testCSVtoJDBCStreamConnectionString() {
		log.info("BEGIN testCSVtoJDBCStringConnectionString");
		new CSVtoJDBC(this.sourceStream, this.conn, "Person");
		log.info("END testCSVtoJDBCStringConnectionString");
	}
	
	/**
	 * @throws IOException if the file is missing or not read properly
	 */
	public void testExecute() throws IOException {
		log.info("BEGIN testExecute");
		CSVtoJDBC subject = new CSVtoJDBC(this.sourceStream, this.conn, "Person");
		subject.execute();
		try {
			Statement cursor = this.conn.createStatement();
			String sqlQuery = "SELECT * from Person";
			ResultSet results = cursor.executeQuery(sqlQuery);
			ResultSetMetaData resMeta = results.getMetaData();
			{
				StringBuilder line = new StringBuilder();
				for(int x = 1; x <= resMeta.getColumnCount(); x++) {
					line.append("[");
					line.append(resMeta.getColumnName(x));
					line.append("]");
				}
				assertEquals("[ROWID][NAME][ID]", line.toString());
				log.info(line.toString());
			}
			String name[] = {"Burton, Joe D", "Smith, Jane W", "Schwarz, Elizabeth R", "Silverstein, Steven"};
			int rowid = 0;
			while(results.next()) {
				StringBuilder line = new StringBuilder();
				for(int x = 1; x <= resMeta.getColumnCount(); x++) {
					line.append("|");
					line.append(results.getString(x));
					line.append("|");
				}
				String expected = "|" + rowid + "||" + name[rowid] + "||" + ++rowid + "|";
				assertEquals(expected, line.toString());
				log.info(line.toString());
			}
		} catch(SQLException e) {
			throw new IOException(e);
		}
		log.info("END testExecute");
	}
	
}
