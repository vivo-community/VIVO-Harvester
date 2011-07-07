package org.vivoweb.harvester.util.repo;

import java.io.IOException;

/**
 * Interface for all users of XMLRecordOutputStream
 * @author Christopher Haines (hainesc@ufl.edu)
 */
public interface RecordStreamOrigin {
	/**
	 * Write a record
	 * @param id the record id to be written
	 * @param data the record data to be written
	 * @throws IOException error writting record
	 */
	public void writeRecord(String id, String data) throws IOException;
}
