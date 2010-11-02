/** */
package org.vivoweb.test.ingest.transfer;

import java.io.ByteArrayOutputStream;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.transfer.ChangeNamespace;
import org.vivoweb.ingest.util.repo.JenaConnect;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/** */
public class ChangeNamespaceTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ChangeNamespaceTest.class);
	/** */
	private JenaConnect model;
	/** */
	private String namespace;
	/** */
	private String newNamespace;
	
	@Override
	protected void setUp() throws Exception {
		this.namespace = "http://test.vivoweb.org/individual/";
		this.newNamespace = "http://testChNS.vivoweb.org/newindividual/";
		this.model = new JenaConnect("jdbc:h2:mem:testChNSh2;MODE=HSQLDB", "sa", "", "HSQLDB", "org.h2.Driver", "testChNS");
		Resource test123 = ResourceFactory.createResource(this.namespace+"test123");
		this.model.getJenaModel().add(test123,ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label"),"testType");
		this.model.getJenaModel().add(ResourceFactory.createResource(this.namespace+"blah321"),ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type"),test123);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.model.close();
		this.model = null;
		this.namespace = null;
		System.gc();
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.transfer.ChangeNamespace#getUnusedURI(java.lang.String, org.vivoweb.ingest.util.repo.JenaConnect) getUnusedURI(String namespace, JenaConnect model)}.
	 */
	public void testGetUnusedURI() {
		String uri = ChangeNamespace.getUnusedURI(this.namespace, this.model);
		assertFalse(this.model.containsURI(uri));
	}
	
	/**
	 * Test method for {@link org.vivoweb.ingest.transfer.ChangeNamespace#changeNS(org.vivoweb.ingest.util.repo.JenaConnect, java.lang.String, java.lang.String) changeNS(JenaConnect model, String oldNamespace, String newNamespace)}.
	 */
	public void testChangeNS() {
		ByteArrayOutputStream baos;
		baos = new ByteArrayOutputStream();
		this.model.exportRDF(baos);
		log.debug(baos.toString());
		ChangeNamespace.changeNS(this.model, this.namespace, this.newNamespace);
		baos = new ByteArrayOutputStream();
		this.model.exportRDF(baos);
		log.debug(baos.toString());
		assertFalse(this.model.containsURI(this.namespace+"test123"));
		assertFalse(this.model.containsURI(this.namespace+"blah321"));
		String query = ""+
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+"\n"+
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+"\n"+
		"SELECT ?uri"+"\n"+
		"WHERE {"+"\n\t"+
			"?uri rdf:type ?typeuri ."+"\n\t"+
			"?typeuri rdfs:label \"testType\""+"\n"+
		"}";
		ResultSet rs = this.model.executeQuery(query);
		assertTrue(rs.hasNext());
		assertTrue(rs.next().getResource("uri").getNameSpace().equals(this.newNamespace));
	}
	
}
