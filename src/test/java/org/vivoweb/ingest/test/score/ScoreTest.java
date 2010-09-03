package org.vivoweb.ingest.test.score;

import org.vivoweb.ingest.score.Score;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hp.hpl.jena.sparql.util.StringUtils;
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
		String[] args;
		Score Test;

		//inputs
		String iArg = "config/jenaModels/VIVO.xml";
		String vArg = "config/jenaModels/VIVO.xml";
		
		//outputs
		String oArg = "config/jenaModels/VIVO.xml";
		//FIXME Nick: You need to generate these configs In your test setup
		//they only work on boxes with appropriately configured VIVO.xml in them, etc
		//If you use a local database in a temp folder, I suggest H2
		//(look at JDBCFetchTest, JenaConnectTest, and RecordHandlerTest to see how I use it),
		//you can just create it in setUp and use it for the test
		//and in tearDown you can close the db connection and delete the temp folder
		//(again, look at the above 3 tests where I did just that)
		
		//model overrides
		String IArg = "testInputModel";
		String OArg = "testOutputModel";
		String VArg = "testVivoModel";
			
		log.info("testArguments Start");
		log.info("Testing good configs");

		//TODO: Nicholas add to arg parsing test
		log.info("Test -i iArg -v vArg -o oArg -a 1 -e workEmail");
		args = new String[]{"-i",iArg,"-v",vArg,"-o",oArg,"-a","1","-e","workEmail"};
		log.info(StringUtils.join(" ", args));
		try {
//			Test = new Score(args);
			//TODO Nick: uncomment the above once this runs on any box, not just yours
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		
		log.info("Test -v vArg -a 1");
		args = new String[]{"-v",vArg,"-a","1"};
		log.info(StringUtils.join(" ", args));
		try {
//			Test = new Score(args);
			//TODO Nick: uncomment the above once this runs on any box, not just yours
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		
		log.info("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg-a 1 -e workEmail");
		args = new String[]{"-i",iArg,"-I",IArg,"-v",vArg,"-V",VArg,"-o",oArg,"-O",OArg,"-a","1","-e","workEmail"};
		log.info(StringUtils.join(" ", args));
		try {
//			Test = new Score(args);
			//TODO Nick: uncomment the above once this runs on any box, not just yours
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			fail(e.getMessage());
		}
		
		log.info("Testing bad configs");
		
		log.info("Testing keep working model");
		//TODO: add load test for model, one with -k, one without
		
		log.info("testArguments End");
	}
	
    /**
     * Called before every test case method.
     */
	@Override
    protected void setUp() {
		//don't need to do anything
    }

    /**
     * Called after every test case method.
     */
	@Override
    protected void tearDown() {
        // release objects under test
    }
}
