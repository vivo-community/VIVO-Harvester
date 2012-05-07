package org.vivoweb.test.harvester.util;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.HarvestLogFormatter;

/**
 * @author Rene Ziede (rziede@ufl.edu)
 *
 */
public class HarvestLogFormatterTest {
	
	/**
	 * Temporary log file to test the reformatting process.
	 */
	private File tempReformattedLogFile;
	
	/**
	 * Temporary input N-Triple file.
	 */
	private File tempInputFile;
	
	private String nTripleFileContents =
		"<?xml version=\"1.0\"?>" + '\n' +
		"<rdf:RDF" + '\n' +
		"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + '\n' +
		"    xmlns:j.0=\"http://vivo.ufl.edu/ontology/vivo-ufl/\"" + '\n' +
		"    xmlns:j.1=\"http://xmlns.com/foaf/0.1/\"" + '\n' +
		"    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + '\n' +
		"    xmlns:j.2=\"http://vivoweb.org/ontology/core#\"" + '\n' +
		"    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" >" + '\n' +
		"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n865807629\">" + '\n' +
		"    <j.0:harvestedBy>DSR-Harvester</j.0:harvestedBy>" + '\n' +
		"    <j.0:dsrNumber>11070808</j.0:dsrNumber>" + '\n' +
		"    <j.2:sponsorAwardId>R03 AG040400</j.2:sponsorAwardId>" + '\n' +
		"    <j.0:psContractNumber>00082591</j.0:psContractNumber>" + '\n' +
		"    <j.2:totalAwardAmount>73250.00</j.2:totalAwardAmount>" + '\n' +
		"    <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Agreement\"/>" + '\n' +
		"    <j.2:dateTimeInterval rdf:resource=\"http://vivo.ufl.edu/individual/n2115363529\"/>" + '\n' +
		"    <j.2:relatedRole rdf:resource=\"http://vivo.ufl.edu/individual/n1712358162\"/>" + '\n' +
		"    <j.0:dateHarvested>2012-04-20-04:00</j.0:dateHarvested>" + '\n' +
		"    <j.2:grantAwardedBy rdf:resource=\"http://vivo.ufl.edu/individual/n363671977\"/>" + '\n' +
		"    <j.2:administeredBy rdf:resource=\"http://vivo.ufl.edu/individual/n8858824\"/>" + '\n' +
		"    <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Grant\"/>" + '\n' +
		"    <rdfs:label>Mechanisms of Reduced Regenerative Potential in Aging Skeletal Muscle</rdfs:label>" + '\n' +
		"  </rdf:Description>" + '\n' +
		"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/individual/n307313097\">" + '\n' +
		"    <j.2:grantSubcontractedThrough rdf:resource=\"http://vivo.ufl.edu/individual/n19728825\"/>" + '\n' +
		"    <j.2:dateTimeInterval rdf:resource=\"http://vivo.ufl.edu/individual/n1918923864\"/>" + '\n' +
		"    <j.2:grantAwardedBy rdf:resource=\"http://vivo.ufl.edu/individual/n1279090289\"/>" + '\n' +
		"  </rdf:Description>" + '\n' +
		"</rdf:RDF>";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		//create test N-Triple file
		//this.reformattedLogFile = FileAide.createTempFile("harvestlog", "nt");
		//create temp log directory
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		//delete temp N-Triple file
		//delete temp log directory
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.util.HarvestLogFormatter#execute()}.
	 * @throws IOException VFS
	 */
	@Test
	public void testExecute() throws IOException {

		//Create a temp file and give it nTriple contents, use temp path for init on HarvestLogFormatter
		this.tempInputFile = FileAide.createTempFile("vivo-ntriple-additions", "xml");
		FileAide.setTextContent(this.tempInputFile.getAbsolutePath() , nTripleFileContents, true);
		
		//Parameters for HarvestLogFormatter
		Map<String, String> inFiles = new Hashtable<String, String>();
		inFiles.put("Addition", this.tempInputFile.getAbsolutePath());
		String targetHarvest = "peoplesoft-biztalk";
		String destRootDir = "/logs/all_harvest_logs/" + targetHarvest + "/";
	
		HarvestLogFormatter hlf = new HarvestLogFormatter(inFiles, destRootDir, targetHarvest);

		hlf.execute();
		
	}
	
}
