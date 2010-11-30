/**
 * 
 */
package org.vivoweb.test.harvester.diff;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.diff.Diff;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;

/**
 * @author drspeedo
 *
 */
public class DiffTest extends TestCase {

	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(DiffTest.class);
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
	private String expectedAddRDF;
	/**
	 * vivo test configuration
	 */
	private String expectedSubRDF;

	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(DiffTest.class);
		// Create vivo rdf
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
						"<core:middleName>J</core:middleName>" +
						"<foaf:firstName>Guy</foaf:firstName>" +
						"<foaf:lastName>Fawkes</foaf:lastName>" +
						"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
						"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
						"<j.1:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</j.1:moniker>" +
						"<j.1:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</j.1:modTime>" +
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
						"<core:workEmail>v1105@ufl.edu</core:workEmail>" +
						"<rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>" +
						"<foaf:firstName>Guy</foaf:firstName>" +
						"<foaf:lastName>Fawkes</foaf:lastName>" +
						"<foaf:prefix>Mr.</foaf:prefix>" +
						"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
						"<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1ValueThing\"/>" +
						"<j.1:moniker rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Faculty Member</j.1:moniker>" +
						"<j.1:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2010-08-09T15:46:21</j.1:modTime>" +
						"<ufVIVO:ufid>78212990</ufVIVO:ufid>" +
					"</rdf:Description>" +
				"</rdf:RDF>";
		
		this.expectedAddRDF = ""+
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<rdf:RDF></rdf:RDF>";
		
		this.expectedSubRDF = ""+
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<rdf:RDF></rdf:RDF>";
	}
	
	/**
	 * @throws IOException error
	 */
	public void testDiff() throws IOException{
		log.info("BEGIN testDiff");

		JenaConnect diffJC;
		try {
			diffJC = new SDBJenaConnect("jdbc:h2:mem:test", "sa", "", "HSQLDB", "org.h2.Driver", "layout2", "diff");
		} catch(ClassNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		}
		
		JenaConnect previousJC = diffJC.neighborConnectClone("hr20101101");
		previousJC.loadRdfFromString(this.previousRDF, null, null);

		JenaConnect incomingJC = diffJC.neighborConnectClone("hr20101104");
		incomingJC.loadRdfFromString(this.incomingRDF, null, null);
					
		//System.out.println("prevModel");
		//previousJC.exportRDF(System.out);
		
		//System.out.println("incomingModel");
		//incomingJC.exportRDF(System.out);
		
					
		//testing new items
		Diff.diff(incomingJC, previousJC, diffJC, null);
		
		assertEquals(this.expectedAddRDF, diffJC.getJenaModel().difference(new MemJenaConnect(new ByteArrayInputStream(this.expectedAddRDF.getBytes()), null, null).getJenaModel()).toString());
		
		diffJC.truncate();
								
		//testing old items
		Diff.diff(previousJC, incomingJC, diffJC, null);

		assertEquals(this.expectedSubRDF, diffJC.getJenaModel().difference(new MemJenaConnect(new ByteArrayInputStream(this.expectedSubRDF.getBytes()), null, null).getJenaModel()).toString());
		//testing delete items
		log.info("END testDiff");
	}

	@Override
	protected void tearDown() throws Exception {
		this.incomingRDF = null;
		this.previousRDF = null;
	}

}
