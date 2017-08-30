/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.http;

import org.apache.jena.rdf.model.Model;

/**
 * TODO
 */
public interface HttpWorkerRequest<T> {
	// ----------------------------------------------------------------------
	// Populate the request
	// ----------------------------------------------------------------------

	HttpWorkerRequest<T> parameter(String name, Object value);

	HttpWorkerRequest<T> accept(Object acceptType);

	// ----------------------------------------------------------------------
	// Determine how the response will be processed.
	// ----------------------------------------------------------------------

	HttpWorkerRequest<String> asString();

	HttpWorkerRequest<org.w3c.dom.Document> asXML();

	HttpWorkerRequest<org.jsoup.nodes.Document> asHtml();

	HttpWorkerRequest<Model> asModel();

	// ----------------------------------------------------------------------
	// Send the request and get the response
	// ----------------------------------------------------------------------

	T execute() throws HttpWorkerException;

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	enum Method {
		GET, POST
	}

	enum Accept {
		RDF_XML("application/rdf+xml");

		private final String mimeType;

		private Accept(String mimeType) {
			this.mimeType = mimeType;
		}

		public String getMimeType() {
			return mimeType;
		}

		@Override
		public String toString() {
			return mimeType;
		}
	}
}
