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
package org.vivoweb.ingest.test.fetch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;
import java.sql.Statement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.fetch.JDBCFetch;
import org.vivoweb.ingest.util.repo.Record;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.TextFileRecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import junit.framework.TestCase;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JDBCFetchTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(JDBCFetchTest.class);
	/** */private static Connection conn;
	/** */private static RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		conn = null;
		rh = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(conn != null) {
			conn.close();
		}
		if(rh != null) {
			rh.close();
		}
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestJDBCFetch").delete(new AllFileSelector());
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.fetch.JDBCFetch#JDBCFetch(java.sql.Connection, org.vivoweb.ingest.util.repo.RecordHandler, java.lang.String)}.
	 */
	public final void testJDBCFetchConstRun() {
		Exception error = null;
		Savepoint save = null;
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:XMLVault/TestJDBCFetch/h2", "sa", "");
			conn.setAutoCommit(false);
			save = conn.setSavepoint();
			Statement cursor = conn.createStatement();
			cursor.executeUpdate("CREATE TABLE department (dep_id int(10) NOT NULL AUTO_INCREMENT, `name` text NOT NULL, description text NOT NULL, PRIMARY KEY (dep_id))");
			cursor.executeUpdate("CREATE TABLE paylevel (id int(10) NOT NULL AUTO_INCREMENT, `name` text NOT NULL, low int(10) NOT NULL, high int(10) NOT NULL, PRIMARY KEY (id))");
			cursor.executeUpdate("CREATE TABLE faculty (fac_id int(10) NOT NULL AUTO_INCREMENT, badge_num int(10) NOT NULL, fname text NOT NULL, mname text NOT NULL, lname text NOT NULL, jobtitle text NOT NULL, salary int(10) NOT NULL, paygrade_id int(10) NOT NULL, dept_id int(10) NOT NULL, PRIMARY KEY (fac_id), CONSTRAINT faculty_ibfk_1 FOREIGN KEY (paygrade_id) REFERENCES paylevel(id), CONSTRAINT faculty_ibfk_2 FOREIGN KEY (dept_id) REFERENCES department(dep_id))");
			cursor.executeUpdate("INSERT INTO paylevel (id, name, low, high) VALUES (1, 'IT Expert', 100000, 300000), (2, 'IT Noob', 20000, 25000);");
			cursor.executeUpdate("INSERT INTO department (dep_id, name, description) VALUES (1, 'CTRIP', 'UF Clinical & Translational Research Informatics Program');");
			cursor.executeUpdate("INSERT INTO faculty (fac_id, badge_num, fname, mname, lname, jobtitle, salary, paygrade_id, dept_id) VALUES (1, 12345678, 'Bob', 'Alfred', 'Johnson', 'Software Engineer', 156000, 1, 1), (2, 98765432, 'Fredrick', 'Markus', 'Brown', 'Junior Software Engineer', 22500, 2, 1);");
			conn.commit();
			save = null;
			rh = new TextFileRecordHandler("XMLVault/TestJDBCFetch/tfrh");
			new JDBCFetch(conn, rh, "jdbc:h2:XMLVault/TestJDBCFetch/h2/").execute();
			assertTrue(rh.iterator().hasNext());
			DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for(Record r : rh) {
				Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
				Element elem = doc.getDocumentElement();
				traverseNodes(elem.getChildNodes());
			}
		} catch(Exception e) {
			error = e;
		} finally {
			if(conn != null && save != null) {
				try {
					conn.rollback(save);
				} catch(Exception e) {
					log.error(e.getMessage(),e);
				}
			}
			if(error != null) {
				log.error(error.getMessage(),error);
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
