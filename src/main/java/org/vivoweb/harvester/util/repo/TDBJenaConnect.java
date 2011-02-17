/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Connection Helper for TDB Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class TDBJenaConnect extends JenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(TDBJenaConnect.class);
	/**
	 * Mapping of directory to Dataset
	 */
	private static HashMap<String, Dataset> dirDatasets = new HashMap<String, Dataset>();
	/**
	 * the TDB directory name
	 */
	private final String dbDir;
	
	/**
	 * Clone Constructor
	 * @param original the original to clone
	 * @param modelName the modelname to connect to
	 */
	private TDBJenaConnect(TDBJenaConnect original, String modelName) {
		this(original.dbDir, modelName);
	}
	
	/**
	 * Constructor (Default Model)
	 * @param dbDir tdb directory name
	 */
	public TDBJenaConnect(String dbDir) {
		this(dbDir, null);
	}
	
	/**
	 * Constructor (TDB Named Model)
	 * @param dbDir tdb directory name
	 * @param modelName the model to connect to
	 */
	public TDBJenaConnect(String dbDir, String modelName) {
		this.dbDir = dbDir;
		init(modelName);
	}
	
	/**
	 * Initialize the tdb jena connect
	 * @param modelName the model name to use
	 */
	private void init(String modelName) {
		if(modelName != null) {
			setModelName(modelName);
		} else {
			setModelName("urn:x-arq:DefaultGraph");
		}
		setJenaModel(getDataSet().getNamedModel(getModelName()));
	}
	
	@Override
	public Dataset getDataSet() {
		log.debug("dbDir: " + this.dbDir);
		if(!dirDatasets.containsKey(this.dbDir)) {
			dirDatasets.put(this.dbDir, TDBFactory.createDataset(this.dbDir));
		}
		return dirDatasets.get(this.dbDir);
	}
	
	@Override
	public JenaConnect neighborConnectClone(String modelName) {
		return new TDBJenaConnect(this, modelName);
	}
	
	@Override
	public void close() {
		getJenaModel().close();
	}
	
	@Override
	public void truncate() {
		getJenaModel().removeAll();
	}
	
	@Override
	public void printParameters() {
		super.printParameters();
		log.debug("type: 'sdb'");
		log.debug("dbDir: '" + this.dbDir + "'");
	}
}
