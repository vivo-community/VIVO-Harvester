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
		
		String src = "/src/";
		String dest = "/dest/";
		String srcFile = src+"test";
		String name = "year";
		String value = "2003";
		
		String xmlContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
			"<bookstore>" +
			"<book category=\"COOKING\">" +
			"<title lang=\"en\">Everyday Italian</title>" +
			"<author>Giada De Laurentiis</author>" +
			"<year>2005</year>" +
			"<price>30.00</price>" +
			"</book>" +
			"<book category=\"CHILDREN\">" +
			"<title lang=\"en\">Harry Potter</title>" +
			"<author>J K. Rowling</author>" +
			"<year>2005</year>" +
			"<price>29.99</price>" +
			"</book>" +
			"<book category=\"WEB\">" +
			"<title lang=\"en\">XQuery Kick Start</title>" +
			"<author>James McGovern</author>" +
			"<author>Per Bothner</author>" +
			"<author>Kurt Cagle</author>" +
			"<author>James Linn</author>" +
			"<author>Vaidyanathan Nagarajan</author>" +
			"<year>2003</year>" +
			"<price>49.99</price>" +
			"</book>" +
			"<book category=\"WEB\">" +
			"<title lang=\"en\">Learning XML</title>" +
			"<author>Erik T. Ray</author>" +
			"<year>2007</year>" +
			"<price>39.95</price>" +
			"</book>" +
			"</bookstore>";
		
		try {
			//Test Case 1
			createSrcFile(src, dest, srcFile+"1.xml", xmlContent);
			XMLGrep xmlGrep1 = new XMLGrep(src, dest, name, value);
			xmlGrep1.execute();
			assertTrue(FileAide.exists(src));
			
			//Test Case 2
			createSrcFile(src, dest, srcFile+"2.xml", xmlContent);
			XMLGrep xmlGrep2 = new XMLGrep(src, dest, null, value);
			xmlGrep2.execute();
			assertTrue(FileAide.exists(src));
			
			//Test Case 3
			createSrcFile(src, dest, srcFile+"3.xml", xmlContent);
			XMLGrep xmlGrep3 = new XMLGrep(src, dest, name, null);
			xmlGrep3.execute();
			assertFalse(FileAide.exists(src));
			
			//Test Case 4
			createSrcFile(src, dest, srcFile+"4.xml", xmlContent);
			XMLGrep xmlGrep4 = new XMLGrep(src, dest, null, null);
			xmlGrep4.execute();
			assertFalse(FileAide.exists(src));
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("javadoc")
	public void createSrcFile(String src, String dest, String srcFile, String xmlContent){
		try {
			FileAide.createFolder(src);
			FileAide.createFolder(dest);
			FileAide.createFile(srcFile);
			FileAide.setTextContent(srcFile, xmlContent);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
