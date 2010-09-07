/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.test.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.HtmlEntities;
import junit.framework.TestCase;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class HtmlEntitiesTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(HtmlEntitiesTest.class);
	/** */private static final String toBeEnc = "\"woot\" & 'awesome' ^are <VERY> ~ready* {for %fun} @ the +store!";
	/** */private static final String toBeDec = "&Alpha;&Gamma;&Omega; &tilde;one for the money, 2&gt;1";
	/** */private static final String toBeBoth = "&diams;&hearts;&clubs;&spades; &tilde; fun";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.HtmlEntities#htmlEncode(java.lang.String) htmlEncode(String s)}.
	 */
	public final void testHtmlEncode() {
		log.info("Encoding: "+toBeEnc);
		log.info(HtmlEntities.htmlEncode(toBeEnc));
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.HtmlEntities#htmlDecode(java.lang.String) htmlDecode(String s)}.
	 */
	public final void testHtmlDecode() {
		log.info("Decoding: "+toBeDec);
		log.info(HtmlEntities.htmlDecode(toBeDec));
	}
	
	/**
	 * Test method for equivalence
	 */
	public final void testBiDirectional() {
		log.info("Decoding: "+toBeBoth);
		String dec = HtmlEntities.htmlDecode(toBeBoth);
		log.info(dec);
		log.info("Encoding: "+dec);
		String enc = HtmlEntities.htmlEncode(dec);
		log.info(enc);
		assertEquals(enc, toBeBoth);
	}
	
}
