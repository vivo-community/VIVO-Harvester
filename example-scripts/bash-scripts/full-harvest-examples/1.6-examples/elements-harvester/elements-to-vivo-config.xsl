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
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                exclude-result-prefixes="rdf rdfs bibo vivo config foaf score ufVivo vitro api svfn symp xs"
        >

    <!-- The base URI you are using for VIVO identifiers -->
    <xsl:param name="baseURI">http://localhost:8080/vivo/individual/</xsl:param>

    <!-- Harvested by statement for the URI (set to blank if not required) -->
    <xsl:param name="harvestedBy"></xsl:param>

    <!-- DO NOT TOUCH: Read the record and journal precedence configuration into variables for processing -->
    <xsl:variable name="record-precedences" select="document('')//config:record-precedences/config:record-precedences" />
    <xsl:variable name="journal-precedence" select="document('')//config:journal-precedences/config:journal-precedence" />

    <!--
        Configure precedence for records
        ================================

        The "for" attribute determines which object type that set of precedences applies to. "default" is used for
        objects where the type does not have it's own configuration.

        Use select-by="field" attribute to choose the field from the highest precedence record in which it occurs.

        Otherwise, it will select the highest precedence record, regardless of whether field exizts.

        If a record is not listed, it will not be used (except when using the "fallback to first record" function).
    -->
    <config:record-precedences>
        <config:record-precedences for="publication" select-by="field">
            <config:record-precedence>pubmed</config:record-precedence>
            <config:record-precedence>manual</config:record-precedence>
            <config:record-precedence>arxiv</config:record-precedence>
        </config:record-precedences>
        <config:record-precedences for="default" select-by="field">
            <config:record-precedence>manual</config:record-precedence>
        </config:record-precedences>
    </config:record-precedences>

    <!--
        Configure precedence for retrieving journal names
        =================================================

        If type="authority", then attempt to use the named authority source included in the publication

        If type="record", then attempt to use the named record, taking the value from "field" (defaults to "journal")
    -->
    <config:journal-precedences>
        <config:journal-precedence type="authority">sherpa-romeo</config:journal-precedence>
        <config:journal-precedence type="authority">science-metrix</config:journal-precedence>
        <config:journal-precedence type="record" field="journal">pubmed</config:journal-precedence>
        <config:journal-precedence type="record" field="journal">manual</config:journal-precedence>
        <config:journal-precedence type="record" field="journal">arxiv</config:journal-precedence>
    </config:journal-precedences>
</xsl:stylesheet>
