/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.http;

/**
 * The exception that an HttpWorker or associated classes might throw.
 */
public class HttpWorkerException extends Exception {
	public HttpWorkerException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpWorkerException(String message) {
		super(message);
	}

	public HttpWorkerException(Throwable cause) {
		super(cause);
	}
}
