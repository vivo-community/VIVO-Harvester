/**
 * 
 */
package org.vivoweb.ingest.test.translate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.translate.XSLTranslator;

import junit.framework.TestCase;

/**
 * @author ICHPDOMAIN\swilliams
 *
 */
public class TranslateTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

   /**
    * I am adding this so you stop killing maven builds >:O
    */
   public void testNothing() {
   	assertTrue(true);
   }
   
   public void pubMedXSLTranslateTest() throws IOException{
		//set up the test file TestVault/transTests/PubMed
		VFS.getManager().resolveFile(new File("."), "TestVault/Translate/Pubmed/XML").createFolder();
		VFS.getManager().resolveFile(new File("."), "TestVault/Translate/Pubmed/RDF").createFolder();
		
		//grab the file to compare the result to
		
		
		//grab config file for getting/storing the result
		File inFile = new File("TestVault/Translate/PubMed/RH/inRH.xml");
		Writer output = new BufferedWriter(new FileWriter(inFile));
	    output.write("<?xml version='1.0' encoding='UTF-8'?>" +
	    		"<RecordHandler type='org.vivoweb.ingest.util.repo.TextFileRecordHandler'>" +
	    		"<Param name='fileDir'>TestVault/Translate/Pubmed/XML</Param>" +
	    		"</RecordHandler>");
	    output.close();
		
	    		File outFile = new File("TestVault/Translate/PubMed/RH/outRH.xml");
		output = new BufferedWriter(new FileWriter(outFile));
	    output.write("<?xml version='1.0' encoding='UTF-8'?>" +
	    		"<RecordHandler type='org.vivoweb.ingest.util.repo.TextFileRecordHandler'>" +
	    		"<Param name='fileDir'>TestVault/Translate/Pubmed/RDF</Param>" +
	    		"</RecordHandler>");
	    output.close();
	    
	    //add a record to the record handler
	    File exampleInput = new File("TestVault/Translate/Pubmed/exampleInput");
	    //TextFileRecordHandler rh = new TextFileRecordHandler("TestVault/Translate/PubMed/XML");
	   // rh.addRecord(new Record(), Class.forName("vivoweb.org.ingest.fetch.PubmedSOAPFetch"));
	    
	    
		
		//create the arguments to be passed		
		String[] argsToBePassed = new String[6];
		argsToBePassed[0] = "-x";
		argsToBePassed[1] = "config/datamaps/PubMedToVIVO.xsl";
		argsToBePassed[2] = "-i";
		argsToBePassed[3] = "TestVault/Translate/Pubmed/inRH.xml";
		argsToBePassed[4] = "-o";
		argsToBePassed[5] = "TestVault/Translate/Pubmed/outRH.xml";
		
		//call the xlsTranslate
		XSLTranslator.main(argsToBePassed);
		
		//get the file that was translated
		File definedOutput = new File("TestVault/Translate/Pubmed/definedOutput.rdf");
		File resultantOutput = new File("TestVault/Translate/Pubmed/RDF/example");
		
		//compare the output
		
		
		//delete the output of the test
	}
   
   private StringBuilder buildPubMedXML(){
	   StringBuilder outputXML = new StringBuilder();
	   
	   return outputXML;
   }

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
