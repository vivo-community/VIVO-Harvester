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
import com.hp.hpl.jena.sdb.Store;

/**
 * Connection Helper for Memory Based Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MemJenaConnect extends SDBJenaConnect {
	
	/**
	 * Set of already used memory model names
	 */
	private static HashSet<String> usedModelNames = new HashSet<String>();
	
	/**
	 * Constructor (Memory Default Model)
	 * @throws IOException error connecting
	 */
	public MemJenaConnect() throws IOException {
		this(null);
	}
	
	/**
	 * Constructor (Memory Named Model)
	 * @param modelName the model name to use
	 * @throws IOException error connecting
	 */
	public MemJenaConnect(String modelName) throws IOException {
		super("jdbc:h2:mem:"+((modelName != null)?modelName:generateUnusedModelName()), "sa", "", "H2", "org.h2.Driver", "layout2", modelName);
	}
	
	public static Store connectStore(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String dbLayout) {
		try {
			return SDBJenaConnect.connectStore(dbUrl, dbUser, dbPass, dbType, dbClass, dbLayout);
		} catch(IOException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	
	/**
	 * Constructor (Load rdf from input stream)
	 * @param in input stream to load rdf from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 * "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML"
	 * @throws IOException error connecting
	 */
	public MemJenaConnect(InputStream in, String namespace, String language) throws IOException {
		this(null);
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
	
}
