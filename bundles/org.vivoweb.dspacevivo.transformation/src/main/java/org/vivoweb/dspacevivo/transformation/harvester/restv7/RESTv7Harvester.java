package org.vivoweb.dspacevivo.transformation.harvester.restv7;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.commons.compress.utils.Lists;
import org.vivoweb.dspacevivo.model.Collection;
import org.vivoweb.dspacevivo.model.Community;
import org.vivoweb.dspacevivo.model.Item;
import org.vivoweb.dspacevivo.model.Repository;
import org.vivoweb.dspacevivo.transformation.harvester.DspaceHarvester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.dspacevivo.model.Statement;
import org.vivoweb.dspacevivo.model.StatementLiteral;

public class RESTv7Harvester extends DspaceHarvester {

    private static Logger log = LoggerFactory.getLogger(RESTv7Harvester.class);
    private String csrfToken = null;
    private String authToken = null;

    public RESTv7Harvester(Properties prprts) {
        super(prprts);
    }

    private HttpResponse<JsonNode> updateCSRF(HttpResponse<JsonNode> i) {
        List<String> get = i.getHeaders().get("DSPACE-XSRF-TOKEN");
        if (get.size() > 0) {
            this.csrfToken = get.get(0);
        }
        return i;
    }

    @Override
    public void connect() {
        updateCSRF(Unirest.get(this.conf.getProperty("endpoint") + "/authn/status").asJson());
        this.authToken = updateCSRF(Unirest.post(this.conf.getProperty("endpoint") + "/authn/login")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-XSRF-TOKEN", this.csrfToken)
                .field("user", this.conf.getProperty("username"))
                .field("password", this.conf.getProperty("password"))
                .asJson()).getHeaders().get("Authorization").get(0);
    }

    public JsonNode getItemsPage(int page, int size) {
        return updateCSRF(Unirest.get(this.conf.getProperty("endpoint") + "/discover/browses/title/items?sort=dc.title,ASC&size=" + size + "&page=" + page)
                .header("X-XSRF-TOKEN", this.csrfToken)
                .header("Authorization", this.authToken)
                .asJson()).getBody();
    }

    @Override
    public Iterator<Item> harvestItems() {
        return new ItemItr((this));
    }

    private JsonNode calllinks(String link) {
        return updateCSRF(Unirest.get(link)
                .header("X-XSRF-TOKEN", this.csrfToken)
                .header("Authorization", this.authToken)
                .asJson()).getBody();

    }

    public Item getItem(JSONObject jsonObject) {
        Item resp = new Item();
        resp.setId(jsonObject.getString("id"));
        resp.setUri(jsonObject.getString("handle"));
        resp.setUri(this.conf.getProperty("uriPrefix") + jsonObject.getString("handle"));
        resp.setUrl(jsonObject.getJSONObject("_links").getJSONObject("self").getString("href"));

        JsonNode collectionLink = calllinks(jsonObject.getJSONObject("_links").getJSONObject("owningCollection").getString("href"));

        resp.dspaceIsPartOfCollectionID(Lists.newArrayList());
        resp.getDspaceIsPartOfCollectionID().add(collectionLink.getObject().getString("handle"));

        resp.setListOfStatements(Lists.newArrayList());
        resp.setListOfStatementLiterals(Lists.newArrayList());
        List<Object> metadataMapping = metadataMapping(resp.getUri(), jsonObject.getJSONObject("metadata"));
        for (Object obj : metadataMapping) {
            if (obj instanceof StatementLiteral) {
                resp.getListOfStatementLiterals().add((StatementLiteral) obj);
            } else {
                resp.getListOfStatements().add((Statement) obj);
            }
        }

        return resp;

    }

    @Override
    public Iterator<Community> harvestCommunity() {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Collection> harvestCollection() {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Repository> harvestRepository() {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //DC 2 RDF Mapping
    public List<Object> metadataMapping(String uri, JSONObject metadata) {
        List<Object> responseList = Lists.newArrayList();
        for (String key : metadata.keySet()) {
            JSONArray jsonArray = metadata.getJSONArray(key);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //Skip nulls
                if (jsonObject.get("value") == null) {
                    continue;
                }
                String property = null;
                String datatype = "xsd:string";
                switch (key) {
                    case "dc.title":
                        property = "http://purl.org/dc/terms/title";
                        break;
                    case "dc.identifier.uri":
                        property = "http://purl.org/dc/terms/uri";
                        break;
                    case "dc.description":
                        property = "http://purl.org/dc/terms/description";
                        break;
                    case "dc.description.abstract":
                        property = "http://purl.org/dc/terms/abstract";
                        break;
                    case "dc.description.tableofcontents":
                        property = "http://purl.org/dc/terms/tableofcontents";
                        break;
                    case "dc.contributor.author":
                        property = "http://purl.org/dc/terms/contributor";
                        break;
                    case "dc.contributor.other":
                        property = "http://purl.org/dc/terms/contributor";
                        break;
                    case "dc.contributor":
                        property = "http://purl.org/dc/terms/contributor";
                        break;
                    case "dc.creator":
                        property = "http://purl.org/dc/terms/creator";
                        break;
                    case "dc.source":
                        property = "http://purl.org/dc/terms/source";
                        break;
                    case "dc.subject":
                        property = "http://purl.org/dc/terms/subject";
                        break;
                    case "dc.language":
                        property = "http://purl.org/dc/terms/language";
                        break;
                    case "dc.publisher":
                        property = "http://purl.org/dc/terms/publisher";
                        break;
                    case "dc.identifier":
                        property = "http://purl.org/dc/terms/identifier";
                        break;
                    case "dc.date":
                        property = "http://purl.org/dc/terms/date";
                        datatype = "xsd:dateTime";
                        break;
                    case "dc.format":
                        property = "http://purl.org/dc/terms/format";
                        break;
                    case "dc.rights":
                        property = "http://purl.org/dc/terms/rights";
                        break;
                    case "dc.relation":
                        property = "http://purl.org/dc/terms/relation";
                        break;
                    case "dc.date.accessioned":
                        property = "http://purl.org/dc/terms/accessioned";
                        datatype = "xsd:dateTime";
                        break;
                    case "dc.date.available":
                        property = "http://purl.org/dc/terms/available";
                        datatype = "xsd:dateTime";
                        break;
                    case "dc.date.issued":
                        property = "http://purl.org/dc/terms/issued";
                        datatype = "xsd:dateTime";
                        break;
                    case "dc.language.iso":
                        property = "http://purl.org/dc/terms/language";
                        break;
                    case "dc.type":
                        property = "http://purl.org/dc/terms/type";
                        break;

                }
                if (property == null) {
                    log.warn("Property without mapping:'" + key + "'");
                    continue;
                }
                StatementLiteral statementLiteral = new StatementLiteral();
                statementLiteral.setSubjectUri(uri);
                statementLiteral.setPredicateUri(property);
                statementLiteral.setObjectLiteral(jsonObject.getString("value"));
                statementLiteral.setLiteralType(datatype);
                responseList.add(statementLiteral);

            }
        }

        return responseList;
    }
}
