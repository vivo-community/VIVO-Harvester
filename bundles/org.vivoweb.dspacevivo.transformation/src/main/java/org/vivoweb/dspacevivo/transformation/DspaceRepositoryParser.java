/**
 * 
 */
package org.vivoweb.dspacevivo.transformation;

import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.BaseDatatype.TypedValue;
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
import org.vivoweb.dspacevivo.model.Item;
import org.vivoweb.dspacevivo.model.Repository;
import org.vivoweb.dspacevivo.model.Statement;
import org.vivoweb.dspacevivo.model.StatementLiteral;
import org.vivoweb.dspacevivo.model.util.DSpaceObjectMapperHelper;
import org.vivoweb.dspacevivo.vocab.dspace.DSPACE;
import org.vivoweb.dspacevivo.vocab.util.ParserHelper;
import org.vivoweb.dspacevivo.vocab.vitro.VITRO;
import org.vivoweb.dspacevivo.vocab.vivo.BIBO;
import org.vivoweb.dspacevivo.vocab.vivo.OBO;
import org.vivoweb.dspacevivo.vocab.vivo.VCARD;
import org.vivoweb.dspacevivo.vocab.vivo.VIVO;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author heon
 *
 */
public class DspaceRepositoryParser {

    /**
     * @param args
     * @throws JsonProcessingException 
     */
    public static void main(String[] args) throws JsonProcessingException {
        Repository repo = new Repository();
        repo.setId("123456789_0");
        repo.setUri("http://localhost:8080/server/rdf/resource/123456789/0");
        //repo.addHasCommunityIdItem("123456789_1");
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
        String prettyRepo = DSpaceObjectMapperHelper.map(repo);

        DspaceRepositoryParser parser = new DspaceRepositoryParser();
        System.out.println(prettyRepo);
        Model repoModel = parser.parse(repo);


        // TODO Module de remplacement de méthode auto-généré

    }
    /*
     * Example of expected result
     * 
<http://localhost:8080/vivo/individual/n4148>
        a                       obo:BFO_0000031 , obo:IAO_0000030 , bibo:Document , obo:BFO_0000001 , obo:BFO_0000002 , p1:Repository , owl:Thing ;
        rdfs:label              "123456789_0"@fr-CA ;
        obo:ARG_2000028         <http://localhost:8080/vivo/individual/n1143> ;
        vitro:mostSpecificType  p1:Repository .

<http://localhost:8080/vivo/individual/n3986>
        a                       vcard:URL , vcard:Addressing , owl:Thing , vcard:Communication , vcard:Explanatory , vcard:Identification ;
        rdfs:label              "123456789_0"@fr-CA ;
        vitro:mostSpecificType  vcard:URL ;
        vivo:rank               1 ;
        vcard:url               "http://dspace.org/1234567/0"^^xsd:anyURI .

<http://localhost:8080/vivo/individual/n1143>
        a                       vcard:Kind , obo:BFO_0000031 , obo:BFO_0000002 , owl:Thing , obo:BFO_0000001 , obo:ARG_2000379 , obo:IAO_0000030 ;
        obo:ARG_2000029         <http://localhost:8080/vivo/individual/n4148> ;
        vitro:mostSpecificType  vcard:Kind ;
        vcard:hasURL            <http://localhost:8080/vivo/individual/n3986> , <http://localhost:8080/vivo/individual/n7146> .

<http://localhost:8080/vivo/individual/n7146>
        a                       vcard:Addressing , vcard:URL , owl:Thing , vcard:Identification , vcard:Communication , vcard:Explanatory ;
        rdfs:label              "1234567/0-b"@fr-CA ;
        vitro:mostSpecificType  vcard:URL ;
        vivo:rank               2 ;
        vcard:url               "http://dspace.org/1234567/0-b"^^xsd:anyURI .
     */

