package org.vivoweb.test.harvester.util;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.XMLGrep;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;
import org.vivoweb.test.harvester.score.ScoreTest;

public class XMLGrepTest extends TestCase {
	
	@SuppressWarnings("javadoc")
	private static Logger log = LoggerFactory.getLogger(XMLGrepTest.class);
	
	protected static final String xmlContent1 = "<ns0:PERSON xmlns:ns0=\"http://uf.biztalk.shibperson\">" +
		"<UFID>83117145</UFID>" +
		"<GLID>vsposato</GLID>" +
		"<UFID2 />" +
		"<GLID2 />" +
		"<ACTIVE>A</ACTIVE>" +
		"<PROTECT>N</PROTECT>" +
		"<AFFILIATION>T</AFFILIATION>" +
		"<NAME type=\"21\">SPOSATO,VINCENT J</NAME>" +
		"<NAME type=\"33\">Sposato,Vincent J</NAME>" +
		"<NAME type=\"35\">Vincent</NAME>" +
		"<NAME type=\"36\">Sposato</NAME>" +
		"<NAME type=\"37\">J</NAME>" +
		"<NAME type=\"232\">Sposato,Vincent</NAME>" +
		"<ADDRESS>" +
		"<ADDRESS1 />" +
		"<ADDRESS2 />" +
		"<ADDRESS3>PO BOX 100152</ADDRESS3>" +
		"<CITY>GAINESVILLE</CITY>" +
		"<STATE>FL</STATE>" +
		"<ZIP>326100152</ZIP>" +
		"</ADDRESS>" +
		"<PHONE type=\"10\">(352) 294-5274   45274</PHONE>" +
		"<EMAIL type=\"1\">vsposato@ufl.edu</EMAIL>" +
		"<DEPTID>27010707</DEPTID>" +
		"<RELATIONSHIP type=\"195\">" +
		"<DEPTID>27010707</DEPTID>" +
		"<DEPTNAME>HA-AHC ESE</DEPTNAME>" +
		"</RELATIONSHIP>" +
		"<RELATIONSHIP type=\"203\">" +
		"<DEPTID>27010707</DEPTID>" +
		"<DEPTNAME>HA-AHC ESE</DEPTNAME>" +
		"</RELATIONSHIP>" +
		"<RELATIONSHIP type=\"223\">" +
		"<DEPTID>27010707</DEPTID>" +
		"<DEPTNAME>CREATEHA-AHC ESE</DEPTNAME>" +
		"</RELATIONSHIP>" +
		"<WORKINGTITLE>IT Expert, Sr. Software Engineer</WORKINGTITLE>" +
		"<DECEASED>N</DECEASED>" +
		"<LOA>Bronze</LOA>" +
		"<ACTION>RENAME</ACTION>" +
		"</ns0:PERSON>";
	
	protected static final String xmlContent3 = "<ns0:PERSON xmlns:ns0=\"http://uf.biztalk.shibperson\">" +
		"<UFID>83117145</UFID>" +
		"<GLID>vsposato</GLID>" +
		"<UFID2 />" +
		"<GLID2 />" +
		"<ACTIVE>A</ACTIVE>" +
		"<PROTECT>N</PROTECT>" +
		"<AFFILIATION>T</AFFILIATION>" +
		"<NAME type=\"21\">SPOSATO,VINCENT J</NAME>" +
		"<NAME type=\"33\">Sposato,Vincent J</NAME>" +
		"<NAME type=\"35\">Vincent</NAME>" +
		"<NAME type=\"36\">Sposato</NAME>" +
		"<NAME type=\"37\">J</NAME>" +
		"<NAME type=\"232\">Sposato,Vincent</NAME>" +
		"<ADDRESS>" +
		"<ADDRESS1 />" +
		"<ADDRESS2 />" +
		"<ADDRESS3>PO BOX 100152</ADDRESS3>" +
		"<CITY>GAINESVILLE</CITY>" +
		"<STATE>FL</STATE>" +
		"<ZIP>326100152</ZIP>" +
		"</ADDRESS>" +
		"<PHONE type=\"10\">(352) 294-5274   45274</PHONE>" +
		"<EMAIL type=\"1\">vsposato@ufl.edu</EMAIL>" +
		"<DEPTID>27010707</DEPTID>" +
		"<RELATIONSHIP type=\"195\">" +
		"<DEPTID>27010707</DEPTID>" +
		"<DEPTNAME>HA-AHC ESE</DEPTNAME>" +
		"</RELATIONSHIP>" +
		"<RELATIONSHIP type=\"203\">" +
		"<DEPTID>27010707</DEPTID>" +
		"<DEPTNAME>HA-AHC ESE</DEPTNAME>" +
		"</RELATIONSHIP>" +
		"<RELATIONSHIP type=\"223\">" +
		"<DEPTID>27010707</DEPTID>" +
		"<DEPTNAME>CREATEHA-AHC ESE</DEPTNAME>" +
		"</RELATIONSHIP>" +
		"<WORKINGTITLE>IT Expert, Sr. Software Engineer</WORKINGTITLE>" +
		"<DECEASED>N</DECEASED>" +
		"<LOA>Bronze</LOA>" +
		"<IGNORE>YES</IGNORE>" +
		"</ns0:PERSON>";
	/**
	 * Destination dir for all files having <Action>RENAME</Action> tag
	 * 
	 */
	private String renamedest;
	
