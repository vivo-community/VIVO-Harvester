package org.vivoweb.harvester.fetch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author drs
 */
public class RSSReader {
	
	/**
	 * Singleton instance
	 */
	private static RSSReader instance = null;
	
	/**
	 * Default Constructor
	 */
	private RSSReader() {
		// singleton
	}
	
	/**
	 * Singleton Accessor
	 * @return the rssreader
	 */
	public static RSSReader getInstance() {
		if(instance == null) {
			instance = new RSSReader();
		}
		return instance;
	}
	
	/**
	 * @throws IOException error
	 * 
	 */
	public void writeNews() throws IOException {
		try {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH.mm.ss.SSS");
			String dateString = sdf.format(date);
			BufferedWriter out = new BufferedWriter(new FileWriter("RSSDump-" + dateString + ".txt"));
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			URL u = new URL("http://us.wowarmory.com/character-feed.atom?r=Area+52&cn=Augimmy&locale=en_US");
			
			Document doc = builder.parse(u.openStream());
			
			NodeList nodes = doc.getElementsByTagName("entry");
			
			for(int i = 0; i < nodes.getLength(); i++) {
				
				Element element = (Element)nodes.item(i);
				
				// System.out.println("Title: " + getElementValue(element,"title"));
				// System.out.println("Link: " + getElementValue(element,"link"));
				// System.out.println("Publish Date: " + getElementValue(element,"pubDate"));
				// System.out.println("Category: " + getElementValue(element,"category"));
				// System.out.println("Creator: " + getElementValue(element,"dc:creator"));
				// System.out.println("Comment: " + getElementValue(element,"wfw:comment"));
				// System.out.println("Description: " + getElementValue(element,"description"));
				// System.out.println("Link: " + getElementValue(element,"guid"));
				// System.out.println();
				
				out.write("Title: " + getElementValue(element, "title") + "");
				out.newLine();
				out.write("Link: " + getElementValue(element, "link"));
				out.newLine();
				out.write("Publish Date: " + getElementValue(element, "published"));
				out.newLine();
				out.write("Updated: " + getElementValue(element, "updated"));
				out.newLine();
				out.write("id: " + getElementValue(element, "id"));
				out.newLine();
				out.write("content: " + getElementValue(element, "content"));
				out.newLine();
			}// for
			out.close();
			
		}// try
		catch(ParserConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * @param e the element
	 * @return the character data
	 */
	private String getCharacterDataFromElement(Element e) {
		try {
			Node child = e.getFirstChild();
			if(child instanceof CharacterData) {
				CharacterData cd = (CharacterData)child;
				return cd.getData();
			}
		} catch(Exception ex) {
			// ignore
		}
		return "";
	} // private String getCharacterDataFromElement
	
	/**
	 * @param value original value
	 * @return the float value
	 */
	protected float getFloat(String value) {
		if(value != null && !value.equals("")) {
			return Float.parseFloat(value);
		}
		return 0;
	}
	
	/**
	 * @param parent parent element
	 * @param label element label
	 * @return character data
	 */
	protected String getElementValue(Element parent, String label) {
		return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));
	}
	
	/**
	 * @param args the args
	 */
	public static void main(String[] args) {
		RSSReader reader = RSSReader.getInstance();
		try {
			reader.writeNews();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
