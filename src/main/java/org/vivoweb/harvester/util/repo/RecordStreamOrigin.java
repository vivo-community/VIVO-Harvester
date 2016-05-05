/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
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
