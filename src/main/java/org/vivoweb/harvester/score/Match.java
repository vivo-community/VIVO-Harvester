/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.score;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.IterableAide;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.MemJenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * VIVO Match
 * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
 * @author Stephen Williams svwilliams@ctrip.ufl.edu
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class Match {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Match.class);
	/**
	 * Model for score data
	 */
	private final JenaConnect scoreJena;
	/**
	 * The most recent result set
	 */
	private Set<Map<String, String>> resultSet;
	/**
	 * the approximate number of lines per query
	 */
	private int batchSize = 100;
	
	/**
	 * Constructor
	 * @param scoreJena the model that contains the score values
	 */
	public Match(JenaConnect scoreJena) {
		if(scoreJena == null) {
			throw new IllegalArgumentException("Score Model cannot be null");
		}
		this.scoreJena = scoreJena;
		
		this.resultSet = null;
	}
	
	/**
	 * Get the approximate number of lines per query
	 * @return the batchSize
	 */
	public int getBatchSize() {
		return this.batchSize;
	}
	
	/**
	 * Set the approximate number of lines per query
	 * @param batchSize the new batchSize
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		if(this.batchSize <= 1) {
			log.warn("Batch Size of '"+batchSize+"' invalid, must be greater than or equal to 1.  Using '1' as Batch Size.");
			this.batchSize = 1;
		}
	}
	
	/**
	 * Clear the results from match
	 */
	public void clearMatchResults() {
		this.resultSet = null;
	}
	
	/**
	 * Get the result set from last match
	 * @return the result set
	 */
	protected Set<Map<String, String>> getResultSet() {
		return this.resultSet;
	}
	
	/**
	 * Check that the results are ready
	 * @param operation the operation attempting to be performed
	 * @throws IllegalStateException resultset is not initialized
	 */
	private void checkResultsReady(String operation) throws IllegalStateException {
		if(this.resultSet == null) {
			throw new IllegalStateException("Cannot perform "+operation+" when no match result set built!");
		}
	}
	
	/**
	 * Find all nodes in the given namepsace matching on the given predicates
	 * @param threshold the value to look for in the sparql query
	 * @throws IOException error connecting
	 */
	public void match(float threshold) throws IOException {
		//Build query to find all nodes matching above the given threshold
		String sQuery = "" +
				"PREFIX scoreValue: <http://vivoweb.org/harvester/scoreValue/>\n" +
				"SELECT DISTINCT ?sVivo ?sInput (sum(?weightValue) AS ?sum) \n" +
				"WHERE { \n" +
				"  ?s scoreValue:InputRes ?sInput . \n" +
				"  ?s scoreValue:VivoRes ?sVivo . \n" +
				"  ?s scoreValue:hasScoreValue ?value . \n" +
				"  ?value scoreValue:WeightedScore ?weightValue . \n" +
				"}" +
				"GROUP BY ?sVivo ?sInput \n" +
				"HAVING (?sum >= " + threshold + ") \n" +
				"ORDER BY ?sInput";
		Set<Map<String, String>> uriMatchEntrySet = new TreeSet<Map<String, String>>(new Comparator<Map<String, String>>() {
			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				StringBuilder o1value = new StringBuilder();
				for(String s1 : o1.keySet()) {
					o1value.append("["+s1+"|"+o1.get(s1)+"]");
				}
				StringBuilder o2value = new StringBuilder();
				for(String s2 : o2.keySet()) {
					o2value.append("["+s2+"|"+o2.get(s2)+"]");
				}
				return o1value.toString().compareTo(o2value.toString());
			}
		});
		
		//log trace
		log.trace("Query Start");
		log.trace("Query:\n" + sQuery);
		Iterable<QuerySolution> matchQuery = IterableAide.adapt(this.scoreJena.executeSelectQuery(sQuery));
		log.trace("Query Complete");
		Map<String, String> tempMap = null;
		for(QuerySolution solution : matchQuery) {
			tempMap = new HashMap<String, String>();
			String sInputURI = solution.getResource("sInput").getURI();
			String sVivoURI = solution.getResource("sVivo").getURI();
			Float score = Float.valueOf(solution.getLiteral("sum").getFloat());
			tempMap.put("sInputURI", sInputURI);
			tempMap.put("sVivoURI", sVivoURI);
			tempMap.put("score", score.toString());
			uriMatchEntrySet.add(tempMap);
		}
		
		this.resultSet = uriMatchEntrySet;
	}
	
	/**
	 * Rename the resource set as the key to the value matched
	 * @param model model to apply renames to
	 */
	public void rename(JenaConnect model) {
		checkResultsReady("rename");
		log.trace("Beginning rename loop");
		int total = this.resultSet.size();
		int count = 0;
		for(Map<String,String> entry : this.resultSet) {
			String oldUri = entry.get("sInputURI").trim();
			String newUri = entry.get("sVivoURI").trim();
			count++;
			float percent = Math.round(10000f * count / total) / 100f;
			log.trace("(" + count + "/" + total + ": " + percent + "%): Renaming match <" + oldUri + "> to <" + newUri + ">");
			//get resource in input model and perform rename
			if(!oldUri.equals(newUri)) {
				Resource res = model.getJenaModel().getResource(oldUri);
				ResourceUtils.renameResource(res, newUri);
			} else {
				log.warn("Discarding Illegal Rename To Same URI!");
				log.debug("Scoring model contains comparison data of the uri <" + oldUri + "> to <" + newUri + "> which are the same! Should not have happened!");
				String sQuery = "" +
					"PREFIX scoreValue: <http://vivoweb.org/harvester/scoreValue/>\n" +
					"DESCRIBE ?s \n" +
					"WHERE { \n" +
					"  ?s scoreValue:InputRes <"+oldUri+"> . \n" +
					"  ?s scoreValue:VivoRes <"+newUri+"> . \n" +
					"}";
				log.trace("Dump Score Data Query:\n"+sQuery);
				try {
					log.trace("Score Data:\n"+this.scoreJena.executeDescribeQuery(sQuery).exportRdfToString());
				} catch(IOException e) {
					log.error("Error Dumping Score Data", e);
				}
			}
		}
		model.sync();
	}
	
	/**
	 * Link matched scoreResources to vivoResources using given linking predicates
	 * @param model model to add link statements to
	 * @param vivoToInput vivo to input property
	 * @param inputToVivo input to vivo property
	 */
	public void link(JenaConnect model, String vivoToInput, String inputToVivo) {
		checkResultsReady("link");
		Property vivoToInputProperty = ResourceFactory.createProperty(vivoToInput);
		Property inputToVivoProperty = ResourceFactory.createProperty(inputToVivo);
		
		log.trace("Beginning link method loop");
		int total = this.resultSet.size();
		int count = 0;
		for(Map<String, String> entry : this.resultSet) {
			// get resources and add linking triples
			String inputUri = entry.get("sInputURI");
			String vivoUri = entry.get("sVivoURI");
			Resource inputRes = model.getJenaModel().getResource(inputUri);
			Resource vivoRes = this.scoreJena.getJenaModel().getResource(vivoUri);
			float percent = Math.round(10000f * count / total) / 100f;
			log.trace("(" + count + "/" + total + ": " + percent + "%): Linking match <" + inputUri + "> to <" + vivoUri + ">");
			log.trace("Adding input to vivo match link [ <" + inputUri + "> <" + inputToVivo + "> <" + vivoUri + "> ]");
			model.getJenaModel().add(inputRes, inputToVivoProperty, vivoRes);
			log.trace("Adding vivo to input match link [ <" + vivoUri + "> <" + vivoToInput + "> <" + inputUri + "> ]");
			model.getJenaModel().add(vivoRes, vivoToInputProperty, inputRes);
		}
		model.sync();
	}
	
	/**
	 * Clear out rdf:type and literal values of matched scoreResources
	 * @param model model to clear types and literals from
	 * @throws IOException error connecting
	 */
	public void clearTypesAndLiterals(JenaConnect model) throws IOException {
		checkResultsReady("clear types and literals");
		if(!this.resultSet.isEmpty()) {
			log.trace("Beginning clear types and literals");
			Set<String> uriFilters = new HashSet<String>();
			int count = 0;
			int inc = 0;
			for(Map<String, String> entry : this.resultSet) {
				String uri = entry.get("sInputURI");
				if(inc == this.batchSize){
					buildTypesAndLiteralsQuery(model, uriFilters);
					count += inc;
					inc = 0;
					uriFilters.clear();
				}
				uriFilters.add("(str(?s) = \"" + uri + "\")");
				inc++;
			}
			buildTypesAndLiteralsQuery(model, uriFilters);
			count += inc;
			log.trace("Cleared " + count + " types and literals");
			log.trace("Ending clear types and literals");
		}
		model.sync();
	}
	
	/**
	 * Build the query for matching types and literals
	 * @param model model to run query on
	 * @param uriFilters uris to match on
	 * @throws IOException error connecting
	 */
	private void buildTypesAndLiteralsQuery(JenaConnect model, Set<String> uriFilters) throws IOException {
		String query = "" +
		"DELETE {\n" +
		"  ?s ?p ?o\n" +
		"} WHERE {\n" +
		"  ?s ?p ?o .\n" +
		"  FILTER ( isLiteral(?o || (str(?p)='http://www.w3.org/1999/02/22-rdf-syntax-ns#type')) && (" + StringUtils.join(uriFilters, " || ") + ")) .\n" +
		"}";
//		String conQuery = query.replaceFirst("DELETE", "CONSTRUCT");
//		log.debug("Construct Query:\n" + conQuery);
//		log.debug("Constructed Literal Set:\n" + this.inputJena.executeConstructQuery(conQuery).exportRdfToString());
		log.trace("Clear Literal Query:\n" + query);
		model.executeUpdateQuery(query);		
	}
	
	/**
	 * Output the nodes (and all related nodes) in the result set
	 * @param model Model from which to get the original statements from
	 * @return the completed model of matches
	 * @throws IOException no idea why it throws this
	 */
	public JenaConnect outputMatches(JenaConnect model) throws IOException {
		JenaConnect returnModel = new MemJenaConnect();
		outputMatches(model, returnModel);
		return returnModel;
	}
	
	/**
	 * Output the nodes (and all related nodes) in the result set
	 * @param model Model from which to get the original statements from
	 * @param outputModel the model to load the completed set of matches
	 * @throws IOException no idea why it throws this
	 */
	public void outputMatches(JenaConnect model, JenaConnect outputModel) throws IOException {
		checkResultsReady("output matches");
		log.trace("Beginning separate output of matches");
		Stack<String> linkRes = new Stack<String>();
		int i = 0;
		for(Map<String, String> entry : this.resultSet) {
			String oldUri = entry.get("sInputURI");
			String newUri = entry.get("sVivoURI");
			i++;
			log.trace("Getting statements for matchSet " + oldUri);
			StmtIterator subjectStmts = model.getJenaModel().listStatements(null, null, model.getJenaModel().getResource(newUri));
			
			while(subjectStmts.hasNext()) {
				Statement stmt = subjectStmts.nextStatement();
				Resource subj = stmt.getSubject();
				if(!linkRes.contains(subj)) {
//					log.trace("Submitting to recursive build " + subj.getURI());
					linkRes.push(subj.getURI());
					outputModel.getJenaModel().add(recursiveBuild(subj, linkRes));
				}
			}
			
			outputModel.getJenaModel().add(model.getJenaModel().listStatements(null, null, model.getJenaModel().getResource(newUri)));
		}
		log.debug("Outputted " + i + " matches");
		outputModel.sync();
	}
	
	/**
	 * @param mainRes item to push into returnModel
	 * @param linkRes list of items to not move to
	 * @return list of recursively built statements
	 * @throws IOException I have no idea why mem throws this
	 */
	private static List<Statement> recursiveBuild(Resource mainRes, Stack<String> linkRes) throws IOException {
		StmtIterator mainStmts = mainRes.listProperties();
		List<Statement> rtnStmtList = mainRes.listProperties().toList();
		
		while(mainStmts.hasNext()) {
			Statement stmt = mainStmts.nextStatement();
			
			//todo change the equals t o
			if(stmt.getObject().isResource() && !linkRes.contains(stmt.getObject().asResource().getURI()) && !stmt.getObject().asResource().equals(mainRes)) {
				linkRes.push(mainRes.getURI());
//				log.trace("Submitting to rcb from within rcb " + stmt.getObject().asResource().getURI());
				rtnStmtList.addAll(recursiveBuild(stmt.getObject().asResource(), linkRes));
			}			
		}
		
		return rtnStmtList;
	}
}