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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains a metadata statement about a record
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class RecordMetaData implements Comparable<RecordMetaData> {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(RecordMetaData.class);
	/**
	 * The date the operation was performed
	 */
	private Calendar date;
	/**
	 * The class that performed the operation
	 */
	private Class<?> operator;
	/**
	 * The operation that was performed
	 */
	private RecordMetaDataType operation;
	/**
	 * The md5 hash of the data
	 */
	private String md5hash;
	
	/**
	 * Constructor
	 * @param operationDate The date the operation was performed
	 * @param operatorClass The class that performed the operation
	 * @param operationType The operation that was performed
	 * @param md5 md5hash of the data
	 */
	protected RecordMetaData(Calendar operationDate, Class<?> operatorClass, RecordMetaDataType operationType, String md5) {
		this.date = operationDate;
		this.operator = operatorClass;
		this.operation = operationType;
		this.md5hash = md5;
	}
	
	/**
	 * Constructor (with timestamp = now)
	 * @param operatorClass The class that performed the operation
	 * @param operationType The operation that was performed
	 * @param md5 md5hash of the data
	 */
	protected RecordMetaData(Class<?> operatorClass, RecordMetaDataType operationType, String md5) {
		this(Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US), operatorClass, operationType, md5);
	}
	
	/**
	 * Getter for date
	 * @return the date
	 */
	public Calendar getDate() {
		return this.date;
	}
	
	/**
	 * Getter for operator
	 * @return the operator
	 */
	public Class<?> getOperator() {
		return this.operator;
	}
	
	/**
	 * Getter for operation
	 * @return the operation
	 */
	public RecordMetaDataType getOperation() {
		return this.operation;
	}
	
	/**
	 * Getter for md5hash
	 * @return the md5 hash
	 */
	public String getMD5() {
		return this.md5hash;
	}
	
	/**
	 * Defines the type of Record MetaData Types
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	public static enum RecordMetaDataType {
		/**
		 * Data was written
		 */
		written(),
		/**
		 * Data was processed
		 */
		processed(),
		/**
		 * Something went wrong! Should never be used!
		 */
		error();
		
		@Override
		public String toString() {
			return this.name();
		}
	}
	
	@Override
	public int compareTo(RecordMetaData o) {
		return (this.date.compareTo(o.date)*-1);
	}
	
	/**
	 * Make a md5hash of a string
	 * @param text the text to md5
	 * @return the md5 hash
	 */
	public static String makeMD5Hash(String text) {
		String retVal = null;
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] data = text.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			retVal = String.format("%1$032X", i);
		} catch(NoSuchAlgorithmException e) {
			log.fatal(e.getMessage(),e);
		}
		return retVal;
	}
}
