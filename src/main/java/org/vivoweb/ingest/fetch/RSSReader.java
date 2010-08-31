package org.vivoweb.ingest.fetch;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
public class RSSReader {
 
	private static RSSReader instance = null;
	
	private RSSReader() {
	}
	
	public static RSSReader getInstance() {
		if(instance == null) {
			instance = new RSSReader();	
		}
		return instance;
	}
	
	public void writeNews() {
		try {
			Date date = new Date();
			SimpleDateFormat sdf = new
			SimpleDateFormat("dd-MMM-yyyy_HH.mm.ss.SSS");
			String dateString = sdf.format(date);
			BufferedWriter out = new BufferedWriter(new FileWriter("RSSDump-" + dateString + ".txt"));
 
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			URL u = new URL("http://us.wowarmory.com/character-feed.atom?r=Area+52&cn=Augimmy&locale=en_US");
			
			Document doc = builder.parse(u.openStream());
			
			NodeList nodes = doc.getElementsByTagName("entry");
			
			
			for(int i=0;i<nodes.getLength();i++) {
				
				Element element = (Element)nodes.item(i);
				
				
				
//				System.out.println("Title: " + getElementValue(element,"title"));
//				System.out.println("Link: " + getElementValue(element,"link"));
//				System.out.println("Publish Date: " + getElementValue(element,"pubDate"));
//				System.out.println("Category: " + getElementValue(element,"category"));
//				System.out.println("Creator: " + getElementValue(element,"dc:creator"));
//				System.out.println("Comment: " + getElementValue(element,"wfw:comment"));
//				System.out.println("Description: " + getElementValue(element,"description"));
//				System.out.println("Link: " + getElementValue(element,"guid"));
//				System.out.println();
				
				out.write("Title: " + getElementValue(element,"title") + "");
				out.newLine();
				out.write("Link: " + getElementValue(element,"link"));
				out.newLine();
				out.write("Publish Date: " + getElementValue(element,"published"));
				out.newLine();
				out.write("Updated: " + getElementValue(element,"updated"));
				out.newLine();
				out.write("id: " + getElementValue(element,"id"));
				out.newLine();
				out.write("content: " + getElementValue(element,"content"));
				out.newLine();
			}//for
			out.close();
			
		}//try
		catch(Exception ex) {
		ex.printStackTrace();	
		}

		
	}
		
		
		private String getCharacterDataFromElement(Element e) {
			try {
				Node child = e.getFirstChild();
				if(child instanceof CharacterData) {
					CharacterData cd = (CharacterData) child;
					return cd.getData();
				}
			}
			catch(Exception ex) {
				
			}
			return "";			
		} //private String getCharacterDataFromElement
		
		protected float getFloat(String value) {
			if(value != null && !value.equals("")) {
				return Float.parseFloat(value);	
			}
			return 0;
		}
		
		protected String getElementValue(Element parent,String label) {
			return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));	
		}
		
		public static void main(String[] args) {
		RSSReader reader = RSSReader.getInstance();
		reader.writeNews();
		}
}
