/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.repo.RecordMetaData.RecordMetaDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Record Handler that stores each record as a file in a directory
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class TextFileRecordHandler extends RecordHandler {
	/**
	 * SLF4J Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(TextFileRecordHandler.class);
	/**
	 * The directory to store record files in
	 */
	protected String fileDir;
	/**
	 * The directory to store record metadata files in
	 */
	private String metaDir;
	
	/**
	 * Default Constructor
	 */
	protected TextFileRecordHandler() {
		// Nothing to do here
		// Used by config construction
		// Should only be used in conjuction with setParams()
	}
	
	/**
	 * Constructor
	 * @param fileDir directory to store records in
	 * @throws IOException error accessing directory
	 */
	public TextFileRecordHandler(String fileDir) throws IOException {
		setFileDirObj(fileDir);
	}
	
	/**
	 * Setter for fileDir
	 * @param fileDir the directory path String
	 * @throws IOException unable to connect
	 */
	private void setFileDirObj(String fileDir) throws IOException {
		if(!FileAide.exists(fileDir)) {
			log.debug("Directory '" + fileDir + "' Does Not Exist, attempting to create");
			FileAide.createFolder(fileDir);
		}
		this.fileDir = fileDir;
		this.metaDir = fileDir+"/.metadata";
		if(!FileAide.exists(this.metaDir)) {
			log.debug("Directory '" + fileDir + "/.metadata' Does Not Exist, attempting to create");
			FileAide.createFolder(this.metaDir);
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		setFileDirObj(getParam(params, "fileDir", true));
	}
	
	/**
	 * Sanitizes a record id
	 * @param id the record id
	 * @return null if no sanitization needed, else the new id
	 */
	private String sanitizeID(String id) {
		String urlDecodedId = "";
		try {
			urlDecodedId = java.net.URLDecoder.decode(id, java.nio.charset.StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			// not going to happen - value came from JDK's own StandardCharsets
		}
		String s = urlDecodedId.replaceAll("\\n", "_-_NEWLINE_-_").replaceAll("\\r", "_-_RETURN_-_").replaceAll("\\t", "_-_TAB_-_").replaceAll(" ", "_-_SPACE_-_").replaceAll("\\\\", "_-_BACKSLASH_-_").replaceAll("/", "_-_FORWARDSLASH_-_").replaceAll(":", "_-_COLON_-_").replaceAll("\\*", "_-_STAR_-_").replaceAll("\\?", "_-_QUESTIONMARK_-_").replaceAll("\"", "_-_DOUBLEQUOTE_-_").replaceAll("<", "_-_LESSTHAN_-_").replaceAll(">", "_-_GREATERTHAN_-_").replaceAll("\\|", "_-_PIPE_-_").replaceAll("\\.", "_-_DOT_-_");
		if(s.equals(id)) {
			return null;
		}
		log.debug("record id sanitized from '" + id + "' to '" + s + "'");
		return s;
	}
	
	@Override
	public boolean addRecord(Record rec, Class<?> operator, boolean overwrite) throws IOException {
		String newID = sanitizeID(rec.getID());
		Record cleanRec;
		if(newID == null) {
			cleanRec = rec;
		} else {
			cleanRec = new Record(newID, rec.getData(), this);
		}
		if(!needsUpdated(cleanRec)) {
			return false;
		}
		// log.debug("Resolving file for record: " + cleanRec.getID());
		String fo = this.fileDir+"/"+cleanRec.getID();
		FileAide.setTextContent(fo, cleanRec.getData(), overwrite);
		createMetaDataFile(cleanRec.getID());
		setWritten(cleanRec, operator);
		return true;
	}
	
	/**
	 * Creates the metadata file for a given record
	 * @param recID the record id
	 * @throws IOException error writing metadata file
	 */
	private void createMetaDataFile(String recID) throws IOException {
		String fmo = null;
		BufferedWriter bw = null;
		try {
			fmo = this.metaDir + "/" + recID;
			FileAide.createFile(fmo);
			
			bw = new BufferedWriter(new OutputStreamWriter(FileAide.getOutputStream(fmo)));
			bw.append("<MetaDataRecordList>\n");
			// bw.append("  <Date>"+rmd.getDate().getTimeInMillis()+"</Date>\n");
			// bw.append("  <Operation>"+rmd.getOperation()+"</Operation>\n");
			// bw.append("  <Operator>"+rmd.getOperator().getName()+"</Operator>\n");
			// bw.append("  <MD5>"+rmd.getMD5()+"</MD5>\n");
			bw.append("</MetaDataRecordList>\n");
			bw.close();
		} catch(IOException e) {
			String error = "Error creating metadata for record " + recID + " at file " + fmo;
			if(bw != null) {
				try {
					bw.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
			throw new IOException(error, e);
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
		}
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		String fo = null;
		fo = this.fileDir+"/"+recID;
		if(!FileAide.exists(fo)) {
			log.warn("Attempted to delete record " + recID + ", but file " + fo + " did not exist.");
		} else if(!FileAide.isWriteable(fo)) {
			throw new IOException("Insufficient file system privileges to delete record " + recID + " from file " + fo);
		} else if(!FileAide.delete(fo)) {
			throw new IOException("Failed to delete record " + recID + " from file " + fo);
		}
		delMetaData(recID);
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		String fo = null;
		BufferedReader br = null;
		try {
			StringBuilder sb = new StringBuilder();
			fo = this.fileDir+"/"+recID;
			if(!FileAide.exists(fo)) {
				throw new IllegalArgumentException("Record " + recID + " does not exist!");
			}
			br = new BufferedReader(new InputStreamReader(FileAide.getInputStream(fo)));
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			br.close();
			return sb.toString().trim();
		} catch(IOException e) {
			if(br != null) {
				try {
					br.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
			throw e;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
		}
	}
	
	@Override
	protected void delMetaData(String recID) throws IOException {
		String fmo = this.metaDir+"/"+recID;
		if(!FileAide.exists(fmo)) {
			log.warn("Attempted to delete record " + recID + " metadata, but file " + fmo + " did not exist.");
		} else if(!FileAide.isWriteable(fmo)) {
			throw new IOException("Insufficient file system privileges to delete record " + recID + " metadata from file " + fmo);
		} else if(!FileAide.delete(fmo)) {
			throw new IOException("Failed to delete record " + recID + " metadata from file " + fmo);
		}
	}
	
	@Override
	protected void addMetaData(Record rec, RecordMetaData rmd) throws IOException {
		String fmo = this.metaDir+"/"+rec.getID();
		if(!FileAide.exists(fmo)) {
			log.debug("Attempted to add record " + rec.getID() + " metadata, but file " + fmo + " did not exist. Initializing record metadata.");
			createMetaDataFile(rec.getID());
		} else if(!FileAide.isWriteable(fmo)) {
			throw new IOException("Insufficient file system privileges to modify record " + rec.getID() + " metadata from file " + fmo);
		}
		
		DocumentBuilder db;
		Document doc;
		InputStream is = null;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			is = FileAide.getInputStream(fmo);
			doc = db.parse(is);
			is.close();
		} catch(Exception e) {
			if(is != null) {
				is.close();
			}
			throw new IOException(e);
		} finally {
			if(is != null) {
				is.close();
			}
		}
		Element rootNode = doc.getDocumentElement();
		Element newNode = doc.createElement("MetaDataRecord");
		Element dateNode = doc.createElement("Date");
		dateNode.appendChild(doc.createTextNode(rmd.getDate().getTimeInMillis() + ""));
		newNode.appendChild(dateNode);
		// Start Remove Here
		Element dateNodeReadable = doc.createElement("DateReadable");
		dateNodeReadable.appendChild(doc.createTextNode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(rmd.getDate().getTime())));
		newNode.appendChild(dateNodeReadable);
		// Stop Remove Here
		Element operationNode = doc.createElement("Operation");
		operationNode.appendChild(doc.createTextNode(rmd.getOperation().toString()));
		newNode.appendChild(operationNode);
		Element operatorNode = doc.createElement("Operator");
		operatorNode.appendChild(doc.createTextNode(rmd.getOperator().getName()));
		newNode.appendChild(operatorNode);
		Element md5Node = doc.createElement("MD5");
		md5Node.appendChild(doc.createTextNode(rmd.getMD5()));
		newNode.appendChild(md5Node);
		rootNode.appendChild(newNode);
		
		OutputStream os = null;
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			os = FileAide.getOutputStream(fmo);
			trans.transform(new DOMSource(doc), new StreamResult(os));
		} catch(Exception e) {
			if(os != null) {
				os.close();
			}
			throw new IOException(e);
		} finally {
			if(os != null) {
				os.close();
			}
		}
		
		// OutputStream os = FileAide.getOutputStream(fmo);
		// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		// bw.append("<MetaDataRecord>\n");
		// bw.append("  <Date>"+rmd.getDate().getTimeInMillis()+"</Date>\n");
		// bw.append("  <Operation>"+rmd.getOperation()+"</Operation>\n");
		// bw.append("  <Operator>"+rmd.getOperator().getName()+"</Operator>\n");
		// bw.append("  <MD5>"+rmd.getMD5()+"</MD5>\n");
		// bw.append("</MetaDataRecord>\n");
		// bw.close();
		// os.close();
	}
	
	@Override
	protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
		try {
			String fmo = this.metaDir+"/"+recID;
			if(!FileAide.exists(fmo)) {
				throw new IOException("Attempted to retrieve record " + recID + " metadata, but file " + fmo + " does not exist");
			} else if(!FileAide.isReadable(fmo)) {
				throw new IOException("Insufficient file system privileges to read record " + recID + " metadata from file " + fmo);
			}
			return new TextFileMetaDataParser().parseMetaData(fmo);
		} catch(ParserConfigurationException e) {
			throw new IOException(e);
		} catch(SAXException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public Iterator<Record> iterator() {
		return new TextFileRecordIterator();
	}
	
	/**
	 * Iterator for TextFileRecordHandler
	 * @author cah
	 */
	private class TextFileRecordIterator implements Iterator<Record> {
		/**
		 * Iterator over the files
		 */
		Iterator<String> fileNameIter;
		
		/**
		 * Default Constructor
		 */
		protected TextFileRecordIterator() {
			Set<String> allFileListing = new TreeSet<String>();
			log.debug("Compiling list of records");
			try {
				allFileListing.addAll(FileAide.getNonHiddenChildren(TextFileRecordHandler.this.fileDir));
			} catch(IOException e) {
				log.error(e.getMessage());
				log.debug("Stacktrace:",e);
			}
			this.fileNameIter = allFileListing.iterator();
			log.debug("List compiled");
		}
		
		@Override
		public boolean hasNext() {
			return this.fileNameIter.hasNext();
		}
		
		@Override
		public Record next() {
			try {
				return getRecord(this.fileNameIter.next());
			} catch(IOException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * MetaData File Parser for TextFileRecordHandlers
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private static class TextFileMetaDataParser extends DefaultHandler {
		/**
		 * The RecordHandler we are building
		 */
		private SortedSet<RecordMetaData> rmdSet;
		/**
		 * The date of the current rmd
		 */
		private Calendar tempDate;
		/**
		 * Class name of the operator for current rmd
		 */
		private Class<?> tempOperator;
		/**
		 * The rmdType of current rmd
		 */
		private RecordMetaDataType tempOperation;
		/**
		 * The md5hash of current rmd
		 */
		private String tempMD5;
		/**
		 * The value of the current cdata
		 */
		private String tempVal;
		
		/**
		 * Default Constructor
		 */
		protected TextFileMetaDataParser() {
			this.rmdSet = new TreeSet<RecordMetaData>();
			this.tempDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
			this.tempDate.setTimeInMillis(0);
			this.tempOperation = RecordMetaDataType.error;
			this.tempOperator = IllegalArgumentException.class;
		}
		
		/**
		 * Parses the metadata file and return sortedset of recordmetadata
		 * @param fmo the fileobject of the metadata file
		 * @return sortedset of recordmetadata
		 * @throws ParserConfigurationException parser configured incorrectly
		 * @throws SAXException error parsing xml
		 * @throws IOException error reading xml
		 */
		protected SortedSet<RecordMetaData> parseMetaData(String fmo) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(FileAide.getInputStream(fmo), this); // parse the file and also register this class for call
			// backs
			return this.rmdSet;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase("MetaDataRecord")) {
				this.tempDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
				this.tempDate.setTimeInMillis(0);
				this.tempOperation = RecordMetaDataType.error;
				this.tempOperator = IllegalArgumentException.class;
				this.tempMD5 = "";
			} else if(qName.equalsIgnoreCase("Date")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("Operation")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("Operator")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("MD5")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("MetaDataRecordList")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("DateReadable")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal = new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("MetaDataRecord")) {
				RecordMetaData rmd = new RecordMetaData(this.tempDate, this.tempOperator, this.tempOperation, this.tempMD5);
				this.rmdSet.add(rmd);
			} else if(qName.equalsIgnoreCase("Date")) {
				this.tempDate.setTimeInMillis(Long.parseLong(this.tempVal));
			} else if(qName.equalsIgnoreCase("Operation")) {
				this.tempOperation = RecordMetaDataType.valueOf(this.tempVal);
			} else if(qName.equalsIgnoreCase("Operator")) {
				try {
					this.tempOperator = Class.forName(this.tempVal);
				} catch(ClassNotFoundException e) {
					throw new SAXException(e.getMessage(), e);
				}
			} else if(qName.equalsIgnoreCase("MD5")) {
				this.tempMD5 = this.tempVal;
			} else if(qName.equalsIgnoreCase("MetaDataRecordList")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("DateReadable")) {
				// Do Nothing, but don't remove so it doesnt go to else clause
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		// Do nothing
	}
	
	@Override
	public Set<String> find(String idText) {
		Set<String> retVal = new TreeSet<String>();
		for(Record r : this) {
			if(r.getID().contains(idText)) {
				retVal.add(r.getID());
			}
		}
		return retVal;
	}
}
