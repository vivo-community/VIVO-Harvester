/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.vivoweb.harvester.fetch.linkeddata.util.xml.XmlNamespaceContext;
import org.vivoweb.harvester.fetch.linkeddata.util.xml.XmlPrefix;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList; 

/**
 * TODO
 */
public class XPathHelper {
	public static final XmlPrefix RDF_PREFIX = new XmlPrefix("rdf",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#");

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	/**
	 * Create a helper instance with an optional list of prefixes.
	 */
	public static XPathHelper getHelper(XmlPrefix... xmlPrefixes) {
		return new XPathHelper(xmlPrefixes);
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final XPath xpath;

	public XPathHelper(XmlPrefix[] xmlPrefixes) {
		this.xpath = XPathFactory.newInstance().newXPath();
		this.xpath.setNamespaceContext(new XmlNamespaceContext(xmlPrefixes));
	}

	/**
	 * Search for an Xpath pattern in the context of a node, returning a handy
	 * list.
	 */
	public List<Node> findNodes(String pattern, Node context)
			throws XpathHelperException {
		try {
			XPathExpression xpe = xpath.compile(pattern);
			NodeList nodes = (NodeList) xpe.evaluate(context,
					XPathConstants.NODESET);
			List<Node> list = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); i++) {
				list.add(nodes.item(i));
			}
			return list;
		} catch (XPathExpressionException e) {
			throw new XpathHelperException("Can't parse '" + pattern
					+ "' in this context.", e);
		}  
	}

	/**
	 * Search for the first node in this context that matches the Xpath pattern.
	 * If not found, return null.
	 */
	public Node findFirstNode(String pattern, Node context)
			throws XpathHelperException {
		try {
			XPathExpression xpe = xpath.compile(pattern);
			NodeList nodes = (NodeList) xpe.evaluate(context,
					XPathConstants.NODESET);
			if (nodes.getLength() == 0) {
				return null;
			} else {
				return nodes.item(0);
			}
		} catch (XPathExpressionException e) {
			throw new XpathHelperException("Can't parse '" + pattern
					+ "' in this context.", e);
		}
	}

	/**
	 * Search for the first node in this context that matches the Xpath pattern.
	 * If not found, throw an exception.
	 */
	public Node findRequiredNode(String pattern, Node context)
			throws XpathHelperException {
		Node result = findFirstNode(pattern, context);
		if (result != null) {
			return result;
		} else {
			throw new XpathHelperException("Can't find a node that matches '"
					+ pattern + "' within this context.");
		}
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	public static class XpathHelperException extends Exception {
		public XpathHelperException(String message, Throwable cause) {
			super(message, cause);
		}

		public XpathHelperException(String message) {
			super(message);
		}
	}
}
