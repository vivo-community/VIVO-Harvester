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
package org.vivoweb.ingest.test.fetch;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.fetch.PubmedSOAPFetch;
import org.vivoweb.ingest.util.repo.Record;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.TextFileRecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Dale Scheppler (dscheppler@ctrip.ufl.edu)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class PubmedSOAPFetchTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(PubmedSOAPFetchTest.class);
	/** */private static String rhDir = "XMLVault/TestPSF";
	/** */private static RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		OutputStream os = VFS.getManager().resolveFile(new File("."), rhDir).resolveFile("test.xml").getContent().getOutputStream();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<RecordHandler type=\"org.vivoweb.ingest.util.repo.TextFileRecordHandler\">\n<Param name=\"fileDir\">"+rhDir+"/tfrh</Param>\n</RecordHandler>");
		bw.close();
		rh = null;
	}
	
	@Override
	protected void tearDown() throws Exception {

		if(rh != null) {
			rh.close();
		}
		VFS.getManager().resolveFile(new File("."), "XMLVault/TestPSF").delete(new AllFileSelector());
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.fetch.PubmedSOAPFetch#main(java.lang.String[])}.
	 */
	public final void testPubmedSOAPFetchMain() {
		Exception error = null;
		try {

			rh = new TextFileRecordHandler("XMLVault/TestPSF/tfrh");
			PubmedSOAPFetch.main(new String[]{"-m","test@test.com","-t","1:8000[dp]", "-l", "Planet Test", "-n", "1", "-b", "1", "-o", "XMLVault/TestPSF/test.xml"});
			assertTrue(rh.iterator().hasNext());
			DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for(Record r : rh) {
				Document doc = docB.parse(new ByteArrayInputStream(r.getData().getBytes()));
				Element elem = doc.getDocumentElement();
				traverseNodes(elem.getChildNodes());
			}
		} catch(Exception e) {
			error = e;
		} finally {
			if(error != null) {
				log.error(error.getMessage(),error);
				fail(error.getMessage());
			}
		}
	}
	
	/**
	 * @param nodeList the nodes
	 */
	private void traverseNodes(NodeList nodeList) {
		for(int x = 0; x < nodeList.getLength(); x++) {
			Node child = nodeList.item(x);
			String name = child.getNodeName();
			if(!name.contains("#text")) {
				log.info(name);
				traverseNodes(child.getChildNodes());
			}
		}
	}
}
