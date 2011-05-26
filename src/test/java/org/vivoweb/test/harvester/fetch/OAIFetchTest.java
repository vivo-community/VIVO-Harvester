/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.test.harvester.fetch;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.oai.OAIFetch;
import org.vivoweb.harvester.util.recordhandler.JDBCRecordHandler;
import org.vivoweb.harvester.util.recordhandler.Record;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Dale Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */

public class OAIFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(OAIFetchTest.class);
	/** */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
//		InitLog.initLogger(null, null);
		this.rh = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:mem:TestOAIFetchRH", "sa", "", "recordTable", "dataField");
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.rh != null) {
			this.rh.close();
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.fetch.oai.OAIFetch#execute() execute()}.
	 * @throws Exception boom
	 */
	public final void testOAIFetchMain() throws Exception {
		log.info("BEGIN testOAIFetchMain");
		new OAIFetch("archivesic.ccsd.cnrs.fr/oai/oai.php", "2000-01-01", "2002-12-12", this.rh).execute();
		assertTrue(this.rh.iterator().hasNext());
		DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		for(Record r : this.rh) {
//			log.info("=====================================");
//			log.info(r.getData());
//			log.info("=====================================");
			Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
			Element elem = doc.getDocumentElement();
			traverseNodes(elem.getChildNodes(), "");
		}
		log.info("END testOAIFetchMain");
	}
	
	/**
	 * @param nodeList the nodes
	 * @param indent ammount to indent
	 */
	private void traverseNodes(NodeList nodeList, String indent) {
		for(int x = 0; x < nodeList.getLength(); x++) {
			Node child = nodeList.item(x);
			String name = child.getNodeName();
			if(!name.contains("#text")) {
				log.info(indent+name);
				traverseNodes(child.getChildNodes(), indent+"  ");
			}
		}
	}
}
