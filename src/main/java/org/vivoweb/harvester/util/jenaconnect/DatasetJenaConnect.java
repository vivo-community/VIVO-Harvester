package org.vivoweb.harvester.util.jenaconnect;

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
		getJenaModel().close();
	}
	
}
