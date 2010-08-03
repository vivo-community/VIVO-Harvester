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
package org.vivoweb.ingest.util.repo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.repo.RecordMetaData.RecordMetaDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class TextFileRecordHandler extends RecordHandler {
	/**
	 * Log4J Logger
	 */
	protected static Log log = LogFactory.getLog(TextFileRecordHandler.class);
	/**
	 * The directory to store record files in
	 */
	protected FileObject fileDirObj;
	/**
	 * The directory to store record metadata files in
	 */
	private FileObject metaDirObj;
	
	/**
	 * Default Constructor
	 */
	protected TextFileRecordHandler() {
		//Nothing to do here
		//Used by config construction
		//Should only be used in conjuction with setParams()
	}
	
	/**
	 * Constructor
	 * @param fileDir directory to store records in
	 * @throws IOException error accessing directory
	 * 
	 */
	public TextFileRecordHandler(String fileDir) throws IOException {
		setFileDirObj(fileDir);
	}
	
	/**
	 * Setter for fileDirObj
	 * @param fileDir the directory path String
	 * @throws IOException unable to connect
	 */
	private void setFileDirObj(String fileDir) throws IOException {
		FileSystemManager fsMan = VFS.getManager();
		this.fileDirObj = fsMan.resolveFile(new File("."),fileDir);
		if(!this.fileDirObj.exists()) {
			log.info("Directory '"+fileDir+"' Does Not Exist, attempting to create");
			this.fileDirObj.createFolder();
		}
		this.metaDirObj = fsMan.resolveFile(this.fileDirObj, ".metadata");
		if(!this.metaDirObj.exists()) {
			log.debug("Directory '"+fileDir+"/.metadata' Does Not Exist, attempting to create");
			this.metaDirObj.createFolder();
		}
	}
	
	@Override
	public void setParams(Map<String,String> params) throws IllegalArgumentException, IOException {
		setFileDirObj(getParam(params,"fileDir",true));
	}
	
	@Override
	public void addRecord(Record rec, Class<?> operator, boolean overwrite) throws IOException {
		try {
			if(!needsUpdated(rec)) {
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//log.debug("Resolving file for record: " + rec.getID());
		FileObject fo = this.fileDirObj.resolveFile(rec.getID());
		if(!overwrite && fo.exists()) {
			throw new IOException("Failed to add record "+rec.getID()+" because file "+fo.getName().getFriendlyURI()+" already exists.");
		}
		fo.createFile();
		if(!fo.isWriteable()) {
			throw new IOException("Insufficient file system privileges to add record "+rec.getID()+" to file "+fo.getName().getFriendlyURI());
		}
		//log.debug("Writting data for record: "+rec.getID());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fo.getContent().getOutputStream(false)));
		bw.append(rec.getData());
		bw.close();
		createMetaDataFile(rec.getID());
		setWritten(rec, operator);
	}
	
	/**
	 * Creates the metadata file for a given record
	 * @param recID the record id
	 * @throws IOException error writing metadata file
	 */
	private void createMetaDataFile(String recID) throws IOException {
		FileObject fmo = this.metaDirObj.resolveFile(recID);
		try {
			fmo.createFile();
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fmo.getContent().getOutputStream(false)));
			bw.append("<MetaDataRecordList>\n");
//			bw.append("  <Date>"+rmd.getDate().getTimeInMillis()+"</Date>\n");
//			bw.append("  <Operation>"+rmd.getOperation()+"</Operation>\n");
//			bw.append("  <Operator>"+rmd.getOperator().getName()+"</Operator>\n");
//			bw.append("  <MD5>"+rmd.getMD5()+"</MD5>\n");
			bw.append("</MetaDataRecordList>\n");
			bw.close();
		} catch(FileSystemException e) {
			throw new IOException("Error creating metadata for record "+recID+" at file "+fmo.getName().getFriendlyURI());
		}
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		FileObject fo = this.fileDirObj.resolveFile(recID);
		if(!fo.exists()) {
			log.warn("Attempted to delete record "+recID+", but file "+fo.getName().getFriendlyURI()+" did not exist.");
		} else if(!fo.isWriteable()) {
			throw new IOException("Insufficient file system privileges to delete record "+recID+" from file "+fo.getName().getFriendlyURI());
		} else if(!fo.delete()) {
			throw new IOException("Failed to delete record "+recID+" from file "+fo.getName().getFriendlyURI());
		}
		delMetaData(recID);
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		StringBuilder sb = new StringBuilder();
		FileObject fo = this.fileDirObj.resolveFile(recID);
		BufferedReader br = new BufferedReader(new InputStreamReader(fo.getContent().getInputStream()));
		String line;
		while((line = br.readLine()) != null){
			sb.append(line);
			sb.append("\n");
		}
		br.close();
		return sb.toString();
	}
	
	@Override
	protected void delMetaData(String recID) throws IOException {
		FileObject fmo = this.metaDirObj.resolveFile(recID);
		if(!fmo.exists()) {
			log.warn("Attempted to delete record "+recID+" metadata, but file "+fmo.getName().getFriendlyURI()+" did not exist.");
		} else if(!fmo.isWriteable()) {
			throw new IOException("Insufficient file system privileges to delete record "+recID+" metadata from file "+fmo.getName().getFriendlyURI());
		} else if(!fmo.delete()) {
			throw new IOException("Failed to delete record "+recID+" metadata from file "+fmo.getName().getFriendlyURI());
		}
	}
	
	@Override
	protected void addMetaData(Record rec, RecordMetaData rmd) throws IOException {
		FileObject fmo = this.metaDirObj.resolveFile(rec.getID());
		if(!fmo.exists()) {
			log.debug("Attempted to add record "+rec.getID()+" metadata, but file "+fmo.getName().getFriendlyURI()+" did not exist. Initializing record metadata.");
			createMetaDataFile(rec.getID());
		} else if(!fmo.isWriteable()) {
			throw new IOException("Insufficient file system privileges to modify record "+rec.getID()+" metadata from file "+fmo.getName().getFriendlyURI());
		}
		
		DocumentBuilder db;
		Document doc;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.parse(fmo.getContent().getInputStream());
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
		}
		Element rootNode = doc.getDocumentElement();
		Element newNode = doc.createElement("MetaDataRecord");
		Element dateNode = doc.createElement("Date");
		dateNode.appendChild(doc.createTextNode(rmd.getDate().getTimeInMillis()+""));
		newNode.appendChild(dateNode);
		//Start Remove Here
		Element dateNodeReadable = doc.createElement("DateReadable");
		dateNodeReadable.appendChild(doc.createTextNode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(rmd.getDate().getTime())));
		newNode.appendChild(dateNodeReadable);
		//Stop Remove Here
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
		
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.transform(new DOMSource(doc), new StreamResult(fmo.getContent().getOutputStream(false)));
		} catch(TransformerConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(TransformerFactoryConfigurationError e) {
			throw new IOException(e.getMessage(),e);
		} catch(TransformerException e) {
			throw new IOException(e.getMessage(),e);
		}
		
//		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fmo.getContent().getOutputStream(true)));
//		bw.append("<MetaDataRecord>\n");
//		bw.append("  <Date>"+rmd.getDate().getTimeInMillis()+"</Date>\n");
//		bw.append("  <Operation>"+rmd.getOperation()+"</Operation>\n");
//		bw.append("  <Operator>"+rmd.getOperator().getName()+"</Operator>\n");
//		bw.append("  <MD5>"+rmd.getMD5()+"</MD5>\n");
//		bw.append("</MetaDataRecord>\n");
//		bw.close();
	}
	
	@Override
	protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
		try {
			FileObject fmo = this.metaDirObj.resolveFile(recID);
			if(!fmo.exists()) {
				throw new IOException("Attempted to retrieve record "+recID+" metadata, but file "+fmo.getName().getFriendlyURI()+" does not exist");
			} else if(!fmo.isReadable()) {
				throw new IOException("Insufficient file system privileges to read record "+recID+" metadata from file "+fmo.getName().getFriendlyURI());
			}
			return new TextFileMetaDataParser().parseMetaData(fmo);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(),e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(),e);
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
		Iterator<FileObject> fileIter;
		
		/**
		 * Default Constructor
		 */
		protected TextFileRecordIterator() {
			LinkedList<FileObject> fileListing = new LinkedList<FileObject>();
			try {
				for(FileObject file : TextFileRecordHandler.this.fileDirObj.getChildren()) {
					if(!file.isHidden()) {
						fileListing.add(file);
							//log.debug("Found file "+file.getName().getBaseName());
					}
				}
			} catch(FileSystemException e) {
				log.error(e.getMessage(),e);
			}
			this.fileIter = fileListing.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return this.fileIter.hasNext();
		}
		
		@Override
		public Record next() {
			try {
				Record result = getRecord(this.fileIter.next().getName().getBaseName());
				return result;
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
		protected SortedSet<RecordMetaData> parseMetaData(FileObject fmo) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(fmo.getContent().getInputStream(), this); // parse the file and also register this class for call backs
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
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("Operation")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("Operator")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("MD5")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("MetaDataRecordList")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("DateReadable")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else {
				throw new SAXException("Unknown Tag: "+qName);
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
					throw new SAXException(e.getMessage(),e);
				}
			} else if(qName.equalsIgnoreCase("MD5")) {
				this.tempMD5 = this.tempVal;
			} else if(qName.equalsIgnoreCase("MetaDataRecordList")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else if(qName.equalsIgnoreCase("DateReadable")) {
				//Do Nothing, but don't remove so it doesnt go to else clause
			} else {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
	}
}
