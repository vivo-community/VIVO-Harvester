/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.cli.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.cli.util.args.ArgDef;
import org.vivoweb.harvester.cli.util.args.ArgList;
import org.vivoweb.harvester.cli.util.args.ArgParser;
import org.vivoweb.harvester.util.FileAide;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
	 */
	private XPathTool(String[] args) throws IOException {
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
	 * Get an xml document Node for the given xml file
	 * @param xmlPath path to the file
	 * @return the document Node
	 * @throws IOException error reading xml file
	 */
	public static Node getDocumentNode(String xmlPath) throws IOException {
		return getDocumentNode(FileAide.getInputStream(xmlPath));
	}
	
	/**
	 * Get an xml document Node for the given xml file
	 * @param xmlStream the config xmlStream
	 * @return the document Node
	 * @throws IOException error reading xml file
	 */
	public static Node getDocumentNode(InputStream xmlStream) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		Document doc;
		try {
			doc = factory.newDocumentBuilder().parse(xmlStream);
		} catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
		return doc;
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param node xml node
	 * @param expression xpath expression to export
	 * @return the string value of the selection
	 */
	public static String getXPathString(Node node, String expression) {
		return getXPathResult(node, expression, XPathConstants.STRING).toString();
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param node xml node
	 * @param expression xpath expression to export
	 * @return the node value of the selection
	 */
	public static Node getXPathNode(Node node, String expression) {
		return (Node)getXPathResult(node, expression, XPathConstants.NODE);
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param node xml node
	 * @param expression xpath expression to export
	 * @return the node value of the selection
	 */
	public static NodeList getXPathNodeList(Node node, String expression) {
		return (NodeList)getXPathResult(node, expression, XPathConstants.NODESET);
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param node xml node
	 * @param mainExpression xpath expression to search for
	 * @param subExpression xpath expression to search for under each mainExpression
	 * @param idExpression xpath expression to resolve for each mainExpression as an identifier
	 * @return the node value of the selection
	 */
	public static Map<String,String> getXPathStringMap(Node node, String mainExpression, String subExpression, String idExpression) {
		Map<String,String> map = new HashMap<String, String>();
		NodeList mainNodes = getXPathNodeList(node, mainExpression);
		for(int x = 0; x < mainNodes.getLength(); x++) {
			Node mainNode = mainNodes.item(x);
			String id = getXPathString(mainNode, idExpression);
			String subValue = getXPathString(mainNode, subExpression);
			map.put(id, subValue);
		}
		return map;
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param node xml node
	 * @param expression xpath expression to export
	 * @param returnType the qname for the return type
	 * @return the value of the selection
	 */
	public static Object getXPathResult(Node node, String expression, QName returnType) {
		try {
			return XPathFactory.newInstance().newXPath().compile(expression).evaluate(node, returnType);
		} catch(XPathExpressionException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	
	/**
	 * Export xpath selection from xml file
	 * @param xmlPath the path to the xml file
	 * @param expression xpath expression to export
	 * @return the value of the selection
	 * @throws IOException error reading xml file
	 */
	public static String getXPathString(String xmlPath, String expression) throws IOException {
		return getXPathString(getDocumentNode(xmlPath), expression);
	}
	
	/**
	 * Runs the xpath
	 * @throws IOException error executing
	 */
	public void execute() throws IOException {
		System.out.println(getXPathString(this.xml, this.exp));
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("XMLAide");
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
			InitLog.initLogger(args, getParser());
			if(harvLev == null) {
				System.clearProperty("console-log-level");
			} else {
				System.setProperty("console-log-level", harvLev);
			}
			log.info(getParser().getAppName() + ": Start");
			new XPathTool(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
