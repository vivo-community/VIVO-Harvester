/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.test.harvester.util.repo;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JDBCRecordHandler;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JenaConnectTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JenaConnectTest.class);
	/** */
	private static final String nl = System.getProperty("line.separator");
	/** */
	private static final String type = "sdb";
	/** */
	private static final String dbLayout = "layout2";
	/** */
	private static final String dbClass = "org.h2.Driver";
	/** */
	private static final String dbType = "H2";
	/** */
	private static final String dbUrl = "jdbc:h2:mem:TestJCmodel";
	/** */
	private static final String modelName = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
	/** */
	private static final String modelName2 = "http://vitro.mannlib.cornell.edu/testing/non-vitro";
	/** */
	private static final String dbUser = "sa";
	/** */
	private static final String dbPass = "";
	/** */
	private static final String namespace = "http://harvester.vivoweb.org/test/repo/JenaConnect#";
	/** */
	private static final String rdfIn = "" + "<?xml version=\"1.0\"?>\n" + "<rdf:RDF\n" + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "    xmlns:si=\"http://www.w3schools.com/rdf/\" > \n" + "  <rdf:Description rdf:about=\"http://www.w3schools.com\">\n" + "    <si:author>Jan Egil Refsnes</si:author>\n" + "    <si:title>W3Schools</si:title>\n" + "  </rdf:Description>\n" + "</rdf:RDF>\n";
	/** */
	private static final String rdfOut = "" + "<?xml version=\"1.0\"?>" + nl + "<rdf:RDF" + nl + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + nl + "    xmlns:si=\"http://www.w3schools.com/rdf/\" > " + nl + "  <rdf:Description rdf:about=\"http://www.w3schools.com\">" + nl + "    <si:author>Jan Egil Refsnes</si:author>" + nl + "    <si:title>W3Schools</si:title>" + nl + "  </rdf:Description>" + nl + "</rdf:RDF>" + nl;
	/** */
	private JenaConnect jc;
	/** */
	private File configFile;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
		this.configFile = File.createTempFile("jcConfig", "xml");
		this.jc = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.jc != null) {
			this.jc.close();
		}
	}
	
	/**
	 * 
	 */
	private void runWriteTest() {
		Resource res = this.jc.getJenaModel().createResource();
		Property prop = this.jc.getJenaModel().createProperty(namespace, "testProperty");
		this.jc.getJenaModel().add(res, prop, "testValue");
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.util.repo.JenaConnect#parseConfig(org.apache.commons.vfs.FileObject)
	 * JenaConnect.parseConfig(FileObject configFile)}.
	 * @throws IOException error
	 */
	public void testParseConfigFile() throws IOException {
		log.info("BEGIN testParseConfigFile");
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.configFile));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Model>\n  <Param name=\"type\">" + type + "</Param>\n  <Param name=\"dbLayout\">" + dbLayout + "</Param>\n  <Param name=\"dbClass\">" + dbClass + "</Param>\n  <Param name=\"dbType\">" + dbType + "</Param>\n  <Param name=\"dbUrl\">" + dbUrl + "</Param>\n  <Param name=\"modelName\">" + modelName + "</Param>\n  <Param name=\"dbUser\">" + dbUser + "</Param>\n  <Param name=\"dbPass\">" + dbPass + "</Param>\n</Model>");
		bw.close();
		this.jc = JenaConnect.parseConfig(this.configFile);
		runWriteTest();
		log.info("END testParseConfigFile");
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.util.repo.RDBJenaConnect#RDBJenaConnect(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * RDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass)}.
	 * @throws IOException error
	 */
	public void testJenaConnectDBConstNoModelName() throws IOException {
		log.info("BEGIN testJenaConnectDBConstNoModelName");
		this.jc = new SDBJenaConnect(dbUrl, dbUser, dbPass, dbType, dbClass, dbLayout);
		runWriteTest();
		log.info("END testJenaConnectDBConstNoModelName");
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.util.repo.RDBJenaConnect#RDBJenaConnect(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * RDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String modelName)}.
	 * @throws IOException error
	 */
	public void testJenaConnectDBConstWithModelName() throws IOException {
		log.info("BEGIN testJenaConnectDBConstWithModelName");
		this.jc = new SDBJenaConnect(dbUrl, dbUser, dbPass, dbType, dbClass, dbLayout, modelName);
		runWriteTest();
		log.info("END testJenaConnectDBConstWithModelName");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.repo.JenaConnect#neighborConnectClone(java.lang.String)
	 * connect(String modelName)}.
	 * @throws IOException error
	 */
	public void testJenaConnectConstSibling() throws IOException {
		log.info("BEGIN testJenaConnectConstSibling");
		this.jc = new SDBJenaConnect(dbUrl, dbUser, dbPass, dbType, dbClass, dbLayout, modelName).neighborConnectClone(modelName2);
		runWriteTest();
		log.info("END testJenaConnectConstSibling");
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.util.repo.MemJenaConnect#MemJenaConnect(java.io.InputStream, java.lang.String, java.lang.String)
	 * JenaConnect(InputStream in, String namespace)}.
	 * @throws IOException error
	 */
	public void testJenaConnectConstInputStream() throws IOException {
		log.info("BEGIN testJenaConnectConstInputStream");
		this.jc = new MemJenaConnect(new ByteArrayInputStream(rdfIn.getBytes()), null, null);
		runWriteTest();
		log.info("END testJenaConnectConstInputStream");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.repo.MemJenaConnect#MemJenaConnect() MemJenaConnect()}.
	 * @throws IOException error
	 */
	public void testJenaConnectMemConstNoModelName() throws IOException {
		log.info("BEGIN testJenaConnectMemConstNoModelName");
		this.jc = new MemJenaConnect();
		runWriteTest();
		log.info("END testJenaConnectMemConstNoModelName");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.repo.MemJenaConnect#MemJenaConnect(java.lang.String)
	 * MemJenaConnect(String
	 * modelName)}.
	 * @throws IOException error
	 */
	public void testJenaConnectMemConstWithModelName() throws IOException {
		log.info("BEGIN testJenaConnectMemConstWithModelName");
		this.jc = new MemJenaConnect(modelName2);
		runWriteTest();
		log.info("END testJenaConnectMemConstWithModelName");
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.util.repo.JenaConnect#loadRdfFromStream(java.io.InputStream, java.lang.String, java.lang.String)
	 * loadRDF(InputStream in, String namespace)}.
	 * @throws IOException error
	 */
	public void testLoadRDF() throws IOException {
		log.info("BEGIN testLoadRDF");
		this.jc = new MemJenaConnect();
		this.jc.loadRdfFromStream(new ByteArrayInputStream(rdfIn.getBytes()), null, null);
		StmtIterator stmnt = this.jc.getJenaModel().listStatements();
		assertTrue(stmnt.hasNext());
		log.info("END testLoadRDF");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.repo.JenaConnect#exportRdfToStream(java.io.OutputStream)
	 * exportRDF(OutputStream out)}.
	 * @throws IOException error
	 */
	public final void testExportRDF() throws IOException {
		log.info("BEGIN testExportRDF");
		this.jc = new MemJenaConnect(new ByteArrayInputStream(rdfIn.getBytes()), null, null);
		assertEquals(rdfOut, this.jc.exportRdfToString());
		log.info("END testExportRDF");
	}
	
	/**
	 * Test method for
	 * {@link org.vivoweb.harvester.util.repo.JenaConnect#loadRdfFromRH(org.vivoweb.harvester.util.repo.RecordHandler, java.lang.String)
	 * importRDF(RecordHandler rh, String namespace)}.
	 * @throws IOException error
	 */
	public final void testImportRDF() throws IOException {
		log.info("BEGIN testImportRDF");
		RecordHandler rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:mem:TestJC-TFRH", "sa", "", "recordTable", "dataField");
		rh.addRecord("department_id-1", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-department=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/department/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/department\">\n  <rdf:Description rdf:ID=\"id-1\">\n    <db-department:name>CTRIP</db-department:name>\n    <db-department:description>UF Clinical &amp; Translational Research Informatics Program</db-department:description>\n  </rdf:Description>\n</rdf:RDF>", getClass());
		rh.addRecord("faculty_id-1", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-faculty=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/faculty/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/faculty\">\n  <rdf:Description rdf:ID=\"id-1\">\n    <db-faculty:badge_num>12345678</db-faculty:badge_num>\n    <db-faculty:fname>Bob</db-faculty:fname>\n    <db-faculty:mname>Alfred</db-faculty:mname>\n    <db-faculty:lname>Johnson</db-faculty:lname>\n    <db-faculty:jobtitle>Software Engineer</db-faculty:jobtitle>\n    <db-faculty:salary>156000</db-faculty:salary>\n    <db-faculty:paygrade_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel#id-1\"/>\n    <db-faculty:dept_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/department#id-1\"/>\n  </rdf:Description>\n</rdf:RDF>", getClass());
		rh.addRecord("faculty_id-2", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-faculty=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/faculty/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/faculty\">\n  <rdf:Description rdf:ID=\"id-2\">\n    <db-faculty:badge_num>98765432</db-faculty:badge_num>\n    <db-faculty:fname>Fredrick</db-faculty:fname>\n    <db-faculty:mname>Markus</db-faculty:mname>\n    <db-faculty:lname>Brown</db-faculty:lname>\n    <db-faculty:jobtitle>Junior Software Engineer</db-faculty:jobtitle>\n    <db-faculty:salary>22500</db-faculty:salary>\n    <db-faculty:paygrade_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel#id-2\"/>\n    <db-faculty:dept_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/department#id-1\"/>\n  </rdf:Description>\n</rdf:RDF>", getClass());
		rh.addRecord("paylevel_id-1", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-paylevel=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/paylevel/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel\">\n  <rdf:Description rdf:ID=\"id-1\">\n    <db-paylevel:name>IT Expert</db-paylevel:name>\n    <db-paylevel:low>100000</db-paylevel:low>\n    <db-paylevel:high>300000</db-paylevel:high>\n  </rdf:Description>\n</rdf:RDF>", getClass());
		rh.addRecord("paylevel_id-2", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-paylevel=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/paylevel/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel\">\n  <rdf:Description rdf:ID=\"id-2\">\n    <db-paylevel:name>IT Noob</db-paylevel:name>\n    <db-paylevel:low>20000</db-paylevel:low>\n    <db-paylevel:high>25000</db-paylevel:high>\n  </rdf:Description>\n</rdf:RDF>", getClass());
		this.jc = new MemJenaConnect();
		this.jc.loadRdfFromRH(rh, null);
		StmtIterator stmnt = this.jc.getJenaModel().listStatements();
		assertTrue(stmnt.hasNext());
		log.info("END testImportRDF");
	}
	
	/**
	 * @throws IOException error
	 */
	public final void testContainsURI() throws IOException {
		log.info("BEGIN testContainsURI");
		this.jc = new MemJenaConnect(new ByteArrayInputStream(rdfIn.getBytes()), null, null);
		assertTrue(this.jc.containsURI("http://www.w3schools.com"));
		assertFalse(this.jc.containsURI("http://www.yourmom.com"));
		log.info("END testContainsURI");
	}
	
}
