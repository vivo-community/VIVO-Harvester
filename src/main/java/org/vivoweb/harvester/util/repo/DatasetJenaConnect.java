/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import com.hp.hpl.jena.query.Dataset;

/**
 * Connection Helper for models in a Dataset
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class DatasetJenaConnect extends JenaConnect {
	
	/**
	 * The dataset we are connected to
	 */
	private Dataset ds;
	
	/**
	 * Constructor
	 * @param jenaDataset the dataset to use
	 * @param modelName the model name to connect to
	 */
	public DatasetJenaConnect(Dataset jenaDataset, String modelName) {
		this.ds = jenaDataset;
		setJenaModel(jenaDataset.getNamedModel(modelName));
		setModelName(modelName);
	}
	
	@Override
	public JenaConnect neighborConnectClone(String newModelName) throws IOException {
		return new DatasetJenaConnect(this.ds, newModelName);
	}
	
	@Override
	public Dataset getDataset() {
		return this.ds;
	}
	
	@Override
	public void close() {
		super.close();
		getJenaModel().close();
	}
	
	@Override
	public void sync() {
		// Do Nothing
	}
}
