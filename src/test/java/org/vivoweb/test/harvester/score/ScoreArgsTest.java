/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.score.Score;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;

/**
 * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class ScoreArgsTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ScoreArgsTest.class);
	/**
	 * Score input test file
	 */
	private String scoreInput;
	/**
	 * vivo test configuration file
	 */
	private File vivoXML;
	/**
	 * vivo rdf statements to load for test
	 */
	private String vivoRDF;
	/** */
	private String overrideVArg;
	/** */
	private String overrideOArg;
	/** */
	/** */
	private String overrideIArg;
	/** */
	private HashMap<String, String> overrideIArgMap;
	/** */
	private HashMap<String, String> overrideOArgProp;
	/** */
	private HashMap<String, String> overrideVArgProp;
	/** */
	private String oArg;
	/** */
	private String vArg;
	/** */
	private String iArg;
	/** */
	private JenaConnect input;
	/** */
	private JenaConnect vivo;
	/** */
	private JenaConnect output;
	
	/**
	 * setup Jena Connects
	 * @throws IOException error
	 */
	private void setupJenaConnects() throws IOException {
		this.input = JenaConnect.parseConfig(this.iArg, this.overrideIArgMap);
		// Load up input data before starting
		this.input.loadRdfFromString(this.scoreInput, null, null);
		
		this.vivo = JenaConnect.parseConfig(this.vArg, this.overrideVArgProp);
		// Load up vivo data before starting
		this.vivo.loadRdfFromString(this.vivoRDF, null, null);
		
		this.output = JenaConnect.parseConfig(this.oArg, this.overrideOArgProp);
	}
	
	/**
	 * Test match algorithm with rename flag
	 * @throws IOException error
	 */
	public void testMatchWithRename() throws IOException {
		log.info("BEGIN testMatchWithRename");
		setupJenaConnects();
		log.debug("Test -i iArg -v vArg -o oArg -m 'vivoworkEmail=scoreworkemail' -r");
		String[] args = new String[]{"-i", this.iArg, "-v", this.vArg, "-o", this.oArg, "-m", "http://vivoweb.org/ontology/core#workEmail=http://vivoweb.org/ontology/score#workEmail", "-r"};
		log.debug(StringUtils.join(args, " "));
		new Score(args);
		log.info("END testMatchWithRename");
	}
	
	/**
	 * Test match algorithm with rename and inplace flag
	 * @throws IOException error
	 */
	public void testInplaceMatchWithRename() throws IOException {
		log.info("BEGIN testInplaceMatchWithRename");
		setupJenaConnects();
		log.debug("Test -i iArg -v vArg -m 'vivoworkEmail=scoreworkemail' -r -a");
		String[] args = new String[]{"-i", this.iArg, "-v", this.vArg, "-m", "http://vivoweb.org/ontology/core#workEmail=http://vivoweb.org/ontology/score#workEmail", "-r", "-a"};
		log.debug(StringUtils.join(args, " "));
		new Score(args);
		log.info("END testInplaceMatchWithRename");
	}
	
	/**
	 * Test match algorithm with linking
	 * @throws IOException error
	 */
	public void testMatchWithLink() throws IOException {
		log.info("BEGIN testMatchWithLink");
		setupJenaConnects();
		log.debug("Test -i iArg -v vArg  -o oArg -m 'vivoworkEmail=scoreworkemail' -l 'vivoObjProp=scoreObjProp' -a");
		String[] args = new String[]{"-i", this.iArg, "-v", this.vArg, "-m", "http://vivoweb.org/ontology/core#workEmail=http://vivoweb.org/ontology/score#workEmail", "-l", "http://vivoweb.org/ontology/core#linkMe=http://vivoweb.org/ontology/core#meLink"};
		log.debug(StringUtils.join(args, " "));
		new Score(args);
		log.info("END testMatchWithLink");
	}
	
	/**
	 * Test to make sure Score blows up when illegal parameter Q is passed
	 * @throws IOException error
	 */
	public void testIllegalQParam() throws IOException {
		log.info("BEGIN testIllegalQParam");
		setupJenaConnects();
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg -Q");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg, "-Q"};
		log.debug(StringUtils.join(args, " "));
		try {
			new Score(args);
			log.error("Invalid Q param not rejected -- score object invalid");
			fail("Invalid Q param not rejected -- score object invalid");
		} catch(Exception e) {
			// this is expected
			log.debug("Caught Expected Exception: "+e.getMessage());
		}
		log.info("END testIllegalQParam");
	}
	
	/**
	 * Test to make sure model not wiped when -w flag is omitted
	 * @throws IOException error
	 */
	public void testKeepWorkingModel() throws IOException {
		log.info("BEGIN testKeepWorkingModel");
		setupJenaConnects();
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg};
		log.debug(StringUtils.join(args, " "));
		new Score(args).execute();
		if(this.input.getJenaModel().isEmpty()) {
			log.error("Model emptied despite -w arg missing");
			fail("Model emptied despite -w arg missing");
		}
		log.info("END testKeepWorkingModel");
	}
	
	/**
	 * Test to ensure -w flag causes working model to be empty
	 * @throws IOException error
	 */
	public void testWipeWorkingModel() throws IOException {
		log.info("BEGIN testWipeWorkingModel");
		setupJenaConnects();
		log.debug("Testing don't keep working model");
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArgl -w");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg, "-w"};
		log.debug(StringUtils.join(args, " "));
		new Score(args).execute();
		if(!this.input.getJenaModel().isEmpty()) {
			log.error("Model not empty -w arg violated");
			fail("Model not empty -w arg violated");
		}
		log.info("END testWipeWorkingModel");
	}
	
	/**
	 * Test to ensure that the output model gets wiped before running when -q flag set
	 * @throws IOException error
	 */
	public void testEmptyOutputModel() throws IOException {
		log.info("BEGIN testEmptyOutputModel");
		setupJenaConnects();
		log.debug("Testing empty output model");
		// empty output model
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg -q");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg,"-q"};
		log.debug(StringUtils.join(args, " "));
		//get size
		long modelSize = this.output.getJenaModel().size();
		new Score(args).execute();
		if (modelSize > this.output.getJenaModel().size()) {
			log.error("Output model not emptied before run");
			fail("Output model not emptied before run");
		}
		log.info("END testEmptyOutputModel");
	}
	
	/**
	 * Test to ensure that output model is not empties when -q flag omitted
	 * @throws IOException error
	 */
	public void testNoEmptyOutputModel() throws IOException {
		log.info("BEGIN testNoEmptyOutputModel");
		setupJenaConnects();
		// don't empty output model
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg};
		log.debug(StringUtils.join(args, " "));
		//get size
		long modelSize = this.output.getJenaModel().size();
		new Score(args).execute();
		if (modelSize < this.output.getJenaModel().size()) {
			log.error("Output model emptied before run");
			fail("Output model emptied before run");
		}
		log.info("END testNoEmptyOutputModel");
	}
	
	@Override
	protected void setUp() {
		InitLog.initLogger(ScoreArgsTest.class);
		
		// create objects under test
		// Create input rdf data
		this.scoreInput = ""+
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
			"<rdf:RDF xmlns:bibo=\"http://purl.org/ontology/bibo/\" " +
					"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" " +
					"xmlns:owlPlus=\"http://www.w3.org/2006/12/owl2-xml#\" " +
					"xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" " +
					"xmlns:skos=\"http://www.w3.org/2008/05/skos#\" " +
					"xmlns:owl=\"http://www.w3.org/2002/07/owl#\" " +
					"xmlns:vocab=\"http://purl.org/vocab/vann/\" " +
					"xmlns:swvocab=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\" " +
					"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
					"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
					"xmlns:core=\"http://vivoweb.org/ontology/core#\" " +
					"xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" " +
					"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " +
					"xmlns:score=\"http://vivoweb.org/ontology/score#\">" +
				"<rdf:Description rdf:about=\"http://vivoweb.org/pubmed/article/pmid20113680\">" +
					"<rdf:type rdf:resource=\"http://purl.org/ontology/bibo/Document\"/>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<bibo:pmid>12345678</bibo:pmid>" +
					"<rdfs:label>Batch 5 effects in controlled human testing</rdfs:label>" +
					"<core:Title>Batch 5 effects in controlled human testing</core:Title>" +
					"<score:Affiliation>Room 5 Downing Street, London, England. v@ufl.edu</score:Affiliation>" +
					"<bibo:volume>40</bibo:volume>" +
					"<bibo:number>2</bibo:number>" +
					"<core:Year>2010</core:Year>" +
					"<score:workEmail>v@ufl.edu</score:workEmail>" +
					"<core:informationResourceInAuthorship rdf:resource=\"http://vivoweb.org/pubmed/article/pmid20113680/authorship1\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh1\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh2\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh3\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh4\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid23656776mesh5\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid23656776mesh6\"/>" +
					"<core:hasSubjectArea rdf:nodeID=\"pmid23656776mesh7\"/>" +
					"<core:hasPublicationVenue rdf:resource=\"http://vivoweb.org/pubmed/journal/j1558-4623\"/>" +
					"<score:hasCreateDate rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776/dateCreated\"/>" +
					"<score:hasCompleteDate rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776/dateCompleted\"/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivoweb.org/pubmed/article/pmid23656776/authorship1\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Authorship\"/>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#DependentResource\"/>" +
					"<core:linkedAuthor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776/author1\"/>" +
					"<core:linkedInformationResource rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<core:authorRank rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</core:authorRank>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivoweb.org/pubmed/article/pmid23656776/author1\">" +
					"<rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>" +
					"<rdfs:label>Fawkes, Guy</rdfs:label>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
					"<score:foreName>Guy</score:foreName>" +
					"<core:middleName>J</core:middleName>" +
					"<score:initials>GF</score:initials>" +
					"<score:suffix/>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<core:authorInAuthorship rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776/authorship1\"/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh1\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Animals</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Animals</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier/>" +
					"<score:QualifierIsMajorTerm/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh2\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Antibodies, Monoclonal</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Antibodies, Monoclonal</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier>adverse effects therapeutic use</score:Qualifier>" +
					"<score:QualifierIsMajorTerm>N Y</score:QualifierIsMajorTerm>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh3\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Humans</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Humans</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier/>" +
					"<score:QualifierIsMajorTerm/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh4\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Lymphoma</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Lymphoma</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier>radiotherapy therapy</score:Qualifier>" +
					"<score:QualifierIsMajorTerm>Y N</score:QualifierIsMajorTerm>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh5\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Nuclear Medicine</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Nuclear Medicine</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier/>" +
					"<score:QualifierIsMajorTerm/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh6\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Radioimmunotherapy</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Radioimmunotherapy</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier>adverse effects methods</score:Qualifier>" +
					"<score:QualifierIsMajorTerm>N Y</score:QualifierIsMajorTerm>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:nodeID=\"pmid23656776mesh7\">" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" +
					"<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<rdfs:label>Treatment Outcome</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
					"<score:Descriptor>Treatment Outcome</score:Descriptor>" +
					"<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" +
					"<score:Qualifier/>" +
					"<score:QualifierIsMajorTerm/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivoweb.org/pubmed/journal/j1558-4623\">" +
					"<rdf:type rdf:resource=\"http://purl.org/ontology/bibo/Journal\"/>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" +
					"<core:Title>Seminars in nuclear medicine</core:Title>" +
					"<rdfs:label>Seminars in nuclear medicine</rdfs:label>" +
					"<bibo:ISSN>1558-4623</bibo:ISSN>" +
					"<core:publicationVenueFor rdf:resource=\"http://vivoweb.org/pubmed/article/pmid23656776\"/>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivoweb.org/pubmed/article/pmid23656776/dateCreated\">" +
					"<core:Year>\"2010\"</core:Year>" +
					"<core:Month>\"02\"</core:Month>" +
					"<core:Day>\"01\"</core:Day>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivoweb.org/pubmed/article/pmid23656776/dateCompleted\">" +
					"<core:Year>2010</core:Year>" +
					"<core:Month>07</core:Month>" +
					"<core:Day>08</core:Day>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/d78212990\">" +
					"<rdfs:label>harvested mans</rdfs:label>" +
					"<score:ufid>78212990</score:ufid>" +
					"<foaf:firstName>Guy</foaf:firstName>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
				"</rdf:Description>" +
			"</rdf:RDF>";
		
		// Create vivo rdf data
		this.vivoRDF = ""+
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<rdf:RDF xmlns:j.0=\"http://aims.fao.org/aos/geopolitical.owl#\" " +
					"xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" " +
					"xmlns:event=\"http://purl.org/NET/c4dm/event.owl#\" " +
					"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
					"xmlns:owl2=\"http://www.w3.org/2006/12/owl2-xml#\" " +
					"xmlns:core=\"http://vivoweb.org/ontology/core#\" " +
					"xmlns:ufVIVO=\"http://vivo.ufl.edu/ontology/vivo-ufl/\" " +
					"xmlns:swrlb=\"http://www.w3.org/2003/11/swrlb#\" " +
					"xmlns:vann=\"http://purl.org/vocab/vann/\" " +
					"xmlns:j.1=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" " +
					"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
					"xmlns:bibo=\"http://purl.org/ontology/bibo/\" " +
					"xmlns:afn=\"http://jena.hpl.hp.com/ARQ/function#\" " +
					"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " +
					"xmlns:swvs=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\" " +
					"xmlns:owl=\"http://www.w3.org/2002/07/owl#\" " +
					"xmlns:dcterms=\"http://purl.org/dc/terms/\" " +
					"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" " +
					"xmlns:swrl=\"http://www.w3.org/2003/11/swrl#\" " +
					"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
				"<rdf:Description rdf:about=\"http://vivo.mydomain.edu/individual/n3574\">" +
					"<core:workEmail>v@ufl.edu</core:workEmail>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>" +
					"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
					"<j.1:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</j.1:moniker>" +
					"<j.1:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</j.1:modTime>" +
					"<foaf:firstName>Guy</foaf:firstName>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
					"<ufVIVO:ufid>78212990</ufVIVO:ufid>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivo.mydomain.edu/individual/n3576\">" +
					"<core:workEmail>v@ufl.edu</core:workEmail>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>" +
					"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
					"<j.1:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</j.1:moniker>" +
					"<j.1:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</j.1:modTime>" +
					"<foaf:firstName>Guy</foaf:firstName>" +
					"<core:middleName>J</core:middleName>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
					"<ufVIVO:ufid>78212990</ufVIVO:ufid>" +
				"</rdf:Description>" +
				"<rdf:Description rdf:about=\"http://vivo.mydomain.edu/individual/n3573\">" +
					"<core:workEmail>vgirl@ufl.edu</core:workEmail>" +
					"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>" +
					"<core:middleName>J</core:middleName>" +
					"<rdfs:label xml:lang=\"en-US\">Fawkes, Girl</rdfs:label>" +
					"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
					"<j.1:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</j.1:moniker>" +
					"<j.1:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</j.1:modTime>" +
					"<foaf:firstName>Girl</foaf:firstName>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
					"<ufVIVO:ufid>58454252</ufVIVO:ufid>" +
				"</rdf:Description>" +
			"</rdf:RDF>";
		
		// create VIVO.xml
		try {
			this.vivoXML = File.createTempFile("scoretest_vivo", ".xml");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.vivoXML));
			out.write(""+
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Model>" +
				"<Param name=\"type\">sdb</Param>" +
				"<Param name=\"dbLayout\">layout2</Param>" +
				"<Param name=\"dbClass\">org.h2.Driver</Param>" +
				"<Param name=\"dbType\">H2</Param>" +
				"<Param name=\"dbUrl\">jdbc:h2:mem:test</Param>" +
				"<Param name=\"modelName\">testVivoModel</Param>" +
				"<Param name=\"dbUser\">sa</Param>" +
				"<Param name=\"dbPass\"></Param>" +
				"</Model>"
			);
			out.close();
		} catch(IOException e) {
			log.error(e.getMessage(), e);
		}
		
		// inputs
		this.iArg = this.vivoXML.getAbsolutePath();
		this.vArg = this.vivoXML.getAbsolutePath();
		
		// outputs
		this.oArg = this.vivoXML.getAbsolutePath();
		
		// model overrides
		this.overrideIArg = "modelName=testInputModel";
		this.overrideIArgMap = new HashMap<String, String>();
		this.overrideIArgMap.put("modelName", "testInputModel");
		
		this.overrideOArg = "modelName=testOutputModel";
		this.overrideOArgProp = new HashMap<String, String>();
		this.overrideOArgProp.put("modelName", "testOutputModel");
		
		this.overrideVArg = "modelName=testVivoModel";
		this.overrideVArgProp = new HashMap<String, String>();
		this.overrideVArgProp.put("modelName", "testVivoModel");
	}

	@Override
	protected void tearDown() {
		// remove temp files
		this.scoreInput = null;
		this.vivoRDF = null;
		this.vivoXML.delete();
		this.vivo.truncate();
		this.input.truncate();
		this.output.truncate();
	}
}
