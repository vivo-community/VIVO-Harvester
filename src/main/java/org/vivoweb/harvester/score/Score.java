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
import org.vivoweb.harvester.score.algorithm.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
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
	private JenaConnect input;
	/**
	 * model containing vivo statements
	 */
	private JenaConnect vivo;
	/**
	 * model containing scoring data statements
	 */
	private JenaConnect scoreData;
	/**
	 * the class of the Algorithm to execute
	 */
	private Map<String,Class<? extends Algorithm>> algorithms;
	/**
	 * the predicates to look for in input model
	 */
	private Map<String,String> inputPredicates;
	/**
	 * the predicates to look for in vivo model
	 */
	private Map<String,String> vivoPredicates;
	/**
	 * limit match Algorithm to only match rdf nodes in input whose URI begin with this namespace
	 */
	private String namespace;
	/**
	 * the weighting (0.0 , 1.0) for this score
	 */
	private Map<String,Double> weights;

	/**
	 * Constructor
	 * @param input model containing statements to be scored
	 * @param vivo model containing vivo statements
	 * @param scoreData model containing scoring data statements
	 * @param algorithms the classes of the algorithms to execute
	 * @param inputPredicates the predicates to look for in input model
	 * @param vivoPredicates the predicates to look for in vivo model
	 * @param namespace limit match Algorithm to only match rdf nodes in input whose URI begin with this namespace
	 * @param weights the weightings (0.0 , 1.0) for this score
	 */
	public Score(JenaConnect input, JenaConnect vivo, JenaConnect scoreData, Map<String,Class<? extends Algorithm>> algorithms, Map<String,String> inputPredicates, Map<String,String> vivoPredicates, String namespace, Map<String,Double> weights) {
		init(input, vivo, scoreData, algorithms, inputPredicates, vivoPredicates, namespace, weights);
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
		Map<String,Double> w = new HashMap<String, Double>();
		Map<String,String> weigh = opts.getValueMap("W");
		for(String runName : weigh.keySet()) {
			w.put(runName, Double.valueOf(weigh.get(runName)));
		}
		init(i, v, s, a, opts.getValueMap("P"), opts.getValueMap("F"), opts.get("n"), w);
	}
	
	/**
	 * Initialize variables
	 * @param i model containing statements to be scored
	 * @param v model containing vivo statements
	 * @param s model containing scoring data statements
	 * @param a the class of the Algorithm to execute
	 * @param iPred the predicate to look for in input model
	 * @param vPred the predicate to look for in vivo model
	 * @param ns limit match Algorithm to only match rdf nodes in input whose URI begin with this namespace
	 * @param w the weighting (0.0 , 1.0) for this score
	 */
	private void init(JenaConnect i, JenaConnect v, JenaConnect s, Map<String,Class<? extends Algorithm>> a, Map<String,String> iPred, Map<String,String> vPred, String ns, Map<String,Double> w) {
		if(i == null) {
			throw new IllegalArgumentException("Input model cannot be null");
		}
		this.input = i;
		
		if(v == null) {
			throw new IllegalArgumentException("Vivo model cannot be null");
		}
		this.vivo = v;
		
		if(s == null) {
			throw new IllegalArgumentException("Score Data model cannot be null");
		}
		this.scoreData = s;
		
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
		
		for(Double weight : w.values()) {
			double d = weight.doubleValue();
			if(d > 1.0) {
				throw new IllegalArgumentException("Weights cannot be greater than 1.0");
			}
			if(d < 0.0) {
				throw new IllegalArgumentException("Weights cannot be less than 0.0");
			}
		}
		this.weights = w;
		
		Map<String,Map<String,? extends Object>> maps = new HashMap<String, Map<String,? extends Object>>();
		maps.put("vivo predicates", this.vivoPredicates);
		maps.put("input predicates", this.inputPredicates);
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
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").setDescription("input JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivo-config").setDescription("vivo JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivo jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("score-config").setDescription("score data JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("scoreOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of score jena model config using VALUE").setRequired(false));
		
		// Parameters
		parser.addArgument(new ArgDef().setShortOption('A').setLongOpt("algorithms").withParameterValueMap("RUN_NAME", "CLASS_NAME").setDescription("for RUN_NAME, use this CLASS_NAME (must implement Algorithm) to evaluate matches").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('W').setLongOpt("weights").withParameterValueMap("RUN_NAME", "WEIGHT").setDescription("for RUN_NAME, assign this weight (0,1) to the scores").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('F').setLongOpt("input-predicates").withParameterValueMap("RUN_NAME", "PREDICATE").setDescription("for RUN_NAME, match ").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("vivo-predicates").withParameterValueMap("RUN_NAME", "PREDICAATE").setDescription("for RUN_NAME, assign this weight (0,1) to the scores").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "SCORE_NAMESPACE").setDescription("limit match Algorithm to only match rdf nodes in input whose URI begin with SCORE_NAMESPACE").setRequired(false));
		return parser;
	}	
	
	/**
	 * Execute score object algorithms
	 * @throws IOException error connecting
	 */
	public void execute() throws IOException {
		log.info("Running specified Algorithims");
//		Class<? extends Algorithm> m;
//		m.getMethod("execute", String.class, String.class).invoke(null, "", "");
		
		match();
	}
	
	/**
	 * Find all nodes in the given namepsace matching on the given predicates
	 * @throws IOException error connecting to dataset
	 */
	private void match() throws IOException {
		//Build query to find all nodes matching on the given predicates
		StringBuilder sQuery =	new StringBuilder(
				"PREFIX scoring: <http://vivoweb.org/harvester/model/scoring#>\n" +
				"SELECT DISTINCT ?sVivo ?sInput"
		);
		
//		int counter = 0;
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
//			counter++;
		}
		
		/*
		
		SELECT DISTINCT ?sVivo ?sInput ?op1 ?op2 ?op3
		FROM NAMED <http://vivoweb.org/harvester/model/scoring#vivoClone>
		FROM NAMED <http://vivoweb.org/harvester/model/scoring#inputClone>
		WHERE {
		  GRAPH scoring:vivoClone {
			{ ?sVivo <http://vivoweb.org/vivoprop1> ?ov1 } UNION 
			{ ?sVivo <http://vivoweb.org/vivoprop2> ?ov2 } UNION 
			{ ?sVivo <http://vivoweb.org/vivoprop3> ?ov3 } . 
			OPTIONAL { ?sVivo <http://vivoweb.org/vivoprop1> ?op1 } . 
			OPTIONAL { ?sVivo <http://vivoweb.org/vivoprop2> ?op2 } . 
			OPTIONAL { ?sVivo <http://vivoweb.org/vivoprop3> ?op3 } . 
		  } . 
		  GRAPH scoring:inputClone {
			{ ?sInput <http://vivoweb.org/scoreprop1> ?oi1 } UNION 
			{ ?sInput <http://vivoweb.org/scoreprop2> ?oi2 } UNION 
			{ ?sInput <http://vivoweb.org/scoreprop3> ?oi3 } . 
			OPTIONAL { ?sInput <http://vivoweb.org/scoreprop1> ?os1 } . 
			OPTIONAL { ?sInput <http://vivoweb.org/scoreprop2> ?os2 } . 
			OPTIONAL { ?sInput <http://vivoweb.org/scoreprop3> ?os3 } . 
		  } . 
		  FILTER(sameTerm(?oi1, ?ov1) && sameTerm(?oi2, ?ov2) && sameTerm(?oi3, ?ov3) && (str(?sVivo) != str(?sInput)) && regex(str(?sInput), "http://www.youruni.edu/") ) .
		}
		
		*/
		
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
		
		log.debug("Match Query:\n"+sQuery.toString());
		
		JenaConnect unionModel = new MemJenaConnect();
		//TODO anyone: determine if this should be an h2 in temp file rather than h2 in memory
		JenaConnect vivoClone = unionModel.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#vivoClone");
		vivoClone.loadRdfFromJC(this.vivo);
		JenaConnect inputClone = unionModel.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#inputClone");
		inputClone.loadRdfFromJC(this.input);
		Dataset ds = unionModel.getConnectionDataSet();
		
		Query query = QueryFactory.create(sQuery.toString(), Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, ds);
		for(QuerySolution solution : IterableAdaptor.adapt(queryExec.execSelect())) {
			String sInputURI = solution.getResource("sInput").getURI();
			String sVivoURI = solution.getResource("sVivo").getURI();
			StringBuilder rdf = new StringBuilder();
			rdf.append("" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<rdf:RDF \n" +
				"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n" +
				"    xmlns:scoreValue=\"http://vivoweb.org/harvestery/scoreValue\">\n" +
				"  <rdf:Description>\n" +
				"    <scoreValue:VivoRes rdf:resource=\""+sVivoURI+"\"/>\n" +
				"    <scoreValue:InputRes rdf:resource=\""+sInputURI+"\"/>\n"
			);
			log.debug("Evaluating <"+sInputURI+"> from input as match for <"+sVivoURI+"> from vivo");
			for(String runName : this.vivoPredicates.keySet()) {
				
				RDFNode os = solution.get("os_"+runName);
				RDFNode op = solution.get("op_"+runName);
				double score = 0/1;
				if(os != null && op != null) {
					// if a resource and same uris
					if(os.isResource() && op.isResource() && os.asResource().getURI().equals(op.asResource().getURI())) {
						score = 1/1;
					} else if(os.isLiteral() && op.isLiteral()) {
						String osStrValue = os.asLiteral().getValue().toString();
						String opStrValue = op.asLiteral().getValue().toString();
						Algorithm alg;
						try {
							alg = this.algorithms.get(runName).newInstance();
						} catch(IllegalAccessException e) {
							throw new IllegalArgumentException(e.getMessage(), e);
						} catch(InstantiationException e) {
							throw new IllegalArgumentException(e.getMessage(), e);
						}
						score = alg.calculate(osStrValue, opStrValue);
					}
				}
				log.trace("os_"+runName+": '"+os+"'");
				log.trace("op_"+runName+": "+op+"'");
				log.trace("score: "+score);
				double weight = this.weights.get(runName).doubleValue();
				rdf.append("" +
					"    <scoreValue:hasScoreValue>\n" +
					"      <rdf:Description>\n" +
					"        <scoreValue:VivoProp rdf:resource=\""+this.vivoPredicates.get(runName)+"\"/>\n" +
					"        <scoreValue:InputProp rdf:resource=\""+this.inputPredicates.get(runName)+"\"/>\n" +
					"        <scoreValue:Algorithm>" + this.algorithms.get(runName).getName() + "</scoreValue:Algorithm>\n" +
					"        <scoreValue:Score>" + score + "</scoreValue:Score>\n" +
					"        <scoreValue:Weight>" + weight + "</scoreValue:Weight>\n" +
					"        <scoreValue:WeightedScore>" + (weight*score) + "</scoreValue:WeightedScore>\n" +
					"      </rdf:Description>\n" +
					"    </scoreValue:hasScoreValue>\n"
				);
			}
			rdf.append("" +
				"  </rdf:Description>\n" +
				"</rdf:RDF>\n"
			);
//			log.debug("Scores for input node <"+sInputURI+"> to vivo node <"+sVivoURI+">:\n"+rdf.toString());
			this.scoreData.loadRdfFromString(rdf.toString(), null, null);
		}
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
