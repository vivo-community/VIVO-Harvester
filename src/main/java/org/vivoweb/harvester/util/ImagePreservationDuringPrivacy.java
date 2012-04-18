/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.util.*;

/**
 * @author drspeedo
 * @author Rene Ziede (rziede@ufl.edu)
 */
public class ImagePreservationDuringPrivacy 
{
	/**
	 * The log object for logging errors, information, and debugging.
	 */
	private static Logger log = LoggerFactory.getLogger(ImagePreservationDuringPrivacy.class);
	/**
	 * Incoming JenaModel, contains changes to vivoModel.
	 */
	protected JenaConnect incomingModel;
	/**	
	 * Private JenaModel, contains privatized image data and records.
	 */
	protected JenaConnect privateModel;
	/**
	 * Established Vivo JenaModel.
	 */
	protected JenaConnect vivoModel;
	/**
	 * Temporary multi-graph JenaModel for querying.
	 */
	private JenaConnect tempModel;
	/**
	 * Should we save image node to private model or restore it from the private model?
	 */
	protected boolean bTransferImg;
	/**
	 * Should we delete image node from Vivo or private models?
	 */
	protected boolean bDeleteImg;

	
	/**
	 * Constructor
	 * @param inModel Incoming model.
	 * @param privModel Private model.
	 * @param vivoModel Vivo model.
	 * @param bTransferImg If we transfer img data between models.
	 * @param bDeleteImg If we delete img data from models.
	 */
	public ImagePreservationDuringPrivacy(JenaConnect inModel, JenaConnect privModel, JenaConnect vivoModel, 
											boolean bTransferImg, boolean bDeleteImg )
	{
		// Check for null models.
		if(inModel == null || privModel == null || vivoModel == null) 
		{
			throw new IllegalArgumentException("A model cannot be null!");
		}
		
		this.incomingModel = inModel;
		this.privateModel = privModel;
		this.vivoModel = vivoModel;
		
		this.bTransferImg = bTransferImg;
		this.bDeleteImg = bDeleteImg;
	}
	
	/**
	 * Constructor
	 * @param args Commandline arguments.
	 * @throws IllegalArgumentException ArgParser thrown.
	 * @throws IOException Error creating task.
	 * @throws UsageException User requested usage message.
	 */
	private ImagePreservationDuringPrivacy(String[] args) throws IllegalArgumentException, IOException, UsageException 
	{
		this(getParser().parse(args));
	}
	
	/**
	 * @throws IOException JenaConnect
	 */
	public void execute() throws IOException
	{

		// Load incomingModel and vivoModel into tempModel.
		try	{
			prepareModels();
		} catch (IOException e) { 
			//Ignore for now.
		}
	
		// Trace the current state of vivoModel and privateModel before performing any operations. For testing.
		//tracePreConditions();
		
		// This multi-graph query finds matched UFIDs with changed privacy flags between incomingModel and vivoModel. 
		String privacyFlagMultiQuery = 	getPrivacyFlagChangeQuery();
		
		// Query Incoming and Vivo models and iterate through solutions. The bDataSetMode flag must be true for multi-graph query.
		ResultSet unionedQueryResult;
		unionedQueryResult = this.tempModel.executeSelectQuery(privacyFlagMultiQuery, true);
		
		for(QuerySolution solution : IterableAdaptor.adapt(unionedQueryResult)) 
		{
			log.trace(solution.toString());
			
			// A null flag is interpreted as 'N'.
			String vivoPrivFlag = solution.get("?vivoPriv") == null ? "N" : solution.get("?vivoPriv").toString();
			String inPrivFlag = solution.get("?incomingPriv") == null ? "N" : solution.get("?incomingPriv").toString();
			String ufid = solution.get("?ufid").toString();

			// Used to create a copy of image data to place in another model.
			String transferImgDataQuery = getTransferQuery(ufid);
			
			if( inPrivFlag.equals("Y") && vivoPrivFlag.equals("N"))
			{
				transitionToPrivacy(transferImgDataQuery, ufid);
			}
			else if ( inPrivFlag.equals("N") && vivoPrivFlag.equals("Y") )
			{
				transitionFromPrivacy(transferImgDataQuery, ufid);	
			}
		}
		
		// Ensure that tempModel is torn down before next execution.
		// TODO Put in try-catch.
		this.tempModel.truncate();
		this.tempModel.close();
	}
			
