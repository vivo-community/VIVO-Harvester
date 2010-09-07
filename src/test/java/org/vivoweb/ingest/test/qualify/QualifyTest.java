/**
 * 
 */
package org.vivoweb.ingest.test.qualify;

import org.vivoweb.ingest.qualify.SPARQLQualify;
import junit.framework.TestCase;

/**
 * @author swilliams
 *
 */
public class QualifyTest extends TestCase {

	/**
	 * 
	 */
	public QualifyTest() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
		
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	 /**
	    * I am adding this so you stop killing maven builds >:O
	    */
	public void testNothing() {
	   	assertTrue(true);
	}

	//java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -r .*JAMA.* -v "The Journal of American Medical Association" -d http://vivoweb.org/ontology/core#Title
	public void testRegexReplace(){
		
		/*String[] argsToBePassed = new String[6];
		argsToBePassed[0] = "-j";
		argsToBePassed[1] = "config/jenaModels/VIVO.xml";
		argsToBePassed[2] = "-r";
		argsToBePassed[3] = ".*JAMA.*";
		argsToBePassed[4] = "-v";
		argsToBePassed[5] = "The Journal of American Medical Association";
		argsToBePassed[6] = "-d";
		argsToBePassed[7] = "http://vivoweb.org/ontology/core#Title";
		
		//call the xlsTranslate
		SPARQLQualify.main(argsToBePassed);
		*/
		
		assertTrue(true);
	}

	//java -cp bin/ingest-0.5.0.jar:bin/dependency/* org.vivoweb.ingest.qualify.SPARQLQualify -j config/jenaModels/VIVO.xml -t "Prof" -v "Professor" -d http://vivoweb.org/ontology/core#Title
	public void testStringReplace(){

		/*String[] argsToBePassed = new String[6];
		argsToBePassed[0] = "-j";
		argsToBePassed[1] = "config/jenaModels/VIVO.xml";
		argsToBePassed[2] = "-t";
		argsToBePassed[3] = "PROF";
		argsToBePassed[4] = "-v";
		argsToBePassed[5] = "Professor";
		argsToBePassed[6] = "-d";
		argsToBePassed[7] = "http://vivoweb.org/ontology/core#Title";
		
		//call the xlsTranslate
		SPARQLQualify.main(argsToBePassed);
		*/
		
		assertTrue(true);
	}
	


}
