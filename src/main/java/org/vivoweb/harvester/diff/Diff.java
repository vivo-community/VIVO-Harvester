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
	private JenaConnect output;
	
	/**
	 * Temporary model containing two graphs, used for entity-preserving Diff behavior.
	 */
	private JenaConnect tempModel;
	
	/**
	 * If using Diff to create a subtraction model, these predicates can be used to exempt entities from the
	 * resulting model, preventing unwanted deletion.
	 */
	private Map<String, String> preservePredicates;
	
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
		this.output = oJC;
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
		if(this.output == null && (this.dumpFile == null )) { // TODO: check the contents of the dumpFiles if any is empty error || this.dumpFile.trim().isEmpty())) 
			throw new IllegalArgumentException("Must provide at least one of an output jena model or a dump file");
		}
		
		this.bHasPreservationPredicates = false;
		
		checkFileName();
	}
	
	/**
	 * Constructor
	 * @param mJC
	 * @param sJC
	 * @param oJC
	 * @param dF
	 * @param dL
	 * @param dTF
	 * @param n3
	 * @param presPreds
	 */
	public Diff(JenaConnect mJC, JenaConnect sJC, JenaConnect oJC, Map<String,String> dF, Map<String,String> dL, String dTF, 
				String n3, Map<String, String> presPreds) {
		this.minuendJC = mJC;
		this.subtrahendJC = sJC;
		this.output = oJC;
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
		if(this.output == null && (this.dumpFile == null )) { // TODO: check the contents of the dumpFiles if any is empty error || this.dumpFile.trim().isEmpty())) 
			throw new IllegalArgumentException("Must provide at least one of an output jena model or a dump file");
		}
		if(presPreds == null) {
			throw new IllegalArgumentException("Vivo Predicate cannot be null");
		}
		this.preservePredicates = presPreds;
		this.bHasPreservationPredicates = (!this.preservePredicates.isEmpty());
		
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
			argList.getValueMap("P"));
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
	 * @param mJC
	 * @param sJC
	 * @param oJC
	 * @param dF
	 * @param dL
	 * @param dTF
	 * @param dNTF
	 * @param preservePredicates
	 * @throws IOException
	 */
	public void preservingDiff(JenaConnect oldJC, JenaConnect newJC, JenaConnect outJC, Map<String,String> dF, Map<String,String> dL, 
									  String dTF, String dNTF, Map<String, String> preservePredicates) throws IOException {
		
		Model subtractionModel = ModelFactory.createDefaultModel();
		Model oldModel = oldJC.getJenaModel();
		Model newModel = newJC.getJenaModel();
		
		subtractionModel = oldModel.difference(newModel);
		JenaConnect subtractionJC = new MemJenaConnect("subtractionJC");
		subtractionJC.getJenaModel().add(subtractionModel);
		
		String labelA = "subtractionModel";
		String labelB = "newModel";
		
		//Load subtraction model and
		unionModels(subtractionJC, newJC, labelA, labelB);
		
		// Preservation Query Builder
		StringBuilder pQBuilder = new StringBuilder();
		
//		"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
//		"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" 							+'\n'+
//		"PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>" 	+'\n'+
//		"SELECT DISTINCT ?ufid ?vivoPriv ?incomingPriv"							+'\n'+
//		"FROM NAMED <http://vivoweb.org/harvester/model/imagepres#vivoClone>"	+'\n'+
//		"FROM NAMED <http://vivoweb.org/harvester/model/imagepres#inputClone>"	+'\n'+
//		"WHERE {"																+'\n'+
//		"	GRAPH imagepres:vivoClone {"										+'\n'+
//		"		?s ufvivo:ufid ?ufid ."											+'\n'+
//		"		OPTIONAL {?s ufvivo:privacy ?vivoPriv .}"						+'\n'+
//		//"		?s ufvivo:privacy ?vivoPriv ."									+'\n'+
//		"	} ."																+'\n'+
//		"	GRAPH imagepres:inputClone {"										+'\n'+
//		"		?s ufvivo:ufid ?ufid ."											+'\n'+
//		"		OPTIONAL {?s ufvivo:privacy ?incomingPriv .}"					+'\n'+
//		"	} ."																+'\n'+
//		//"	FILTER ( str(?vivoPriv) != str(?incomingPriv) ) ."					+'\n'+
//		"}";
		
		pQBuilder.append("PREFIX diff: <http://vivoweb.org/harvester/model/diff#>");
		pQBuilder.append("DELETE DATA FROM <http://vivoweb.org/harvester/model/diff#subtractionModel> {");
		pQBuilder.append("	?s ?p ?o .");
		pQBuilder.append("}");
		pQBuilder.append("FROM NAMED <http://vivoweb.org/harvester/model/diff#newModel>");
		pQBuilder.append("WHERE {");
		pQBuilder.append("	GRAPH diff:newModel {");
		pQBuilder.append("		?newSub ?newPred ?newObj .");
		pQBuilder.append("		?newSub a [PROGRAM] . OR");
//				...
//				...
//				...
		pQBuilder.append("	} .");
		pQBuilder.append("	GRAPH diff:subtractionModel {");
		pQBuilder.append("		?s ?p ?o .");
		pQBuilder.append("		FILTER(?s != ?newSub) .");
		pQBuilder.append("		FILTER(?o != ?newSub) .");
		pQBuilder.append("	} .");
		pQBuilder.append("}");
	
	}
	
	/**
	 * Execute the diff
	 * @throws IOException error accessing file
	 */
	public void execute() throws IOException {
		if(this.bHasPreservationPredicates)
		{
			preservingDiff(this.minuendJC, this.subtrahendJC, this.output, this.dumpFile, this.dumpLanguage, this.dumpNTriple, this.dumpN3, this.preservePredicates);
		}
		else
			diff(this.minuendJC, this.subtrahendJC, this.output, this.dumpFile, this.dumpLanguage, this.dumpNTriple, this.dumpN3);
	}
	
	/**
	 * @throws IOException
	 */
	private void unionModels(JenaConnect modelA, JenaConnect modelB, String labelA, String labelB) throws IOException
	{
		this.tempModel = new MemJenaConnect("tempModel");
		JenaConnect modelAClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/diff#" + labelA);
		modelAClone.loadRdfFromJC(modelA);
		JenaConnect modelBClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/diff#" + labelB);
		modelBClone.loadRdfFromJC(modelB);
		
		// Check names in tempModel dataset.
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
	 * @param args commandline arguments
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
