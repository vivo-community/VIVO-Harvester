/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Export xpath selection from xml file
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class XPathTool {
	/**
	 * SLF4J Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(XPathTool.class);
	/**
	 * path to xml file to read data from
	 */
	private String xml;
	/**
	 * xpath expression to export
	 */
	private String exp;
	
	/**
	 * Constructor
	 * @param xmlFile path to xml file to read data from
	 * @param expression xpath expression to export
	 */
	public XPathTool(String xmlFile, String expression) {
		this.xml = xmlFile;
		this.exp = expression;
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private XPathTool(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList arguments
	 */
	private XPathTool(ArgList argList) {
		this(argList.get("x"), argList.get("e"));
	}
	
	/**
	 * Export xpath selection from xml file
	 * @param xmlFile path to xml file to read data from
	 * @param expression xpath expression to export
	 * @return the value of the selection
	 * @throws IOException error reading xml file
	 */
	public static String getXPathResult(String xmlFile, String expression) throws IOException {
		return getXpathStreamResult(FileAide.getInputStream(xmlFile), expression);
	}


	/**
	 * @param xmlIS input stream of the xml document
	 * @param expression the expression used in xpath to find the proper location
	 * @return A string which is the resulting value of the xpath request
	 * @throws IOException thrown if there is an issue parsing the input stream
	 */
	public static String getXpathStreamResult(InputStream xmlIS, String expression) throws IOException{
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); // never forget this!
			Document doc = factory.newDocumentBuilder().parse(new InputSource(xmlIS));
			String value = XPathFactory.newInstance().newXPath().compile(expression).evaluate(doc, XPathConstants.STRING).toString();
			return value;
		} catch(ParserConfigurationException e) {
			throw new IOException(e);
		} catch(SAXException e) {
			throw new IOException(e);
		} catch(XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Runs the xpath
	 * @throws IOException error executing
	 */
	public void execute() throws IOException {
		System.out.println(getXPathResult(this.xml, this.exp));
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("XPathTool");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('x').setLongOpt("xml-file").withParameter(true, "XML_FILE").setDescription("path to xml file to read data from").setRequired(true));
		// Params
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("expression").withParameter(true, "XPATH_EXPRESSION").setDescription("xpath expression to export").setRequired(true));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			String harvLev = System.getProperty("console-log-level");
			System.setProperty("console-log-level", "OFF");
			InitLog.initLogger(args, getParser(), "h");
			if(harvLev == null) {
				System.clearProperty("console-log-level");
			} else {
				System.setProperty("console-log-level", harvLev);
			}
			log.info(getParser().getAppName() + ": Start");
			new XPathTool(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
