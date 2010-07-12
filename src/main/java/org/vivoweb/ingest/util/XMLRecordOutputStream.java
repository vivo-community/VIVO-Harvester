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
package org.vivoweb.ingest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.fetch.PubmedSOAPFetch;
import org.xml.sax.SAXException;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class XMLRecordOutputStream extends OutputStream {
	private static Log log = LogFactory.getLog(XMLRecordOutputStream.class);
	private ByteArrayOutputStream buf;
	private RecordHandler rh;
	private byte[] closeTag;
	private Pattern idRegex;
	private String header;
	private String footer;
	
	/**
	 * Constructor
	 * @param tagToSplitOn defines the record tag type
	 * @param headerInfo prepended to each record
	 * @param footerInfo appended to each record
	 * @param idLocationRegex regex to find the data to be used as ID
	 * @param recordHandler RecordHandler to write records to
	 */
	public XMLRecordOutputStream(String tagToSplitOn, String headerInfo, String footerInfo, String idLocationRegex, RecordHandler recordHandler) {
		this.buf = new ByteArrayOutputStream();
		this.rh = recordHandler;
		this.idRegex = Pattern.compile(idLocationRegex);
		this.closeTag = ("</"+tagToSplitOn+">").getBytes();
		this.header = headerInfo;
		this.footer = footerInfo;
	}

	@Override
	public void write(int arg0) throws IOException {
		this.buf.write(arg0);
		byte[] a = this.buf.toByteArray();
		if(compareByteArrays(a, this.closeTag)) {
//			log.debug("Complete Record Written to buffer");
			String record = new String(a);
			Matcher m = this.idRegex.matcher(record);
			m.find();
			String id = m.group(1);
//			log.debug("Adding record id: "+id);
			this.rh.addRecord(id.trim(), this.header+record.trim()+this.footer);
			this.buf.reset();
		}
	}
	
	private boolean compareByteArrays(byte[] a, byte[] b) {
		if(a.length < b.length) {
			return false;
		}
		int o = a.length-b.length;
		for(int i = 0; i < b.length; i++) {
			if(a[o+i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param args commandline arguments
	 * @throws ParserConfigurationException xml parse error 
	 * @throws SAXException xml parse error
	 * @throws IOException error connecting
	 */
	public static void main(String... args) throws ParserConfigurationException, SAXException, IOException {
		RecordHandler dataStore;
//		dataStore = RecordHandler.parseConfig("config/PubmedJenaRecordHandler.xml");
//		dataStore = RecordHandler.parseConfig("config/PubmedRDFRecordHandler.xml");
		dataStore = RecordHandler.parseConfig("config/PubmedXMLRecordHandler.xml");
//		dataStore = new MapRecordHandler();
//		dataStore = new JDBCRecordHandler("com.mysql.jdbc.Driver", "mysql", "127.0.0.1", "3306", "jdbcrecordstore", "jdbcRecordStore", "5j63ucbNdZ5MCRda", "recordTable");
//		dataStore = new JenaRecordHandler("com.mysql.jdbc.Driver", "mysql", "127.0.0.1", "3306", "jenarecordstore", "jenaRecordStore", "j6QvzjGG5muJmYN4", "MySQL", "http://localhost/jenarecordhandlerdemo#data");
//		dataStore = new JenaRecordHandler("config/JenaModel.xml", "http://localhost/jenarecordhandlerdemo#data");
//		dataStore = new TextFileRecordHandler("XMLVault");
//		dataStore = new TextFileRecordHandler("ftp://yourMom:y0urM0m123@127.0.0.1:21/path/to/dir");
		XMLRecordOutputStream os = new XMLRecordOutputStream("PubmedArticle", "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<PMID>(.*?)</PMID>.*?", dataStore);
		PubmedSOAPFetch f = new PubmedSOAPFetch("hainesc@ctrip.ufl.edu", "University of Florid", os);
		f.fetchPubMedEnv(f.ESearchEnv(f.fetchAll(), new Integer(5)));
//		LinkedList<String> ids = new LinkedList<String>();
//		for(Record r : dataStore) {
//			System.out.println("========================================================");
//			System.out.println(r.getID());
//			System.out.println("--------------------------------------------------------");
//			System.out.println(r.getData());
//			System.out.println("========================================================\n");
//			ids.add(r.getID());
//		}
//		for(String id : ids) {
//			dataStore.delRecord(id);
//		}
	}
}
