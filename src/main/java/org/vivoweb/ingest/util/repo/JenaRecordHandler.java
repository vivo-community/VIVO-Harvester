/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.util.repo;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.repo.RecordMetaData.RecordMetaDataType;
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
	 * metadata type
	 */
	protected Property metaType;
	/**
	 * metadata relation
	 */
	protected Property metaRel;
	/**
	 * metadata calendar
	 */
	protected Property metaCal;
	/**
	 * metadata operation
	 */
	protected Property metaOperation;
	/**
	 * metadata operator
	 */
	protected Property metaOperator;
	/**
	 * metadata md5
	 */
	protected Property metaMD5;
	
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
		this.metaType = this.model.createProperty(rhNameSpace, "metaData");
		this.metaRel = this.model.createProperty(rhNameSpace, "metaRel");
		this.metaCal = this.model.createProperty(rhNameSpace, "metaCal");
		this.metaOperation = this.model.createProperty(rhNameSpace, "metaOperation");
		this.metaOperator = this.model.createProperty(rhNameSpace, "metaOperation");
		this.metaMD5 = this.model.createProperty(rhNameSpace, "metaMD5");
	}
	
	@Override
	public void addRecord(Record rec, Class<?> creator, boolean overwrite) throws IOException {
		if(!needsUpdated(rec)) {
			return;
		}
		Resource record = getRecordResource(rec.getID());
		if(!overwrite && record != null) {
			throw new IOException("Record already exists!");
		} else if(record == null) {
			record = this.model.createResource();
		}
		this.model.add(this.model.createStatement(record, this.isA, this.recType));
		this.model.add(this.model.createStatement(record, this.idType, rec.getID()));
		this.model.add(this.model.createStatement(record, this.dataType, rec.getData()));
		this.addMetaData(rec, creator, RecordMetaDataType.written);
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
		delMetaData(recID);
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

	
	@Override
	protected void addMetaData(Record rec, RecordMetaData rmd) throws IOException {
		Resource record = getRecordResource(rec.getID());
		if(record == null) {
			throw new IOException("Record "+rec.getID()+" does not exist!");
		}
		Resource metaData = this.model.createResource();
		this.model.add(this.model.createStatement(metaData, this.isA, this.metaType));
		this.model.add(this.model.createStatement(metaData, this.metaRel, record));
		this.model.add(this.model.createStatement(metaData, this.metaCal, rmd.getDate().getTimeInMillis()+""));
		this.model.add(this.model.createStatement(metaData, this.metaOperation, rmd.getOperation().toString()));
		this.model.add(this.model.createStatement(metaData, this.metaOperator, rmd.getOperator().getName()));
		this.model.add(this.model.createStatement(metaData, this.metaMD5, rmd.getMD5()));
	}
	

	@Override
	protected void delMetaData(String recID) throws IOException {
		// create query string
		String sQuery = ""
				+ "PREFIX rhns: <"+rhNameSpace+"> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "DELETE { "
				+ "  ?record ?p ?v "
				+ "} "
				+ "WHERE { "
				+ "  ?record rdf:type rhns:"+this.metaType.getLocalName()+" . "
				+ "  ?record rhns:"+this.metaRel.getLocalName()+" \""+recID+"\" . "
				+ "  ?record ?p ?v . "
				+ "}";
		UpdateRequest ur = UpdateFactory.create(sQuery);
		UpdateAction.execute(ur, this.model);
	}
	

	@Override
	protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
		SortedSet<RecordMetaData> retVal = new TreeSet<RecordMetaData>();
		// create query string
		String sQuery = ""
				+ "PREFIX rhns: <"+rhNameSpace+"> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				+ "Select ?cal ?operation ?operator ?md5 \n"
				+ "WHERE { \n"
				+ "  ?meta rdf:type rhns:"+this.metaType.getLocalName()+" . \n"
				+ "  ?meta rhns:"+this.metaRel.getLocalName()+" \""+recID+"\" . \n"
				+ "  ?meta rhns:"+this.metaCal.getLocalName()+" ?cal . \n"
				+ "  ?meta rhns:"+this.metaOperation.getLocalName()+" ?operation . \n"
				+ "  ?meta rhns:"+this.metaOperator.getLocalName()+" ?operator . \n"
				+ "  ?meta rhns:"+this.metaMD5.getLocalName()+" ?md5 . \n"
				+ "}";
		
		// create query
		Query query = QueryFactory.create(sQuery);
		
		// execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, JenaRecordHandler.this.model);
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution querySol = rs.next();
			List<String> resultVars = rs.getResultVars();
			//Get Calendar
			String varCal = resultVars.get(0);
			Literal litCal = querySol.getLiteral(varCal);
			String strCal = litCal.getString();
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
			cal.setTimeInMillis(Long.parseLong(strCal));
			//Get Operation
			String varOperation = resultVars.get(0);
			Literal litOperation = querySol.getLiteral(varOperation);
			String strOperation = litOperation.getString();
			RecordMetaDataType operation = RecordMetaDataType.valueOf(strOperation);
			//Get Operator
			String varOperator = resultVars.get(0);
			Literal litOperator = querySol.getLiteral(varOperator);
			String strOperator = litOperator.getString();
			Class<?> operator;
			try {
				operator = Class.forName(strOperator);
			} catch(ClassNotFoundException e) {
				throw new IOException(e.getMessage(),e);
			}
			//Get MD5
			String varMD5 = resultVars.get(0);
			Literal litMD5 = querySol.getLiteral(varMD5);
			String md5 = litMD5.getString();
			retVal.add(new RecordMetaData(cal, operator, operation, md5));
		}
		return null;
	}

	
	@Override
	public void close() throws IOException {
		this.model.close();
	}
}
