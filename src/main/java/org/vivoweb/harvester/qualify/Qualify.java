/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.IterableAide;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Qualify data using SPARQL queries
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 * @author Nicholas Skaggs (nskaggs@ctrip.ufl.edu)
 */
public class Qualify {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Qualify.class);
	/**
	 * Jena Model we are working in
	 */
	private final JenaConnect model;
	
	/**
	 * Constructor
	 * @param jenaModel the JENA model to run qualifications on
	 */
	public Qualify(JenaConnect jenaModel) {
		this.model = jenaModel;
		if(this.model == null) {
			throw new IllegalArgumentException("Must provide a jena model");
		}
	}
	
	/**
	 * Replace records exactly matching uri & datatype & oldValue with newValue
	 * @param dataType data type to match
	 * @param oldValue old value to match
	 * @param newValue new value to set
	 */
	public void strReplace(String dataType, String oldValue, String newValue) {
		StmtIterator stmtItr = this.model.getJenaModel().listStatements(null, this.model.getJenaModel().createProperty(dataType), oldValue);
		ArrayList<Statement> statements = new ArrayList<Statement>();
		while(stmtItr.hasNext()) {
			statements.add(stmtItr.next());
		}
		for(Statement stmt : statements) {
			log.trace("Replacing record");
			log.debug("oldValue: " + oldValue);
			log.debug("newValue: " + newValue);
			stmt.changeObject(newValue);
		}
	}
	
	/**
	 * Replace records matching predicate & the regexMatch with newValue
	 * @param predicate data type to match
	 * @param regexMatch regular expression to match
	 * @param newValue new value
	 * @throws IOException error connecting
	 */
	public void regexReplace(String predicate, String regexMatch, String newValue) throws IOException {
		String query = "" + "SELECT ?s ?o \n" 
						  + "WHERE {\n" 
						  + "  ?s <" + predicate + "> ?o .\n" 
						  + "  FILTER (regex(str(?o), \"" + regexMatch + "\", \"s\")) .\n" + "}";
		log.debug(query);
		StringBuilder insertQ = new StringBuilder("INSERT DATA {\n");
		StringBuilder deleteQ = new StringBuilder("DELETE DATA {\n");
		for(QuerySolution s : IterableAide.adapt(this.model.executeSelectQuery(query))) {
			Literal obj = s.getLiteral("o");
			RDFDatatype datatype = obj.getDatatype();
			String lang = obj.getLanguage();
			String objStr = obj.getValue().toString();
			String oldStr = encodeString(objStr, datatype, lang);
			log.trace("Replacing record");
			log.debug("oldValue: " + oldStr);
			String newStr = encodeString(objStr.replaceAll(regexMatch, newValue), datatype, lang);
			String sUri = s.getResource("s").getURI();
			log.debug("newValue: " + newStr);
			deleteQ.append("  <" + sUri + "> <" + predicate + "> " + oldStr + " .\n");
			insertQ.append("  <" + sUri + "> <" + predicate + "> " + newStr + " .\n");
		}
		insertQ.append("}");
		deleteQ.append("}");
		log.debug("Removing old data:\n" + deleteQ);
		this.model.executeUpdateQuery(deleteQ.toString());
		log.debug("Inserting updated data:\n" + insertQ);
		this.model.executeUpdateQuery(insertQ.toString());
	}
	
	/**
	 * Encode a string with its rdfdatatype or lang
	 * @param str the string
	 * @param datatype the datatype
	 * @param lang the language
	 * @return the encoded string
	 */
	private static String encodeString(String str, RDFDatatype datatype, String lang) {
		String encStr = "\"" + str + "\"";
		if(datatype != null) {
			encStr += "^^<" + datatype.getURI().trim() + ">";
		} else if(StringUtils.isNotBlank(lang)) {
			encStr += "@" + lang.trim();
		}
		return encStr;
	}
	
	/**
	 * Remove all subjects and objects in a given namespace
	 * @param ns the namespace to remove all resources from
	 * @throws IOException error connecting
	 */
	public void cleanResources(String ns) throws IOException {
		String query = "" + "DELETE { ?s ?p ?o } " 
		+ "WHERE { " + "?s ?p ?o .  " 
		+ "FILTER (regex(str(?s), \"^" + ns + "\" ) || regex(str(?o), \"^" + ns + "\" ))" + "}";
		log.debug(query);
		this.model.executeUpdateQuery(query);
	}
	
	/**
	 * Remove all predicates in a given namespace
	 * @param ns the namespace to remove all predicates from
	 * @throws IOException error connecting
	 */
	public void cleanPredicates(String ns) throws IOException {
		String predicateQuery = "" + "DELETE { ?s ?p ?o } " 
		+ "WHERE { " + "?s ?p ?o .  " 
		+ "FILTER regex(str(?p), \"^" + ns + "\" ) " + "}";
		log.debug(predicateQuery);
		this.model.executeUpdateQuery(predicateQuery);
	}
}
