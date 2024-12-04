/**
 *
 */

package org.vivoweb.harvester.extractdspace.transformation;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
 */
public class DspaceItemParser {

    private static final Logger logger = LoggerFactory.getLogger(DspaceItemParser.class);

    public static void main(String[] args) throws JsonProcessingException {
        Item anItem = new Item();
        anItem.setId("123456789_68");
        String itemUri =
            "http://dspacevivo.vivoweb.org/individual/123456789_68";
        anItem.setUri(itemUri);
        anItem.setUrl("http://localhost:8080/server/rdf/resource/123456789/68");
        List<String> arrayList = new ArrayList<String>();
        arrayList.add("http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png");
        anItem.setDspaceBitstreamURLs(arrayList);
        anItem.addDspaceIsPartOfCollectionIDItem("123456789_62");
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "dc:date", "2022-04-02T06:28:50Z",
                "xsd:dateTime"));
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "dcterms:available",
                "2022-04-02T06:28:50Z", "xsd:dateTime"));
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "dcterms:hasPart",
                "http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png",
                "xsd:anyURI"));
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "dcterms:isPartOf",
                "http://localhost:8080/server/rdf/resource/123456789/62", "xsd:anyURI"));
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "dcterms:issued", "2022", null));
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "dcterms:title", "Test thesis",
                null));
        anItem.addListOfStatementLiteralsItem(
            DSpaceStatementHelper.createStatementLiteral(itemUri, "bibo:uri",
                "http://localhost:4000/handle/123456789/68", "xsd:anyURI"));
        DspaceItemParser parser = new DspaceItemParser();
        String prettyRepo = DSpaceObjectMapperHelper.map(anItem);
        logger.info(prettyRepo);
        Model repoModel = parser.parse(anItem);
        ParserHelper.dumpStdoutModel(repoModel);
    }

    public Model parse(Item anItem) {
        Model model = ModelFactory.createDefaultModel();
        PrefixMapping prefixMapping = ParserHelper.getVIVOPrefixes();
        model.setNsPrefixes(prefixMapping);
        Resource anItemURI = ResourceFactory.createResource(anItem.getUri());
        Resource anItemURI_vcard = ResourceFactory.createResource(anItem.getUri() + "-vcard");
        Resource anItemURI_vcard_url =
            ResourceFactory.createResource(anItem.getUri() + "-vcard-url");

        model.add(anItemURI, RDF.type, OBO.BFO_0000031);
        model.add(anItemURI, RDF.type, OBO.IAO_0000030);
        model.add(anItemURI, RDF.type, OBO.BFO_0000001);
        model.add(anItemURI, RDF.type, OBO.BFO_0000002);
        model.add(anItemURI, RDF.type, OWL.Thing);
        model.add(anItemURI, RDF.type, BIBO.Document);
        model.add(anItemURI, VITRO.mostSpecificType, BIBO.Document);
        model.add(anItemURI, OBO.ARG_2000028, anItemURI_vcard);
        try {
            Literal biteStream =
                ResourceFactory.createTypedLiteral(anItem.getDspaceBitstreamURLs().get(0),
                    XSDDatatype.XSDanyURI);
            model.add(anItemURI, DSPACE.hasBitstream, biteStream);
        } catch (Exception ignored) {
        }

        model.add(anItemURI_vcard, RDF.type, VCARD.Kind);
        model.add(anItemURI_vcard, RDF.type, OBO.BFO_0000031);
        model.add(anItemURI_vcard, RDF.type, OBO.BFO_0000002);
        model.add(anItemURI_vcard, RDF.type, OWL.Thing);
        model.add(anItemURI_vcard, RDF.type, OBO.BFO_0000001);
        model.add(anItemURI_vcard, RDF.type, OBO.ARG_2000379);
        model.add(anItemURI_vcard, RDF.type, OBO.IAO_0000030);
        model.add(anItemURI_vcard, OBO.ARG_2000029, anItemURI);
        model.add(anItemURI_vcard, VCARD.hasURL, anItemURI_vcard_url);

        model.add(anItemURI_vcard_url, RDF.type, VCARD.Addressing);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.URL);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Identification);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Communication);
        model.add(anItemURI_vcard_url, RDF.type, VCARD.Explanatory);
        model.add(anItemURI_vcard_url, RDF.type, OWL.Thing);

        model.add(anItemURI_vcard_url, VITRO.mostSpecificType, VCARD.URL);
        model.add(anItemURI_vcard_url, VIVO.rank,
            ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDnonNegativeInteger));

        try {
            model.add(anItemURI_vcard_url, VCARD.url,
                ResourceFactory.createTypedLiteral(anItem.getUrl(), XSDDatatype.XSDanyURI));

        } catch (Exception e) {
            logger.info("Item : (" + anItem.getId() + ") has no URL");
        }

        List<Statement> statements = anItem.getListOfStatements();
        if (statements != null) {
            for (Statement statement : statements) {
                String subj =
                    Objects.requireNonNull(prefixMapping.expandPrefix(statement.getSubjectUri()));
                String pred =
                    Objects.requireNonNull(prefixMapping.expandPrefix(statement.getPredicateUri()));
                String obj =
                    Objects.requireNonNull(prefixMapping.expandPrefix(statement.getObjectUri()));

                model.add(model.createResource(subj), model.createProperty(pred),
                    model.createResource(obj));
            }
        }

        List<StatementLiteral> stmtls = anItem.getListOfStatementLiterals();
        if (stmtls != null) {
            for (StatementLiteral statement : stmtls) {
                String subj =
                    Objects.requireNonNull(prefixMapping.expandPrefix(statement.getSubjectUri()));
                String pred =
                    Objects.requireNonNull(prefixMapping.expandPrefix(statement.getPredicateUri()));
                String obj = Objects.requireNonNull(statement.getObjectLiteral());

                String lType = statement.getLiteralType();
                Literal aLiteral;
                if (lType != null && !lType.isEmpty()) {
                    XSDDatatype dataType = ParserHelper.parseDataType(lType);
                    aLiteral = ResourceFactory.createTypedLiteral(obj, dataType);
                } else {
                    aLiteral = ResourceFactory.createPlainLiteral(obj);
                }

                if (pred.contains("title") && pred.contains("terms")) {
                    model.add(model.createResource(subj), RDFS.label, aLiteral);
                } else if (pred.contains("description") && pred.contains("terms")) {
                    model.add(model.createResource(subj), BIBO.abstract_, aLiteral);
                } else if (pred.contains("subject")) {
                    model.add(model.createResource(subj), VIVO.freetextKeyword, aLiteral);
                }
                model.add(model.createResource(subj), model.createProperty(pred), aLiteral);
            }
        }

        return model;
    }
}
