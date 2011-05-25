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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Export xpath selection from xml file
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class XPathAide {
	/**
	 * SLF4J Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(XPathAide.class);
	
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
}
