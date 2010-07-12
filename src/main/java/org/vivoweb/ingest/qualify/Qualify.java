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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.JenaConnect;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Massage field data into the exact format we need
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class Qualify extends Task {
	
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Qualify.class);
	/**
	 * Jena Model we are working in
	 */
	private Model model;
	
	/**
	 * Setter for model
	 * @param newModel the model to set
	 */
	protected void setModel(Model newModel) {
		this.model = newModel;
	}
	
	/**
	 * Setter for model
	 * @param configFileName the config file that describes the model to set
	 */
	protected void setModel(String configFileName) {
		try {
			this.model = JenaConnect.parseConfig(configFileName).getJenaModel();
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(),e);
		} catch(SAXException e) {
			log.error(e.getMessage(),e);
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Getter for model
	 * @return the model
	 */
	protected Model getModel() {
		return this.model;
	}
	
	/**
	 * Replace data matching dataType & matchValue with newValue
	 * @param dataType field to search
	 * @param matchValue match this value
	 * @param newValue replace matches with this value
	 * @param regex treat matchValue as a regular expression?
	 */
	public abstract void replace(String dataType, String matchValue, String newValue, boolean regex);
	
}
