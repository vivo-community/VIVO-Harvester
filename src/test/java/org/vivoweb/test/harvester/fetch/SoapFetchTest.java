/**
 * 
 */
package org.vivoweb.test.harvester.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang.IllegalClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.fetch.SOAPFetch;
import org.vivoweb.harvester.util.InitLog;
import org.xml.sax.SAXException;
import junit.framework.TestCase;

/**
 * @author jrpence
 *
 */
public class SoapFetchTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SoapFetchTest.class);
	URL url;
	OutputStream output;
	InputStream xmlFileStream;
	String sesID;
	/**
	 * @param name
	 */
	public SoapFetchTest(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		InitLog.initLogger(null, null);
		
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.fetch.SOAPFetch#SOAPFetch(java.net.URL, java.io.OutputStream, java.io.InputStream, java.lang.String)}.
	 */
//	public void testSOAPFetchURLOutputStreamInputStreamString() {
//		fail("Not yet implemented");
//	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.fetch.SOAPFetch#xmlFormat(java.lang.String)}.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	public void testXmlFormat() throws IOException, TransformerException, ParserConfigurationException, SAXException {
		this.url =null;
		this.xmlFileStream = null;
		this.output=null;
		this.sesID=null;
		SOAPFetch testSubject = null;
		try{	
			testSubject = new SOAPFetch(this.url,this.output, this.xmlFileStream, this.sesID);
		}
		catch(IllegalArgumentException e){
			log.error(e.getMessage());
		}
		if(testSubject == null){
			throw new IllegalClassException("testSubject found to be null");
		}
		String testXML = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:authenticateResponse xmlns:ns2=\"http://auth.cxf.wokmws.thomsonreuters.com\"><return>2WAmHd@IPbBg@CiEE26</return></ns2:authenticateResponse></soap:Body></soap:Envelope>";
		String result = testSubject.formatResult(testXML);
		log.info("XMLResult:" + result);
		
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.fetch.SOAPFetch#xmlFormat2(java.lang.String)}.
	 */
//	public void testXmlFormat2() {
//		fail("Not yet implemented");
//	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.fetch.SOAPFetch#execute()}.
	 */
//	public void testExecute() {
//		fail("Not yet implemented");
//	}
	
}
