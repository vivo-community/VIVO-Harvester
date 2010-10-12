/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.ingest.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException; // import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.vivoweb.ingest.score.Score;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.util.StringUtils;
import junit.framework.TestCase;

/**
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class ScoreTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ScoreTest.class);
	/**
	 * Score input test file
	 */
	private File scoreInput;
	/**
	 * vivo test configuration file
	 */
	private File vivoXML;
	/**
	 * vivo rdf statements to load for test
	 */
	private File vivoRDF;
	
	/**
	 * Test Argument parsing for scoring
	 */
	public void testArguments() {
		String[] args;
		Score Test;
		
		// inputs
		String iArg = this.vivoXML.getAbsolutePath();
		String vArg = this.vivoXML.getAbsolutePath();
		
		// outputs
		String oArg = this.vivoXML.getAbsolutePath();
		
		// model overrides
		String IArg = "modelName=testInputModel";
		Properties IArgProp = new Properties();
		IArgProp.put("modelName", "testInputModel");
		
		String OArg = "modelName=testOutputModel";
		Properties OArgProp = new Properties();
		OArgProp.put("modelName", "testOutputModel");
		
		String VArg = "modelName=testVivoModel";
		Properties VArgProp = new Properties();
		VArgProp.put("modelName", "testVivoModel");
		
		JenaConnect input;
		JenaConnect output;
		JenaConnect vivo;
		
		// Load up everything before starting
		try {
			input = JenaConnect.parseConfig(iArg, IArgProp);
			input.loadRDF(VFS.getManager().toFileObject(this.scoreInput).getContent().getInputStream(), null);
			
			vivo = JenaConnect.parseConfig(vArg, VArgProp);
			vivo.loadRDF(VFS.getManager().toFileObject(this.vivoRDF).getContent().getInputStream(), null);
			
			output = JenaConnect.parseConfig(oArg, OArgProp);
			
			log.info("testArguments Start");
			log.info("Testing good configs");
			
			log.info("Test -i iArg -v vArg -o oArg -a 1 -e workEmail");
			args = new String[]{"-i", iArg, "-v", vArg, "-o", oArg, "-a", "1", "-e", "workEmail"};
			log.info(StringUtils.join(" ", args));
			try {
				Test = new Score(args);
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
			log.info("Test -v vArg -a 1");
			args = new String[]{"-v", vArg, "-a", "1"};
			log.info(StringUtils.join(" ", args));
			try {
				Test = new Score(args);
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
			log.info("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg -a 1 -e workEmail");
			args = new String[]{"-i", iArg, "-I", IArg, "-v", vArg, "-V", VArg, "-o", oArg, "-O", OArg, "-a", "1", "-e", "workEmail"};
			log.info(StringUtils.join(" ", args));
			try {
				Test = new Score(args);
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
			log.info("Testing bad configs");
			log.info("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg -Q");
			args = new String[]{"-i", iArg, "-I", IArg, "-v", vArg, "-V", VArg, "-o", oArg, "-O", OArg, "-Q"};
			log.info(StringUtils.join(" ", args));
			try {
				Test = new Score(args);
				log.error("Invalid arguement passed -- score object invalid");
				fail("Invalid arguement passed -- score object invalid");
			} catch(Exception e) {
				// we want exception
			}
			
			log.info("Testing keep working model");
			// keep input model
			
			log.info("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg");
			args = new String[]{"-i", iArg, "-I", IArg, "-v", vArg, "-V", VArg, "-o", oArg, "-O", OArg};
			log.info(StringUtils.join(" ", args));
			try {
				Test = new Score(args);
				Test.execute();
				if(Test.getScoreInput().getJenaModel().isEmpty()) {
					log.error("Model emptied despite -w arg missing");
					fail("Model emptied despite -w arg missing");
				}
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
			// don't keep input model
			log.info("Testing don't keep working model");
			log.info("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArgl -w");
			args = new String[]{"-i", iArg, "-I", IArg, "-v", vArg, "-V", VArg, "-o", oArg, "-O", OArg, "-w"};
			log.info(StringUtils.join(" ", args));
			try {
				Test = new Score(args);
				Test.execute();
				if(!Test.getScoreInput().getJenaModel().isEmpty()) {
					log.error("Model not empty -w arg violated");
					fail("Model not empty -w arg violated");
				}
				Test.close();
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
			input.close();
			vivo.close();
			output.close();
		} catch(IOException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} catch(SAXException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		}
		
		log.info("testArguments End");
	}
	
	/**
	 * Test Scoring Algorithms
	 */
	public void testAlgorithims() {
		Score Test;
		List<String> workEmail = Arrays.asList("workEmail");
		List<String> blank = Arrays.asList();
		JenaConnect input;
		JenaConnect output;
		JenaConnect vivo;
		
		// load input models
		try {
			Properties inputProp = new Properties();
			inputProp.put("modelName", "input");
			input = JenaConnect.parseConfig(this.vivoXML, inputProp);
			input.loadRDF(VFS.getManager().toFileObject(this.scoreInput).getContent().getInputStream(), null);
			
			Properties vivoProp = new Properties();
			vivoProp.put("modelName", "vivo");
			vivo = JenaConnect.parseConfig(this.vivoXML, vivoProp);
			vivo.loadRDF(VFS.getManager().toFileObject(this.vivoRDF).getContent().getInputStream(), null);
			
			Properties outputProp = new Properties();
			outputProp.put("modelName", "output");
			output = JenaConnect.parseConfig(this.vivoXML, outputProp);
			
			// run author score
			Test = new Score(input, vivo, output, false, blank, blank, blank, "1", null, null, null);
			Test.execute();
			
			// check output model
			if(Test.getScoreOutput().getJenaModel().isEmpty()) {
				log.error("Didn't match anything with author name scoring");
				fail("Didn't match anything with author name scoring");
			}
			
			// empty output model
			Test.getScoreOutput().getJenaModel().removeAll();
			
			// run exactmatch score
			Test = new Score(input, vivo, output, false, workEmail, blank, blank, null, null, null, null);
			Test.execute();
			
			// check output model
			if(output.getJenaModel().isEmpty()) {
				log.error("Didn't match anything with exact match scoring");
				fail("Didn't match anything with exact match scoring");
			}
			
			// empty output model
			Test.getScoreOutput().getJenaModel().removeAll();
			
			// testing Foriegn Key Score Method
			input.getJenaModel().write(System.out, "RDF/XML");
			
			Score.main(new String[]{"-v", this.vivoXML.getAbsolutePath(), "-I", "modelName=input", "-O", "modelName=output", "-V", "modelName=vivo", "-f", "http://vivoweb.org/ontology/score#ufid=http://vivo.ufl.edu/ontology/vivo-ufl/ufid", "-x", "http://vivoweb.org/ontology/core#worksFor", "-y", "http://vivoweb.org/ontology/core#departmentOf"});
			
			StmtIterator stmnts = Test.getScoreOutput().getJenaModel().listStatements();
			while(stmnts.hasNext()) {
				Statement stmnt = stmnts.next();
				System.out.println("Statement Found");
				System.out.println(" - sub: " + stmnt.getSubject().getURI());
				System.out.println(" - pre: " + stmnt.getPredicate());
				System.out.println(" - obj: " + stmnt.getObject());
			}
			
			// check output model
			if(output.getJenaModel().isEmpty()) {
				log.error("Didn't match anything with foriegn key scoring");
				fail("Didn't match anything with foriegn key scoring");
			}
			
			Test.close();
			input.close();
			vivo.close();
			output.close();
		} catch(IOException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} catch(SAXException e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}
	
	/**
	 * Called before every test case method.
	 */
	@Override
	protected void setUp() {
		
		// create objects under test
		// Create input rdf file -- Stanley Goldsmith :-) pubmed id 20113680
		try {
			this.scoreInput = File.createTempFile("scoretest_input", ".rdf");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.scoreInput));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<rdf:RDF xmlns:bibo=\"http://purl.org/ontology/bibo/\" " + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" " + "xmlns:owlPlus=\"http://www.w3.org/2006/12/owl2-xml#\" " + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" " + "xmlns:skos=\"http://www.w3.org/2008/05/skos#\" " + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\" " + "xmlns:vocab=\"http://purl.org/vocab/vann/\" " + "xmlns:swvocab=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\" " + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:core=\"http://vivoweb.org/ontology/core#\" " + "xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" " + "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " + "xmlns:score=\"http://vivoweb.org/ontology/score#\">" + "<rdf:Description rdf:about=\"http://vivoweb.org/pubMed/article/pmid20113680\">" + "<rdf:type rdf:resource=\"http://purl.org/ontology/bibo/Document\"/>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<bibo:pmid>20113680</bibo:pmid>" + "<rdfs:label>Radioimmunotherapy of lymphoma: Bexxar and Zevalin.</rdfs:label>" + "<core:Title>Radioimmunotherapy of lymphoma: Bexxar and Zevalin.</core:Title>" + "<score:Affiliation>Division of Nuclear Medicine, New York-Presbyterian Hospital, Weill Cornell College of Medicine, New York, NY 10065, USA. sjg2002@med.cornell.edu</score:Affiliation>" + "<bibo:volume>40</bibo:volume>" + "<bibo:number>2</bibo:number>" + "<core:Year>2010</core:Year>" + "<score:workEmail>sjg2002@med.cornell.edu</score:workEmail>" + "<core:informationResourceInAuthorship rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680/authorship1\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh1\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh2\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh3\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh4\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh5\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh6\"/>" + "<core:hasSubjectArea rdf:nodeID=\"pmid20113680mesh7\"/>" + "<core:hasPublicationVenue rdf:resource=\"http://vivoweb.org/pubMed/journal/j1558-4623\"/>" + "<score:hasCreateDate rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680/dateCreated\"/>" + "<score:hasCompleteDate rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680/dateCompleted\"/>" + "</rdf:Description>" + "<rdf:Description rdf:about=\"http://vivoweb.org/pubMed/article/pmid20113680/authorship1\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Authorship\"/>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#DependentResource\"/>" + "<core:linkedAuthor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680/author1\"/>" + "<core:linkedInformationResource rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<core:authorRank rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</core:authorRank>" + "</rdf:Description>" + "<rdf:Description rdf:about=\"http://vivoweb.org/pubMed/article/pmid20113680/author1\">" + "<rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>" + "<rdfs:label>Goldsmith, Stanley J</rdfs:label>" + "<foaf:lastName>Goldsmith</foaf:lastName>" + "<score:foreName>Stanley J</score:foreName>" + "<score:initials>SJ</score:initials>" + "<score:suffix/>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<core:authorInAuthorship rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680/authorship1\"/>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh1\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Animals</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Animals</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier/>" + "<score:QualifierIsMajorTerm/>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh2\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Antibodies, Monoclonal</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Antibodies, Monoclonal</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier>adverse effects therapeutic use</score:Qualifier>" + "<score:QualifierIsMajorTerm>N Y</score:QualifierIsMajorTerm>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh3\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Humans</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Humans</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier/>" + "<score:QualifierIsMajorTerm/>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh4\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Lymphoma</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Lymphoma</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier>radiotherapy therapy</score:Qualifier>" + "<score:QualifierIsMajorTerm>Y N</score:QualifierIsMajorTerm>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh5\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Nuclear Medicine</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Nuclear Medicine</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier/>" + "<score:QualifierIsMajorTerm/>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh6\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Radioimmunotherapy</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Radioimmunotherapy</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier>adverse effects methods</score:Qualifier>" + "<score:QualifierIsMajorTerm>N Y</score:QualifierIsMajorTerm>" + "</rdf:Description>" + "<rdf:Description rdf:nodeID=\"pmid20113680mesh7\">" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/score#MeshTerm\"/>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#SubjectArea\"/>" + "<core:SubjectAreaFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<rdfs:label>Treatment Outcome</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<score:meshTermOf rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "<score:Descriptor>Treatment Outcome</score:Descriptor>" + "<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>" + "<score:Qualifier/>" + "<score:QualifierIsMajorTerm/>" + "</rdf:Description>" + "<rdf:Description rdf:about=\"http://vivoweb.org/pubMed/journal/j1558-4623\">" + "<rdf:type rdf:resource=\"http://purl.org/ontology/bibo/Journal\"/>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing\"/>" + "<core:Title>Seminars in nuclear medicine</core:Title>" + "<rdfs:label>Seminars in nuclear medicine</rdfs:label>" + "<bibo:ISSN>1558-4623</bibo:ISSN>" + "<core:publicationVenueFor rdf:resource=\"http://vivoweb.org/pubMed/article/pmid20113680\"/>" + "</rdf:Description>" + "<rdf:Description rdf:about=\"http://vivoweb.org/pubMed/article/pmid20113680/dateCreated\">" + "<core:Year>\"2010\"</core:Year>" + "<core:Month>\"02\"</core:Month>" + "<core:Day>\"01\"</core:Day>" + "</rdf:Description>" + "<rdf:Description rdf:about=\"http://vivoweb.org/pubMed/article/pmid20113680/dateCompleted\">" + "<core:Year>2010</core:Year>" + "<core:Month>07</core:Month>" + "<core:Day>08</core:Day>" + "</rdf:Description>" + "<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/d78212990\">" + "<rdfs:label>ingested mans</rdfs:label>" + "<score:ufid>78212990</score:ufid>" + "<foaf:firstName>Stan</foaf:firstName>" + "<foaf:lastName>Goldsmith</foaf:lastName>" + "</rdf:Description>" + "</rdf:RDF>");
			out.close();
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
		}
		
		// Create vivo rdf file -- stanley goldsmith :-)
		try {
			this.vivoRDF = File.createTempFile("scoretest_vivo", ".rdf");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.vivoRDF));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<rdf:RDF xmlns:j.0=\"http://aims.fao.org/aos/geopolitical.owl#\" " + "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" " + "xmlns:event=\"http://purl.org/NET/c4dm/event.owl#\" " + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:owl2=\"http://www.w3.org/2006/12/owl2-xml#\" " + "xmlns:core=\"http://vivoweb.org/ontology/core#\" " + "xmlns:ufVIVO=\"http://vivo.ufl.edu/ontology/vivo-ufl/\" " + "xmlns:swrlb=\"http://www.w3.org/2003/11/swrlb#\" " + "xmlns:vann=\"http://purl.org/vocab/vann/\" " + "xmlns:j.1=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" " + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " + "xmlns:bibo=\"http://purl.org/ontology/bibo/\" " + "xmlns:afn=\"http://jena.hpl.hp.com/ARQ/function#\" " + "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " + "xmlns:swvs=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\" " + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\" " + "xmlns:dcterms=\"http://purl.org/dc/terms/\" " + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" " + "xmlns:swrl=\"http://www.w3.org/2003/11/swrl#\" " + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" + "<rdf:Description rdf:about=\"http://vivo.mydomain.edu/individual/n3574\">" + "<core:workEmail>sjg2002@med.cornell.edu</core:workEmail>" + "<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>" + "<core:middleName>J</core:middleName>" + "<rdfs:label xml:lang=\"en-US\">Goldsmith, Stanley</rdfs:label>" + "<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" + "<j.1:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</j.1:moniker>" + "<j.1:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</j.1:modTime>" + "<foaf:firstName>Stanley</foaf:firstName>" + "<foaf:lastName>Goldsmith</foaf:lastName>" + "<ufVIVO:ufid>78212990</ufVIVO:ufid>" + "</rdf:Description>" + "</rdf:RDF>");
			out.close();
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
		}
		
		// create VIVO.xml
		try {
			this.vivoXML = File.createTempFile("scoretest_vivo", ".xml");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.vivoXML));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Model>" + "<Param name=\"dbClass\">org.h2.Driver</Param>" + "<Param name=\"dbType\">HSQLDB</Param>" + "<Param name=\"dbUrl\">jdbc:h2:mem:test</Param>" + "<Param name=\"modelName\">testVivoModel</Param>" + "<Param name=\"dbUser\">sa</Param>" + "<Param name=\"dbPass\"></Param>" + "</Model>");
			out.close();
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
		}
	}
	
	/**
	 * Called after every test case method.
	 */
	@Override
	protected void tearDown() {
		// remove temp files
		this.scoreInput.delete();
		this.vivoXML.delete();
		this.vivoRDF.delete();
	}
}
