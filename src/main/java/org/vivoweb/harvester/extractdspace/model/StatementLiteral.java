package org.vivoweb.harvester.extractdspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class StatementLiteral {
    @JsonProperty("dspaceType")
    private final String dspaceType = "statementLiteral";

    @JsonProperty("subjectUri")
    private String subjectUri = null;

    @JsonProperty("predicateUri")
    private String predicateUri = null;

    @JsonProperty("objectLiteral")
    private String objectLiteral = null;

    @JsonProperty("literalType")
    private String literalType = null;

    @JsonProperty("dspaceType")
    public String getDspaceType() {
        return dspaceType;
    }

    public StatementLiteral subjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
        return this;
    }

    @JsonProperty("subjectUri")
    @NotNull
    public String getSubjectUri() {
        return subjectUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    public StatementLiteral predicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
        return this;
    }

    @JsonProperty("predicateUri")
    @NotNull
    public String getPredicateUri() {
        return predicateUri;
    }

    public void setPredicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
    }

    public StatementLiteral objectLiteral(String objectLiteral) {
        this.objectLiteral = objectLiteral;
        return this;
    }

    @JsonProperty("objectLiteral")
    @NotNull
    public String getObjectLiteral() {
        return objectLiteral;
    }

    public void setObjectLiteral(String objectLiteral) {
        this.objectLiteral = objectLiteral;
    }

    public StatementLiteral literalType(String literalType) {
        this.literalType = literalType;
        return this;
    }

    @JsonProperty("literalType")
    @NotNull
    public String getLiteralType() {
        return literalType;
    }

    public void setLiteralType(String literalType) {
        this.literalType = literalType;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatementLiteral statementLiteral = (StatementLiteral) o;
        return Objects.equals(this.subjectUri, statementLiteral.subjectUri) &&
            Objects.equals(this.predicateUri, statementLiteral.predicateUri) &&
            Objects.equals(this.objectLiteral, statementLiteral.objectLiteral) &&
            Objects.equals(this.literalType, statementLiteral.literalType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dspaceType, subjectUri, predicateUri, objectLiteral, literalType);
    }

    @Override
    public String toString() {

        return "class StatementLiteral {\n" +
            "    dspaceType: " + toIndentedString(dspaceType) + "\n" +
            "    subjectUri: " + toIndentedString(subjectUri) + "\n" +
            "    predicateUri: " + toIndentedString(predicateUri) + "\n" +
            "    objectLiteral: " + toIndentedString(objectLiteral) + "\n" +
            "    literalType: " + toIndentedString(literalType) + "\n" +
            "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
