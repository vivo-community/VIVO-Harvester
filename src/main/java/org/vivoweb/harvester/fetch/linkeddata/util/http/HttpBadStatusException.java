/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.http;

import org.apache.http.StatusLine;

/**
 * How to report a 404 or a 500 error, etc.
 */
public class HttpBadStatusException extends HttpWorkerException {
	public HttpBadStatusException(BasicHttpWorkerRequest<?> request,
			StatusLine statusLine, String responseBody) {
		super("HTTP returned a status of '" + statusLine
				+ "' for this request: " + request + ", response was '"
				+ responseBody + "'");
	}

}
