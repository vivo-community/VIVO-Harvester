/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Michael Barbieri (mbarbier@ufl.edu)
 * This file does two things.  First, it removes bad characters from the input file so that they can be read as XML by XSLTranslator.  Second, it removes
 * the unprefixed "xmlns=" attribute found in the root node of many MODS files, which left alone causes XSLTranslator to explode as if by magic.
 */
public class SanitizeMODSXML {
	/**
	 * The record handler for the input files. 
	 */
	private final RecordHandler inStore;
	/**
	 * The record handler for the converted MODS XML files.
	 */
	private final RecordHandler outStore;
	/**
	 * Force all input files to be processed even if nothing has changed.
	 */
	private final boolean force;
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SanitizeMODSXML.class);

	/**
	 * Constructor
	 * @param inStore the record handler for the input files.
	 * @param outStore the record handler for the sanitized files
	 * @param force sanitize files even if processing not needed
	 */
	public SanitizeMODSXML(RecordHandler inStore, RecordHandler outStore, boolean force) {
		this.inStore = inStore;
		this.outStore = outStore;
		this.force = force;
	}

	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public SanitizeMODSXML(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}

	/**
	 * Constructor
	 * @param argList option set of parsed args
	 * @throws IOException error creating task
	 */
	public SanitizeMODSXML(ArgList argList) throws IOException {
		this(RecordHandler.parseConfig(argList.get("i"), argList.getValueMap("I")), RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")), argList.has("f"));
	}

	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SanitizeMODSXML");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("Record handler for input files").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of input recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("Record handler for output files").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		return parser;
	}



	/**
	 * Sanitize all files in directory
	 * @throws IOException if an error in reading or writing occurs
	 */
	public void execute() throws IOException {
		int sanitized = 0;
		int skipped = 0;

		for(Record r : this.inStore) {
			if(this.force || r.needsProcessed(this.getClass())) {
				log.trace("Sanitizing record " + r.getID());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				sanitizeRecord(r, baos);
				this.outStore.addRecord(r.getID(), baos.toString(), this.getClass());
				r.setProcessed(this.getClass());
				baos.close();
				sanitized++;
			} else {
				log.trace("No sanitize needed: " + r.getID());
				skipped++;
			}
		}
		log.info(String.valueOf(sanitized) + " records sanitized.");
		log.info(String.valueOf(skipped) + " records did not need sanitization.");
	}

	/**
	 * Sanitize a MODS XML file
	 * @param inputRecord the record containing data from the input file
	 * @param outStream the stream to write the sanitized data to
	 * @throws IOException if an error in reading or writing occurs
	 */
	private void sanitizeRecord(Record inputRecord, OutputStream outStream) throws IOException
	{
		String xmlData = readRecord(inputRecord);
		xmlData = removeBadAttribute(xmlData);
		outStream.write(xmlData.getBytes());
	}



	/**
	 * Removes the unprefixed "xmlns" attribute from the root node.
	 * @param inputXml the input XML
	 * @return the XML with the bad attribute removed
	 * @throws IOException error reading XML
	 */
	private String removeBadAttribute(String inputXml) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = factory.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(inputXml.getBytes("utf-8"))));
			Element documentElement = doc.getDocumentElement();
			if(documentElement.hasAttribute("xmlns")) {
				log.debug("Removing xmlns element");
				documentElement.removeAttribute("xmlns");
			}
			return xmlToString(doc);
		}
		catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
		catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		}
	}


	/**
	 * Takes a Document and turns it into a String
	 * @param doc the Document to serialize
	 * @return the string version
	 * @throws IOException if a problem occurred in conversion
	 */
	private String xmlToString(Document doc) throws IOException {
        try {
            StringWriter stringWriter = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(stringWriter));
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new IOException(e.getMessage(), e);
        }
    }



	/**
	 * Loads the data from a record, and replaces all bad characters
	 * @param record the file to read 
	 * @return a String containing all data in the file
	 */
	private String readRecord(Record record) {
		String data = record.getData();
		StringBuilder builder = new StringBuilder(data.length());
		int dataLength = data.length();
		for(int i = 0; i < dataLength; i++) {
			char currentChar = data.charAt(i);
			builder.append(getReplacement(currentChar));
		}
		String output = builder.toString();
		return output;
	}



	/**
	 * Tests a character to see if it should be replaced with another one or combination
	 * @param character the character to test
	 * @return The character to replace
	 */
	private String getReplacement(char character) {
		String replacement;

		if((character >= 6) && (character <= 31))
			replacement = "";
		else
			replacement = String.valueOf(character);

		return replacement;
	}


	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new SanitizeMODSXML(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info(getParser().getAppName() + ": End");
		}
	}
}
