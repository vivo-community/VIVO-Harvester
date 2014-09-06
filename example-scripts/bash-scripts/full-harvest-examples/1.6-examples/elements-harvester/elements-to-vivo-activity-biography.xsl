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

    <!-- Import general config / utils XSLT -->
    <xsl:import href="elements-to-vivo-config.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!--
        Biography Professional Activity
        ===============================
        This professional activity is adding statements to the user (person) object.
        As such, it needs to be executed as part of the relationship processing - so we add the mode="processRelationship".
        In this mode, the template will be passed a parameter, which is the URI of the user object.
    -->
    <xsl:template match="api:object[@category='activity' and @type='c-biography']" mode="processRelationship">
        <xsl:param name="userURI" />

        <!-- Retrieve the full object for the current api:object. This maps in the XML previously downloaded and stored in a separate file -->
        <xsl:variable name="fullActivityObj" select="svfn:fullObject(.)" />

        <!--
            If we have a value for the biography, output an RDF object for the user, with the overview statement
            (this will be added to other RDF assertions made elsewhere for the same user (URI))
        -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$userURI" />
            <xsl:with-param name="rdfNodes">
                <xsl:copy-of select="svfn:renderPropertyFromField($fullActivityObj,'vivo:overview','c-description')" />
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