	/**
	 * @param transferQuery Query selecting what data to transfer.
	 * @param ufid String form of UFID literal to include in query.
	 * @throws IOException JenaConnect
	 */
	private void transitionToPrivacy(String transferQuery, String ufid) throws IOException
	{
		log.trace("Operating on UFID: " + ufid);
		
		String removeImgDataFromVivo = getRemoveImgFromVivoQuery(ufid);
		String setPrivacyTrue = getPrivacyFlagQuery(ufid, true);
		JenaConnect appendModel;
		
		// Construct a model of changes to append to privateModel. Remove same data from vivoModel.
		if( this.bTransferImg )
		{
			appendModel = this.vivoModel.executeConstructQuery(transferQuery);
			this.privateModel.loadRdfFromJC(appendModel);
			
			// Trace the contents of the appendModel. Testing only.
			//traceModel("appendModel Contents: ", appendModel);
			appendModel.truncate();
			
			// Trace the contents of the updated privateModel. Testing only.
			//traceModel("New privateModel contents: " + new Boolean(this.privateModel.executeAskQuery("ASK { ?s ?p ?o .}")).toString(), this.privateModel);
			
			if( this.bDeleteImg )
			{
				this.vivoModel.executeUpdateQuery(removeImgDataFromVivo);
				
				// Trace the contents of the updated vivoModel. Testing only.
				//traceModel("New Vivo Model contents: " + new Boolean(this.vivoModel.executeAskQuery("ASK { ?s ?p ?o .}")).toString(), this.vivoModel);
			}
		}
		else
		{
			if( this.bDeleteImg )
			{
				log.warn("Image data being deleted without being stored in privateModel!");
				this.vivoModel.executeUpdateQuery(removeImgDataFromVivo);
				
				// Trace the contents of the updated vivoModel. Testing only.
				//traceModel("New Vivo Model contents: " + new Boolean(this.vivoModel.executeAskQuery("ASK { ?s ?p ?o .}")).toString(), this.vivoModel);
			}
		}
		
		this.vivoModel.executeUpdateQuery(setPrivacyTrue);

	}
	
	/**
	 * @param transferQuery Query selecting what data to transfer.
	 * @param ufid String form of UFID literal to include in queries.
	 * @throws IOException JenaConnect
	 */
	private void transitionFromPrivacy(String transferQuery, String ufid) throws IOException
	{
		log.trace("Operating on UFID: " + ufid);
		
		String removePersonFromPrivate = getRemoveImgFromPrivateQuery(ufid);
		String setPrivacyFalse = getPrivacyFlagQuery(ufid, false);
		JenaConnect appendModel;
		
		// Construct a model of changes to append to vivoModel. Remove person from privateModel.
		if( this.bTransferImg )
		{
			appendModel = this.privateModel.executeConstructQuery(transferQuery);
			this.vivoModel.loadRdfFromJC(appendModel);
			
			// Trace the contents of the appendModel. Testing only.
			//traceModel("appendModel Contents: ", appendModel);

			// Trace the contents of the updated vivoModel. Testing only.
			//traceModel("New Vivo Model contents: " + new Boolean(this.vivoModel.executeAskQuery("ASK { ?s ?p ?o .}")).toString(), this.vivoModel);
			
			if( this.bDeleteImg )
			{
				this.privateModel.executeUpdateQuery(removePersonFromPrivate);
				
				// Trace the contents of the updated privateModel. Testing only.
				//traceModel("New privateModel contents: " + new Boolean(this.privateModel.executeAskQuery("ASK { ?s ?p ?o .}")).toString(), this.privateModel);
			}
			
		}
		else
		{
			if( this.bDeleteImg )
			{
				log.warn("Image data being deleted without being stored back in vivoModel!");
			}
		}

		this.vivoModel.executeUpdateQuery(setPrivacyFalse);
		
	}
	
