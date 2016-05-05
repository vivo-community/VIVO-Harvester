/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.discovery; 

import org.vivoweb.harvester.fetch.linkeddata.LinkedDataFetchException;

/**
 * Problem in the URI Discovery phase.
 */
public class DiscoveryWorkerException extends LinkedDataFetchException {
	public DiscoveryWorkerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DiscoveryWorkerException(String message) {
		super(message);
	}
}
