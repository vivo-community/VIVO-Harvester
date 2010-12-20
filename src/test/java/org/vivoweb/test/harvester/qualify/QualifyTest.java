/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.qualify;

import java.io.IOException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.qualify.Qualify;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Stephen Williams (swilliams@ctrip.ufl.edu)
 */
public class QualifyTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(QualifyTest.class);
	/** */
	private JenaConnect jena;
	/** */
	private Property label;
	/** */
	private Property scoreAffilitation;
	
	@Override
	public void setUp() throws Exception {
		InitLog.initLogger(QualifyTest.class);
		this.jena = new MemJenaConnect();
		this.label = this.jena.getJenaModel().createProperty("http://www.w3.org/2000/01/rdf-schema#label");
		this.scoreAffilitation = this.jena.getJenaModel().createProperty("http://vivoweb.org/harvester/score#Affiliation");
	}
	
	@Override
	public void tearDown() throws Exception {
		this.jena.close();
		this.jena = null;
		System.gc();
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.qualify.Qualify#main(java.lang.String[]) main(String... args)} using
	 * regex replace.
	 * @throws IOException error
	 */
	public void testRegexReplace() throws IOException {
		log.info("BEGIN testRegexReplace");
		Resource res1 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#1");
		Resource res2 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#2");
		Resource res3 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#3");
		Resource res4 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#4");
		String searchValue = "IATRR";
		this.jena.getJenaModel().add(res1, this.label, this.jena.getJenaModel().createLiteral(searchValue,"en"));
		this.jena.getJenaModel().add(res2, this.label, this.jena.getJenaModel().createTypedLiteral("woo"+searchValue+"blah"));
		this.jena.getJenaModel().add(res3, this.label, "testing "+searchValue+"fun far"+searchValue+"bo yea");
		this.jena.getJenaModel().add(res4, this.label, "I A T R R");
		String replaceValue = "I Am Testing Regex Replace";
		System.out.println(this.jena.exportRdfToString());
		// call qualify
		new Qualify(this.jena, this.label.getURI(), searchValue, replaceValue, true, null, false, false).execute();
		System.out.println(this.jena.exportRdfToString());
		assertEquals(replaceValue, this.jena.getJenaModel().getProperty(res1, this.label).getString());
		assertEquals("woo"+replaceValue+"blah", this.jena.getJenaModel().getProperty(res2, this.label).getString());
		assertFalse(this.jena.getJenaModel().getProperty(res3, this.label).getString().equals(replaceValue));
		log.info("END testRegexReplace");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.qualify.Qualify#main(java.lang.String[]) main(String... args)} using
	 * string replace.
	 */
	public void testStringReplace() {
		log.info("BEGIN testStringReplace");
		Resource res1 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#1");
		Resource res2 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#2");
		Resource res3 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#3");
		this.jena.getJenaModel().add(res1, this.label, "IATTR");
		this.jena.getJenaModel().add(res2, this.label, "wooIATTRblah");
		this.jena.getJenaModel().add(res3, this.label, "I A T T R");
		String expectedValue = "I Am Testing Test Replace";
		// call qualify
		new Qualify(this.jena, this.label.getURI(), "IATTR", expectedValue, false, null, false, false).execute();
		assertEquals(expectedValue, this.jena.getJenaModel().getProperty(res1, this.label).getString());
		assertFalse(this.jena.getJenaModel().getProperty(res2, this.label).getString().equals(expectedValue));
		assertFalse(this.jena.getJenaModel().getProperty(res3, this.label).getString().equals(expectedValue));
		log.info("END testStringReplace");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.qualify.Qualify#main(java.lang.String[]) main(String... args)} using
	 * string replace.
	 * @throws IOException error
	 */
	public void testRemoveNamespace() throws IOException {
		log.info("BEGIN testStringReplace");
		Resource res1 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#1");
		Resource res2 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#2");
		Resource res3 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSPARQLQualify/item#3");
		this.jena.getJenaModel().add(res1, this.label, "IATTR");
		this.jena.getJenaModel().add(res2, this.scoreAffilitation, "wooIATTRblah");
		this.jena.getJenaModel().add(res3, this.label, "I A T T R");
		String namespace = "http://vivoweb.org/harvester/score#";
		// call qualify
		new Qualify(this.jena, this.label.getURI(), null, null, false, namespace, true, false).execute();
		log.debug("Jena Dump Post-Qualify\n" + this.jena.exportRdfToString());
		assertTrue(this.jena.getJenaModel().containsResource(res1));
		assertFalse(this.jena.getJenaModel().containsResource(res2));
		assertTrue(this.jena.getJenaModel().containsResource(res3));
		log.info("END testStringReplace");
	}
	
}
