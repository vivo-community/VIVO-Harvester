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
                xmlns:symp="http://www.symplectic.co.uk/vivo/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                exclude-result-prefixes="rdf rdfs bibo vivo foaf score ufVivo vitro api symp svfn config xs"
        >

    <!--
        Template for handling relationships between users and publications as editors
    -->

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match relationship of type publication-user editorship association -->
    <xsl:template match="api:relationship[@type='publication-user-editorship']">
        <!-- Get the publication object reference from the relationship -->
        <xsl:variable name="publication" select="api:related/api:object[@category='publication']" />

        <!-- Get the user object reference from the relationship -->
        <xsl:variable name="user" select="api:related/api:object[@category='user']" />

        <!-- Add an editor of statement to the user object -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="svfn:userURI($user)" />
            <xsl:with-param name="rdfNodes">
                <vivo:relatedBy rdf:resource="{svfn:objectURI($publication)}"/>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Add an edited by statement to the publication object -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="svfn:objectURI($publication)" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/IAO_0000030"/>
                <vivo:relatedBy rdf:resource="{svfn:userURI($user)}"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
