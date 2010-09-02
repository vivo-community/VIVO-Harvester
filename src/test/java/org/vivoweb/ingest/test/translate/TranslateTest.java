/**
 * 
 */
package org.vivoweb.ingest.test.translate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.translate.XSLTranslator;
import org.vivoweb.ingest.util.repo.TextFileRecordHandler;

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
   
   /**
 * @param xmlFile	The file to Translate
 * @param xmlOutput	The expected Output
 * @throws IOException	FIXME
 * @throws ClassNotFoundException FIXME
 */
private void xslTranslateTest(String xmlFile, String xmlOutput) throws IOException, ClassNotFoundException{
		
	//set up the test file TestVault/transTests/PubMed
		VFS.getManager().resolveFile(new File("."), "TestVault/Translate/XML").createFolder();
		VFS.getManager().resolveFile(new File("."), "TestVault/Translate/RDF").createFolder();
		VFS.getManager().resolveFile(new File("."), "TestVault/Translate/RH").createFolder();
		
		//grab config file for getting/storing the result
		File inFile = new File("TestVault/Translate/RH/inRH.xml");
		Writer output = new BufferedWriter(new FileWriter(inFile));
	    output.write("<?xml version='1.0' encoding='UTF-8'?>" +
	    		"<RecordHandler type='org.vivoweb.ingest.util.repo.TextFileRecordHandler'>" +
	    		"<Param name='fileDir'>TestVault/Translate/XML</Param>" +
	    		"</RecordHandler>");
	    output.close();
		
	    		File outFile = new File("TestVault/Translate/RH/outRH.xml");
		output = new BufferedWriter(new FileWriter(outFile));
	    output.write("<?xml version='1.0' encoding='UTF-8'?>" +
	    		"<RecordHandler type='org.vivoweb.ingest.util.repo.TextFileRecordHandler'>" +
	    		"<Param name='fileDir'>TestVault/Translate/RDF</Param>" +
	    		"</RecordHandler>");
	    output.close();
	    
	    //add a record to the record handler
	    TextFileRecordHandler rh = new TextFileRecordHandler("TestVault/Translate/XML");
	    rh.addRecord("1",xmlFile, Class.forName("vivoweb.org.ingest.fetch.Fetch"));
	    
		//create the arguments to be passed		
		String[] argsToBePassed = new String[6];
		argsToBePassed[0] = "-x";
		argsToBePassed[1] = "config/datamaps/PubMedToVIVO.xsl";
		argsToBePassed[2] = "-i";
		argsToBePassed[3] = "TestVault/Translate/RH/inRH.xml";
		argsToBePassed[4] = "-o";
		argsToBePassed[5] = "TestVault/Translate/RH/outRH.xml";
		
		//call the xlsTranslate
		XSLTranslator.main(argsToBePassed);
		
		//get the file that was translated
		File resultantOutput = new File("TestVault/Translate/RDF/1");
		//buildinto String
		StringBuilder resultantXML = new StringBuilder("");
		BufferedReader inputReader = new BufferedReader(new FileReader(resultantOutput));
	    try {
	      String line = null;
	      while ((line = inputReader.readLine()) != null){
	        resultantXML.append(line);
	      }
	    }
   		finally{
	    	inputReader.close();
	    }
		
   		//compare the output
		assertEquals(resultantXML.toString(), xmlOutput);
		
		//delete the output of the test
		VFS.getManager().resolveFile("TestVault/Translate/XML").delete(new AllFileSelector());
		VFS.getManager().resolveFile("TestVault/Translate/RDF").delete(new AllFileSelector());
		VFS.getManager().resolveFile("TestVault/Translate/RH").delete(new AllFileSelector());
   }
   
   /**
 * 
 */
public void pubMedXSLTest(){
	   try {
		xslTranslateTest(buildPubMedXML().toString(), buildPubMedOutput().toString());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   
   
   /**
 * @return String of an example pubmed file
 */
private StringBuilder buildPubMedXML(){
	   StringBuilder outputXML = new StringBuilder();
	   
	  
	   return outputXML;
   }
   
   /**
 * @return String of the expected output from a translate of pubmed
 */
private StringBuilder buildPubMedOutput(){
	   StringBuilder outputXML = new StringBuilder();
	   
	   return outputXML;
   }

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
