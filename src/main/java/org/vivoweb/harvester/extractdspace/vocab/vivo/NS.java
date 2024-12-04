package org.vivoweb.harvester.extractdspace.vocab.vivo;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class NS {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://www.w3.org/2003/06/sw-vocab-status/ns#";
    /**
     * <p>The ontology model that holds the vocabulary terms</p>
     */
    private static final OntModel M_MODEL =
        ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final AnnotationProperty term_status = M_MODEL.createAnnotationProperty(
        "http://www.w3.org/2003/06/sw-vocab-status/ns#term_status");

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
