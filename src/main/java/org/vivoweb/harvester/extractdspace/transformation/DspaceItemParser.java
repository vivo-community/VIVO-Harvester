/**
 * 
 */
package org.vivoweb.harvester.extractdspace.transformation;

import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import org.vivoweb.harvester.extractdspace.model.Item;
import org.vivoweb.harvester.extractdspace.model.Statement;
import org.vivoweb.harvester.extractdspace.model.StatementLiteral;
import org.vivoweb.harvester.extractdspace.model.util.DSpaceObjectMapperHelper;
import org.vivoweb.harvester.extractdspace.model.util.DSpaceStatementHelper;
import org.vivoweb.harvester.extractdspace.vocab.dspace.DSPACE;
import org.vivoweb.harvester.extractdspace.vocab.util.ParserHelper;
import org.vivoweb.harvester.extractdspace.vocab.vitro.VITRO;
import org.vivoweb.harvester.extractdspace.vocab.vivo.BIBO;
import org.vivoweb.harvester.extractdspace.vocab.vivo.OBO;
import org.vivoweb.harvester.extractdspace.vocab.vivo.VCARD;
import org.vivoweb.harvester.extractdspace.vocab.vivo.VIVO;

/**
 * @author heon
 *
 */
public class DspaceItemParser {
    private static Logger logger = LoggerFactory.getLogger(DspaceItemParser.class);


