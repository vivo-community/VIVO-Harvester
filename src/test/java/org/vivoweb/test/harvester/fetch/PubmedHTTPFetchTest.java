/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.fetch;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.PubmedHTTPFetch;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author James Pence (jrpence@ctrip.ufl.edu)
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class PubmedHTTPFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(PubmedHTTPFetchTest.class);
	/** */
	private File configFile;
	/** */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(PubmedHTTPFetchTest.class);
		this.configFile = File.createTempFile("rhConfig", "xml");
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.configFile));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<RecordHandler type=\"org.vivoweb.harvester.util.repo.JDBCRecordHandler\">\n	<Param name=\"dbClass\">org.h2.Driver</Param>\n	<Param name=\"dbUrl\">jdbc:h2:mem:TestPMSFetchRH</Param>\n	<Param name=\"dbUser\">sa</Param>\n	<Param name=\"dbPass\"></Param>\n	<Param name=\"dbTable\">recordTable</Param>\n	<Param name=\"dataFieldName\">dataField</Param>\n</RecordHandler>");
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
	 * Test method for {@link org.vivoweb.harvester.fetch.PubmedHTTPFetch#main(java.lang.String[]) main(String... args)}.
	 * @throws IOException error
	 * @throws ParserConfigurationException error 
	 * @throws SAXException error
	 */
	public final void testPubmedHTTPFetchMain() throws IOException, ParserConfigurationException, SAXException {
		log.info("BEGIN testPubmedHTTPFetchMain");
		this.rh = RecordHandler.parseConfig(this.configFile.getAbsolutePath());
		
		//test 1 record
		new PubmedHTTPFetch(new String[]{"-m", "test@test.com", "-t", "1:8000[dp]", "-n", "5", "-b", "1", "-o", this.configFile.getAbsolutePath()}).execute();
		assertTrue(this.rh.iterator().hasNext());
		DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		for(Record r : this.rh) {
			Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
			Element elem = doc.getDocumentElement();
			traverseNodes(elem.getChildNodes());
		}
		log.info("END testPubmedHTTPFetchMain");
	}
	
	/**
	 * @param nodeList the nodes
	 */
	private void traverseNodes(NodeList nodeList) {
		for(int x = 0; x < nodeList.getLength(); x++) {
			Node child = nodeList.item(x);
			String name = child.getNodeName();
			if(!name.contains("#text")) {
				log.trace(name);
				traverseNodes(child.getChildNodes());
			}
		}
	}
}
