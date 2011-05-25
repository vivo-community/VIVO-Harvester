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
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.IterableAide;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * Changes the namespace for all matching uris
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ChangeNamespace {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ChangeNamespace.class);
	
	/**
	 * Gets an unused URI in the the given namespace for the given models
	 * @param namespace the namespace
	 * @param models models to check in
	 * @return the uri
	 * @throws IOException error connecting
	 */
	public static String getUnusedURI(String namespace, JenaConnect... models) throws IOException {
		if((namespace == null) || namespace.equals("")) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		String uri = null;
		Random random = new Random();
		while(uri == null) {
			uri = namespace + "n" + random.nextInt(Integer.MAX_VALUE);
			log.trace("evaluating uri <" + uri + ">");
			for(JenaConnect model : models) {
				boolean modelContains = model.containsURI(uri);
				log.trace("model <" + model.getModelName() + "> contains this uri?: " + modelContains);
				if(modelContains) {
					uri = null;
					break;
				}
			}
		}
		log.debug("Using new URI: <" + uri + ">");
		return uri;
	}
	
	/**
	 * Changes the namespace for all matching uris
	 * @param model the model to change namespaces for
	 * @param vivo the model to search for uris in
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @param errorLog log error messages for changed nodes
	 * @throws IOException error connecting
	 */
	public static void changeNS(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, boolean errorLog) throws IOException {
		if(model == null) {
			throw new IllegalArgumentException("No input model provided! Must provide an input model");
		}
		if(vivo == null) {
			throw new IllegalArgumentException("No vivo model provided! Must provide a vivo model");
		}
		if((oldNamespace == null) || oldNamespace.trim().equals("")) {
			throw new IllegalArgumentException("old namespace cannot be empty");
		}
		if((newNamespace == null) || newNamespace.trim().equals("")) {
			throw new IllegalArgumentException("new namespace cannot be empty");
		}
		if(oldNamespace.trim().equals(newNamespace.trim())) {
			log.trace("namespaces are equal, nothing to change");
			return;
		}
		batchRename(model, vivo, oldNamespace.trim(), newNamespace.trim(), errorLog);
	}
	
	/**
	 * Rename unmatched resources from a given namespace in the given model to another (vivo) model
	 * @param model the model to change namespaces for
	 * @param vivo the model to search for uris in
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @param errorLog log error messages for changed nodes
	 * @throws IOException error connecting
	 */
	private static void batchRename(JenaConnect model, JenaConnect vivo, String oldNamespace, String newNamespace, boolean errorLog) throws IOException {
		//Grab all resources matching namespaces needing changed
		String subjectQuery = "" + 
		"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + 
		"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" + 
		"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> \n" + 
		"PREFIX owl:   <http://www.w3.org/2002/07/owl#> \n" + 
		"PREFIX swrl:  <http://www.w3.org/2003/11/swrl#> \n" + 
		"PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#> \n" + 
		"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" + 
		"PREFIX bibo: <http://purl.org/ontology/bibo/> \n" + 
		"PREFIX dcelem: <http://purl.org/dc/elements/1.1/> \n" + 
		"PREFIX dcterms: <http://purl.org/dc/terms/> \n" + 
		"PREFIX event: <http://purl.org/NET/c4dm/event.owl#> \n" + 
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" + 
		"PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#> \n" + 
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n" + 
		"PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> \n" + 
		"PREFIX core: <http://vivoweb.org/ontology/core#> \n" + 
		"\n" + 
		"SELECT ?sub \n" + 
		"WHERE {\n" + 
		"\t" + "?sub ?p ?o . \n" + 
		"\t" + "FILTER regex(str(?sub), \"" + oldNamespace + "\" ) \n" + "} ORDER BY ?sub";
		log.debug("Change Query:\n" + subjectQuery);
		
		Set<String> changeArray = new TreeSet<String>();
		for(QuerySolution solution : IterableAide.adapt(model.executeSelectQuery(subjectQuery))) {
			String renameURI = solution.getResource("sub").getURI();
			changeArray.add(renameURI);
		}
		
		int total = changeArray.size();
		int count = 0;
		for(String sub : changeArray) {
			count++;
			Resource res = model.getJenaModel().getResource(sub);
			float percent = Math.round(10000f * count / total) / 100f;
			log.trace("(" + count + "/" + total + ": " + percent + "%): Finding unused URI for resource <" + res + ">");
			String uri = getUnusedURI(newNamespace, vivo, model);
			if(errorLog) {
				log.warn("Resource <" + res.getURI() + "> was found and renamed to new uri <" + uri + ">!");
			}
			ResourceUtils.renameResource(res, uri);
		}
		log.info("Changed namespace for " + changeArray.size() + " rdf nodes");
	}
}
