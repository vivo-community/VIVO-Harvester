/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.translate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.cli.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.FileAide;
import org.w3c.dom.Document;
import com.hp.gloze.Gloze;

/**
 * Gloze Tranlator This class translates XML into its own natural RDF ontology using the gloze library. Translation into
 * the VIVO ontology is completed using the RDF Translator. TODO Stephen: Identify additional parameters required for
 * translation TODO Stephen: Identify methods to invoke in the gloze library
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class GlozeTranslator extends Translator{
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Logger log = LoggerFactory.getLogger(GlozeTranslator.class);
	/**
	 * The incoming schema to help gloze translate the xml file
	 */
	protected File incomingSchema;
	/**
	 * the uri base for relative nodes in the xml file
	 */
	protected URI uriBase;
	/**
	 * Gloze translator
	 */
	private Gloze gl;
	
	/**
	 * Constructor
	 * @param uriBase the base for all URIs generated
	 * @param xmlSchema the xml schema for gloze to use while translating
	 * @throws IOException error creating temp schema file
	 */
	public GlozeTranslator(String uriBase, String xmlSchema) throws IOException {
		if(uriBase == null) {
			throw new IllegalArgumentException("Must provide a uri base");
		}
		if(xmlSchema == null) {
			throw new IllegalArgumentException("Must provide an xml schema");
		}
		try {
			this.uriBase = new URI(uriBase);
			this.gl = new Gloze(getIncomingSchemaURL(xmlSchema), this.uriBase);
		} catch(URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		} catch(Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Setter for schema
	 * @param schemaPath the schema that gloze can use, but doesn't need to translate the xml
	 * @return the file containing the schema data
	 * @throws IOException error creating temp schema file
	 */
	private URL getIncomingSchemaURL(String schemaPath) throws IOException {
		if(StringUtils.isBlank(schemaPath)) {
			return null;
		}
		File tempSchema = File.createTempFile("glozeTranslatorSchema", null);
		FileOutputStream fos = new FileOutputStream(tempSchema);
		String schemaString = FileAide.getTextContent(schemaPath);
		fos.write(schemaString.getBytes());
		fos.close();
		tempSchema.deleteOnExit();
		return tempSchema.toURI().toURL();
	}
	
	@Override
	public String translate(String input) {
		MemJenaConnect outputModel = new MemJenaConnect();
		
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
			this.gl.lift(doc, null, this.uriBase, outputModel.getJenaModel());
		} catch(Exception e) {
			log.error("", e);
			throw new Error(e);
		}
		return outputModel.exportRdfToString();
	}

	@Override
	protected Logger getLog() {
		return log;
	}
}
