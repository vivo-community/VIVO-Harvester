/**
 *
 */

package org.vivoweb.harvester.extractdspace.transformation;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.jena.vocabulary.XSD;
import org.vivoweb.harvester.extractdspace.model.Repository;
import org.vivoweb.harvester.extractdspace.model.Statement;
import org.vivoweb.harvester.extractdspace.model.StatementLiteral;
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
public class DspaceRepositoryParser {

    public static void main(String[] args) throws JsonProcessingException {
        Repository repo = new Repository();
        repo.setId("123456789_0");
        repo.setUri("http://localhost:8080/server/rdf/resource/123456789/0");
        Statement stmt = new Statement();
        stmt.setSubjectUri("dspace:123456789_0");
        stmt.setPredicateUri("dcterms:hasPart");
        stmt.setObjectUri("http://localhost:8080/server/rdf/resource/123456789/1");
        StatementLiteral stmtLit = new StatementLiteral();
        stmtLit.setSubjectUri("dspace:123456789_0");
        stmtLit.setPredicateUri("rdf:label");
        stmtLit.setObjectLiteral("a repository for testing");
        stmtLit.setLiteralType(XSD.xstring.getURI());

        repo.addListOfStatementsItem(stmt);
        repo.addListOfStatementLiteralsItem(stmtLit);
    }

    private Model parse(Repository repo) {
        Model model = ModelFactory.createDefaultModel();
        PrefixMapping prefixMapping = ParserHelper.getVIVOPrefixes();
        model.setNsPrefixes(prefixMapping);
        Resource orgdURI = ResourceFactory.createResource(DSPACE.getURI() + repo.getId());
        Resource orgdURI_vcard =
            ResourceFactory.createResource(DSPACE.getURI() + repo.getId() + "-vcard");
        Resource orgdURI_vcard_url =
            ResourceFactory.createResource(DSPACE.getURI() + repo.getId() + "-vcard-url");

        model.add(orgdURI, RDF.type, OBO.BFO_0000031);
        model.add(orgdURI, RDF.type, OBO.IAO_0000030);
        model.add(orgdURI, RDF.type, OBO.BFO_0000001);
        model.add(orgdURI, RDF.type, OBO.BFO_0000002);
        model.add(orgdURI, RDF.type, OWL.Thing);
        model.add(orgdURI, RDF.type, BIBO.Document);
        model.add(orgdURI, RDF.type, DSPACE.Repository);
        model.add(orgdURI, VITRO.mostSpecificType, DSPACE.Repository);
        model.add(orgdURI, OBO.ARG_2000028, orgdURI_vcard);
        List<Statement> stmts = repo.getListOfStatements();

        for (Statement statement : stmts) {
            @NotNull
            String subj = prefixMapping.expandPrefix(statement.getSubjectUri());
            @NotNull
            String pred = prefixMapping.expandPrefix(statement.getPredicateUri());
            @NotNull
            String obj = prefixMapping.expandPrefix(statement.getObjectUri());
            model.add(model.createResource(subj), model.createProperty(pred),
                model.createResource(obj));
        }

        @Valid List<StatementLiteral> stmtls = repo.getListOfStatementLiterals();
        for (StatementLiteral statement : stmtls) {
            @NotNull
            String subj = prefixMapping.expandPrefix(statement.getSubjectUri());
            @NotNull
            String pred = prefixMapping.expandPrefix(statement.getPredicateUri());
            @NotNull
            String obj = statement.getObjectLiteral();
            Literal aLiteral = ResourceFactory.createTypedLiteral(obj, XSDDatatype.XSDstring);
            model.add(model.createResource(subj), model.createProperty(pred), aLiteral);
        }

        model.add(orgdURI_vcard, RDF.type, VCARD.Kind);
        model.add(orgdURI_vcard, RDF.type, OBO.BFO_0000031);
        model.add(orgdURI_vcard, RDF.type, OBO.BFO_0000002);
        model.add(orgdURI_vcard, RDF.type, OWL.Thing);
        model.add(orgdURI_vcard, RDF.type, OBO.BFO_0000001);
        model.add(orgdURI_vcard, RDF.type, OBO.ARG_2000379);
        model.add(orgdURI_vcard, RDF.type, OBO.IAO_0000030);
        model.add(orgdURI_vcard, OBO.ARG_2000029, orgdURI);
        model.add(orgdURI_vcard, VCARD.hasURL, orgdURI_vcard_url);

        model.add(orgdURI_vcard_url, RDF.type, VCARD.Addressing);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.URL);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Identification);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Communication);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Explanatory);
        model.add(orgdURI_vcard_url, RDF.type, OWL.Thing);

        model.add(orgdURI_vcard_url, VITRO.mostSpecificType, VCARD.URL);
        model.add(orgdURI_vcard_url, VIVO.rank,
            ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDnonNegativeInteger));
        model.add(orgdURI_vcard_url, VCARD.url, ResourceFactory.createResource(repo.getUri()));

        ParserHelper.dumpStdoutModel(model);
        return null;
    }

}
