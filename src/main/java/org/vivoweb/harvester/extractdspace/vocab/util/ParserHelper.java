package org.vivoweb.harvester.extractdspace.vocab.util;


import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.vivoweb.harvester.extractdspace.vocab.dspace.DSPACE;

public class ParserHelper {
    public static String SparqlPrefix =
        "PREFIX  crdc: <http://purl.org/uqam.ca/vocabulary/crdc_ccrd#> \n" +
            " PREFIX  ocrer: <http://purl.org/net/OCRe/research.owl#> \n" +
            " PREFIX  p3:   <http://vivoweb.org/ontology/vitroAnnotfr_CA#> \n" +
            " PREFIX  owl:  <http://www.w3.org/2002/07/owl#> \n" +
            " PREFIX  scires: <http://vivoweb.org/ontology/scientific-research#> \n" +
            " PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
            " PREFIX  swrlb: <http://www.w3.org/2003/11/swrlb#> \n" +
            " PREFIX  skos: <http://www.w3.org/2004/02/skos/core#> \n" +
            " PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            " PREFIX  ocresd: <http://purl.org/net/OCRe/study_design.owl#> \n" +
            " PREFIX  swo:  <http://www.ebi.ac.uk/efo/swo/> \n" +
            " PREFIX  cito: <http://purl.org/spar/cito/> \n" +
            " PREFIX  geo:  <http://aims.fao.org/aos/geopolitical.owl#> \n" +
            " PREFIX  ocresst: <http://purl.org/net/OCRe/statistics.owl#> \n" +
            " PREFIX  dcterms: <http://purl.org/dc/terms/> \n" +
            " PREFIX  vivo: <http://vivoweb.org/ontology/core#> \n" +
            " PREFIX  text: <http://jena.apache.org/text#> \n" +
            " PREFIX  event: <http://purl.org/NET/c4dm/event.owl#> \n" +
            " PREFIX  vann: <http://purl.org/vocab/vann/> \n" +
            " PREFIX  foaf: <http://xmlns.com/foaf/0.1/> \n" +
            " PREFIX  c4o:  <http://purl.org/spar/c4o/> \n" +
            " PREFIX  fabio: <http://purl.org/spar/fabio/> \n" +
            " PREFIX  swrl: <http://www.w3.org/2003/11/swrl#> \n" +
            " PREFIX  vcard: <http://www.w3.org/2006/vcard/ns#> \n" +
            " PREFIX  crdc-data: <http://purl.org/uqam.ca/vocabulary/crdc-ccrd/individual#> \n" +
            " PREFIX  vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
            " PREFIX  vitro-public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#> \n" +
            " PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            " PREFIX  ocresp: <http://purl.org/net/OCRe/study_protocol.owl#> \n" +
            " PREFIX  bibo: <http://purl.org/ontology/bibo/> \n" +
            " PREFIX  obo:  <http://purl.obolibrary.org/obo/> \n" +
            " PREFIX  dspace:  <http://vivoweb.org/ontology/dspace#> \n" +
            " PREFIX  dc: <http://purl.org/dc/elements/1.1/> \n" +
            " PREFIX  ro:   <http://purl.obolibrary.org/obo/ro.owl#> \n";