	/**
	 * @return Completed query.
	 */
	private String getPrivacyFlagChangeQuery()
	{
		// TODO Consider namespace check for performance tuning. 
		// This multi-graph query finds matched UFIDs with changed privacy flags between incomingModel and vivoModel. 
		String privacyFlagChangeQuery = 	
			"PREFIX imagepres: <http://vivoweb.org/harvester/model/imagepres#>" 	+'\n'+
			"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" 							+'\n'+
			"PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>" 	+'\n'+
			"SELECT DISTINCT ?ufid ?vivoPriv ?incomingPriv"							+'\n'+
			"FROM NAMED <http://vivoweb.org/harvester/model/imagepres#vivoClone>"	+'\n'+
			"FROM NAMED <http://vivoweb.org/harvester/model/imagepres#inputClone>"	+'\n'+
			"WHERE {"																+'\n'+
			"	GRAPH imagepres:vivoClone {"										+'\n'+
			"		?s ufvivo:ufid ?ufid ."											+'\n'+
			"		OPTIONAL {?s ufvivo:privacy ?vivoPriv .}"						+'\n'+
			//"		?s ufvivo:privacy ?vivoPriv ."									+'\n'+
			"	} ."																+'\n'+
			"	GRAPH imagepres:inputClone {"										+'\n'+
			"		?s ufvivo:ufid ?ufid ."											+'\n'+
			"		OPTIONAL {?s ufvivo:privacy ?incomingPriv .}"					+'\n'+
			"	} ."																+'\n'+
			//"	FILTER ( str(?vivoPriv) != str(?incomingPriv) ) ."					+'\n'+
			"}";
		
		return privacyFlagChangeQuery;
		
	}
		
	/**
	 * @param ufid String form of UFID literal to include in query.
	 * @return Completed query.
	 */
	private String getTransferQuery(String ufid)
	{
		// This query grabs the mainImage and all sub-nodes associated with a specific UFID from
		// either vivoModel or privateModel.
		String transferImgData =
			"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" 							+'\n'+
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"				+'\n'+
			"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>"		+'\n'+
			"PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>"	+'\n'+
			"CONSTRUCT { " 															+'\n'+
			"	 ?uri	ufvivo:ufid " + 		"\""+ufid+"\" ."					+'\n'+
			"	 ?uri	public:mainImage			?img ."							+'\n'+
			"	 ?img   ?p							?o ."							+'\n'+
			"    ?o   	?p2							?o2 ."							+'\n'+
			"    ?o2	?p3							?o3 ."       					+'\n'+
			"    ?o3	?p4							?o4 ."       					+'\n'+
			"    ?o4	?p5							?o5 ."       					+'\n'+
			"}"																		+'\n'+
			"WHERE { " 																+'\n'+
			"	?uri		ufvivo:ufid " +		"\""+ufid+"\" ."					+'\n'+
			"	?uri		public:mainImage	?img ."								+'\n'+
			"	OPTIONAL { ?img  	?p		?o . "									+'\n'+
			"	OPTIONAL { ?o   	?p2		?o2 . "									+'\n'+
			"	OPTIONAL { ?o2		?p3		?o3 . "       							+'\n'+
			"	OPTIONAL { ?o3		?p4		?o4 . "       							+'\n'+
			"	OPTIONAL { ?o4		?p5		?o5 . "									+'\n'+
			"	}}}}}"       														+'\n'+
			"}";
		return transferImgData;
	}
		
	/**
	 * @param ufid String form of UFID literal to include in query.
	 * @return Completed query.
	 */
	private String getRemoveImgFromVivoQuery(String ufid)
	{
		String removeImgDataFromVivo =
			"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" 							+'\n'+
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"				+'\n'+
			"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>"		+'\n'+
			"PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>"	+'\n'+
			"DELETE { " 															+'\n'+
			"	 ?uri	public:mainImage			?img ."							+'\n'+
			"	 ?img   ?p							?o ."							+'\n'+
			"    ?o   	?p2							?o2 ."							+'\n'+
			"    ?o2	?p3							?o3 ."       					+'\n'+
			"    ?o3	?p4							?o4 ."       					+'\n'+
			"    ?o4	?p5							?o5 ."       					+'\n'+
			"}"																		+'\n'+
			"WHERE { " 																+'\n'+
			"	?uri		ufvivo:ufid " +		"\""+ufid+"\" ."					+'\n'+
			"	?uri		public:mainImage	?img ."								+'\n'+
			"	OPTIONAL { ?img  	?p		?o . "									+'\n'+
			"	OPTIONAL { ?o   	?p2		?o2 . "									+'\n'+
			"	OPTIONAL { ?o2		?p3		?o3 . "       							+'\n'+
			"	OPTIONAL { ?o3		?p4		?o4 . "       							+'\n'+
			"	OPTIONAL { ?o4		?p5		?o5 . "									+'\n'+
			"	}}}}}"       														+'\n'+
			"}";
		
		return removeImgDataFromVivo;
	}
		
