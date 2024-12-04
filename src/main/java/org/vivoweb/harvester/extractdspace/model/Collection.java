package org.vivoweb.harvester.extractdspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Collection {
    @JsonProperty("dspaceType")
    private final String dspaceType = "collection";

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("uri")
    private String uri = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("hasItem")
    private List<Item> hasItem = null;

    @JsonProperty("isPartOfCommunityID")
    private List<String> isPartOfCommunityID = null;

    @JsonProperty("listOfStatements")
    private List<Statement> listOfStatements = null;

    @JsonProperty("listOfStatementLiterals")
    private List<StatementLiteral> listOfStatementLiterals = null;


    @JsonProperty("dspaceType")
    public String getDspaceType() {
        return dspaceType;
    }


    public Collection id(String id) {
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

    public Collection uri(String uri) {
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

    public Collection url(String url) {
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

    public Collection hasItem(List<Item> hasItem) {
        this.hasItem = hasItem;
        return this;
    }

    public Collection addHasItemItem(Item hasItemItem) {
        if (this.hasItem == null) {
            this.hasItem = new ArrayList<>();
        }
        this.hasItem.add(hasItemItem);
        return this;
    }

    @JsonProperty("hasItem")
    @Valid
    public List<Item> getHasItem() {
        return hasItem;
    }

    public void setHasItem(List<Item> hasItem) {
        this.hasItem = hasItem;
    }

    public Collection isPartOfCommunityID(List<String> isPartOfCommunityID) {
        this.isPartOfCommunityID = isPartOfCommunityID;
        return this;
    }

    public Collection addIsPartOfCommunityIDItem(String isPartOfCommunityIDItem) {
        if (this.isPartOfCommunityID == null) {
            this.isPartOfCommunityID = new ArrayList<>();
        }
        this.isPartOfCommunityID.add(isPartOfCommunityIDItem);
        return this;
    }

    @JsonProperty("isPartOfCommunityID")
    public List<String> getIsPartOfCommunityID() {
        return isPartOfCommunityID;
    }

    public void setIsPartOfCommunityID(List<String> isPartOfCommunityID) {
        this.isPartOfCommunityID = isPartOfCommunityID;
    }

    public Collection listOfStatements(List<Statement> listOfStatements) {
        this.listOfStatements = listOfStatements;
        return this;
    }

    public Collection addListOfStatementsItem(Statement listOfStatementsItem) {
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

    public Collection listOfStatementLiterals(List<StatementLiteral> listOfStatementLiterals) {
        this.listOfStatementLiterals = listOfStatementLiterals;
        return this;
    }

    public Collection addListOfStatementLiteralsItem(StatementLiteral listOfStatementLiteralsItem) {
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
        Collection collection = (Collection) o;
        return Objects.equals(this.id, collection.id) && Objects.equals(this.uri, collection.uri) &&
            Objects.equals(this.url, collection.url) &&
            Objects.equals(this.hasItem, collection.hasItem) &&
            Objects.equals(this.isPartOfCommunityID, collection.isPartOfCommunityID) &&
            Objects.equals(this.listOfStatements, collection.listOfStatements) &&
            Objects.equals(this.listOfStatementLiterals, collection.listOfStatementLiterals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dspaceType, id, uri, url, hasItem, isPartOfCommunityID,
            listOfStatements, listOfStatementLiterals);
    }

    @Override
    public String toString() {

        return "class Collection {\n" +
            "    dspaceType: " + toIndentedString(dspaceType) + "\n" +
            "    id: " + toIndentedString(id) + "\n" +
            "    uri: " + toIndentedString(uri) + "\n" +
            "    url: " + toIndentedString(url) + "\n" +
            "    hasItem: " + toIndentedString(hasItem) + "\n" +
            "    isPartOfCommunityID: " + toIndentedString(isPartOfCommunityID) +
            "\n" +
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
