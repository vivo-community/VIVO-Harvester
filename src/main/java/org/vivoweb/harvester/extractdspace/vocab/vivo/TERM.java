package org.vivoweb.harvester.extractdspace.vocab.vivo;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class TERM {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://purl.org/dc/terms/";
    /**
     * <p>The ontology model that holds the vocabulary terms</p>
     */
    private static final OntModel M_MODEL =
        ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final ObjectProperty contributor =
        M_MODEL.createObjectProperty("http://purl.org/dc/terms/contributor");
    public static final AnnotationProperty description =
        M_MODEL.createAnnotationProperty("http://purl.org/dc/terms/description");
    public static final AnnotationProperty source =
        M_MODEL.createAnnotationProperty("http://purl.org/dc/terms/source");

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
