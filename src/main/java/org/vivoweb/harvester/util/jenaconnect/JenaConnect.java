/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.jenaconnect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.recordhandler.Record;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;

/**
 * Connection Helper for Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class JenaConnect {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JenaConnect.class);
	/**
	 * Model we are connecting to
	 */
	private Model jenaModel;
	/**
	 * The modelname
	 */
	private String modelName;
	
	/**
	 * Factory (connects to the same jena triple store as another jena connect, but uses a different named model)
	 * @param newModelName the model name to use
	 * @return the new jenaconnect
	 * @throws IOException unable to secure db connection
	 */
	public abstract JenaConnect neighborConnectClone(String newModelName) throws IOException;
	
	/**
	 * Get the size of a jena model
	 * @return the number of statement in this model
	 * @throws IOException error connecting
	 */
	public int size() throws IOException {
		ResultSet resultSet = executeSelectQuery("SELECT (count(?s) as ?size) WHERE { ?s ?p ?o }");
		// read first result
		if(resultSet.hasNext()) {
			//Display count
			return resultSet.next().get("size").asLiteral().getInt();
		}
		return 0;
	}
	
	/**
	 * Get the dataset for this connection Can be very expensive when using RDB connections (SDB, TDB, and Mem are fine)
	 * @return the database connection's dataset
	 * @throws IOException error connecting
	 */
	public abstract Dataset getDataset() throws IOException;
	
	/**
	 * Load in RDF
	 * @param in input stream to read rdf from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 */
	public void loadRdfFromStream(InputStream in, String namespace, String language) {
		getJenaModel().read(in, namespace, language);
	}
	
	/**
	 * Load the RDF from a file
	 * @param fileName the file to read from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 * @throws IOException error accessing file
	 */
	public void loadRdfFromFile(String fileName, String namespace, String language) throws IOException {
		loadRdfFromStream(FileAide.getInputStream(fileName), namespace, language);
	}
	
	/**
	 * Load in RDF
	 * @param rdf rdf string
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 */
	public void loadRdfFromString(String rdf, String namespace, String language) {
		loadRdfFromStream(new ByteArrayInputStream(rdf.getBytes()), namespace, language);
	}
	
	/**
	 * Load in RDF from a model
	 * @param jc the model to load in
	 */
	public void loadRdfFromJC(JenaConnect jc) {
		getJenaModel().add(jc.getJenaModel());
	}
	
	/**
	 * Export all RDF
	 * @param out output stream to write rdf to
	 * @throws IOException error writing to stream
	 */
	public void exportRdfToStream(OutputStream out) throws IOException {
		RDFWriter fasterWriter = this.jenaModel.getWriter("RDF/XML");
		fasterWriter.setProperty("showXmlDeclaration", "true");
		fasterWriter.setProperty("allowBadURIs", "true");
		fasterWriter.setProperty("relativeURIs", "");
		OutputStreamWriter osw = new OutputStreamWriter(out, Charset.availableCharsets().get("UTF-8"));
		fasterWriter.write(this.jenaModel, osw, "");
		osw.flush();
		out.flush();
	}
	
	/**
	 * Export all RDF
	 * @return the rdf
	 */
	public String exportRdfToString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			exportRdfToStream(baos);
		} catch(IOException e) {
			throw new Error(e);
		}
		return baos.toString();
	}
	
	/**
	 * Export the RDF to a file
	 * @param fileName the file to write to
	 * @throws IOException error writing to file
	 */
	public void exportRdfToFile(String fileName) throws IOException {
		exportRdfToStream(FileAide.getOutputStream(fileName));
	}
	
	/**
	 * Export the RDF to a file
	 * @param fileName the file to write to
	 * @param append append to the file
	 * @throws IOException error writing to file
	 */
	public void exportRdfToFile(String fileName, boolean append) throws IOException {
		exportRdfToStream(FileAide.getOutputStream(fileName, append));
	}
	
	/**
	 * Remove RDF from another JenaConnect
	 * @param inputJC the Model to read from
	 */
	public void removeRdfFromJC(JenaConnect inputJC) {
		this.jenaModel.remove(inputJC.getJenaModel());
	}
	
	/**
	 * Remove RDF from an input stream
	 * @param in input stream to read rdf from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 */
	public void removeRdfFromStream(InputStream in, String namespace, String language) {
		removeRdfFromJC(new MemJenaConnect(in, namespace, language));
	}
	
	/**
	 * Remove the RDF from a file
	 * @param fileName the file to read from
	 * @param namespace the base uri to use for imported uris
	 * @param language the language the rdf is in. Predefined values for lang are "RDF/XML", "N-TRIPLE", "TURTLE" (or
	 *        "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for
	 *        "RDF/XML"
	 * @throws IOException error connecting
	 */
	public void removeRdfFromFile(String fileName, String namespace, String language) throws IOException {
		removeRdfFromStream(FileAide.getInputStream(fileName), namespace, language);
	}
	
	/**
	 * Removes all records in a RecordHandler from the model
	 * @param rh the RecordHandler to pull records from
	 * @param namespace the base uri to use for imported uris
	 * @param language the rdf syntax language (RDF/XML, N3, TTL, etc). null = RDF/XML
	 * @return number of records removed
	 */
	public int removeRdfFromRH(RecordHandler rh, String namespace, String language) {
		int processCount = 0;
		for(Record r : rh) {
			log.trace("removing record: " + r.getID());
			if(namespace != null) {
				// log.trace("using namespace '"+namespace+"'");
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(r.getData().getBytes());
			getJenaModel().remove(new MemJenaConnect(bais, namespace, language).getJenaModel());
			try {
				bais.close();
			} catch(IOException e) {
				// ignore
			}
			processCount++;
		}
		return processCount;
	}
	
	/**
	 * Adds all records in a RecordHandler to the model
	 * @param rh the RecordHandler to pull records from
	 * @param namespace the base uri to use for imported uris
	 * @param language the rdf syntax language (RDF/XML, N3, TTL, etc).  null = RDF/XML
	 * @return number of records added
	 */
	public int loadRdfFromRH(RecordHandler rh, String namespace, String language) {
		int processCount = 0;
		for(Record r : rh) {
			log.trace("loading record: " + r.getID());
			if(namespace != null) {
				// log.trace("using namespace '"+namespace+"'");
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(r.getData().getBytes());
			getJenaModel().read(bais, namespace, language);
			try {
				bais.close();
			} catch(IOException e) {
				// ignore
			}
			processCount++;
		}
		return processCount;
	}
	
	/**
	 * Closes the model
	 */
	public void close() {
		sync();
	}
	
	/**
	 * Syncronizes the model to the datastore
	 */
	public void sync() {
		log.trace("Syncronizing the model...");
		log.trace("Syncronization of model complete");
	}
	
	/**
	 * Build a QueryExecution from a queryString
	 * @param queryString the query to build execution for
	 * @param datasetMode execute against dataset
	 * @return the QueryExecution
	 * @throws IOException error connecting
	 */
	private QueryExecution buildQueryExec(String queryString, boolean datasetMode) throws IOException {
		QueryExecution qe;
		if(datasetMode) {
			qe = QueryExecutionFactory.create(QueryFactory.create(queryString, Syntax.syntaxARQ), getDataset());
		} else {
			qe = QueryExecutionFactory.create(QueryFactory.create(queryString, Syntax.syntaxARQ), getJenaModel());
		}
		return qe;
	}
	
	/**
	 * Executes a sparql select query against the JENA model and returns the selected result set
	 * @param queryString the query to execute against the model
	 * @return the executed query result set
	 * @throws IOException error connecting
	 */
	public ResultSet executeSelectQuery(String queryString) throws IOException {
		return executeSelectQuery(queryString, false, false);
	}
	
	/**
	 * Executes a sparql select query against the JENA model and returns the selected result set
	 * @param queryString the query to execute against the model
	 * @param copyResultSet copy the resultset
	 * @param datasetMode execute against dataset
	 * @return the executed query result set
	 * @throws IOException error connecting
	 */
	public ResultSet executeSelectQuery(String queryString, boolean copyResultSet, boolean datasetMode) throws IOException {
		ResultSet rs = buildQueryExec(queryString, datasetMode).execSelect();
		if(copyResultSet) {
			rs = ResultSetFactory.copyResults(rs);
		}
		return rs;
	}
	
	/**
	 * Executes a sparql construct query against the JENA model and returns the constructed result model
	 * @param queryString the query to execute against the model
	 * @return the executed query result model
	 * @throws IOException error connecting
	 */
	public JenaConnect executeConstructQuery(String queryString) throws IOException {
		return executeConstructQuery(queryString, false);
	}
	
	/**
	 * Executes a sparql construct query against the JENA model and returns the constructed result model
	 * @param queryString the query to execute against the model
	 * @param datasetMode execute against dataset
	 * @return the executed query result model
	 * @throws IOException error connecting
	 */
	public JenaConnect executeConstructQuery(String queryString, boolean datasetMode) throws IOException {
		JenaConnect jc = new MemJenaConnect();
		jc.getJenaModel().add(buildQueryExec(queryString, datasetMode).execConstruct());
		return jc;
	}
	
	/**
	 * Executes a sparql describe query against the JENA model and returns the description result model
	 * @param queryString the query to execute against the model
	 * @return the executed query result model
	 * @throws IOException error connecting
	 */
	public JenaConnect executeDescribeQuery(String queryString) throws IOException {
		return executeDescribeQuery(queryString, false);
	}
	
	/**
	 * Executes a sparql describe query against the JENA model and returns the description result model
	 * @param queryString the query to execute against the model
	 * @param datasetMode execute against dataset
	 * @return the executed query result model
	 * @throws IOException error connecting
	 */
	public JenaConnect executeDescribeQuery(String queryString, boolean datasetMode) throws IOException {
		JenaConnect jc = new MemJenaConnect();
		jc.getJenaModel().add(buildQueryExec(queryString, datasetMode).execDescribe());
		return jc;
	}
	
	/**
	 * Executes a sparql describe query against the JENA model and returns the description result model
	 * @param queryString the query to execute against the model
	 * @return the executed query result model
	 * @throws IOException error connecting
	 */
	public boolean executeAskQuery(String queryString) throws IOException {
		return executeAskQuery(queryString, false);
	}
	
	/**
	 * Executes a sparql describe query against the JENA model and returns the description result model
	 * @param queryString the query to execute against the model
	 * @param datasetMode execute against dataset
	 * @return the executed query result model
	 * @throws IOException error connecting
	 */
	public boolean executeAskQuery(String queryString, boolean datasetMode) throws IOException {
		return buildQueryExec(queryString, datasetMode).execAsk();
	}
	
	/**
	 * Executes a sparql update query against the JENA model
	 * @param queryString the query to execute against the model
	 * @throws IOException error connecting
	 */
	public void executeUpdateQuery(String queryString) throws IOException {
		executeUpdateQuery(queryString, false);
	}
	
	/**
	 * Executes a sparql update query against the JENA model
	 * @param queryString the query to execute against the model
	 * @param datasetMode execute against dataset
	 * @throws IOException error connecting
	 */
	public void executeUpdateQuery(String queryString, boolean datasetMode) throws IOException {
		this.jenaModel.begin();
		this.jenaModel.notifyEvent(GraphEvents.startRead);
		try {
//			log.debug("query:\n" + queryString);
			if(datasetMode) {
//				log.trace("Executing query against dataset");
				UpdateAction.execute(UpdateFactory.create(queryString), getDataset());
			} else {
//				log.trace("Executing query against model");
				UpdateAction.execute(UpdateFactory.create(queryString), getJenaModel());
			}
		} finally {
			this.jenaModel.notifyEvent(GraphEvents.finishRead);
			this.jenaModel.commit();
		}
	}
	
	/**
	 * RDF formats
	 */
	protected static HashMap<String, ResultSetFormat> formatSymbols = new HashMap<String, ResultSetFormat>();
	static {
		formatSymbols.put(ResultSetFormat.syntaxXML.getSymbol(), ResultSetFormat.syntaxXML);
		formatSymbols.put(ResultSetFormat.syntaxRDF_XML.getSymbol(), ResultSetFormat.syntaxRDF_XML);
		formatSymbols.put(ResultSetFormat.syntaxRDF_N3.getSymbol(), ResultSetFormat.syntaxRDF_N3);
		formatSymbols.put(ResultSetFormat.syntaxCSV.getSymbol(), ResultSetFormat.syntaxCSV);
		formatSymbols.put(ResultSetFormat.syntaxText.getSymbol(), ResultSetFormat.syntaxText);
		formatSymbols.put(ResultSetFormat.syntaxJSON.getSymbol(), ResultSetFormat.syntaxJSON);
	}
	
	/**
	 * Execute a Query and output result to System.out
	 * @param queryParam the query
	 * @param resultFormatParam the format to return the results in ('RS_RDF',etc for select queries / 'RDF/XML',etc for
	 *        construct/describe queries)
	 * @param datasetMode run against dataset rather than model
	 * @throws IOException error writing to output
	 */
	public void executeQuery(String queryParam, String resultFormatParam, boolean datasetMode) throws IOException {
		executeQuery(queryParam, resultFormatParam, System.out, datasetMode);
	}
	
	/**
	 * Execute a Query
	 * @param queryParam the query
	 * @param resultFormatParam the format to return the results in ('RS_TEXT' default for select queries / 'RDF/XML'
	 *        default for construct/describe queries)
	 * @param output output stream to write to - null uses System.out
	 * @param datasetMode run against dataset rather than model
	 * @throws IOException error writing to output
	 */
	public void executeQuery(String queryParam, String resultFormatParam, OutputStream output, boolean datasetMode) throws IOException {
		OutputStream out = (output != null) ? output : System.out;
		QueryExecution qe = null;
		try {
			Query query = QueryFactory.create(queryParam, Syntax.syntaxARQ);
			if(datasetMode) {
//				log.trace("Executing query against dataset");
				qe = QueryExecutionFactory.create(query, getDataset());
			} else {
//				log.trace("Executing query against model");
				qe = QueryExecutionFactory.create(query, getJenaModel());
			}
			if(query.isSelectType()) {
				ResultSetFormat rsf = formatSymbols.get(resultFormatParam);
				if(rsf == null) {
					rsf = ResultSetFormat.syntaxText;
				}
				ResultSetFormatter.output(out, qe.execSelect(), rsf);
			} else if(query.isAskType()) {
				out.write((Boolean.toString(qe.execAsk())+"\n").getBytes());
			} else {
				Model resultModel = null;
				if(query.isConstructType()) {
					resultModel = qe.execConstruct();
				} else if(query.isDescribeType()) {
					resultModel = qe.execDescribe();
				} else {
					throw new IllegalArgumentException("Query Invalid: Not Select, Construct, Ask, or Describe");
				}
				
				resultModel.write(out, resultFormatParam);
			}
		} catch(QueryParseException e1) {
			try {
				executeUpdateQuery(queryParam, datasetMode);
				log.info("Update Successfully Applied");
			} catch(QueryParseException e2) {
				log.error("Invalid Query:\n"+queryParam);
				log.trace("Attempted Query Exception:",e1);
				log.trace("Attempted Update Exception:",e2);
			}
		} finally {
			if(qe != null) {
				qe.close();
			}
		}
	}
	
	/**
	 * Accessor for Jena Model
	 * @return the Jena Model
	 */
	public Model getJenaModel() {
		return this.jenaModel;
	}
	
	/**
	 * Setter
	 * @param jena the new model
	 */
	protected void setJenaModel(Model jena) {
		this.jenaModel = jena;
	}
	
	/**
	 * Checks if the model contains the given uri
	 * @param uri the uri to check for
	 * @return true if found, false otherwise
	 * @throws IOException error connecting
	 */
	public boolean containsURI(String uri) throws IOException {
		return executeAskQuery("ASK { <" + uri + "> ?p ?o }");
	}
	
	/**
	 * Remove all statements from model
	 */
	public void truncate() {
		Model sourceModel = getJenaModel();
		sourceModel.enterCriticalSection(Lock.WRITE);
		try{
			// this method is used so that any listeners can see each statement removed
			sourceModel.removeAll((Resource)null,(Property)null,(RDFNode)null);
		} finally {
			sourceModel.leaveCriticalSection();
		}
	}
	
	/**
	 * Set the modelName
	 * @param modelName the model name
	 */
	protected void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	/**
	 * Get the modelName
	 * @return the modelName
	 */
	public String getModelName() {
		return this.modelName;
	}
	
	/**
	 * Is this model empty
	 * @return true if empty, false otherwise
	 * @throws IOException error connecting
	 */
	public boolean isEmpty() throws IOException {
		return !executeAskQuery("ASK { ?s ?p ?o }");
	}
	
	/**
	 * Output the jena model information
	 */
	public void printParameters() {
		log.trace("modelName: '" + getModelName() + "'");
	}

	/**
	 * Create a new, independant, model containing all the statements in this JenaConnect which
	 * are not in another.
         The new model need not be of the same type as either this model or
         the argument model: typically it will be a memory-based model.
	 * @return a new model containing all the statements in this JenaConnect that
	 *         are not in the given JenaConnect.
	 * @param model the other JenaConnect whose statements are to be excluded.
	 */
	public JenaConnect difference(JenaConnect model) {
		JenaConnect diff = new MemJenaConnect();
		diff.getJenaModel().add(getJenaModel().difference(model.getJenaModel()));
		return diff;
	}
}
