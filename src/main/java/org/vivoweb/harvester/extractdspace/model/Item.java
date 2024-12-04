package org.vivoweb.harvester.extractdspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Item {
    @JsonProperty("dspaceType")
    private final String dspaceType = "item";

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("uri")
    private String uri = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("dspaceBitstreamURLs")
    private List<String> dspaceBitstreamURLs = null;

    @JsonProperty("dspaceIsPartOfCollectionID")
    private List<String> dspaceIsPartOfCollectionID = null;

    @JsonProperty("listOfStatements")
    private List<Statement> listOfStatements = null;

    @JsonProperty("listOfStatementLiterals")
    private List<StatementLiteral> listOfStatementLiterals = null;

    @JsonProperty("dspaceType")
    public String getDspaceType() {
        return dspaceType;
    }


    public Item id(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("id")
    @NotNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Item uri(String uri) {
        this.uri = uri;
        return this;
    }

    @JsonProperty("uri")
    @NotNull
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Item url(String url) {
        this.url = url;
        return this;
    }

    @JsonProperty("url")
    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Item dspaceBitstreamURLs(List<String> dspaceBitstreamURLs) {
        this.dspaceBitstreamURLs = dspaceBitstreamURLs;
        return this;
    }

    public Item addDspaceBitstreamURLsItem(String dspaceBitstreamURLsItem) {
        if (this.dspaceBitstreamURLs == null) {
            this.dspaceBitstreamURLs = new ArrayList<>();
        }
        this.dspaceBitstreamURLs.add(dspaceBitstreamURLsItem);
        return this;
    }

    @JsonProperty("dspaceBitstreamURLs")
    public List<String> getDspaceBitstreamURLs() {
        return dspaceBitstreamURLs;
    }

    public void setDspaceBitstreamURLs(List<String> dspaceBitstreamURLs) {
        this.dspaceBitstreamURLs = dspaceBitstreamURLs;
    }

    public Item dspaceIsPartOfCollectionID(List<String> dspaceIsPartOfCollectionID) {
        this.dspaceIsPartOfCollectionID = dspaceIsPartOfCollectionID;
        return this;
    }

    public Item addDspaceIsPartOfCollectionIDItem(String dspaceIsPartOfCollectionIDItem) {
        if (this.dspaceIsPartOfCollectionID == null) {
            this.dspaceIsPartOfCollectionID = new ArrayList<>();
        }
        this.dspaceIsPartOfCollectionID.add(dspaceIsPartOfCollectionIDItem);
        return this;
    }

    @JsonProperty("dspaceIsPartOfCollectionID")
    public List<String> getDspaceIsPartOfCollectionID() {
        return dspaceIsPartOfCollectionID;
    }

    public void setDspaceIsPartOfCollectionID(List<String> dspaceIsPartOfCollectionID) {
        this.dspaceIsPartOfCollectionID = dspaceIsPartOfCollectionID;
    }

    public Item listOfStatements(List<Statement> listOfStatements) {
        this.listOfStatements = listOfStatements;
        return this;
    }

    public Item addListOfStatementsItem(Statement listOfStatementsItem) {
        if (this.listOfStatements == null) {
            this.listOfStatements = new ArrayList<>();
        }
        this.listOfStatements.add(listOfStatementsItem);
        return this;
    }

    @JsonProperty("listOfStatements")
    @Valid
    public List<Statement> getListOfStatements() {
        return listOfStatements;
    }

    public void setListOfStatements(List<Statement> listOfStatements) {
        this.listOfStatements = listOfStatements;
    }

    public Item listOfStatementLiterals(List<StatementLiteral> listOfStatementLiterals) {
        this.listOfStatementLiterals = listOfStatementLiterals;
        return this;
    }

    public Item addListOfStatementLiteralsItem(StatementLiteral listOfStatementLiteralsItem) {
        if (this.listOfStatementLiterals == null) {
            this.listOfStatementLiterals = new ArrayList<>();
        }
        this.listOfStatementLiterals.add(listOfStatementLiteralsItem);
        return this;
    }

    @JsonProperty("listOfStatementLiterals")
    @Valid
    public List<StatementLiteral> getListOfStatementLiterals() {
        return listOfStatementLiterals;
    }

    public void setListOfStatementLiterals(List<StatementLiteral> listOfStatementLiterals) {
        this.listOfStatementLiterals = listOfStatementLiterals;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return Objects.equals(this.id, item.id) && Objects.equals(this.uri, item.uri) &&
            Objects.equals(this.url, item.url) &&
            Objects.equals(this.dspaceBitstreamURLs, item.dspaceBitstreamURLs) &&
            Objects.equals(this.dspaceIsPartOfCollectionID, item.dspaceIsPartOfCollectionID) &&
            Objects.equals(this.listOfStatements, item.listOfStatements) &&
            Objects.equals(this.listOfStatementLiterals, item.listOfStatementLiterals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dspaceType, id, uri, url, dspaceBitstreamURLs,
            dspaceIsPartOfCollectionID, listOfStatements, listOfStatementLiterals);
    }

    @Override
    public String toString() {

        return "class Item {\n" +
            "    dspaceType: " + toIndentedString(dspaceType) + "\n" +
            "    id: " + toIndentedString(id) + "\n" +
            "    uri: " + toIndentedString(uri) + "\n" +
            "    url: " + toIndentedString(url) + "\n" +
            "    dspaceBitstreamURLs: " + toIndentedString(dspaceBitstreamURLs) +
            "\n" +
            "    dspaceIsPartOfCollectionID: " +
            toIndentedString(dspaceIsPartOfCollectionID) + "\n" +
            "    listOfStatements: " + toIndentedString(listOfStatements) + "\n" +
            "    listOfStatementLiterals: " + toIndentedString(listOfStatementLiterals) +
            "\n" +
            "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
