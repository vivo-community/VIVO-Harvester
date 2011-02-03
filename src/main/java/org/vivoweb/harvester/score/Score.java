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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.score.algorithm.Algorithm;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.TDBJenaConnect;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * VIVO Score
 * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
 * @author Stephen Williams svwilliams@ctrip.ufl.edu
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 * @thanks Chris Westling cmw48@cornell.edu
 */
public class Score {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Score.class);
	/**
	 * model containing statements to be scored
	 */
	private JenaConnect inputJena;
	/**
	 * model containing vivoJena statements
	 */
	private JenaConnect vivoJena;
	/**
	 * model containing scoring data statements
	 */
	private JenaConnect scoreJena;
	/**
	 * model in which to store temp copy of input and vivo data statements
	 */
	private JenaConnect tempJena;
	/**
	 * the class of the Algorithm to execute
	 */
	private Map<String,Class<? extends Algorithm>> algorithms;
	/**
	 * the predicates to look for in inputJena model
	 */
	private Map<String,String> inputPredicates;
	/**
	 * the predicates to look for in vivoJena model
	 */
	private Map<String,String> vivoPredicates;
	/**
	 * limit match Algorithm to only match rdf nodes in inputJena whose URI begin with this namespace
	 */
	private String namespace;
	/**
	 * the weighting (0.0 , 1.0) for this score
	 */
	private Map<String,Float> weights;

	/**
	 * Constructor
	 * @param inputJena model containing statements to be scored
	 * @param vivoJena model containing vivoJena statements
	 * @param scoreJena model containing scoring data statements
	 * @param tempJenaDir model in which to store temp copy of input and vivo data statements
	 * @param algorithms the classes of the algorithms to execute
	 * @param inputPredicates the predicates to look for in inputJena model
	 * @param vivoPredicates the predicates to look for in vivoJena model
	 * @param namespace limit match Algorithm to only match rdf nodes in inputJena whose URI begin with this namespace
	 * @param weights the weightings (0.0 , 1.0) for this score
	 * @throws IOException error initializing jena models
	 */
	public Score(JenaConnect inputJena, JenaConnect vivoJena, JenaConnect scoreJena, String tempJenaDir, Map<String,Class<? extends Algorithm>> algorithms, Map<String,String> inputPredicates, Map<String,String> vivoPredicates, String namespace, Map<String,Float> weights) throws IOException {
		init(inputJena, vivoJena, scoreJena, tempJenaDir, algorithms, inputPredicates, vivoPredicates, namespace, weights);
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 */
	public Score(String... args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
			Scoring.close();
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	public Score(ArgList opts) throws IOException {
		JenaConnect i = JenaConnect.parseConfig(opts.get("i"), opts.getValueMap("I"));
		JenaConnect v = JenaConnect.parseConfig(opts.get("v"), opts.getValueMap("V"));
		JenaConnect s = JenaConnect.parseConfig(opts.get("s"), opts.getValueMap("S"));
		String t = opts.get("t");
		Map<String,Class<? extends Algorithm>> a = new HashMap<String, Class<? extends Algorithm>>();
		Map<String, String> algs = opts.getValueMap("A");
		for(String runName : algs.keySet()) {
			try {
				a.put(runName, Class.forName(algs.get(runName)).asSubclass(Algorithm.class));
			} catch(ClassNotFoundException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			} catch(ClassCastException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}
		Map<String,Float> w = new HashMap<String, Float>();
		Map<String,String> weigh = opts.getValueMap("W");
		for(String runName : weigh.keySet()) {
			w.put(runName, Float.valueOf(weigh.get(runName)));
		}
		init(i, v, s, t, a, opts.getValueMap("P"), opts.getValueMap("F"), opts.get("n"), w);
	}
	
	/**
	 * Initialize variables
	 * @param i model containing statements to be scored
	 * @param v model containing vivoJena statements
	 * @param s model containing scoring data statements
	 * @param t directory to put model in which to store temp copy of input and vivo data statements
	 * @param a the class of the Algorithm to execute
	 * @param iPred the predicate to look for in inputJena model
	 * @param vPred the predicate to look for in vivoJena model
	 * @param ns limit match Algorithm to only match rdf nodes in inputJena whose URI begin with this namespace
	 * @param w the weighting (0.0 , 1.0) for this score
	 * @throws IOException error initializing jena models
	 */
	private void init(JenaConnect i, JenaConnect v, JenaConnect s, String t, Map<String,Class<? extends Algorithm>> a, Map<String,String> iPred, Map<String,String> vPred, String ns, Map<String,Float> w) throws IOException {
		if(i == null) {
			throw new IllegalArgumentException("Input model cannot be null");
		}
		this.inputJena = i;
		
		if(v == null) {
			throw new IllegalArgumentException("Vivo model cannot be null");
		}
		this.vivoJena = v;
		
		if(s == null) {
			throw new IllegalArgumentException("Score Data model cannot be null");
		}
		this.scoreJena = s;
		
		String tempDir = t;
		if(tempDir == null) {
			log.info("temp model directory is not specified, using system temp directory");
//			tempDir = File.createTempFile("tempVivoInputCopyJena", "db").getAbsolutePath();
//			log.info("temp model is not specifiedhi , using memory jena model");
			this.tempJena = new MemJenaConnect();
		} else {
			this.tempJena = new TDBJenaConnect(tempDir);
		}
		
		
		if(a == null) {
			throw new IllegalArgumentException("Algorithm cannot be null");
		}
		this.algorithms = a;
		
		if(iPred == null) {
			throw new IllegalArgumentException("Input Predicate cannot be null");
		}
		this.inputPredicates = iPred;
		
		if(vPred == null) {
			throw new IllegalArgumentException("Vivo Predicate cannot be null");
		}
		this.vivoPredicates = vPred;
		
		if (this.algorithms.size() < 1) {
			throw new IllegalArgumentException("No runs specified!");
		}
		
		this.namespace = ns;
		
		for(Float weight : w.values()) {
			float d = weight.floatValue();
			if(d > 1f) {
				throw new IllegalArgumentException("Weights cannot be greater than 1.0");
			}
			if(d < 0f) {
				throw new IllegalArgumentException("Weights cannot be less than 0.0");
			}
		}
		this.weights = w;
		
		Map<String,Map<String,? extends Object>> maps = new HashMap<String, Map<String,? extends Object>>();
		maps.put("vivoJena predicates", this.vivoPredicates);
		maps.put("inputJena predicates", this.inputPredicates);
		maps.put("algorithms", this.algorithms);
		maps.put("weights", this.weights);
		verifyRunNames(maps);
	}
	
	/**
	 * Verify that each map contains the same keys
	 * @param maps mapping of map name to map
	 */
	private void verifyRunNames(Map<String,Map<String,? extends Object>> maps) {
		for(String x : maps.keySet()) {
			for(String y : maps.keySet()) {
				if(x != y && !maps.get(y).keySet().containsAll(maps.get(x).keySet())) {
					for(String runName: maps.get(x).keySet()) {
						if(x != y && !maps.get(y).containsKey(runName)) {
							throw new IllegalArgumentException("run name '"+runName+"' found in "+x+", but not in "+y);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Get the ArgParser
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Score");
		// Models
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJena JENA configuration filename").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of inputJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoJena-config").withParameter(true, "CONFIG_FILE").setDescription("vivoJena JENA configuration filename").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivoJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("score-config").withParameter(true, "CONFIG_FILE").setDescription("score data JENA configuration filename").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("scoreOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of score jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tempJenaDir").withParameter(true, "DIRECTORY_PATH").setDescription("directory to store temp jena model").setRequired(false));
//		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("temp-config").setDescription("temp model JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(false));
//		parser.addArgument(new ArgDef().setShortOption('T').setLongOpt("tempOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of temp jena model config using VALUE").setRequired(false));
		
		// Parameters
		parser.addArgument(new ArgDef().setShortOption('A').setLongOpt("algorithms").withParameterValueMap("RUN_NAME", "CLASS_NAME").setDescription("for RUN_NAME, use this CLASS_NAME (must implement Algorithm) to evaluate matches").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('W').setLongOpt("weights").withParameterValueMap("RUN_NAME", "WEIGHT").setDescription("for RUN_NAME, assign this weight (0,1) to the scores").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('F').setLongOpt("inputJena-predicates").withParameterValueMap("RUN_NAME", "PREDICATE").setDescription("for RUN_NAME, match ").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("vivoJena-predicates").withParameterValueMap("RUN_NAME", "PREDICAATE").setDescription("for RUN_NAME, assign this weight (0,1) to the scores").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "SCORE_NAMESPACE").setDescription("limit match Algorithm to only match rdf nodes in inputJena whose URI begin with SCORE_NAMESPACE").setRequired(false));
		return parser;
	}	
	
	/**
	 * Execute score object algorithms
	 * @throws IOException error connecting
	 */
	public void execute() throws IOException {
		// Bring all models into a single Dataset
		JenaConnect vivoClone = this.tempJena.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#vivoClone");
		if(vivoClone.isEmpty()) {
			log.trace("Loading VIVO model into temp copy model");
			vivoClone.loadRdfFromJC(this.vivoJena);
		} else {
			log.trace("VIVO model already in temp copy model");
		}
		JenaConnect inputClone = this.tempJena.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#inputClone");
		if(inputClone.isEmpty()) {
			log.trace("Loading Input model into temp copy model");
			inputClone.loadRdfFromJC(this.inputJena);
		} else {
			log.trace("Input model already in temp copy model");
		}
		Dataset ds = this.tempJena.getDataSet();

		log.trace("Building Query");
		String sQuery = buildSelectQuery();
		log.debug("Match Query:\n"+sQuery);
		// Execute query
		log.trace("Building Query Execution");
		Query query = QueryFactory.create(sQuery, Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, ds);
		log.trace("Executing Query");
		ResultSet rs = queryExec.execSelect();
		int incrementer = 0;
		StringBuilder indScore;
		StringBuilder scoreSparql = new StringBuilder();
		if(!rs.hasNext()) {
			log.info("No Results Found");
		} else {
			log.info("Processing Results");
		}
		for(QuerySolution solution : IterableAdaptor.adapt(rs)) {
			incrementer++;
			indScore = new StringBuilder();
			String sInputURI = solution.getResource("sInput").getURI();
			String sVivoURI = solution.getResource("sVivo").getURI();
			log.debug("Evaluating <"+sInputURI+"> from inputJena as match for <"+sVivoURI+"> from vivoJena");
			// Build Score Record
			indScore.append("" +
				"  _:node" + incrementer + " scoreValue:VivoRes <" + sVivoURI + "> .\n" +
				"  _:node" + incrementer + " scoreValue:InputRes <" + sInputURI + "> .\n"
			);
			for(String runName : this.vivoPredicates.keySet()) {
				RDFNode os = solution.get("os_"+runName);
				RDFNode op = solution.get("op_"+runName);
				log.trace("os_"+runName+": '"+os+"'");
				log.trace("op_"+runName+": '"+op+"'");
				indScore.append(buildScoreSparqlFragment(incrementer, op, os, runName));
			}
			log.debug("Scores for inputJena node <"+sInputURI+"> to vivoJena node <"+sVivoURI+">:\n"+indScore.toString());
			scoreSparql.append(indScore);
			if(incrementer == 50) {
				loadRdfToScoreData(scoreSparql.toString());
				incrementer = 0;
				scoreSparql = new StringBuilder();
			}
		}
		if(incrementer > 0) {
			loadRdfToScoreData(scoreSparql.toString());
		}
		log.trace("Result Processing Complete");
	}
	
	/**
	 * Load a batch of scoring data to the score model
	 * @param scores the score rdf/xml fragments
	 */
	private void loadRdfToScoreData(String scores) {
		String sparql = "" +
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
		"PREFIX scoreValue: <http://vivoweb.org/harvester/scoreValue/> \n" +
		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
		"INSERT DATA {\n" +
			scores +
		"}\n";
		// Push Score Data into score model
		log.trace("Loading Score Data into Score Model:\n"+sparql);
		this.scoreJena.executeUpdateQuery(sparql);
	}
	
	/**
	 * Build the select query
	 * @return the query
	 */
	private String buildSelectQuery() {
		//Build query to find all nodes matching on the given predicates
		StringBuilder sQuery =	new StringBuilder(
				"PREFIX scoring: <http://vivoweb.org/harvester/model/scoring#>\n" +
				"SELECT DISTINCT ?sVivo ?sInput"
		);
		
		StringBuilder vivoOptionals = new StringBuilder();
		StringBuilder inputOptionals = new StringBuilder();
		List<String> filters = new ArrayList<String>();
		List<String> vivoUnions = new ArrayList<String>();
		List<String> inputUnions = new ArrayList<String>();
		
		for (String runName : this.inputPredicates.keySet()) {
			String vivoProperty = this.vivoPredicates.get(runName);
			String inputProperty = this.inputPredicates.get(runName);
			sQuery.append(" ?os_" + runName);
//			sQuery.append(" ?ov_" + runName);
			sQuery.append(" ?op_" + runName);
//			sQuery.append(" ?oi_" + runName);
			vivoUnions.add("{ ?sVivo <" + vivoProperty + "> ?ov_" + runName + " }");
			vivoOptionals.append("    OPTIONAL { ?sVivo <").append(vivoProperty).append("> ").append("?op_" + runName).append(" } . \n");
			inputUnions.add("{ ?sInput <" + inputProperty + "> ?oi_" + runName + " }");
			inputOptionals.append("    "+"OPTIONAL { "+"?sInput <").append(inputProperty).append("> ").append("?os_" + runName).append(" }"+" . \n");
			filters.add("sameTerm(?os_" + runName + ", ?ov_" + runName + ")");
		}
		
		sQuery.append("\n" +
				"FROM NAMED <http://vivoweb.org/harvester/model/scoring#vivoClone>\n" +
				"FROM NAMED <http://vivoweb.org/harvester/model/scoring#inputClone>\n" +
				"WHERE {\n");
		sQuery.append("  GRAPH scoring:vivoClone {\n    ");
		sQuery.append(StringUtils.join(vivoUnions, " UNION \n    "));
		sQuery.append(" . \n");
		sQuery.append(vivoOptionals.toString());
		sQuery.append("  } . \n");
		sQuery.append("  GRAPH scoring:inputClone {\n"+"    ");
		sQuery.append(StringUtils.join(inputUnions, " UNION \n    "));
		sQuery.append(" . \n");
		sQuery.append(inputOptionals.toString());
		sQuery.append("  } . \n");
		sQuery.append("  FILTER( (");
		sQuery.append(StringUtils.join(filters, " || "));
		sQuery.append(") && (str(?sVivo) != str(?sInput))");
		if(this.namespace != null) {
			sQuery.append(" && regex(str(?sInput), \"^"+this.namespace+"\")");
		}
		sQuery.append(" ) .\n");
		sQuery.append("}");
		return sQuery.toString();
	}
	
	/**
	 * Build the sparql fragment for two rdf nodes
	 * @param nodenum the node number
	 * @param op vivoJena node
	 * @param os inputJena node
	 * @param runName the run identifier
	 * @return the sparql fragment
	 */
	private String buildScoreSparqlFragment(int nodenum, RDFNode op, RDFNode os, String runName) {
		float score = 0f;
		if(os != null && op != null) {
			// if a resource and same uris
			if(os.isResource() && op.isResource() && os.asResource().getURI().equals(op.asResource().getURI())) {
				score = 1/1;
			} else if(os.isLiteral() && op.isLiteral()) {
				String osStrValue = os.asLiteral().getValue().toString();
				String opStrValue = op.asLiteral().getValue().toString();
				try {
					score = this.algorithms.get(runName).newInstance().calculate(osStrValue, opStrValue);
				} catch(IllegalAccessException e) {
					throw new IllegalArgumentException(e.getMessage(), e);
				} catch(InstantiationException e) {
					throw new IllegalArgumentException(e.getMessage(), e);
				}
			}
		}
		log.trace("score: "+score);
		String fragment = "" +
			"  _:node" + nodenum + " scoreValue:hasScoreValue _:nodeScoreValue" + nodenum + " .\n" +
			"  _:nodeScoreValue" + nodenum + " scoreValue:VivoProp <" + this.vivoPredicates.get(runName) + "> .\n" +
			"  _:nodeScoreValue" + nodenum + " scoreValue:InputProp <" + this.inputPredicates.get(runName) + "> .\n" +
			"  _:nodeScoreValue" + nodenum + " scoreValue:Algorithm \"" + this.algorithms.get(runName).getName() + "\" .\n" +
			"  _:nodeScoreValue" + nodenum + " scoreValue:Score \"" + score + "\"^^xsd:float .\n" +
			"  _:nodeScoreValue" + nodenum + " scoreValue:Weight \"" + this.weights.get(runName) + "\"^^xsd:float .\n" +
			"  _:nodeScoreValue" + nodenum + " scoreValue:WeightedScore \"" + (this.weights.get(runName).doubleValue()*score) + "\"^^xsd:float .\n" +
			"";
		return fragment;
	}

	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(Score.class);
		log.info(getParser().getAppName()+": Start");
		try {
			new Score(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
}
