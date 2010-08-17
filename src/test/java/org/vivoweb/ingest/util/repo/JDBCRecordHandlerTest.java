/**
 * 
 */
package org.vivoweb.ingest.util.repo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;
import junit.framework.TestCase;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class JDBCRecordHandlerTest extends TestCase {
	
	/**
	 * 
	 */
	private RecordHandler rh;
	/**
	 * 
	 */
	private String jdbcDriverClass = "org.h2.Driver";
	/**
	 * 
	 */
	private String connLine = "jdbc:h2:XMLVault/TestJDBC/h2";
	/**
	 * 
	 */
	private String username = "sa";
	/**
	 * 
	 */
	private String password = "";
	/**
	 * 
	 */
	private String tableName = "testdb";
	/**
	 * 
	 */
	private String dataFieldName = "data";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.rh = new JDBCRecordHandler(this.jdbcDriverClass, this.connLine, this.username, this.password, this.tableName, this.dataFieldName);
	}
	
	/**
	 * 
	 */
	public void testAddRecord() {
		try {
			String recID = "test1";
			String recData = "MyDataIsReally Awesome";
			String recDataMD5 = RecordMetaData.md5hex(recData);
			this.rh.addRecord(recID, recData, this.getClass());
			Record r = this.rh.getRecord(recID);
			String rDataMD5 = RecordMetaData.md5hex(r.getData());
			assertEquals(recData.trim(), r.getData().trim());
			assertEquals(recDataMD5, rDataMD5);
		} catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
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
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestJDBC").delete(new AllFileSelector());
	}
	
}
