/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.test.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.SpecialEntities;
import junit.framework.TestCase;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class SpecialEntitiesTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(SpecialEntitiesTest.class);
	/** */
	private static final String toBeHtmlEnc = "\"woot\" & 'awesome' ^are <VERY> ~ready* {for %fun} @ the +store!";
	/** */
	private static final String toBeHtmlDec = "&quot;woot&quot; &amp; &apos;awesome&apos; ^are &lt;VERY&gt; ~ready* {for %fun} @ the +store!";
	/** */
	private static final String toBeXmlEnc = "<tag field=\"value\">text & more 'test', test</tag>";
	/** */
	private static final String toBeXmlDec = "&lt;tag field=&quot;value&quot;&gt;text &amp; more &apos;test&apos;, test&lt;/tag&gt;";
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.SpecialEntities#htmlEncode(java.lang.String) htmlEncode(String s)}
	 * .
	 */
	public final void testHtmlEncode() {
		log.info("Testing HTML Encoding");
		log.info("HTML Encoding: " + toBeHtmlEnc);
		String enc = SpecialEntities.htmlEncode(toBeHtmlEnc);
		log.info(enc);
		assertEquals(enc, toBeHtmlDec);
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.SpecialEntities#htmlDecode(java.lang.String) htmlDecode(String s)}
	 * .
	 */
	public final void testHtmlDecode() {
		log.info("Testing HTML Decoding");
		log.info("HTML Decoding: " + toBeHtmlDec);
		String dec = SpecialEntities.htmlDecode(toBeHtmlDec);
		log.info(dec);
		assertEquals(dec, toBeHtmlEnc);
	}
	
	/**
	 * Test method for html equivalence
	 */
	public final void testBiDirectionalHtml() {
		log.info("Testing HTML Equvalence");
		String dec;
		String enc;
		dec = null;
		enc = null;
		log.info("HTML Encoding: " + toBeHtmlEnc);
		enc = SpecialEntities.htmlEncode(toBeHtmlEnc);
		log.info(enc);
		log.info("HTML Decoding: " + enc);
		dec = SpecialEntities.htmlDecode(enc);
		log.info(dec);
		assertEquals(dec, toBeHtmlEnc);
		dec = null;
		enc = null;
		log.info("HTML Decoding: " + toBeHtmlDec);
		dec = SpecialEntities.htmlDecode(toBeHtmlDec);
		log.info(dec);
		log.info("HTML Encoding: " + dec);
		enc = SpecialEntities.htmlEncode(dec);
		log.info(enc);
		assertEquals(enc, toBeHtmlDec);
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.SpecialEntities#xmlEncode(java.lang.String) xmlEncode(String s)}.
	 */
	public final void testXmlEncode() {
		log.info("Testing XML Encoding");
		log.info("XML Encoding: " + toBeXmlEnc);
		String enc = SpecialEntities.xmlEncode(toBeXmlEnc);
		log.info(enc);
		assertEquals(enc, toBeXmlDec);
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.SpecialEntities#xmlDecode(java.lang.String) xmlDecode(String s)}.
	 */
	public final void testXmlDecode() {
		log.info("Testing XML Decoding");
		log.info("XML Decoding: " + toBeXmlDec);
		String dec = SpecialEntities.xmlDecode(toBeXmlDec);
		log.info(dec);
		assertEquals(dec, toBeXmlEnc);
	}
	
	/**
	 * Test method for Xml equivalence
	 */
	public final void testBiDirectionalXml() {
		log.info("Testing XML Equvalence");
		String dec;
		String enc;
		dec = null;
		enc = null;
		log.info("XML Encoding: " + toBeXmlEnc);
		enc = SpecialEntities.xmlEncode(toBeXmlEnc);
		log.info(enc);
		log.info("XML Decoding: " + enc);
		dec = SpecialEntities.xmlDecode(enc);
		log.info(dec);
		assertEquals(dec, toBeXmlEnc);
		dec = null;
		enc = null;
		log.info("XML Decoding: " + toBeXmlDec);
		dec = SpecialEntities.xmlDecode(toBeXmlDec);
		log.info(dec);
		log.info("XML Encoding: " + dec);
		enc = SpecialEntities.xmlEncode(dec);
		log.info(enc);
		assertEquals(enc, toBeXmlDec);
	}
	
}
