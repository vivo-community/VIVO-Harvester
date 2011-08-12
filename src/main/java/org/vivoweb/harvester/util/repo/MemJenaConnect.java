/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.SpecialEntities;

/**
 * Connection Helper for Memory Based Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MemJenaConnect extends TDBJenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(MemJenaConnect.class);
	/**
	 * Map of already used memory model names to directories
	 */
	private static HashMap<String, String> usedModelNames = new HashMap<String, String>();
	
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
		super(getDir(modelName), modelName);
	}
	
	/**
	 * Constructor (Load rdf from input stream)
	 * @param in input stream to load rdf from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 */
	public MemJenaConnect(InputStream in, String namespace, String language) {
		this(null);
		loadRdfFromStream(in, namespace, language);
	}
	
	/**
	 * Get the directory in which the model named is held
	 * @param modelName the model name
	 * @return the directory path
	 */
	private static String getDir(String modelName) {
		String mod = (modelName != null) ? modelName : generateUnusedModelName();
		mod = SpecialEntities.xmlEncode(mod, '/', ':');
		if(!usedModelNames.containsKey(mod)) {
			log.trace("attempting to create temp file for: " + mod);
			File f;
			try {
				f = FileAide.createTempFile(mod, ".tdb");
			} catch(IOException e) {
				throw new IllegalArgumentException(e);
			}
			log.trace("created: " + f.getAbsolutePath());
			f.delete();
			f.mkdir();
			usedModelNames.put(mod, f.getAbsolutePath());
		}
		return usedModelNames.get(mod);
	}
	
	/**
	 * Get an unused memory model name
	 * @return the name
	 */
	private static String generateUnusedModelName() {
		Random random = new Random();
		String name = null;
		while(name == null) {
			name = "DEFAULT" + random.nextInt(Integer.MAX_VALUE);
			if(usedModelNames.containsKey(name)) {
				name = null;
			}
		}
		return name;
	}
}
