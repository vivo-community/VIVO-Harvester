/** */
package org.vivoweb.test.harvester.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.update.ChangeNamespace;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/** */
public class ChangeNamespaceTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ChangeNamespaceTest.class);
	/** */
	private JenaConnect model;
	/** */
	private JenaConnect vivo;
	/** */
	private String namespace;
	/** */
	private String newNamespace;
	/** */
	private List<Property> properties;
	
	@Override
	protected void setUp() throws Exception {
		InitLog.initLogger(ChangeNamespaceTest.class);
		this.namespace = "http://testChNS.vivoweb.org/individual/";
		this.newNamespace = "http://vivo.test.edu/individual/";
		this.model = new JenaConnect("jdbc:h2:mem:testChNSh2change;MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "testChNSchange");
		this.vivo = new JenaConnect("jdbc:h2:mem:testChNSh2vivo;MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "testChNSvivo");
		String vivoData = ""+
		"<rdf:RDF"+
		"\n xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+
		"\n xmlns:bibo=\"http://purl.org/ontology/bibo/\""+
		"\n xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\""+
		"\n xmlns:localVivo=\"http://vivo.test.edu/ontology/vivo-test/\""+
		"\n xmlns:core=\"http://vivoweb.org/ontology/core#\""+
		"\n xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""+
		"\n xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">"+
		"\n    <rdf:Description rdf:about=\"http://vivo.test.edu/individual/n123456\">"+
		"\n        <bibo:prefixName>Dr.</bibo:prefixName>"+
		"\n        <localVivo:loginName>john.smith</localVivo:loginName>"+
		"\n        <localVivo:uniqueId>1234567890</localVivo:uniqueId>"+
		"\n        <core:workEmail>john.smith@test.edu</core:workEmail>"+
		"\n        <core:workFax>123 098 7654</core:workFax>"+
		"\n        <core:workPhone>123 456 7890</core:workPhone>"+
		"\n        <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>"+
		"\n        <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"+
		"\n        <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Agent\"/>"+
		"\n        <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>"+
		"\n        <rdfs:label xml:lang=\"en-US\">Smith, John</rdfs:label>"+
		"\n        <foaf:firstName>Smith</foaf:firstName>"+
		"\n        <foaf:lastName>John</foaf:lastName>"+
		"\n    </rdf:Description>"+
		"\n    <rdf:Description rdf:about=\"http://vivo.test.edu/individual/n654321\">"+
		"\n        <bibo:prefixName>Dr.</bibo:prefixName>"+
		"\n        <localVivo:loginName>bob.johnson</localVivo:loginName>"+
		"\n        <localVivo:uniqueId>2345678901</localVivo:uniqueId>"+
		"\n        <core:workEmail>bob.johnson@test.edu</core:workEmail>"+
		"\n        <core:workFax>123 987 6543</core:workFax>"+
		"\n        <core:workPhone>123 345 6789</core:workPhone>"+
		"\n        <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/>"+
		"\n        <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"+
		"\n        <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Agent\"/>"+
		"\n        <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>"+
		"\n        <rdfs:label xml:lang=\"en-US\">Johnson, Bob</rdfs:label>"+
		"\n        <foaf:firstName>Johnson</foaf:firstName>"+
		"\n        <foaf:lastName>Robert</foaf:lastName>"+
		"\n    </rdf:Description>"+
		"\n    <rdf:Description rdf:about=\"http://vivo.test.edu/individual/n321456\">"+
		"\n        <localVivo:uniqueId>2345678901</localVivo:uniqueId>"+
		"\n        <rdfs:label xml:lang=\"en-US\">Johnson, Rob</rdfs:label>"+
		"\n    </rdf:Description>"+
		"\n    <rdf:Description rdf:about=\"http://vivobad.test.edu/wrong/n987456\">"+
		"\n        <localVivo:uniqueId>2345678901</localVivo:uniqueId>"+
		"\n        <rdfs:label xml:lang=\"en-US\">Johnson, Rob</rdfs:label>"+
		"\n    </rdf:Description>"+
		"\n</rdf:RDF>";
		this.vivo.loadRDF(new ByteArrayInputStream(vivoData.getBytes()), null, null);
		Resource test123 = ResourceFactory.createResource(this.namespace+"test123");
		this.model.getJenaModel().add(test123,ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label"),"Johnson, Robert");
		this.model.getJenaModel().add(test123,ResourceFactory.createProperty("http://vivo.test.edu/ontology/vivo-test/", "uniqueId"),"2345678901");
		this.model.getJenaModel().add(test123,ResourceFactory.createProperty("http://vivoweb.org/ontology/core#", "workEmail"),"robert.johnson@sci.test.edu");
		Resource test321 = ResourceFactory.createResource(this.namespace+"test321");
		this.model.getJenaModel().add(test321,ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label"),"Laplace, Philip");
		this.model.getJenaModel().add(test321,ResourceFactory.createProperty("http://vivo.test.edu/ontology/vivo-test/", "uniqueId"),"1234567891");
		this.model.getJenaModel().add(test321,ResourceFactory.createProperty("http://vivoweb.org/ontology/core#", "workEmail"),"philip.laplace@test.edu");
		Resource testBad = ResourceFactory.createResource("http://norename.blah.com/blah/testBad");
		this.model.getJenaModel().add(testBad,ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label"),"Not Renamed");
		this.model.getJenaModel().add(testBad,ResourceFactory.createProperty("http://vivo.test.edu/ontology/vivo-test/", "uniqueId"),"1234567892");
		this.model.getJenaModel().add(testBad,ResourceFactory.createProperty("http://vivoweb.org/ontology/core#", "workEmail"),"bad@blah.com");
		this.properties = Arrays.asList(ResourceFactory.createProperty("http://vivo.test.edu/ontology/vivo-test/uniqueId"));
		log.info("testing Start");
	}
	
	@Override
	protected void tearDown() throws Exception {
		log.info("testing End");
		this.model.close();
		this.model = null;
		this.vivo.close();
		this.vivo = null;
		this.namespace = null;
		this.newNamespace = null;
		this.properties = null;
		System.gc();
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.update.ChangeNamespace#getMatchingURIs(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.util.List, org.vivoweb.harvester.util.repo.JenaConnect) getMatchingURIs(Resource current, String namespace, List&lt;Propetry&gt; properties, JenaConnect vivo)}.
	 */
	public void testGetMatchingURIs() {
		log.info("testing getMatchingURIs Start");
		List<String> uris = ChangeNamespace.getMatchingURIs(this.model.getJenaModel().getResource(this.namespace+"test123"), this.newNamespace, this.properties, this.vivo);
		assertTrue(uris.contains("http://vivo.test.edu/individual/n321456"));
		assertTrue(uris.contains("http://vivo.test.edu/individual/n654321"));
		assertFalse(uris.contains("http://vivobad.test.edu/wrong/n987456"));
		assertTrue(uris.size() == 2);
		log.info("testing getMatchingURIs End");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.update.ChangeNamespace#getMatchingURI(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.util.List, org.vivoweb.harvester.util.repo.JenaConnect)  getMatchingURI(Resource current, String namespace, List&lt;Propetry&gt; properties, JenaConnect vivo)}.
	 */
	public void testGetMatchingURI() {
		log.info("testing getMatchingURI Start");
		String uri = ChangeNamespace.getMatchingURI(this.model.getJenaModel().getResource(this.namespace+"test123"), this.newNamespace, this.properties, this.vivo);
		assertTrue(uri.equals("http://vivo.test.edu/individual/n654321") || uri.equals("http://vivo.test.edu/individual/n321456"));
		log.info("testing getMatchingURI End");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.update.ChangeNamespace#getUnusedURI(java.lang.String, org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect...) getUnusedURI(String namespace, JenaConnect vivo, JenaConnect... models)}.
	 */
	public void testGetUnusedURI() {
		log.info("testing getUnusedURI Start");
		String uri = ChangeNamespace.getUnusedURI(this.newNamespace, this.vivo);
		assertFalse(this.vivo.containsURI(uri));
		log.info("testing getUnusedURI End");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.update.ChangeNamespace#getURI(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.util.List, org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, boolean) getURI(Resource current, String namespace, List&lt;Propetry&gt; properties, JenaConnect vivo, JenaConnect model, boolean errorOnNewURI)}.
	 */
	public void testGetURI() {
		log.info("testing getURI Start");
		String matchuri = ChangeNamespace.getURI(this.model.getJenaModel().getResource(this.namespace+"test123"), this.newNamespace, this.properties, this.vivo, this.model, false);
		assertTrue(matchuri.equals("http://vivo.test.edu/individual/n654321") || matchuri.equals("http://vivo.test.edu/individual/n321456"));
		String newuri = ChangeNamespace.getURI(this.model.getJenaModel().getResource(this.namespace+"test321"), this.newNamespace, this.properties, this.vivo, this.model, false);
		assertFalse(this.vivo.containsURI(newuri));
		log.info("testing getURI End");
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.update.ChangeNamespace#changeNS(org.vivoweb.harvester.util.repo.JenaConnect, org.vivoweb.harvester.util.repo.JenaConnect, java.lang.String, java.lang.String, java.util.List, boolean) changeNS(JenaConnect model, String oldNamespace, String newNamespace, List&lt;Property&gt; properties, boolean errorOnNewURI)}.
	 */
	public void testChangeNS() {
		log.info("testing changeNS Start");
		ByteArrayOutputStream baos;
		baos = new ByteArrayOutputStream();
		this.model.exportRDF(baos);
		log.debug(baos.toString());
		ChangeNamespace.changeNS(this.model, this.vivo, this.namespace, this.newNamespace, this.properties, false);
		baos = new ByteArrayOutputStream();
		this.model.exportRDF(baos);
		log.debug(baos.toString());
		assertFalse(this.model.containsURI(this.namespace+"test123"));
		assertFalse(this.model.containsURI(this.namespace+"test321"));
		String query = ""+
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+"\n"+
		"PREFIX localVivo: <http://vivo.test.edu/ontology/vivo-test/>"+"\n"+
		"SELECT ?uri"+"\n"+
		"WHERE {"+"\n\t"+
			"?uri localVivo:uniqueId ?id ."+"\n\t"+
		"}";
		ResultSet rs = this.model.executeQuery(query);
		assertTrue(rs.hasNext());
		while(rs.hasNext()) {
			String ns = rs.next().getResource("uri").getNameSpace();
			assertTrue(ns.equals(this.newNamespace) || ns.equals("http://norename.blah.com/blah/"));
		}
		log.info("testing changeNS End");
	}
}
