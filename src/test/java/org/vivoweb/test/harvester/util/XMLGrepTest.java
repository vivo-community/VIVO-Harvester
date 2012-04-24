package org.vivoweb.test.harvester.util;

import static org.junit.Assert.*; 
import java.io.IOException;
import org.junit.Test;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.XMLGrep;

public class XMLGrepTest {
		
	@SuppressWarnings("javadoc")
	@Test
	public void testExecute() {
		
		String renamedest = "rename/";
		String ignoredest = "igonore/";
		String src = "soapsrc/";
		
		String srcFile ="test";
		
		String xmlContent1 ="<ns0:PERSON xmlns:ns0=\"http://uf.biztalk.shibperson\">"+
		"<UFID>83117145</UFID>"+
		"<GLID>vsposato</GLID>"+
		"<UFID2 />"+
		"<GLID2 />"+
		"<ACTIVE>A</ACTIVE>"+
		"<PROTECT>N</PROTECT>"+
		"<AFFILIATION>T</AFFILIATION>"+
		"<NAME type=\"21\">SPOSATO,VINCENT J</NAME>"+
		"<NAME type=\"33\">Sposato,Vincent J</NAME>"+
		"<NAME type=\"35\">Vincent</NAME>"+
		"<NAME type=\"36\">Sposato</NAME>"+
		"<NAME type=\"37\">J</NAME>"+
		"<NAME type=\"232\">Sposato,Vincent</NAME>"+
		"<ADDRESS>"+
		"<ADDRESS1 />"+
		"<ADDRESS2 />"+
		"<ADDRESS3>PO BOX 100152</ADDRESS3>"+
		"<CITY>GAINESVILLE</CITY>"+
		"<STATE>FL</STATE>"+
		"<ZIP>326100152</ZIP>"+
		"</ADDRESS>"+
		"<PHONE type=\"10\">(352) 294-5274   45274</PHONE>"+
		"<EMAIL type=\"1\">vsposato@ufl.edu</EMAIL>"+
		"<DEPTID>27010707</DEPTID>"+
		"<RELATIONSHIP type=\"195\">"+
		"<DEPTID>27010707</DEPTID>"+
		"<DEPTNAME>HA-AHC ESE</DEPTNAME>"+
		"</RELATIONSHIP>"+
		"<RELATIONSHIP type=\"203\">"+
		"<DEPTID>27010707</DEPTID>"+
		"<DEPTNAME>HA-AHC ESE</DEPTNAME>"+
		"</RELATIONSHIP>"+
		"<RELATIONSHIP type=\"223\">"+
		"<DEPTID>27010707</DEPTID>"+
		"<DEPTNAME>CREATEHA-AHC ESE</DEPTNAME>"+
		"</RELATIONSHIP>"+
		"<WORKINGTITLE>IT Expert, Sr. Software Engineer</WORKINGTITLE>"+
		"<DECEASED>N</DECEASED>"+
		"<LOA>Bronze</LOA>"+
		"<ACTION>RENAME</ACTION>"+
		"</ns0:PERSON>";
		
		String xmlContent2 ="<ns0:PERSON xmlns:ns0=\"http://uf.biztalk.shibperson\">"+
			"<UFID>83117145</UFID>"+
			"<GLID>vsposato</GLID>"+
			"<UFID2 />"+
			"<GLID2 />"+
			"<ACTIVE>A</ACTIVE>"+
			"<PROTECT>N</PROTECT>"+
			"<AFFILIATION>T</AFFILIATION>"+
			"<NAME type=\"21\">SPOSATO,VINCENT J</NAME>"+
			"<NAME type=\"33\">Sposato,Vincent J</NAME>"+
			"<NAME type=\"35\">Vincent</NAME>"+
			"<NAME type=\"36\">Sposato</NAME>"+
			"<NAME type=\"37\">J</NAME>"+
			"<NAME type=\"232\">Sposato,Vincent</NAME>"+
			"<ADDRESS>"+
			"<ADDRESS1 />"+
			"<ADDRESS2 />"+
			"<ADDRESS3>PO BOX 100152</ADDRESS3>"+
			"<CITY>GAINESVILLE</CITY>"+
			"<STATE>FL</STATE>"+
			"<ZIP>326100152</ZIP>"+
			"</ADDRESS>"+
			"<PHONE type=\"10\">(352) 294-5274   45274</PHONE>"+
			"<EMAIL type=\"1\">vsposato@ufl.edu</EMAIL>"+
			"<DEPTID>27010707</DEPTID>"+
			"<RELATIONSHIP type=\"195\">"+
			"<DEPTID>27010707</DEPTID>"+
			"<DEPTNAME>HA-AHC ESE</DEPTNAME>"+
			"</RELATIONSHIP>"+
			"<RELATIONSHIP type=\"203\">"+
			"<DEPTID>27010707</DEPTID>"+
			"<DEPTNAME>HA-AHC ESE</DEPTNAME>"+
			"</RELATIONSHIP>"+
			"<RELATIONSHIP type=\"223\">"+
			"<DEPTID>27010707</DEPTID>"+
			"<DEPTNAME>CREATEHA-AHC ESE</DEPTNAME>"+
			"</RELATIONSHIP>"+
			"<WORKINGTITLE>IT Expert, Sr. Software Engineer</WORKINGTITLE>"+
			"<DECEASED>N</DECEASED>"+
			"<LOA>Bronze</LOA>"+
			"<ACTION>CREATE</ACTION>"+
			"</ns0:PERSON>";

		String xmlContent3 ="<ns0:PERSON xmlns:ns0=\"http://uf.biztalk.shibperson\">"+
			"<UFID>83117145</UFID>"+
			"<GLID>vsposato</GLID>"+
			"<UFID2 />"+
			"<GLID2 />"+
			"<ACTIVE>A</ACTIVE>"+
			"<PROTECT>N</PROTECT>"+
			"<AFFILIATION>T</AFFILIATION>"+
			"<NAME type=\"21\">SPOSATO,VINCENT J</NAME>"+
			"<NAME type=\"33\">Sposato,Vincent J</NAME>"+
			"<NAME type=\"35\">Vincent</NAME>"+
			"<NAME type=\"36\">Sposato</NAME>"+
			"<NAME type=\"37\">J</NAME>"+
			"<NAME type=\"232\">Sposato,Vincent</NAME>"+
			"<ADDRESS>"+
			"<ADDRESS1 />"+
			"<ADDRESS2 />"+
			"<ADDRESS3>PO BOX 100152</ADDRESS3>"+
			"<CITY>GAINESVILLE</CITY>"+
			"<STATE>FL</STATE>"+
			"<ZIP>326100152</ZIP>"+
			"</ADDRESS>"+
			"<PHONE type=\"10\">(352) 294-5274   45274</PHONE>"+
			"<EMAIL type=\"1\">vsposato@ufl.edu</EMAIL>"+
			"<DEPTID>27010707</DEPTID>"+
			"<RELATIONSHIP type=\"195\">"+
			"<DEPTID>27010707</DEPTID>"+
			"<DEPTNAME>HA-AHC ESE</DEPTNAME>"+
			"</RELATIONSHIP>"+
			"<RELATIONSHIP type=\"203\">"+
			"<DEPTID>27010707</DEPTID>"+
			"<DEPTNAME>HA-AHC ESE</DEPTNAME>"+
			"</RELATIONSHIP>"+
			"<RELATIONSHIP type=\"223\">"+
			"<DEPTID>27010707</DEPTID>"+
			"<DEPTNAME>CREATEHA-AHC ESE</DEPTNAME>"+
			"</RELATIONSHIP>"+
			"<WORKINGTITLE>IT Expert, Sr. Software Engineer</WORKINGTITLE>"+
			"<DECEASED>N</DECEASED>"+
			"<LOA>Bronze</LOA>"+
			"<IGNORE>YES</IGNORE>"+
			"</ns0:PERSON>";
		try {
			//Test Case 1 Only On tag value
			
			createSrcFile(src, renamedest, srcFile+"1.xml", xmlContent1);
			XMLGrep xmlGrep1 = new XMLGrep(src, renamedest, "RENAME",null );
		    xmlGrep1.execute();
			assertTrue(FileAide.exists(renamedest+"/"+srcFile+"1.xml"));
			FileAide.delete(renamedest);
			FileAide.delete(src);
			
			
			
			
			//Test Case 2  tag name and Value
			
			createSrcFile(src, ignoredest, srcFile+"2.xml", xmlContent3);
			XMLGrep xmlGrep2 = new XMLGrep(src, ignoredest, "YES","IGNORE" );
			xmlGrep2.execute();
			assertTrue(FileAide.exists(ignoredest+"/"+srcFile+"2.xml"));
			FileAide.delete(ignoredest);
			FileAide.delete(src);
			
			/*
			//Test Case 3
			createSrcFile(src, dest, srcFile+"3.xml", xmlContent);
			XMLGrep xmlGrep3 = new XMLGrep(src, dest, name, null);
			xmlGrep3.execute();
			assertFalse(FileAide.exists(src));*/
			
			/*//Test Case 4
			createSrcFile(src, dest, srcFile+"4.xml", xmlContent);
			XMLGrep xmlGrep4 = new XMLGrep(src, dest, null, null);
			xmlGrep4.execute();
			assertFalse(FileAide.exists(src));*/
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("javadoc")
	public void createSrcFile(String src, String dest, String srcFile, String xmlContent){
		try {
			System.out.println(System.getProperty("user.dir"));
			FileAide.createFolder(src);
			FileAide.createFolder(dest);
			FileAide.createFile(src+srcFile);
			FileAide.setTextContent(src+srcFile, xmlContent);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
