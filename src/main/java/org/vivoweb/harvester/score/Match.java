/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation Christopher
 * Barnes, Narayan Raum - scoring ideas and algorithim Yang Li - pairwise scoring Algorithm Christopher Barnes - regex
 * scoring algorithim
 ******************************************************************************/
package org.vivoweb.harvester.score;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.ResourceUtils;
//import com.hp.hpl.jena.rdf.model.Statement;
//import com.hp.hpl.jena.rdf.model.StmtIterator;
//import org.vivoweb.harvester.util.repo.MemJenaConnect;
//import java.util.Stack;

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
	 * Model for VIVO instance
	 */
	private final JenaConnect scoreJena;
	/**
	 * Model where input is stored
	 */
	private final JenaConnect inputJena;
	/**
	 * Link the resources found by match Algorithm
	 */
	private final String linkProp;
	/**
	 * Rename resources found by match Algorithm
	 */
	private final boolean renameRes;
	/**
	 * Pubmed Match threshold
	 */
	private final String matchThreshold;
	/**
	 * Clear all literal values out of matched sets
	 */
	private final boolean clearLiterals;
	
	/**
	 * Constructor
	 * @param inputJena model containing statements to be scored
	 * @param scoreJena the model that contains the score values
	 * @param thresholdArg pubmed threshold
	 * @param renameRes should I just rename the args?
	 * @param linkProp bidirectional link
	 * @param clearLiterals clear all the literal values out of matches
	 */
	public Match(JenaConnect inputJena, JenaConnect scoreJena, boolean renameRes, String thresholdArg, String linkProp, boolean clearLiterals) {
		if(scoreJena == null) {
			throw new IllegalArgumentException("Score Model cannot be null");
		}
		this.scoreJena = scoreJena;
		
		if(inputJena == null) {
			throw new IllegalArgumentException("Match Input cannot be null");
		}
		this.inputJena = inputJena;
		
		this.matchThreshold = thresholdArg;
		this.renameRes = renameRes;
		this.linkProp = linkProp;
		this.clearLiterals = clearLiterals;
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 */
	public Match(String... args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	public Match(ArgList opts) throws IOException {
		// Connect to score data model
		this.scoreJena = JenaConnect.parseConfig(opts.get("s"), opts.getValueMap("S"));
		
		// Connect to input model
		this.inputJena = JenaConnect.parseConfig(opts.get("i"), opts.getValueMap("I"));
		
		this.renameRes = opts.has("r");
		this.linkProp = opts.get("l");
		this.matchThreshold = opts.get("m");
		this.clearLiterals = opts.has("c");
	}
	
	/**
	 * Find all nodes in the given namepsace matching on the given predicates
	 * @param threshold the value to look for in the sparql query
	 * @return mapping of the found matches
	 * @throws IOException error connecting to dataset
	 */
	private Map<String,String> match(String threshold) throws IOException{
		//Build query to find all nodes matching on the given predicates
		StringBuilder sQuery =	new StringBuilder(
				"PREFIX scoring: <http://vivoweb.org/harvester/scorevalue/>\n" +
				"SELECT DISTINCT ?sVivo ?sInput" +
				"Where {" +
				"  ?s scoring:InputRes ?sInput ." +
				"  ?s scoring:VivoRes ?sVivo ." +
				"  ?s scoring:WeightedScore ?weightValue ." +
				"  FILTER(?weightValue >= " + threshold + " )" +
				"}"
		);
						
		Dataset ds = this.scoreJena.getConnectionDataSet();
		Query query = QueryFactory.create(sQuery.toString(), Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, ds);
		HashMap<String,String> uriMatchMap = new HashMap<String,String>();
		for(QuerySolution solution : IterableAdaptor.adapt(queryExec.execSelect())) {
			String sScoreURI = solution.getResource("sInput").getURI();
			String sVivoURI = solution.getResource("sVivo").getURI();

			uriMatchMap.put(sScoreURI, sVivoURI);
			log.debug("Match found: <"+sScoreURI+"> in Match matched with <"+sVivoURI+"> in Vivo");
		}

		log.info("Match found " + uriMatchMap.keySet().size() + " links between Vivo and the Input model");
		
		return uriMatchMap;
	}
	
	/**
	 * Rename the resource set as the key to the value matched
	 * @param matchSet a result set of scoreResources, vivoResources
	 */
	private void rename(Map<String,String> matchSet){
		for(String oldUri : matchSet.keySet()) {
			//get resource in input model and perform rename
			Resource res = this.inputJena.getJenaModel().getResource(oldUri);
			ResourceUtils.renameResource(res, matchSet.get(oldUri));
		}
	}
	
	/**
	 * Link matched scoreResources to vivoResources using given linking predicates
	 * @param matchSet a mapping of matched scoreResources to vivoResources
	 * @param objPropList the object properties to be used to link the two items set in string form -> vivo-object-property-to-scoreJena-object = scoreJena-object-property-to-vivo-object
	 */
	private void link(Map<String,String> matchSet, String objPropList) {
		//split the objPropList into its vivo and scoreJena components (vivo->scoreJena=scoreJena->vivo)
		String[] objPropArray = objPropList.split("=");
		if(objPropArray.length != 2){
			throw new IllegalArgumentException("Two object properties vivo-object-property-to-scoreJena-object and scoreJena-object-property-to-vivo-object " +
					"sepearated by an equal sign must be set to link resources");
		}
		Property vivoToScoreObjProp = ResourceFactory.createProperty(objPropArray[0]);
		Property scoreToVivoObjProp = ResourceFactory.createProperty(objPropArray[1]);
		
		for(String oldUri : matchSet.keySet()) {
			// get resources and add linking triples
			Resource scoreRes = this.inputJena.getJenaModel().getResource(oldUri);	
			Resource vivoRes = this.scoreJena.getJenaModel().getResource(matchSet.get(oldUri));
			this.inputJena.getJenaModel().add(vivoRes, vivoToScoreObjProp, scoreRes);
			this.inputJena.getJenaModel().add(scoreRes, scoreToVivoObjProp, vivoRes);			
		}
	}
	
	/**
	 * Clear out literal values of matched scoreResources
	 * TODO: TEST
	 * @param resultMap a mapping of matched scoreResources to vivoResources
	 * @throws IOException error building construct
	 */
	private void clearLiterals(Map<String, String> resultMap) throws IOException {
		if(!resultMap.values().isEmpty()) {
			Set<String> uriFilters = new HashSet<String>();
			for(String uri : resultMap.values()) {
				uriFilters.add("(str(?s) = \"" + uri + "\")");
			}
			String query = "" +
			"DELETE {\n" +
			"  ?s ?p ?o\n" +
			"} WHERE {\n" +
			"  ?s ?p ?o .\n" +
			"  FILTER ( isLiteral(?o || (str(?p)=='http://www.w3.org/1999/02/22-rdf-syntax-ns#type')) && ("+StringUtils.join(uriFilters, " || ")+")) .\n" +
			"}";
			String conQuery = query.replaceFirst("DELETE", "CONSTRUCT");
			log.debug("Construct Query:\n"+conQuery);
			log.debug("Constructed Literal Set:\n"+this.inputJena.executeConstructQuery(conQuery).toString());
			log.debug("Clear Literal Query:\n" + query);
			this.inputJena.executeUpdateQuery(query);
		}
	}
	
	/* * Traverses paperNode and adds to toReplace model
	 * @param mainRes the main resource
	 * @param linkRes the resource to link it to
	 * @return the model containing the sanitized info so far
	 * @throws IOException error connecting
	 * /
	private static JenaConnect recursiveBuild(Resource mainRes, Stack<Resource> linkRes) throws IOException {
		JenaConnect returnModel = new MemJenaConnect();
		StmtIterator mainStmts = mainRes.listProperties();
		
		while (mainStmts.hasNext()) {
			Statement stmt = mainStmts.nextStatement();
			
				// log.debug(stmt.toString());
				returnModel.getJenaModel().add(stmt);
				
				//todo change the equals t o
				if (stmt.getObject().isResource() && !linkRes.contains(stmt.getObject().asResource()) && !stmt.getObject().asResource().equals(mainRes)) {
					linkRes.push(mainRes);
					returnModel.getJenaModel().add(recursiveBuild(stmt.getObject().asResource(), linkRes).getJenaModel());
					linkRes.pop();
				}
				if (!linkRes.contains(stmt.getSubject()) && !stmt.getSubject().equals(mainRes)) {
					linkRes.push(mainRes);
					returnModel.getJenaModel().add(recursiveBuild(stmt.getSubject(), linkRes).getJenaModel());
					linkRes.pop();
				}
		}
		
		return returnModel;
	}*/
	
	/**
	 * Get the OptionParser
	 * @return the OptionParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Match");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").setDescription("inputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("score-config").setDescription("scoreConfig JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(true));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output-config").setDescription("outputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE"));
		
		// Model name overrides
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("scoreOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of score jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		// Matching Algorithms 
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("match").withParameter(true, "THRESHOLD").setDescription("given the set threshhold return from the score set the items to link").setRequired(false));
		
		// Linking Methods
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("link").setDescription("link the two matched entities together using the pair of object properties vivoObj=scoreObj").withParameter(false, "RDF_PREDICATE"));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rename").setDescription("rename or remove the matched entity from scoring").setRequired(false));
		
		// options
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("clear-literals").setDescription("clear all literals out of the nodes matched").setRequired(false));
		return parser;
	}	
	
	/**
	 * Execute scoreJena object algorithms
	 * @throws IOException error connecting
	 */
	public void execute() throws IOException {
		log.info("Running specified algorithims");
		
		if(this.matchThreshold != null) {
			Map<String,String> pubmedResultMap = match(this.matchThreshold);
			
			if(this.renameRes) {
				rename(pubmedResultMap);
			} else if(this.linkProp != null && !this.linkProp.isEmpty()) {
				link(pubmedResultMap,this.linkProp);
			}
			
			if(this.clearLiterals) {
				clearLiterals(pubmedResultMap);
			}
		}
	}

	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(Match.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new Match(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
}
