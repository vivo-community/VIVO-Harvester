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
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class SPARQLQualify extends Qualify {
	private static Log log = LogFactory.getLog(SPARQLQualify.class);
	private HashMap<String, Map<String, String>> qualifyParams;
	
	/**
	 * Default Constructor
	 */
	public SPARQLQualify() {
		
	}
	
	/**
	 * Constructor
	 * @param model the JENA model to run qualifications on
	 */
	public SPARQLQualify(Model model) {
		setModel(model);
	}
	
	@Override
	public void replace(String dataType, String matchValue, String newValue, boolean regex) {
		if(regex) {
			regexReplace(dataType, matchValue, newValue);
		} else {
			strReplace("?uri", dataType, matchValue, newValue);
		}
	}
	
	private void strReplace(String uri, String dataType, String oldValue, String newValue) {
		// create query string
		String sQuery = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "DELETE { "+uri+" <"+dataType+"> ?value } "
				+ "INSERT { "+uri+" <"+dataType+"> \""+newValue+"\" } "
				+ "WHERE { "+uri+" <"+dataType+"> \""+oldValue+"\" }";
		
		// run update
		UpdateRequest ur = UpdateFactory.create(sQuery);
		UpdateAction.execute(ur, getModel());
	}

	private void regexReplace(String dataType, String regexMatch, String newValue) {
	// create query string
		String sQuery = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "Select ?record ?dataField "
				+ "WHERE { "
				+ "  ?record <"+dataType+"> ?dataField . "
				+ "}";
		
		// create query
		Query query = QueryFactory.create(sQuery);
		
		// execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, getModel());
		ResultSet resultSet = qe.execSelect();
		
		// read first result
		String data = null;
		if(resultSet.hasNext()) {
			QuerySolution result = resultSet.next();
			data = result.getLiteral(resultSet.getResultVars().get(1)).getString();
			if(data.matches(regexMatch)) {
				String newData = data.replaceAll(regexMatch, newValue);
				if(!newData.equals(data)) {
					String record = result.getLiteral(resultSet.getResultVars().get(0)).getString();
					strReplace(record, dataType, data, newData);
				}
			}
		}
	}
	

	@Override
	protected void acceptParams(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
		try {
			setModel(JenaConnect.parseConfig(getParam(params, "modelConfig", true)).getJenaModel());
		} catch(NullPointerException e) {
			throw new IOException("Jena Model Configuration Invalid",e);
		}
		this.qualifyParams = new HashMap<String,Map<String,String>>();
		for(String paramID : params.keySet()) {
			String[] temp = paramID.trim().split("\\.", 2);
			if(temp.length != 2) {
				throw new SAXException("Parameter improperly configured: "+paramID+" -> "+params.get(paramID));
			}
			temp[0] = temp[0].trim();
			temp[1] = temp[1].trim();
			if(!this.qualifyParams.containsKey(temp[0])) {
				this.qualifyParams.put(temp[0], new HashMap<String,String>());
			}
			this.qualifyParams.get(temp[0]).put(temp[1], params.get(paramID).trim());
		}
	}	

	@Override
	protected void runTask() throws NumberFormatException {
		for(Map<String, String> qualifyRun : this.qualifyParams.values()) {
			String dataType = getParam(qualifyRun, "dataType", true);
			String matchValue = getParam(qualifyRun, "matchValue", true);
			String newValue = getParam(qualifyRun, "newValue", true);
			String isRegex = getParam(qualifyRun, "isRegex", true);
			boolean regex = Boolean.parseBoolean(isRegex);
			log.trace("Running: replace(\""+dataType+"\", \""+matchValue+"\", \""+newValue+"\", "+regex+");");
			replace(dataType, matchValue, newValue, regex);
		}
	}

}
