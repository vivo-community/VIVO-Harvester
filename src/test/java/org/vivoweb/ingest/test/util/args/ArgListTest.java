/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.test.util.args;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import junit.framework.TestCase;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ArgListTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ArgListTest.class);
	/** */
	private static ArgParser parser;
	
	@Override
	protected void setUp() throws Exception {
		parser = new ArgParser("ArgListTest");
	}
	
	@Override
	protected void tearDown() throws Exception {
		parser = null;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private final void makeArgList() {
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("driver").withParameter(true, "JDBC_DRIVER").setDescription("jdbc driver class").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("connection").withParameter(true, "JDBC_CONN").setDescription("jdbc connection string").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("username").withParameter(true, "USERNAME").setDescription("database username").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("password").withParameter(true, "PASSWORD").setDescription("database password").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.args.ArgList#get(java.lang.String) get(String arg)}.
	 */
	public final void testGet() {
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "OUT_FILE").setDescription("output file").setRequired(true));
		try {
			ArgList a = new ArgList(parser, new String[]{"-o", "Testing"});
			assertEquals(a.get("o"), "Testing");
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.args.ArgList#getAll(java.lang.String) getAll(String arg)}.
	 */
	public final void testGetAllString() {
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("except").withParameters(true, "EXCEPTION").setDescription("exception").setDefaultValue("test"));
		try {
			ArgList a = new ArgList(parser, new String[]{"-e", "Testing1", "-e", "Testing2", "-e", "Testing3"});
			List<String> l = a.getAll("e");
			assertTrue(l.contains("Testing1"));
			assertTrue(l.contains("Testing2"));
			assertTrue(l.contains("Testing3"));
			assertFalse(l.contains("test"));
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.args.ArgList#getAll(java.lang.String, boolean) getAll(String arg,
	 * boolean includeDefaultValue)}.
	 */
	public final void testGetAllStringBoolean() {
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("except").withParameters(true, "EXCEPTION").setDescription("exception").setDefaultValue("test"));
		try {
			ArgList a = new ArgList(parser, new String[]{"-e", "Testing1", "-e", "Testing2", "-e", "Testing3"});
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
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.util.args.ArgList#has(java.lang.String) has(String arg)}.
	 */
	public final void testHas() {
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("flag").setDescription("test flag"));
		parser.addArgument(new ArgDef().setShortOption('z').setLongOpt("zig").setDescription("test missing flag"));
		try {
			ArgList a = new ArgList(parser, new String[]{"-f"});
			assertTrue(a.has("f"));
			assertFalse(a.has("z"));
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
}
