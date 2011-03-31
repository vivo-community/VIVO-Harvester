/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.test.harvester.util;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.SpecialEntities;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class SpecialEntitiesTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SpecialEntitiesTest.class);
	/** */
	private static final String toBeHtmlEnc = "\"woot\" & 'awesome' ^are <VERY> ~ready* {for %fun} @ the +store!";
	/** */
	private static final String toBeHtmlDec = "&quot;woot&quot; &amp; &apos;awesome&apos; ^are &lt;VERY&gt; ~ready* {for %fun} @ the +store!";
	/** */
	private static final String toBeXmlEnc = "<tag field=\"value\">text & more 'test', test</tag>";
	/** */
	private static final String toBeXmlDec = "&lt;tag field=&quot;value&quot;&gt;text &amp; more &apos;test&apos;, test&lt;/tag&gt;";
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.SpecialEntities#htmlEncode(java.lang.String, char...)
	 * htmlEncode(String s, char... exceptions)} .
	 */
	public final void testHtmlEncode() {
		log.info("BEGIN testHtmlEncode");
		log.debug("HTML Encoding: " + toBeHtmlEnc);
		String enc = SpecialEntities.htmlEncode(toBeHtmlEnc);
		log.debug(enc);
		assertEquals(enc, toBeHtmlDec);
		log.info("END testHtmlEncode");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.SpecialEntities#htmlDecode(java.lang.String) htmlDecode(String
	 * s)} .
	 */
	public final void testHtmlDecode() {
		log.info("BEGIN testHtmlDecode");
		log.debug("HTML Decoding: " + toBeHtmlDec);
		String dec = SpecialEntities.htmlDecode(toBeHtmlDec);
		log.debug(dec);
		assertEquals(dec, toBeHtmlEnc);
		log.info("END testHtmlDecode");
	}
	
	/**
	 * Test method for html equivalence
	 */
	public final void testBiDirectionalHtml() {
		log.info("BEGIN testBiDirectionalHtml");
		String dec;
		String enc;
		dec = null;
		enc = null;
		log.debug("HTML Encoding: " + toBeHtmlEnc);
		enc = SpecialEntities.htmlEncode(toBeHtmlEnc);
		log.info(enc);
		log.debug("HTML Decoding: " + enc);
		dec = SpecialEntities.htmlDecode(enc);
		log.debug(dec);
		assertEquals(dec, toBeHtmlEnc);
		dec = null;
		enc = null;
		log.debug("HTML Decoding: " + toBeHtmlDec);
		dec = SpecialEntities.htmlDecode(toBeHtmlDec);
		log.debug(dec);
		log.debug("HTML Encoding: " + dec);
		enc = SpecialEntities.htmlEncode(dec);
		log.debug(enc);
		assertEquals(enc, toBeHtmlDec);
		log.info("END testBiDirectionalHtml");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.SpecialEntities#xmlEncode(java.lang.String, char...)
	 * xmlEncode(String s, char... exceptions)}.
	 */
	public final void testXmlEncode() {
		log.info("BEGIN testXmlEncode");
		log.debug("XML Encoding: " + toBeXmlEnc);
		String enc = SpecialEntities.xmlEncode(toBeXmlEnc);
		log.debug(enc);
		assertEquals(enc, toBeXmlDec);
		log.info("END testXmlEncode");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.SpecialEntities#xmlDecode(java.lang.String) xmlDecode(String
	 * s)}.
	 */
	public final void testXmlDecode() {
		log.info("BEGIN testXmlDecode");
		log.debug("XML Decoding: " + toBeXmlDec);
		String dec = SpecialEntities.xmlDecode(toBeXmlDec);
		log.debug(dec);
		assertEquals(dec, toBeXmlEnc);
		log.info("END testXmlDecode");
	}
	
	/**
	 * Test method for Xml equivalence
	 */
	public final void testBiDirectionalXml() {
		log.info("BEGIN testBiDirectionalXml");
		String dec;
		String enc;
		dec = null;
		enc = null;
		log.debug("XML Encoding: " + toBeXmlEnc);
		enc = SpecialEntities.xmlEncode(toBeXmlEnc);
		log.debug(enc);
		log.debug("XML Decoding: " + enc);
		dec = SpecialEntities.xmlDecode(enc);
		log.debug(dec);
		assertEquals(dec, toBeXmlEnc);
		dec = null;
		enc = null;
		log.debug("XML Decoding: " + toBeXmlDec);
		dec = SpecialEntities.xmlDecode(toBeXmlDec);
		log.debug(dec);
		log.debug("XML Encoding: " + dec);
		enc = SpecialEntities.xmlEncode(dec);
		log.debug(enc);
		assertEquals(enc, toBeXmlDec);
		log.info("END testBiDirectionalXml");
	}
	
}
