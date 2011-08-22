/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
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
		try {
			FileAide.createFolder(this.dbDir);
		} catch(IOException e) {
			throw new IllegalArgumentException("Invalid Directory", e);
		}
		if(modelName != null) {
			setModelName(modelName);
		} else {
			setModelName("urn:x-arq:DefaultGraph");
		}
		setJenaModel(getDataset().getNamedModel(getModelName()));
	}
	
	@Override
	public Dataset getDataset() {
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
		super.close();
		getJenaModel().close();
	}
	
	@Override
	public void printParameters() {
		super.printParameters();
		log.trace("type: 'tdb'");
		log.trace("dbDir: '" + this.dbDir + "'");
	}
	
	@Override
	public void sync() {
		// Do Nothing
	}
}
