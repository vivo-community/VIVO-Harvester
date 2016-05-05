/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection Helper for Database Backed Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class DBJenaConnect extends JenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(DBJenaConnect.class);
	/**
	 * JDBC Connection Line
	 */
	private final String dbUrl;
	/**
	 * JDBC username
	 */
	private final String dbUser;
	/**
	 * JDBC password
	 */
	private final String dbPass;
	/**
	 * Database Type (for Jena)
	 */
	private final String dbType;
	/**
	 * JDBC driver class
	 */
	private final String dbClass;
	
	/**
	 * Base Constructor
	 * @param dbUrl the JDBC Connection Line
	 * @param dbUser the JDBC username
	 * @param dbPass the JDBC password
	 * @param dbType the Database Type (for Jena)
	 * @param dbClass the JDBC driver class
	 */
	protected DBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass) {
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
		this.dbType = dbType;
		this.dbClass = dbClass;
	}
	
	/**
	 * Clone Constructor
	 * @param original the original to clone
	 */
	protected DBJenaConnect(DBJenaConnect original) {
		this.dbUrl = original.dbUrl;
		this.dbUser = original.dbUser;
		this.dbPass = original.dbPass;
		this.dbType = original.dbType;
		this.dbClass = original.dbClass;
	}
	
	/**
	 * Get the dbType for Jena
	 * @return the dbType
	 */
	protected String getDbType() {
		return this.dbType;
	}
	
	/**
	 * Build a new jdbc connection using connect info
	 * @return a new jdbc connection
	 * @throws IOException error making jdbc connection
	 */
	protected Connection buildConnection() throws IOException {
		try {
			Class.forName(this.dbClass);
			return DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPass);
		} catch(SQLException e) {
			throw new IOException(e);
		} catch(ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void printParameters() {
		super.printParameters();
		log.trace("dbUrl: '" + this.dbUrl + "'");
		log.trace("dbType: '" + this.dbType + "'");
		log.trace("dbClass: '" + this.dbClass + "'");
	}
	
	@Override
	public void sync() {
		// Do Nothing
	}
}
