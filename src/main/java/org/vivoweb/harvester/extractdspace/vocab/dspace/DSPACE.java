package org.vivoweb.harvester.extractdspace.vocab.dspace;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class DSPACE {
    public static final String NS = "http://vivoweb.org/ontology/dspace#";
    public static final String VERSION_INFO =
        "Created by Michel Héon PhD; Université du Québec à Montréal; heon.michel@uqam.ca";
    private static final OntModel M_MODEL =
        ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final ObjectProperty hasCollection =
        M_MODEL.createObjectProperty("http://vivoweb.org/ontology/dspace#hasCollection");
    public static final ObjectProperty hasCommunity =
        M_MODEL.createObjectProperty("http://vivoweb.org/ontology/dspace#hasCommunity");
    public static final ObjectProperty hasItem =
        M_MODEL.createObjectProperty("http://vivoweb.org/ontology/dspace#hasItem");
    public static final ObjectProperty isPartOfCollection =
        M_MODEL.createObjectProperty("http://vivoweb.org/ontology/dspace#isPartOfCollection");
    public static final ObjectProperty isPartOfCommunity =
        M_MODEL.createObjectProperty("http://vivoweb.org/ontology/dspace#isPartOfCommunity");
    public static final ObjectProperty isPartOfRepository =
        M_MODEL.createObjectProperty("http://vivoweb.org/ontology/dspace#isPartOfRepository");
    public static final DatatypeProperty hasBitstream =
        M_MODEL.createDatatypeProperty("http://vivoweb.org/ontology/dspace#hasBitstream");
    public static final OntClass Collection =
        M_MODEL.createClass("http://vivoweb.org/ontology/dspace#Collection");
    public static final OntClass Community =
        M_MODEL.createClass("http://vivoweb.org/ontology/dspace#Community");
    public static final OntClass Item =
        M_MODEL.createClass("http://vivoweb.org/ontology/dspace#Item");
    public static final OntClass Repository =
        M_MODEL.createClass("http://vivoweb.org/ontology/dspace#Repository");

    public static String getURI() {
        return NS;
    }

}
