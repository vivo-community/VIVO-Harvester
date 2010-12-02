/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation Christopher
 * Barnes, Narayan Raum - scoring ideas and algorithim Yang Li - pairwise scoring algorithm Christopher Barnes - regex
 * scoring algorithim
 ******************************************************************************/
package org.vivoweb.harvester.score;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;
import com.hp.hpl.jena.util.ResourceUtils;

/***
 * VIVO Score
 * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
 */
public class Score {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Score.class);
	/**
	 * Model for VIVO instance
	 */
	private final JenaConnect vivo;
	/**
	 * Model where input is stored
	 */
	private final JenaConnect scoreInput;
	/**
	 * Model where output is stored
	 */
	private final JenaConnect scoreOutput;
	/**
	 * Option to remove input model after scoring
	 */
	private final boolean wipeInputModel;
	/**
	 * Option to remove output model before filling
	 */
	private boolean wipeOutputModel;
	/**
	 * Option to push Matches and Non-Matches to output model
	 */
	private final boolean pushAll;
	/**
	 * Arguments for exact match algorithm
	 */
	private final List<String> matchList;
	
	/**
	 * Execute score object algorithms
	 */
	public void execute() {
		log.info("Running specified algorithims");
		
		// Empty input model
		if (this.wipeOutputModel) {
			log.info("Emptying output model");	
			this.scoreOutput.getJenaModel().removeAll();
		}
		
		HashMap<String,String> resultMap;
		if (this.matchList != null && !this.matchList.isEmpty()) {
			resultMap = match(matchList);
		}
		
		// Empty input model
		if (this.wipeInputModel) {
			log.info("Emptying input model");	
			this.scoreInput.getJenaModel().removeAll();
		}
	}
	
	/**
	 * Get the OptionParser
	 * @return the OptionParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Score");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").setDescription("inputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivo-config").setDescription("vivoConfig JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(true));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output-config").setDescription("outputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE"));
		
		// Model name overrides
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivo jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		// Matching Algorithms 
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("match").setDescription("perform a match based on the paramaters given.  Properties may be passed as single properties applicable to both models or vivoProperty=scoreProperty").withParameters(true, "RDF_PREDICATES"));
	
		// Linking Methods
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("link").setDescription("link the two matched entities together using the pair of object properties vivoObj=scoreObj").withParameters(true, "RDF_PREDICATES"));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rename").setDescription("rename or remove the matched entity from scoring").withParameters(true, "RDF_PREDICATES"));
		
		// options
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("link-rename-inplace").setDescription("If set, this will not use the output model it will manipulate the records in place"));
		parser.addArgument(new ArgDef().setShortOption('w').setLongOpt("wipe-input-model").setDescription("If set, this will clear the input model after scoring is complete"));
		parser.addArgument(new ArgDef().setShortOption('q').setLongOpt("wipe-output-model").setDescription("If set, this will clear the output model before scoring begins"));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("push-all").setDescription("If set, this will push all matches and non matches to output model"));
				
		return parser;
	}
	
	/**
	 * Constructor
	 * @param jenaVivo model containing vivo statements
	 * @param jenaScoreInput model containing statements to be scored
	 * @param jenaScoreOutput output model
	 * @param clearInputModelArg If set, this will clear the input model after scoring is complete
	 * @param clearOutputModelArg If set, this will clear the output model before scoring begins
	 * @param exactMatchArg perform an exact match scoring
	 * @param pairwiseArg perform a pairwise scoring
	 * @param regexArg perform a regular expression scoring
	 * @param authorNameArg perform a author name scoring
	 * @param foreignKeyArg arguments for foreign key match
	 * @param objToVIVOArg the predicate that connects the object in score to the object in vivo
	 * @param objToScoreArg the predicate that connects the object in vivo to the object in score
	 */
	@SuppressWarnings("unused")
	public Score(JenaConnect jenaScoreInput, JenaConnect jenaVivo, JenaConnect jenaScoreOutput, boolean clearInputModelArg, boolean clearOutputModelArg, List<String> matchArg, boolean renameArg, String linkingArgs) {
		this.wipeInputModel = clearInputModelArg;
		this.wipeOutputModel = clearOutputModelArg;
		this.matchList = matchArg;
		this.pushAll = false;
		
		this.vivo = jenaVivo;
		
		// Create working model
		this.scoreInput = jenaScoreInput;
		
		// Create output model
		this.scoreOutput = jenaScoreOutput;		
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
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	public Score(ArgList opts) throws IOException {
		// Get optional inputs / set defaults
		// Check for config files, before parsing name options
		String jenaVIVO = opts.get("v");
		
		Properties inputOverrides = opts.getProperties("I");
		String jenaInput;
		if (opts.has("i")) {
			jenaInput = opts.get("i");
		} else {
			jenaInput = jenaVIVO;
			if (!inputOverrides.containsKey("modelName")) {
				inputOverrides.setProperty("modelName", "Scoring");
			}
		}
		
		Properties outputOverrides = opts.getProperties("O");
		String jenaOutput;
		if (opts.has("o")) {
			jenaOutput = opts.get("o");
		} else {
			jenaOutput = jenaVIVO;
			if (!outputOverrides.containsKey("modelName")) {
				outputOverrides.setProperty("modelName", "Staging");
			}
		}
		
		// Connect to vivo
		this.vivo = JenaConnect.parseConfig(jenaVIVO, opts.getProperties("V"));
		
		// Create working model
		this.scoreInput = JenaConnect.parseConfig(jenaInput, inputOverrides);
		
		// Create output model
		this.scoreOutput = JenaConnect.parseConfig(jenaOutput, outputOverrides);
		
		this.wipeInputModel = opts.has("w");
		this.wipeOutputModel = opts.has("q");
		this.pushAll = opts.has("l");
		this.matchList = opts.getAll("m");
	}
	
//	/**
//	 * Executes a sparql query against a JENA model and returns a result set
//	 * @param model a model containing statements
//	 * @param queryString the query to execute against the model
//	 * @return queryExec the executed query result set
//	 */
//	private static ResultSet executeQuery(Model model, String queryString) {
//		//		log.debug("query: " + queryString);
//		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
//		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
//		
//		return queryExec.execSelect();
//	}
	
	/**
	 * Commits node to a matched model
	 * @param result a model containing vivo statements
	 * @param authorNode the node of the author
	 * @param matchNode the node to match
	 * @param paperNode the node of the paper
	 */
	private static void commitResultNode(JenaConnect result, Resource authorNode, RDFNode matchNode, Resource paperNode) {
		log.trace("Found " + matchNode.toString() + " for person " + authorNode.toString());
		log.trace("Adding paper " + paperNode.toString());
		
		result.getJenaModel().add(recursiveSanitizeBuild(paperNode, new Stack<Resource>()).getJenaModel());
		
		replaceResource(authorNode, paperNode, result);
		
		// take results and store in matched model
		result.getJenaModel().commit();
	}
	
	/**
	 * Commits resultset to a matched model
	 * @param result a model containing vivo statements
	 * @param storeResult the result to be stored
	 * @param matchValue the node to match
	 * @param paperNode the node of the paper
	 */
	private static void commitResultSet(JenaConnect result, ResultSet storeResult, String matchValue, Resource paperNode) {
		Resource authorNode;
		QuerySolution vivoSolution;
		
		if (!storeResult.hasNext()) {
			result.getJenaModel().add(recursiveSanitizeBuild(paperNode,new Stack<Resource>()).getJenaModel());
		}
		
		// loop thru resultset
		while (storeResult.hasNext()) {
			vivoSolution = storeResult.next();
			
			// Grab person URI
			authorNode = vivoSolution.getResource("x");
			log.trace("Found \"" + matchValue + "\" for person <" + authorNode + ">");
			log.trace("Adding paper <" + paperNode + ">");
			
			result.getJenaModel().add(recursiveSanitizeBuild(paperNode, new Stack<Resource>()).getJenaModel());
			
			replaceResource(authorNode, paperNode, result);
			
			// take results and store in matched model
			result.getJenaModel().commit();
		}
	}
	
	/**
	 * Traverses paperNode and adds to toReplace model
	 * @param mainNode primary node
	 * @param paperNode node of paper
	 * @param toReplace model to replace
	 */
	private static void replaceResource(Resource mainNode, Resource paperNode, JenaConnect toReplace) {
		Resource authorship;
		String authorQuery;
		Property linkedAuthorOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkedAuthor");
		Property authorshipForPerson = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#authorInAuthorship");
		
		Property authorshipForPaper = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#informationResourceInAuthorship");
		Property paperOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkedInformationResource");
		Property rankOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#authorRank");
		
		Resource flag1 = ResourceFactory.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing");
		Resource authorshipClass = ResourceFactory.createResource("http://vivoweb.org/ontology/core#Authorship");
		
		Property rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Property rdfLabel = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
		int authorRank = 1;
		
		log.trace("Link paper <" + paperNode + "> to person <" + mainNode + "> in VIVO");
		authorship = ResourceFactory.createResource(paperNode.toString() + "/vivoAuthorship/l1");
		
		// string that finds the last name of the person in VIVO
		Statement authorLName = mainNode.getProperty(ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/lastName"));
		
		if (authorLName == null) {
			Statement authorworkEmail = mainNode.getProperty(ResourceFactory.createProperty("http://vivoweb.org/ontology/core#workEmail"));
			log.debug("Author Last name property is null, trying to find via email");
			if (authorworkEmail == null) {
				log.warn("Cannot find author -- linking failed");
				return;
			}
			authorQuery =	"PREFIX core: <http://vivoweb.org/ontology/core#> "+
							"SELECT ?badNode " +
							"WHERE {" +
								"?badNode core:workEmail ?value . " +
								"?badNode core:authorInAuthorship ?authorship . " +
								"?authorship core:linkedInformationResource <" + paperNode.toString() + "> ." +
								"FILTER (str(?value) = \"" + authorworkEmail.getObject().asLiteral().getValue() + "\")" +
							" }";
		} else {
			authorQuery =	"PREFIX core: <http://vivoweb.org/ontology/core#> " +
							"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
							"SELECT ?badNode " +
							"WHERE {" +
								"?badNode foaf:lastName ?value . " +
								"?badNode core:authorInAuthorship ?authorship . " +
								"?authorship core:linkedInformationResource <" + paperNode.toString() + ">" +
								"FILTER (str(?value) = \"" + authorLName.getObject().asLiteral().getValue() + "\")" +
							"}";
		}
		
		log.debug(authorQuery);
		
		ResultSet killList = toReplace.executeSelectQuery(authorQuery);
		
		while (killList.hasNext()) {
			QuerySolution killSolution = killList.next();
			
			// Grab person URI
			Resource removeAuthor = killSolution.getResource("badNode");
			
			// query the paper for the first author node (assumption that affiliation matches first author)
			log.debug("Delete Resource " + removeAuthor.toString());
			
			// return a statement iterator with all the statements for the Author that matches, then remove those
			// statements
			// model.remove is broken so we are using statement.remove
			StmtIterator deleteStmts = toReplace.getJenaModel().listStatements(null, null, removeAuthor);
			while (deleteStmts.hasNext()) {
				Statement dStmt = deleteStmts.next();
				log.debug("Delete Statement " + dStmt.toString());
				
				if (!dStmt.getSubject().equals(removeAuthor)) {
					Statement authorRankStmt = dStmt.getSubject().getProperty(rankOf);
					authorRank = authorRankStmt.getObject().asLiteral().getInt();
					
					StmtIterator authorshipStmts = dStmt.getSubject().listProperties();
					while (authorshipStmts.hasNext()) {
						log.debug("Delete Statement " + authorshipStmts.next().toString());
					}
					dStmt.getSubject().removeProperties();
					
					StmtIterator deleteAuthorshipStmts = toReplace.getJenaModel().listStatements(null, null, dStmt.getSubject());
					while (deleteAuthorshipStmts.hasNext()) {
						Statement dASStmt = deleteAuthorshipStmts.next();
						log.debug("Delete Statement " + dASStmt.toString());
						dASStmt.remove();
					}
					
				}
				
			}
			
			StmtIterator authorStmts = removeAuthor.listProperties();
			while (authorStmts.hasNext()) {
				log.debug("Delete Statement " + authorStmts.next().toString());
			}
			removeAuthor.removeProperties();
		}
		
		toReplace.getJenaModel().add(authorship, linkedAuthorOf, mainNode);
		log.trace("Link Statement [" + authorship.toString() + ", " + linkedAuthorOf.toString() + ", " + mainNode.toString() + "]");
		toReplace.getJenaModel().add(mainNode, authorshipForPerson, authorship);
		log.trace("Link Statement [" + mainNode.toString() + ", " + authorshipForPerson.toString() + ", " + authorship.toString() + "]");
		toReplace.getJenaModel().add(authorship, paperOf, paperNode);
		log.trace("Link Statement [" + authorship.toString() + ", " + paperOf.toString() + ", " + paperNode.toString() + "]");
		toReplace.getJenaModel().add(paperNode, authorshipForPaper, authorship);
		log.trace("Link Statement [" + paperNode.toString() + ", " + authorshipForPaper.toString() + ", " + authorship.toString() + "]");
		toReplace.getJenaModel().add(authorship, rdfType, flag1);
		log.trace("Link Statement [" + authorship.toString() + ", " + rdfType.toString() + ", " + flag1.toString() + "]");
		toReplace.getJenaModel().add(authorship, rdfType, authorshipClass);
		log.trace("Link Statement [" + authorship.toString() + ", " + rdfType.toString() + ", " + authorshipClass.toString() + "]");
		toReplace.getJenaModel().add(authorship, rdfLabel, "Authorship for Paper");
		log.trace("Link Statement [" + authorship.toString() + ", " + rdfLabel.toString() + ", " + "Authorship for Paper]");
		toReplace.getJenaModel().addLiteral(authorship, rankOf, authorRank);
		log.trace("Link Statement [" + authorship.toString() + ", " + rankOf.toString() + ", " + String.valueOf(authorRank) + "]");
		
		toReplace.getJenaModel().commit();
	}
	
	/**
	 * Traverses paperNode and adds to toReplace model
	 * @param mainRes the main resource
	 * @param linkRes the resource to link it to
	 * @return the model containing the sanitized info so far
	 * TODO change linkRes to be a string builder of the URI of the resource, that way you can do a String.Contains() for the URI of a resource
	 */
	private static JenaConnect recursiveSanitizeBuild(Resource mainRes, Stack<Resource> linkRes) {
		JenaConnect returnModel = new MemJenaConnect();
		StmtIterator mainStmts = mainRes.listProperties();
		
		while (mainStmts.hasNext()) {
			Statement stmt = mainStmts.nextStatement();
			
			// Don't add any scoring statements
			if (!stmt.getPredicate().getNameSpace().equalsIgnoreCase("http://vivoweb.org/ontology/score#")) {
				// log.debug(stmt.toString());
				returnModel.getJenaModel().add(stmt);
				
				//todo change the equals t o
				if (stmt.getObject().isResource() && !linkRes.contains(stmt.getObject().asResource()) && !stmt.getObject().asResource().equals(mainRes)) {
					linkRes.push(mainRes);
					returnModel.getJenaModel().add(recursiveSanitizeBuild(stmt.getObject().asResource(), linkRes).getJenaModel());
					linkRes.pop();
				}
				if (!linkRes.contains(stmt.getSubject()) && !stmt.getSubject().equals(mainRes)) {
					linkRes.push(mainRes);
					returnModel.getJenaModel().add(recursiveSanitizeBuild(stmt.getSubject(), linkRes).getJenaModel());
					linkRes.pop();
				}
			}
		}
		
		return returnModel;
	}
	

	
	
	/**
	 * Match on two predicates and insert foreign key links for each match
	 * @param scoreAttribute the predicate of the object in scoring to match on
	 * @param vivoAttribute the predicate of the object in vivo to match on
	 * @param scoreToVIVONode the predicate that connects the object in score to the object in vivo
	 * @param vivoToScoreNode the predicate that connects the object in vivo to the object in score
	 */
	public void foreignKeyMatch(String scoreAttribute, String vivoAttribute, String scoreToVIVONode, String vivoToScoreNode) {
		// Foreign Key Match
		log.info("Executing foreignKeyMatch for <" + scoreAttribute + "> against <" + vivoAttribute + ">");
		Property scoreAttr = this.scoreInput.getJenaModel().getProperty(scoreAttribute);
		StmtIterator stmtitr = this.scoreInput.getJenaModel().listStatements(null, scoreAttr, (RDFNode)null);
		if (!stmtitr.hasNext()) {
			log.trace("No matches found for <" + scoreAttribute + "> in input");
			return;
		}
		log.trace("Matches found for <" + scoreAttribute + "> in input");
		// look for exact match in vivo
		for (Statement stmt : IterableAdaptor.adapt(stmtitr)) {
			Resource sub = stmt.getSubject();
			String obj = stmt.getLiteral().getValue().toString();
			log.trace("Checking for \"" + obj + "\" from <" + sub + "> in VIVO");
			String query = ""+
				"SELECT ?sub"+"\n"+
				"WHERE {"+"\n\t"+
					"?sub <" + vivoAttribute + "> ?obj ."+"\n\t"+
					"FILTER (str(?obj) = \"" + obj + "\")"+"\n"+
				"}";
			log.debug(query);
			ResultSet matches = this.vivo.executeSelectQuery(query);
			if (!matches.hasNext()) {
				log.trace("No matches in VIVO found");
				if (this.pushAll) {
					this.scoreOutput.getJenaModel().add(recursiveSanitizeBuild(sub, new Stack<Resource>()).getJenaModel());
				}
			} else {
				log.trace("Matches in VIVO found");
				// loop thru resources
				while (matches.hasNext()) {
					// Grab person URI
					Resource vivoNode = matches.next().getResource("sub");
					log.trace("Found <" + sub + "> for VIVO entity <" + vivoNode + ">");
					log.trace("Adding entity <" + sub + "> to output");
					
					this.scoreOutput.getJenaModel().add(recursiveSanitizeBuild(sub, new Stack<Resource>()).getJenaModel());
					
					log.trace("Linking entity <" + sub + "> to VIVO entity <" + vivoNode + ">");
					
					this.scoreOutput.getJenaModel().add(sub, ResourceFactory.createProperty(scoreToVIVONode), vivoNode);
					this.scoreOutput.getJenaModel().add(vivoNode, ResourceFactory.createProperty(vivoToScoreNode), sub);
					
					// take results and store in matched model
					this.scoreOutput.getJenaModel().commit();
				}
			}
		}
	}
	
	/**
	 * Executes an exact matching algorithm for author disambiguation
	 * @param scoreAttribute attribute to perform the exact match in scoring
	 * @param vivoAttribute attribute to perform the exact match in vivo TODO: Add in foreign key match with removal of
	 * similarly linked item eg. -f <http://site/workEmail>,<http://vivo/workEmail> -toVivo <objectProperty>
	 * -toScoreItem <objectProperty> Thinking out loud - we'll need to modify the end results of exact match now that we
	 * are not creating authorships and authors for pubmed entries we'll need to just link the author whose name parts
	 * match someone in vivo Working on that now
	 */
	public void exactMatch(String scoreAttribute, String vivoAttribute) {
		String queryString;
		String matchValue;
		Resource paperNode;
		
		ResultSet scoreInputResult;
		
		String matchQuery = "SELECT ?x ?scoreAttribute " + "WHERE { ?x <" + scoreAttribute + "> ?scoreAttribute}";
		
		// Exact Match
		log.info("Executing exactMatch for <" + scoreAttribute + "> against <" + vivoAttribute + ">");
		log.debug(matchQuery);
		scoreInputResult = this.scoreInput.executeSelectQuery(matchQuery);
		
		// Log extra info message if none found
		if (!scoreInputResult.hasNext()) {
			log.trace("No matches found for <" + scoreAttribute + "> in input");
		} else {
			log.trace("Looping thru matching <" + scoreAttribute + "> from input");
		}
		
		// look for exact match in vivo
		for (QuerySolution scoreSolution : IterableAdaptor.adapt(scoreInputResult)) {
			matchValue = scoreSolution.getLiteral("scoreAttribute").getValue().toString();
			paperNode = scoreSolution.getResource("x");
			
			log.trace("Checking for \"" + matchValue + "\" from <" + paperNode + "> in VIVO");
			
			// Select all matching attributes from vivo store
			queryString =	"SELECT ?x " + 
							"WHERE { " + 
								"?x <" + vivoAttribute + "> ?value " + 
								"FILTER (str(?value) = \"" + matchValue + "\")" +
							"}";
			
			log.debug(queryString);
			
			commitResultSet(this.scoreOutput, this.vivo.executeSelectQuery(queryString), matchValue, paperNode);
		}
	}
	
	/**
	 * @return HashMap of the found matches
	 */
	private HashMap<String,String> match(List<String> propertyList){
		if (propertyList.size() < 1) {
			throw new IllegalArgumentException("No properties! SELECT cannot be created!");
		}
		
		Resource res;
		log.trace("Begin Match Query Build");
		
		//Find all namespace matches
		StringBuilder sQuery =	new StringBuilder(
			"SELECT ?sVIVO ?sScore \n" +
			"WHERE {\n"
		);

		int counter = 0;
		StringBuilder vivoWhere = new StringBuilder(" Graph ?vivoGraph {\n ");
		StringBuilder scoreWhere = new StringBuilder(" Graph ?scoreGraph {\n ");
		
		for (String properties : propertyList) {
			String[] propSplit = properties.split("=");
			if (propSplit.length > 2){
				log.error("The Data Properites passed can not contain multiple equals");
			} else if (properties.split("=").length == 2){
				vivoWhere.append("\t?sVIVO <").append(propSplit[0]).append("> ").append("?o" + counter).append(" . \n");
				scoreWhere.append("\t?sScore <").append(propSplit[1]).append("> ").append("?o" + counter).append(" . \n");
			} else {
				vivoWhere.append("\t?sVIVO <").append(properties).append("> ").append("?o" + counter).append(" . \n");
				scoreWhere.append("\t?sScore <").append(properties).append("> ").append("?o" + counter).append(" . \n");
			}			
		}
		
		vivoWhere.append("} . \n");
		scoreWhere.append("} . \n");
		
		//filters where the two items that match are the same node
		sQuery.append("\tFILTER (str(?sNew) != str(?sOld)) . \n");
				
		log.debug("Match Query:\n"+sQuery.toString());
		log.trace("End Match Query Build");
		
		log.trace("Begin Union Model");
		
		Graph vivoGraph = .createGraph("vivoGraph");
		
		
		
		//Model unionModel = model.getJenaModel().union(vivo.getJenaModel());
		log.trace("End Union Model");
		
		log.trace("Begin Run Query to ResultSet");
		Query query = QueryFactory.create(sQuery.toString(), Syntax.syntaxARQ);
		//QueryExecution queryExec = QueryExecutionFactory.create(query, unionModel);
		log.trace("End Run Query to ResultSet");
		
		log.trace("Begin Rename Matches");
		
		HashMap<String,String> uriArray = new HashMap<String,String>();
		/*for(QuerySolution solution : IterableAdaptor.adapt(queryExec.execSelect())) {
			uriArray.put(solution.getResource("sOld").getURI(), solution.getResource("sNew").getURI());
		}*/

		log.info("match found " + uriArray.keySet().size() + " links between vivo and the input model");
		
		return uriArray;
	}

	
	/**
	 * @param matchSet
	 */
	private void rename(HashMap<String,String> matchSet){
		/*Resource res;
		
		for(String oldUri : matchSet.keySet()) {
			res = model.getJenaModel().getResource(oldUri);		
			ResourceUtils.renameResource(res, uriArray.get(oldUri));
		}*/
	}
	
	/**
	 * Accessor for vivo model
	 * @return the vivo
	 */
	public JenaConnect getVivo() {
		return this.vivo;
	}
	
	/**
	 * Accessor for input model
	 * @return the scoreInput
	 */
	public JenaConnect getScoreInput() {
		return this.scoreInput;
	}
	
	/**
	 * Accessor for output model
	 * @return the scoreOutput
	 */
	public JenaConnect getScoreOutput() {
		return this.scoreOutput;
	}
	
	/**
	 * Close the resources used by score
	 */
	public void close() {
		// Close and done
		this.scoreInput.close();
		this.scoreOutput.close();
		this.vivo.close();
	}

	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		InitLog.initLogger(Score.class);
		log.info(getParser().getAppName()+": Start");
		try {
			Score Scoring = new Score(args);
			Scoring.execute();
			Scoring.close();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
}
