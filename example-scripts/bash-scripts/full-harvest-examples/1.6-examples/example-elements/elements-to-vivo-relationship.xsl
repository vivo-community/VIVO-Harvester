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
        Relationships link object in Elements - i.e. users (as authors) to publications, users to activities, publications to grants, etc.

        We process relationships in order to output the RDF statements that link objects together
        (even though they are properties of the objects themselves - the statements of the stub objects output here
        are combined with the statements elsewhere to make the full objects).

        In some cases, we need to add the statements for an activity directly to the user that they are linked to -
        this is also done from here, by reading the document of the activity and apply templates in a special mode.
    -->

    <!-- Default template - do not output relationship, unless overridden -->
    <xsl:template match="api:relationship" />

    <!--
        Import XSLT to handle each type of relationship
    -->
    <xsl:include href="elements-to-vivo-relationship-publication-author.xsl" />
    <xsl:include href="elements-to-vivo-relationship-publication-editor.xsl" />
    <xsl:include href="elements-to-vivo-relationship-professional-activity.xsl" />
    <xsl:include href="elements-to-vivo-relationship-teaching-activity.xsl" />
</xsl:stylesheet>
