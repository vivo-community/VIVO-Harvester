/**
 * 
 */
package org.vivoweb.harvester.score;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.vivoweb.harvester.score.Score;

/**
 * @author Chris Westling
 * @author Nicholas Skaggs  
 *
 */
public class PubmedScore {
	
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
	 * Execute score object algorithms
	 */
	public void execute() {
		log.info("Running specified algorithims");
		
		// Empty input model
		if(this.wipeOutputModel) {
			log.info("Emptying output model");	
			this.scoreOutput.getJenaModel().removeAll();
		}
		
		this.pubmedMatch();
		
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
	 * @param pushAllArg If set, this will push all statements into vivo
	 */
	public PubmedScore(JenaConnect jenaScoreInput, JenaConnect jenaVivo, JenaConnect jenaScoreOutput, boolean clearInputModelArg, boolean clearOutputModelArg, boolean pushAllArg) throws IllegalArgumentException, IOException, ParserConfigurationException, SAXException {
		this.wipeInputModel = clearInputModelArg;
		this.wipeOutputModel = clearOutputModelArg;
		this.pushAll = pushAllArg;
		
		// Connect to vivo
		this.vivo = jenaVivo;
		
		// Connect to input
		this.scoreInput = jenaScoreInput;
		
		// Connect to ouput
		this.scoreOutput = jenaScoreOutput;		
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 * @throws IllegalArgumentException arguments invalid
	 * @throws SAXException error parsing configs
	 * @throws ParserConfigurationException error parsing configs
	 */
	public PubmedScore(String... args) throws IllegalArgumentException, IOException, ParserConfigurationException, SAXException {
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
		
		// Connect to input
		this.scoreInput = JenaConnect.parseConfig(jenaInput, inputOverrides);
		
		// Create to output
		this.scoreOutput = JenaConnect.parseConfig(jenaOutput, outputOverrides);
		
		this.wipeInputModel = opts.has("w");
		this.wipeOutputModel = opts.has("q");
		this.pushAll = opts.has("l");
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
	 * Executes a weighted matching algorithm for author disambiguation for pubmed publications
	 * Ported from INSITU code from Chris Westling cmw48@cornell.edu
	 * 	Algorithm for Pubmed Name Matching
	 *				fullEmailScore + foreNameScore + partEmailScore + domainPartEmailScore + initMatchScore
	 *	PM =    -----------------------------------------------------------------------------------------
	 *												2
	 */
	public void pubmedMatch() {
		String queryString;
		ResultSet vivoResult;
		QuerySolution scoreSolution;
		ResultSet scoreInputResult;
		Resource author;
		Resource paper;
		String foreName;
		String middleName;
		String lastName;
		String workEmail;
		
		String matchQuery =	"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
							"PREFIX core: <http://vivoweb.org/ontology/core#> " +
							"PREFIX score: <http://vivoweb.org/ontology/score#> " +
							"SELECT REDUCED ?x ?lastName ?foreName ?middleName ?workEmail " +
							"WHERE {" +
								"?x foaf:lastName ?lastName . " +
								"?x score:foreName ?foreName . " +
								"OPTIONAL { ?x core:middleName ?middleName } . " +
								"OPTIONAL { ?x score:workEmail ?workEmail } " +
							"}";

		log.info("Executing Pubmed Scoring");	

		log.debug(matchQuery);
		scoreInputResult = this.scoreInput.executeSelectQuery(matchQuery);
		
		// Log info message if none found
		if (!scoreInputResult.hasNext()) {
			log.info("No authors found in harvested pubmed publications");
		} else {
			log.info("Looping thru authors from harvested pubmed publications");
		}
		
		// look for exact match in vivo
		while (scoreInputResult.hasNext()) {
			//Grab results
			scoreSolution = scoreInputResult.next();
			log.trace(scoreSolution.toString());
			author = scoreSolution.getResource("x");
			//paper = scoreSolution.getResource("publication");
			foreName = scoreSolution.get("foreName").toString();
			lastName = scoreSolution.get("lastName").toString();
			if (scoreSolution.get("workEmail") != null) {
				workEmail = scoreSolution.get("workEmail").toString();
			} else {
				workEmail = null;
			}
			
			//general string cleanup
			//remove , from names
			foreName = foreName.replace(",", " ");			
			lastName = lastName.replace(",", "");
			
			//support middlename parse out of forename from pubmed
			if (scoreSolution.get("middleName") == null) {	
				log.trace("Checking for middle name from " + lastName + ", " + foreName);
			
				//parse out middle initial / name from foreName
				String splitName[] = foreName.split(" ");
				
				if (splitName.length == 2) {
					foreName = splitName[0];
					middleName = splitName[1];
					foreName = foreName.trim();
					middleName = middleName.trim();
				} else {
					middleName = null;
				}
				log.trace("Using " + lastName + ", " + foreName + ", " + middleName + " as name");
			} else {
				middleName = scoreSolution.get("middleName").toString();
				middleName = middleName.replace(",", "");
				log.trace("Using supplied pubmed middle name " + lastName + ", " + foreName + ", " + middleName);
			}
			
			
			
			// ensure first name and last name are not blank
			if (lastName.toString() == null || foreName.toString() == null) {
				log.trace("Incomplete name, skipping");
				continue;
			}
				
			// Select all matching authors from vivo store
			queryString =	"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + 
							"PREFIX core: <http://vivoweb.org/ontology/core#> " + 
							"SELECT DISTINCT ?x ?lastName ?foreName ?middleName ?workEmail " +
							"WHERE { " +
								"{?x foaf:lastName" + " \"" + lastName + "\"} UNION " +
								"{?x foaf:firstName" + " \"" + foreName + "\"} UNION " +
								"{?x core:middleName" + " \"" + middleName + "\"} UNION " +
								"{?x core:workEmail" + " \"" + workEmail + "\"} . " +
								"OPTIONAL { ?x foaf:lastName ?lastName } . " +
								"OPTIONAL { ?x foaf:firstName ?foreName } . " +
								"OPTIONAL { ?x core:middleName ?middleName } . " +
								"OPTIONAL { ?x core:workEmail ?workEmail } " +
							"}";
			
			log.debug(queryString);
			
			vivoResult = this.vivo.executeSelectQuery(queryString);
			
			// Run comparisions
			while (vivoResult.hasNext()) {
				log.trace(vivoResult.next().toString());
			}
		}
//		This is cmw48's logic, don't blame this on CTRIP:		
//		# Scoring Logic											  
//		if lastnamematch then {
//		    if fullemailmatch then {
//			    fullemailscore = 1.0; 
//			} else {	
//			    if forenamematch then {
//				    forenamescore += .50
//				}
//			    if partemailmatch then {
//				    partemailscore += .05
//				}		
//			    if domainpartemailmatch then {
//				    domainpartemailscore += .15
//				}    
//				if bothinitmatch then {
//				    initmatchscore += .25
//				} else {
//				    if firstinitmatch then {
//					    initmatchscore += .20
//					}
//				}
//		}		
//
//		# Max scores based on logic above
//
//		       1 + .50 + .05 + .15 + .25
//		 Pm =  ------------------------
//		                  2			
//					
//		# Narrative logic
//
//		if 100% last name match then
//		    #look for email match
//			test for zerolength affiliation string
//			if email address exists in affiliation string
//			    immediately write as <affiliationEmail>
//				if targetEmail is nonzerolength
//				    if targetEmail == affiliationEmail
//					    score = 1    #full email match means 100% certainty
//					else:  # look for partial email match 
//					    if first three chars of both strings match  "cmw"
//						    score = 
//						else if domain part of email matches "cornell.edu"
//		                    score = 
//		            else no email match
//			#check for a full initial match
//		    #assume that each pub has (numauthors) authors, based on the premise that LastName is always present
//		    #cycle through authors starting with author[0] to find match	
//			    #since initials or forename tags may not appear in some PubMed records, test and set values				
//				#look for first name match
//		        #how long is our TargetAuthor firstname?			
//				#look only at section of PM first name that's as long as our target search string  (target = Chris, PMFirst = Christopher, then foreName = leftstr(PMFirst, len(target)))
//				    If foreName == firstName
//					    score += .5
//				# we know first initial matches, is there a middle initial?
//		        # find out if initials string is > 1
//				# if more than one character in initials, there must be a middle initial
//		        # middle initial *should be* rightmost character
//				# do we have a middle initial in our SearchAuthorName
//		            if both initials match
//						score = .25
//					#else there is no middle initial
//					if only first initial matches
//					    score = .20
//		        # check to see if Forename match initials?
//		        if TempAuthorName.forename.strip() == SearchAuthorName.firstinit + " " + SearchAuthorName.midinit:
//		            both initials match, score += .25
//		        # else 		
//				    forename is probably a two letter first name like "He" or "Li" - set flag to analyze 
//
//		# Expanded if-then logic with Flag class							  
//
//		If lastname is 100% match: 
//		    If Flags.fullemailmatch = True
//		             Counts.score += 1.0   
//			if Flags.fullemailmatch <> True:
//		        if forenamematch == True:
//		            Counts.score += .50
//		        else:
//		            pass
//		        if Flags.partemailmatch == True:
//		            Counts.score += .05
//		        else:
//		            pass
//		        if Flags.domainpartemailmatch == True:
//		            Counts.score += .15
//		        else:
//		            pass
//		        if Flags.bothinitmatch == True:
//		            Counts.score += .25
//		        else:
//		            if Flags.firstinitmatch == True:
//		                Counts.score += .20
//		            else:
//		                pass
//		    else:
//		        pass
//		    #reset flag data

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
	
	
}
