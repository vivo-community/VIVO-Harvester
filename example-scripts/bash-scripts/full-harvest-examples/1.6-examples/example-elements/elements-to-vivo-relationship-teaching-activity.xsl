<?xml version="1.0" encoding="UTF-8"?>
<!--
 | Copyright (c) 2012 Symplectic Limited. All rights reserved.
 | This Source Code Form is subject to the terms of the Mozilla Public
 | License, v. 2.0. If a copy of the MPL was not distributed with this
 | file, You can obtain one at http://mozilla.org/MPL/2.0/.
 -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:bibo="http://purl.org/ontology/bibo/"
                xmlns:vivo="http://vivoweb.org/ontology/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:symp="http://www.symplectic.co.uk/vivo/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                xmlns:obo="http://purl.obolibrary.org/obo/"
                exclude-result-prefixes="rdf rdfs bibo vivo foaf score ufVivo vitro api symp svfn config xs"
        >

    <!--
        Template for handling relationships between users and professional activities.
    -->

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-activity.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <xsl:template match="api:relationship[@type='user-teaching-association']">



        <xsl:variable name="activity" select="api:related/api:object[@category='teaching-activity']" />

        <xsl:variable name="fullActivityObj" select="svfn:fullObject($activity)" />

        <xsl:variable name="user" select="api:related/api:object[@category='user']" />

        <xsl:variable name="startDate" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='start-date']/api:date/api:year"/>

        <xsl:variable name="endDate" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='end-date']/api:date/api:year"/>

        <xsl:if test="$fullActivityObj/symp:entry/api:object[@type='course-taught']">

            <xsl:variable name="associationURI" select="svfn:relationshipURI(.,'teacher-role-')" />

            <xsl:variable name="courseName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='title']/api:text"/>

            <xsl:variable name="roleName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-formal-teaching-type']/api:text"/>


            <xsl:variable name="courseURI">
            <xsl:if test="$courseName">
                <xsl:copy-of select="concat($baseURI, 'course-', svfn:stringToURI($courseName))" />
            </xsl:if>
            </xsl:variable>

            <xsl:variable name="startDateURI" select="concat($associationURI,'-startDate')"/>

            <xsl:variable name="endDateURI" select="concat($associationURI,'-endDate')"/>

            <!-- Only output this relationship is the courseName is actually present; This is a data integrity check for the imported 'course-taught' activities -->

            <xsl:if test="$courseName">

            <xsl:variable name="dateIntervalURI">
                <xsl:choose>
                    <xsl:when test="$endDate">
                        <xsl:value-of select="concat($associationURI,'-dateInterval-',$startDate,'-',$endDate)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($associationURI,'-dateInterval-',$startDate)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <!-- Create the Course (Activity) Object -->
            <xsl:if test="$courseName">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$courseURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000015"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Course"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000003"/>
                    <rdfs:label><xsl:value-of select="$courseName"/></rdfs:label>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <!-- Create the association (Teacher Role) object and associate it with the (Course) object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$associationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000023"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000020"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000017"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002"/>
                    <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}"/>
                    <rdfs:label><xsl:value-of select="$roleName"/></rdfs:label>
                    <xsl:if test="$courseName">
                    <obo:BFO_0000054 rdf:resource="{$courseURI}"/>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>

            <!--Add a reference to the association object to the user object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="svfn:userURI($user)" />
                <xsl:with-param name="rdfNodes">
                    <obo:RO_0000053 rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Output the Start Date Object -->
            <xsl:if test="$startDate">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$startDateURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate,'-01-01T00:00:00')"/></vivo:dateTime>
                        <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <!-- Output the End Date Object -->
            <xsl:if test="$endDate">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$endDateURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate,'-01-01T00:00:00')"/></vivo:dateTime>
                        <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <!-- Output the Date Interval Object -->
            <xsl:if test="$startDate or $endDate">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
                        <xsl:if test="$startDate">
                            <vivo:start rdf:resource="{$startDateURI}"/>
                        </xsl:if>
                        <!-- Add a condition to "close" the interval with the start date is end date doesn't existing to avoid the appearance of it looking current -->
                        <xsl:choose>
                        <xsl:when test="$endDate">
                            <vivo:end rdf:resource="{$endDateURI}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <vivo:end rdf:resource="{$startDateURI}"/>
                        </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

        </xsl:if>
        </xsl:if>

    </xsl:template>

</xsl:stylesheet>