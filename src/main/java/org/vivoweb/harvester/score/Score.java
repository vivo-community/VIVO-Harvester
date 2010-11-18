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
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
	private final List<String> exactMatch;
	/**
	 * Arguments for pairwise algorithm
	 */
	private final List<String> pairwise;
	// TODO cah: remove when regex used
	//	/**
	//	 * Arguments for regex algorithm
	//	 */
	//	private final List<String> regex;
	/**
	 * Arguments for authorname algorithm
	 */
	private final String authorName;
	/**
	 * Arguments for foreign Key
	 */
	private final List<String> foreignKey;
	/**
	 * the predicate that connects the object in score to the object in vivo
	 */
	private final String objToVIVO;
	/**
	 * the predicate that connects the object in vivo to the object in score
	 */
	private final String objToScore;
	
	
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
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(), e);
		} catch(SAXException e) {
			log.error(e.getMessage(), e);
		} catch(IOException e) {
			log.error(e.getMessage(), e);
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
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
	 * Execute score object algorithms
	 */
	public void execute() {
		log.info("Running specified algorithims");
		
		// Empty input model
		if(this.wipeOutputModel) {
			log.info("Emptying output model");	
			this.scoreOutput.getJenaModel().removeAll();
		}
		
		// Call authorname matching
		if(this.authorName != null) {
			this.authorNameMatch(Integer.parseInt(this.authorName));
		}
		
		// call for ForeignKey linking
		if(this.foreignKey != null && !this.foreignKey.isEmpty()) {
			if(this.objToScore == null) {
				throw new IllegalArgumentException("Invalid Parameters, you must supply the object property from VIVO to the scoring model");
			}
			if(this.objToVIVO == null) {
				throw new IllegalArgumentException("Invalid Parameters, you must supply the object property from the scoring model to VIVO");
			}
			for(String attributePair : this.foreignKey) {
				String[] fKey = attributePair.split("=");
				if(fKey.length != 2) {
					throw new IllegalArgumentException("Invalid Parameters, You must supply the 2 data property one for the scoring and vivo models @ '" + attributePair + "'");
				}
				this.foreignKeyMatch(fKey[0], fKey[1], this.objToVIVO, this.objToScore);
			}
		}
		
		// Call each exactMatch
		if(this.exactMatch != null && !this.exactMatch.isEmpty()) {
			for(String attribute : this.exactMatch) {
				// this.exactMatch(attribute);
				// TODO Nicolas: fix exact match to take in two attributes <>,<> check with chaines
				// for proper format (? comma seperated list ?)
				this.exactMatch("<http://vivoweb.org/ontology/score#" + attribute + ">", "<http://vivoweb.org/ontology/core#" + attribute + ">");
			}
		}
		
		// Call each pairwise
		if(this.pairwise != null && !this.pairwise.isEmpty()) {
			for(String attribute : this.pairwise) {
				this.pairwise(attribute);
			}
		}
		
		// Call each regex
		// TODO Chris: uncomment when regex implemented
		// if(this.regex != null && !this.regex.isEmpty()) {
		// for(String attribute : this.regex) {
		// this.regex(attribute);
		// }
		// }
		
		// Empty input model
		if(this.wipeInputModel) {
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
		
		// scoring algorithms
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("exactMatch").setDescription("perform an exact match scoring").withParameters(true, "RDF_PREDICATES"));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("ufMatch").setDescription("perform an exact match scoring against the UF VIVO extension").withParameters(true, "RDF_PREDICATE"));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("pairWise").setDescription("perform a pairwise scoring").withParameters(true, "RDF_PREDICATE"));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("authorName").setDescription("perform a author name scoring").withParameter(true, "MIN_CHARS"));
		// TODO Chris: uncomment when regex implemented
		// parser.addArgument(new
		// ArgDef().setShortOption('r').setLongOpt("regex").setDescription("perform a regular expression scoring").withParameters(true,
		// "REGEX"));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("foreignKeyMatch").setDescription("preform a exact match where the id is a foreign link").withParameters(true, "RDF_PREDICATES"));
		
		// Object Property
		parser.addArgument(new ArgDef().setShortOption('x').setLongOpt("objPropToVIVO").setDescription("set the Object Property to the VIVO Model").withParameter(true, "OBJ_PROPERTIES"));
		parser.addArgument(new ArgDef().setShortOption('y').setLongOpt("objPropToScore").setDescription("set the Object Property to the Score Model").withParameter(true, "OBJ_PROPERTIES"));
		
		// options
		parser.addArgument(new ArgDef().setShortOption('w').setLongOpt("wipe-input-model").setDescription("If set, this will clear the input model after scoring is complete"));
		parser.addArgument(new ArgDef().setShortOption('q').setLongOpt("wipe-output-model").setDescription("If set, this will clear the output model before scoring begins"));
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("push-all").setDescription("If set, this will push all matches and non matches to output model"));
		
		// exactMatch foreignLink
		// exactMatch subNodeLink
		// score -e core:workEmail=score:workEmail -n Authorship .....
		// score -e core:UFID=score:UFID -f hasworkRole=workRoleIn
		
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
	public Score(JenaConnect jenaScoreInput, JenaConnect jenaVivo, JenaConnect jenaScoreOutput, boolean clearInputModelArg, boolean clearOutputModelArg, List<String> exactMatchArg, List<String> pairwiseArg, List<String> regexArg, String authorNameArg, List<String> foreignKeyArg, String objToVIVOArg, String objToScoreArg) {
		this.wipeInputModel = clearInputModelArg;
		this.wipeOutputModel = clearOutputModelArg;
		this.exactMatch = exactMatchArg;
		this.pairwise = pairwiseArg;
		//TODO cah: uncomment when regex implemented
		//		this.regex = regexArg;
		this.authorName = authorNameArg;
		this.foreignKey = foreignKeyArg;
		this.objToVIVO = objToVIVOArg;
		this.objToScore = objToScoreArg;
		this.pushAll = false;
		
		// Connect to vivo
		this.vivo = jenaVivo;
		StmtIterator vivoStmtItr = this.vivo.getJenaModel().listStatements();
		int vivoCount = 0;
		while(vivoStmtItr.hasNext()) {
			vivoStmtItr.next();
			vivoCount++;
		}
		log.debug("vivo has " + vivoCount + " statements in it");
		
		// Create working model
		this.scoreInput = jenaScoreInput;
		StmtIterator inputStmtItr = this.scoreInput.getJenaModel().listStatements();
		int inputCount = 0;
		while(inputStmtItr.hasNext()) {
			inputStmtItr.next();
			inputCount++;
		}
		log.debug("input has " + inputCount + " statements in it");
		
		// Create output model
		this.scoreOutput = jenaScoreOutput;
		StmtIterator outputStmtItr = this.scoreOutput.getJenaModel().listStatements();
		int outputCount = 0;
		while(outputStmtItr.hasNext()) {
			outputStmtItr.next();
			outputCount++;
		}
		log.debug("output has " + outputCount + " statements in it");
		
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 * @throws IllegalArgumentException arguments invalid
	 * @throws SAXException error parsing configs
	 * @throws ParserConfigurationException error parsing configs
	 */
	public Score(String... args) throws IllegalArgumentException, IOException, ParserConfigurationException, SAXException {
		ArgList opts = new ArgList(getParser(), args);
		
		// Get optional inputs / set defaults
		// Check for config files, before parsing name options
		String jenaVIVO = opts.get("v");
		
		Properties inputOverrides = opts.getProperties("I");
		String jenaInput;
		if(opts.has("i")) {
			jenaInput = opts.get("i");
		} else {
			jenaInput = jenaVIVO;
			if(!inputOverrides.containsKey("modelName")) {
				inputOverrides.setProperty("modelName", "Scoring");
			}
		}
		
		Properties outputOverrides = opts.getProperties("O");
		String jenaOutput;
		if(opts.has("o")) {
			jenaOutput = opts.get("o");
		} else {
			jenaOutput = jenaVIVO;
			if(!outputOverrides.containsKey("modelName")) {
				outputOverrides.setProperty("modelName", "Staging");
			}
		}
		
		// Connect to vivo
		this.vivo = JenaConnect.parseConfig(jenaVIVO, opts.getProperties("V"));
		StmtIterator vivoStmtItr = this.vivo.getJenaModel().listStatements();
		int vivoCount = 0;
		while(vivoStmtItr.hasNext()) {
			vivoStmtItr.next();
			vivoCount++;
		}
		log.debug("vivo has " + vivoCount + " statements in it");
		
		// Create working model
		this.scoreInput = JenaConnect.parseConfig(jenaInput, inputOverrides);
		StmtIterator inputStmtItr = this.scoreInput.getJenaModel().listStatements();
		int inputCount = 0;
		while(inputStmtItr.hasNext()) {
			inputStmtItr.next();
			inputCount++;
		}
		log.debug("input has " + inputCount + " statements in it");
		
		// Create output model
		this.scoreOutput = JenaConnect.parseConfig(jenaOutput, outputOverrides);
		StmtIterator outputStmtItr = this.scoreOutput.getJenaModel().listStatements();
		int outputCount = 0;
		while(outputStmtItr.hasNext()) {
			outputStmtItr.next();
			outputCount++;
		}
		log.debug("output has " + outputCount + " statements in it");
		
		this.wipeInputModel = opts.has("w");
		this.wipeOutputModel = opts.has("q");
		this.pushAll = opts.has("l");
		this.exactMatch = opts.getAll("e");
		this.pairwise = opts.getAll("p");
		//TODO cah: uncomment when regex implemented
		//		this.regex = opts.getAll("r");
		this.authorName = opts.get("a");
		this.foreignKey = opts.getAll("f");
		this.objToVIVO = opts.get("x");
		this.objToScore = opts.get("y");
	}
	
	/**
	 * Executes a sparql query against a JENA model and returns a result set
	 * @param model a model containing statements
	 * @param queryString the query to execute against the model
	 * @return queryExec the executed query result set
	 */
	private static ResultSet executeQuery(Model model, String queryString) {
		//		log.debug("query: " + queryString);
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
		
		return queryExec.execSelect();
	}
	
	/**
	 * Commits node to a matched model
	 * @param result a model containing vivo statements
	 * @param authorNode the node of the author
	 * @param paperResource the paper of the resource
	 * @param matchNode the node to match
	 * @param paperNode the node of the paper
	 */
	private static void commitResultNode(Model result, RDFNode authorNode, Resource paperResource, RDFNode matchNode, RDFNode paperNode) {
		log.trace("Found " + matchNode.toString() + " for person " + authorNode.toString());
		log.trace("Adding paper " + paperNode.toString());
		
		result.add(recursiveSanitizeBuild(paperResource, new Stack<Resource>()));
		
		replaceResource(authorNode, paperNode, result);
		
		// take results and store in matched model
		result.commit();
	}
	
	/**
	 * Commits resultset to a matched model
	 * @param result a model containing vivo statements
	 * @param storeResult the result to be stored
	 * @param paperResource the paper of the resource
	 * @param matchNode the node to match
	 * @param paperNode the node of the paper
	 */
	private static void commitResultSet(Model result, ResultSet storeResult, Resource paperResource, RDFNode matchNode, RDFNode paperNode) {
		RDFNode authorNode;
		QuerySolution vivoSolution;
		
		if(!storeResult.hasNext()){
			result.add(recursiveSanitizeBuild(paperResource,new Stack<Resource>()));
		}
		
		// loop thru resultset
		while(storeResult.hasNext()) {
			vivoSolution = storeResult.next();
			
			// Grab person URI
			authorNode = vivoSolution.get("x");
			log.trace("Found " + matchNode.toString() + " for person " + authorNode.toString());
			log.trace("Adding paper " + paperNode.toString());
			
			result.add(recursiveSanitizeBuild(paperResource, new Stack<Resource>()));
			
			replaceResource(authorNode, paperNode, result);
			
			// take results and store in matched model
			result.commit();
		}
	}
	
	/**
	 * Traverses paperNode and adds to toReplace model
	 * @param mainNode primary node
	 * @param paperNode node of paper
	 * @param toReplace model to replace
	 */
	private static void replaceResource(RDFNode mainNode, RDFNode paperNode, Model toReplace) {
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
		
		log.trace("Link paper " + paperNode.toString() + " to person " + mainNode.toString() + " in VIVO");
		authorship = ResourceFactory.createResource(paperNode.toString() + "/vivoAuthorship/l1");
		
		// string that finds the last name of the person in VIVO
		Statement authorLName = ((Resource)mainNode).getProperty(ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/lastName"));
		
		if (authorLName == null) {
			Statement authorworkEmail = ((Resource)mainNode).getProperty(ResourceFactory.createProperty("http://vivoweb.org/ontology/core#workEmail"));
			log.debug("Author Last name property is null, trying to find via email");
			if (authorworkEmail == null) {
				log.warn("Cannot find author -- linking failed");
				return;
			}
			authorQuery = "PREFIX core: <http://vivoweb.org/ontology/core#> " + "SELECT ?badNode " + "WHERE {?badNode core:workEmail \"" + authorworkEmail.getObject().toString() + "\" . " + "?badNode core:authorInAuthorship ?authorship . " + "?authorship core:linkedInformationResource <" + paperNode.toString() + "> }";
		} else {
			authorQuery = "PREFIX core: <http://vivoweb.org/ontology/core#> " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + "SELECT ?badNode " + "WHERE {?badNode foaf:lastName \"" + authorLName.getObject().toString() + "\" . " + "?badNode core:authorInAuthorship ?authorship . " + "?authorship core:linkedInformationResource <" + paperNode.toString() + "> }";
		}
		
		log.debug(authorQuery);
		
		ResultSet killList = executeQuery(toReplace, authorQuery);
		
		while(killList.hasNext()) {
			QuerySolution killSolution = killList.next();
			
			// Grab person URI
			Resource removeAuthor = killSolution.getResource("badNode");
			
			// query the paper for the first author node (assumption that affiliation matches first author)
			log.debug("Delete Resource " + removeAuthor.toString());
			
			// return a statement iterator with all the statements for the Author that matches, then remove those
			// statements
			// model.remove is broken so we are using statement.remove
			StmtIterator deleteStmts = toReplace.listStatements(null, null, removeAuthor);
			while(deleteStmts.hasNext()) {
				Statement dStmt = deleteStmts.next();
				log.debug("Delete Statement " + dStmt.toString());
				
				if(!dStmt.getSubject().equals(removeAuthor)) {
					Statement authorRankStmt = dStmt.getSubject().getProperty(rankOf);
					authorRank = authorRankStmt.getObject().asLiteral().getInt();
					
					StmtIterator authorshipStmts = dStmt.getSubject().listProperties();
					while(authorshipStmts.hasNext()) {
						log.debug("Delete Statement " + authorshipStmts.next().toString());
					}
					dStmt.getSubject().removeProperties();
					
					StmtIterator deleteAuthorshipStmts = toReplace.listStatements(null, null, dStmt.getSubject());
					while(deleteAuthorshipStmts.hasNext()) {
						Statement dASStmt = deleteAuthorshipStmts.next();
						log.debug("Delete Statement " + dASStmt.toString());
						dASStmt.remove();
					}
					
				}
				
			}
			
			StmtIterator authorStmts = removeAuthor.listProperties();
			while(authorStmts.hasNext()) {
				log.debug("Delete Statement " + authorStmts.next().toString());
			}
			removeAuthor.removeProperties();
		}
		
		toReplace.add(authorship, linkedAuthorOf, mainNode);
		log.trace("Link Statement [" + authorship.toString() + ", " + linkedAuthorOf.toString() + ", " + mainNode.toString() + "]");
		toReplace.add((Resource)mainNode, authorshipForPerson, authorship);
		log.trace("Link Statement [" + mainNode.toString() + ", " + authorshipForPerson.toString() + ", " + authorship.toString() + "]");
		toReplace.add(authorship, paperOf, paperNode);
		log.trace("Link Statement [" + authorship.toString() + ", " + paperOf.toString() + ", " + paperNode.toString() + "]");
		toReplace.add((Resource)paperNode, authorshipForPaper, authorship);
		log.trace("Link Statement [" + paperNode.toString() + ", " + authorshipForPaper.toString() + ", " + authorship.toString() + "]");
		toReplace.add(authorship, rdfType, flag1);
		log.trace("Link Statement [" + authorship.toString() + ", " + rdfType.toString() + ", " + flag1.toString() + "]");
		toReplace.add(authorship, rdfType, authorshipClass);
		log.trace("Link Statement [" + authorship.toString() + ", " + rdfType.toString() + ", " + authorshipClass.toString() + "]");
		toReplace.add(authorship, rdfLabel, "Authorship for Paper");
		log.trace("Link Statement [" + authorship.toString() + ", " + rdfLabel.toString() + ", " + "Authorship for Paper]");
		toReplace.addLiteral(authorship, rankOf, authorRank);
		log.trace("Link Statement [" + authorship.toString() + ", " + rankOf.toString() + ", " + String.valueOf(authorRank) + "]");
		
		toReplace.commit();
	}
	
	/**
	 * Traverses paperNode and adds to toReplace model
	 * @param mainRes the main resource
	 * @param linkRes the resource to link it to
	 * @return the model containing the sanitized info so far
	 * TODO change linkRes to be a string builder of the URI of the resource, that way you can do a String.Contains() for the URI of a resource
	 */
	private static Model recursiveSanitizeBuild(Resource mainRes, Stack<Resource> linkRes) {
		Model returnModel = ModelFactory.createDefaultModel();
		StmtIterator mainStmts = mainRes.listProperties();
		
		while(mainStmts.hasNext()) {
			Statement stmt = mainStmts.nextStatement();
			
			// Don't add any scoring statements
			if(!stmt.getPredicate().getNameSpace().equalsIgnoreCase("http://vivoweb.org/ontology/score#")) {
				// log.debug(stmt.toString());
				returnModel.add(stmt);
				
				//todo change the equals t o
				if(stmt.getObject().isResource() && !linkRes.contains(stmt.getObject().asResource()) && !stmt.getObject().asResource().equals(mainRes)) {
					linkRes.push(mainRes);
					returnModel.add(recursiveSanitizeBuild(stmt.getObject().asResource(), linkRes));
					linkRes.pop();
				}
				if(!linkRes.contains(stmt.getSubject()) && !stmt.getSubject().equals(mainRes)) {
					linkRes.push(mainRes);
					returnModel.add(recursiveSanitizeBuild(stmt.getSubject(), linkRes));
					linkRes.pop();
				}
			}
		}
		
		return returnModel;
	}
	
	/**
	 * Executes a pair scoring method, utilizing the matchAttribute. This attribute is expected to return 2 to n results
	 * from the given query. This "pair" will then be utilized as a matching scheme to construct a sub dataset. This
	 * dataset can be scored and stored as a match
	 * @param attribute an attribute to perform the matching query
	 */
	public void pairwise(String attribute) {
		// iterate thru scoringInput pairs against matched pairs
		// TODO Nicholas: finish implementation
		// if pairs match, store publication to matched author in Model
		
		// Create pairs list from input
		log.info("Executing pairWise for " + attribute);
		log.warn("Pairwise is not complete");
		
		// Log extra info message if none found
		// Create pairs list from vivo
		// Log extra info message if none found
		// look for exact match in vivo
		// create pairs of *attribute* from matched
	}
	
	// TODO Chris: uncomment when regex implemented
	//	/**
	//	 * Executes a regex scoring method
	//	 * @param regexString string containing regular expression
	//	 */
	//	private void regex(String regexString) {
	//		// TODO Chris: finish implementation
	//		log.info("Executing " + regexString + " regular expression");
	//		log.warn("Regex is not complete");
	//	}
	
	/**
	 * Executes an author name matching algorithm for author disambiguation
	 * @param minChars minimum number of chars to require for first name portion of match
	 */
	public void authorNameMatch(int minChars) {
		
		//TODO: Nicholas This function needs some logical cleanup.. :-)
		
		String queryString;
		Resource paperResource;
		RDFNode lastNameNode;
		RDFNode foreNameNode;
		RDFNode middleNameNode;
		RDFNode paperNode;
		RDFNode authorNode = null;
		RDFNode matchNode = null;
		RDFNode loopNode;
		String vivoForeNameInitials;
		String pubmedFornameInitials;
		String lastName;
		String middleName;
		ResultSet vivoResult;
		QuerySolution scoreSolution;
		QuerySolution scorePaperSolution;
		QuerySolution vivoSolution;
		ResultSet scoreInputResult;
		ResultSet scorePaperResult;
		String scoreMatch;
		ArrayList<QuerySolution> matchNodes = new ArrayList<QuerySolution>();
		int loop;
		int minimum = minChars;
		
		String matchQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + "PREFIX core: <http://vivoweb.org/ontology/core#> " + "PREFIX score: <http://vivoweb.org/ontology/score#> " + "SELECT REDUCED ?x ?lastName ?foreName ?middleName " + "WHERE { ?x foaf:lastName ?lastName . ?x score:foreName ?foreName . OPTIONAL { ?x core:middleName ?middleName}}";
		
		log.info("Executing authorNameMatch");
		log.debug(matchQuery);
		scoreInputResult = executeQuery(this.scoreInput.getJenaModel(), matchQuery);
		
		// Log extra info message if none found
		if(!scoreInputResult.hasNext()) {
			log.trace("No author names found in input");
		} else {
			log.trace("Looping thru matching authors from input");
		}
		
		// look for exact match in vivo
		while (scoreInputResult.hasNext()) {
			scoreSolution = scoreInputResult.next();
			lastNameNode = scoreSolution.get("lastName");
			foreNameNode = scoreSolution.get("foreName");
			middleNameNode = scoreSolution.get("middleName");
			authorNode = scoreSolution.get("x");
			
			//Find paper
			//TODO: Have Stephen show Nicholas what a fool he is for writing this
			matchQuery = "PREFIX core: <http://vivoweb.org/ontology/core#> " + "SELECT ?x ?paper " + "WHERE { ?x core:linkedAuthor <" + authorNode + "> . ?x core:linkedInformationResource ?paper}";
			
			log.debug(matchQuery);
			scorePaperResult = executeQuery(this.scoreInput.getJenaModel(), matchQuery);
			
			if (scorePaperResult.hasNext()) {
				scorePaperSolution = scorePaperResult.next();
				paperNode = scorePaperSolution.get("paper");
				paperResource = scorePaperSolution.getResource("paper");
			} else {
				//go to next one, can't find paper
				log.error("Can't find paper for " + authorNode);
				continue;
			}
			
			matchNodes.clear();
			matchNode = null;
			
			//reset minChars if first name is less than than the passed in minimum
			if (foreNameNode.toString().length() < minChars) {
				minimum = foreNameNode.toString().length();
				log.trace("Reset minimum characters to match to " + minimum);
			}
			
			//support middlename parse out of forename or from pubmed
			if (middleNameNode != null) {
				log.trace("Checking for " + lastNameNode.toString() + ", " + foreNameNode.toString() + " " + middleNameNode.toString() + " from " + paperNode.toString() + " in VIVO");
				pubmedFornameInitials = foreNameNode.toString().substring(0,1) + middleNameNode.toString().substring(0,1);
				log.trace("Using " + pubmedFornameInitials + " as pubmed author first and middle initial");			
			} else {
				log.trace("Checking for " + lastNameNode.toString() + ", " + foreNameNode.toString() + " from " + paperNode.toString() + " in VIVO");
			
				//parse out middle initial / name from foreName
				String splitName[] = foreNameNode.toString().split(" ");
				
				if (splitName.length == 2) {
					lastName = splitName[0];
					middleName = splitName[1];
					pubmedFornameInitials = lastName.substring(0,1) + middleName.substring(0,1);
					log.trace("Using " + pubmedFornameInitials + " as pubmed author first and middle initial");
				} else {
					lastName = null;
					middleName = null;
					pubmedFornameInitials = "";
				}
			}
			
			// ensure first name and last name are not blank
			if (lastNameNode.toString() == null || foreNameNode.toString() == null) {
				log.trace("Incomplete name, skipping");
			} else {
				scoreMatch = lastNameNode.toString();
				
				// Select all matching authors from vivo store
				queryString = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + "PREFIX core: <http://vivoweb.org/ontology/core#> " + "SELECT REDUCED ?x ?firstName ?middleName " + "WHERE { ?x foaf:lastName" + " \"" + scoreMatch + "\" . ?x foaf:firstName ?firstName . OPTIONAL { ?x core:middleName ?middleName}}";
				
				log.debug(queryString);
				
				vivoResult = executeQuery(this.vivo.getJenaModel(), queryString);
				
				// Loop thru results and only keep if the last name, and first initial match
				while(vivoResult.hasNext()) {
					vivoSolution = vivoResult.next();
					log.trace(vivoSolution.toString());
					loopNode = vivoSolution.get("firstName");
					middleNameNode = vivoSolution.get("middleName");
					
					if (loopNode.toString().length() >= 1 && foreNameNode.toString().length() >= 1) {
						if (foreNameNode.toString().substring(0, 1).equals(loopNode.toString().substring(0, 1))) {								
							loop = 0;
							while(loopNode.toString().regionMatches(true, 0, foreNameNode.toString(), 0, loop)) {
								loop++;
							}
							loop--;
							
							if (middleNameNode != null) {
								log.trace("Checking " + loopNode + " " + middleNameNode.toString() + " " + vivoSolution.get("x"));
							}
							else {
								log.trace("Checking " + loopNode + " " + vivoSolution.get("x"));
							}
							
							//Grab the initials, and check that as best match
							if (middleNameNode != null) {
								middleName = middleNameNode.toString();
								vivoForeNameInitials = loopNode.toString().substring(0,1) + middleNameNode.toString().substring(0,1);
								log.trace(loopNode.toString() + " has first and middle initial of " + vivoForeNameInitials);
								
								//If initials match, set as match, unless we've matched to a name below of at least 2 chars
								//TODO Nicholas: Fix the preference for the last "best" match
								if (vivoForeNameInitials.equalsIgnoreCase(pubmedFornameInitials) && loopNode.toString().length() == 1) {
									log.trace("Setting " + loopNode.toString()  + " " + middleName + " as best match, matched initials " + vivoForeNameInitials);
									matchNode = loopNode;
									authorNode = vivoSolution.get("x");
								}
							} else {
								middleName = "";
								vivoForeNameInitials = "";
							}
							
							if(loop < minimum) {
								log.trace(loopNode.toString() + " only matched " + loop + " of " + foreNameNode.toString().length() + ". Minimum needed to match is " + minChars);
							} else {
								// if loopNode matches more of foreNameNode, it's the new best match
								//loopNode must also not cotradict Pubmed
								// TODO Nicholas: Fix the preference for the first "best" match
								if ((matchNode == null || !matchNode.toString().regionMatches(true, 0, foreNameNode.toString(), 0, loop)) && (vivoForeNameInitials.equalsIgnoreCase(pubmedFornameInitials) || pubmedFornameInitials.isEmpty() || vivoForeNameInitials.isEmpty())) {
									log.trace("Setting " + loopNode.toString() + " " + middleName + " as best match, matched " + loop + " of " + foreNameNode.toString().length());
									matchNode = loopNode;
									authorNode = vivoSolution.get("x");
								} else {
									//check for better match
									if (vivoForeNameInitials.equalsIgnoreCase(pubmedFornameInitials)) {
										log.trace("Setting " + loopNode.toString() + " " + middleName + " as new best match, matched " + loop + " of " + foreNameNode.toString().length() + " and initials " + pubmedFornameInitials);
										matchNode = loopNode;
										authorNode = vivoSolution.get("x");
									} else {
										log.trace(loopNode.toString() + " matched " + loop + " of " + foreNameNode.toString().length());
									}
								}
							}
						} else {
							// do nothing
						}
					}
				}
				if(matchNode != null && authorNode != null) {
					log.trace("Keeping " + matchNode.toString());
					commitResultNode(this.scoreOutput.getJenaModel(), authorNode, paperResource, matchNode, paperNode);
				}
			}
		}
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
		//		Property vivoAttr = this.scoreInput.getJenaModel().getProperty(vivoAttribute);
		StmtIterator stmtitr = this.scoreInput.getJenaModel().listStatements(null, scoreAttr, (RDFNode)null);
		//		String stmtQuery = "SELECT ?sub ?obj\nWHERE\n{\n  ?sub <" + scoreAttribute + "> ?obj\n}";
		//		System.out.println(stmtQuery);
		//		ResultSet stmtRS = this.vivo.executeQuery(stmtQuery);
		if(!stmtitr.hasNext()) {
			log.trace("No matches found for <" + scoreAttribute + "> in input");
			return;
		}
		log.trace("Matches found for <" + scoreAttribute + "> in input");
		//		for(Statement s : IterableAdaptor.adapt(this.vivo.getJenaModel().listStatements(null, vivoAttr, (RDFNode)null))) {
		//			log.debug("VIVO Match: " + s);
		//		}
		// look for exact match in vivo
		while(stmtitr.hasNext()) {
			Statement stmt = stmtitr.next();
			Resource sub = stmt.getSubject();
			String obj = stmt.getLiteral().getString();
			log.trace("Checking for \"" + obj + "\" from <" + sub + "> in VIVO");
			//			StmtIterator matches = this.vivo.getJenaModel().listStatements(null, vivoAttr, obj);
			String query = ""+
				"SELECT ?sub"+"\n"+
				"WHERE {"+"\n\t"+
					"?sub <" + vivoAttribute + "> ?obj ."+"\n\t"+
					"FILTER (str(?obj) = \"" + obj + "\")"+"\n"+
				"}";
			log.debug(query);
			ResultSet matches = executeQuery(this.vivo.getJenaModel(), query);
			if(!matches.hasNext()) {
				log.trace("No matches in VIVO found");
				if (this.pushAll){
					this.scoreOutput.getJenaModel().add(recursiveSanitizeBuild(sub, new Stack<Resource>()));
				}
			} else {
				log.trace("Matches in VIVO found");
				// loop thru resources
				while(matches.hasNext()) {
					// Grab person URI
					//					Resource vivoNode = matchesRS.next().getSubject();
					Resource vivoNode = matches.next().getResource("sub");
					log.trace("Found <" + sub + "> for VIVO entity <" + vivoNode + ">");
					log.trace("Adding entity <" + sub + "> to output");
					
					this.scoreOutput.getJenaModel().add(recursiveSanitizeBuild(sub, new Stack<Resource>()));
					
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
		String scoreMatch;
		String queryString;
		Resource paperResource;
		RDFNode matchNode;
		RDFNode paperNode;
		ResultSet vivoResult;
		QuerySolution scoreSolution;
		ResultSet scoreInputResult;
		
		String matchQuery = "SELECT ?x ?scoreAttribute " + "WHERE { ?x " + scoreAttribute + " ?scoreAttribute}";
		
		// Exact Match
		log.info("Executing exactMatch for " + scoreAttribute + " against " + vivoAttribute);
		log.debug(matchQuery);
		scoreInputResult = executeQuery(this.scoreInput.getJenaModel(), matchQuery);
		
		// Log extra info message if none found
		if(!scoreInputResult.hasNext()) {
			log.trace("No matches found for " + scoreAttribute + " in input");
		} else {
			log.trace("Looping thru matching " + scoreAttribute + " from input");
		}
		
		// look for exact match in vivo
		while(scoreInputResult.hasNext()) {
			scoreSolution = scoreInputResult.next();
			matchNode = scoreSolution.get("scoreAttribute");
			paperNode = scoreSolution.get("x");
			paperResource = scoreSolution.getResource("x");
			
			scoreMatch = matchNode.toString();
			
			log.trace("Checking for " + scoreMatch + " from " + paperNode.toString() + " in VIVO");
			
			// Select all matching attributes from vivo store
			queryString = "SELECT ?x " + "WHERE { ?x " + vivoAttribute + " \"" + scoreMatch + "\" }";
			
			log.debug(queryString);
			
			vivoResult = executeQuery(this.vivo.getJenaModel(), queryString);
			
			commitResultSet(this.scoreOutput.getJenaModel(), vivoResult, paperResource, matchNode, paperNode);
		}
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
}