    private Model parse(Repository repo) {
        Model model = ModelFactory.createDefaultModel();
        PrefixMapping prefixMapping = ParserHelper.getVIVOPrefixes();
        model.setNsPrefixes(prefixMapping);
        Resource orgdURI = ResourceFactory.createResource(DSPACE.getURI() +repo.getId());
        Resource orgdURI_vcard = ResourceFactory.createResource(DSPACE.getURI() +repo.getId()+"-vcard");
        Resource orgdURI_vcard_url = ResourceFactory.createResource(DSPACE.getURI() +repo.getId()+"-vcard-url");
        /*
         * Création de l'individu
         */
        model.add(orgdURI, RDF.type, OBO.BFO_0000031);
        model.add(orgdURI, RDF.type, OBO.IAO_0000030);
        model.add(orgdURI, RDF.type, OBO.BFO_0000001);
        model.add(orgdURI, RDF.type, OBO.BFO_0000002);
        model.add(orgdURI, RDF.type, OWL.Thing);
        model.add(orgdURI, RDF.type, BIBO.Document);
        model.add(orgdURI, RDF.type, DSPACE.Repository);
        model.add(orgdURI, VITRO.mostSpecificType, DSPACE.Repository);
        model.add(orgdURI, OBO.ARG_2000028, orgdURI_vcard );
        List<Statement> stmts = repo.getListOfStatements();
        /*
         * Premier individu
         */
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
        @Valid List<StatementLiteral> stmtls = repo.getListOfStatementLiterals();
        for (Iterator iterator = stmtls.iterator(); iterator.hasNext();) {
            StatementLiteral statement = (StatementLiteral) iterator.next();
            @NotNull
            String subj = prefixMapping.expandPrefix(statement.getSubjectUri());
            @NotNull
            String pred = prefixMapping.expandPrefix(statement.getPredicateUri());
            @NotNull
            String obj = statement.getObjectLiteral();
            String lType = statement.getLiteralType();
            TypedValue tv = new BaseDatatype.TypedValue(obj,lType);
            Literal aLiteral = ResourceFactory.createTypedLiteral(obj, XSDDatatype.XSDstring);
            Resource r = ResourceFactory.createResource();
            model.add(model.createResource(subj),model.createProperty(pred), aLiteral);
        }

        /*
         * Construct VCARD king Statement 
         */
        model.add(orgdURI_vcard, RDF.type, VCARD.Kind);
        model.add(orgdURI_vcard, RDF.type, OBO.BFO_0000031);
        model.add(orgdURI_vcard, RDF.type, OBO.BFO_0000002);
        model.add(orgdURI_vcard, RDF.type, OWL.Thing);
        model.add(orgdURI_vcard, RDF.type, OBO.BFO_0000001);
        model.add(orgdURI_vcard, RDF.type, OBO.ARG_2000379);
        model.add(orgdURI_vcard, RDF.type, OBO.IAO_0000030);
        model.add(orgdURI_vcard, OBO.ARG_2000029 , orgdURI);
        model.add(orgdURI_vcard, VCARD.hasURL, orgdURI_vcard_url);

        /*
         * Construct VCARD-URL Statement 
	<http://localhost:8080/vivo/individual/n7146>
        a                       vcard:Addressing , vcard:URL , owl:Thing , vcard:Identification , vcard:Communication , vcard:Explanatory ;
        rdfs:label              "1234567/0-b"@fr-CA ;
        vitro:mostSpecificType  vcard:URL ;
        vivo:rank               2 ;
        vcard:url               "http://dspace.org/1234567/0-b"^^xsd:anyURI .
         */	
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Addressing);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.URL);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Identification);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Communication);
        model.add(orgdURI_vcard_url, RDF.type, VCARD.Explanatory);
        model.add(orgdURI_vcard_url, RDF.type, OWL.Thing);

        model.add(orgdURI_vcard_url, VITRO.mostSpecificType, VCARD.URL);
        model.add(orgdURI_vcard_url, VIVO.rank, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDnonNegativeInteger));
        model.add(orgdURI_vcard_url, VCARD.url, ResourceFactory.createResource(repo.getUri()));

        ParserHelper.dumpStdoutModel(model);


        // TODO Module de remplacement de méthode auto-généré
        return null;
    }

}
