/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.sql.SQLException;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * Connection Helper for RDB Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class RDBJenaConnect extends JenaConnect {
	/**
	 * The jdbc connection
	 */
	private IDBConnection conn;
	
	/**
	 * Constructor (Default Model)
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 * @throws ClassNotFoundException error loading class
	 */
	public RDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass) throws ClassNotFoundException {
		this(dbUrl, dbUser, dbPass, dbType, dbClass, null);
	}
	
	/**
	 * Constructor (Named Model)
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 * @param modelName the model to connect to
	 * @throws ClassNotFoundException error loading class
	 */
	public RDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String modelName) throws ClassNotFoundException {
		this(initDB(dbUrl, dbUser, dbPass, dbType, dbClass), modelName);
	}
	
	/**
	 * Constructor (connects to existing connection, using a specified named model)
	 * @param conn the connection to use
	 * @param modelName the model name to use
	 */
	private RDBJenaConnect(IDBConnection conn, String modelName) {
		if(modelName != null) {
			this.setModelName(modelName);
			this.setJenaModel(initModel(conn).openModel(modelName, false));
		} else {
			this.setModelName(GraphRDB.DEFAULT);
			this.setJenaModel(initModel(conn).createDefaultModel());
		}
	}
	
	@Override
	public void close() {
		this.getJenaModel().close();
		try {
			this.conn.close();
		} catch(Exception e) {
			// ignore
		}
	}

	@Override
	public JenaConnect neighborConnectClone(String modelName) throws IOException {
		try {
			return new RDBJenaConnect(new DBConnection(this.conn.getConnection(), this.conn.getDatabaseType()), modelName);
		} catch(SQLException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Get Model for a database connection
	 * @param dbConn the database connection
	 * @return the Model
	 */
	private ModelMaker initModel(IDBConnection dbConn) {
		this.conn = dbConn;
		return ModelFactory.createModelRDBMaker(dbConn);
	}
	
	/**
	 * Setup database connection
	 * @param dbUrl url of server
	 * @param dbUser username to connect with
	 * @param dbPass password to connect with
	 * @param dbType database type
	 * @param dbClass jdbc connection class
	 * @return the database connection
	 * @throws ClassNotFoundException error loading driver
	 */
	private static IDBConnection initDB(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass) throws ClassNotFoundException {
		Class.forName(dbClass);
		return new DBConnection(dbUrl, dbUser, dbPass, dbType);
	}

	@Override
	public void truncate() {
		this.getJenaModel().removeAll();
	}
	
}
