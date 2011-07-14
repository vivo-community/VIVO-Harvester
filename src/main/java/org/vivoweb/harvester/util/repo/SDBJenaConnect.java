/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Connection Helper for SDB Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class SDBJenaConnect extends DBJenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SDBJenaConnect.class);
	/**
	 * The sdb store
	 */
	private final Store store;
	/**
	 * the SDB layout scheme
	 */
	private final String dbLayout;
	
	/**
	 * Clone Constructor
	 * @param original the original to clone
	 * @param modelName the modelname to connect to
	 * @throws IOException error creating connection
	 */
	private SDBJenaConnect(SDBJenaConnect original, String modelName) throws IOException {
		super(original);
		this.dbLayout = original.dbLayout;
		this.store = connectStore(original.buildConnection(), original.getDbType(), this.dbLayout);
		init(modelName);
	}
	
	/**
	 * Constructor (Default Model)
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 * @param dbLayout sdb layout type
	 * @throws IOException error connecting to store
	 */
	public SDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String dbLayout) throws IOException {
		this(dbUrl, dbUser, dbPass, dbType, dbClass, dbLayout, null);
	}
	
	/**
	 * Constructor (SDB Named Model)
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 * @param dbLayout sdb layout type
	 * @param modelName the model to connect to
	 * @throws IOException error connecting to store
	 */
	public SDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String dbLayout, String modelName) throws IOException {
		super(dbUrl, dbUser, dbPass, dbType, dbClass);
		this.dbLayout = dbLayout;
		this.store = connectStore(buildConnection(), getDbType(), this.dbLayout);
		init(modelName);
	}
	
	/**
	 * Connect to an SDB store
	 * @param conn JDBC Connection
	 * @param dbType Jena database type
	 * @param dbLayout sdb layout type
	 * @return the store
	 */
	protected static Store connectStore(Connection conn, String dbType, String dbLayout) {
		return SDBFactory.connectStore(SDBConnectionFactory.create(conn), new StoreDesc(dbLayout, dbType));
	}
	
	/**
	 * Initialize the sdb jena connect
	 * @param modelName the model name to use
	 * @throws IOException error connecting to store
	 */
	private void init(String modelName) throws IOException {
		initStore();
		if(modelName != null) {
			setModelName(modelName);
			setJenaModel(SDBFactory.connectNamedModel(this.store, modelName));
		} else {
			setModelName(Quad.defaultGraphIRI.getURI());
			setJenaModel(SDBFactory.connectDefaultModel(this.store));
		}
	}
	
	@Override
	public Dataset getDataset() {
		return SDBFactory.connectDataset(this.store);
	}
	
	@Override
	public JenaConnect neighborConnectClone(String modelName) throws IOException {
		return new SDBJenaConnect(this, modelName);
	}
	
	/**
	 * Initialize the store if needed
	 * @throws IOException error connecting to store
	 */
	private void initStore() throws IOException {
		try {
			if(!StoreUtils.isFormatted(this.store)) {
				this.store.getTableFormatter().create();
			}
		} catch(SQLException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void close() {
		super.close();
		getJenaModel().close();
		this.store.close();
		this.store.getConnection().close();
	}
	
	@Override
	public void printParameters() {
		super.printParameters();
		log.trace("type: 'sdb'");
		log.trace("dbLayout: '" + this.dbLayout + "'");
	}
}
