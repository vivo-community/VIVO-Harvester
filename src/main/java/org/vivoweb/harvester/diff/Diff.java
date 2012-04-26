/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.diff;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * Set math to find difference (subtraction) of one model from another
 * @author Stephen Williams
 */
public class Diff {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Diff.class);
	/**
	 * Models to read records from
	 */
	private JenaConnect minuendJC;
	/**
	 * Models to read records from
	 */
	private JenaConnect subtrahendJC;
	/**
	 * Model to write records to
	 */
	private JenaConnect outputJC;
	
	/**
	 * Temporary model containing two graphs, used for entity-preserving Diff behavior.
	 */
	private JenaConnect tempModel;
	
	/**
	 * Model for the diff.
	 */
	private JenaConnect diffModel;
	
	/**
	 * If using Diff to create a subtraction model, these predicates can be used to exempt entities from the
	 * resulting model, preventing unwanted deletion.
	 */
	private List<String> preservationPredicates;
	
	/**
	 * Are we preserving any entities on this diff?
	 */
	private boolean bHasPreservationPredicates;
	
	/**
	 * dump model to file option - filename
	 */
	private Map<String, String> dumpFile;
	/**
	 * dump model to file option - language
	 */
	private Map<String, String> dumpLanguage;
	/**
	 * dump model to a ntriple file
	 */
	private String dumpNTriple;
	/**
	 * dump model to a n3 file
	 */
	private String dumpN3;
	
	/**
	 * Constructor
	 * @param mJC minuend jenaconnect
	 * @param sJC subtrahend jenaconnect
	 * @param oJC output jenaconnect
	 * @param dF dump file path
	 * @param dL dump language
	 * @param dTF dump ntriple
	 * @param n3 dump n3 
	 */
	public Diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, Map<String,String> dF, Map<String,String> dL, String dTF, String n3) {
		this.minuendJC = mJC;
		this.subtrahendJC = sJC;
		this.outputJC = oJC;
		this.dumpFile = dF;
		this.dumpLanguage = dL;
		this.dumpNTriple = dTF;
		this.dumpN3=n3;
		if(this.minuendJC == null) {
			throw new IllegalArgumentException("Must provide a minuend jena model");
		}
		if(this.subtrahendJC == null) {
			throw new IllegalArgumentException("Must provide a subtrahend jena model");
		}
		if(this.outputJC == null && (this.dumpFile == null )) { // TODO: check the contents of the dumpFiles if any is empty error || this.dumpFile.trim().isEmpty())) 
			throw new IllegalArgumentException("Must provide at least one of an output jena model or a dump file");
		}
		
		this.bHasPreservationPredicates = false;
		
		checkFileName();
	}
	
	/**
	 * Constructor
	 * @param mJC The minuend Jena Connect
	 * @param sJC The subtrahend Jena Connect
	 * @param oJC The output Jena Connect
	 * @param dF  The dump-file path.
	 * @param dL  The dump-file language.
	 * @param dTF The dump N Triple
	 * @param n3  The dump N3
	 * @param presPreds The types to preserve in selective diff.
	 */
	public Diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, Map<String,String> dF, Map<String,String> dL, String dTF, 
				String n3, List<String> presPreds) {
		this.minuendJC = mJC;
		this.subtrahendJC = sJC;
		this.outputJC = oJC;
		this.dumpFile = dF;
		this.dumpLanguage = dL;
		this.dumpNTriple = dTF;
		this.dumpN3=n3;
		if(this.minuendJC == null) {
			throw new IllegalArgumentException("Must provide a minuend jena model");
		}
		if(this.subtrahendJC == null) {
			throw new IllegalArgumentException("Must provide a subtrahend jena model");
		}
		if(this.outputJC == null && (this.dumpFile == null )) { // TODO: check the contents of the dumpFiles if any is empty error || this.dumpFile.trim().isEmpty())) 
			throw new IllegalArgumentException("Must provide at least one of an output jena model or a dump file");
		}

		this.preservationPredicates = presPreds;
		this.bHasPreservationPredicates = (!this.preservationPredicates.isEmpty());
		
		checkFileName();
	}
	
	
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error reading config files
	 * @throws UsageException user requested usage message
	 */
	private Diff(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed commandline arguments
	 * @throws IOException error reading config files
	 */
	private Diff(ArgList argList) throws IOException {
		this(
			JenaConnect.parseConfig(argList.get("m"), argList.getValueMap("M")),
			JenaConnect.parseConfig(argList.get("s"), argList.getValueMap("S")),
			JenaConnect.parseConfig(argList.get("o"), argList.getValueMap("O")),
			
			argList.getValueMap("d"),
			argList.getValueMap("l"),
			argList.get("t"),
			argList.get("n"),
			argList.getAll("P"));
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Diff");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("minuend").withParameter(true, "CONFIG_FILE").setDescription("config file for source jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('M').setLongOpt("minuendOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of source jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("subtrahend").withParameter(true, "CONFIG_FILE").setDescription("config file for removemode jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("subtrahendOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of remove jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("preserved-predicates").withParameterValueMap("NAME", "PREDICATE").setDescription(" ").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("dumptolanguage").withParameterValueMap("FILE_NAME", "LANGUAGE").setDescription("language for output").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameterValueMap("FILE_NAME", "FILENAME").setDescription("filename for output").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("dumpntripletofile").withParameter(true, "FILENAME").setDescription("filename for N triple output").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("dumpn3tofile").withParameter(true, "FILENAME").setDescription("filename for N 3 output").setRequired(false));
		return parser;
	}
	
	/**
	 * Make sure that every dumpLanguage Key,Value pair has an identical key in
	 * the dumpFile map.
	 * @return - false if 1 language definition does not have a path to a file defined
	 */
	private boolean checkFileName(){
		
		if( this.dumpLanguage == null)
		{
			return false;
		}
		
		boolean valid = true;
		for(String fileName : this.dumpLanguage.keySet()){
			if (!this.dumpFile.containsKey(fileName)){
				valid = false;
				throw new IllegalArgumentException("file name '" + fileName + "' found in languages but not in paths");
			}
		}
		return valid;
	}
	
	/**
	 * Perform diff of mJC and sJC and put result in oJC and/or dF
	 * @param mJC minuend jenaconnect
	 * @param sJC subtrahend jenaconnect
	 * @param oJC output jenaconnect
	 * @param dF dump file path
	 * @param dL dump language
	 * @param dTF dumpfile ntriple
	 * @param dNTF dumpfile n3
	 * @throws IOException error accessing file
	 */
	@SuppressWarnings("unused")
	public static void diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, Map<String,String> dF, Map<String,String> dL, String dTF, String dNTF) throws IOException {
		// c - b = a
		// minuend - subtrahend = difference
		// minuend.diff(subtrahend) = differenece
		// c.diff(b) = a
		
		Model diffModel = ModelFactory.createDefaultModel();
		Model minuendModel = mJC.getJenaModel();
		Model subtrahendModel = sJC.getJenaModel();
		
		diffModel = minuendModel.difference(subtrahendModel);

		if (dF != null) {
			for(String filename : dF.keySet()) {
				String filepath = dF.get(filename);
				String filelanguage = "";
				if (dL.containsKey(filename)){
					filelanguage = dL.get(filename);
				} else {
					filelanguage = "RDF/XML";
				}
				
				RDFWriter fasterWriter = diffModel.getWriter(filelanguage);
				if (filelanguage.equals("RDF/XML")){
					fasterWriter.setProperty("showXmlDeclaration", "true");
					fasterWriter.setProperty("allowBadURIs", "true");
					fasterWriter.setProperty("relativeURIs", "");
				}
				OutputStreamWriter osw = new OutputStreamWriter(FileAide.getOutputStream(filepath), Charset.availableCharsets().get("UTF-8"));
				fasterWriter.write(diffModel, osw, "");
				log.debug(filelanguage + " Data was exported to " + filepath);	
			}
		}

		if(oJC != null) {
			oJC.getJenaModel().add(diffModel);
			oJC.sync();
		}
	}
	
	/**
	 * A selectiveDiff preserves certain types from the previous model by removing them from the subtraction model.
	 * Non-static, unlike regular 'diff'. 
	 * @throws IOException JC
	 */
	public void selectiveDiff() throws IOException {
		
		// Use Jena to construct a subtractionModel from oldModel - newModel.
//		Model subtractionModel = ModelFactory.createDefaultModel();
//		Model minuendModel = mJC.getJenaModel();
//		Model subtrahendModel = sJC.getJenaModel();
//		subtractionModel = minuendModel.difference(subtrahendModel);
		
		Model subtractionModel = ModelFactory.createDefaultModel();
		Model minuendModel = this.minuendJC.getJenaModel();
		Model subtrahendModel = this.subtrahendJC.getJenaModel();
		subtractionModel = minuendModel.difference(subtrahendModel);
		
		// Load subtractionModel into a temporary MemJena.
		//JenaConnect subtractionJC = new MemJenaConnect("subtractionJC");
		this.diffModel = new MemJenaConnect("subtractionJC");
		
		JenaConnect newSubtractionJC = new MemJenaConnect("newSubJC");
		this.diffModel.getJenaModel().add(subtractionModel);
		Model newSubtractionModel;
		JenaConnect appendModel;

		// Trace preconditions.
		traceModel( "OldModel (minuend)", this.minuendJC);
		traceModel( "NewModel (subtrahend)", this.subtrahendJC);
		traceModel( "Subtraction Model", this.diffModel);
	
		//Load newModel and subtractionModel JCs into a joined model for multi-graph query.
		//unionModels(subtractionJC, sJC, tempModel, "subtractionModel", "newModel");
		unionModels();
		traceModel("tempModel outside union: ", this.tempModel);		

		//DEBUG ------------------------------------------------------------------
			log.debug("S-P-O Query on subtractionModel half of unioned tempModel: ");
			String testQuerySubtraction =
				"PREFIX diff: <http://vivoweb.org/harvester/model/diff#>\n" +
				"SELECT ?s ?p ?o\n" +
				"FROM NAMED  <http://vivoweb.org/harvester/model/diff#subtractionModel>\n" +
				"WHERE { GRAPH diff:subtractionModel {\n" +
				" ?s ?p ?o .} }";
			ResultSet testSet = this.tempModel.executeSelectQuery(testQuerySubtraction);
			for(QuerySolution soln : IterableAdaptor.adapt(testSet)){
				log.trace(soln.toString());
			}
		//DEBUG ------------------------------------------------------------------
			
		//DEBUG ------------------------------------------------------------------
			log.debug("S-P-O Query on newModel half of unioned tempModel: ");
			String testQueryNewModel =
				"PREFIX diff: <http://vivoweb.org/harvester/model/diff#>\n" +
				"SELECT ?s ?p ?o\n" +
				"FROM NAMED  <http://vivoweb.org/harvester/model/diff#newModel>\n" +
				"WHERE { GRAPH diff:newModel {\n" +
				" ?s ?p ?o .} }";
			ResultSet testSet2 = this.tempModel.executeSelectQuery(testQueryNewModel);
			for(QuerySolution soln : IterableAdaptor.adapt(testSet2)){
				log.trace(soln.toString());
			}
		//DEBUG ------------------------------------------------------------------
		
		log.debug("Preserving? " + this.bHasPreservationPredicates);
		log.debug("How many types? " + this.preservationPredicates.size());
		
		for(String objType : this.preservationPredicates)
		{
			String preservationQuery = buildPreservationQuery(objType);
			
			//Construct triples we wish to keep from the subtraction graph copy in tempModel and append.
			appendModel = this.tempModel.executeConstructQuery(preservationQuery, true);
			
			traceModel("An appendModel: ", appendModel);
			
			newSubtractionJC.loadRdfFromJC(appendModel);
		}
		
		//String transferBackToSubtraction = getTransferQuery();
		//newSubtractionJC = tempModel.executeConstructQuery(transferBackToSubtraction);

		traceModel( "New SubtractionModel", newSubtractionJC );
		
		// Dump subtractionModel to RDF/XML file.
		if (this.dumpFile != null) {
			newSubtractionModel = newSubtractionJC.getJenaModel();
			
			for(String filename : this.dumpFile.keySet()) {
				String filepath = this.dumpFile.get(filename);
				String filelanguage = "";
				if (this.dumpFile.containsKey(filename)){
					filelanguage = this.dumpFile.get(filename);
				} else {
					filelanguage = "RDF/XML";
				}
				
				RDFWriter fasterWriter = newSubtractionModel.getWriter(filelanguage);
				if (filelanguage.equals("RDF/XML")){
					fasterWriter.setProperty("showXmlDeclaration", "true");
					fasterWriter.setProperty("allowBadURIs", "true");
					fasterWriter.setProperty("relativeURIs", "");
				}
				OutputStreamWriter osw = new OutputStreamWriter(FileAide.getOutputStream(filepath), Charset.availableCharsets().get("UTF-8"));
				fasterWriter.write(newSubtractionModel, osw, "");
				log.debug(filelanguage + " Data was exported to " + filepath);	
			}
		}

		// Load subtractionModel into outputModel and update.
		if(this.outputJC != null) {
			this.outputJC.getJenaModel().add(newSubtractionJC.getJenaModel());
			this.outputJC.sync();
		}
		
	}
	
	/**
	 * @param objectType The type of object to preserve
	 * @return The query string to be executed.
	 */
	public static String buildPreservationQuery(String objectType)
	{
		// Preservation Query Builder
		StringBuilder pQBuilder = new StringBuilder();
		
		pQBuilder.append("PREFIX diff: <http://vivoweb.org/harvester/model/diff#>\n");
//		pQBuilder.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n");
		pQBuilder.append("CONSTRUCT {\n");
		pQBuilder.append("	?s ?p ?o .\n");
//		pQBuilder.append("	?newSub ?newPred ?newObj .\n");
		pQBuilder.append("}\n");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#newModel>\n");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#subtractionModel>\n");
		pQBuilder.append("WHERE {\n");
		pQBuilder.append("	GRAPH diff:newModel {\n");
//		pQBuilder.append("  	?s ?p ?o .\n");
		pQBuilder.append("		?newSub a " + objectType + ".\n");
		pQBuilder.append("		?newSub ?newPred ?newObj .\n");
//		pQBuilder.append("		?newSub a foaf:Person");
		pQBuilder.append("	} .\n");
		pQBuilder.append("	GRAPH diff:subtractionModel {\n");
		pQBuilder.append("		?s ?p ?o .\n");
//		pQBuilder.append("		FILTER(?s != ?newSub) .\n");
//		pQBuilder.append("		FILTER(?o != ?newSub) .\n");
		pQBuilder.append("	} .\n");
		pQBuilder.append("}");
		
		return pQBuilder.toString();
	}
	
	/**
	 * Traces the contents of an entire model for testing/logging.
	 * @param traceLabel Preceding label.
	 * @param model The model to trace.
	 * @throws IOException Jena query.
	 */
	private static void traceModel(String traceLabel, JenaConnect model) throws IOException
	{
		log.trace(traceLabel);
		
		ResultSet traceSet = model.executeSelectQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o .}");
		for(QuerySolution soln : IterableAdaptor.adapt(traceSet)){
			log.trace(soln.toString());
		}
	}
	
	/**
	 * Execute the diff
	 * @throws IOException error accessing file
	 */
	public void execute() throws IOException {
		if(this.bHasPreservationPredicates)
		{
			selectiveDiff();
		}
		else
			diff(this.minuendJC, this.subtrahendJC, this.outputJC, this.dumpFile, this.dumpLanguage, this.dumpNTriple, this.dumpN3);
	}
	
	/**
	 * @throws IOException JenaConnect
	 */
//	private void unionModels(JenaConnect modelA, JenaConnect modelB, JenaConnect unionModel,
//									String labelA, String labelB) throws IOException
	private void unionModels() throws IOException
	{
		this.tempModel = new MemJenaConnect("urn:x-arq:UnionGraph");
//		JenaConnect vivoClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/imagepres#vivoClone");
//		vivoClone.loadRdfFromJC(this.vivoModel);
//		JenaConnect inputClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/imagepres#inputClone");
//		inputClone.loadRdfFromJC(this.incomingModel);
		
		JenaConnect subtractionClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/diff#subtractionModel");
		subtractionClone.loadRdfFromJC(this.diffModel);
		JenaConnect newModelClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/diff#newModel");
		newModelClone.loadRdfFromJC(this.subtrahendJC);
		
		//DEBUG
		//traceModel("modelAClone: ", subtractionClone);
		//traceModel("modelBClone: ", newModelClone);
		//traceModel("tempModel after Union: ", this.tempModel);
		
		//TESTING
//		ResultSet test = this.tempModel.executeSelectQuery( 
//			"PREFIX diff: <http://vivoweb.org/harvester/model/diff#>" 				+'\n'+
//			"SELECT DISTINCT ?s ?p ?o"												+'\n'+
//			"FROM NAMED <http://vivoweb.org/harvester/model/diff#subtractionModel>"	+'\n'+
//			"WHERE {"																+'\n'+
//			"	GRAPH diff:subtractionModel {"										+'\n'+
//			"		?s ?p ?o ."														+'\n'+
//			"	} ."																+'\n'+
//			"}" );
//		for(QuerySolution soln : IterableAdaptor.adapt(test)){
//			log.trace(soln.toString());
//		}
		
		// Check names in tempModel data set.
		Iterator<String> dSetNames = this.tempModel.getDataset().listNames();
		log.trace("tempModel DataSet names: "); 
		for (String s : IterableAdaptor.adapt(dSetNames))
		{
			log.trace("\t \t" + s);
		}
		
		// Check for triples in tempModel.
		if(!this.tempModel.executeAskQuery("ASK { ?s ?p ?o }")) 
		{
			log.debug("Empty Dataset: Temp");
		}
	}
	
	/**
	 * Main Method
	 * @param args command-line arguments
	 */
	public static void main(String... args) {
		Exception error = null;
			try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new Diff(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
