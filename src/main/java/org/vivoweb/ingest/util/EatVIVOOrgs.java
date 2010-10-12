/**
 * 
 */
package org.vivoweb.ingest.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author Christopher Haines
 */
public class EatVIVOOrgs {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(EatVIVOOrgs.class);
	/**
	 * properties to consume
	 */
	private static List<Property> otherProperties;
	/**
	 * model to read from
	 */
	private static JenaConnect inmodel;
	/**
	 * model to write out to
	 */
	private static JenaConnect outmodel;
	
	/**
	 * Consume an org and all of its sub organizations
	 * @param URI the uri of the org to eat
	 * @throws MalformedURLException error
	 * @throws IOException error
	 */
	private static void eatOrg(String URI) throws MalformedURLException, IOException {
		log.debug("eating: " + URI);
		Resource orgRes = inmodel.getJenaModel().getResource(URI);
		// eat suborgs
		StmtIterator subOrgItr = orgRes.listProperties(inmodel.getJenaModel().createProperty("http://vivoweb.org/ontology/core#", "hasSubOrganization"));
		while(subOrgItr.hasNext()) {
			Statement subOrgStmt = subOrgItr.next();
			outmodel.getJenaModel().add(subOrgStmt);
			String subOrgURI = subOrgStmt.getResource().getURI();
			eatOrg(subOrgURI);
		}
		// eat the rest
		for(Property prop : otherProperties) {
			StmtIterator otherPropItr = orgRes.listProperties(prop);
			while(otherPropItr.hasNext()) {
				outmodel.getJenaModel().add(otherPropItr.next());
			}
		}
	}
	
	/**
	 * @param args cmd line args
	 * @throws IOException error
	 * @throws MalformedURLException error
	 * @throws SAXException error
	 * @throws ParserConfigurationException error
	 */
	public static void main(String[] args) throws MalformedURLException, IOException, ParserConfigurationException, SAXException {
		inmodel = JenaConnect.parseConfig("config/jenaModels/myVIVO.xml");
		outmodel = new JenaConnect();
		otherProperties = new LinkedList<Property>();
		Property type = inmodel.getJenaModel().getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		otherProperties.add(type);
		Property ufDeptID = inmodel.getJenaModel().getProperty("http://vivo.ufl.edu/ontology/vivo-ufl/", "deptID");
		otherProperties.add(ufDeptID);
		otherProperties.add(inmodel.getJenaModel().getProperty("http://vivoweb.org/ontology/core#", "subOrganizationWithin"));
		otherProperties.add(inmodel.getJenaModel().getProperty("http://www.w3.org/2000/01/rdf-schema#", "label"));
		//		String ufURI = "http://vivo.ufl.edu/individual/UniversityofFlorida";
		//		eatOrg(ufURI);
		for(Statement stmt : IterableAdaptor.adapt(inmodel.getJenaModel().listStatements(null, ufDeptID, (RDFNode)null))) {
			eatOrg(stmt.getSubject().getURI());
		}
		outmodel.exportRDF(VFS.getManager().resolveFile(new File("."), "backups/ufOrgs.rdf.xml").getContent().getOutputStream(false));
	}
	
}
