/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 * Narayan Raum, Yang Li, Christopher Barnes, Chris Westling
 * - scoring algorithm ideas
 *****************************************************************************************************************************/
package org.vivoweb.harvester.score;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.score.algorithm.Algorithm;
import org.vivoweb.harvester.score.algorithm.EqualityTest;
import org.vivoweb.harvester.util.IterableAide;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.MemJenaConnect;
import org.vivoweb.harvester.util.jenaconnect.TDBJenaConnect;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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
	 * number of records to use in batch
	 */
	private int batchSize = 100;
	
	/**
	 * Constructor
	 * @param inputJena model containing statements to be scored
	 * @param vivoJena model containing vivoJena statements
	 * @param scoreJena model containing scoring data statements
	 * @param tempJenaDir model in which to store temp copy of input and vivo data statements
	 */
	public Score(JenaConnect inputJena, JenaConnect vivoJena, JenaConnect scoreJena, String tempJenaDir) {
		if(inputJena == null) {
			throw new IllegalArgumentException("Input model cannot be null");
		}
		this.inputJena = inputJena;
		
		if(vivoJena == null) {
			throw new IllegalArgumentException("Vivo model cannot be null");
		}
		this.vivoJena = vivoJena;
		
		if(scoreJena == null) {
			throw new IllegalArgumentException("Score Data model cannot be null");
		}
		this.scoreJena = scoreJena;
		
		String tempDir = tempJenaDir;
		if(tempDir == null) {
			log.trace("temp model directory is not specified, using system temp directory");
			//			tempDir = File.createTempFile("tempVivoInputCopyJena", "db").getAbsolutePath();
			//			log.debug("temp model is not specifiedhi , using memory jena model");
			this.tempJena = new MemJenaConnect();
		} else {
			this.tempJena = new TDBJenaConnect(tempDir);
		}
	}
	
	/**
	 * Set the processing batch size
	 * @param size the size to use
	 */
	public void setBatchSize(int size) {
		this.batchSize = size;
		if(this.batchSize <= 1) {
			log.warn("Batch Size of '"+size+"' invalid, must be greater than or equal to 1.  Using '1' as Batch Size.");
			this.batchSize = 1;
		}
	}
	
	/**
	 * Get the processing batch size
	 * @return the batch size
	 */
	public int getBatchSize() {
		return this.batchSize;
	}
	
	/**
	 * Build the vivo and input clones and get the dataset
	 * @return the dataset
	 * @throws IOException error connecting to the models
	 */
	private Dataset prepDataset() throws IOException {
		// Bring all models into a single Dataset
		JenaConnect vivoClone = this.tempJena.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#vivoClone");
		if(vivoClone.isEmpty()) {
			log.debug("Loading VIVO model into temp copy model");
			vivoClone.loadRdfFromJC(this.vivoJena);
//			log.debug("vivo clone contents:\n"+vivoClone.exportRdfToString());
		} else {
			log.debug("VIVO model already in temp copy model");
		}
		JenaConnect inputClone = this.tempJena.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#inputClone");
		if(inputClone.isEmpty()) {
			log.debug("Loading Input model into temp copy model");
			inputClone.loadRdfFromJC(this.inputJena);
//			log.debug("input clone contents:\n"+inputClone.exportRdfToString());
		} else {
			log.debug("Input model already in temp copy model");
		}
		Dataset ds = this.tempJena.getDataset();
		log.trace("testing Dataset");
		if(!this.tempJena.executeAskQuery("ASK { ?s ?p ?o }", true)) {
			log.trace("Empty Dataset");
		}
		return ds;
	}
	
	/**
	 * Get the result set
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @param equalityOnlyMode query only returns perfect matches
	 * @return the resultset
	 * @throws IOException error connecting to the models
	 */
	private ResultSet getResultSet(Set<FieldComparison> comparisons, String namespace, boolean equalityOnlyMode) throws IOException {
		Dataset ds = prepDataset();
		log.trace("Building Query");
		String sQuery = buildSelectQuery(comparisons, namespace, equalityOnlyMode);
		log.trace("Score Query:\n" + sQuery);
		// Execute query
		log.trace("Building Query Execution");
		Query query = QueryFactory.create(sQuery, Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, ds);
		log.debug("Executing Query");
		ResultSet rs = queryExec.execSelect();
		return rs;
	}
	
	/**
	 * Build the solution set
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @param equalityOnlyMode query only returns perfect matches
	 * @return the solution set
	 * @throws IOException error connecting to the models
	 */
	private Set<Map<String, String>> buildSolutionSet(Set<FieldComparison> comparisons, String namespace, boolean equalityOnlyMode) throws IOException {
		ResultSet rs = getResultSet(comparisons, namespace, equalityOnlyMode);
		Set<Map<String, String>> solSet = getNewSolSet();
		if(!rs.hasNext()) {
			log.info("No Results Found");
		} else {
			log.info("Building Record Set");
			Map<String, String> tempMap;
			for(QuerySolution solution : IterableAide.adapt(rs)) {
				String sinputuri = solution.getResource("sInput").getURI();
				String svivouri = solution.getResource("sVivo").getURI();
				log.trace("Potential Match: <" + sinputuri + "> to <" + svivouri + ">");
				tempMap = new HashMap<String, String>();
				tempMap.put("sInput", sinputuri);
				tempMap.put("sVivo", svivouri);
				for(FieldComparison comparison : comparisons) {
					String runName = comparison.getName();
					RDFNode os = solution.get("os_" + runName);
					RDFNode op = solution.get("op_" + runName);
					addRunName(tempMap, runName, os, op);
				}
				solSet.add(tempMap);
			}
		}
		return solSet;
	}
	
	/**
	 * Build the solution set for a filtered score
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @param matchThreshold the threshold for finding matches
	 * @return the solution set
	 * @throws IOException error connecting to the models
	 */
	private Set<Map<String, String>> buildFilterSolutionSet(Set<FieldComparison> comparisons, String namespace, float matchThreshold) throws IOException {
		Match matcher = new Match(this.scoreJena);
		matcher.match(matchThreshold);
		Set<Map<String, String>> matchSet = matcher.getResultSet();
		Set<Map<String, String>> solSet = getNewSolSet();
		if(matchSet.isEmpty()) {
			log.info("No Results Found");
		} else {
			log.info("Building Record Set");
			Map<String, String> tempMap;
			for(Map<String, String> entry : matchSet) {
				String sinputuri = entry.get("sInputURI");
				Resource sInput = this.inputJena.getJenaModel().getResource(sinputuri);
				if(StringUtils.isBlank(namespace) || sInput.getNameSpace().startsWith(namespace)) {
					String svivouri = entry.get("sVivoURI");
					Resource sVivo = this.vivoJena.getJenaModel().getResource(svivouri);
					log.trace("Potential Match: <" + sinputuri + "> to <" + svivouri + ">");
					tempMap = new HashMap<String, String>();
					tempMap.put("sInput", sinputuri);
					tempMap.put("sVivo", svivouri);
					for(FieldComparison comparison : comparisons) {
						String runName = comparison.getName();
						Property os_runName = comparison.getInputProperty();
						RDFNode os = sInput.getProperty(os_runName).getObject();
						Property op_runName = comparison.getReferenceProperty();
						RDFNode op = sVivo.getProperty(op_runName).getObject();
						addRunName(tempMap, runName, os, op);
					}
					solSet.add(tempMap);
				}
			}
		}
		return solSet;
	}
	
	/**
	 * Add the os and op nodes to tempMap for runName
	 * @param tempMap the map to add to
	 * @param runName the runName
	 * @param os the os
	 * @param op the op
	 */
	private static void addRunName(Map<String,String> tempMap, String runName, RDFNode os, RDFNode op) {
		if((os != null) && os.isResource()) {
			tempMap.put("URI_os_" + runName, os.asResource().getURI());
		} else if((os != null) && os.isLiteral()) {
			tempMap.put("LIT_os_" + runName, os.asLiteral().getValue().toString());
		}
		if((op != null) && op.isResource()) {
			tempMap.put("URI_op_" + runName, op.asResource().getURI());
		} else if((op != null) && op.isLiteral()) {
			tempMap.put("LIT_op_" + runName, op.asLiteral().getValue().toString());
		}
	}
	
	/**
	 * Get an empty solution set
	 * @return the new empty solution set
	 */
	private static Set<Map<String, String>> getNewSolSet() {
		return new TreeSet<Map<String, String>>(new Comparator<Map<String, String>>() {
			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				String o1key = o1.get("sInput") + o1.get("sVivo");
				String o2key = o2.get("sInput") + o2.get("sVivo");
				return o1key.compareTo(o2key);
			}
		});
	}
	
	/**
	 * Determine if the comparison set contains only instances of EqualityTest
	 * @param comparisons Set of FieldComparisons to evaluate
	 * @return true if all instances of EqualityTest, false otherwise
	 */
	private boolean isEqualityOnly(Set<FieldComparison> comparisons) {
		for(FieldComparison comparison : comparisons) {
			Class<? extends Algorithm> algClass = comparison.getAlgorithm();
			try {
				algClass.asSubclass(EqualityTest.class);
			} catch(ClassCastException e) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Execute score object algorithms
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @param matchThreshold the threshold for finding matches
	 * @throws IOException error connecting
	 */
	public void execute(Set<FieldComparison> comparisons, String namespace, float matchThreshold) throws IOException {
		boolean equalityOnlyMode = isEqualityOnly(comparisons);
		Set<Map<String, String>> solSet = buildFilterSolutionSet(comparisons, namespace, matchThreshold);
		evaluate(comparisons, solSet, equalityOnlyMode);
	}

	/**
	 * Execute score object algorithms
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @throws IOException error connecting
	 */
	public void execute(Set<FieldComparison> comparisons, String namespace) throws IOException {
		boolean equalityOnlyMode = isEqualityOnly(comparisons);
		Set<Map<String, String>> solSet = buildSolutionSet(comparisons, namespace, equalityOnlyMode);
		evaluate(comparisons, solSet, equalityOnlyMode);
	}
	
	/**
	 * Evaluate the given solution set for the set of comparisons
	 * @param comparisons Set of FieldComparisons to run
	 * @param matchSet the set of possible matches
	 * @param equalityOnlyMode query only returns perfect matches
	 * @throws IOException error connecting
	 */
	private void evaluate(Set<FieldComparison> comparisons, Set<Map<String, String>> matchSet, boolean equalityOnlyMode) throws IOException {
		if(!matchSet.isEmpty()) {
			log.info("Processing Results");
			int total = matchSet.size();
			int count = 0;
			int incrementer = 0;
			double recordBatchSize = Math.ceil(this.batchSize / (2.0+(comparisons.size()*7)));
			StringBuilder indScore;
			StringBuilder scoreSparql = new StringBuilder();
			for(Map<String, String> eval : matchSet) {
				count++;
				incrementer++;
				indScore = new StringBuilder();
				String sInputURI = eval.get("sInput");
				String sVivoURI = eval.get("sVivo");
				float percent = Math.round(10000f * count / total) / 100f;
				log.debug("(" + count + "/" + total + ": " + percent + "%): Evaluating <" + sInputURI + "> from inputJena as match for <" + sVivoURI + "> from vivoJena");
				// Build Score Record
				indScore.append("" +
					"  _:node" + incrementer + " scoreValue:VivoRes <" + sVivoURI + "> .\n" +
					"  _:node" + incrementer + " scoreValue:InputRes <" + sInputURI + "> .\n"
				);
				double sum_total = 0;
				for(FieldComparison comparison : comparisons) {
					String runName = comparison.getName();
					String osUri = eval.get("URI_os_" + runName);
					String osLit = eval.get("LIT_os_" + runName);
					String opUri = eval.get("URI_op_" + runName);
					String opLit = eval.get("LIT_op_" + runName);
					log.debug("os_" + runName + ": '" + ((osUri != null) ? osUri : osLit) + "'");
					log.debug("op_" + runName + ": '" + ((opUri != null) ? opUri : opLit) + "'");
					sum_total += appendScoreSparqlFragment(comparison, indScore, incrementer, opUri, opLit, osUri, osLit, equalityOnlyMode);
				}
				log.debug("sum_total: "+sum_total);
				log.trace("Scores for inputJena node <" + sInputURI + "> to vivoJena node <" + sVivoURI + ">:\n" + indScore.toString());
				scoreSparql.append(indScore);
				if(incrementer == recordBatchSize) {
					loadRdfToScoreData(scoreSparql.toString());
					incrementer = 0;
					scoreSparql = new StringBuilder();
				}
			}
			if(incrementer > 0) {
				loadRdfToScoreData(scoreSparql.toString());
			}
			log.info("Result Processing Complete");
		}
		this.scoreJena.sync();
	}
	
	/**
	 * Load a batch of scoring data to the score model
	 * @param scores the score rdf/xml fragments
	 * @throws IOException error connecting
	 */
	private void loadRdfToScoreData(String scores) throws IOException {
		String sparql = "" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX scoreValue: <http://vivoweb.org/harvester/scoreValue/> \n" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
			"INSERT DATA {\n" +
				scores +
			"}";
		// Push Score Data into score model
		log.trace("Loading Score Data into Score Model:\n" + sparql);
		this.scoreJena.executeUpdateQuery(sparql);
	}
	
	/**
	 * Builds the select query for equality only mode
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @return the equality only mode query
	 */
	private String buildEqualitySelectQuery(Set<FieldComparison> comparisons, String namespace) {
		//Build query to find all nodes matching on the given predicates
		StringBuilder sQuery = new StringBuilder("PREFIX scoring: <http://vivoweb.org/harvester/model/scoring#>\n" + "SELECT DISTINCT ?sVivo ?sInput");
		
		List<String> filters = new ArrayList<String>();
		List<String> vivoSelects = new ArrayList<String>();
		List<String> inputSelects = new ArrayList<String>();
		
		for(FieldComparison comparison : comparisons) {
			String runName = comparison.getName();
			Property vivoProperty = comparison.getReferenceProperty();
			Property inputProperty = comparison.getInputProperty();
			sQuery.append(" ?os_" + runName);
			sQuery.append(" ?op_" + runName);
			vivoSelects.add("?sVivo <" + vivoProperty + "> ?op_" + runName + " .");
			inputSelects.add("?sInput <" + inputProperty + "> ?os_" + runName + " .");
			filters.add("(str(?os_" + runName + ") = str(?op_" + runName + "))");
		}
		
		sQuery.append("\n" + "FROM NAMED <http://vivoweb.org/harvester/model/scoring#vivoClone>\n" + "FROM NAMED <http://vivoweb.org/harvester/model/scoring#inputClone>\n" + "WHERE {\n");
		sQuery.append("  GRAPH scoring:vivoClone {\n    ");
		sQuery.append(StringUtils.join(vivoSelects, "\n    "));
		sQuery.append("\n  } . \n  GRAPH scoring:inputClone {\n" + "    ");
		sQuery.append(StringUtils.join(inputSelects, "\n    "));
		sQuery.append("\n  } . \n  FILTER( (");
		sQuery.append(StringUtils.join(filters, " && "));
		sQuery.append(") && (str(?sVivo) != str(?sInput))");
		if(namespace != null) {
			sQuery.append(" && regex(str(?sInput), \"^" + namespace + "\")");
		}
		sQuery.append(" ) .\n");
		sQuery.append("}");
		return sQuery.toString();
	}
	
	/**
	 * Build the select query
	 * @param comparisons Set of FieldComparisons to run
	 * @param namespace namespace to filter on
	 * @param equalityOnlyMode query only returns perfect matches
	 * @return the query
	 */
	private String buildSelectQuery(Set<FieldComparison> comparisons, String namespace, boolean equalityOnlyMode) {
		if(equalityOnlyMode) {
			return buildEqualitySelectQuery(comparisons, namespace);
		}
		//Build query to find all nodes matching on the given predicates
		StringBuilder sQuery = new StringBuilder("PREFIX scoring: <http://vivoweb.org/harvester/model/scoring#>\n" + "SELECT DISTINCT ?sVivo ?sInput");
		
		StringBuilder vivoOptionals = new StringBuilder();
		StringBuilder inputOptionals = new StringBuilder();
		List<String> filters = new ArrayList<String>();
		List<String> vivoUnions = new ArrayList<String>();
		List<String> inputUnions = new ArrayList<String>();
		
		for(FieldComparison comparison : comparisons) {
			String runName = comparison.getName(); 
			Property vivoProperty = comparison.getReferenceProperty();
			Property inputProperty = comparison.getInputProperty();
			sQuery.append(" ?os_" + runName);
//			sQuery.append(" ?ov_" + runName);
			sQuery.append(" ?op_" + runName);
//			sQuery.append(" ?oi_" + runName);
			vivoUnions.add("{ ?sVivo <" + vivoProperty + "> ?ov_" + runName + " }");
			vivoOptionals.append("    OPTIONAL { ?sVivo <").append(vivoProperty).append("> ").append("?op_" + runName).append(" } . \n");
			inputUnions.add("{ ?sInput <" + inputProperty + "> ?oi_" + runName + " }");
			inputOptionals.append("    " + "OPTIONAL { " + "?sInput <").append(inputProperty).append("> ").append("?os_" + runName).append(" }" + " . \n");
			filters.add("(str(?os_" + runName + ") = str(?ov_" + runName + "))");
		}
		
		sQuery.append("\n" + "FROM NAMED <http://vivoweb.org/harvester/model/scoring#vivoClone>\n" + "FROM NAMED <http://vivoweb.org/harvester/model/scoring#inputClone>\n" + "WHERE {\n");
		sQuery.append("  GRAPH scoring:vivoClone {\n    ");
		sQuery.append(StringUtils.join(vivoUnions, " UNION \n    "));
		sQuery.append(" . \n");
		sQuery.append(vivoOptionals.toString());
		sQuery.append("  } . \n");
		sQuery.append("  GRAPH scoring:inputClone {\n" + "    ");
		sQuery.append(StringUtils.join(inputUnions, " UNION \n    "));
		sQuery.append(" . \n");
		sQuery.append(inputOptionals.toString());
		sQuery.append("  } . \n");
		sQuery.append("  FILTER( (");
		sQuery.append(StringUtils.join(filters, " || "));
		sQuery.append(") && (str(?sVivo) != str(?sInput))");
		if(namespace != null) {
			sQuery.append(" && regex(str(?sInput), \"^" + namespace + "\")");
		}
		sQuery.append(" ) .\n");
		sQuery.append("}");
		return sQuery.toString();
	}
	
	/**
	 * Append the sparql fragment for two rdf nodes to the given stringbuilder
	 * @param comparison current FieldComparisons
	 * @param sb the stringbuilder to append fragment to
	 * @param nodenum the node number
	 * @param opUri vivoJena node as a URI
	 * @param opLit vivoJena node as a Literal string
	 * @param osUri inputJena node as a URI
	 * @param osLit inputJena node as a Literal string
	 * @param equalityOnlyMode query only returns perfect matches
	 * @return the score
	 */
	private double appendScoreSparqlFragment(FieldComparison comparison, StringBuilder sb, int nodenum, String opUri, String opLit, String osUri, String osLit, boolean equalityOnlyMode) {
		float score = 0f;
		Class<? extends Algorithm> algorithm = comparison.getAlgorithm();
		Property refProperty = comparison.getReferenceProperty();
		Property inputProperty = comparison.getInputProperty();
		Float weight = Float.valueOf(comparison.getWeight());
		// if a resource and same uris
		if(equalityOnlyMode || ((osUri != null) && (opUri != null) && osUri.equals(opUri))) {
			score = 1 / 1f;
		} else if((osLit != null) && (opLit != null)) {
			try {
				score = algorithm.newInstance().calculate(osLit, opLit);
			} catch(IllegalAccessException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			} catch(InstantiationException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}
		double weightedscore = weight.doubleValue() * score;
		log.debug("score: " + score);
		log.debug("weighted_score: " + weightedscore);
		String runName = comparison.getName();
		String fragment = "" +
			"  _:node" + nodenum + " scoreValue:hasScoreValue _:nodeScoreValue" + runName + nodenum + " .\n" +
			"  _:nodeScoreValue" + runName + nodenum + " scoreValue:VivoProp <" + refProperty + "> .\n" +
			"  _:nodeScoreValue" + runName + nodenum + " scoreValue:InputProp <" + inputProperty + "> .\n" +
			"  _:nodeScoreValue" + runName + nodenum + " scoreValue:Algorithm \"" + algorithm.getName() + "\" .\n" +
			"  _:nodeScoreValue" + runName + nodenum + " scoreValue:Score \"" + score + "\"^^xsd:float .\n" +
			"  _:nodeScoreValue" + runName + nodenum + " scoreValue:Weight \"" + weight + "\"^^xsd:float .\n" +
			"  _:nodeScoreValue" + runName + nodenum + " scoreValue:WeightedScore \"" + weightedscore + "\"^^xsd:float .\n";
		sb.append(fragment);
		return weightedscore;
	}
}
