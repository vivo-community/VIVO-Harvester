/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.qualify;

import org.vivoweb.ingest.util.Task;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public abstract class Qualify extends Task {
	//This code was marked as may cause compile errors by UCDetector.
	//Change visibility of class to Default
	//FIXME This code was marked as may cause compile errors by UCDetector.
	
	private Model model;
	
	/**
	 * @param model the model to set
	 */
	protected void setModel(Model model) {
		this.model = model;
	}
	
	/**
	 * @param configFileName the config file that describes the model to set
	 */
	protected void setModel(String configFileName) {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		
	}
	
	/**
	 * @return the model
	 */
	protected Model getModel() {
		return this.model;
	}
	
	/**
	 * @param dataType field to search
	 * @param matchValue match this value
	 * @param newValue replace matches with this value
	 * @param regex treat matchValue as a regular expression?
	 */
	public abstract void replace(String dataType, String matchValue, String newValue, boolean regex);
	
}
