/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.http;

/**
 * The base class for issuing HTTP requests.
 */
public interface HttpWorker {
	/**
	 * Create a GET request object. Add parameters, headers, etc, then call
	 * execute() on the request.
	 */
	HttpWorkerRequest<String> get(String url) throws HttpWorkerException;

	/**
	 * Create a POST request object. Add parameters, headers, etc, then call
	 * execute() on the request.
	 */
	HttpWorkerRequest<String> post(String url) throws HttpWorkerException;

}
