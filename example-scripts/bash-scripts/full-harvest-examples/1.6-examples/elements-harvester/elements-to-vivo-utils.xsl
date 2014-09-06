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
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:bibo="http://purl.org/ontology/bibo/"
                xmlns:vivo="http://vivoweb.org/ontology/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:obo="http://purl.obolibrary.org/obo/"
                exclude-result-prefixes="api config xs fn svfn"
                >

    <xsl:import href="elements-to-vivo-datatypes.xsl" />

    <!-- ======================================
         Function Library
         ======================================- -->

    <!--
        svfn:objectURI
        ==============
        Create a URI for the RDF objects based on the passed Elements object
    -->
    <xsl:function name="svfn:objectURI" as="xs:string">
        <xsl:param name="object" />

        <xsl:value-of select="concat($baseURI,$object/@category,$object/@id)" />
    </xsl:function>

    <!--
        svfn:userURI
        ============
        Create a URI for a user based on the passed Elements object
    -->
    <xsl:function name="svfn:userURI" as="xs:string">
        <xsl:param name="object" />

        <xsl:value-of select="concat($baseURI,$object/@username)" />
    </xsl:function>

    <!--
        svfn:relationshipURI
        ====================
        Create a URI for a relationship object, based on the given Elements relationship object
    -->
    <xsl:function name="svfn:relationshipURI" as="xs:string">
        <xsl:param name="relationship" />
        <xsl:param name="type" />

        <xsl:value-of select="concat($baseURI,$type,$relationship/@id)" />
    </xsl:function>

    <!--
        svfn:renderDateObject
        =====================
        Generate a VIVO date object for the supplied date object
    -->
    <xsl:function name="svfn:renderDateObject">
        <xsl:param name="object" />
        <xsl:param name="dateObjectURI" as="xs:string" />
        <xsl:param name="date" />

        <!-- only output if the date exists -->
        <xsl:if test="$date">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$dateObjectURI" />
                <!-- generate property statements - concat generated statements with fixed statements, as they are only required if the generated statements are output successfully -->
                <xsl:with-param name="rdfNodes">
                    <xsl:call-template name="_concat_nodes_if">
                        <xsl:with-param name="nodesRequired">
                            <xsl:apply-templates select="$date" />
                        </xsl:with-param>
                        <xsl:with-param name="nodesToAdd">
                            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:function>

    <!--
        svfn:datePrecision
        ==================
        Determine how precise the Elements date object is
    -->
    <xsl:function name="svfn:datePrecision" as="xs:string">
        <xsl:param name="date" />

        <xsl:choose>
            <xsl:when test="string($date/api:day) and string($date/api:month) and string($date/api:year)">yearMonthDayPrecision</xsl:when>
            <xsl:when test="string($date/api:month) and string($date/api:year)">yearMonthPrecision</xsl:when>
            <xsl:when test="string($date/api:year)">yearPrecision</xsl:when>
            <xsl:otherwise>none</xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:dateYear
        =============
        Return the formatted year
    -->
    <xsl:function name="svfn:dateYear">
        <xsl:param name="date" />

        <xsl:value-of select="$date/api:year" />
    </xsl:function>

    <!--
        svfn:dateMonth
        ==============
        Return the formatted month
    -->
    <xsl:function name="svfn:dateMonth">
        <xsl:param name="date" />

        <xsl:choose>
            <xsl:when test="string-length($date/api:month)=1">0<xsl:value-of select="$date/api:month" /></xsl:when>
            <xsl:when test="string-length($date/api:month)=2"><xsl:value-of select="$date/api:month" /></xsl:when>
            <xsl:otherwise>01</xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:dateDay
        ============
        Return the formatted day
    -->
    <xsl:function name="svfn:dateDay">
        <xsl:param name="date" />

        <xsl:choose>
            <xsl:when test="string-length($date/api:day)=1">0<xsl:value-of select="$date/api:day" /></xsl:when>
            <xsl:when test="string-length($date/api:day)=2"><xsl:value-of select="$date/api:day" /></xsl:when>
            <xsl:otherwise>01</xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:stringToURI
        ================
        Convert a string into a URI-friendly form (for identifiers)
    -->
    <xsl:function name="svfn:stringToURI">
        <xsl:param name="string" as="xs:string" />

        <xsl:value-of select="fn:encode-for-uri(fn:replace(fn:replace(fn:lower-case(fn:normalize-space($string)), '\s', '-'), '[^a-z0-9\-]', ''))" />
    </xsl:function>

    <!--
        svfn:fullObject
        ===============
        Load the XML for the full object, given an Elements object reference
    -->
    <xsl:function name="svfn:fullObject">
        <xsl:param name="object" />
        <xsl:variable name="filename" select="concat('data/raw-records/',$object/@category,'/',$object/@id)" />

        <xsl:if test="fn:doc-available($filename)">
            <xsl:copy-of select="document($filename)" />
        </xsl:if>
    </xsl:function>

    <!--
        svfn:renderPropertyFromFieldOrFirst
        ===================================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        and render it as the RDF property (propertyName)
        If the field is not present in any preferred records, output the value in the first record present.
    -->
    <xsl:function name="svfn:renderPropertyFromFieldOrFirst">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:copy-of select="svfn:_renderPropertyFromField($object, $propertyName, $fieldName, svfn:getRecordFieldOrFirst($object, $fieldName))" />
    </xsl:function>

    <!--
        svfn:renderPropertyFromField
        ============================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        and render it as the RDF property (propertyName)
    -->
    <xsl:function name="svfn:renderPropertyFromField">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:copy-of select="svfn:_renderPropertyFromField($object, $propertyName, $fieldName, svfn:getRecordField($object, $fieldName))" />
    </xsl:function>

    <!--
        svfn:renderPropertyFromField
        ============================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        and render it as the RDF property (propertyName)
        Overloaded method that takes a comma delimited list of record names to use as the preference order (override the central configuration)
    -->
    <xsl:function name="svfn:renderPropertyFromField">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" as="xs:string" />

        <xsl:copy-of select="svfn:_renderPropertyFromField($object, $propertyName, $fieldName, svfn:getRecordField($object, $fieldName, $records))" />
    </xsl:function>

    <!--
        svfn:getRecordFieldOrFirst
        ==========================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        If the field is not present in any preferred records, output the value in the first record present.
    -->
    <xsl:function name="svfn:getRecordFieldOrFirst">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:choose>
            <xsl:when test="$record-precedences[@for=$object/@category]">
                <xsl:variable name="record-precedence" select="$record-precedences[@for=$object/@category]/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for=$object/@category]/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, true())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="record-precedence" select="$record-precedences[@for='default']/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for='default']/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, true())" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:getRecordField
        ================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
    -->
    <xsl:function name="svfn:getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:choose>
            <xsl:when test="$record-precedences[@for=$object/@category]">
                <xsl:variable name="record-precedence" select="$record-precedences[@for=$object/@category]/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for=$object/@category]/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, false())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="record-precedence" select="$record-precedences[@for='default']/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for='default']/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, false())" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:getRecordField
        ================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        Overloaded method that takes a comma delimited list of record names to use as the preference order (override the central configuration)
    -->
    <xsl:function name="svfn:getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" as="xs:string" />

        <xsl:choose>
            <xsl:when test="$record-precedences[@for=$object/@category]">
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for=$object/@category]/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, fn:tokenize($records,','), $record-precedence-select-by, 1, false())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for='default']/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, fn:tokenize($records,','), $record-precedence-select-by, 1, false())" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:function>

    <!--
        svfn:getRecordField
        ================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        Overloaded method that takes a comma delimited list of record names to use as the preference order (override the central configuration)
        and select-by is 'field' (find first occurrence of the field) or 'record' (use first preferred record, even if the field is not present)
    -->
    <xsl:function name="svfn:getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" as="xs:string" />
        <xsl:param name="select-by" as="xs:string" />

        <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, fn:tokenize($records,','), $select-by, 1, false())" />
    </xsl:function>

    <!--
        Internal XSLT Functions (should not be called from outside this file)
    -->

    <xsl:function name="svfn:_renderPropertyFromField">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="fieldNode" />

        <xsl:apply-templates select="$fieldNode" mode="renderForProperty">
            <xsl:with-param name="propertyName" select="$propertyName" />
            <xsl:with-param name="fieldName" select="$fieldName" />
        </xsl:apply-templates>
    </xsl:function>

    <xsl:function name="svfn:_getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" />
        <xsl:param name="select-by" />
        <xsl:param name="position" as="xs:integer" />
        <xsl:param name="useDefault" as="xs:boolean" />

        <xsl:choose>
            <xsl:when test="$records[$position]">
                <xsl:choose>
                    <xsl:when test="$select-by='field'">
                        <xsl:choose>
                            <xsl:when test="$object/api:records/api:record[@source-name=$records[$position]]/api:native/api:field[@name=$fieldName]">
                                <xsl:copy-of select="$object/api:records/api:record[@source-name=$records[$position]]/api:native/api:field[@name=$fieldName]" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="svfn:_getRecordField($object,$fieldName,$records,$select-by,$position+1,$useDefault)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$object/api:records/api:record[@source-name=$records[$position]]/api:native">
                                <xsl:copy-of select="$object/api:records/api:record[@source-name=$records[$position]]/api:native/api:field[@name=$fieldName]" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="svfn:_getRecordField($object,$fieldName,$records,$select-by,$position+1,$useDefault)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$useDefault">
                <xsl:copy-of select="$object/api:records/api:record[1]/api:native/api:field[@name=$fieldName]" />
            </xsl:when>
        </xsl:choose>
    </xsl:function>

    <!-- ======================================
         Template Library
         ======================================- -->

    <!-- _render_rdf_document -->
    <xsl:template name="render_rdf_document">
        <xsl:param name="rdfNodes" />

        <xsl:if test="$rdfNodes/*">
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                     xmlns:vivo="http://vivoweb.org/ontology/core#"
                     xmlns:dc="http://purl.org/dc/elements/1.1/"
                     xmlns:foaf="http://xmlns.com/foaf/0.1/"
                     xmlns:owl="http://www.w3.org/2002/07/owl#"
                     xmlns:owlPlus="http://www.w3.org/2006/12/owl2-xml#"
                     xmlns:score="http://vivoweb.org/ontology/score#"
                     xmlns:skos="http://www.w3.org/2008/05/skos#"
                     xmlns:swvocab="http://www.w3.org/2003/06/sw-vocab-status/ns#"
                     xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                     xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                     xmlns:vocab="http://purl.org/vocab/vann/"
                     xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                    >
                <xsl:copy-of select="$rdfNodes" />
            </rdf:RDF>
        </xsl:if>
    </xsl:template>

    <!-- _render_rdf_object -->
    <xsl:template name="render_rdf_object">
        <xsl:param name="rdfNodes" />
        <xsl:param name="objectURI" />

        <xsl:if test="$rdfNodes/*">
            <rdf:Description rdf:about="{$objectURI}">
                <xsl:copy-of select="$rdfNodes" />
                <xsl:if test="$harvestedBy!=''">
                    <ufVivo:harvestedBy><xsl:value-of select="$harvestedBy" /></ufVivo:harvestedBy>
                </xsl:if>
            </rdf:Description>
        </xsl:if>
    </xsl:template>

    <xsl:template name="render_empty_rdf">
        <rdf:RDF></rdf:RDF>
    </xsl:template>

    <xsl:template name="_concat_nodes_if">
        <xsl:param name="nodesRequired" />
        <xsl:param name="nodesToAdd" />

        <xsl:if test="$nodesRequired/*">
            <xsl:copy-of select="$nodesRequired" />
            <xsl:copy-of select="$nodesToAdd" />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