    public static PrefixMapping getVIVOPrefixes() {
        PrefixMappingImpl globalPrefixMapping = new PrefixMappingImpl();
        globalPrefixMapping.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        globalPrefixMapping.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        globalPrefixMapping.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        globalPrefixMapping.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        globalPrefixMapping.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        globalPrefixMapping.setNsPrefix("obo", "http://purl.obolibrary.org/obo/");
        globalPrefixMapping.setNsPrefix("study_protocol",
            "http://purl.org/net/OCRe/study_protocol.owl#");
        globalPrefixMapping.setNsPrefix("ns", "http://www.w3.org/2003/06/sw-vocab-status/ns#");
        globalPrefixMapping.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        globalPrefixMapping.setNsPrefix("geopolitical",
            "http://aims.fao.org/aos/geopolitical.owl#");
        globalPrefixMapping.setNsPrefix("vitro", "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#");
        globalPrefixMapping.setNsPrefix("vitro-public",
            "http://vitro.mannlib.cornell.edu/ns/vitro/public#");
        globalPrefixMapping.setNsPrefix("skos2", "http://www.w3.org/2008/05/skos#");
        globalPrefixMapping.setNsPrefix("core", "http://vivoweb.org/ontology/core#");
        globalPrefixMapping.setNsPrefix("vivo", "http://vivoweb.org/ontology/core#");
        globalPrefixMapping.setNsPrefix("terms", "http://purl.org/dc/terms/");
        globalPrefixMapping.setNsPrefix("statistics", "http://purl.org/net/OCRe/statistics.owl#");
        globalPrefixMapping.setNsPrefix("scires",
            "http://vivoweb.org/ontology/scientific-research#");
        globalPrefixMapping.setNsPrefix("swrlb", "http://www.w3.org/2003/11/swrlb#");
        globalPrefixMapping.setNsPrefix("swrl", "http://www.w3.org/2003/11/swrl#");
        globalPrefixMapping.setNsPrefix("ocresd", "http://purl.org/net/OCRe/study_design.owl#");
        globalPrefixMapping.setNsPrefix("swo", "http://www.ebi.ac.uk/efo/swo/");
        globalPrefixMapping.setNsPrefix("cito", "http://purl.org/spar/cito/");
        globalPrefixMapping.setNsPrefix("geo", "http://aims.fao.org/aos/geopolitical.owl#");
        globalPrefixMapping.setNsPrefix("ocresst", "http://purl.org/net/OCRe/statistics.owl#");
        globalPrefixMapping.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        globalPrefixMapping.setNsPrefix("text", "http://jena.apache.org/text#");
        globalPrefixMapping.setNsPrefix("event", "http://purl.org/NET/c4dm/event.owl#");
        globalPrefixMapping.setNsPrefix("vann", "http://purl.org/vocab/vann/");
        globalPrefixMapping.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        globalPrefixMapping.setNsPrefix("c4o", "http://purl.org/spar/c4o/");
        globalPrefixMapping.setNsPrefix("fabio", "http://purl.org/spar/fabio/");
        globalPrefixMapping.setNsPrefix("vcard", "http://www.w3.org/2006/vcard/ns#");
        globalPrefixMapping.setNsPrefix("ocresp", "http://purl.org/net/OCRe/study_protocol.owl#");
        globalPrefixMapping.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        globalPrefixMapping.setNsPrefix("sfnc", "http://vivoweb.org/sparql/function#");
        globalPrefixMapping.setNsPrefix("crdc-ccrd-data",
            "http://purl.org/uqam.ca/vocabulary/crdc-ccrd/individual#");
        globalPrefixMapping.setNsPrefix("crdc-data",
            "http://purl.org/uqam.ca/vocabulary/crdc-ccrd/individual#");
        globalPrefixMapping.setNsPrefix("crdc-ccrd",
            "http://purl.org/uqam.ca/vocabulary/crdc_ccrd#");
        globalPrefixMapping.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
        globalPrefixMapping.setNsPrefix("dspace", DSPACE.NS);
        return globalPrefixMapping;
    }

    public static String dumpModel(Model aModel) {
        ByteArrayOutputStream modelString = new ByteArrayOutputStream();
        RDFDataMgr.write(modelString, aModel, Lang.TURTLE);
        return new String(modelString.toByteArray());
    }

    public static void dumpStdoutModel(Model aModel) {
        RDFDataMgr.write(System.out, aModel, Lang.TURTLE);
    }

    public static String dumpModelNtriples(Model aModel) {
        ByteArrayOutputStream modelString = new ByteArrayOutputStream();
        RDFDataMgr.write(modelString, aModel, Lang.NTRIPLES);
        return new String(modelString.toByteArray());
    }

    public static void dumpStdoutModelNtriples(Model aModel) {
        RDFDataMgr.write(System.out, aModel, Lang.NTRIPLES);
    }

    public static XSDDatatype parseDataType(String literalType) {
        String lt =
            literalType.replace("xsd:", "").replace("http://www.w3.org/2001/XMLSchema#", "");
        return new XSDDatatype(lt);
    }

    public static void main(String[] args) {
        XSDDatatype val = ParserHelper.parseDataType("xsd:string");
        System.out.println(val);

    }
}
