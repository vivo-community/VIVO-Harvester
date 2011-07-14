/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.IterableAdaptor;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;

/**
 * Connection Helper for RDB Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class RDBJenaConnect extends DBJenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RDBJenaConnect.class);
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
	 * @throws IOException error connecting
	 */
	public RDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass) throws IOException {
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
	 * @throws IOException error connecting
	 */
	public RDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String modelName) throws IOException {
		super(dbUrl, dbUser, dbPass, dbType, dbClass);
		this.conn = initDB(buildConnection(), getDbType());
		init(modelName);
	}
	
	/**
	 * Clone Constructor
	 * @param original the original to clone
	 * @param modelName the modelname to connect to
	 * @throws IOException error creating connection
	 */
	private RDBJenaConnect(RDBJenaConnect original, String modelName) throws IOException {
		super(original);
		this.conn = initDB(buildConnection(), getDbType());
		init(modelName);
	}
	
	/**
	 * Constructor (connects to existing connection, using a specified named model)
	 * @param modelName the model name to use
	 */
	private void init(String modelName) {
		if(modelName != null) {
			setModelName(modelName);
			setJenaModel(initModel(this.conn).openModel(modelName, false));
		} else {
			setModelName(GraphRDB.DEFAULT);
			setJenaModel(initModel(this.conn).createDefaultModel());
		}
	}
	
	@Override
	public void close() {
		super.close();
		getJenaModel().close();
		try {
			this.conn.close();
		} catch(Exception e) {
			// ignore
		}
	}
	
	@Override
	public JenaConnect neighborConnectClone(String modelName) throws IOException {
		return new RDBJenaConnect(this, modelName);
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
	 * @param conn JDBC connection
	 * @param dbType database type
	 * @return the database connection
	 */
	private static IDBConnection initDB(Connection conn, String dbType) {
		return new DBConnection(conn, dbType);
	}
	
	@Override
	public Dataset getDataset() throws IOException {
		DataSourceImpl ds = new DataSourceImpl(getJenaModel());
		for(String name : IterableAdaptor.adapt(this.conn.getAllModelNames())) {
			ds.addNamedModel(name, neighborConnectClone(name).getJenaModel());
		}
		return ds;
	}
	
	@Override
	public void printParameters() {
		super.printParameters();
		log.trace("type: 'rdb'");
	}
}
