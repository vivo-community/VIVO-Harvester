/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.translate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * Takes XML Files and uses an XSL file to translate the data into the desired ontology
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class XSLTranslator {
	/**
	 * the log property for logging errors, information, debugging
	 */
	private static Logger log = LoggerFactory.getLogger(XSLTranslator.class);
	/**
	 * The translation xsl is the map that will reconstruct our input stream's document into the appropriate format
	 */
	private String translationString;
	/**
	 * record handler for incoming records
	 */
	protected RecordHandler inStore;
	/**
	 * record handler for storing records
	 */
	protected RecordHandler outStore;
	/**
	 * force process records
	 */
	private boolean force;
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public XSLTranslator(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argumentList <ul>
	 * <li>translationStream the file that details the translation from the original xml to the target format</li>
	 * <li>inRecordHandler the files/records that require translation</li>
	 * <li>outRecordHandler the output record for the translated files</li>
	 * <li>force translate all input records, even if previously processed</li>
	 * </ul>
	 * @throws IOException error reading files
	 */
	public XSLTranslator(ArgList argumentList) throws IOException {
		// set Translation file
		setTranslation(VFS.getManager().resolveFile(new File("."), argumentList.get("x")).getContent().getInputStream());
		
		// create record handlers
		this.inStore = RecordHandler.parseConfig(argumentList.get("i"), argumentList.getValueMap("I"));
		this.outStore = RecordHandler.parseConfig(argumentList.get("o"), argumentList.getValueMap("O"));
		this.force = argumentList.has("f");
	}
	
	/**
	 * Constructor
	 * @param translationStream the file that details the translation from the original xml to the target format</li>
	 * @param inRecordHandler the files/records that require translation
	 * @param outRecordHandler the output record for the translated files
	 * @param force translate all input records, even if previously processed
	 * @throws IOException error reading files
	 */
	public XSLTranslator(RecordHandler inRecordHandler, RecordHandler outRecordHandler, InputStream translationStream, boolean force) throws IOException {
		// set Translation file
		setTranslation(translationStream);
		
		// create record handlers
		this.inStore = inRecordHandler;
		this.outStore = outRecordHandler;
		this.force = force;
	}
	
	/**
	 * Set translation file from a file
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
	
	/**
	 * checks again for the necessary file and makes sure that they exist
	 * @throws IOException error processing
	 */
	public void execute() throws IOException {
		// get from the in record and translate
		int translated = 0;
		int passed = 0;
		
		for(Record r : this.inStore) {
			if(r.needsProcessed(this.getClass()) || this.force) {
				log.trace("Translating Record " + r.getID());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				xmlTranslate(new ByteArrayInputStream(r.getData().getBytes()), baos, new ByteArrayInputStream(this.translationString.getBytes()));
				this.outStore.addRecord(r.getID(), baos.toString(), this.getClass());
				r.setProcessed(this.getClass());
				baos.close();
				translated++;
			} else {
				log.trace("No Translation Needed: " + r.getID());
				passed++;
			}
		}
		log.info(Integer.toString(translated) + " records translated.");
		log.info(Integer.toString(passed) + " records did not need translation");
	}
	
	/**
	 * using the javax xml transform factory this method uses the xsl to translate XML into the desired format
	 * designated in the xsl.
	 * @param inStream the input stream
	 * @param outStream the output stream
	 * @param translationStream the stream for the xsl
	 * @throws IOException error translating
	 */
	public static void xmlTranslate(InputStream inStream, OutputStream outStream, InputStream translationStream) throws IOException {
		StreamResult outputResult = new StreamResult(outStream);
		// JAXP reads data using the Source interface
		Source xmlSource = new StreamSource(inStream);
		Source xslSource = new StreamSource(translationStream);
		try {
			// the factory pattern supports different XSLT processors
			// this outputs to outStream (through outputResult)
			TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null).newTransformer(xslSource).transform(xmlSource, outputResult);
		} catch(TransformerConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(TransformerException e) {
			throw new IOException(e.getMessage(), e);
		}
		outStream.flush();
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("XSLTranslator");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input record handler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of input recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output record handler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('x').setLongOpt("xslFile").withParameter(true, "XSL_FILE").setDescription("xsl file").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("force").setDescription("force translation of all input records, even if previously processed").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(XSLTranslator.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new XSLTranslator(args).execute();
		} catch(IllegalArgumentException e) {
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
}
