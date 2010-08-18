/**
 * 
 */
package org.vivoweb.ingest.util.repo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class RecordHandlerTest extends TestCase {
	
	/**
	 * 
	 */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.rh = null;
	}
	
	/**
	 * 
	 */
	public void testJDBCAddRecord() {
		try {
			this.rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:XMLVault/TestRH/JDBC/h2", "sa", "", "testdb", "data");
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/**
	 * 
	 */
	public void testTextFileAddRecord() {
		try {
			this.rh = new TextFileRecordHandler("XMLVault/TestRH/TextFile");
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/**
	 * 
	 */
	public void testMapAddRecord() {
		try {
			this.rh = new MapRecordHandler();
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/**
	 * 
	 */
	public void testJenaAddRecord() {
		try {
			this.rh = new JenaRecordHandler(new JenaConnect("jdbc:h2:XMLVault/TestRH/Jena/h2", "sa", "", "MySQL", "org.h2.Driver").getJenaModel(), "dataField");
			runAddRecord();
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
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
		Thread.sleep(20000);
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestRH").delete(new AllFileSelector());
	}
	
}
