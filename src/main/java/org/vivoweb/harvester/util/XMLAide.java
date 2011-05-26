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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Export xpath selection from xml file
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class XMLAide {
	/**
	 * Node containing the namespace information
	 */
	private Node namespaceNode;
	/**
	 * Node to search on
	 */
	private Node searchNode;
	
	/**
	 * Initialize on a node and given namespace node
	 * @param searchNode the node to search on
	 * @param namespaceNode the node containing namespace information
	 */
	public XMLAide(Node searchNode, Node namespaceNode) {
		this.searchNode = searchNode;
		this.namespaceNode = namespaceNode;
	}
	
	/**
	 * Get an xml document Node for the given xml file
	 * @param xmlPath path to the file
	 * @return the document Node
	 * @throws IOException error reading xml file
	 */
	public static Document getDocumentNode(String xmlPath) throws IOException {
		return getDocumentNode(FileAide.getInputStream(xmlPath));
	}
	
	/**
	 * Get an xml document Node for the given xml file
	 * @param xmlStream the xml stream
	 * @return the document Node
	 * @throws IOException error reading xml file
	 */
	public static Document getDocumentNode(InputStream xmlStream) throws IOException {
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
	 * Get the root Node for the given xml file
	 * @param xmlPath path to the file
	 * @return the root Node
	 * @throws IOException error reading xml file
	 */
	public static Node getRootNode(String xmlPath) throws IOException {
		return getDocumentNode(xmlPath).getFirstChild();
	}
	
	/**
	 * Get the root Node for the given xml file
	 * @param xmlStream the xml stream
	 * @return the root Node
	 * @throws IOException error reading xml file
	 */
	public static Node getRootNode(InputStream xmlStream) throws IOException {
		return getDocumentNode(xmlStream).getFirstChild();
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param node xml node
	 * @param mainExpression xpath expression to search for
	 * @param subExpression xpath expression to search for under each mainExpression
	 * @param idExpression xpath expression to resolve for each mainExpression as an identifier
	 * @param namespaceNode Node containing namespace mappings
	 * @return the node value of the selection
	 * @throws TransformerException error processing xpath
	 */
	public static Map<String,String> getXPathStringMap(Node node, String mainExpression, String subExpression, String idExpression, Node namespaceNode) throws TransformerException {
		Map<String,String> map = new HashMap<String, String>();
		NodeList mainNodes = XPathAPI.selectNodeList(node, mainExpression, namespaceNode);
		for(int x = 0; x < mainNodes.getLength(); x++) {
			Node mainNode = mainNodes.item(x);
			String id = XPathAPI.eval(mainNode, idExpression, namespaceNode).str();
			String subValue = XPathAPI.eval(mainNode, subExpression, namespaceNode).str();
			map.put(id, subValue);
		}
		return map;
	}
	
	/**
	 * Export xpath selection from xml document
	 * @param mainExpression xpath expression to search for
	 * @param subExpression xpath expression to search for under each mainExpression
	 * @param idExpression xpath expression to resolve for each mainExpression as an identifier
	 * @return the node value of the selection
	 * @throws TransformerException error processing xpath
	 */
	public Map<String,String> getXPathStringMap(String mainExpression, String subExpression, String idExpression) throws TransformerException {
		return getXPathStringMap(this.searchNode, mainExpression, subExpression, idExpression, this.namespaceNode);
	}
    
    /**
     * Get the XObject value returned by the given XPath
     * @param xpath the path to find
     * @return the XObject value for the XPath
	 * @throws TransformerException error processing xpath
     */
    public XObject getXObject(String xpath) throws TransformerException {
		return XPathAPI.eval(this.searchNode, xpath, this.namespaceNode);
    }
    
    /**
     * Write the given node out as an XML string
     * @param n the node to write as XML
     * @return the XML string
     * @throws TransformerException error transforming
     */
    public static String toString(Node n) throws TransformerException {
        // Element docEl = getDocument().getDocumentElement();
        // return docEl.toString();
        Source input = new DOMSource(n);
        StringWriter sw = new StringWriter();
        Result output = new StreamResult(sw);
    	Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
        idTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        idTransformer.transform(input, output);
        return sw.toString();
    }
}
