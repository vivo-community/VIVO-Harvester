/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.test.harvester.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.diff.Diff;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import junit.framework.TestCase;

/**
 * @author Chris Haines hainesc@ufl.edu
 */
public class DiffTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(DiffTest.class);
	/** */
	private JenaConnect original;
	/** */
	private JenaConnect incoming;
	/** */
	private JenaConnect output;
	/** */
	private List<Statement> shareStatements;
	/** */
	private List<Statement> subStatements;
	/** */
	private List<Statement> addStatements;
	/** */
	//private SDBJenaConnect input;
	private JenaConnect input;
	/** */
	private JenaConnect prevHarvest;
	/** */
	//private TDBJenaConnect temp;
	
	/**
	 * previousHarvest model will be stored here
	 */
	protected static final String prevHarvestRDF = "" +
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<rdf:RDF xmlns:j.0=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\" " +
			"xmlns:j.1=\"http://vivo.ufl.edu/ontology/vivo-ufl/\" " + 
			"xmlns:j.2=\"http://purl.org/ontology/bibo/\" " +
			"xmlns:j.3=\"http://xmlns.com/foaf/0.1/\" " +
			"xmlns:j.4=\"http://vivoweb.org/ontology/core#\" " +
			"xmlns:owl=\"http://www.w3.org/2002/07/owl#\" " +
			"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" " +
			"xmlns:owlPlus=\"http://www.w3.org/2006/12/owl2-xml#\" " +
			"xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" " +
			"xmlns:skos=\"http://www.w3.org/2008/05/skos#\" " +
			"xmlns:vocab=\"http://purl.org/vocab/vann/\" " +
			"xmlns:swvocab=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\" " +
			"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
			"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
			"xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\"> " +
		"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n1836184267\">"+
			"<j.4:faxNumber></j.4:faxNumber>" +
			"<j.1:Deceased>N</j.1:Deceased>" +
			"<j.4:middleName>J</j.4:middleName>" +
			"<j.1:harvestedBy>PeopleSoft-BizTalk-Harvester</j.1:harvestedBy>" +
			"<rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>" +
			"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#NonAcademic\"/>" +
			"<j.4:primaryEmail>vincent.sposato@ufl.edu</j.4:primaryEmail>" +
			"<j.4:primaryEmail>vincent.sposato@gmail.com</j.4:primaryEmail>" +
			"<j.4:primaryEmail>vincent.sposato.jr@gmail.com</j.4:primaryEmail>" +
			"<j.1:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">vsposato</j.1:gatorlink>" +
			"<j.3:firstName>Vincent</j.3:firstName>" +
			"<j.3:lastName>Sposato</j.3:lastName>" +
			"<j.1:ufid>83117145</j.1:ufid>" +
			"<j.1:privacy>N</j.1:privacy>" +
			"<rdfs:label>Sposato,Vincent J</rdfs:label>"+
			"<j.4:primaryPhoneNumber>(904) 716-3289</j.4:primaryPhoneNumber>" +
			"<j.4:primaryPhoneNumber>(123) 456-7890</j.4:primaryPhoneNumber>" +
			"<j.4:primaryPhoneNumber>(123) 456-7890 7890</j.4:primaryPhoneNumber>" +
			"<j.1:homeDept rdf:resource=\"http://vivo.ufl.edu/individual/n1581870954\"/>" +
			"<j.4:preferredTitle>IT Expert, Sr. Software Engineer</j.4:preferredTitle>" +
			"</rdf:Description>" +
		"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n78212990\">"+
			"<j.4:faxNumber></j.4:faxNumber>" +
			"<j.1:Deceased>N</j.1:Deceased>" +
			"<j.4:middleName>J</j.4:middleName>" +
			"<j.1:harvestedBy>PeopleSoft-BizTalk-Harvester</j.1:harvestedBy>" +
			"<rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>" +
			"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#NonAcademic\"/>" +
			"<j.4:primaryEmail>svwilliams@gmail.com</j.4:primaryEmail>" +
			"<j.1:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">drspeedo</j.1:gatorlink>" +
			"<j.3:firstName>Stephen</j.3:firstName>" +
			"<j.3:lastName>Williams</j.3:lastName>" +
			"<j.1:ufid>78212990</j.1:ufid>" +
			"<j.1:privacy>N</j.1:privacy>" +
			"<rdfs:label>Williams,Stephen V</rdfs:label>"+
			"<j.4:primaryPhoneNumber>(352) 256-2529</j.4:primaryPhoneNumber>" +
			"<j.1:homeDept rdf:resource=\"http://vivo.ufl.edu/individual/n1581870954\"/>" +
			"<j.4:preferredTitle>IT Senior, Sr. Software Engineer and Team Lead</j.4:preferredTitle>" +
		"</rdf:Description>" +
		"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n44444321\">"+
			"<j.4:faxNumber></j.4:faxNumber>" +
			"<j.1:Deceased>N</j.1:Deceased>" +
			"<j.4:middleName>J</j.4:middleName>" +
			"<j.1:harvestedBy>PeopleSoft-BizTalk-Harvester</j.1:harvestedBy>" +
			"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Faculty\"/>" +
			"<j.4:primaryEmail>tlogan@ufl.edu</j.4:primaryEmail>" +
			"<j.1:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">tlogan</j.1:gatorlink>" +
			"<j.3:firstName>Theodore</j.3:firstName>" +
			"<j.3:lastName>Logan</j.3:lastName>" +
			"<j.1:ufid>44444321</j.1:ufid>" +
			"<j.1:privacy>N</j.1:privacy>" +
			"<rdfs:label>Logan Esq,Theodore </rdfs:label>"+
			"<j.4:primaryPhoneNumber>(423) 867-5309</j.4:primaryPhoneNumber>" +
			"<j.1:homeDept rdf:resource=\"http://vivo.ufl.edu/individual/n1581870954\"/>" +
			"<j.4:preferredTitle>High Broseph</j.4:preferredTitle>" +
		"</rdf:Description>" +
			"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n55555321\">"+
			"<j.4:faxNumber></j.4:faxNumber>" +
			"<j.1:Deceased>N</j.1:Deceased>" +
			"<j.4:middleName>S</j.4:middleName>" +
			"<j.1:harvestedBy>PeopleSoft-BizTalk-Harvester</j.1:harvestedBy>" +
			"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Faculty\"/>" +
			"<j.4:primaryEmail>bpreston@ufl.edu</j.4:primaryEmail>" +
			"<j.1:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">bpreston</j.1:gatorlink>" +
			"<j.3:firstName>William</j.3:firstName>" +
			"<j.3:lastName>Preston</j.3:lastName>" +
			"<j.1:ufid>55555321</j.1:ufid>" +
			"<j.1:privacy>N</j.1:privacy>" +
			"<rdfs:label>Preston, William</rdfs:label>"+
			"<j.4:primaryPhoneNumber>(423) 867-4444</j.4:primaryPhoneNumber>" +
			"<j.1:homeDept rdf:resource=\"http://vivo.ufl.edu/individual/n1581870954\"/>" +
			"<j.4:preferredTitle>King Broseph</j.4:preferredTitle>" +
		"</rdf:Description>" +
		"</rdf:RDF>";
	/**
	 * inputModel will be stored here
	 */
	protected static final String inputRDF = "" +
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<rdf:RDF xmlns:j.0=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\" " +
			"xmlns:j.1=\"http://vivo.ufl.edu/ontology/vivo-ufl/\" " + 
			"xmlns:j.2=\"http://purl.org/ontology/bibo/\" " +
		    "xmlns:j.3=\"http://xmlns.com/foaf/0.1/\" " +
		    "xmlns:j.4=\"http://vivoweb.org/ontology/core#\" " +
		    "xmlns:owl=\"http://www.w3.org/2002/07/owl#\" " + 
		    "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" " +
			"xmlns:owlPlus=\"http://www.w3.org/2006/12/owl2-xml#\" " +
			"xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" " +
			"xmlns:skos=\"http://www.w3.org/2008/05/skos#\" " +
			"xmlns:vocab=\"http://purl.org/vocab/vann/\" " +
			"xmlns:swvocab=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\" " +
			"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
			"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
			"xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\"> " +
		"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n1836184267\">"+
			"<j.4:faxNumber></j.4:faxNumber>" +
			"<j.1:Deceased>N</j.1:Deceased>" +
			"<j.4:middleName>J</j.4:middleName>" +
			"<j.1:harvestedBy>PeopleSoft-BizTalk-Harvester</j.1:harvestedBy>" +
			"<rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>" +
			"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#NonAcademic\"/>" +
			"<j.4:primaryEmail>vsposato@ufl.edu</j.4:primaryEmail>" +
			"<j.1:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">vsposato</j.1:gatorlink>" +
			"<j.3:firstName>Vincent</j.3:firstName>" +
			"<j.3:lastName>Sposato</j.3:lastName>" +
			"<j.1:ufid>83117145</j.1:ufid>" +
			"<j.1:privacy>N</j.1:privacy>" +
			"<rdfs:label>Sposato,Vincent J</rdfs:label>"+
			"<j.4:primaryPhoneNumber>(352) 294-5274   45274</j.4:primaryPhoneNumber>" +
			"<j.1:homeDept rdf:resource=\"http://vivo.ufl.edu/individual/n1581870954\"/>" +
			"<j.4:preferredTitle>IT Expert, Sr. Software Engineer</j.4:preferredTitle>" +
		"</rdf:Description>" +
			"<rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n44444321\">"+
			"<j.4:faxNumber></j.4:faxNumber>" +
			"<j.1:Deceased>N</j.1:Deceased>" +
			"<j.4:middleName>J</j.4:middleName>" +
			"<j.1:harvestedBy>PeopleSoft-BizTalk-Harvester</j.1:harvestedBy>" +
			//"<rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>" +
			"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Faculty\"/>" +
			"<j.4:primaryEmail>tlogan@ufl.edu</j.4:primaryEmail>" +
			"<j.1:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">tlogan</j.1:gatorlink>" +
			"<j.3:firstName>Theodore</j.3:firstName>" +
			"<j.3:lastName>Logan</j.3:lastName>" +
			"<j.1:ufid>44444321</j.1:ufid>" +
			"<j.1:privacy>N</j.1:privacy>" +
			"<rdfs:label>Logan Esq,Theodore </rdfs:label>"+
			//"<j.4:primaryPhoneNumber>(423) 867-5309</j.4:primaryPhoneNumber>" +
			"<j.1:homeDept rdf:resource=\"http://vivo.ufl.edu/individual/n1581870954\"/>" +
			"<j.4:preferredTitle>High Broseph</j.4:preferredTitle>" +
		"</rdf:Description>" +
		"</rdf:RDF>";

	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
		Resource resA = ResourceFactory.createResource("http://test.vivoweb.org/harvester/test/diff/resA");
		Resource resB = ResourceFactory.createResource("http://test.vivoweb.org/harvester/test/diff/resB");
		Resource resC = ResourceFactory.createResource("http://test.vivoweb.org/harvester/test/diff/resC");
		Resource resD = ResourceFactory.createResource("http://test.vivoweb.org/harvester/test/diff/resD");
		Property propA = ResourceFactory.createProperty("http://test.vivoweb.org/harvester/test/diff/propA");
		Property propB = ResourceFactory.createProperty("http://test.vivoweb.org/harvester/test/diff/propB");
		Property propC = ResourceFactory.createProperty("http://test.vivoweb.org/harvester/test/diff/propC");
		Property propD = ResourceFactory.createProperty("http://test.vivoweb.org/harvester/test/diff/propD");
		this.shareStatements = new LinkedList<Statement>();
		this.shareStatements.add(new StatementImpl(resA, propA, ResourceFactory.createTypedLiteral("resApropA")));
		this.shareStatements.add(new StatementImpl(resA, propB, ResourceFactory.createTypedLiteral("resApropB")));
		this.shareStatements.add(new StatementImpl(resA, propC, ResourceFactory.createTypedLiteral("resApropC")));
		this.shareStatements.add(new StatementImpl(resB, propB, ResourceFactory.createTypedLiteral("resBpropB")));
		this.shareStatements.add(new StatementImpl(resB, propD, ResourceFactory.createTypedLiteral("resBpropD")));
		this.shareStatements.add(new StatementImpl(resC, propA, ResourceFactory.createTypedLiteral("resCpropA")));
		this.shareStatements.add(new StatementImpl(resC, propD, ResourceFactory.createTypedLiteral("resCpropD")));
		this.shareStatements.add(new StatementImpl(resD, propB, ResourceFactory.createTypedLiteral("resDpropB")));
		this.shareStatements.add(new StatementImpl(resD, propC, ResourceFactory.createTypedLiteral("resDpropC")));
		this.shareStatements.add(new StatementImpl(resD, propD, ResourceFactory.createTypedLiteral("resDpropD")));
		this.subStatements = new LinkedList<Statement>();
		this.subStatements.add(new StatementImpl(resA, propD, ResourceFactory.createTypedLiteral("resApropDold")));
		this.subStatements.add(new StatementImpl(resB, propA, ResourceFactory.createTypedLiteral("resBpropAold")));
		this.subStatements.add(new StatementImpl(resC, propC, ResourceFactory.createTypedLiteral("resCpropCold")));
		this.addStatements = new LinkedList<Statement>();
		this.addStatements.add(new StatementImpl(resA, propD, ResourceFactory.createTypedLiteral("resApropDnew")));
		this.addStatements.add(new StatementImpl(resB, propA, ResourceFactory.createTypedLiteral("resBpropAnew")));
		this.addStatements.add(new StatementImpl(resC, propB, ResourceFactory.createTypedLiteral("resCpropBnew")));
		this.original = new MemJenaConnect("original");
		this.original.getJenaModel().add(this.shareStatements);
		this.original.getJenaModel().add(this.subStatements);
		this.incoming = new MemJenaConnect("incomming");
		this.incoming.getJenaModel().add(this.shareStatements);
		this.incoming.getJenaModel().add(this.addStatements);
		this.output = new MemJenaConnect("output");

		// load input models
		this.input = new SDBJenaConnect("jdbc:h2:mem:test", "sa", "", "H2", "org.h2.Driver", "layout2", "input");
		//System.out.println(this.inputRDF.subSequence(730, 820));
		this.input.loadRdfFromString(inputRDF, null, null);
		
		// load previous harvest model
		this.prevHarvest = new SDBJenaConnect("jdbc:h2:mem:test", "sa", "", "H2", "org.h2.Driver", "layout2", "prevHarvest");
		this.prevHarvest.loadRdfFromString(prevHarvestRDF, null, null);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.original.truncate();
		this.original.close();
		this.incoming.truncate();
		this.incoming.close();
		this.output.truncate();
		this.output.close();
		this.shareStatements.clear();
		this.addStatements.clear();
		this.subStatements.clear();
		this.input.truncate();
		this.input.close();
		this.prevHarvest.truncate();
		this.prevHarvest.close();
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.diff.Diff#diff(org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, java.lang.String)}.
	 * @throws IOException error
	 */
	@SuppressWarnings("javadoc")
	public final void testDiffAdds() throws IOException {
		log.info("BEGIN testDiffAdds");
		Diff.diff(this.incoming, this.original, this.output, null, null, null, null);
		assertFalse(this.output.isEmpty());
		for(Statement sub : this.subStatements) {
			assertFalse(this.output.getJenaModel().contains(sub));
		}
		for(Statement add : this.addStatements) {
			assertTrue(this.output.getJenaModel().contains(add));
		}
		for(Statement shared : this.shareStatements) {
			assertFalse(this.output.getJenaModel().contains(shared));
		}
		log.info("END testDiffAdds");
	}

	@SuppressWarnings("javadoc") //TODO Replace with JavaDoc
	public final void testDiffPrevHarvestAdditions() throws IOException {
		log.info("BEGIN testDiffPrevHarvestAdditions");
		Diff.diff(this.input, this.prevHarvest, this.output, null, null, null, null);
		assertFalse(this.output.isEmpty());
		this.output.exportRdfToStream(System.out);
		
		/*for(Statement sub : this.subStatements) {
			assertFalse(this.output.getJenaModel().contains(sub));
		}
		for(Statement add : this.addStatements) {
			assertTrue(this.output.getJenaModel().contains(add));
		}
		for(Statement shared : this.shareStatements) {
			assertFalse(this.output.getJenaModel().contains(shared));
		}*/
		log.info("END testDiffPrevHarvestAdditions");
	}
	
	@SuppressWarnings("javadoc") //TODO Replace with JavaDoc
	public final void testDiffPrevHarvestSubtractions() throws IOException {
		log.info("BEGIN testDiffPrevHarvestSubtractions");
		Diff.diff(this.prevHarvest, this.input, this.output, null, null, null, null);
		assertFalse(this.output.isEmpty());
		this.output.exportRdfToStream(System.out);
		log.info("END testDiffPrevHarvestSubtractions");
	}
	
	@SuppressWarnings("javadoc") //TODO Replace with JavaDoc
	public final void testDiffPrevHarvestSubtractionsIgnore() throws IOException {
		log.info("BEGIN testDiffPrevHarvestSubtractionsIgnore");
		//Diff.diff(this.prevHarvest, this.input, this.output, null, null, null, null);
		List<String> preserveTypes = new ArrayList<String>();
		preserveTypes.add("http://xmlns.com/foaf/0.1/Person");
		preserveTypes.add("http://vivoweb.org/ontology/core#Faculty");
		
		Diff differ = new Diff(this.prevHarvest, this.input, this.output, null, null, null, 
			null, preserveTypes);
		differ.execute();

		assertFalse(this.output.isEmpty());
		assertTrue(this.output.containsURI("http://vivo.ufl.edu/individual/n1836184267"));
		assertFalse(this.output.containsURI("http://vivo.ufl.edu/individual/n78212990"));
		log.info("END testDiffPrevHarvestSubtractionsIgnore");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.diff.Diff#diff(org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, java.lang.String)}.
	 * @throws IOException error
	 */
	@SuppressWarnings("javadoc")
	public final void testDiffSubs() throws IOException {
		log.info("BEGIN testDiffSubs");
		Diff.diff(this.original, this.incoming, this.output, null, null, null, null);
		assertFalse(this.output.isEmpty());
		for(Statement sub : this.subStatements) {
			assertTrue(this.output.getJenaModel().contains(sub));
		}
		for(Statement add : this.addStatements) {
			assertFalse(this.output.getJenaModel().contains(add));
		}
		for(Statement shared : this.shareStatements) {
			assertFalse(this.output.getJenaModel().contains(shared));
		}
		log.info("END testDiffSubs");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.diff.Diff#diff(org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, java.lang.String)}.
	 * @throws IOException error
	 */
	@SuppressWarnings("javadoc")
	public final void testDiffSame() throws IOException {
		log.info("BEGIN testDiffSame");
		Diff.diff(this.original, this.original, this.output, null, null, null, null);
		assertTrue(this.output.isEmpty());
		for(Statement sub : this.subStatements) {
			assertFalse(this.output.getJenaModel().contains(sub));
		}
		for(Statement add : this.addStatements) {
			assertFalse(this.output.getJenaModel().contains(add));
		}
		for(Statement shared : this.shareStatements) {
			assertFalse(this.output.getJenaModel().contains(shared));
		}
		log.info("END testDiffSame");
	}
	
	/**
	 * Testing Output to XML/RDF
	 * @throws IOException JenaConnect
	 */
	public final void testDiffDumpFile() throws IOException {
		log.info("Begin testDiffDumpFile");
		Diff.diff(this.original, this.incoming, this.output, null, null, null, null);
		//TODO:  Test Ouput File Creation for all three types
		log.info("End testDiffDumpFile");
	}
	
}
