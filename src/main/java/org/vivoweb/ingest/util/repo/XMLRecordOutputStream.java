/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.util.repo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An Output Stream that breaks XML blobs into individual Records and writes to a RecordHandler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class XMLRecordOutputStream extends OutputStream {
	/**
	 * Log4J Logger
	 */
	@SuppressWarnings("unused")
	// log lines are commented because it slowed down the process significantly
	private static Log log = LogFactory.getLog(XMLRecordOutputStream.class);
	/**
	 * Buffer to hold data until a complete record is formed
	 */
	private ByteArrayOutputStream buf;
	/**
	 * RecordHandler to write record to
	 */
	private RecordHandler rh;
	/**
	 * the byte array that represent a closing record tag
	 */
	private byte[] closeTag;
	/**
	 * Regex to find the identifing data in the record data
	 */
	private Pattern idRegex;
	/**
	 * Prepend to each record
	 */
	private String header;
	/**
	 * Append to each record
	 */
	private String footer;
	/**
	 * The class writing records
	 */
	private Class<?> opClass;
	
	/**
	 * Constructor
	 * @param tagToSplitOn defines the record tag type
	 * @param headerInfo prepended to each record
	 * @param footerInfo appended to each record
	 * @param idLocationRegex regex to find the data to be used as ID
	 * @param recordHandler RecordHandler to write records to
	 * @param operator the class writing records
	 */
	public XMLRecordOutputStream(String tagToSplitOn, String headerInfo, String footerInfo, String idLocationRegex, RecordHandler recordHandler, Class<?> operator) {
		this.buf = new ByteArrayOutputStream();
		this.rh = recordHandler;
		this.idRegex = Pattern.compile(idLocationRegex);
		this.closeTag = ("</" + tagToSplitOn + ">").getBytes();
		this.header = headerInfo;
		this.footer = footerInfo;
		this.opClass = operator;
	}
	
	@Override
	public void write(int arg0) throws IOException {
		this.buf.write(arg0);
		byte[] a = this.buf.toByteArray();
		if(compareByteArrays(a, this.closeTag)) {
			// Slows things down ALOT to have these
			// log.debug("Complete Record Written to buffer");
			String record = new String(a);
			Matcher m = this.idRegex.matcher(record);
			m.find();
			String id = m.group(1);
			// Slows things down ALOT to have these
			// log.debug("Adding record id: "+id);
			this.rh.addRecord(id.trim(), this.header + record.trim() + this.footer, this.opClass);
			this.buf.reset();
		}
	}
	
	/**
	 * Compare two byte arrays
	 * @param arrayOne first to compare
	 * @param arrayTwo second to compare
	 * @return true if the last bytes in arrayOne is equivalent to arrayTwo, false otherwise
	 */
	private boolean compareByteArrays(byte[] arrayOne, byte[] arrayTwo) {
		if(arrayOne.length < arrayTwo.length) {
			return false;
		}
		int o = arrayOne.length - arrayTwo.length;
		for(int i = 0; i < arrayTwo.length; i++) {
			if(arrayOne[o + i] != arrayTwo[i]) {
				return false;
			}
		}
		return true;
	}
}