    /**
     * @param args
     * @throws JsonProcessingException 
     */
    public static void main(String[] args) throws JsonProcessingException {
        /*
         * The scenario to test
	<http://localhost:8080/server/rdf/resource/123456789/68>
        dspace:hasBitstream        <http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png> ;
        dspace:isPartOfCollection  <http://localhost:8080/server/rdf/resource/123456789/62> ;
        dc:date                    "2022-04-02T06:28:50Z"^^xsd:dateTime ;
        dcterms:available          "2022-04-02T06:28:50Z"^^xsd:dateTime ;
        dcterms:hasPart            <http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png> ;
        dcterms:isPartOf           <http://localhost:8080/server/rdf/resource/123456789/62> ;
        dcterms:issued             "2022" ;
        dcterms:title              "Test thesis" ;
        bibo:uri                   <http://localhost:4000/handle/123456789/68> ;
        void:sparqlEndpoint        <http://localhost/fuseki/dspace/sparql> ;
        foaf:homepage              <http://localhost:8080/> .
         */

        Item anItem = new Item();
        anItem.setId("123456789_68");
        String itemUri = "http://dspacevivo.vivoweb.org/individual/123456789_68"; // namesapce defined in runtime.properties
        anItem.setUri(itemUri);
        anItem.setUrl("http://localhost:8080/server/rdf/resource/123456789/68");
        List<String> arrayList = new ArrayList<String>();
        arrayList.add("http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png");
        anItem.setDspaceBitstreamURLs(arrayList);
        anItem.addDspaceIsPartOfCollectionIDItem("123456789_62");
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri,"dc:date","2022-04-02T06:28:50Z","xsd:dateTime"));
        anItem.addListOfStatementLiteralsItem(DSpaceStatementHelper.createStatementLiteral(itemUri,"dcterms:available","2022-04-02T06:28:50Z","xsd:dateTime"));
        anItem.addListOfStatementLiteralsItem(DSpaceStatementHelper.createStatementLiteral(itemUri,"dcterms:hasPart","http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png","xsd:anyURI"));
        anItem.addListOfStatementLiteralsItem(DSpaceStatementHelper.createStatementLiteral(itemUri,"dcterms:isPartOf","http://localhost:8080/server/rdf/resource/123456789/62","xsd:anyURI"));
        anItem.addListOfStatementLiteralsItem(DSpaceStatementHelper.createStatementLiteral(itemUri,"dcterms:issued","2022",null));
        anItem.addListOfStatementLiteralsItem(DSpaceStatementHelper.createStatementLiteral(itemUri,"dcterms:title","Test thesis",null));
        anItem.addListOfStatementLiteralsItem(DSpaceStatementHelper.createStatementLiteral(itemUri,"bibo:uri","http://localhost:4000/handle/123456789/68","xsd:anyURI"));
        DspaceItemParser parser = new DspaceItemParser();
        String prettyRepo = DSpaceObjectMapperHelper.map(anItem);
        logger.info(prettyRepo);
        Model repoModel = parser.parse(anItem);
        ParserHelper.dumpStdoutModel(repoModel);
    }

    /*
     * Example to construct
<http://dspacevivo.vivoweb.org/individual/n770>
        a                       owl:Thing , obo:BFO_0000002 , obo:IAO_0000030 , bibo:Document , vivo:Dataset , obo:BFO_0000001 , obo:BFO_0000031 , dspace:Item ;
        rdfs:label              "DSItem"@en-US ;
        obo:ARG_2000028         <http://dspacevivo.vivoweb.org/individual/n3653> ;
        vitro:mostSpecificType  dspace:Item .

<http://dspacevivo.vivoweb.org/individual/n3653>
        a                       obo:IAO_0000030 , obo:BFO_0000001 , owl:Thing , obo:BFO_0000002 , obo:BFO_0000031 , obo:ARG_2000379 , vcard:Kind ;
        obo:ARG_2000029         <http://dspacevivo.vivoweb.org/individual/n770> ;
        vitro:mostSpecificType  vcard:Kind ;
        vcard:hasURL            <http://dspacevivo.vivoweb.org/individual/n3822> .	 

<http://dspacevivo.vivoweb.org/individual/n3822>
        a                       owl:Thing , vcard:Identification , vcard:Addressing , vcard:Explanatory , vcard:Communication , vcard:URL ;
        vitro:mostSpecificType  vcard:URL ;
        vivo:rank               1 ;
        vcard:url               "http://dspace.org/1234567/0"^^xsd:anyURI .

     */
    public Model parse(Item anItem) {
        Model model = ModelFactory.createDefaultModel();
        PrefixMapping prefixMapping = ParserHelper.getVIVOPrefixes();
        model.setNsPrefixes(prefixMapping);
        Resource anItemURI = ResourceFactory.createResource(anItem.getUri());
        Resource anItemURI_vcard = ResourceFactory.createResource(anItem.getUri()+"-vcard");
        Resource anItemURI_vcard_url = ResourceFactory.createResource(anItem.getUri()+"-vcard-url");
        /*
         * Individual creation
         */
        model.add(anItemURI, RDF.type, OBO.BFO_0000031);
        model.add(anItemURI, RDF.type, OBO.IAO_0000030);
        model.add(anItemURI, RDF.type, OBO.BFO_0000001);
        model.add(anItemURI, RDF.type, OBO.BFO_0000002);
        model.add(anItemURI, RDF.type, OWL.Thing);
        model.add(anItemURI, RDF.type, BIBO.Document);
        model.add(anItemURI, RDF.type, DSPACE.Item);
        model.add(anItemURI, RDF.type, VIVO.Dataset);
        model.add(anItemURI, VITRO.mostSpecificType, DSPACE.Item);
        model.add(anItemURI, OBO.ARG_2000028, anItemURI_vcard );
        try {
            Literal biteStream = ResourceFactory.createTypedLiteral(anItem.getDspaceBitstreamURLs().get(0),XSDDatatype.XSDanyURI);
            model.add(anItemURI, DSPACE.hasBitstream, biteStream );
        } catch (Exception e) {
        }


        /*
         * Construct VCARD king Statement
         * Ex. 
		<http://dspacevivo.vivoweb.org/individual/n3653>
        		a obo:IAO_0000030 , obo:BFO_0000001 , owl:Thing , obo:BFO_0000002 , obo:BFO_0000031 , obo:ARG_2000379 , vcard:Kind ;
        		obo:ARG_2000029         <http://dspacevivo.vivoweb.org/individual/n770> ;
        		vitro:mostSpecificType  vcard:Kind ;
        		vcard:hasURL            <http://dspacevivo.vivoweb.org/individual/n3822> .	 

         */
        model.add(anItemURI_vcard, RDF.type, VCARD.Kind);
        model.add(anItemURI_vcard, RDF.type, OBO.BFO_0000031);
        model.add(anItemURI_vcard, RDF.type, OBO.BFO_0000002);
        model.add(anItemURI_vcard, RDF.type, OWL.Thing);
        model.add(anItemURI_vcard, RDF.type, OBO.BFO_0000001);
        model.add(anItemURI_vcard, RDF.type, OBO.ARG_2000379);
        model.add(anItemURI_vcard, RDF.type, OBO.IAO_0000030);
        model.add(anItemURI_vcard, OBO.ARG_2000029 , anItemURI);
        model.add(anItemURI_vcard, VCARD.hasURL, anItemURI_vcard_url);

        /*
         * Construct VCARD-URL Statement 
<http://dspacevivo.vivoweb.org/individual/n3822>
        a                       owl:Thing , vcard:Identification , vcard:Addressing , vcard:Explanatory , vcard:Communication , vcard:URL ;
        vitro:mostSpecificType  vcard:URL ;
        vivo:rank               1 ;
        vcard:url               "http://dspace.org/1234567/0"^^xsd:anyURI .

         */	
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Addressing);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.URL);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Identification);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Communication);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Explanatory);
        model.add(anItemURI_vcard_url, RDF.type, OWL.Thing);

        model.add(anItemURI_vcard_url, VITRO.mostSpecificType, VCARD.URL);
        model.add(anItemURI_vcard_url, VIVO.rank, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDnonNegativeInteger));
        try {
            model.add(anItemURI_vcard_url, VCARD.url, ResourceFactory.createTypedLiteral(anItem.getUrl(), XSDDatatype.XSDanyURI));

        } catch (Exception e) {
            logger.info("Item : ("+anItem.getId()+") has no URL");
        }

        /*
         * Manage Statements
         */
        List<Statement> stmts = anItem.getListOfStatements();
        if (stmts!=null) {
            for (Iterator iterator = stmts.iterator(); iterator.hasNext();) {
                Statement statement = (Statement) iterator.next();
                @NotNull
                String subj = prefixMapping.expandPrefix(statement.getSubjectUri());
                @NotNull
                String pred = prefixMapping.expandPrefix(statement.getPredicateUri());
                @NotNull
                String obj = prefixMapping.expandPrefix(statement.getObjectUri());
                model.add(model.createResource(subj),model.createProperty(pred), model.createResource(obj));
            }

        }
        @Valid List<StatementLiteral> stmtls = anItem.getListOfStatementLiterals();
        if (stmtls!=null) {
            for (Iterator iterator = stmtls.iterator(); iterator.hasNext();) {
                StatementLiteral statement = (StatementLiteral) iterator.next();
                @NotNull
                String subj = prefixMapping.expandPrefix(statement.getSubjectUri());
                @NotNull
                String pred = prefixMapping.expandPrefix(statement.getPredicateUri());
                @NotNull
                String obj = statement.getObjectLiteral();
                String lType = statement.getLiteralType();
                Literal aLiteral;
                if (lType != null && !lType.isEmpty()) {
                    XSDDatatype dataType = ParserHelper.parseDataType(lType);
                    aLiteral = ResourceFactory.createTypedLiteral(obj, dataType);
                } else {
                    aLiteral = ResourceFactory.createPlainLiteral(obj);
                }
                
                /*
                 * Adding Title
                 */
                if (pred.contains("title") && pred.contains("terms")) {
                    model.add(model.createResource(subj),RDFS.label, aLiteral);
                /*
                 * Adding abstract
                 */
                } else if (pred.contains("description") && pred.contains("terms")) {
                    model.add(model.createResource(subj),BIBO.abstract_, aLiteral);
                /*
                 * Adding Keywords
                 */
                } else if (pred.contains("subject")) {
                    model.add(model.createResource(subj),VIVO.freetextKeyword, aLiteral);
                } 
                model.add(model.createResource(subj),model.createProperty(pred), aLiteral);
            }
        }
        return model;
    }
}