	/**
	 * @param ufid String form of UFID literal to include in query.
	 * @return Completed query.
	 */
	private String getRemoveImgFromPrivateQuery(String ufid)
	{
		String removePersonFromPrivate =
			"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" 							+'\n'+
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"				+'\n'+
			"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>"		+'\n'+
			"PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>"	+'\n'+
			"DELETE { "																+'\n'+
			"	 ?uri	ufvivo:ufid " +			"\""+ufid+"\" ."					+'\n'+
			"	 ?uri	public:mainImage			?img ."							+'\n'+
			"	 ?img   ?p							?o ."							+'\n'+
			"    ?o   	?p2							?o2 ."							+'\n'+
			"    ?o2	?p3							?o3 ."       					+'\n'+
			"    ?o3	?p4							?o4 ."       					+'\n'+
			"    ?o4	?p5							?o5 ."       					+'\n'+
			"}"																		+'\n'+
			"WHERE { " 																+'\n'+
			"	?uri		ufvivo:ufid " +		"\""+ufid+"\" ."					+'\n'+
			"	OPTIONAL { ?uri		public:mainImage	?img ."						+'\n'+
			"	OPTIONAL { ?img  	?p		?o . "									+'\n'+
			"	OPTIONAL { ?o   	?p2		?o2 . "									+'\n'+
			"	OPTIONAL { ?o2		?p3		?o3 . "       							+'\n'+
			"	OPTIONAL { ?o3		?p4		?o4 . "       							+'\n'+
			"	OPTIONAL { ?o4		?p5		?o5 . "									+'\n'+
			"	}}}}}}"       														+'\n'+
			"}";
		
		return removePersonFromPrivate;
	}
		
	/**
	 * @param ufid String form of UFID literal to include in query.
	 * @param newPrivacy Desired new privacy flag.
	 * @return Completed query.
	 */
	private String getPrivacyFlagQuery(String ufid, boolean newPrivacy)
	{
		String newFlag;
		newFlag = (newPrivacy) ? "Y" : "N";
		
		String setPrivacy =
			"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
			"DELETE{ ?s ufvivo:privacy ?priv . }"									+'\n'+
			"WHERE{ "																+'\n'+
			"	?s ufvivo:ufid"	+ "\""+ufid+"\" ."									+'\n'+
			"	?s ufvivo:privacy ?priv ." 											+'\n'+
			"}"																		+'\n'+
			"INSERT{ ?s ufvivo:privacy \"" + newFlag + "\" }"						+'\n'+
			"WHERE{ ?s ufvivo:ufid"	+ "\""+ufid+"\" . }";
		
		return setPrivacy;
	}
		
	/**
	 * This method outputs the contents of relevant models and runs simple queries to establish the
	 * 'health' of the models and to check assumptions. Used for testing.
	 * @throws IOException Jena query.
	 */
	@SuppressWarnings("unused")
	private void tracePreConditions() throws IOException
	{
		// This query returns the UFID, last name, privacy flag, and image node from one graph. Testing only.
		String privacyFlagQuery = 	
			"PREFIX ufvivo:<http://vivo.ufl.edu/ontology/vivo-ufl/>" 				+'\n'+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" 							+'\n'+
			"PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>" 	+'\n'+
			"SELECT DISTINCT ?ufid ?name ?priv ?img"								+'\n'+
			"WHERE {" 																+'\n'+
			"	?s 	ufvivo:ufid 	?ufid ." 										+'\n'+
			"	?s 	foaf:lastName 	?name ." 										+'\n'+
			"	?s 	ufvivo:privacy 	?priv ."										+'\n'+
			"	OPTIONAL {?s public:mainImage ?img .}"								+'\n'+
			"}";
		
		ResultSet vivoQueryResult;
		ResultSet incomingQueryResult;
		
		// Query the Vivo model and trace results to console. Testing only.
		vivoQueryResult = this.vivoModel.executeSelectQuery(privacyFlagQuery);
		traceResultSet("ResultSet for privacyFlagQuery on vivoModel. " + "Has results?: " + vivoQueryResult.hasNext(), vivoQueryResult);

		// Query the Incoming model and trace results to console. Testing only.
		incomingQueryResult = this.incomingModel.executeSelectQuery(privacyFlagQuery);
		traceResultSet("ResultSet for privacyFlagQuery on incomingModel. " + "Has results?: " + incomingQueryResult.hasNext(), incomingQueryResult);
		
		traceModel("Pre-Alteration vivomodel Contents: ", this.vivoModel);
		traceModel("Pre-Alteration privateModel Contents: ", this.privateModel);
	}
	
