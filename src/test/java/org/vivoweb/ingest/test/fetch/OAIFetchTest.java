/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.test.fetch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; // import org.vivoweb.ingest.fetch.OAIFetch;
import org.vivoweb.ingest.util.repo.RecordHandler;

/**
 * @author Dale Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class OAIFetchTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(OAIFetchTest.class);
	/** */
	private RecordHandler rh;
	/** */
	private File configFile;
	
	@Override
	protected void setUp() throws Exception {
		this.configFile = File.createTempFile("oaiRHConfig", "xml");
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.configFile));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<RecordHandler type=\"org.vivoweb.ingest.util.repo.JDBCRecordHandler\">\n	<Param name=\"jdbcDriverClass\">org.h2.Driver</Param>\n	<Param name=\"connLine\">jdbc:h2:mem:TestOAIFetchRH</Param>\n	<Param name=\"username\">sa</Param>\n	<Param name=\"password\"></Param>\n	<Param name=\"tableName\">recordTable</Param>\n	<Param name=\"dataFieldName\">dataField</Param>\n</RecordHandler>");
		bw.close();
		this.rh = null;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.rh != null) {
			this.rh.close();
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.fetch.OAIFetch#main(java.lang.String[]) main(String... args)}.
	 */
	public final void testOAIFetchMain() {
		try {
			this.rh = RecordHandler.parseConfig(this.configFile.getAbsolutePath());
			// FIXME cah: fix this later
			// OAIFetch.main(new String[]{"-u", "archivesic.ccsd.cnrs.fr/oai/oai.php", "-s", "2000-01-01", "-e",
			// "2002-12-12", "-o", this.configFile.getAbsolutePath()});
			// assertTrue(this.rh.iterator().hasNext());
			// DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// for(Record r : this.rh) {
			// log.info("=====================================");
			// log.info(r.getData());
			// log.info("=====================================");
			// Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
			// Element elem = doc.getDocumentElement();
			// traverseNodes(elem.getChildNodes());
			// }
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}
	
	// /**
	// * @param nodeList the nodes
	// */
	// private void traverseNodes(NodeList nodeList) {
	// for(int x = 0; x < nodeList.getLength(); x++) {
	// Node child = nodeList.item(x);
	// String name = child.getNodeName();
	// if(!name.contains("#text")) {
	// log.info(name);
	// traverseNodes(child.getChildNodes());
	// }
	// }
	// }
}
