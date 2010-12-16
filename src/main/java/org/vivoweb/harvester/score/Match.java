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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;
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
	 * Model for VIVO instance
	 */
	private final JenaConnect score;
	/**
	 * Model where input is stored
	 */
	private final JenaConnect scoreInput;
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
	 * @param inputRDF model containing statements to be scored
	 * @param vivo model containing vivo statements
	 * @param matchNamespace namespace to match in (null matches any)
	 * @param matchArg predicate pairs to match on
	 * @param thresholdArg pubmed threshold
	 * @param renameRes should I just rename the args?
	 * @param linkProp bidirectional link
	 * @param clearLiterals clear all the literal values out of matches
	 */
	public Match(JenaConnect scoreInput, JenaConnect scoreModel, boolean renameRes, String thresholdArg, String linkProp, boolean clearLiterals) {
		if(scoreModel == null) {
			throw new IllegalArgumentException("Score Model cannot be null");
		}
		this.score = scoreModel;
		
		if(scoreInput == null) {
			throw new IllegalArgumentException("Match Input cannot be null");
		}
		this.scoreInput = scoreInput;
		
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
			Scoring.close();
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	public Match(ArgList opts) throws IOException {
		// Connect to vivo
		this.score = JenaConnect.parseConfig(opts.get("v"), opts.getValueMap("V"));
		
		// Create working model
		this.scoreInput = JenaConnect.parseConfig(opts.get("i"), opts.getValueMap("I"));
		
		this.renameRes = opts.has("r");
		this.linkProp = opts.get("l");
		this.matchThreshold = opts.get("m");
		this.clearLiterals = opts.has("c");
	}
	
	/**
	 * Executes a weighted matching Algorithm for author disambiguation for pubmed publications
	 * Ported from INSITU code from Chris Westling cmw48@cornell.edu
	 * 	Algorithm for Pubmed Name Matching
	 *				fullEmailScore + foreNameScore + partEmailScore + domainPartEmailScore + initMatchScore
	 *	PM =    -----------------------------------------------------------------------------------------
	 *												2
	 * @return mapping of the found matches
	 */
	/*public Map<String,String> pubmedMatch() {
		double weightScore = 0;
		int loop = 0;
		double minThreshold = 0.5;
		
		String matchQuery =	"" +
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
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
		ResultSet scoreInputResult = this.scoreInput.executeSelectQuery(matchQuery);
		
		// Log info message if none found
		if (!scoreInputResult.hasNext()) {
			log.info("No authors found in harvested pubmed publications");
		} else {
			log.info("Looping thru authors from harvested pubmed publications");
		}
		
		//Create map of uris
		HashMap<String,String> uriArray = new HashMap<String,String>();
		
		// look for exact match in vivo
		while (scoreInputResult.hasNext()) {
			HashMap<String,Double> matchArray = new HashMap<String,Double>();
			//Grab results
			QuerySolution scoreSolution = scoreInputResult.next();
			log.trace(scoreSolution.toString());
			//author = scoreSolution.getResource("x");
			//paper = scoreSolution.getResource("publication");
			String foreName = scoreSolution.get("foreName").toString();
			String lastName = scoreSolution.get("lastName").toString();
			String workEmail;
			if (scoreSolution.get("workEmail") != null) {
				workEmail = scoreSolution.get("workEmail").toString();
			} else {
				workEmail = null;
			}
			
			//general string cleanup
			//remove , from names
			foreName = foreName.replace(",", " ");			
			lastName = lastName.replace(",", "");
			
			String middleName = null;
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
				}
				log.trace("Using " + lastName + ", " + foreName + ", " + middleName + " as name");
			} else {
				middleName = scoreSolution.get("middleName").toString();
				middleName = middleName.replace(",", "");
				log.trace("Using pubmed middle name " + lastName + ", " + foreName + ", " + middleName);
			}
			
			// ensure first name and last name are not blank
			if (lastName.toString() == null || foreName.toString() == null) {
				log.trace("Incomplete name, skipping");
				continue;
			}
			
			// Select all matching authors from vivo store
			String queryString = "" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + 
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
			
			ResultSet vivoResult = this.vivo.executeSelectQuery(queryString);
			
			// Run comparisions
			while (vivoResult.hasNext()) {
				String matchForeName = null;
				String matchMiddleName = null;
				String matchLastName = null;
				String matchWorkEmail = null;
				weightScore = 0;
				QuerySolution vivoSolution = vivoResult.next();
				log.trace(vivoSolution.toString());
						
				//Grab comparision strings
				if (vivoSolution.get("foreName") != null) {
					matchForeName = vivoSolution.get("foreName").toString();
				}
				if (vivoSolution.get("lastName") != null) {
					matchLastName = vivoSolution.get("lastName").toString();
				}
				if (vivoSolution.get("middleName") != null) {
					matchMiddleName = vivoSolution.get("middleName").toString();
				}
				if (vivoSolution.get("workEmail") != null) {
					matchWorkEmail = vivoSolution.get("workEmail").toString();
				}
				
				//email match weight
				if (matchWorkEmail != null && workEmail != null) {
					if (workEmail.equalsIgnoreCase(matchWorkEmail)) {
						weightScore += 1;
					}  else {
						//split email and match email parts
						if (workEmail.split("@")[0].equalsIgnoreCase(matchWorkEmail.split("@")[0]) ) {
							weightScore += .05;
						}
						if (workEmail.split("@")[1].equalsIgnoreCase(matchWorkEmail.split("@")[1]) ) {
							weightScore += .15;
						}
					}
				}
				
				//foreName match weight
				if (matchForeName != null) {
					//add score weight for each letter matched
					loop = 0;
					while (matchForeName.regionMatches(true, 0, foreName, 0, loop)) {
						loop++;
						weightScore += .1;
					}
				}
				
				//MiddleName match weight
				if (matchMiddleName != null) {
					//add score weight for each letter matched
					loop = 0;
					while (matchMiddleName.regionMatches(true, 0, middleName, 0, loop)) {
						loop++;
						weightScore += .05;
					}
				}
				
				//LastName match weight
				if (matchLastName != null) {
					//add score weight for each letter matched
					loop = 0;
					while (matchLastName.regionMatches(true, 0, lastName, 0, loop)) {
						loop++;
						weightScore += .075;
					}
				}
				
				//Store in hash
				matchArray.put(vivoSolution.get("x").toString(),new Double(weightScore));
			}
			
			String uri = scoreSolution.getResource("x").getURI();
			if(!matchArray.isEmpty()) {
				//Print all weights and select highest as match, above minimum threshold
				Entry<String, Double> highKey = matchArray.entrySet().iterator().next();
				for (Entry<String, Double> i: matchArray.entrySet()) {
			        log.trace(i.getKey() + " : " + i.getValue());
			        if (i.getValue().doubleValue() > highKey.getValue().doubleValue()) {
			        	highKey = i;
			        	log.trace("Selecting as best match " + highKey.getKey());
			        }
				}
				
				//Match author to highKey
				if (highKey.getValue().doubleValue() > minThreshold) {
					uriArray.put(uri, highKey.getKey());
					log.trace("Match found: <" + uri + "> in Score matched with <" + highKey.getKey() + "> in Vivo");
				} else {
					log.trace("No Match found for <" + uri + ">");
				}
			} else {
				log.trace("No Match found for <" + uri + ">");
			}
		}
		return uriArray;

	}*/
	
	/**
	 * Find all nodes in the given namepsace matching on the given predicates
	 * @param matchMap properties set of predicates to be searched for in vivo and score. Formats is "vivoProp => inputProp"
	 * @param namespace the namespace to match in
	 * @return mapping of the found matches
	 * @throws IOException error connecting to dataset
	 */
	private Map<String,String> match(String threshold) throws IOException{
		//Build query to find all nodes matching on the given predicates
		StringBuilder sQuery =	new StringBuilder(
				"PREFIX scoring: <http://vivoweb.org/harvester/model/scoring#>\n" +
				"SELECT DISTINCT ?sVivo ?sInput"
		);
		
		int counter = 0;
		StringBuilder vivoOptionals = new StringBuilder();
		StringBuilder inputOptionals = new StringBuilder();
		List<String> filters = new ArrayList<String>();
		List<String> vivoUnions = new ArrayList<String>();
		List<String> inputUnions = new ArrayList<String>();
		
		/*for (String property : matchMap.keySet()) {
			sQuery.append(" ?os" + counter);
//			sQuery.append(" ?ov" + counter);
			sQuery.append(" ?op" + counter);
//			sQuery.append(" ?oi" + counter);
			vivoUnions.add("{ ?sVivo <" + property + "> ?ov" + counter + " }");
			vivoOptionals.append("    OPTIONAL { ?sVivo <").append(property).append("> ").append("?op" + counter).append(" } . \n");
			inputUnions.add("{ ?sInput <" + matchMap.get(property) + "> ?oi" + counter + " }");
			inputOptionals.append("    "+"OPTIONAL { "+"?sInput <").append(matchMap.get(property)).append("> ").append("?os" + counter).append(" }"+" . \n");
			filters.add("sameTerm(?os" + counter + ", ?ov" + counter + ")");
			counter++;
		}*/
		
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

		sQuery.append(" ) .\n");
		sQuery.append("}");
		
		log.debug("Match Query:\n"+sQuery.toString());
		
		SDBJenaConnect unionModel = new MemJenaConnect();
		JenaConnect vivoClone = unionModel.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#vivoClone");
		vivoClone.loadRdfFromJC(this.score);
		JenaConnect inputClone = unionModel.neighborConnectClone("http://vivoweb.org/harvester/model/scoring#inputClone");
		inputClone.loadRdfFromJC(this.scoreInput);
		Dataset ds = unionModel.getConnectionDataSet();
		
		Query query = QueryFactory.create(sQuery.toString(), Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(query, ds);
		HashMap<String,String> uriMatchMap = new HashMap<String,String>();
		HashMap<String,Map<String,Double>> evalMap = new HashMap<String, Map<String, Double>>();
		for(QuerySolution solution : IterableAdaptor.adapt(queryExec.execSelect())) {
			String sScoreURI = solution.getResource("sInput").getURI();
			String sVivoURI = solution.getResource("sVivo").getURI();

			uriMatchMap.put(sScoreURI, sVivoURI);
			log.debug("Match found: <"+sScoreURI+"> in Match matched with <"+sVivoURI+"> in Vivo");

			if(!evalMap.containsKey(sScoreURI)) {
				evalMap.put(sScoreURI, new HashMap<String, Double>());
			}
			log.debug("Evaluating <"+sScoreURI+"> from input as match for <"+sVivoURI+"> from vivo");
			double[] weights = new double[counter];
			for(int x = 0; x < counter; x++) {
				RDFNode os = solution.get("os"+x);
				RDFNode op = solution.get("op"+x);
				log.debug("os"+x+": "+os);
				log.debug("op"+x+": "+op);
				weights[x] = 0/1;
				if(os != null && op != null) {
					// if a resource and same uris
					if(os.isResource() && op.isResource() && os.asResource().getURI().equals(op.asResource().getURI())) {
						weights[x] = 1/1;
					} else if(os.isLiteral() && op.isLiteral()) {
						String osStrValue = os.asLiteral().getValue().toString();
						String opStrValue = op.asLiteral().getValue().toString();
//						//Naive Region Match 
//						// compare string sequences
//						for(int loop = 0; loop < osStrValue.length() && opStrValue.regionMatches(true, 0, osStrValue, 0, loop); loop++) {
//							weights[x] = (loop+1)/osStrValue.length();
//						}
						// Metaphone (Improved Soundex)
						DoubleMetaphone dm = new DoubleMetaphone();
						String osDM = dm.doubleMetaphone(osStrValue);
						String opDM = dm.doubleMetaphone(opStrValue);
						// Levenshtein:
						//    ((osDM.len + opDM.len)-(Levenshtein Distance))/(osDM.len + opDM.len)
						weights[x] = ((osDM.length() + opDM.length()) - StringUtils.getLevenshteinDistance(osDM, opDM))/(osDM.length() + opDM.length());
					}
				}
			}
			double matchWeight = 0/1;
			for(double weight : weights) {
				matchWeight += weight;
			}
			matchWeight /= counter;
			if(evalMap.get(sScoreURI).containsKey(sVivoURI)) {
				log.warn("Match for <"+sScoreURI+"> to <"+sVivoURI+"> Already In Model: "+evalMap.get(sScoreURI).get(sVivoURI));
			}
			log.debug("Match for <"+sScoreURI+"> to <"+sVivoURI+">: "+matchWeight);
			evalMap.get(sScoreURI).put(sVivoURI, Double.valueOf(matchWeight));
//			uriArray.put(sScoreURI, sVivoURI);
//			log.debug("Match found: <"+sScoreURI+"> in Score matched with <"+sVivoURI+"> in Vivo");
		}

		log.info("Match found " + uriMatchMap.keySet().size() + " links between Vivo and the Input model");
		
		return uriMatchMap;
	}
	/*
	This is cmw48's logic, don't blame this on CTRIP:		
	# Scoring Logic											  
	if lastnamematch then {
	    if fullemailmatch then {
		    fullemailscore = 1.0; 
		} else {	
		    if forenamematch then {
			    forenamescore += .50
			}
		    if partemailmatch then {
			    partemailscore += .05
			}		
		    if domainpartemailmatch then {
			    domainpartemailscore += .15
			}    
			if bothinitmatch then {
			    initmatchscore += .25
			} else if firstinitmatch then {
			    initmatchscore += .20
			}
	}		

	# Max scores based on logic above

	       1 + .50 + .05 + .15 + .25
	 Pm =  ------------------------
	                  2			
				
	# Narrative logic

	if 100% last name match then
	    #look for email match
		test for zerolength affiliation string
		if email address exists in affiliation string
		    immediately write as <affiliationEmail>
			if targetEmail is nonzerolength
			    if targetEmail == affiliationEmail
				    score = 1    #full email match means 100% certainty
				else:  # look for partial email match 
				    if first three chars of both strings match  "cmw"
					    score = 
					else if domain part of email matches "cornell.edu"
	                    score = 
	            else no email match
		#check for a full initial match
	    #assume that each pub has (numauthors) authors, based on the premise that LastName is always present
	    #cycle through authors starting with author[0] to find match	
		    #since initials or forename tags may not appear in some PubMed records, test and set values				
			#look for first name match
	        #how long is our TargetAuthor firstname?			
			#look only at section of PM first name that's as long as our target search string  (target = Chris, PMFirst = Christopher, then foreName = leftstr(PMFirst, len(target)))
			    If foreName == firstName
				    score += .5
			# we know first initial matches, is there a middle initial?
	        # find out if initials string is > 1
			# if more than one character in initials, there must be a middle initial
	        # middle initial *should be* rightmost character
			# do we have a middle initial in our SearchAuthorName
	            if both initials match
					score = .25
				#else there is no middle initial
				if only first initial matches
				    score = .20
	        # check to see if Forename match initials?
	        if TempAuthorName.forename.strip() == SearchAuthorName.firstinit + " " + SearchAuthorName.midinit:
	            both initials match, score += .25
	        # else 		
			    forename is probably a two letter first name like "He" or "Li" - set flag to analyze 

	# Expanded if-then logic with Flag class							  

	If lastname is 100% match: 
	    If Flags.fullemailmatch = True
	             Counts.score += 1.0   
		if Flags.fullemailmatch <> True:
	        if forenamematch == True:
	            Counts.score += .50
	        else:
	            pass
	        if Flags.partemailmatch == True:
	            Counts.score += .05
	        else:
	            pass
	        if Flags.domainpartemailmatch == True:
	            Counts.score += .15
	        else:
	            pass
	        if Flags.bothinitmatch == True:
	            Counts.score += .25
	        else:
	            if Flags.firstinitmatch == True:
	                Counts.score += .20
	            else:
	                pass
	    else:
	        pass
	    #reset flag data
*/
	
	/**
	 * Rename the resource set as the key to the value matched
	 * @param matchSet a result set of scoreResources, vivoResources
	 */
	private void rename(Map<String,String> matchSet){
		for(String oldUri : matchSet.keySet()) {
			//check for inplace - if we're doing this inplace or we're pushing all, scoreoutput has been set/copied from scoreinput
			//get resource in output model and perform rename
			Resource res = this.scoreInput.getJenaModel().getResource(oldUri);
			ResourceUtils.renameResource(res, matchSet.get(oldUri));
		}
	}
	
	/**
	 * Link matched scoreResources to vivoResources using given linking predicates
	 * @param matchSet a mapping of matched scoreResources to vivoResources
	 * @param objPropList the object properties to be used to link the two items set in string form -> vivo-object-property-to-score-object = score-object-property-to-vivo-object
	 */
	private void link(Map<String,String> matchSet, String objPropList) {
		//split the objPropList into its vivo and score components (vivo->score=score->vivo)
		String[] objPropArray = objPropList.split("=");
		if(objPropArray.length != 2){
			throw new IllegalArgumentException("Two object properties vivo-object-property-to-score-object and score-object-property-to-vivo-object " +
					"sepearated by an equal sign must be set to link resources");
		}
		Property vivoToScoreObjProp = ResourceFactory.createProperty(objPropArray[0]);
		Property scoreToVivoObjProp = ResourceFactory.createProperty(objPropArray[1]);
		
		for(String oldUri : matchSet.keySet()) {
			//check for inplace - if we're doing this inplace or we're pushing all, scoreoutput has been set/copied from scoreinput
			Resource scoreRes = this.scoreInput.getJenaModel().getResource(oldUri);	
			Resource vivoRes = this.score.getJenaModel().getResource(matchSet.get(oldUri));
			this.scoreInput.getJenaModel().add(vivoRes, vivoToScoreObjProp, scoreRes);
			this.scoreInput.getJenaModel().add(scoreRes, scoreToVivoObjProp, vivoRes);			
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
			log.debug("Constructed Literal Set:\n"+this.scoreInput.executeConstructQuery(conQuery).toString());
			log.debug("Clear Literal Query:\n" + query);
			this.scoreInput.executeUpdateQuery(query);
		}
	}
	
	
	 /* Traverses paperNode and adds to toReplace model
	 * @param mainRes the main resource
	 * @param linkRes the resource to link it to
	 * @return the model containing the sanitized info so far
	 * TODO change linkRes to be a string builder of the URI of the resource, that way you can do a String.Contains() for the URI of a resource
	 * @throws IOException error connecting
	 */
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
	}
	
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
	 * Execute score object algorithms
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