	/**
	 * Traces the contents of an entire model for testing/logging.
	 * @param traceLabel Preceding label.
	 * @param model The model to trace.
	 * @throws IOException Jena query.
	 */
	private void traceModel(String traceLabel, JenaConnect model) throws IOException
	{
		log.trace(traceLabel);
		
		ResultSet traceSet = model.executeSelectQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o .}");
		for(QuerySolution soln : IterableAdaptor.adapt(traceSet)){
			log.trace(soln.toString());
		}
	}
	
	/**
	 * Traces the contents of a result set for testing/logging.
	 * @param traceLabel Preceding label.
	 * @param set Set to trace.
	 */
	private void traceResultSet(String traceLabel, ResultSet set)
	{
		log.trace(traceLabel);
		
		for(QuerySolution soln : IterableAdaptor.adapt(set)){
			log.trace(soln.toString());
		}
	}
	
	/**
	 * Loads a copy of vivoModel and incomingModel into tempModel for use in multi-graph queries.
	 * @throws IOException JenaConnect
	 */
	private void prepareModels() throws IOException
	{
		//this.tempModel = new TDBJenaConnect("data/temp-data", "urn:x-arq:UnionGraph");
		this.tempModel = new MemJenaConnect("tempModel");
		JenaConnect vivoClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/imagepres#vivoClone");
		vivoClone.loadRdfFromJC(this.vivoModel);
		JenaConnect inputClone = this.tempModel.neighborConnectClone("http://vivoweb.org/harvester/model/imagepres#inputClone");
		inputClone.loadRdfFromJC(this.incomingModel);
		
		// Check names in tempModel dataset.
		Iterator<String> dSetNames = this.tempModel.getDataset().listNames();
		log.trace("tempModel DataSet names: "); 
		for (String s : IterableAdaptor.adapt(dSetNames))
		{
			log.trace("\t \t" + s);
		}
			
		// Check for triples in vivoModel.
		if(!this.vivoModel.executeAskQuery("ASK { ?s ?p ?o }")) 
		{
			log.debug("Empty Dataset: Vivo");
		}
		
		// Check for triples in incomingModel.
		if(!this.incomingModel.executeAskQuery("ASK { ?s ?p ?o }")) 
		{
			log.debug("Empty Dataset: Incoming");
		}
		
		// Check for triples in privateModel.
		if(!this.privateModel.executeAskQuery("ASK { ?s ?p ?o }")) 
		{
			log.debug("Empty Dataset: Private");
		}
		
		// Check for triples in tempModel.
		if(!this.tempModel.executeAskQuery("ASK { ?s ?p ?o }")) 
		{
			log.debug("Empty Dataset: Temp");
		}
	}
	
	/**
	 * Constructor.
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	private ImagePreservationDuringPrivacy(ArgList argList) throws IOException 
	{
		this(
			JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")), 
			JenaConnect.parseConfig(argList.get("p"), argList.getValueMap("P")),
			JenaConnect.parseConfig(argList.get("v"), argList.getValueMap("V")),
			argList.has("s"), argList.has("d")
			);
	}
	
	
	
	/**
	 * Get the ArgParser for this task.
	 * @return the ArgParser
	 */
	private static ArgParser getParser() 
	{
		ArgParser parser = new ArgParser("ImagePreservationDuringPrivacy");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJena JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of inputJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoJena-config").withParameter(true, "CONFIG_FILE").setDescription("vivoJena JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('V').setLongOpt("vivoOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of vivoJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("privateJena-config").withParameter(true, "CONFIG_FILE").setDescription("private entries data JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("privateOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of private entries jena model config using VALUE").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("dTransfer").setDescription("Transfer img between models.").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dDelete").setDescription("Delete img from models.").setRequired(false));
		
		return parser;
	}
	
	/**
	 * Main method.
	 * @param args commandline arguments
	 */
	public static void main(String... args) 
	{
		Exception error = null;
		try 
		{
			InitLog.initLogger(args, getParser());
			log.info("ImagePreservationDuringPrivacy: Start");
			new ImagePreservationDuringPrivacy(args).execute();
		} 
		catch(IllegalArgumentException e) 
		{
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} 
		catch(UsageException e) 
		{
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} 
		catch(Exception e) 
		{
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} 
		finally 
		{
			log.info("ImagePreservationDuringPrivacy: End");
			if(error != null) 
			{
				System.exit(1);
			}
		}
	}
}
