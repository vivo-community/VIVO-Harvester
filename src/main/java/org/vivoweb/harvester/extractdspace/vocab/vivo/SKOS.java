package org.vivoweb.harvester.extractdspace.vocab.vivo;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class SKOS {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://www.w3.org/2004/02/skos/core#";
    /**
     * <p>The ontology model that holds the vocabulary terms</p>
     */
    private static final OntModel M_MODEL =
        ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final ObjectProperty broader =
        M_MODEL.createObjectProperty("http://www.w3.org/2004/02/skos/core#broader");
    public static final ObjectProperty narrower =
        M_MODEL.createObjectProperty("http://www.w3.org/2004/02/skos/core#narrower");
    public static final ObjectProperty related =
        M_MODEL.createObjectProperty("http://www.w3.org/2004/02/skos/core#related");
    public static final AnnotationProperty scopeNote =
        M_MODEL.createAnnotationProperty("http://www.w3.org/2004/02/skos/core#scopeNote");
    public static final OntClass Concept =
        M_MODEL.createClass("http://www.w3.org/2004/02/skos/core#Concept");

    /**
     * <p>The namespace of the vocabulary as a string</p>
     *
     * @return namespace as String
     * @see #NS
     */
    public static String getURI() {
        return NS;
    }

}
