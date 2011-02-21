/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.diff.Diff;
import org.vivoweb.harvester.fetch.JDBCFetch;
import org.vivoweb.harvester.qualify.ChangeNamespace;
import org.vivoweb.harvester.score.Match;
import org.vivoweb.harvester.score.Score;
import org.vivoweb.harvester.score.algorithm.Algorithm;
import org.vivoweb.harvester.score.algorithm.EqualityTest;
import org.vivoweb.harvester.translate.XSLTranslator;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.Merge;
import org.vivoweb.harvester.util.repo.JDBCRecordHandler;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;

/**
 * 	
 */
public class DemoPSMerge {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(DemoPSMerge.class);
	
	/**
	 * Configure the parameters for JDBCFetch
	 * @param tableNames Set of table names
	 * @param fromClauses Mapping of extra tables for the from section
	 * @param dataFields Mapping of tablename to list of datafields
	 * @param idFields Mapping of tablename to idField name
	 * @param whereClauses List of conditions
	 * @param relations Mapping of tablename to mapping of fieldname to tablename
	 * @param queryStrings The user defined SQL Query string
	 */
	@SuppressWarnings("unused")
	private static void configFetchParams(Set<String> tableNames, Map<String, String> fromClauses, Map<String, List<String>> dataFields, Map<String, List<String>> idFields, Map<String, List<String>> whereClauses, Map<String, Map<String, String>> relations, Map<String, String> queryStrings) {
		tableNames.add("t_UF_DIR_EMP_STU_1");
		idFields.put("t_UF_DIR_EMP_STU_1", Arrays.asList("UF_IDENTIFIER"));
		whereClauses.put("t_UF_DIR_EMP_STU_1", Arrays.asList("t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		
		tableNames.add("t_UF_DIR_EMP_STU_2");
		idFields.put("t_UF_DIR_EMP_STU_2", Arrays.asList("UF_UUID", "UF_TYPE_CD"));
		whereClauses.put("t_UF_DIR_EMP_STU_2", Arrays.asList("t_UF_DIR_EMP_STU_2.UF_UUID=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_DIR_EMP_STU_2", "t_UF_DIR_EMP_STU_1");
		
		tableNames.add("t_UF_DIR_EMP_STU_3");
		idFields.put("t_UF_DIR_EMP_STU_3", Arrays.asList("UF_UUID", "UF_TYPE_CD"));
		whereClauses.put("t_UF_DIR_EMP_STU_3", Arrays.asList("t_UF_DIR_EMP_STU_3.UF_UUID=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_DIR_EMP_STU_3", "t_UF_DIR_EMP_STU_1");
		
		tableNames.add("t_UF_DIR_EMP_STU_4");
		idFields.put("t_UF_DIR_EMP_STU_4", Arrays.asList("UF_UUID", "UF_TYPE_CD"));
		whereClauses.put("t_UF_DIR_EMP_STU_4", Arrays.asList("t_UF_DIR_EMP_STU_4.UF_UUID=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_DIR_EMP_STU_4", "t_UF_DIR_EMP_STU_1");
		
		tableNames.add("t_UF_DIR_EMP_STU_5");
		idFields.put("t_UF_DIR_EMP_STU_5", Arrays.asList("UF_UUID1", "UF_TYPE_CD", "UF_UUID2", "PS_DEPTID", "UF_BEGIN_TS"));
		whereClauses.put("t_UF_DIR_EMP_STU_5", Arrays.asList("t_UF_DIR_EMP_STU_5.UF_UUID1=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_DIR_EMP_STU_5", "t_UF_DIR_EMP_STU_1");
		
		tableNames.add("t_UF_DIR_EMP_STU_6");
		idFields.put("t_UF_DIR_EMP_STU_6", Arrays.asList("UF_UUID1", "UF_TYPE_CD", "UF_UUID2", "PS_DEPTID", "UF_BEGIN_TS"));
		whereClauses.put("t_UF_DIR_EMP_STU_6", Arrays.asList("t_UF_DIR_EMP_STU_6.UF_UUID1=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_DIR_EMP_STU_6", "t_UF_DIR_EMP_STU_1");
		
		tableNames.add("t_UF_PER_UFAU");
		idFields.put("t_UF_PER_UFAU", Arrays.asList("UF_UUID", "UF_JOB_TITLE", "UF_PS_DEPTID"));
		whereClauses.put("t_UF_PER_UFAU", Arrays.asList("t_UF_PER_UFAU.UF_UUID=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_PER_UFAU", "t_UF_DIR_EMP_STU_1");
		
		tableNames.add("t_UF_PA_GL_ACCT");
		idFields.put("t_UF_PA_GL_ACCT", Arrays.asList("OPRID", "USERIDALIAS"));
		whereClauses.put("t_UF_PA_GL_ACCT", Arrays.asList("t_UF_PA_GL_ACCT.OPRID=t_UF_DIR_EMP_STU_1.UF_IDENTIFIER", "t_UF_DIR_EMP_STU_1.UF_PROTECT_FLG='N'", "t_UF_DIR_EMP_STU_1.UF_SECURITY_FLG='N'"));
		fromClauses.put("t_UF_PA_GL_ACCT", "t_UF_DIR_EMP_STU_1");
		
		//		tableNames.add("t_PS_H_UF_ACAD_ORG");
		//		idFields.put("t_PS_H_UF_ACAD_ORG", Arrays.asList("DTL_ID"));
		//		dataFields.put("t_PS_H_UF_ACAD_ORG", Arrays.asList(
		//			"DTL_DESC"//,
		//			//"TREE_NAME",
		//			//"L2_ID",
		//			//"L2_DESC",
		//			//"L3_ID",
		//			//"L3_DESC",
		//			//"L4_ID",
		//			//"L4_DESC"
		//		));
		
		List<String> ufidLimiters = Arrays.asList(
			//			"t_UF_DIR_EMP_STU_1.UF_IDENTIFIER LIKE '%8973%'"
			);
		whereClauses.put("t_UF_DIR_EMP_STU_1", new ArrayList<String>(whereClauses.get("t_UF_DIR_EMP_STU_1")));
		whereClauses.get("t_UF_DIR_EMP_STU_1").addAll(ufidLimiters);
		whereClauses.put("t_UF_DIR_EMP_STU_2", new ArrayList<String>(whereClauses.get("t_UF_DIR_EMP_STU_2")));
		whereClauses.get("t_UF_DIR_EMP_STU_2").addAll(ufidLimiters);
		whereClauses.put("t_UF_DIR_EMP_STU_3", new ArrayList<String>(whereClauses.get("t_UF_DIR_EMP_STU_3")));
		whereClauses.get("t_UF_DIR_EMP_STU_3").addAll(ufidLimiters);
		whereClauses.put("t_UF_DIR_EMP_STU_4", new ArrayList<String>(whereClauses.get("t_UF_DIR_EMP_STU_4")));
		whereClauses.get("t_UF_DIR_EMP_STU_4").addAll(ufidLimiters);
		whereClauses.put("t_UF_DIR_EMP_STU_5", new ArrayList<String>(whereClauses.get("t_UF_DIR_EMP_STU_5")));
		whereClauses.get("t_UF_DIR_EMP_STU_5").addAll(ufidLimiters);
		whereClauses.put("t_UF_DIR_EMP_STU_6", new ArrayList<String>(whereClauses.get("t_UF_DIR_EMP_STU_6")));
		whereClauses.get("t_UF_DIR_EMP_STU_6").addAll(ufidLimiters);
		whereClauses.put("t_UF_PER_UFAU", new ArrayList<String>(whereClauses.get("t_UF_PER_UFAU")));
		whereClauses.get("t_UF_PER_UFAU").addAll(ufidLimiters);
		whereClauses.put("t_UF_PA_GL_ACCT", new ArrayList<String>(whereClauses.get("t_UF_PA_GL_ACCT")));
		whereClauses.get("t_UF_PA_GL_ACCT").addAll(ufidLimiters);
	}
	
	/**
	 * @param args cmdline args
	 * @throws SQLException error
	 * @throws ClassNotFoundException error
	 * @throws IOException error
	 */
	//	@SuppressWarnings("unused")
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		System.setProperty("process-task", "Fetch");
		InitLog.initLogger(null, null);
		// setup fetch parameter variables
		Set<String> tableNames = new TreeSet<String>();
		Map<String, String> fromClauses = new HashMap<String, String>();
		Map<String, List<String>> dataFields = new HashMap<String, List<String>>();
		Map<String, List<String>> idFields = new HashMap<String, List<String>>();
		Map<String, List<String>> whereClauses = new HashMap<String, List<String>>();
		Map<String, Map<String, String>> relations = new HashMap<String, Map<String, String>>();
		Map<String, String> queryStrings = new HashMap<String, String>();
		
		// configure parameters
		configFetchParams(tableNames, fromClauses, dataFields, idFields, whereClauses, relations, queryStrings);
		Class.forName("net.sourceforge.jtds.jdbc.Driver");
		String connLine = "jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD";
		Connection dbConn = DriverManager.getConnection(connLine, args[0], args[1]);
		RecordHandler rawRH = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:harvested-data/demoRawPS/store", "sa", "", "rawData", "rawID");
		
		// Execute Fetch
		log.trace("Fetching Raw Records");
		JDBCFetch psFetch = new JDBCFetch(dbConn, rawRH, connLine + "/", "", "", tableNames, fromClauses, dataFields, idFields, whereClauses, relations, queryStrings);
		psFetch.execute();
		
		System.setProperty("process-task", "Merge");
		InitLog.initLogger(null, null);
		// Merge related records
		RecordHandler mergedRH = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:harvested-data/demoMergedPS/store", "sa", "", "mergedData", "mergedID");
		log.trace("Merging Related Raw Records");
		Merge psMerge = new Merge(rawRH, mergedRH, "t_UF_DIR_EMP_STU_1_(id_-_.*?)");
		psMerge.execute();
		
		System.setProperty("process-task", "Translate");
		InitLog.initLogger(null, null);
		// Execute Translate
		InputStream xsl = VFS.getManager().resolveFile(new File("."), "config/datamaps/PeopleSoftToVivo.xsl").getContent().getInputStream();
		RecordHandler transRH = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:harvested-data/demoTransPS/store", "sa", "", "transData", "transID");
		log.trace("Translating Merged Records");
		XSLTranslator psTranslate = new XSLTranslator(mergedRH, transRH, xsl, true);
		psTranslate.execute();
		
		System.setProperty("process-task", "Transfer");
		InitLog.initLogger(null, null);
		// connect to input model
		JenaConnect psInput = new SDBJenaConnect("jdbc:h2:harvested-data/demoInputPS/store", "sa", "", "H2", "org.h2.Driver", "layout2", "psTempModel");
		// clear model
		log.trace("Truncating Input Model");
		psInput.truncate();
		// import from record handler into input model
		log.trace("Loading Translated Data into Input Model");
		psInput.loadRdfFromRH(transRH, "http://vivo.ufl.edu/individual/");
		
		System.setProperty("process-task", "Score.Setup");
		InitLog.initLogger(null, null);
		// connect to vivo model
		JenaConnect vivoJena = new SDBJenaConnect("jdbc:h2:harvested-data/demoVivo/store", "sa", "", "H2", "org.h2.Driver", "layout2", "vivoModel");
		// clear model and load vivo data
		//		vivoJena.truncate();
		//		log.trace("Loading vivo rdf into model");
		//		vivoJena.loadRdfFromFile("harvested-data/vivoData.rdf.ttl", "http://vivo.ufl.edu/individual/", "TTL");
		log.trace("Vivo Size: " + vivoJena.size());
		
		JenaConnect scoreJena = psInput.neighborConnectClone("scoreData");
		log.trace("Truncating Score Data Model");
		scoreJena.truncate();
		//		JenaConnect tempJena = psInput.neighborConnectClone("tempModel");
		String tempJena = "harvested-data/tempModel";
		
		// setup parameter variables
		HashMap<String, Class<? extends Algorithm>> algorithms = new HashMap<String, Class<? extends Algorithm>>();
		HashMap<String, String> inputPredicates = new HashMap<String, String>();
		HashMap<String, String> vivoPredicates = new HashMap<String, String>();
		HashMap<String, Float> weights = new HashMap<String, Float>();
		
		System.setProperty("process-task", "Score.People");
		InitLog.initLogger(null, null);
		// clear parameters and temp model
		log.trace("Truncating Temp Model");
		algorithms.clear();
		weights.clear();
		inputPredicates.clear();
		vivoPredicates.clear();
		// Execute Score for People
		algorithms.put("ufid", EqualityTest.class);
		weights.put("ufid", Float.valueOf(1f));
		inputPredicates.put("ufid", "http://vivo.ufl.edu/ontology/vivo-ufl/ufid");
		vivoPredicates.put("ufid", "http://vivo.ufl.edu/ontology/vivo-ufl/ufid");
		log.trace("Running People Score");
		Score psScorePeople = new Score(psInput, vivoJena, scoreJena, tempJena, algorithms, inputPredicates, vivoPredicates, "http://vivoweb.org/harvest/ufl/peoplesoft/person/", weights);
		psScorePeople.execute();
		
		System.setProperty("process-task", "Score.Departments");
		InitLog.initLogger(null, null);
		// clear parameters and temp model
		log.trace("Truncating Temp Model");
		algorithms.clear();
		weights.clear();
		inputPredicates.clear();
		vivoPredicates.clear();
		// Execute Score for Departments
		algorithms.put("deptId", EqualityTest.class);
		weights.put("deptId", Float.valueOf(1f));
		inputPredicates.put("deptId", "http://vivo.ufl.edu/ontology/vivo-ufl/deptID");
		vivoPredicates.put("deptId", "http://vivo.ufl.edu/ontology/vivo-ufl/deptID");
		log.trace("Running Departments Score");
		Score psScoreDepts = new Score(psInput, vivoJena, scoreJena, tempJena, algorithms, inputPredicates, vivoPredicates, "http://vivoweb.org/harvest/ufl/peoplesoft/org/", weights);
		psScoreDepts.execute();
		
		System.setProperty("process-task", "Match.PeopleDepartments");
		InitLog.initLogger(null, null);
		// Find matches for people and departments using scores and rename nodes to matching uri
		log.trace("Running Match for People and Departments");
		Match psPeopleOrgMatch = new Match(psInput, scoreJena, null, true, 1.0f, null, false,200);
		psPeopleOrgMatch.execute();
		
		System.setProperty("process-task", "Score.Positions");
		InitLog.initLogger(null, null);
		// clear parameters and temp model
		log.trace("Truncating Temp Model");
		algorithms.clear();
		weights.clear();
		inputPredicates.clear();
		vivoPredicates.clear();
		// Execute Score for Positions
		algorithms.put("posOrg", EqualityTest.class);
		weights.put("posOrg", Float.valueOf(1f));
		inputPredicates.put("posOrg", "http://vivoweb.org/ontology/core#positionInOrganization");
		vivoPredicates.put("posOrg", "http://vivoweb.org/ontology/core#positionInOrganization");
		algorithms.put("posPer", EqualityTest.class);
		weights.put("posPer", Float.valueOf(1f));
		inputPredicates.put("posPer", "http://vivoweb.org/ontology/core#positionForPerson");
		vivoPredicates.put("posPer", "http://vivoweb.org/ontology/core#positionForPerson");
		algorithms.put("deptPos", EqualityTest.class);
		weights.put("deptPos", Float.valueOf(1f));
		inputPredicates.put("deptPos", "http://vivo.ufl.edu/ontology/vivo-ufl/deptIDofPosition");
		vivoPredicates.put("deptPos", "hhttp://vivo.ufl.edu/ontology/vivo-ufl/deptIDofPosition");
		log.trace("Running Position Score");
		Score psScorePos = new Score(psInput, vivoJena, scoreJena, tempJena, algorithms, inputPredicates, vivoPredicates, "http://vivoweb.org/harvest/ufl/peoplesoft/position/", weights);
		psScorePos.execute();
		
		System.setProperty("process-task", "Match.Positions");
		InitLog.initLogger(null, null);
		// Find matches for positions using scores and rename nodes to matching uri
		log.trace("Running Match for Positions");
		Match psPosMatch = new Match(psInput, scoreJena, null, true, 1.0f, null, false);
		psPosMatch.execute();
		
		System.setProperty("process-task", "ChangeNamespace.People");
		InitLog.initLogger(null, null);
		// Execute ChangeNamespace to get unmatched People into current namespace
		log.trace("Running People Change Namespace");
		ChangeNamespace psCNpeople = new ChangeNamespace(psInput, vivoJena, "http://vivoweb.org/harvest/ufl/peoplesoft/person/", "http://vivo.ufl.edu/individual/", false);
		psCNpeople.execute();
		
		System.setProperty("process-task", "ChangeNamespace.Departments");
		InitLog.initLogger(null, null);
		// Execute ChangeNamespace to get unmatched Departments into current namespace
		log.trace("Running Departments Change Namespace");
		ChangeNamespace psCNdepts = new ChangeNamespace(psInput, vivoJena, "http://vivoweb.org/harvest/ufl/peoplesoft/org/", "http://vivo.ufl.edu/individual/", true);
		psCNdepts.execute();
		
		System.setProperty("process-task", "ChangeNamespace.Positions");
		InitLog.initLogger(null, null);
		// Execute ChangeNamespace to get unmatched Positions into current namespace
		log.trace("Running Positions Change Namespace");
		ChangeNamespace psCNpos = new ChangeNamespace(psInput, vivoJena, "http://vivoweb.org/harvest/ufl/peoplesoft/position/", "http://vivo.ufl.edu/individual/", false);
		psCNpos.execute();
		
		System.setProperty("process-task", "DiffSetup");
		InitLog.initLogger(null, null);
		// Connect to previous harvest model
		JenaConnect psPrevHarvest = vivoJena.neighborConnectClone("uflPeopleSoft");
		//		// clear model and load previous connect data
		//		psPrevHarvest.truncate();
		//		log.trace("Loading previous harvest rdf into model");
		//		psPrevHarvest.loadRdfFromFile("harvested-data/psHarvestData.rdf.ttl", "http://vivo.ufl.edu/individual/", "TTL");
		log.trace("Previous Harvest Size: " + psPrevHarvest.size());
		
		// Setup adds/subs models
		JenaConnect psSubsModel = new SDBJenaConnect("jdbc:h2:harvested-data/demoDiffs/store", "sa", "", "H2", "org.h2.Driver", "layout2", "subsModel");
		JenaConnect psAddsModel = psSubsModel.neighborConnectClone("addsModel");
		
		System.setProperty("process-task", "Diff.Subs");
		InitLog.initLogger(null, null);
		// Find Subtractions
		Diff psDiffSubs = new Diff(psPrevHarvest, psInput, psSubsModel);
		log.trace("Finding subtractions");
		psDiffSubs.execute();
		
		System.setProperty("process-task", "Diff.Adds");
		InitLog.initLogger(null, null);
		// Find Additions
		Diff psDiffAdds = new Diff(psInput, psPrevHarvest, psAddsModel);
		log.trace("Finding additions");
		psDiffAdds.execute();
		
		System.setProperty("process-task", "Diff.ApplyPrev");
		InitLog.initLogger(null, null);
		// Apply Subtractions to Previous model
		log.trace("Applying subtractions to harvest model");
		psPrevHarvest.removeRdfFromJC(psSubsModel);
		// Apply Additions to Previous model
		log.trace("Applying additions to harvest model");
		psPrevHarvest.loadRdfFromJC(psAddsModel);
		
		System.setProperty("process-task", "Diff.ApplyVivo");
		InitLog.initLogger(null, null);
		// Apply Subtractions to VIVO
		log.trace("Applying subtractions to vivo");
		vivoJena.removeRdfFromJC(psSubsModel);
		// Apply Additions to VIVO
		log.trace("Applying additions to vivo");
		vivoJena.loadRdfFromJC(psAddsModel);
	}
}