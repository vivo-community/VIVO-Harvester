package org.vivoweb.harvester.extractdspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class Statement {
    @JsonProperty("dspaceType")
    private final String dspaceType = "statement";

    @JsonProperty("subjectUri")
    private String subjectUri = null;

    @JsonProperty("predicateUri")
    private String predicateUri = null;

    @JsonProperty("objectUri")
    private String objectUri = null;

    @JsonProperty("dspaceType")
    public String getDspaceType() {
        return dspaceType;
    }

    public Statement subjectUri(String subjectUri) {
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

    public Statement predicateUri(String predicateUri) {
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

    public Statement objectUri(String objectUri) {
        this.objectUri = objectUri;
        return this;
    }

    @JsonProperty("objectUri")
    @NotNull
    public String getObjectUri() {
        return objectUri;
    }

    public void setObjectUri(String objectUri) {
        this.objectUri = objectUri;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Statement statement = (Statement) o;
        return Objects.equals(this.subjectUri, statement.subjectUri) &&
            Objects.equals(this.predicateUri, statement.predicateUri) &&
            Objects.equals(this.objectUri, statement.objectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dspaceType, subjectUri, predicateUri, objectUri);
    }

    @Override
    public String toString() {

        return "class Statement {\n" +
            "    dspaceType: " + toIndentedString(dspaceType) + "\n" +
            "    subjectUri: " + toIndentedString(subjectUri) + "\n" +
            "    predicateUri: " + toIndentedString(predicateUri) + "\n" +
            "    objectUri: " + toIndentedString(objectUri) + "\n" +
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
