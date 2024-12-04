package org.vivoweb.harvester.extractdspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Repository {
    @JsonProperty("dspaceType")
    private final String dspaceType = "repository";

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("uri")
    private String uri = null;

    @JsonProperty("hasCommunity")
    private List<Community> hasCommunity = null;

    @JsonProperty("listOfStatements")
    private List<Statement> listOfStatements = null;

    @JsonProperty("listOfStatementLiterals")
    private List<StatementLiteral> listOfStatementLiterals = null;

    @JsonProperty("dspaceType")
    public String getDspaceType() {
        return dspaceType;
    }

    public Repository id(String id) {
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

    public Repository uri(String uri) {
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

    public Repository hasCommunity(List<Community> hasCommunity) {
        this.hasCommunity = hasCommunity;
        return this;
    }

    public Repository addHasCommunityItem(Community hasCommunityItem) {
        if (this.hasCommunity == null) {
            this.hasCommunity = new ArrayList<>();
        }
        this.hasCommunity.add(hasCommunityItem);
        return this;
    }

    @JsonProperty("hasCommunity")
    @Valid
    public List<Community> getHasCommunity() {
        return hasCommunity;
    }

    public void setHasCommunity(List<Community> hasCommunity) {
        this.hasCommunity = hasCommunity;
    }

    public Repository listOfStatements(List<Statement> listOfStatements) {
        this.listOfStatements = listOfStatements;
        return this;
    }

    public Repository addListOfStatementsItem(Statement listOfStatementsItem) {
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

    public Repository listOfStatementLiterals(List<StatementLiteral> listOfStatementLiterals) {
        this.listOfStatementLiterals = listOfStatementLiterals;
        return this;
    }

    public Repository addListOfStatementLiteralsItem(StatementLiteral listOfStatementLiteralsItem) {
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
        Repository repository = (Repository) o;
        return Objects.equals(this.id, repository.id) && Objects.equals(this.uri, repository.uri) &&
            Objects.equals(this.hasCommunity, repository.hasCommunity) &&
            Objects.equals(this.listOfStatements, repository.listOfStatements) &&
            Objects.equals(this.listOfStatementLiterals, repository.listOfStatementLiterals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dspaceType, id, uri, hasCommunity, listOfStatements,
            listOfStatementLiterals);
    }

    @Override
    public String toString() {

        return "class Repository {\n" +
            "    dspaceType: " + toIndentedString(dspaceType) + "\n" +
            "    id: " + toIndentedString(id) + "\n" +
            "    uri: " + toIndentedString(uri) + "\n" +
            "    hasCommunity: " + toIndentedString(hasCommunity) + "\n" +
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
