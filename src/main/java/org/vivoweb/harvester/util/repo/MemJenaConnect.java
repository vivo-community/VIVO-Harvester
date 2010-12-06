/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * Connection Helper for Memory Based Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MemJenaConnect extends JenaConnect {
	
	/**
	 * Set of already used memory model names
	 */
	private static HashSet<String> usedModelNames = new HashSet<String>();
	
	/**
	 * Constructor (Memory Default Model)
	 */
	public MemJenaConnect() {
		this(generateUnusedModelName());
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
			this.setModelName(modelName);
		} else {
			String name = generateUnusedModelName();
			m = mm.openModel(name, false);
			this.setModelName(name);
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
	
	/**
	 * Get an unused memory model name
	 * @return the name
	 */
	private static String generateUnusedModelName() {
		Random random = new Random();
		String name = null;
		while(name == null) {
			name = "DEFAULT"+random.nextInt(Integer.MAX_VALUE);
			if(usedModelNames.contains(name)) {
				name = null;
			}
		}
		usedModelNames.add(name);
		return name;
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
