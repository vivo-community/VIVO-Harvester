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
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.CSVtoRDF;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.MapRecordHandler;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author jrpence
 */
public class CSVtoRDFTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(CSVtoRDFTest.class);
	/** */
	private InputStream sourceStream;
	/** */
	private RecordHandler testRH;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String sourcefile ="\"NAME\",\"ID\"\n\"Burton, Joe D\",\"1\"\n\"Smith, Jane W\",\"2\"\n\"Schwarz, Elizabeth R\",\"3\"\n\"Silverstein, Steven\",\"4\"\n\"Schwarz, Elizabeth R\",\"3\"\n\"Silverstein, Steven\",\"4\"\n\"Schwarz, Elizabeth R\",\"3\"\n\"Silverstein, Steven\",\"4\"";
		this.sourceStream = new ByteArrayInputStream(sourcefile.getBytes());
		InitLog.initLogger(null, null);
//		this.testRH =  new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:mem:TestJDBCFetchRHDB", "sa", "", "recordTable", "dataField");

		this.testRH = new MapRecordHandler();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 */
	/*public void testCSVtoJDBCStreamConnectionString() {
		log.info("BEGIN testCSVtoJDBCStringConnectionString");
		new CSVtoRDF(this.sourceStream,testRH,nameSpace);
		log.info("END testCSVtoJDBCStringConnectionString");
	}*/
	
	/**
	 * @throws IOException if the file is missing or not read properly
	 * @throws SQLException if there are problems with accessing the database
	 * @throws ParserConfigurationException  issues with parsing the the config
	 * @throws SAXException If there is an issue with the xml or xsl
	 */
	public void testExecute() throws IOException, SQLException, ParserConfigurationException, SAXException {
		log.info("BEGIN testExecute");
		String nameSpace = "http://a.test.of.CSV2RDF/";
		CSVtoRDF subject = new CSVtoRDF(this.sourceStream,this.testRH,nameSpace);
		subject.execute();

		DocumentBuilder docC = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Iterator<Record> recIT = this.testRH.iterator();
		while(recIT.hasNext()){
			Record r = recIT.next();
			if(r.getData() == null) continue;
			java.lang.System.out.println("Record '" 
				+ r.getID() + "':\n\n" 
				+ r.getData());
			Document doc = docC.parse(new ByteArrayInputStream(r.getData().getBytes()));
			Element elem = doc.getDocumentElement();
			java.lang.System.out.println("\n");
			traverseNodes(elem.getChildNodes());
			java.lang.System.out.println("\n");
		}

		log.info("END testExecute");
	}
	

	/**
	 * @param nodeList the nodes
	 */
	private void traverseNodes(NodeList nodeList) {
		for(int x = 0; x < nodeList.getLength(); x++) {
			Node child = nodeList.item(x);
			String name = child.getNodeName();
			if(!name.contains("#text")) {
				java.lang.System.out.println(name);
				traverseNodes(child.getChildNodes());
			}
		}
	}
}
