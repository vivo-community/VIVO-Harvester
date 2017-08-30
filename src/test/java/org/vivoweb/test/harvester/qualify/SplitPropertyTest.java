/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.test.harvester.qualify;

import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.qualify.SplitProperty;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/**
 * Test Split Property
 * @author Christopher Haines (hainesc@ufl.edu)
 */
public class SplitPropertyTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SplitPropertyTest.class);
	/** */
	private JenaConnect jena;
	/** */
	private Property label;
	/** */
	private Property scoreLabelParts;
	
	@Override
	public void setUp() throws Exception {
		InitLog.initLogger(null, null);
		this.jena = new MemJenaConnect();
		this.jena.truncate();
		this.label = this.jena.getJenaModel().createProperty("http://www.w3.org/2000/01/rdf-schema#label");
		this.scoreLabelParts = this.jena.getJenaModel().createProperty("http://vivoweb.org/harvester/score#LabelParts");
	}
	
	@Override
	public void tearDown() throws Exception {
		this.jena.close();
		this.jena = null;
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.qualify.SplitProperty#splitPropertyValues(org.vivoweb.harvester.util.repo.JenaConnect, java.lang.String, java.lang.String, java.lang.String, boolean) splitPropertyValues(JenaConnect model, String oldPropertyURI, String splitRegex, String newPropertyURI, boolean trim)}
	 * @throws Exception error
	 */
	public final void testSplitPropertyValues() throws Exception{
		log.info("BEGIN testSplitPropertyValues");
		Resource res1 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSplitPropertyValues/item#1");
		Resource res2 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSplitPropertyValues/item#2");
		Resource res3 = this.jena.getJenaModel().createResource("http://harvester.vivoweb.org/testSplitPropertyValues/item#3");
		String searchValue = " ";
		this.jena.getJenaModel().add(res1, this.label, this.jena.getJenaModel().createLiteral("part1-1" + searchValue + "part1-2" + searchValue + "part1-3", "en"));
		this.jena.getJenaModel().add(res2, this.label, this.jena.getJenaModel().createTypedLiteral("part2-1" + searchValue + searchValue + "part2-2"));
		this.jena.getJenaModel().add(res3, this.label, "part3-1");
		log.debug("Pre-Split:\n" + this.jena.exportRdfToString());
		// call qualify
//		new Qualify(this.jena, this.label.getURI(), searchValue, replaceValue, true, null, false, false).execute();
		this.jena.loadRdfFromJC(SplitProperty.splitPropertyValues(this.jena, this.label.getURI(), searchValue, this.scoreLabelParts.getURI(), true));
		log.debug("Post-Split:\n" + this.jena.exportRdfToString());
		Set<String> part1s = new HashSet<String>();
		part1s.add("part1-1");
		part1s.add("part1-2");
		part1s.add("part1-3");
		Set<String> res1parts = new HashSet<String>();
		for(Statement s : IterableAdaptor.adapt(this.jena.getJenaModel().listStatements(res1, this.scoreLabelParts, (RDFNode)null))) {
			res1parts.add(s.getString());
		}
		assertTrue(res1parts.containsAll(part1s));
		assertTrue(part1s.containsAll(res1parts));
		Set<String> part2s = new HashSet<String>();
		part2s.add("part2-1");
		part2s.add("part2-2");
		Set<String> res2parts = new HashSet<String>();
		for(Statement s : IterableAdaptor.adapt(this.jena.getJenaModel().listStatements(res2, this.scoreLabelParts, (RDFNode)null))) {
			res2parts.add(s.getString());
		}
		assertTrue(res2parts.containsAll(part2s));
		assertTrue(part2s.containsAll(res2parts));
		Set<String> part3s = new HashSet<String>();
		part3s.add("part3-1");
		Set<String> res3parts = new HashSet<String>();
		for(Statement s : IterableAdaptor.adapt(this.jena.getJenaModel().listStatements(res3, this.scoreLabelParts, (RDFNode)null))) {
			res3parts.add(s.getString());
		}
		assertTrue(res3parts.containsAll(part3s));
		assertTrue(part3s.containsAll(res3parts));
		log.info("END testSplitPropertyValues");
	}
	
}
