/**
 * 
 */
package org.vivoweb.harvester.score;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

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
	@SuppressWarnings("unused")
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
		ArgParser parser = new ArgParser("PubmedScore");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").setDescription("inputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivo-config").setDescription("vivoConfig JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(true));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output-config").setDescription("outputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE"));
		
		// Model name overrides
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivo jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		// options
		parser.addArgument(new ArgDef().setShortOption('w').setLongOpt("wipe-input-model").setDescription("If set, this will clear the input model after scoring is complete"));
		parser.addArgument(new ArgDef().setShortOption('q').setLongOpt("wipe-output-model").setDescription("If set, this will clear the output model before scoring begins"));
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("push-all").setDescription("If set, this will push all matches and non matches to output model"));
		
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
	public PubmedScore(JenaConnect jenaScoreInput, JenaConnect jenaVivo, JenaConnect jenaScoreOutput, boolean clearInputModelArg, boolean clearOutputModelArg, boolean pushAllArg) {
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
	 * @param args commandline arguments
	 * @throws IOException error parsing options
	 * @throws IllegalArgumentException arguments invalid
	 */
	public PubmedScore(String... args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param opts argument list
	 * @throws IOException error parsing options
	 */
	public PubmedScore(ArgList opts) throws IOException {
		// Get optional inputs / set defaults
		// Check for config files, before parsing name options
		String jenaVIVO = opts.get("v");
		
		Map<String,String> inputOverrides = opts.getValueMap("I");
		String jenaInput;
		if(opts.has("i")) {
			jenaInput = opts.get("i");
		} else {
			jenaInput = jenaVIVO;
			if(!inputOverrides.containsKey("modelName")) {
				inputOverrides.put("modelName", "Scoring");
			}
		}
		
		Map<String,String> outputOverrides = opts.getValueMap("O");
		String jenaOutput;
		if(opts.has("o")) {
			jenaOutput = opts.get("o");
		} else {
			jenaOutput = jenaVIVO;
			if(!outputOverrides.containsKey("modelName")) {
				outputOverrides.put("modelName", "Staging");
			}
		}
		
		// Connect to vivo
		this.vivo = JenaConnect.parseConfig(jenaVIVO, opts.getValueMap("V"));
		
		// Connect to input
		this.scoreInput = JenaConnect.parseConfig(jenaInput, inputOverrides);
		
		// Create to output
		this.scoreOutput = JenaConnect.parseConfig(jenaOutput, outputOverrides);
		
		this.wipeInputModel = opts.has("w");
		this.wipeOutputModel = opts.has("q");
		this.pushAll = opts.has("l");
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
		QuerySolution vivoSolution;
		ResultSet scoreInputResult;
		String foreName;
		String middleName;
		String lastName;
		String workEmail;
		HashMap<String,Double> matchArray = new HashMap<String,Double>();
		String matchForeName;
		String matchMiddleName;
		String matchLastName;
		String matchWorkEmail;
		double weightScore = 0;
		int loop = 0;
		double minThreshold = 0.5;
		
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
			//author = scoreSolution.getResource("x");
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
				log.trace("Using pubmed middle name " + lastName + ", " + foreName + ", " + middleName);
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
				weightScore = 0;
				vivoSolution = vivoResult.next();
				log.trace(vivoSolution.toString());
								
				//Grab comparision strings
				if (vivoSolution.get("foreName") != null) {
					matchForeName = vivoSolution.get("foreName").toString();
				} else {
					matchForeName = null;
				}
				if (vivoSolution.get("lastName") != null) {
					matchLastName = vivoSolution.get("lastName").toString();
				} else {
					matchLastName = null;
				}
				if (vivoSolution.get("middleName") != null) {
					matchMiddleName = vivoSolution.get("middleName").toString();
				} else {
					matchMiddleName = null;
				}
				if (vivoSolution.get("workEmail") != null) {
					matchWorkEmail = vivoSolution.get("workEmail").toString();
				} else {
					matchWorkEmail = null;
				}
				
				//email match weight
				if (matchWorkEmail != null && workEmail != null) {
					if (workEmail == matchWorkEmail) {
						weightScore += 1;
					}  else {
						//split email and match email parts
						if (workEmail.split("@")[0] == matchWorkEmail.split("@")[0] ) {
							weightScore += .05;
						}
						if (workEmail.split("@")[1] == matchWorkEmail.split("@")[1] ) {
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
				log.trace("Matched to " + highKey.getKey());
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
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}
	
	
}
