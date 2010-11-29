/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;

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
	 * @throws ClassNotFoundException no such class
	 */
	public SDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String dbLayout) throws ClassNotFoundException {
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
	 * @throws ClassNotFoundException no such class
	 */
	public SDBJenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass, String dbLayout, String modelName) throws ClassNotFoundException {
		Class.forName(dbClass);
		this.store = SDBFactory.connectStore(SDBConnectionFactory.create(dbUrl, dbUser, dbPass), new StoreDesc(dbLayout, dbType));
		this.setJenaModel(SDBFactory.connectNamedModel(this.store, modelName));
	}
	
	/**
	 * Constructor (connects to existing store, using a specified named model)
	 * @param oldStore the store to use
	 * @param modelName the model name to use
	 */
	private SDBJenaConnect(Store oldStore, String modelName) {
		this.store = oldStore;
		this.setJenaModel(SDBFactory.connectNamedModel(this.store, modelName));
	}

	@Override
	public JenaConnect connect(String modelName) throws IOException {
		return new SDBJenaConnect(this.store, modelName);
	}
	
	@Override
	public void close() {
		this.getJenaModel().close();
		this.store.close();
	}
	
}
