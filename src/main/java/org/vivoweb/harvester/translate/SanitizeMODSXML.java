/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.translate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class does two things. First, it removes bad characters from the input file so that they can be read as
 * XML by XSLTranslator. Second, it removes the unprefixed "xmlns=" attribute found in the root node of many
 * MODS files, which left alone causes XSLTranslator to explode as if by magic.
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class SanitizeMODSXML extends Translator {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SanitizeMODSXML.class);
	
	@Override
	public String translate(String input) {
		// remove bad characters from input
		String xmlData = readRecordData(input);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = factory.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xmlData.getBytes("utf-8"))));
			//remove xmlns attribute
			removeBadAttribute(doc);
			
			//do other sanitization to doc, such as normalize dates
			normalizeIssuedDates(doc);
			
			return xmlToString(doc);
		} catch(SAXException e) {
			throw new Error(e);
		} catch(ParserConfigurationException e) {
			throw new Error(e);
		} catch(UnsupportedEncodingException e) {
			throw new Error(e);
		} catch(IOException e) {
			throw new Error(e);
		}
	}
	
	/**
	 * Normalizes date values in IssuedDate elements.
	 * @param doc is the dom object to work on
	 */
	private void normalizeIssuedDates(Document doc) {
		NodeList issuedEles = doc.getElementsByTagName("dateIssued");
		Hashtable<String, String> months = new Hashtable<String, String>(12, 1);
		months.put("jan", "01");
		months.put("feb", "02");
		months.put("mar", "03");
		months.put("apr", "04");
		months.put("may", "05");
		months.put("jun", "06");
		months.put("jul", "07");
		months.put("aug", "08");
		months.put("sep", "09");
		months.put("oct", "10");
		months.put("nov", "11");
		months.put("dec", "12");
		months.put("spr", "03");
		months.put("sum", "06");
		months.put("fal", "09");
		months.put("win", "12");
		
		for(int i = 0; i < issuedEles.getLength(); i++) {
			Node ele = issuedEles.item(i);
			Node parent = ele.getParentNode().getParentNode();
			String date = ele.getFirstChild().getNodeValue();
			String startyear = "";
			String startmonth = "";
			String endyear = "";
			String endmonth = "";
			String startday = "";
			String endday = "";
			//normalize date here
			//if it doesn't start with a 4-digit year, try to find a four digit sequence and make that the year
			if(!date.matches("^\\d{4}.*")) {
				Pattern p = Pattern.compile("(\\d{4})");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group();
				}
			} else if(date.matches("^\\d{4}")) { // for pattern yyyy
				Pattern p = Pattern.compile("^(\\d{4})");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
				}
			} else if(date.matches("^\\d{4}-\\d{4}.*")) { // for pattern yyyy-yyyy
				Pattern p = Pattern.compile("^(\\d{4})-(\\d{4}).*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					endyear = m.group(2);
				}
			} else if(date.matches("^\\d{4}-\\d{1,2}(/\\d{1,2}|/)?")) { // for pattern yyyy-mm(/dd)
				Pattern p = Pattern.compile("^(\\d{4})-(\\d{1,2})(/(\\d{1,2})|/)?");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2);
					if(m.group(4) != null) {
						startday = m.group(4);
					}
				}
			} else if(date.matches("^\\d{4}-\\d{1,2}/\\d{4}")) { // for pattern yyyy-mm/yyyy
				Pattern p = Pattern.compile("^(\\d{4})-(\\d{1,2})/\\d{4}.*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2);
				}
			} else if(date.matches("^\\d{4}-\\d{1,2}/\\d{1,2}/(?:\\d{4}|\\d{2})")) { // for pattern yyyy-mm/dd/yy(yy)
				Pattern p = Pattern.compile("^(\\d{4})-(\\d{1,2})/(\\d{1,2})/(?:\\d{4}|\\d{2}).*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2);
					startday = m.group(3);
				}
			} else if(date.matches("^\\d{4}-\\d{1,2}/\\d{1,2}/\\d{4}")) { // for pattern yyyy-mm/dd/yyyy
				Pattern p = Pattern.compile("^(\\d{4})-(\\d{1,2})/(\\d{1,2})/\\d{4}.*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2);
					startday = m.group(3);
				}
			} else if(date.matches("^\\d{4}-\\d{1,2}/\\d{1,2}-\\d{1,2}/\\d{4}")) { // for pattern yyyy-mm/dd-dd/yyyy
				Pattern p = Pattern.compile("^(\\d{4})-(\\d{1,2})/(\\d{1,2})-(\\d{1,2})/\\d{4}.*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2);
					startday = m.group(3);
					endyear = startyear;
					endmonth = startmonth;
					endday = m.group(4);
				}
			} else if(date.matches("^\\d{4}-[a-zA-Z]{3,} \\d{1,2}-\\d{1,2}.*")) { // for pattern yyyy-Month day-day
				Pattern p = Pattern.compile("^(\\d{4})-([a-zA-z]{3,}) (\\d{1,2})-(\\d{1,2}).*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2).toLowerCase().substring(0, 3);
					if(months.containsKey(startmonth)) {
						startmonth = months.get(startmonth);
					} else {
						startmonth = "";
					}
					startday = m.group(3);
					endyear = startyear;
					endmonth = startmonth;
					endday = m.group(4);
				}
			} else if(date.matches("^\\d{4}-[a-zA-Z]{3,} \\d{4}.*")) { // for pattern yyyy-Month year
				Pattern p = Pattern.compile("^(\\d{4})-([a-zA-z]{3,}) (\\d{4}).*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2);
					startmonth = m.group(2).toLowerCase().substring(0, 3);
					if(months.containsKey(startmonth)) {
						startmonth = months.get(startmonth);
					} else {
						startmonth = "";
					}
				}
			} else if(date.matches("^\\d{4}-[a-zA-Z]{3,} \\d{1,2}.*")) { // for pattern yyyy-Month day
				Pattern p = Pattern.compile("^(\\d{4})-([a-zA-z]{3,}) (\\d{1,2}).*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2).toLowerCase().substring(0, 3);
					if(months.containsKey(startmonth)) {
						startmonth = months.get(startmonth);
					} else {
						startmonth = "";
					}
					startday = m.group(3);
				}
			} else if(date.matches("^\\d{4}-[a-zA-Z]+.*")) { // for pattern yyyy-Month
				Pattern p = Pattern.compile("^(\\d{4})-([a-zA-z]{3,}).*");
				Matcher m = p.matcher(date);
				while(m.find()) {
					startyear = m.group(1);
					startmonth = m.group(2).toLowerCase().substring(0, 3);
					if(months.containsKey(startmonth)) {
						startmonth = months.get(startmonth);
					} else {
						startmonth = "";
					}
				}
			} else {
				log.debug("Couldn't parse dateIssued value: " + date);
				continue;
			}
			String newdate = startyear;
			if(startmonth != "") {
				if(startmonth.length() == 1)
					startmonth = "0" + startmonth;
				newdate += "-" + startmonth;
				if(startday != "") {
					if(startday.length() == 1)
						startday = "0" + startday;
					newdate += "-" + startday;
				}
			}
			if(endyear != "" && (!endyear.equals(startyear) || !endmonth.equals(startmonth) || !endday.equals(startday))) {
				newdate += ":" + endyear;
				if(endmonth != "") {
					if(endmonth.length() == 1)
						endmonth = "0" + endmonth;
					newdate += "-" + endmonth;
					if(endday != "") {
						if(endday.length() == 1)
							endday = "0" + endday;
						newdate += "-" + endday;
					}
				}
			}
			if(!newdate.equals(date)) {
				Node noteEle = doc.createElement("note");
				noteEle.appendChild(doc.createTextNode("dateIssued text: " + date));
				ele.getFirstChild().setNodeValue(newdate);
				parent.appendChild(noteEle);
			}
		}
	}
	
	/**
	 * Removes the unprefixed "xmlns" attribute from the root node.
	 * @param doc is the dom object to work on
	 */
	private void removeBadAttribute(Document doc) {
		Element documentElement = doc.getDocumentElement();
		if(documentElement.hasAttribute("xmlns")) {
			log.debug("Removing xmlns element");
			documentElement.removeAttribute("xmlns");
		}
	}
	
	/**
	 * Takes a Document and turns it into a String
	 * @param doc the Document to serialize
	 * @return the string version
	 * @throws IOException if a problem occurred in conversion
	 */
	private String xmlToString(Document doc) throws IOException {
		try {
			StringWriter stringWriter = new StringWriter();
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(stringWriter));
			return stringWriter.getBuffer().toString();
		} catch(TransformerConfigurationException e) {
			throw new IOException(e.getMessage(), e);
		} catch(TransformerException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * replaces all bad characters in a record's data
	 * @param data the record data
	 * @return a String containing all clean data in the record
	 */
	private String readRecordData(String data) {
		StringBuilder builder = new StringBuilder(data.length());
		int dataLength = data.length();
		for(int i = 0; i < dataLength; i++) {
			char currentChar = data.charAt(i);
			builder.append(getReplacement(currentChar));
		}
		String output = builder.toString();
		return output;
	}
	
	/**
	 * Tests a character to see if it should be replaced with another one or combination
	 * @param character the character to test
	 * @return The character to replace
	 */
	private String getReplacement(char character) {
		String replacement;
		
		if((character >= 6) && (character <= 31) && (character != 13) && (character != 10))
			replacement = "";
		else
			replacement = String.valueOf(character);
		
		return replacement;
	}
	
	@Override
	protected Logger getLog() {
		return log;
	}
}
