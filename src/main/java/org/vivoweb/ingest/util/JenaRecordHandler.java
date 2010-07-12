/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * RecordHandler that stores data in a Jena Model
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JenaRecordHandler extends RecordHandler {
	
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(JenaRecordHandler.class);
	/**
	 * the jena model we are using to store records
	 */
	protected Model model;
	/**
	 * namespace for recordhandlers
	 */
	protected static final String rhNameSpace = "http://ingest.vivoweb.org/util/jenarecordhandler#";
	/**
	 * record type
	 */
	protected Property recType;
	/**
	 * id type
	 */
	protected Property idType;
	/**
	 * data type
	 */
	protected Property dataType;
	/**
	 * rdf:type
	 */
	protected Property isA;
	
	/**
	 * Default Constructor
	 */
	protected JenaRecordHandler() {
		//Nothing to do here
		//Used by config construction
		//Should be used in conjunction with setParams()
	}
	
	/**
	 * Constructor (w/ Named Model)
	 * @param jdbcDriverClass jdbc driver class
	 * @param connType type of jdbc connection
	 * @param host host to conenct to
	 * @param port port to connect on
	 * @param dbName name of the database
	 * @param modelName name of the model
	 * @param username username to use
	 * @param password password to use
	 * @param dbType ex:"MySQL"
	 * @param dataFieldType rdf Predicate (including namespace) that describes data type
	 */
	public JenaRecordHandler(String jdbcDriverClass, String connType, String host, String port, String dbName,
			String username, String password, String dbType, String modelName, String dataFieldType) {
		this.model = new JenaConnect("jdbc:" + connType + "://" + host + ":" + port + "/" + dbName, username, password,
				modelName, dbType, jdbcDriverClass).getJenaModel();
		initVars(dataFieldType);
	}
	
	/**
	 * Constructor (w/o Named Model)
	 * @param jdbcDriverClass jdbc driver class
	 * @param connType type of jdbc connection
	 * @param host host to conenct to
	 * @param port port to connect on
	 * @param dbName name of the database
	 * @param username username to use
	 * @param password password to use
	 * @param dbType ex:"MySQL"
	 * @param dataFieldType rdf Predicate (including namespace) that describes data type
	 */
	public JenaRecordHandler(String jdbcDriverClass, String connType, String host, String port, String dbName,
			String username, String password, String dbType, String dataFieldType) {
		this.model = new JenaConnect("jdbc:" + connType + "://" + host + ":" + port + "/" + dbName, username, password,
				dbType, jdbcDriverClass).getJenaModel();
		initVars(dataFieldType);
	}
	
	/**
	 * Constructor (w/ Model Config File)
	 * @param configFile the model config file
	 * @param dataFieldType rdf Predicate (including namespace) that describes data type
	 * @throws IOException error connecting
	 * @throws SAXException xml parse error
	 * @throws ParserConfigurationException xml parse error
	 */
	public JenaRecordHandler(String configFile, String dataFieldType) throws ParserConfigurationException, SAXException, IOException {
		this.model = JenaConnect.parseConfig(configFile).getJenaModel();
		initVars(dataFieldType);
	}
	
	/**
	 * Initializes all the variables
	 * @param dataFieldType the type for data storage
	 */
	private void initVars(String dataFieldType) {
		this.recType = this.model.createProperty(rhNameSpace, "record");
		this.idType = this.model.createProperty(rhNameSpace, "idField");
		this.dataType = this.model.createProperty(dataFieldType);
		this.isA = this.model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#","type");
	}
	
	@Override
	public void addRecord(Record rec, boolean overwrite) throws IOException {
		Resource record = getRecordResource(rec.getID());
		if(!overwrite && record != null) {
			throw new IOException("Record already exists!");
		} else if(record == null) {
			record = this.model.createResource();
		}
		this.model.add(this.model.createStatement(record, this.isA, this.recType));
		this.model.add(this.model.createStatement(record, this.idType, rec.getID()));
		this.model.add(this.model.createStatement(record, this.dataType, rec.getData()));
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		// create query string
		String sQuery = ""
				+ "PREFIX rhns: <"+rhNameSpace+"> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "DELETE { "
				+ "  ?record ?p ?v "
				+ "} "
				+ "WHERE { "
				+ "  ?record rdf:type rhns:"+this.recType.getLocalName()+" . "
				+ "  ?record rhns:"+this.idType.getLocalName()+" \""+recID+"\" . "
				+ "  ?record ?p ?v . "
				+ "}";
		UpdateRequest ur = UpdateFactory.create(sQuery);
		UpdateAction.execute(ur, this.model);
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		// create query string
		String sQuery = ""
				+ "PREFIX rhns: <"+rhNameSpace+"> \n"
				+ "PREFIX lns: <"+this.dataType.getNameSpace()+"> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "Select ?dataField "
				+ "WHERE { "
				+ "  ?record rdf:type rhns:"+this.recType.getLocalName()+" . "
				+ "  ?record rhns:"+this.idType.getLocalName()+" \""+recID+"\" . "
				+ "  ?record lns:"+this.dataType.getLocalName()+" ?dataField . "
				+ "}";
		
		// create query
		Query query = QueryFactory.create(sQuery);
		
		// execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, this.model);
		ResultSet resultSet = qe.execSelect();
		
		// read first result
		String data = null;
		if(resultSet.hasNext()) {
			QuerySolution result = resultSet.next();
			data = result.getLiteral(resultSet.getResultVars().get(0)).getString();
		}
		return data;
	}
	
	/**
	 * Retrieves a record's resource from jena model
	 * @param recID the record id to retrieve
	 * @return the resource
	 */
	private Resource getRecordResource(String recID) {
		try {
			return this.model.listStatements(null, this.idType, recID).nextStatement().getSubject();
		} catch(NullPointerException e) {
			log.debug("Record not found",e);
		} catch(NoSuchElementException e) {
			log.debug("Record not found",e);
		}
		return null;
	}
	
	@Override
	public Iterator<Record> iterator() {
		return new JenaRecordIterator();
	}
	
	/**
	 * Iterator for JenaRecordHandler
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private class JenaRecordIterator implements Iterator<Record> {
		
		/**
		 * ResultSet from query
		 */
		private ResultSet resultSet;
		
		/**
		 * Default Constructor
		 */
		protected JenaRecordIterator() {
		// create query string
			String sQuery = ""
					+ "PREFIX rhns: <"+JenaRecordHandler.rhNameSpace+"> \n"
					+ "PREFIX lns: <"+JenaRecordHandler.this.dataType.getNameSpace()+"> \n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
					+ "Select ?idField \n"
					+ "WHERE { \n"
					+ "  ?record rdf:type rhns:"+JenaRecordHandler.this.recType.getLocalName()+" . \n"
					+ "  ?record rhns:"+JenaRecordHandler.this.idType.getLocalName()+" ?idField . \n"
					+ "  ?record lns:"+JenaRecordHandler.this.dataType.getLocalName()+" ?dataField . \n"
					+ "}";
			
			// create query
			Query query = QueryFactory.create(sQuery);
			
			// execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, JenaRecordHandler.this.model);
			this.resultSet = qe.execSelect();
		}
		
		@Override
		public boolean hasNext() {
			return this.resultSet.hasNext();
		}
		
		@Override
		public Record next() {
			try {
				QuerySolution querySol = this.resultSet.next();
				List<String> resultVars = this.resultSet.getResultVars();
				String var = resultVars.get(0);
				Literal lit = querySol.getLiteral(var);
				String id = lit.getString();
				Record result = getRecord(id);
				return result;
			} catch(IOException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		String jenaConfig = getParam(params,"jenaConfig",false);
		String dataFieldType = getParam(params,"dataFieldType",true);
		if(jenaConfig != null) {
			try {
				this.model = JenaConnect.parseConfig(jenaConfig).getJenaModel();
			} catch(ParserConfigurationException e) {
				throw new IllegalArgumentException(e);
			} catch(SAXException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			String jdbcDriverClass = getParam(params,"jdbcDriverClass",true);
			String connType = getParam(params,"connType",true);
			String host = getParam(params,"host",true);
			String port = getParam(params,"port",true);
			String dbName = getParam(params,"dbName",true);
			String username = getParam(params,"username",true);
			String password = getParam(params,"password",true);
			String dbType = getParam(params,"dbType",true);
			String modelName = getParam(params,"modelName",false);
			if(modelName != null) {
				this.model = new JenaConnect("jdbc:" + connType + "://" + host + ":" + port + "/" + dbName, username, password,
						modelName, dbType, jdbcDriverClass).getJenaModel();
			} else {
				this.model = new JenaConnect("jdbc:" + connType + "://" + host + ":" + port + "/" + dbName, username, password,
						dbType, jdbcDriverClass).getJenaModel();
			}
		}
		initVars(dataFieldType);
	}
	
}
