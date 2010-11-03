/**
 * 
 */
package org.vivoweb.test.ingest.diff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.diff.Diff;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.vivoweb.test.ingest.score.ScoreTest;

import junit.framework.TestCase;

/**
 * @author drspeedo
 *
 */
public class DiffTest extends TestCase {

	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ScoreTest.class);
	/**
	 * previous ingest rdf statements to load for test
	 */
	private File previousRDF;
	/**
	 * incoming ingest rdf statements to load for test
	 */
	private File incomingRDF;
	/**
	 * expected rdf statements to load for test
	 */
	private File expectedRDF;
	/**
	 * vivo test configuration file
	 */
	private File vivoXML;
	
	
	
	
	
	/**
	 * @param name
	 */
	public DiffTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		// Create vivo rdf file
		try {
			this.previousRDF = File.createTempFile("scoretest_vivo", ".rdf");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.previousRDF));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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
					 "</rdf:RDF>");
			out.close();
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
		}
		
		// Create incoming rdf file
		// differences 
		//		changed email address
		// 		removal of middlename
		//		addition of prefix
		try {
			this.incomingRDF = File.createTempFile("scoretest_vivo", ".rdf");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.incomingRDF));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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
					 "</rdf:RDF>");
			out.close();
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
		}
		
		/*  to add (new items)
		<?xml version="1.0"?>
			<rdf:RDF
			    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			    xmlns:j.0="http://xmlns.com/foaf/0.1/"
			    xmlns:j.1="http://vivoweb.org/ontology/core#" > 
			  <rdf:Description rdf:about="http://vivo.mydomain.edu/individual/n3574">
			    <j.1:workEmail>v1105@ufl.edu</j.1:workEmail>
			    <j.0:prefix>Mr.</j.0:prefix>
			  </rdf:Description>
			</rdf:RDF>
		*/
		
		
		
		/*  to delete (old items)
		<?xml version="1.0"?>
		<rdf:RDF
		    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		    xmlns:j.0="http://vivoweb.org/ontology/core#" > 
		  <rdf:Description rdf:about="http://vivo.mydomain.edu/individual/n3574">
		    <j.0:middleName>J</j.0:middleName>
		    <j.0:workEmail>v@ufl.edu</j.0:workEmail>
		  </rdf:Description>
		</rdf:RDF>
		*/
		
		// create VIVO.xml
		try {
			this.vivoXML = File.createTempFile("scoretest_vivo", ".xml");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.vivoXML));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					 "<Model>" +
					 "<Param name=\"dbClass\">org.h2.Driver</Param>" +
					 "<Param name=\"dbType\">HSQLDB</Param>" +
					 "<Param name=\"dbUrl\">jdbc:h2:mem:test</Param>" +
					 "<Param name=\"modelName\">testVivoModel</Param>" +
					 "<Param name=\"dbUser\">sa</Param>" +
					 "<Param name=\"dbPass\"></Param>" +
					 "</Model>");
			out.close();
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 */
	public void testEverything(){
		
		JenaConnect previousJC;
		JenaConnect incomingJC;
		JenaConnect diffJC;
		
		try {
			Properties previousProp = new Properties();
			previousProp.put("modelName", "hr20101101");
			previousJC = JenaConnect.parseConfig(this.vivoXML, previousProp);
			previousJC.loadRDF(VFS.getManager().toFileObject(this.previousRDF).getContent().getInputStream(), null);
			
			Properties incomingProp = new Properties();
			incomingProp.put("modelName", "hr20101104");
			incomingJC = JenaConnect.parseConfig(this.vivoXML, incomingProp);
			incomingJC.loadRDF(VFS.getManager().toFileObject(this.incomingRDF).getContent().getInputStream(), null);
		
			Properties diffProp = new Properties();
			diffProp.put("modelName", "diff");
			diffJC = JenaConnect.parseConfig(this.vivoXML, diffProp);
						
			//System.out.println("prevModel");
			//previousJC.exportRDF(System.out);
			
			//System.out.println("incomingModel");
			//incomingJC.exportRDF(System.out);
			
						
			//testing new items
			Diff.main(new String[]{"-s",this.vivoXML.getAbsolutePath(),"-S","modelName=hr20101101","-m",this.vivoXML.getAbsolutePath(),
					"-M","modelName=hr20101104","-o",this.vivoXML.getAbsolutePath(),"-O","modelName=diff"});
			
			System.out.println("diffModel - new items");
			diffJC.exportRDF(System.out);
			
			diffJC.getJenaModel().remove(diffJC.getJenaModel());
			
						
			//testing old items
			Diff.main(new String[]{"-s",this.vivoXML.getAbsolutePath(),"-S","modelName=hr20101104","-m",this.vivoXML.getAbsolutePath(),
					"-M","modelName=hr20101101","-o",this.vivoXML.getAbsolutePath(),"-O","modelName=diff"});
			
			System.out.println("diffModel - old items");
			diffJC.exportRDF(System.out);
			
			//testing delete items
			
			
		} catch(Exception e) {
			
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
