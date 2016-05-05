/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.test.harvester.util.args;

import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ArgListTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ArgListTest.class);
	/** */
	private static ArgParser parser;
	/** */
	private File confFile;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
		parser = new ArgParser("ArgListTest");
		this.confFile = FileAide.createTempFile("ArgListTest", ".xml");
		String confString = "" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Task>\n" +
				"   <Param name=\"driver\">com.mysql.jdbc.Driver</Param>\n" +
				"   <Param name=\"connection\">jdbc:mysql://127.0.0.1:3306/DemoDB</Param>\n" +
				"   <Param name=\"username\">DemoDB</Param>\n" +
				"   <Param name=\"password\">b8F5LdFPVEqHP3XX</Param>\n" +
				"</Task>";
		FileAide.setTextContent(this.confFile.getAbsolutePath(), confString);
		makeArgList();
	}
	
	@Override
	protected void tearDown() throws Exception {
		parser = null;
		this.confFile.delete();
		this.confFile = null;
	}
	
	/** */
	private final void makeArgList() {
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("driver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("connection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("username").withParameter(true, "USERNAME").setDescription("database username").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("password").withParameter(true, "PASSWORD").setDescription("database password").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(false));
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.args.ArgList#get(java.lang.String) get(String arg)}.
	 * @throws Exception error
	 */
	public final void testConfig() throws Exception {
		log.info("BEGIN testConfig");
		ArgList a = parser.parse(new String[]{"-X", this.confFile.getAbsolutePath()});
		assertEquals(a.get("u"), "DemoDB");
		log.info("END testConfig");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.args.ArgList#get(java.lang.String) get(String arg)}.
	 * @throws Exception error
	 */
	public final void testGet() throws Exception {
		log.info("BEGIN testGet");
		ArgList a = parser.parse(new String[]{"-o", "Testing"});
		assertEquals(a.get("o"), "Testing");
		log.info("END testGet");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.args.ArgList#getAll(java.lang.String) getAll(String arg)}.
	 * @throws Exception error
	 */
	public final void testGetAllString() throws Exception {
		log.info("BEGIN testGetAllString");
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("except").withParameters(true, "EXCEPTION").setDescription("exception").setDefaultValue("test"));
		ArgList a = parser.parse(new String[]{"-e", "Testing1", "-e", "Testing2", "-e", "Testing3"});
		List<String> l = a.getAll("e");
		assertTrue(l.contains("Testing1"));
		assertTrue(l.contains("Testing2"));
		assertTrue(l.contains("Testing3"));
		assertFalse(l.contains("test"));
		log.info("END testGetAllString");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.args.ArgList#getAll(java.lang.String, boolean) getAll(String
	 * arg,
	 * boolean includeDefaultValue)}.
	 * @throws Exception error
	 */
	public final void testGetAllStringBoolean() throws Exception {
		log.info("BEGIN testGetAllStringBoolean");
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("except").withParameters(true, "EXCEPTION").setDescription("exception").setDefaultValue("test"));
		ArgList a = parser.parse(new String[]{"-e", "Testing1", "-e", "Testing2", "-e", "Testing3"});
		List<String> l = a.getAll("e", false);
		assertTrue(l.contains("Testing1"));
		assertTrue(l.contains("Testing2"));
		assertTrue(l.contains("Testing3"));
		assertFalse(l.contains("test"));
		l = a.getAll("e", true);
		assertTrue(l.contains("Testing1"));
		assertTrue(l.contains("Testing2"));
		assertTrue(l.contains("Testing3"));
		assertTrue(l.contains("test"));
		log.info("END testGetAllStringBoolean");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.args.ArgList#has(java.lang.String) has(String arg)}.
	 * @throws Exception error
	 */
	public final void testHas() throws Exception {
		log.info("BEGIN testHas");
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("flag").setDescription("test flag"));
		parser.addArgument(new ArgDef().setShortOption('z').setLongOpt("zig").setDescription("test missing flag"));
		ArgList a = parser.parse(new String[]{"-f"});
		assertTrue(a.has("f"));
		assertFalse(a.has("z"));
		log.info("END testHas");
	}
	
}
