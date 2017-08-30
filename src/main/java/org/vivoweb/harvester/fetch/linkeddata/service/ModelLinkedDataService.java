package org.vivoweb.harvester.fetch.linkeddata.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Jena Model based LinkedDataService that gets all the 
 * SPO statements for the URI. */
public class ModelLinkedDataService implements LinkedDataService{
    Model dataModel;
    
    public ModelLinkedDataService(Model model){
        this.dataModel = model;
    }
    
    public ModelLinkedDataService( ) {
        // 
    }

    /** Adds just the SubPrdObj statements for the URI. */
    @Override
	public void getLinkedData(String uri, Model outputModel ) throws Exception{
        if( uri == null || outputModel==null ) 
            throw new Error("model and uri must not be null ");
        StmtIterator it = 
            dataModel.listStatements(ResourceFactory.createResource( uri ), (Property) null ,(RDFNode) null );
        outputModel.add(it);
    }
    
    public String getLinkedData( String uri) throws Exception {
    	 if ( uri == null  )  throw  new Error("uri must not be null ");    	 
    	 return null;    	 
    }
;

}
