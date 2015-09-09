/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
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
import org.vivoweb.harvester.qualify.RenameResources;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
	 * Model where output goes
	 */
	private final JenaConnect outputJena;
	/**
	 * Link the resources found by match Algorithm
	 */
	private final Map<String, String> linkProps;
	/**
	 * Rename resources found by match Algorithm
	 */
	private final boolean renameRes;
	/**
	 * Match threshold
	 */
	private final float matchThreshold;
	/**
	 * Clear all literal values out of matched sets
	 */
	private final boolean clearLiterals;
	/**
	 * number of records to use in batch
	 */
	private int batchSize;
	
	/**
	 * Constructor
	 * @param inputJena model containing statements to be scored
	 * @param scoreJena the model that contains the score values
	 * @param outputJena the model to which matched structures are written
	 * @param threshold match things with a total score greater than or equal to this threshold
	 * @param renameRes should I just rename the args?
	 * @param linkProps bidirectional link
	 * @param clearLiterals clear all the literal values out of matches
	 * @param size the size of each batch
	 */
	public Match(JenaConnect inputJena, JenaConnect scoreJena, JenaConnect outputJena, boolean renameRes, float threshold, Map<String, String> linkProps, boolean clearLiterals, int size) {
		if(scoreJena == null) {
			throw new IllegalArgumentException("Score Model cannot be null");
		}
		this.scoreJena = scoreJena;
		
		if(inputJena == null) {
			throw new IllegalArgumentException("Match Input cannot be null");
		}
		this.inputJena = inputJena;
		
		this.outputJena = outputJena;
		
		this.matchThreshold = threshold;
		this.renameRes = renameRes;
		this.linkProps = linkProps;
		this.clearLiterals = clearLiterals;
		
		this.batchSize = size;
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 * @throws UsageException user requested usage message
	 */
	private Match(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Set the processing batch size
	 * @param size the size to use
	 */
	public void setBatchSize(int size) {
		this.batchSize = size;
	}
	
	/**
	 * Constructor
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	private Match(ArgList opts) throws IOException {
		this(
			JenaConnect.parseConfig(opts.get("i"), opts.getValueMap("I")), 
			JenaConnect.parseConfig(opts.get("s"), opts.getValueMap("S")), 
			JenaConnect.parseConfig(opts.get("o"), opts.getValueMap("O")), 
			Boolean.parseBoolean(opts.get("r")), 
			Float.parseFloat(opts.get("t")), 
			opts.getValueMap("l"), 
			Boolean.parseBoolean(opts.get("c")), 
			Integer.parseInt(opts.get("b"))
		);
	}
	
	/**
	 * Find all nodes in the given namepsace matching on the given predicates
	 * @param threshold the value to look for in the sparql query
	 * @param scoreJena the jena model containing score data
	 * @return mapping of the found matches
	 * @throws IOException error connecting
	 */
	protected static Set<Map<String, String>> match(float threshold, JenaConnect scoreJena) throws IOException {
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
		log.trace("Match Query:\n" + sQuery);
		log.trace("Query Execution Start");
		Iterable<QuerySolution> matchQuery = IterableAdaptor.adapt(scoreJena.executeSelectQuery(sQuery));
		log.trace("Query Execution Complete");
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
		
		return uriMatchEntrySet;
	}
	
	/**
	 * Rename the resource set as the key to the value matched
	 * @param matchSet a result set of scoreResources, vivoResources
	 */
	private void rename(Set<Map<String, String>> matchSet) {
		log.info("Beginning Rename of matches");
		int total = matchSet.size();
		int count = 0;
		for(Map<String,String> entry : matchSet) {
			String oldUri = entry.get("sInputURI");
			String newUri = entry.get("sVivoURI");
			count++;
			float percent = Math.round(10000f * count / total) / 100f;
			log.trace("(" + count + "/" + total + ": " + percent + "%): Renaming match <" + oldUri + "> to <" + newUri + ">");
			//get resource in input model and perform rename
			if(oldUri != newUri){
				Resource res = this.inputJena.getJenaModel().getResource(oldUri);
				RenameResources.renameResource(res, newUri);
			}
		}
		log.info("Rename of matches complete");
	}
	
	/**
	 * Link matched scoreResources to vivoResources using given linking predicates
	 * @param matchSet a mapping of matched scoreResources to vivoResources
	 * @param vivoToInput vivo to input property
	 * @param inputToVivo input to vivo property
	 */
	private void link(Set<Map<String, String>> matchSet, String vivoToInput, String inputToVivo) {
		Property vivoToInputProperty = ResourceFactory.createProperty(vivoToInput);
		Property inputToVivoProperty = ResourceFactory.createProperty(inputToVivo);
		
		log.trace("Beginning link method loop");
		int total = matchSet.size();
		int count = 0;
		for(Map<String, String> entry : matchSet) {
			// get resources and add linking triples
			String inputUri = entry.get("sInputURI");
			String vivoUri = entry.get("sVivoURI");
			Resource inputRes = this.inputJena.getJenaModel().getResource(inputUri);
			Resource vivoRes = this.scoreJena.getJenaModel().getResource(vivoUri);
			float percent = Math.round(10000f * count / total) / 100f;
			log.trace("(" + count + "/" + total + ": " + percent + "%): Linking match <" + inputUri + "> to <" + vivoUri + ">");
			log.trace("Adding input to vivo match link [ <" + inputUri + "> <" + inputToVivo + "> <" + vivoUri + "> ]");
			this.inputJena.getJenaModel().add(inputRes, inputToVivoProperty, vivoRes);
			log.trace("Adding vivo to input match link [ <" + vivoUri + "> <" + vivoToInput + "> <" + inputUri + "> ]");
			this.inputJena.getJenaModel().add(vivoRes, vivoToInputProperty, inputRes);
		}
	}
	
	/**
	 * Clear out rdf:type and literal values of matched scoreResources TODO stephen: TEST
	 * @param resultSet a mapping of matched scoreResources to vivoResources
	 * @throws IOException error connecting
	 */
	private void clearTypesAndLiterals(Set<Map<String, String>> resultSet) throws IOException {
		if(!resultSet.isEmpty()) {
			log.info("Beginning clear types and literals");
			Set<String> uriFilters = new HashSet<String>();
			int count = 0;
			int inc = 0;
			for(Map<String, String> entry : resultSet) {
				String uri = entry.get("sInputURI");
				if(inc == this.batchSize){
					buildTypesAndLiteralsQuery(uriFilters);
					count += inc;
					inc = 0;
					uriFilters.clear();
				}
				uriFilters.add("(str(?s) = \"" + uri + "\")");
				inc++;
			}
			buildTypesAndLiteralsQuery(uriFilters);
			count += inc;
			log.trace("Cleared " + count + " types and literals");
			log.info("Ending clear types and literals");
		}
	}
	
	/**
	 * Build the query for matching types and literals
	 * @param uriFilters uris to match on
	 * @throws IOException error connecting
	 */
	private void buildTypesAndLiteralsQuery(Set<String> uriFilters) throws IOException {
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
		log.trace("Query Execution Start");
		this.inputJena.executeUpdateQuery(query);
		log.trace("Query Execution Complete");
	}
	
	/**
	 * @param matchSet the set of matches to run against
	 * @return the completed model of matches
	 * @throws IOException no idea why it throws this
	 */
	private JenaConnect outputMatches(Set<Map<String, String>> matchSet) throws IOException {
		log.info("Beginning separate output of matches");
		Stack<String> linkRes = new Stack<String>();
		JenaConnect returnModel = new MemJenaConnect();
		int i = 0;
		for(Map<String, String> entry : matchSet) {
			String oldUri = entry.get("sInputURI");
			String newUri = entry.get("sVivoURI");
			i++;
			log.trace("Getting statements for matchSet " + oldUri);
			StmtIterator subjectStmts = this.inputJena.getJenaModel().listStatements(null, null, this.inputJena.getJenaModel().getResource(newUri));
			
			while(subjectStmts.hasNext()) {
				Statement stmt = subjectStmts.nextStatement();
				Resource subj = stmt.getSubject();
				if(!linkRes.contains(subj)) {
//					log.trace("Submitting to recursive build " + subj.getURI());
					linkRes.push(subj.getURI());
					returnModel.getJenaModel().add(recursiveBuild(subj, linkRes));
				}
			}
			
			returnModel.getJenaModel().add(this.inputJena.getJenaModel().listStatements(null, null, this.inputJena.getJenaModel().getResource(newUri)));
		}
		log.info("Outputted " + i + " matches");
		return returnModel;
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
	
	/**
	 * Get the OptionParser
	 * @return the OptionParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Match");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").withParameter(true, "CONFIG_FILE").setDescription("inputConfig JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("score-config").withParameter(true, "CONFIG_FILE").setDescription("scoreConfig JENA configuration filename").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output-config").setDescription("outputConfig JENA configuration filename, when set nodes that meet the threshold are pushed to the output model").withParameter(true, "CONFIG_FILE").setRequired(false));
		
		// Model name overrides
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("scoreOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of score jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		// Matching Algorithms
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("threshold").withParameter(true, "THRESHOLD").setDescription("match records with a score over THRESHOLD").setRequired(true));
		
		// Linking Methods
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("link").withParameterValueMap("VIVO_TO_INPUT_PREDICATE", "INPUT_TO_VIVO_PREDICATE").setDescription("link the two matched entities together using INPUT_TO_VIVO_PREDICATE and INPUT_TO_VIVO_PREDICATE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rename").setDescription("rename or remove the matched entity from scoring").setRequired(false));
		
		// options
		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("clear-type-and-literals").setDescription("clear all rdf:type and literal values out of the nodes matched").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('b').setLongOpt("batch-size").withParameter(true, "BATCH_SIZE").setDescription("number of records to process in batch - default 150 - lower this if getting StackOverflow or OutOfMemory").setDefaultValue("150").setRequired(false));
		return parser;
	}
	
	/**
	 * Execute scoreJena object algorithms
	 * @throws IOException error connecting
	 */
	public void execute() throws IOException {
		log.info("Finding matches");
		
		Set<Map<String, String>> resultSet = match(this.matchThreshold, this.scoreJena);
		for(Map<String, String> entry : resultSet) {
			String sInputURI = entry.get("sInputURI");
			log.trace("input: " + sInputURI);
			String sVivoURI = entry.get("sVivoURI");
			log.trace("vivo: " + sVivoURI);
			String score = entry.get("score");
			log.trace("score: " + score);
			log.debug("Match found: <" + sInputURI + "> in Input matched with <" + sVivoURI + "> in Vivo");
		}
		log.info("Found " + resultSet.size() + " links between Vivo and the Input model");
		
		if(this.clearLiterals) {
			clearTypesAndLiterals(resultSet);
		}
		
		if(this.renameRes) {
			rename(resultSet);
		}
		
		if(this.linkProps != null) {
			for(String vivoToInput : this.linkProps.keySet()) {
				link(resultSet, vivoToInput, this.linkProps.get(vivoToInput));
			}
		}
		
		if(this.outputJena != null) {
			this.outputJena.getJenaModel().add(outputMatches(resultSet).getJenaModel());
			this.outputJena.sync();
		}
		this.inputJena.sync();
	}
	
	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new Match(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
}
