/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.IterableAdaptor;
import org.vivoweb.harvester.util.repo.RecordMetaData.RecordMetaDataType;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PropertyNotFoundException;

/**
 * RecordHandler that stores data in a Jena Model
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JenaRecordHandler extends RecordHandler {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JenaRecordHandler.class);
	/**
	 * the jena model we are using to store records
	 */
	protected JenaConnect model;
	/**
	 * namespace for recordhandlers
	 */
	protected static final String rhNameSpace = "http://harvester.vivoweb.org/util/jenarecordhandler#";
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
		// Nothing to do here
		// Used by config construction
		// Should be used in conjunction with setParams()
	}
	
	/**
	 * Constructor (w/ Given Model)
	 * @param jena the model to use
	 * @param dataFieldType rdf Predicate (including namespace) that describes data type
	 */
	public JenaRecordHandler(JenaConnect jena, String dataFieldType) {
		this.model = jena;
		initVars(dataFieldType);
	}
	
	/**
	 * Constructor (w/ Model Config File)
	 * @param configFile the model config file
	 * @param dataFieldType rdf Predicate (including namespace) that describes data type
	 * @throws IOException error connecting
	 */
	public JenaRecordHandler(String configFile, String dataFieldType) throws IOException {
		this.model = JenaConnect.parseConfig(configFile);
		initVars(dataFieldType);
	}
	
	/**
	 * Initializes all the variables
	 * @param dataFieldType the type for data storage
	 */
	private void initVars(String dataFieldType) {
		this.recType = this.model.getJenaModel().createProperty(rhNameSpace, "record");
		this.idType = this.model.getJenaModel().createProperty(rhNameSpace, "idField");
		this.dataType = this.model.getJenaModel().createProperty(dataFieldType);
		this.isA = this.model.getJenaModel().createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		this.metaType = this.model.getJenaModel().createProperty(rhNameSpace, "metaData");
		this.metaRel = this.model.getJenaModel().createProperty(rhNameSpace, "metaRel");
		this.metaCal = this.model.getJenaModel().createProperty(rhNameSpace, "metaCal");
		this.metaOperation = this.model.getJenaModel().createProperty(rhNameSpace, "metaOperation");
		this.metaOperator = this.model.getJenaModel().createProperty(rhNameSpace, "metaOperator");
		this.metaMD5 = this.model.getJenaModel().createProperty(rhNameSpace, "metaMD5");
	}
	
	@Override
	public boolean addRecord(Record rec, Class<?> creator, boolean overwrite) throws IOException {
		if(!needsUpdated(rec)) {
			return false;
		}
		Resource record = getRecordResource(rec.getID());
		if(!overwrite && record != null) {
			throw new IOException("Record already exists!");
		} else if(record == null) {
			log.debug("Record did not exist...adding");
			record = this.model.getJenaModel().createResource();
		}
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(record, this.isA, this.recType));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(record, this.idType, rec.getID()));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(record, this.dataType, rec.getData()));
		this.addMetaData(rec, creator, RecordMetaDataType.written);
		return true;
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		delMetaData(recID);
		Resource r = getRecordResource(recID);
		if(r == null) {
			log.debug("Record Does Not Exist");
			return;
		}
		r.removeProperties();
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		// create query string
		String sQuery = "" + "PREFIX rhns: <" + rhNameSpace + "> \n" + "PREFIX lns: <" + this.dataType.getNameSpace() + "> \n" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + "Select ?dataField " + "WHERE { " + "  ?record rdf:type rhns:" + this.recType.getLocalName() + " . " + "  ?record rhns:" + this.idType.getLocalName() + " \"" + recID + "\" . " + "  ?record lns:" + this.dataType.getLocalName() + " ?dataField . " + "}";
		
		// create query
		Query query = QueryFactory.create(sQuery);
		
		// execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, this.model.getJenaModel());
		ResultSet resultSet = qe.execSelect();
		
		// read first result
		String data = null;
		if(resultSet.hasNext()) {
			QuerySolution result = resultSet.next();
			data = result.getLiteral(resultSet.getResultVars().get(0)).getString();
		} else {
			throw new IllegalArgumentException("Record " + recID + " does not exist!");
		}
		return data;
	}
	
	/**
	 * Retrieves a record's resource from jena model
	 * @param recID the record id to retrieve
	 * @return the resource
	 */
	private Resource getRecordResource(String recID) {
		List<Statement> a = this.model.getJenaModel().listStatements(null, this.idType, recID).toList();
		if(a == null || a.isEmpty()) {
			return null;
		}
		return a.get(0).getSubject();
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
			String sQuery = "" + "PREFIX rhns: <" + JenaRecordHandler.rhNameSpace + "> \n" + "PREFIX lns: <" + JenaRecordHandler.this.dataType.getNameSpace() + "> \n" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "Select ?idField \n" + "WHERE { \n" + "  ?record rdf:type rhns:" + JenaRecordHandler.this.recType.getLocalName() + " . \n" + "  ?record rhns:" + JenaRecordHandler.this.idType.getLocalName() + " ?idField . \n" + "  ?record lns:" + JenaRecordHandler.this.dataType.getLocalName() + " ?dataField . \n" + "}";
			
			// create query
			Query query = QueryFactory.create(sQuery);
			
			// execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, JenaRecordHandler.this.model.getJenaModel());
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
	public void setParams(Map<String, String> params) throws IOException {
		String dataFieldType = getParam(params, "dataFieldType", true);
		String jenaConfig = getParam(params, "jenaConfig", false);
		if(jenaConfig != null) {
			this.model = JenaConnect.parseConfig(jenaConfig);
		} else {
			Properties overrideParams = new Properties();
			for(String param : params.keySet()) {
				overrideParams.setProperty(param, params.get(param));
			}
			this.model = JenaConnect.parseConfig((String)null, overrideParams);
		}
		initVars(dataFieldType);
	}
	
	@Override
	protected void addMetaData(Record rec, RecordMetaData rmd) throws IOException {
		Resource record = getRecordResource(rec.getID());
		if(record == null) {
			throw new IOException("Record " + rec.getID() + " does not exist!");
		}
		Resource metaData = this.model.getJenaModel().createResource();
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(metaData, this.isA, this.metaType));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(metaData, this.metaRel, record));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(metaData, this.metaCal, rmd.getDate().getTimeInMillis() + ""));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(metaData, this.metaOperation, rmd.getOperation().toString()));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(metaData, this.metaOperator, rmd.getOperator().getName()));
		this.model.getJenaModel().add(this.model.getJenaModel().createStatement(metaData, this.metaMD5, rmd.getMD5()));
	}
	
	@Override
	protected void delMetaData(String recID) throws IOException {
		Resource r = getRecordResource(recID);
		if(r == null) {
			throw new IOException("No Matching Record Found For Which To Delete MetaData");
		}
		List<Resource> list = this.model.getJenaModel().listResourcesWithProperty(this.metaRel, r).toList();
		if(list == null || list.isEmpty()) {
			log.debug("No Metadata to delete");
			return;
		}
		for(Resource metaRes : list) {
			metaRes.removeProperties();
		}
	}
	
	@Override
	protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
		SortedSet<RecordMetaData> retVal = new TreeSet<RecordMetaData>();
		
		Resource r = getRecordResource(recID);
		if(r == null) {
			throw new IOException("No Matching Record Found For Which To Retrieve MetaData");
		}
		List<Resource> list = this.model.getJenaModel().listResourcesWithProperty(this.metaRel, r).toList();
		if(list == null || list.isEmpty()) {
			throw new IOException("No Matching MetaData Found");
		}
		for(Resource metaRes : list) {
			try {
				// Get Calendar
				String strCal = metaRes.getRequiredProperty(this.metaCal).getString();
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
				cal.setTimeInMillis(Long.parseLong(strCal));
				// Get Operation
				String strOperation = metaRes.getRequiredProperty(this.metaOperation).getString();
				RecordMetaDataType operation = RecordMetaDataType.valueOf(strOperation);
				// Get Operator
				String strOperator = metaRes.getRequiredProperty(this.metaOperator).getString();
				Class<?> operator = Class.forName(strOperator);
				// Get MD5
				String md5 = metaRes.getRequiredProperty(this.metaMD5).getString();
				retVal.add(new RecordMetaData(cal, operator, operation, md5));
			} catch(PropertyNotFoundException e) {
				throw new IOException("Property Not Found", e);
			} catch(ClassNotFoundException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		return retVal;
	}
	
	@Override
	public void close() throws IOException {
		this.model.close();
	}

	@Override
	public List<String> find(String idText) {
		List<String> retVal = new LinkedList<String>();
		String query = ""+
		"PREFIX rhns: <" + JenaRecordHandler.rhNameSpace + ">"+"\n"+
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+"\n"+
		"SELECT ?idField"+"\n"+
		"WHERE {"+"\n\t"+
			"?record rdf:type rhns:" + JenaRecordHandler.this.recType.getLocalName() + " ."+"\n\t"+
			"?record rhns:" + JenaRecordHandler.this.idType.getLocalName() + " ?idField ."+"\n\t"+
			"FILTER regex(?idField, \"" + idText + "\")"+"\n"+
		"}";
		for(QuerySolution record : IterableAdaptor.adapt(this.model.executeSelectQuery(query))) {
			retVal.add(record.getLiteral("idField").getString());
		}
		return retVal;
	}
}
