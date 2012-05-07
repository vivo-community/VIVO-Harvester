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
		"<http://vivo.ufl.edu/harvested/thumbDirDownload/ufid> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime> \"2012-05-04T17:15:23-04:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ." + '\n' +
		"<http://vivo.ufl.edu/harvested/fullDirDownload/ufid> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime> \"2012-05-04T17:15:23-04:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .";
	
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
		inFiles.put("ADD", this.tempInputFile.getAbsolutePath());
		String targetHarvest = "peoplesoft-biztalk";
		String destRootDir = "/logs/all_harvest_logs/" + targetHarvest + "/";
	
		HarvestLogFormatter hlf = new HarvestLogFormatter(inFiles, destRootDir, targetHarvest);

		hlf.execute();
		
	}
	
}
