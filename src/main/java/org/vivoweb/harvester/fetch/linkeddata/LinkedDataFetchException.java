/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata;

/**
 * Indicates a problem in the LinkedDataFetch.
 * 
 * This should be the parent class for any other Exception classes define in the
 * application.
 * 
 * We want to add context at every level, so we don't implement any constructors
 * that don't provide a message.
 */
public class LinkedDataFetchException extends Exception {
	public LinkedDataFetchException(String message, Throwable cause) {
		super(message, cause);
	}

	public LinkedDataFetchException(String message) {
		super(message);
	}

}
