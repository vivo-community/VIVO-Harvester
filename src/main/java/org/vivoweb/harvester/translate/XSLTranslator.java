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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes XML Files and uses an XSL file to translate the data into the desired ontology
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class XSLTranslator extends Translator {
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Logger log = LoggerFactory.getLogger(XSLTranslator.class);
	/**
	 * The translation xsl is the map that will reconstruct our input stream's document into the appropriate format
	 */
	private String translationString;
	
	/**
	 * Constructor
	 * @param translationStream the file that details the translation from the original xml to the target format</li>
	 * @throws IOException error reading files
	 */
	public XSLTranslator(InputStream translationStream) throws IOException {
		setTranslation(translationStream);
	}
	
	/**
	 * Set translation file from an inputSteam
	 * @param transFileStream valid type of translation file is xslt
	 * @throws IOException error reading from stream
	 */
	public void setTranslation(InputStream transFileStream) throws IOException {
		// copy xsl into memory for faster translations
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(transFileStream, baos);
		this.translationString = baos.toString();
	}
	
	/**
	 * Set translation file from a file
	 * @param transFile valid type of translation file is xslt
	 * @throws IOException error reading from file
	 */
	public void setTranslation(File transFile) throws IOException {
		setTranslation(new FileInputStream(transFile));
	}
	
	@Override
	public String translate(String input) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ByteArrayInputStream inStream = new ByteArrayInputStream(input.getBytes());
		ByteArrayInputStream translationStream = new ByteArrayInputStream(this.translationString.getBytes());
		StreamResult outputResult = new StreamResult(outStream);
		// JAXP reads data using the Source interface
		Source xmlSource = new StreamSource(inStream);
		Source xslSource = new StreamSource(translationStream);
		try {
			// the factory pattern supports different XSLT processors
			// this outputs to outStream (through outputResult)
			TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null).newTransformer(xslSource).transform(xmlSource, outputResult);
			outStream.flush();
		} catch(TransformerConfigurationException e) {
			throw new Error(e);
		} catch(TransformerException e) {
			throw new Error(e);
		} catch(IOException e) {
			throw new Error(e);
		}
		return outStream.toString();
	}

	@Override
	protected Logger getLog() {
		return log;
	}
}
