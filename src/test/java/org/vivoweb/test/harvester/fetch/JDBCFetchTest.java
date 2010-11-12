/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.fetch;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;
import java.sql.Statement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.JDBCFetch;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JDBCRecordHandler;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JDBCFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JDBCFetchTest.class);
	/** */
	private Connection conn;
	/** */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger();
		this.conn = null;
		this.rh = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.conn != null) {
			this.conn.close();
		}
		if(this.rh != null) {
			this.rh.close();
		}
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.fetch.JDBCFetch#JDBCFetch(java.sql.Connection, org.vivoweb.harvester.util.repo.RecordHandler, java.lang.String)
	 * JDBCFetch(Connection dbConn, RecordHandler output, String uriNameSpace)}.
	 */
	public final void testJDBCFetchConstRun() {
		Exception error = null;
		Savepoint save = null;
		try {
			Class.forName("org.h2.Driver");
			this.conn = DriverManager.getConnection("jdbc:h2:mem:TestJDBCFetchDB", "sa", "");
			this.conn.setAutoCommit(false);
			save = this.conn.setSavepoint();
			Statement cursor = this.conn.createStatement();
			cursor.executeUpdate("CREATE TABLE department (dep_id int(10) NOT NULL AUTO_INCREMENT, `name` text NOT NULL, description text NOT NULL, PRIMARY KEY (dep_id))");
			cursor.executeUpdate("CREATE TABLE paylevel (id int(10) NOT NULL AUTO_INCREMENT, `name` text NOT NULL, low int(10) NOT NULL, high int(10) NOT NULL, PRIMARY KEY (id))");
			cursor.executeUpdate("CREATE TABLE faculty (fac_id int(10) NOT NULL AUTO_INCREMENT, badge_num int(10) NOT NULL, fname text NOT NULL, mname text NOT NULL, lname text NOT NULL, jobtitle text NOT NULL, salary int(10) NOT NULL, paygrade_id int(10) NOT NULL, dept_id int(10) NOT NULL, PRIMARY KEY (fac_id), CONSTRAINT faculty_ibfk_1 FOREIGN KEY (paygrade_id) REFERENCES paylevel(id), CONSTRAINT faculty_ibfk_2 FOREIGN KEY (dept_id) REFERENCES department(dep_id))");
			cursor.executeUpdate("INSERT INTO paylevel (id, name, low, high) VALUES (1, 'IT Expert', 100000, 300000), (2, 'IT Noob', 20000, 25000);");
			cursor.executeUpdate("INSERT INTO department (dep_id, name, description) VALUES (1, 'CTRIP', 'UF Clinical & Translational Research Informatics Program');");
			cursor.executeUpdate("INSERT INTO faculty (fac_id, badge_num, fname, mname, lname, jobtitle, salary, paygrade_id, dept_id) VALUES (1, 12345678, 'Bob', 'Alfred', 'Johnson', 'Software Engineer', 156000, 1, 1), (2, 98765432, 'Fredrick', 'Markus', 'Brown', 'Junior Software Engineer', 22500, 2, 1);");
			this.conn.commit();
			save = null;
			this.rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:mem:TestJDBCFetchRH", "sa", "", "recordTable", "dataField");
			new JDBCFetch(this.conn, this.rh, "jdbc:h2:mem:TestJDBCFetchDB").execute();
			assertTrue(this.rh.iterator().hasNext());
			DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for(Record r : this.rh) {
				Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
				Element elem = doc.getDocumentElement();
				traverseNodes(elem.getChildNodes());
			}
		} catch(Exception e) {
			error = e;
		} finally {
			if(this.conn != null && save != null) {
				try {
					this.conn.rollback(save);
				} catch(Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			if(error != null) {
				log.error(error.getMessage(), error);
				fail(error.getMessage());
			}
		}
	}
	
	/**
	 * @param nodeList the nodes
	 */
	private void traverseNodes(NodeList nodeList) {
		for(int x = 0; x < nodeList.getLength(); x++) {
			Node child = nodeList.item(x);
			String name = child.getNodeName();
			if(!name.contains("#text")) {
				log.info(name);
				traverseNodes(child.getChildNodes());
			}
		}
	}
}
