/**
 * 
 */
package org.vivoweb.ingest.score;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 *
 */
public class ScoreTest extends TestCase {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ScoreTest.class);
	
	
	/**
	 * Test Argument parsing for scoring
	 */
	@SuppressWarnings("unused")
	public void testArguments() {
		String args;
		Score Test;
		//inputs
		String iArg = "XMLVault/TestRDF/20722075.rdf";
		String IArg = "config/recordHandlers/PubmedRDFRecordHandler.xml";
		String TArg = "config/jenaModels/VIVO.xml";
		String VArg = "config/jenaModels/VIVO.xml";
		
		//outputs
		String OArg = "config/jenaModels/VIVO.xml";
		
		//model overrides
		String tArg = "testTempModel";
		String oArg = "testOutputModel";
		String vArg = "testVivoModel";
			
		log.info("testArguments Start");
		log.info("Testing good configs");
		
		log.info("Testing rdf input configs");
		log.info("Test -I IArg -V VArg -a 1 -e workEmail -p -r");
		args = "-I " + IArg + " -V " + VArg + " -a 1 -e workEmail -p -r";
		Test = new Score(args);
		log.info("Test -I IArg -V VArg -v vArg -O OArg -o oArg -O OArg -o oArg -a 1 -e workEmail -p -r");
		args = "-I " + IArg + " -V " + VArg + " -v " + vArg + " -O " + OArg + " -o " + oArg + " -a 1 -e workEmail -p -r";
		Test = new Score(args);
		
		log.info("Testing Model input configs");
		log.info("Test -T TArg -V VArg -a 1 -e workEmail -p -r");
		args = "-T " + TArg + " -V " + VArg + " -a 1 -e workEmail -p -r";
		Test = new Score(args);
		log.info("Test -T TArg -t tArg -V VArg -v vArg -O OArg -o oArg -a 1 -e workEmail -p -r");
		args = "-T " + TArg + " -t " + tArg + " -V " + VArg + " -v " + vArg + " -O " + OArg + " -o " + oArg + " -a 1 -e workEmail -p -r";
		Test = new Score(args);
		
		log.info("Testing rdfFile input configs");
		log.info("Test -i iArg -V VArg -a 1 -e workEmail -p -r");
		args = "-i " + iArg + " -V " + VArg + " -a 1 -e workEmail -p -r";
		Test = new Score(args);
		log.info("Test -i iArg -V VArg -v vArg -O OArg -o oArg -a 1 -e workEmail -p -r");
		args = "-i " + iArg + " -V " + VArg + " -v " + vArg + " -O " + OArg + " -o " + oArg + " -a 1 -e workEmail -p -r";
		Test = new Score(args);
		
		log.info("Testing bad configs");
		
		log.info("testArguments End");
	}
	
    /**
     * Called before every test case method.
     */
	@Override
    protected void setUp() {
        // create objects under test
    }

    /**
     * Called after every test case method.
     */
	@Override
    protected void tearDown() {
        // release objects under test
    }
}
