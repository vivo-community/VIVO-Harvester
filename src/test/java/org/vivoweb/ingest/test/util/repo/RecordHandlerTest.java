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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.repo.JDBCRecordHandler;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.vivoweb.ingest.util.repo.JenaRecordHandler;
import org.vivoweb.ingest.util.repo.MapRecordHandler;
import org.vivoweb.ingest.util.repo.Record;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.RecordMetaData;
import org.vivoweb.ingest.util.repo.TextFileRecordHandler;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class RecordHandlerTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(RecordHandlerTest.class);
	
	/**
	 * 
	 */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestRH").createFolder();
		this.rh = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		ArrayList<String> ids = new ArrayList<String>();
		//Get list of record ids
		for (Record r : this.rh) {
			/*
			 * Do not do this:
			 * this.rh.delRecord(r.getID());
			 * since that will generate ConcurrentModificationException
			 */
			ids.add(r.getID());
		}
		//Delete records for all ids
		for (String id : ids) {
			this.rh.delRecord(id);
		}
		this.rh.close();
		//Delete the testing folder
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestRH").delete(new AllFileSelector());
	}
	
	/**
	 * 
	 */
	public void testJDBCAddRecord() {
		log.info("BEGIN JDBCRH Test");
		try {
			this.rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:XMLVault/TestRH/JDBC/h2", "sa", "", "testdb", "data");
			runBattery();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		log.info("END JDBCRH Test");
	}
	
	/**
	 * 
	 */
	public void testTextFileAddRecord() {
		log.info("BEGIN TFRH Test");
		try {
			this.rh = new TextFileRecordHandler("XMLVault/TestRH/TextFile");
			runBattery();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		log.info("END TFRH Test");
	}
	
	/**
	 * 
	 */
	public void testMapAddRecord() {
		log.info("BEGIN MapRH Test");
		try {
			this.rh = new MapRecordHandler();
			runBattery();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		log.info("END MapRH Test");
	}
	
	/**
	 * 
	 */
	public void testJenaAddRecord() {
		log.info("BEGIN JenaRH Test");
		try {
			this.rh = new JenaRecordHandler(new JenaConnect("jdbc:h2:XMLVault/TestRH/Jena/h2;MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver"), "http://localhost/jenarecordhandlerdemo#data");
			runBattery();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		log.info("END JenaRH Test");
	}
	
	/**
	 * @throws IOException error
	 */
	private void runBattery() throws IOException {
		runAddRecord();
		runNoModRecord();
		runModRecord();
		runDelRecord();
	}
	
	/**
	 * @throws IOException error
	 */
	private void runAddRecord() throws IOException {
		log.info("Start adding test");
		String recID = "test1";
		String recData = "MyDataIsReally Awesome";
		String recDataMD5 = RecordMetaData.md5hex(recData);
		assertTrue(this.rh.addRecord(recID, recData, this.getClass()));
		Record r = this.rh.getRecord(recID);
		log.info("Record Data: "+r.getData());
		String rDataMD5 = RecordMetaData.md5hex(r.getData());
		assertEquals(recData.trim(), r.getData().trim());
		assertEquals(recDataMD5, rDataMD5);
		log.info("End adding test");
	}
	
	/**
	 * @throws IOException error
	 */
	private void runNoModRecord() throws IOException {
		log.info("Start no mod test");
		String recID = "test1";
		String recData = "MyDataIsReally Awesome";
		assertFalse(this.rh.addRecord(recID, recData, this.getClass()));
		log.info("End no mod test");
	}
	
	/**
	 * @throws IOException error
	 */
	private void runModRecord() throws IOException {
		log.info("Start mod test");
		String recID = "test1";
		String recData = "MyDataIsReally Awesome - Again!";
		assertTrue(this.rh.addRecord(recID, recData, this.getClass()));
		log.info("End mod test");
	}
	
	/**
	 * @throws IOException error
	 */
	private void runDelRecord() throws IOException {
		log.info("Start del test");
		String recID = "test1";
		this.rh.delRecord(recID);
		try {
			this.rh.getRecord(recID);
			fail("Illegal Record ID Request Should Throw IllegalArgumentException!");
		} catch(IllegalArgumentException e) {
			//ignore since this is the expected behavior
		}
		log.info("End del test");
	}
	
}
