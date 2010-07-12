/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.fetch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.RecordHandler;
import org.vivoweb.ingest.util.Task;
import org.vivoweb.ingest.util.XMLRecordOutputStream;
import org.xml.sax.SAXException;
import ORG.oclc.oai.harvester2.app.RawWrite;

/**
 * Class for harvesting from OAI Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class OAIFetch extends Task {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(OAIFetch.class);
	/**
	 * The website address of the OAI Repository without the protocol prefix (No http://)
	 */
	private String strAddress;
	/**
	 * The start date for the range of records to pull, format is YYYY-MM-DD<br>
	 * If time is required, format is YYYY-MM-DDTHH:MM:SS:MSZ<br>
	 * Some repositories do not support millisecond resolution.<br>
	 * Example 2010-01-15T13:45:12:50Z<br>
	 */
	private String strStartDate;
	/**
	 * The end date for the range of records to pull, format is YYYY-MM-DD<br>
	 * If time is required, format is YYYY-MM-DDTHH:MM:SS:MSZ<br>
	 * Some repositories do not support millisecond resolution.<br>
	 * Example 2010-01-15T13:45:12:50Z<br>
	 */
	private String strEndDate;
	/**
	 * The output stream to send the harvested XML to
	 */
	private OutputStream osOutStream;
	
	/**
	 * Constuctor
	 * @param address The website address of the repository, without http://
	 * @param outStream The output stream to write to
	 */
	public OAIFetch(String address, OutputStream outStream) {
		this(address, "0001-01-01", "8000-01-01", outStream);
	}
	
	/**
	 * Constuctor
	 * @param address The website address of the repository, without http://
	 * @param startDate The date at which to begin fetching records, format and time resolution depends on repository.
	 * @param endDate The date at which to stop fetching records, format and time resolution depends on repository.
	 * @param outStream The output stream to write to
	 */
	public OAIFetch(String address, String startDate, String endDate, OutputStream outStream) {
		this.strAddress = address;
		this.strStartDate = startDate;
		this.strEndDate = endDate;
		this.osOutStream = outStream;
	}
	
	public static OAIFetch getInstance(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
		String address = getParam(params, "address", true);
		String startDate = getParam(params, "startDate", true);
		String endDate = getParam(params, "endDate", true);
		String repositoryConfig = getParam(params, "repositoryConfig", true);
		RecordHandler rhRecordHandler = RecordHandler.parseConfig(repositoryConfig);
		rhRecordHandler.setOverwriteDefault(true);
		OutputStream os = new XMLRecordOutputStream("record", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><harvest>", "</harvest>", ".*?<identifier>(.*?)</identifier>.*?", rhRecordHandler);
		return new OAIFetch(address, startDate, endDate, os);
	}
	
	@Override
	public void executeTask() throws NumberFormatException {
		try {
			System.out.println("http://" + this.strAddress);
			System.out.println(this.strStartDate);
			System.out.println(this.strEndDate);
			RawWrite.run("http://" + this.strAddress, this.strStartDate, this.strEndDate, "oai_dc", "", this.osOutStream);
		} catch(IOException e) {
			log.error(e.getMessage(),e);
		} catch(ParserConfigurationException e) {
			log.error(e.getMessage(),e);
		} catch(SAXException e) {
			log.error(e.getMessage(),e);
		} catch(TransformerException e) {
			log.error(e.getMessage(),e);
		} catch(NoSuchFieldException e) {
			log.error(e.getMessage(),e);
		}
	}
}