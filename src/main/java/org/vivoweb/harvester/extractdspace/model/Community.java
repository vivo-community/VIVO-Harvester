package org.vivoweb.harvester.extractdspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Community {
    @JsonProperty("dspaceType")
    private final String dspaceType = "community";

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("uri")
    private String uri = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("hasCollection")
    private List<Collection> hasCollection = null;

    @JsonProperty("hasSubCommunity")
    private List<Community> hasSubCommunity = null;

    @JsonProperty("isSubcommunityOfID")
    private List<String> isSubcommunityOfID = null;

    @JsonProperty("isPartOfRepositoryID")
    private List<String> isPartOfRepositoryID = null;

    @JsonProperty("listOfStatements")
    private List<Statement> listOfStatements = null;

    @JsonProperty("listOfStatementLiterals")
    private List<StatementLiteral> listOfStatementLiterals = null;

    @JsonProperty("dspaceType")
    public String getDspaceType() {
        return dspaceType;
    }


    public Community id(String id) {
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

    public Community uri(String uri) {
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

    public Community url(String url) {
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

    public Community hasCollection(List<Collection> hasCollection) {
        this.hasCollection = hasCollection;
        return this;
    }

    public Community addHasCollectionItem(Collection hasCollectionItem) {
        if (this.hasCollection == null) {
            this.hasCollection = new ArrayList<>();
        }
        this.hasCollection.add(hasCollectionItem);
        return this;
    }

    @JsonProperty("hasCollection")
    @Valid
    public List<Collection> getHasCollection() {
        return hasCollection;
    }

    public void setHasCollection(List<Collection> hasCollection) {
        this.hasCollection = hasCollection;
    }

    public Community hasSubCommunity(List<Community> hasSubCommunity) {
        this.hasSubCommunity = hasSubCommunity;
        return this;
    }

    public Community addHasSubCommunityItem(Community hasSubCommunityItem) {
        if (this.hasSubCommunity == null) {
            this.hasSubCommunity = new ArrayList<>();
        }
        this.hasSubCommunity.add(hasSubCommunityItem);
        return this;
    }

    @JsonProperty("hasSubCommunity")
    @Valid
    public List<Community> getHasSubCommunity() {
        return hasSubCommunity;
    }

    public void setHasSubCommunity(List<Community> hasSubCommunity) {
        this.hasSubCommunity = hasSubCommunity;
    }

    public Community isSubcommunityOfID(List<String> isSubcommunityOfID) {
        this.isSubcommunityOfID = isSubcommunityOfID;
        return this;
    }

    public Community addIsSubcommunityOfIDItem(String isSubcommunityOfIDItem) {
        if (this.isSubcommunityOfID == null) {
            this.isSubcommunityOfID = new ArrayList<>();
        }
        this.isSubcommunityOfID.add(isSubcommunityOfIDItem);
        return this;
    }

    @JsonProperty("isSubcommunityOfID")
    public List<String> getIsSubcommunityOfID() {
        return isSubcommunityOfID;
    }

    public void setIsSubcommunityOfID(List<String> isSubcommunityOfID) {
        this.isSubcommunityOfID = isSubcommunityOfID;
    }

    public Community isPartOfRepositoryID(List<String> isPartOfRepositoryID) {
        this.isPartOfRepositoryID = isPartOfRepositoryID;
        return this;
    }

    public Community addIsPartOfRepositoryIDItem(String isPartOfRepositoryIDItem) {
        if (this.isPartOfRepositoryID == null) {
            this.isPartOfRepositoryID = new ArrayList<>();
        }
        this.isPartOfRepositoryID.add(isPartOfRepositoryIDItem);
        return this;
    }

    @JsonProperty("isPartOfRepositoryID")
    public List<String> getIsPartOfRepositoryID() {
        return isPartOfRepositoryID;
    }

    public void setIsPartOfRepositoryID(List<String> isPartOfRepositoryID) {
        this.isPartOfRepositoryID = isPartOfRepositoryID;
    }

    public Community listOfStatements(List<Statement> listOfStatements) {
        this.listOfStatements = listOfStatements;
        return this;
    }

    public Community addListOfStatementsItem(Statement listOfStatementsItem) {
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

    public Community listOfStatementLiterals(List<StatementLiteral> listOfStatementLiterals) {
        this.listOfStatementLiterals = listOfStatementLiterals;
        return this;
    }

    public Community addListOfStatementLiteralsItem(StatementLiteral listOfStatementLiteralsItem) {
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
        Community community = (Community) o;
        return Objects.equals(this.id, community.id) && Objects.equals(this.uri, community.uri) &&
            Objects.equals(this.url, community.url) &&
            Objects.equals(this.hasCollection, community.hasCollection) &&
            Objects.equals(this.hasSubCommunity, community.hasSubCommunity) &&
            Objects.equals(this.isSubcommunityOfID, community.isSubcommunityOfID) &&
            Objects.equals(this.isPartOfRepositoryID, community.isPartOfRepositoryID) &&
            Objects.equals(this.listOfStatements, community.listOfStatements) &&
            Objects.equals(this.listOfStatementLiterals, community.listOfStatementLiterals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dspaceType, id, uri, url, hasCollection, hasSubCommunity,
            isSubcommunityOfID, isPartOfRepositoryID, listOfStatements, listOfStatementLiterals);
    }


    @Override
    public String toString() {

        return "class Community {\n" +
            "    dspaceType: " + toIndentedString(dspaceType) + "\n" +
            "    id: " + toIndentedString(id) + "\n" +
            "    uri: " + toIndentedString(uri) + "\n" +
            "    url: " + toIndentedString(url) + "\n" +
            "    hasCollection: " + toIndentedString(hasCollection) + "\n" +
            "    hasSubCommunity: " + toIndentedString(hasSubCommunity) + "\n" +
            "    isSubcommunityOfID: " + toIndentedString(isSubcommunityOfID) +
            "\n" +
            "    isPartOfRepositoryID: " + toIndentedString(isPartOfRepositoryID) +
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
