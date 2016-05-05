/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * A collection of static methods for helping deal with RDF
 */
public final class RdfUtils {
	public static Model toModel(InputStream rdfxml) {
		Model model = ModelFactory.createDefaultModel();
		model.read(rdfxml, null);
		return model;
	}

	public static Model toModel(String rdfxml) {
		Model model = ModelFactory.createDefaultModel();
		model.read(rdfxml, "RDF/XML");
		return model;
	}

	private RdfUtils() {
		// nothing to instantiate
	}
}
