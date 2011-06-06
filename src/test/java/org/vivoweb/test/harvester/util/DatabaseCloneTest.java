/**
 * 
 */
package org.vivoweb.test.harvester.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.vivoweb.harvester.util.DatabaseClone;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import junit.framework.TestCase;

/**
 * @author cah
 *
 */
public class DatabaseCloneTest extends TestCase {
	/** */
	private static String inputDriverClass = "org.h2.Driver";
	/** */
	private static String inputConnLine = "jdbc:h2:databaseclonetest-temp/input/store";
	/** */
	private static String inputUsername = "sa";
	/** */
	private static String inputPassword = "";
	/** */
	private Connection inputConn;
	/** */
	private static String outputDriverClass = "org.h2.Driver";
	/** */
	private static String outputConnLine = "jdbc:h2:databaseclonetest-temp/output/store";
	/** */
	private static String outputUsername = "sa";
	/** */
	private static String outputPassword = "";
	/** */
	private Connection outputConn;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
		Class.forName(inputDriverClass);
		this.inputConn = DriverManager.getConnection(inputConnLine, inputUsername, inputPassword);
		Class.forName(outputDriverClass);
		this.outputConn = DriverManager.getConnection(outputConnLine, outputUsername, outputPassword);
		
		Statement cursor = this.inputConn.createStatement();
		cursor.executeUpdate("" +
				"CREATE TABLE `department` (" +
				"  `dept_id` varchar(8) NOT NULL," +
				"  `dept_name` varchar(255) NOT NULL," +
				"  `type_id` int(3) NOT NULL," +
				"  `super_dept_id` varchar(8) NOT NULL," +
				"  PRIMARY KEY (`dept_id`)" +
				");" +
		"");
		cursor.executeUpdate("" +
				"INSERT INTO `department` (`dept_id`, `dept_name`, `type_id`, `super_dept_id`) VALUES" +
				"('12345678', 'Division of Medicine', 402, '00000000')," +
				"('87654321', 'Department of Research Informatics', 403, '12345678')," +
				"('09876543', 'Clinical and Translational Program', 405, '87654321')," +
				"('00000000', 'University of Sample Data', 401, '');" +
		"");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.DatabaseClone#execute() execute()}.
	 * @throws Exception error
	 */
	public final void testExecute() throws Exception {
		new DatabaseClone(this.inputConn, null, this.outputConn, null, null, null, null).execute();
		Statement cursor = this.outputConn.createStatement();
		ResultSet rs = cursor.executeQuery("SELECT * FROM department");
		assertTrue(rs.next());
		int count = 0;
		Set<String> ids = new HashSet<String>();
		ids.add("12345678");
		ids.add("87654321");
		ids.add("09876543");
		ids.add("00000000");
		
		do{
			assertTrue(ids.remove(rs.getString("dept_id")));
			count++;
		}while(rs.next());
		assertEquals(4, count);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.inputConn != null) {
			try {
				this.inputConn.createStatement().executeUpdate("DROP TABLE `department`");
			} catch(SQLException e) {
				// ignore
			}
			try {
				this.inputConn.close();
			} catch(SQLException e) {
				// ignore
			}
		}
		if(this.outputConn != null) {
			try {
				this.outputConn.createStatement().executeUpdate("DROP TABLE `department`");
			} catch(SQLException e) {
				// ignore
			}
			try {
				this.outputConn.close();
			} catch(SQLException e) {
				// ignore
			}
		}
		FileAide.delete("databaseclonetest-temp/");
	}
}
