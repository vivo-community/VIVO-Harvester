/**
 * 
 */
package org.vivoweb.ingest.util.repo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;

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
		super.setUp();
		this.rh = null;
	}
	
	/**
	 * 
	 */
	public void testJDBCAddRecord() {
		log.info("BEGIN JDBCRH Test");
		try {
			this.rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:XMLVault/TestRH/JDBC/h2", "sa", "", "testdb", "data");
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
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
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
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
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
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
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		log.info("END JenaRH Test");
	}
	
	/**
	 * @throws IOException error
	 */
	private void runAddRecord() throws IOException {
		String recID = "test1";
		String recData = "MyDataIsReally Awesome";
		String recDataMD5 = RecordMetaData.md5hex(recData);
		this.rh.addRecord(recID, recData, this.getClass());
		Record r = this.rh.getRecord(recID);
		log.info("Record Data: "+r.getData());
		String rDataMD5 = RecordMetaData.md5hex(r.getData());
		assertEquals(recData.trim(), r.getData().trim());
		assertEquals(recDataMD5, rDataMD5);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ArrayList<String> ids = new ArrayList<String>();
		for (Record r : this.rh) {
			ids.add(r.getID());
		}
		for (String id : ids) {
			this.rh.delRecord(id);
		}
		this.rh.close();
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestRH").delete(new AllFileSelector());
	}
	
}
