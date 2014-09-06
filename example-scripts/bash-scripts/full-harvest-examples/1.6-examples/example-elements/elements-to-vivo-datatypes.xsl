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
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                exclude-result-prefixes="rdf rdfs bibo vivo foaf score ufVivo vitro api symp svfn xs"
        >

    <!--
        Main XSLT file for applying Elements field type renderings
        ==========================================================
    -->

    <!--
        Convert an Elements date to a vivo:dateTime statement, with dateTimePrecision

        Note, unlike the other templates that have a "renderForProprty" mode (as they are outputting a named property element,
        this template is rendering two specific VIVO ontology properties (for use on a particular class).
    -->
    <xsl:template match="api:date">
        <!-- Determine the date precision -->
        <xsl:variable name="datePrecision" select="svfn:datePrecision(.)" />

        <xsl:if test="$datePrecision!='none'">
            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#{$datePrecision}" />
            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="svfn:dateYear(.)" />-<xsl:value-of select="svfn:dateMonth(.)" />-<xsl:value-of select="svfn:dateDay(.)" />T00:00:00Z</vivo:dateTime>
        </xsl:if>
    </xsl:template>

    <!--
        Render a keyword field to a named property. The name of the VIVO property (namespace:element) is passed in propertyName,
        the field name is Elements is passed in fieldName.
    -->
    <xsl:template match="api:keyword" mode="renderForProperty">
        <xsl:param name="propertyName" />
        <xsl:param name="fieldName" />

        <xsl:element name="{$propertyName}">
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!--
        Render a pagination field to a named property. The name of the VIVO property (namespace:element) is passed in propertyName,
        the field name is Elements is passed in fieldName.
    -->
    <xsl:template match="api:pagination" mode="renderForProperty">
        <xsl:param name="propertyName" />
        <xsl:param name="fieldName" />

        <xsl:choose>
            <xsl:when test="$propertyName='bibo:pageStart'"><xsl:element name="{$propertyName}"><xsl:value-of select="api:begin-page" /></xsl:element></xsl:when>
            <xsl:when test="$propertyName='bibo:pageEnd'"><xsl:element name="{$propertyName}"><xsl:value-of select="api:end-page" /></xsl:element></xsl:when>
            <xsl:otherwise />
        </xsl:choose>
    </xsl:template>

    <!--
        Render a people list field to a named property. The name of the VIVO property (namespace:element) is passed in propertyName,
        the field name is Elements is passed in fieldName.
        This template is wrapping the output of the api:person template into a property, in order to create a comma-delimited list
    -->
    <xsl:template match="api:people" mode="renderForProperty">
        <xsl:param name="propertyName" />
        <xsl:param name="fieldName" />

        <xsl:element name="{$propertyName}">
            <xsl:apply-templates select="api:person" mode="renderForProperty">
                <xsl:with-param name="propertyName" select="$propertyName" />
                <xsl:with-param name="fieldName" select="$fieldName" />
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <!--
        Render a person. This template is designed to create a comma-delimited list.
    -->
    <xsl:template match="api:person" mode="renderForProperty">
        <xsl:param name="propertyName" />
        <xsl:param name="fieldName" />

        <xsl:if test="preceding-sibling::*">
            <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="api:last-name" /><xsl:text> </xsl:text><xsl:value-of select="api:initials" />
    </xsl:template>

    <!--
        Render a text field to a named property. The name of the VIVO property (namespace:element) is passed in propertyName,
        the field name is Elements is passed in fieldName.
    -->
    <xsl:template match="api:text" mode="renderForProperty">
        <xsl:param name="propertyName" />
        <xsl:param name="fieldName" />

        <xsl:element name="{$propertyName}">
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
