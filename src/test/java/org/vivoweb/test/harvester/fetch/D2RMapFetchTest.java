/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.fetch;

import java.sql.Connection;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.D2RMapFetch;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JDBCRecordHandler;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * @author Eliza Chan (elc2013@med.cornell.edu)
 */
public class D2RMapFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(D2RMapFetchTest.class);
	/** */
	private Connection conn;
	/** */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(D2RMapFetchTest.class, null, null);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.conn != null) {
			this.conn.close();
		}
		if(this.rh != null) {
			this.rh.close();
		}
		this.conn = null;
		this.rh = null;
	}
	
	/**
	 * test d2rmat
	 * @throws Exception error
	 */
	public final void testD2RMapFetchInit() throws Exception {
		log.info("BEGIN testD2RMapFetchInit");
		this.rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:mem:TestD2RMapFetchRHDB", "sa", "", "recordTable", "dataField");
		D2RMapFetch fetch = new D2RMapFetch(this.rh);
		fetch.execute();
		assertTrue(fetch != null);
		log.info("END testD2RMapFetchInit");
	}
	

}
