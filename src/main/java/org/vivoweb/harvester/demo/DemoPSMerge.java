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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.JDBCFetch;
import org.vivoweb.harvester.qualify.ChangeNamespace;
import org.vivoweb.harvester.score.FieldComparison;
import org.vivoweb.harvester.score.Match;
import org.vivoweb.harvester.score.Score;
import org.vivoweb.harvester.score.algorithm.EqualityTest;
import org.vivoweb.harvester.translate.XSLTranslator;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.SDBJenaConnect;
import org.vivoweb.harvester.util.recordhandler.JDBCRecordHandler;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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
		rawRH.merge(mergedRH, Pattern.compile("t_UF_DIR_EMP_STU_1_(id_-_.*?)"));
		
		System.setProperty("process-task", "Translate");
		InitLog.initLogger(null, null);
		// Execute Translate
		InputStream xsl = FileAide.getInputStream("config/datamaps/PeopleSoftToVivo.xsl");
		RecordHandler transRH = new JDBCRecordHandler("org.h2.Driver", "jdbc:h2:harvested-data/demoTransPS/store", "sa", "", "transData", "transID");
		log.trace("Translating Merged Records");
		XSLTranslator psTranslate = new XSLTranslator(xsl);
		psTranslate.translateRecordHandler(mergedRH, transRH, true);
		
		System.setProperty("process-task", "Transfer");
		InitLog.initLogger(null, null);
		// connect to input model
		JenaConnect psInput = new SDBJenaConnect("jdbc:h2:harvested-data/demoInputPS/store", "sa", "", "H2", "org.h2.Driver", "layout2", "psTempModel");
		// clear model
		log.trace("Truncating Input Model");
		psInput.truncate();
		// import from record handler into input model
		log.trace("Loading Translated Data into Input Model");
		psInput.loadRdfFromRH(transRH, "http://vivo.ufl.edu/individual/", null);
		
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
		Score psScore = new Score(psInput, vivoJena, scoreJena, tempJena);
		Match psMatch = new Match(scoreJena);
		Set<FieldComparison> comparisons = new HashSet<FieldComparison>();
		
		System.setProperty("process-task", "Score.People");
		InitLog.initLogger(null, null);
		// clear parameters
		comparisons.clear();
		// Execute Score for People
		Property ufid = ResourceFactory.createProperty("http://vivo.ufl.edu/ontology/vivo-ufl/ufid");
		comparisons.add(new FieldComparison("ufid", EqualityTest.class, ufid, ufid, 1f));
		log.trace("Running People Score");
		psScore.execute(comparisons, "http://vivoweb.org/harvest/ufl/peoplesoft/person/");
		
		System.setProperty("process-task", "Score.Departments");
		InitLog.initLogger(null, null);
		// clear parameters
		comparisons.clear();
		// Execute Score for Departments
		Property deptid = ResourceFactory.createProperty("http://vivo.ufl.edu/ontology/vivo-ufl/deptID");
		comparisons.add(new FieldComparison("deptId", EqualityTest.class, deptid, deptid, 1f));
		log.trace("Running Departments Score");
		psScore.execute(comparisons, "http://vivoweb.org/harvest/ufl/peoplesoft/org/");
		
		System.setProperty("process-task", "Match.PeopleDepartments");
		InitLog.initLogger(null, null);
		// Find matches for people and departments using scores and rename nodes to matching uri
		log.trace("Running Match for People and Departments");
		psMatch.match(1.0f);
		psMatch.rename(psInput);
		psMatch.clearMatchResults();
		
		System.setProperty("process-task", "Score.Positions");
		InitLog.initLogger(null, null);
		// clear parameters
		comparisons.clear();
		// Execute Score for Positions
		Property posOrg = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#positionInOrganization");
		comparisons.add(new FieldComparison("posOrg", EqualityTest.class, posOrg, posOrg, 1f));
		Property posPer = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#positionForPerson");
		comparisons.add(new FieldComparison("posPer", EqualityTest.class, posPer, posPer, 1f));
		Property deptPos = ResourceFactory.createProperty("http://vivo.ufl.edu/ontology/vivo-ufl/deptIDofPosition");
		comparisons.add(new FieldComparison("deptPos", EqualityTest.class, deptPos, deptPos, 1f));
		log.trace("Running Position Score");
		psScore.execute(comparisons, "http://vivoweb.org/harvest/ufl/peoplesoft/position/");
		
		System.setProperty("process-task", "Match.Positions");
		InitLog.initLogger(null, null);
		// Find matches for positions using scores and rename nodes to matching uri
		log.trace("Running Match for Positions");
		psMatch.match(1.0f);
		psMatch.rename(psInput);
		psMatch.clearMatchResults();
		
		System.setProperty("process-task", "ChangeNamespace.People");
		InitLog.initLogger(null, null);
		// Execute ChangeNamespace to get unmatched People into current namespace
		log.trace("Running People Change Namespace");
		ChangeNamespace.changeNS(psInput, vivoJena, "http://vivoweb.org/harvest/ufl/peoplesoft/person/", "http://vivo.ufl.edu/individual/", false);
		
		System.setProperty("process-task", "ChangeNamespace.Departments");
		InitLog.initLogger(null, null);
		// Execute ChangeNamespace to get unmatched Departments into current namespace
		log.trace("Running Departments Change Namespace");
		ChangeNamespace.changeNS(psInput, vivoJena, "http://vivoweb.org/harvest/ufl/peoplesoft/org/", "http://vivo.ufl.edu/individual/", true);
		
		System.setProperty("process-task", "ChangeNamespace.Positions");
		InitLog.initLogger(null, null);
		// Execute ChangeNamespace to get unmatched Positions into current namespace
		log.trace("Running Positions Change Namespace");
		ChangeNamespace.changeNS(psInput, vivoJena, "http://vivoweb.org/harvest/ufl/peoplesoft/position/", "http://vivo.ufl.edu/individual/", false);
		
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
		log.trace("Finding subtractions");
		psSubsModel.loadRdfFromJC(psPrevHarvest.difference(psInput));
		
		System.setProperty("process-task", "Diff.Adds");
		InitLog.initLogger(null, null);
		// Find Additions
		log.trace("Finding additions");
		psAddsModel.loadRdfFromJC(psInput.difference(psPrevHarvest));
		
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