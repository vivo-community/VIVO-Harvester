package org.vivoweb.harvester.extractdspace.vocab.vivo;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class SKOS2 {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://www.w3.org/2008/05/skos#";
    /**
     * <p>The ontology model that holds the vocabulary terms</p>
     */
    private static final OntModel M_MODEL =
        ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final AnnotationProperty editorialNote =
        M_MODEL.createAnnotationProperty("http://www.w3.org/2008/05/skos#editorialNote");
    public static final AnnotationProperty scopeNote =
        M_MODEL.createAnnotationProperty("http://www.w3.org/2008/05/skos#scopeNote");

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