	/**
	 * Destination dir for all files having <Ignore>Yes</Ignore> tag
	 */
	private String ignoredest;
	
	/**
	 * Source dir for input xml messages 
	 */
	private String src;
	
	/**
	 * Source xml file name 
	 */
	private String srcFile;
	
	protected void setUp() throws Exception {
		InitLog.initLogger(null, null);
		// load input models
		this.src = "soapsrc/";
		this.srcFile = "test";
		FileAide.createFolder(src);
		
	}
	
	protected void tearDown() {
		try {
		FileAide.delete(src);
	} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private void testValueAndTag(String ignoredest, String src, String srcFile, String xmlContent3) throws IOException {
	
	/**
	 * This the test case to test the Xml grep functionality on the bases of a Tagname and value.  All files with <tag> value<tag> will be moved to specified destination dir
	 * @throws IOException
	 */
	@SuppressWarnings("javadoc")
	public void testValueAndTagPositiveTest() throws IOException {
		this.ignoredest = "ignore/";
		createSrcFile(ignoredest, srcFile, xmlContent3);
		XMLGrep xmlGrep = new XMLGrep(src, ignoredest, "YES", "IGNORE");
		xmlGrep.execute();
		assertTrue(FileAide.exists(ignoredest + "/" + srcFile));
		assertTrue(!FileAide.exists(src + "/" + srcFile));
		FileAide.delete(ignoredest);
		FileAide.delete(src);
	}
	
	public void testValueAndTagNegativeTest() throws IOException {
		this.ignoredest = "ignore/";
		createSrcFile(ignoredest, srcFile, xmlContent1);
		XMLGrep xmlGrep = new XMLGrep(src, ignoredest, "YES", "IGNORE");
		xmlGrep.execute();
		assertTrue(!FileAide.exists(ignoredest + "/" + srcFile));
		assertTrue(FileAide.exists(src + "/" + srcFile));
		FileAide.delete(ignoredest);
		FileAide.delete(src);
	}
	//private void testValueOnly(String renamedest, String src, String srcFile, String xmlContent1) throws IOException {
	
	/**
	 * This the test case to test the Xml grep functionality on the bases of only tag value.  All files with specified Tag value will be moved to specified destination dir.
	 * @throws IOException
	 */
	@SuppressWarnings("javadoc")
	public void testValueOnlyPositiveTest() throws IOException {
		this.renamedest = "rename/";
		createSrcFile(renamedest, srcFile, xmlContent3);
		XMLGrep xmlGrep = new XMLGrep(src, renamedest, "RENAME", null);
		xmlGrep.execute();
		assertTrue(!FileAide.exists(renamedest + "/" + srcFile));
		assertTrue(FileAide.exists(src + "/" + srcFile));
	}
	
	@SuppressWarnings("javadoc")
	public void testValueOnlyNegativeTest() throws IOException {
		this.renamedest = "rename/";
		createSrcFile(renamedest, srcFile, xmlContent3);
		XMLGrep xmlGrep = new XMLGrep(src, renamedest, "RENAME", null);
		xmlGrep.execute();
		assertTrue(!FileAide.exists(renamedest + "/" + srcFile));
		assertTrue(FileAide.exists(src + "/" + srcFile));
	}

	public void testTagOnlyPositiveTest() throws IOException {
		this.ignoredest = "ignore/";
		createSrcFile(ignoredest, srcFile, xmlContent3);
		XMLGrep xmlGrep = new XMLGrep(src, ignoredest, null, "IGNORE");
		xmlGrep.execute();
		assertTrue(FileAide.exists(ignoredest + "/" + srcFile));
		assertTrue(!FileAide.exists(src + "/" + srcFile));
		FileAide.delete(ignoredest);
		FileAide.delete(src);
	}
	
	public void testTagOnlyNegativeTest() throws IOException {
		this.ignoredest = "ignore/";
		createSrcFile(ignoredest, srcFile, xmlContent1);
		XMLGrep xmlGrep = new XMLGrep(src, ignoredest, null, "IGNORE");
		xmlGrep.execute();
		assertTrue(!FileAide.exists(ignoredest + "/" + srcFile));
		assertTrue(FileAide.exists(src + "/" + srcFile));
		FileAide.delete(ignoredest);
		FileAide.delete(src);
	}
	
	
	private void createSrcFile(String dest, String srcFile, String xmlContent) {
		try {
			System.out.println(System.getProperty("user.dir"));
			FileAide.createFolder(dest);
			FileAide.createFile(src + srcFile);
			FileAide.setTextContent(src + srcFile, xmlContent);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
