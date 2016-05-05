/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;

/**
 * Wraps a file an RDF File as a JenaConnect
 * @author Christopher Haines hainesc@ufl.edu
 */
public class FileJenaConnect extends MemJenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(FileJenaConnect.class);
	/**
	 * 
	 */
	private final String filepath;
	
	/**
	 * Constructor
	 * @param filepath path to the file
	 * @throws IOException error reading file
	 */
	public FileJenaConnect(String filepath) throws IOException {
		this(filepath, null, null);
	}
	
	/**
	 * Constructor
	 * @param filepath path to the file
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 * @throws IOException error reading file
	 */
	public FileJenaConnect(String filepath, String language) throws IOException {
		this(filepath, null, language);
	}
	
	/**
	 * Constructor
	 * @param filepath path to the file
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 * @throws IOException error reading file
	 */
	public FileJenaConnect(String filepath, String namespace, String language) throws IOException {
		super(FileAide.getInputStream(filepath), namespace, language);
		this.filepath = filepath;
	}
	
	@Override
	public void sync() {
		log.trace("Syncronizing the model...");
		try {
			exportRdfToFile(this.filepath);
			log.trace("Syncronization of model complete");
		} catch(IOException e) {
			log.error("Failed to syncronize the model!");
			log.debug("Stacktrace:",e);
		}
	}
}
