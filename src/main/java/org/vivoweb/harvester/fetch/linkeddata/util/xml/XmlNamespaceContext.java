/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class XmlNamespaceContext implements NamespaceContext {
	private final Map<String, String> namespaceMap;

	public XmlNamespaceContext(XmlPrefix[] xmlPrefixes) {
		//this(XMLConstants.NULL_NS_URI, xmlPrefixes);
		this("",xmlPrefixes);
	}

	public XmlNamespaceContext(String defaultNamespaceUri,
			XmlPrefix[] xmlPrefixes) {
		this.namespaceMap = new HashMap<>();
		this.namespaceMap.put(XMLConstants.XML_NS_PREFIX,
				XMLConstants.XML_NS_URI);
		this.namespaceMap.put(XMLConstants.XMLNS_ATTRIBUTE,
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		this.namespaceMap.put(XMLConstants.DEFAULT_NS_PREFIX,
				defaultNamespaceUri);
		for (XmlPrefix xp : xmlPrefixes) {
			this.namespaceMap.put(xp.prefix, xp.nsUri);
		}
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("prefix may not be null.");
		}

		String uri;
		String ns = namespaceMap.get(prefix);
		if (ns == null) {
			//uri = XMLConstants.NULL_NS_URI;
			uri = "";
		} else {
			uri = namespaceMap.get(prefix);
		}
		return uri;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("namespaceURI may not be null.");
		}

		for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
			if (namespaceURI.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("namespaceURI may not be null.");
		}

		Set<String> prefixes = new HashSet<>();
		for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
			if (namespaceURI.equals(entry.getValue())) {
				prefixes.add(entry.getKey());
			}
		}
		return Collections.unmodifiableCollection(prefixes).iterator();
	}

}
