/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.recordhandler;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.MemJenaConnect;
import org.vivoweb.harvester.util.recordhandler.RecordMetaData.RecordMetaDataType;

/**
 * Record Handler Interface
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class RecordHandler implements Iterable<Record> {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RecordHandler.class);
	/**
	 * Do we overwrite existing records by default
	 */
	private boolean overwriteDefault = true;
	
	/**
	 * Adds a record to the RecordHandler
	 * @param rec record to add
	 * @param creator the creator
	 * @param overwrite when set to true, will automatically overwrite existing records
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public abstract boolean addRecord(Record rec, Class<?> creator, boolean overwrite) throws IOException;
	
	/**
	 * Adds a record to the RecordHandler
	 * @param recID record id to add
	 * @param recData record data to add
	 * @param creator the creator
	 * @param overwrite when set to true, will automatically overwrite existing records
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public boolean addRecord(String recID, String recData, Class<?> creator, boolean overwrite) throws IOException {
		return addRecord(new Record(recID, recData, this), creator, overwrite);
	}
	
	/**
	 * Adds a record to the RecordHandler If overwriteDefault is set to true, will automatically overwrite existing
	 * records
	 * @param rec record to add
	 * @param creator the creator
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public boolean addRecord(Record rec, Class<?> creator) throws IOException {
		return addRecord(rec, creator, isOverwriteDefault());
	}
	
	/**
	 * Adds a record to the RecordHandler If overwriteDefault is set to true, will automatically overwrite existing
	 * records
	 * @param recID record id to add
	 * @param recData record data to add
	 * @param creator the creator
	 * @return true if added, false if not needed (aka record already existed and was the same)
	 * @throws IOException error adding
	 */
	public boolean addRecord(String recID, String recData, Class<?> creator) throws IOException {
		return addRecord(new Record(recID, recData, this), creator);
	}
	
	/**
	 * Get a record
	 * @param recID record id to get
	 * @return record
	 * @throws IllegalArgumentException record not found
	 * @throws IOException error reading
	 */
	public Record getRecord(String recID) throws IllegalArgumentException, IOException {
		return new Record(recID, getRecordData(recID), this);
	}
	
	/**
	 * Retrieve the data for a given record
	 * @param recID id of record to retrieve
	 * @return data from record
	 * @throws IllegalArgumentException id not found
	 * @throws IOException error reading
	 */
	public abstract String getRecordData(String recID) throws IllegalArgumentException, IOException;
	
	/**
	 * Retrieves all metadata for a given record
	 * @param recID id of record to retrieve metadata for
	 * @return the metadata map
	 * @throws IOException error retrieving record metadata
	 */
	protected abstract SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException;
	
	/**
	 * Get the last RecordMetaData of a given type by a given operator for a given record
	 * @param recID id of record to retrieve metadata for
	 * @param type the type of metadata, null for any type
	 * @param operator the operator to get for, null for any type
	 * @return the last metadata of the specified type
	 * @throws IOException error retrieving record metadata
	 */
	protected RecordMetaData getLastMetaData(String recID, RecordMetaData.RecordMetaDataType type, Class<?> operator) throws IOException {
		for(RecordMetaData rmd : getRecordMetaData(recID)) {
			if(((type == null) || (rmd.getOperation() == type)) && ((operator == null) || rmd.getOperator().equals(operator))) {
				return rmd;
			}
		}
		return null;
	}
	
	/**
	 * Add a metadata record to indicate that the given operator has processed the given record
	 * @param rec record to set processed
	 * @param operator the class performing the processing
	 * @throws IOException error setting processed
	 */
	protected void setProcessed(Record rec, Class<?> operator) throws IOException {
		addMetaData(rec, operator, RecordMetaDataType.processed);
	}
	
	/**
	 * Add a metadata record to indicate that the given operator has written the given record
	 * @param rec record to set written
	 * @param operator the class performing the writing
	 * @throws IOException error setting written
	 */
	protected void setWritten(Record rec, Class<?> operator) throws IOException {
		addMetaData(rec, operator, RecordMetaDataType.written);
	}
	
	/**
	 * Adds a metadata record for the given record
	 * @param rec record to add metadata for
	 * @param operator the class operating on the record
	 * @param type the operation type
	 * @throws IOException error adding meta data
	 */
	protected void addMetaData(Record rec, Class<?> operator, RecordMetaDataType type) throws IOException {
		addMetaData(rec, new RecordMetaData(operator, type, RecordMetaData.md5hex(rec.getData())));
	}
	
	/**
	 * Adds a metadata record
	 * @param rec record to add metadata for
	 * @param rmd the metadata record
	 * @throws IOException error adding meta data
	 */
	protected abstract void addMetaData(Record rec, RecordMetaData rmd) throws IOException;
	
	/**
	 * Deletes all metadata for a record
	 * @param recID record id to delete metadata for
	 * @throws IOException error deleting metadata
	 */
	protected abstract void delMetaData(String recID) throws IOException;
	
	/**
	 * Delete the specified Record
	 * @param recID id of record to delete
	 * @throws IOException i/o error
	 */
	public abstract void delRecord(String recID) throws IOException;
	
	/**
	 * Setter for overwriteDefault
	 * @param overwrite the new value for overwriteDefault
	 */
	public void setOverwriteDefault(boolean overwrite) {
		this.overwriteDefault = overwrite;
	}
	
	/**
	 * Getter for overwriteDefault
	 * @return the overwriteDefault
	 */
	public boolean isOverwriteDefault() {
		return this.overwriteDefault;
	}
	
	/**
	 * Closes the recordhandler
	 * @throws IOException error closing
	 */
	public abstract void close() throws IOException;
	
	/**
	 * Has the given record been written since last processed by operator?
	 * @param id the record to check fo
	 * @param operator the class to check for
	 * @return true if written since last processed by operator or if never been processed by operator
	 */
	public boolean needsProcessed(String id, Class<?> operator) {
		try {
			RecordMetaData rmdWrite = getLastMetaData(id, RecordMetaDataType.written, null);
			Calendar write = rmdWrite.getDate();
			RecordMetaData rmdProcess = getLastMetaData(id, RecordMetaDataType.processed, operator);
			// log.debug("rmdWrite: "+rmdWrite);
			// log.debug("rmdProcess: "+rmdProcess);
			if(rmdProcess == null) {
				return true;
			}
			Calendar processed = rmdProcess.getDate();
			return (processed.compareTo(write) < 0);
		} catch(IOException e) {
			// error getting metadata file... assume it does not exist
			//			log.debug("Record "+id+" has no metadata... need to update.");
			return true;
		}
	}
	
	/**
	 * Does the given record contain updated information compared to existing record data
	 * @param rec the record
	 * @return true if need updated or record is new
	 */
	protected boolean needsUpdated(Record rec) {
		// log.debug("Checking if Record "+rec.getID()+" needs updated");
		try {
			RecordMetaData rmd = getLastMetaData(rec.getID(), RecordMetaDataType.written, null);
			// Check if previous written record meta data exists
			if(rmd != null) {
				// Get previous record meta data md5
				String oldMD5 = rmd.getMD5();
				// If md5s same
				String newMD5 = RecordMetaData.md5hex(rec.getData());
				if(newMD5.equals(oldMD5)) {
					// do nothing more
					// log.debug("Record "+rec.getID()+" has not changed... no need to update.");
					return false;
				}
				// log.debug("Record has changed... need to update");
			} else {
				// log.debug("Record never written... need to update");
			}
			return true;
		} catch(IOException e) {
			// error getting metadata file... assume it does not exist
			//			log.debug("Record "+rec.getID()+" has no metadata... need to update.");
			return true;
		}
	}
	
	/**
	 * Find records with idText in their id
	 * @param idText the text to find
	 * @return list of ids that match
	 * @throws IOException error searching
	 */
	public abstract Set<String> find(String idText) throws IOException;
	
	/**
	 * Merge records in this recordhandler using regex and write to output
	 * @param output output recordhandler
	 * @param regex regex for finding primary records (with a grouping for the subsection to use to find sub-records)
	 * @throws IOException error in record handling
	 */
	public void merge(RecordHandler output, Pattern regex) throws IOException {
		Map<String, String> matchedIDs = new HashMap<String, String>();
		log.info("Building List of Primary Records");
		for(Record r : this) {
			Matcher m = regex.matcher(r.getID());
			if(m.matches()) {
				log.debug("Matched record '" + r.getID() + "' => '" + m.group(1) + "'");
				matchedIDs.put(r.getID(), m.group(1));
			}
		}
		int count = matchedIDs.size();
		log.debug("Matched " + count + " records");
		int cur = 0;
		JenaConnect jc = new MemJenaConnect();
		for(String rid : new TreeSet<String>(matchedIDs.keySet())) {
			cur++;
			String matchTerm = matchedIDs.get(rid);
			log.debug("(" + cur + "/" + count + ": " + Math.round(10000f * cur / count) / 100f + "%): merging '" + matchTerm + "'");
			jc.truncate();
			for(String id : find(matchTerm)) {
				log.trace("Merging record '" + id + "' into '" + matchTerm + "'");
				jc.loadRdfFromString(getRecord(id).getData(), null, null);
			}
			output.addRecord(matchTerm, jc.exportRdfToString(), RecordHandler.class);
		}
		jc.close();
	}
}
