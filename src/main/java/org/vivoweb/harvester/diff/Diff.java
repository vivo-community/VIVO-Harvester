/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.diff;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;

/**
 * Set math to find difference (subtraction) of one model from another
 * @author Stephen Williams
 * @author Rene Ziede (rziede@ufl.edu)
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
	 * If using Diff to create a subtraction model, these types can be used to restrict updates
	 * to properties of these types, preventing unwanted deletion.
	 */
	private List<String> updateTypes;
	
	/**
	 * Are we preserving any entities on this diff?
	 */
	private boolean bHasUpdateTypes;
	
	/**
	 * Are we using selectiveDiff?
	 */
	private boolean bUsingSelectiveDiff;
	
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
		
		this.bHasUpdateTypes = false;
		
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
	 * @param bSelectiveDiff If we're using selective diff.
	 * @param updateTypes The types this diff is allowed to affect.
	 */
	public Diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, Map<String,String> dF, Map<String,String> dL, String dTF, 
				String n3, boolean bSelectiveDiff, List<String> updateTypes) {
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

		this.updateTypes = updateTypes;
		this.bUsingSelectiveDiff = bSelectiveDiff;
		this.bHasUpdateTypes = (!this.updateTypes.isEmpty());
		// Note: If the user specifies updateTypes but forgot to set the bUsingSelectiveDiff flag, they probably wanted to use s.Diff.
		if( this.bHasUpdateTypes ) this.bUsingSelectiveDiff = true;
		
		
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
			argList.has("e"),
			argList.getAll("U"));
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
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("selective-diff").setDescription("Use selective diff").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('U').setLongOpt("update-types").withParameterValueMap("NAME", "TYPE").setDescription("Type to be updated").setRequired(false));
		
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
		// minuend.diff(subtrahend) = difference
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
	 * Pulled into a separate method to force a change in scope for dropping heavy-weight temporary variables.
	 * selecticeDiff Optimization.
	 */
	private void prepareDiffModels()
	{
		// Use Jena to construct a subtractionModel from oldModel - newModel.
		Model subtractionModel = ModelFactory.createDefaultModel();
		Model minuendModel = this.minuendJC.getJenaModel();
		Model subtrahendModel = this.subtrahendJC.getJenaModel();
		subtractionModel = minuendModel.difference(subtrahendModel);
				
		// Load subtractionModel into a temporary MemJena.
		this.diffModel = new MemJenaConnect("subtractionJC");
		this.diffModel.getJenaModel().add(subtractionModel);
		
	}
	
	/**
	 * A selectiveDiff preserves certain types from the previous model by removing them from the subtraction model.
	 * Non-static, unlike regular 'diff'. 
	 * @throws IOException JC
	 */
	public void selectiveDiff() throws IOException {

		prepareDiffModels();
		//JenaConnect appendModel = new MemJenaConnect("appendJC");
	
		//JVM 'suggestion' to garbage collect at its next reasonable opportunity.
		//TODO: May have no benefit, need to test.
		System.gc();
		
		//Load newModel and subtractionModel JCs into a joined model for multi-graph query.
		unionModels();

		System.gc();
		
		//JenaConnect newSubtractionJC = new MemJenaConnect("newSubJC");
		//Model newSubtractionModel = ModelFactory.createDefaultModel();
		
		//Unload diff model and re-use as new-diff model to save memory.
		this.diffModel.truncate();
		
		if( this.bHasUpdateTypes )
		{
			for(String objType : this.updateTypes)
			{
				String preservationQuery = buildPreservationQuery(objType);
				//log.trace(preservationQuery);
				
				//Construct triples we wish to keep from the subtraction graph copy in tempModel and append.
				//appendModel.loadRdfFromJC(this.tempModel.executeConstructQuery(preservationQuery, true));
				//traceModel("An appendModel: ", appendModel);
				
				//Construct triples we wish to keep from the subtraction graph copy in tempModel and append.
				this.diffModel.loadRdfFromJC(this.tempModel.executeConstructQuery(preservationQuery, true));
			}
			
			traceModel( "New SubtractionModel", this.diffModel );
		}
		else
		{
			String preservationQuery = buildPreservationQuery();
			//appendModel = this.tempModel.executeConstructQuery(preservationQuery, true);
			//traceModel("An appendModel: ", appendModel);
			
			//Construct triples we wish to keep from the subtraction graph copy in tempModel and append.
			this.diffModel.loadRdfFromJC(this.tempModel.executeConstructQuery(preservationQuery, true));
		}
		
		
		// Dump subtractionModel to RDF/XML file.
		if (this.dumpFile != null) {
			Model newSubtractionModel = this.diffModel.getJenaModel();
			
			for(String filename : this.dumpFile.keySet()) {
				String filepath = this.dumpFile.get(filename);
				String filelanguage = "";
				if (this.dumpLanguage.containsKey(filename)){
					filelanguage = this.dumpLanguage.get(filename);
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
			this.outputJC.getJenaModel().add(this.diffModel.getJenaModel());
			this.outputJC.sync();
		}
	}
	
	/**
	 * @param objectType The type of object to preserve
	 * @return The query string to be executed.
	 */
	public static String buildPreservationQuery(String objectType)
	{
		//DEBUG
		log.trace("objectType in Query Builder: " + objectType);
		
		// Preservation Query Builder
		StringBuilder pQBuilder = new StringBuilder();
		
		pQBuilder.append("PREFIX diff: <http://vivoweb.org/harvester/model/diff#>\n");
		pQBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
		pQBuilder.append("CONSTRUCT {\n");
		pQBuilder.append("	?s ?p ?o .\n");
		pQBuilder.append("}\n");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#newModel>\n");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#subtractionModel>\n");
		pQBuilder.append("WHERE {\n");
		pQBuilder.append("	GRAPH diff:newModel {\n");
		// Changed order for potential faster culling.
		pQBuilder.append("		?newSub rdf:type <" + objectType + "> .\n");
		pQBuilder.append("		?newSub ?newPred ?newObj .\n");
		pQBuilder.append("	} .\n");
		pQBuilder.append("	GRAPH diff:subtractionModel {\n");
		pQBuilder.append("		?s ?p ?o .\n");
//		pQBuilder.append("		FILTER(str(?o2) = str(" + objectType + ")) .\n");
		pQBuilder.append("	} .\n");
//		pQBuilder.append("	FILTER( ((str(?s) = str(?newSub)) || (str(?o) = str(?newSub))) && (str(?o2) = str( \"" + objectType + "\"))) .\n");
		// Changed order for potential faster culling.
		pQBuilder.append("  FILTER( (str(?s) = str(?newSub)) || (str(?o) = str(?newSub)) ) .\n");
//		pQBuilder.append("		FILTER(str(?o2) != str(" + objectType + ")) .\n");
		pQBuilder.append("}");
		
		return pQBuilder.toString();
	}
	
	/**
	 * This version is called when bUsingSelectiveDiff is true but no types are specified.
	 * @return The query string to be executed.
	 */
	public static String buildPreservationQuery()
	{
		// Preservation Query Builder
		StringBuilder pQBuilder = new StringBuilder();
		
		pQBuilder.append("PREFIX diff: <http://vivoweb.org/harvester/model/diff#>\n");
		pQBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
		pQBuilder.append("CONSTRUCT {\n");
		pQBuilder.append("	?s ?p ?o .\n");
		pQBuilder.append("}\n");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#newModel>\n");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#subtractionModel>\n");
		pQBuilder.append("WHERE {\n");
		pQBuilder.append("	GRAPH diff:newModel {\n");
		pQBuilder.append("		?newSub ?newPred ?newObj .\n");
		pQBuilder.append("	} .\n");
		pQBuilder.append("	GRAPH diff:subtractionModel {\n");
		pQBuilder.append("		?s ?p ?o .\n");
		pQBuilder.append("	} .\n");
		pQBuilder.append("  FILTER((str(?s) = str(?newSub)) || (str(?o) = str(?newSub))) .\n");
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
		if(this.bUsingSelectiveDiff)
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
		
		JenaConnect subtractionClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/diff#subtractionModel");
		subtractionClone.loadRdfFromJC(this.diffModel);
		JenaConnect newModelClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/diff#newModel");
		newModelClone.loadRdfFromJC(this.subtrahendJC);
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
