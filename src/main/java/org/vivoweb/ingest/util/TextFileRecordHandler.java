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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
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
	}
	
	@Override
	public void setParams(Map<String,String> params) throws IllegalArgumentException, IOException {
		setFileDirObj(getParam(params,"fileDir",true));
	}
	
	@Override
	public void addRecord(Record rec, boolean overwrite) throws IOException {
		log.debug("Resolving file for record: " + rec.getID());
		FileObject fo = this.fileDirObj.resolveFile(rec.getID());
		if(!overwrite && fo.exists()) {
			throw new IOException("Failed to add record "+rec.getID()+" because file "+fo.getName().getFriendlyURI()+" already exists.");
		}
		fo.createFile();
		if(!fo.isWriteable()) {
			throw new IOException("Insufficient file system privileges to add record "+rec.getID()+" to file "+fo.getName().getFriendlyURI());
		}
		log.debug("Writting data for record: "+rec.getID());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fo.getContent().getOutputStream(false)));
		bw.append(rec.getData());
		bw.close();
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
						log.debug("Found file "+file.getName().getBaseName());
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
	
}
