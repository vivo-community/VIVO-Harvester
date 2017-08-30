/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.test.harvester.fetch.nih;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.nih.PubmedFetch;
import org.vivoweb.harvester.fetch.nih.ScopusFetch;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.JenaRecordHandler;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Eliza Chan (elc2013@med.cornell.edu)
 */
public class ScopusFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ScopusFetchTest.class);
	/** */
	private RecordHandler rh;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
		this.rh = new JenaRecordHandler(new MemJenaConnect(), "http://vivoweb.org/harvester/test/datatype");
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(this.rh != null) {
			this.rh.close();
		}
	}
	
	/**
	 * Test method for constructing query string with publication years
	 * @throws IOException error
	 */
	public final void testConstructPubYearQStr() throws IOException {
		log.info("BEGIN testConstructPubYearQStr");
		try {
			ScopusFetch s = new ScopusFetch("test@test.com", "", "0", "0", this.rh);
			String startYear = "1980";
			String endYear = "1985";
			log.info("Test construct query String from year " + startYear + " to " + "year " + endYear);
			String testQStr = s.constructPubYearQStr(startYear, endYear);
			String expectedQStr = "PUBYEAR+IS+1980+OR+PUBYEAR+IS+1981+OR+PUBYEAR+IS+1982+OR+" +
					"PUBYEAR+IS+1983+OR+PUBYEAR+IS+1984+OR+PUBYEAR+IS+1985";
			log.info("Test query String: " + testQStr);
			log.info("Expected query String: " + expectedQStr);
			assertEquals(testQStr, expectedQStr);
		} catch(Exception e) {
			log.info(e.toString());
		}
		log.info("END testConstructPubYearQStr");
	}

	/**
	 * Test method for constructing complete query string with count and start params
	 * @throws IOException error
	 */
	public final void testConstructCompleteQStr() throws IOException {
		log.info("BEGIN testConstructCompleteQStr");
		try {
			ScopusFetch s = new ScopusFetch("test@test.com", "", "0", "0", this.rh);
			int start = 0;
			int count = 200;
			int totalResults = 250;
			log.info("Test construct query String list for a total of " + totalResults + " articles");
			ArrayList<String> testQStrList = s.constructCompleteQStr("", count, start, totalResults);
			String[] expectedQStrList = 
				{
					"&count=" + String.valueOf(count) + "&start=" + String.valueOf(start) + "&view=COMPLETE", 
					"&count=" + String.valueOf(count) + "&start=" + String.valueOf(start+count) + "&view=COMPLETE"
				};
			int c = 0;
			for (String queryCompleteStr: testQStrList) {
				log.info("Test query String: " + queryCompleteStr);
				log.info("Expected query String: " + expectedQStrList[c]);
				assertEquals(queryCompleteStr, expectedQStrList[c]);
				c++;
			}

		} catch(Exception e) {
			log.info(e.toString());
		}
		log.info("END testConstructCompleteQStr");
	}

}
