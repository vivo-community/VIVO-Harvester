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
package org.vivoweb.ingest.test.util.repo;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.TextFileRecordHandler;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import junit.framework.TestCase;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JenaConnectTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(JenaConnectTest.class);
	/** */private static final String nl = System.getProperty("line.separator");
	/** */private JenaConnect jc;
	/** */private final String dbClass = "org.h2.Driver";
	/** */private final String dbType = "HSQLDB";
	/** */private final String dbUrl = "jdbc:h2:XMLVault/TestJC/model;MODE=HSQLDB";
	/** */private final String modelName = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
	/** */private final String modelName2 = "http://vitro.mannlib.cornell.edu/testing/non-vitro";
	/** */private final String dbUser = "sa";
	/** */private final String dbPass = "";
	/** */private final String namespace = "http://ingest.vivoweb.org/test/repo/JenaConnect#";
	/** */private final String rdfIn = ""+
	"<?xml version=\"1.0\"?>\n"+
	"<rdf:RDF\n"+
	"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
	"    xmlns:si=\"http://www.w3schools.com/rdf/\" > \n"+
	"  <rdf:Description rdf:about=\"http://www.w3schools.com\">\n"+
	"    <si:author>Jan Egil Refsnes</si:author>\n"+
	"    <si:title>W3Schools</si:title>\n"+
	"  </rdf:Description>\n"+
	"</rdf:RDF>\n";
	/** */private final String rdfOut = ""+
	"<?xml version=\"1.0\"?>"+nl+
	"<rdf:RDF"+nl+
	"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+nl+
	"    xmlns:si=\"http://www.w3schools.com/rdf/\" > "+nl+
	"  <rdf:Description rdf:about=\"http://www.w3schools.com\">"+nl+
	"    <si:title>W3Schools</si:title>"+nl+
	"    <si:author>Jan Egil Refsnes</si:author>"+nl+
	"  </rdf:Description>"+nl+
	"</rdf:RDF>"+nl;
	
	@Override
	protected void setUp() throws Exception {
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestJC").createFolder();
		this.jc = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.jc != null) {
			this.jc.close();
		}
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestJC").delete(new AllFileSelector());
	}
	
	/**
	 * 
	 */
	private void runWriteTest() {
		Resource res = this.jc.getJenaModel().createResource();
		Property prop = this.jc.getJenaModel().createProperty(this.namespace, "testProperty");
		this.jc.getJenaModel().add(res, prop, "testValue");
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#parseConfig(org.apache.commons.vfs.FileObject) JenaConnect.parseConfig(FileObject configFile)}.
	 */
	public void testParseConfigFile() {
		try {
			FileObject fo = VFS.getManager().resolveFile(new File("."), "XMLVault/TestJC/jcConfig.xml");
			BufferedWriter bfos = new BufferedWriter(new OutputStreamWriter(fo.getContent().getOutputStream(false)));
			bfos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Model>\n  <Param name=\"dbClass\">"+this.dbClass+"</Param>\n  <Param name=\"dbType\">"+this.dbType+"</Param>\n  <Param name=\"dbUrl\">"+this.dbUrl+"</Param>\n  <Param name=\"modelName\">"+this.modelName+"</Param>\n  <Param name=\"dbUser\">"+this.dbUser+"</Param>\n  <Param name=\"dbPass\">"+this.dbPass+"</Param>\n</Model>");
			bfos.close();
			this.jc = JenaConnect.parseConfig(fo);
			fo.close();
			runWriteTest();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#JenaConnect(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String) JenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass)}.
	 */
	public void testJenaConnectDBConstNoModelName() {
		this.jc = new JenaConnect(this.dbUrl, this.dbUser, this.dbPass, this.dbType, this.dbClass);
		runWriteTest();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#JenaConnect(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String) JenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String modelName)}.
	 */
	public void testJenaConnectDBConstWithModelName() {
		this.jc = new JenaConnect(this.dbUrl, this.dbUser, this.dbPass, this.dbType, this.dbClass, this.modelName);
		runWriteTest();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#JenaConnect(org.vivoweb.ingest.util.repo.JenaConnect, java.lang.String) JenaConnect(JenaConnect old, String modelName)}.
	 */
	public void testJenaConnectConstSibling() {
		try {
			this.jc = new JenaConnect(new JenaConnect(this.dbUrl, this.dbUser, this.dbPass, this.dbType, this.dbClass, this.modelName), this.modelName2);
			runWriteTest();
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#JenaConnect(java.io.InputStream) JenaConnect(InputStream in)}.
	 */
	public void testJenaConnectConstInputStream() {
		this.jc = new JenaConnect(new ByteArrayInputStream(this.rdfIn.getBytes()));
		runWriteTest();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#JenaConnect()}.
	 */
	public void testJenaConnectMemConstNoModelName() {
		this.jc = new JenaConnect();
		runWriteTest();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#JenaConnect(java.lang.String)}.
	 */
	public void testJenaConnectMemConstWithModelName() {
		this.jc = new JenaConnect(this.modelName2);
		runWriteTest();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#loadRDF(java.io.InputStream)}.
	 */
	public void testLoadRDF() {
		this.jc = new JenaConnect();
		this.jc.loadRDF(new ByteArrayInputStream(this.rdfIn.getBytes()));
		StmtIterator stmnt = this.jc.getJenaModel().listStatements();
		assertTrue(stmnt.hasNext());
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#exportRDF(java.io.OutputStream)}.
	 */
	public final void testExportRDF() {
		try {
			this.jc = new JenaConnect(new ByteArrayInputStream(this.rdfIn.getBytes()));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.jc.exportRDF(baos);
			baos.flush();
			String output = baos.toString();
			assertEquals(this.rdfOut, output);
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.repo.JenaConnect#importRDF(org.vivoweb.ingest.util.repo.RecordHandler)}.
	 */
	public final void testImportRDF() {
		try {
//			RecordHandler rh = new TextFileRecordHandler("XMLVault/JDBCRDF");
			RecordHandler rh = new TextFileRecordHandler("XMLVault/TestJC/TFRDF");
			rh.addRecord("department_id-1", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-department=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/department/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/department\">\n  <rdf:Description rdf:ID=\"id-1\">\n    <db-department:name>CTRIP</db-department:name>\n    <db-department:description>UF Clinical &amp; Translational Research Informatics Program</db-department:description>\n  </rdf:Description>\n</rdf:RDF>", getClass());
			rh.addRecord("faculty_id-1", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-faculty=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/faculty/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/faculty\">\n  <rdf:Description rdf:ID=\"id-1\">\n    <db-faculty:badge_num>12345678</db-faculty:badge_num>\n    <db-faculty:fname>Bob</db-faculty:fname>\n    <db-faculty:mname>Alfred</db-faculty:mname>\n    <db-faculty:lname>Johnson</db-faculty:lname>\n    <db-faculty:jobtitle>Software Engineer</db-faculty:jobtitle>\n    <db-faculty:salary>156000</db-faculty:salary>\n    <db-faculty:paygrade_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel#id-1\"/>\n    <db-faculty:dept_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/department#id-1\"/>\n  </rdf:Description>\n</rdf:RDF>", getClass());
			rh.addRecord("faculty_id-2", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-faculty=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/faculty/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/faculty\">\n  <rdf:Description rdf:ID=\"id-2\">\n    <db-faculty:badge_num>98765432</db-faculty:badge_num>\n    <db-faculty:fname>Fredrick</db-faculty:fname>\n    <db-faculty:mname>Markus</db-faculty:mname>\n    <db-faculty:lname>Brown</db-faculty:lname>\n    <db-faculty:jobtitle>Junior Software Engineer</db-faculty:jobtitle>\n    <db-faculty:salary>22500</db-faculty:salary>\n    <db-faculty:paygrade_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel#id-2\"/>\n    <db-faculty:dept_id rdf:resource=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/department#id-1\"/>\n  </rdf:Description>\n</rdf:RDF>", getClass());
			rh.addRecord("paylevel_id-1", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-paylevel=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/paylevel/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel\">\n  <rdf:Description rdf:ID=\"id-1\">\n    <db-paylevel:name>IT Expert</db-paylevel:name>\n    <db-paylevel:low>100000</db-paylevel:low>\n    <db-paylevel:high>300000</db-paylevel:high>\n  </rdf:Description>\n</rdf:RDF>", getClass());
			rh.addRecord("paylevel_id-2", "<?xml version=\"1.0\"?>\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n         xmlns:db-paylevel=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/fields/paylevel/\"\n         xml:base=\"jdbc:mysql://127.0.0.1:3306/jdbctestharvest/paylevel\">\n  <rdf:Description rdf:ID=\"id-2\">\n    <db-paylevel:name>IT Noob</db-paylevel:name>\n    <db-paylevel:low>20000</db-paylevel:low>\n    <db-paylevel:high>25000</db-paylevel:high>\n  </rdf:Description>\n</rdf:RDF>", getClass());
			this.jc = new JenaConnect();
			this.jc.importRDF(rh);
//			this.jc.exportRDF(VFS.getManager().resolveFile(new File("."), "XMLVault/rdf.dump.xml").getContent().getOutputStream(false));
			StmtIterator stmnt = this.jc.getJenaModel().listStatements();
			assertTrue(stmnt.hasNext());
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		}
	}
	
}
