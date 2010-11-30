/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.io.InputStream;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * Connection Helper for Memory Based Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MemJenaConnect extends JenaConnect {
	
	/**
	 * Constructor (Memory Default Model)
	 */
	public MemJenaConnect() {
		this(null);
	}
	
	/**
	 * Constructor (Memory Named Model)
	 * @param modelName the model name to use
	 */
	public MemJenaConnect(String modelName) {
		ModelMaker mm = ModelFactory.createMemModelMaker();
		Model m;
		if(modelName != null) {
			m = mm.openModel(modelName, false);
		} else {
			m = mm.createDefaultModel();
		}
		this.setJenaModel(m);
	}
	
	/**
	 * Constructor (Load rdf from input stream)
	 * @param in input stream to load rdf from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 * "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML"
	 */
	public MemJenaConnect(InputStream in, String namespace, String language) {
		this();
		this.loadRdfFromStream(in, namespace, language);
	}
	
	@Override
	public void close() {
		this.getJenaModel().close();
	}

	@Override
	public JenaConnect neighborConnectClone(String modelName) throws IOException {
		return new MemJenaConnect(modelName);
	}

	@Override
	public void truncate() {
		this.getJenaModel().removeAll();
	}
	
}
