/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Just some handy methods for working with XML documents.
 */
public class XmlUtils {
	private static DocumentBuilderFactory factory = createDocBuilderFactory();

	private static DocumentBuilderFactory createDocBuilderFactory() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true); // never forget this!
		return dbf;
	}

	public static Document parseXml(String string) throws XmlUtilsException {
		try {
			return factory.newDocumentBuilder().parse(
					new InputSource(new StringReader(string)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new XmlUtilsException("Failed to parse XML from String", e);
		}
	}

	public static Document parseXml(Reader reader) throws XmlUtilsException {
		try {
			return factory.newDocumentBuilder().parse(new InputSource(reader));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new XmlUtilsException("Failed to parse XML from Reader", e);
		}
	}

	public static Document parseXml(InputStream stream)
			throws XmlUtilsException {
		try {
			return factory.newDocumentBuilder().parse(new InputSource(stream));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new XmlUtilsException("Failed to parse XML from InputStream",
					e);
		}
	}

	public static class XmlUtilsException extends Exception {
		public XmlUtilsException(String message, Throwable cause) {
			super(message, cause);
		}

		public XmlUtilsException(String message) {
			super(message);
		}
	}
}
