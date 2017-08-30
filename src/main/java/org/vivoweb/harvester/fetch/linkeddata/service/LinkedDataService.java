package org.vivoweb.harvester.fetch.linkeddata.service;

import org.apache.jena.rdf.model.Model;

/**
 * Interface for classes that can be used to get RDF linked data.
 */
public interface LinkedDataService {
    /**
     * Get the linked data for the URI and add it to Model m. 
     * @throws Exception 
     */
    public void getLinkedData( String uri, Model m ) throws Exception;
    
    public String getLinkedData( String uri) throws Exception;
}

