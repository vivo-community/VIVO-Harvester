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
package org.vivoweb.ingest.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.ModelLoader;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class JenaConnect {
	
	private static Log log = LogFactory.getLog(JenaConnect.class);
	private Model jenaModel;
	
	/**
	 * Config File Based Factory
	 * @param configFile the vfs config file descriptor
	 * @return JenaConnect instance
	 * @throws IOException error connecting
	 * @throws SAXException xml parse error
	 * @throws ParserConfigurationException xml parse error
	 */
	public static JenaConnect parseConfig(FileObject configFile) throws ParserConfigurationException, SAXException, IOException {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		return new JenaConnectConfigParser().parseConfig(configFile.getContent().getInputStream());
	}
	
	/**
	 * Config File Based Factory
	 * @param configFile the config file descriptor
	 * @return JenaConnect instance
	 * @throws IOException error connecting
	 * @throws SAXException xml parse error
	 * @throws ParserConfigurationException xml parse error
	 */
	public static JenaConnect parseConfig(File configFile) throws ParserConfigurationException, SAXException, IOException {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		
		return parseConfig(VFS.getManager().resolveFile(new File("."), configFile.getAbsolutePath()));
	}
	
	/**
	 * Config File Based Factory
	 * @param configFileName the config file path
	 * @return JenaConnect instance
	 * @throws ParserConfigurationException error connecting
	 * @throws SAXException xml parse error
	 * @throws IOException xml parse error
	 */
	public static JenaConnect parseConfig(String configFileName) throws ParserConfigurationException, SAXException, IOException {
		return parseConfig(VFS.getManager().resolveFile(new File("."), configFileName));
	}
	
	/**
	 * Constructor (w/o Named Model)
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 */
	public JenaConnect(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass) {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of constructor "JenaConnect.JenaConnect" to Protected.
		//FIXME This code was marked as may cause compile errors by UCDetector.
		try {
			this.setJenaModel(this.createModel(dbUrl, dbUser, dbPass, dbType, dbClass));
		} catch(InstantiationException e) {
			log.error(e.getMessage(), e);
		} catch(IllegalAccessException e) {
			log.error(e.getMessage(), e);
		} catch(ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Constructor (w/ Named Model)
	 * @param dbUrl jdbc connection url
	 * @param dbUser username to use
	 * @param dbPass password to use
	 * @param modelName the model to connect to
	 * @param dbType database type ex:"MySQL"
	 * @param dbClass jdbc driver class
	 */
	public JenaConnect(String dbUrl, String dbUser, String dbPass, String modelName, String dbType, String dbClass) {
		this.setJenaModel(this.loadModel(dbUrl, dbUser, dbPass, modelName, dbType, dbClass));
	}
	
	/**
	 * Constructor (Load rdf from input stream)
	 * @param in input stream to load rdf from
	 */
	public JenaConnect(InputStream in) {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of Constructor to Private
		//FIXME This code was marked as may cause compile errors by UCDetector.
		this.setJenaModel(ModelFactory.createDefaultModel());
		this.loadRDF(in);
	}
	
	/**
	 * Constructor (Load rdf from File)
	 * @param inFilePath location of file to read rdf from
	 * @throws FileSystemException error getting file contents
	 */
	public JenaConnect(String inFilePath) throws FileSystemException {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		this(VFS.getManager().resolveFile(new File("."), inFilePath).getContent().getInputStream());
	}
	
	/**
	 * Accessor for Jena Model
	 * @return the Jena Model
	 */
	public Model getJenaModel() {
		return this.jenaModel;
	}
	

	private void setJenaModel(Model jena) {
		this.jenaModel = jena;
	}
	

	private Model createModel(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return initModel(initDB(dbUrl, dbUser, dbPass, dbType, dbClass)).createDefaultModel();
	}
	
	private Model loadModel(String dbUrl, String dbUser, String dbPass, String modelName, String dbType, String dbClass) {
		return ModelLoader.connectToDB(dbUrl, dbUser, dbPass, modelName, dbType, dbClass);
	}
	
	private IDBConnection initDB(String dbUrl, String dbUser, String dbPass, String dbType, String dbClass)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName(dbClass).newInstance();
		return new DBConnection(dbUrl, dbUser, dbPass, dbType);
	}
	
	private ModelMaker initModel(IDBConnection dbcon) {
		return ModelFactory.createModelRDBMaker(dbcon);
	}
	
	/**
	 * Load in RDF
	 * @param in input stream to read rdf from
	 */
	public void loadRDF(InputStream in) {
		//This code was marked as may cause compile errors by UCDetector.
		//Change visibility of method "JenaConnect.loadRDF" to Private
		//FIXME This code was marked as may cause compile errors by UCDetector.
		this.getJenaModel().read(in, null);
		log.info("RDF Data was loaded");
	}
	
	/**
	 * Export all RDF
	 * @param out output stream to write rdf to
	 */
	public void exportRDF(OutputStream out) {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		RDFWriter fasterWriter = this.jenaModel.getWriter("RDF/XML");
		fasterWriter.setProperty("allowBadURIs", "true");
		fasterWriter.setProperty("relativeURIs", "");
		fasterWriter.write(this.jenaModel, out, "");
		log.info("RDF/XML Data was exported");
	}
	
	/**
	 * Adds all records in a RecordHandler to the model
	 * @param rh the RecordHandler to pull records from
	 */
	public void importRDF(RecordHandler rh) {
		//This code was marked as never used by UCDetector.
		//FIXME Determine if this code is necessary.
		for(Record r : rh) {
			this.getJenaModel().read(r.getData());
		}
	}
	
	private static class JenaConnectConfigParser extends DefaultHandler {
		private JenaConnect jc;
		private Map<String,String> params;
		private String tempVal;
		private String tempParamName;
		
		protected JenaConnectConfigParser() {
			this.params = new HashMap<String,String>();
			this.tempVal = "";
			this.tempParamName = "";
		}
		
		protected JenaConnect parseConfig(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			JenaConnectConfigParser p = new JenaConnectConfigParser();
			sp.parse(inputStream, p); // parse the file and also register this class for call backs
			return p.jc;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.tempVal = "";
			this.tempParamName = "";
			if(qName.equalsIgnoreCase("Param")) {
				this.tempParamName = attributes.getValue("name");
			} else if(!qName.equalsIgnoreCase("Model")) {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal = new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("Model")) {
				try {
					if(this.params.containsKey("modelName")) {
						this.jc = new JenaConnect(this.params.get("dbUrl"), this.params.get("dbUser"), this.params.get("dbPass"), this.params.get("modelName"), this.params.get("dbType"), this.params.get("dbClass"));
					} else {
						this.jc = new JenaConnect(this.params.get("dbUrl"), this.params.get("dbUser"), this.params.get("dbPass"), this.params.get("dbType"), this.params.get("dbClass"));
					}
				} catch(SecurityException e) {
					throw new SAXException(e.getMessage(),e);
				} catch(IllegalArgumentException e) {
					throw new SAXException(e.getMessage(),e);
				}
			} else if(qName.equalsIgnoreCase("Param")) {
				this.params.put(this.tempParamName, this.tempVal);
			} else {
				throw new SAXException("Unknown Tag: "+qName);
			}
		}
	}
	
}
