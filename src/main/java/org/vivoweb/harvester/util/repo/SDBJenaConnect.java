/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.sql.SQLException;
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
public class SDBJenaConnect extends JenaConnect {
	/**
	 * The sdb store
	 */
	private Store store;

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
		this(connectStore(dbUrl, dbUser, dbPass, dbType, dbClass, dbLayout), modelName);
	}
	
	/**
	 * Constructor (connects to existing store, using a specified named model)
	 * @param oldStore the store to use
	 * @param modelName the model name to use
	 * @throws IOException error connecting to store
	 */
	protected SDBJenaConnect(Store oldStore, String modelName) throws IOException {
		init(oldStore, modelName);
	}
	
	/**
	 * Connect to an SDB store
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 * @param dbLayout sdb layout type
	 * @return the store
	 * @throws IOException error connecting to store
	 */
	public static Store connectStore(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String dbLayout) throws IOException {
		try {
			Class.forName(dbClass);
		} catch(ClassNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		}
		return SDBFactory.connectStore(SDBConnectionFactory.create(dbUrl, dbUser, dbPass), new StoreDesc(dbLayout, dbType));
	}
	
	/**
	 * Initialize the sdb jena connect
	 * @param oldStore the store to use
	 * @param modelName the model name to use
	 * @throws IOException error connecting to store
	 */
	protected void init(Store oldStore, String modelName) throws IOException {
		this.store = oldStore;
		initStore();
		if(modelName != null) {
			this.setModelName(modelName);
			this.setJenaModel(SDBFactory.connectNamedModel(this.store, modelName));
		} else {
			this.setModelName(Quad.defaultGraphIRI.getURI());
			this.setJenaModel(SDBFactory.connectDefaultModel(this.store));
		}
	}
	
	@Override
	public Dataset getDataSet() {
		return SDBFactory.connectDataset(this.store);
	}

	@Override
	public JenaConnect neighborConnectClone(String modelName) throws IOException {
		return new SDBJenaConnect(this.store, modelName);
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
			throw new IOException(e.getMessage(), e);
		}
	}
	
	@Override
	public void close() {
		this.getJenaModel().close();
		this.store.getConnection().close();
		this.store.close();
	}

	@Override
	public void truncate() {
		this.store.getTableFormatter().truncate();
	}
	
}
