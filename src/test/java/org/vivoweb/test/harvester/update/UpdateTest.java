package org.vivoweb.test.harvester.update;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.update.Update;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;
import com.hp.hpl.jena.rdf.model.Model;

/**
 *
 */
public class UpdateTest extends TestCase {

	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(UpdateTest.class);
	/**
	 * previous harvester rdf statements to load for test
	 */
	private String previousRDF;
	/**
	 * incoming harvester rdf statements to load for test
	 */
	private String incomingRDF;
	/**
	 * expected rdf statements to load for test
	 */
	private String expectedPrevModelString;
	
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(UpdateTest.class);
		
		this.previousRDF = ""+
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
						"xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" " +
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
						"<core:middleName>J</core:middleName>" +
						"<foaf:firstName>Guy</foaf:firstName>" +
						"<foaf:lastName>Fawkes</foaf:lastName>" +
						"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
						"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
						"<vitro:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</vitro:moniker>" +
						"<vitro:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</vitro:modTime>" +
						"<ufVIVO:ufid>78212990</ufVIVO:ufid>" +
					"</rdf:Description>" +
				"</rdf:RDF>";
	
		// Create incoming rdf
		// differences 
		//		changed email address
		// 		removal of middlename
		//		addition of prefix
		this.incomingRDF = ""+
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
						"xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" " +
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
						"<core:workEmail>v1105@ufl.edu</core:workEmail>" +
						"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>" +
						"<foaf:firstName>Guy</foaf:firstName>" +
						"<foaf:lastName>Fawkes</foaf:lastName>" +
						"<foaf:prefix>Mr.</foaf:prefix>" +
						"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
						"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
						"<vitro:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</vitro:moniker>" +
						"<vitro:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</vitro:modTime>" +
						"<ufVIVO:ufid>78212990</ufVIVO:ufid>" +
					"</rdf:Description>" +
				"</rdf:RDF>";
		
		this.expectedPrevModelString = "<ModelCom   {http://vivo.mydomain.edu/individual/n3574 @foaf:prefix \"Mr.\"; http://vivo.mydomain.edu/individual/n3574 @rdf:type core:FacultyMember; http://vivo.mydomain.edu/individual/n3574 @core:workEmail \"v1105@ufl.edu\"; http://vivo.mydomain.edu/individual/n3574 @foaf:firstName \"Guy\"; http://vivo.mydomain.edu/individual/n3574 @foaf:lastName \"Fawkes\"; http://vivo.mydomain.edu/individual/n3574 @rdfs:label \"Fawkes, Guy\"@en-US; http://vivo.mydomain.edu/individual/n3574 @rdf:type j.1:Flag1ValueThing; http://vivo.mydomain.edu/individual/n3574 @j.1:moniker \"Faculty Member\"^^http://www.w3.org/2001/XMLSchema#string; http://vivo.mydomain.edu/individual/n3574 @j.1:modTime \"2010-08-09T15:46:21\"^^http://www.w3.org/2001/XMLSchema#dateTime; http://vivo.mydomain.edu/individual/n3574 @ufVIVO:ufid \"78212990\"} | >";
	}
	
	/**
	 * @throws IOException error
	 * @throws ClassNotFoundException error
	 */
	public void testUpdate() throws IOException, ClassNotFoundException{
		log.info("BEGIN testUpdate");
		JenaConnect previousJC = new SDBJenaConnect("jdbc:h2:mem:test", "sa", "", "HSQLDB", "org.h2.Driver", "layout2", "hr20101101");
		previousJC.loadRdfFromString(this.previousRDF, null, null);
		
		JenaConnect incomingJC = previousJC.neighborConnectClone("hr20101104");
		incomingJC.loadRdfFromString(this.incomingRDF, null, null);
		
		log.debug("prevModel - pre update\n"+previousJC.exportRdfToString());
		log.debug("incomingModel - pre update\n"+incomingJC.exportRdfToString());
		
		//testing new items
		new Update(previousJC, incomingJC, null, false, false, false).execute();
		
		log.debug("prevModel - post update\n"+previousJC.exportRdfToString());
		log.debug("incomingModel - post update\n"+incomingJC.exportRdfToString());
		
		Model expectedPrevious = previousJC.getJenaModel().difference(new MemJenaConnect(new ByteArrayInputStream(this.incomingRDF.getBytes()), null, null).getJenaModel());
		assertEquals(this.expectedPrevModelString, expectedPrevious.toString());
		log.info("END testUpdate");
	}

	@Override
	protected void tearDown() throws Exception {
		this.incomingRDF = null;
		this.previousRDF = null;
		this.expectedPrevModelString = null;
	}

}
