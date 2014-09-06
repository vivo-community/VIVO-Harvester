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
        XSLT for generating VIVO representation of a publication object (journal article, conference preceding, etc.)
    -->

    <!-- Import the utility XSLT -->
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match objects of type 'publication' -->
    <xsl:template match="api:object[@category='publication']">
        <!-- Attempt to generate a URI for the publication date object -->
        <xsl:variable name="publicationDateURI" select="concat(svfn:objectURI(.),'-publicationDate')" />

        <!-- Generate the publication date object. Custom XSLT 2 function that takes the current object, the URI for the date, and the field that holds the publication date -->
        <xsl:variable name="publicationDateObject" select="svfn:renderDateObject(.,$publicationDateURI,svfn:getRecordField(.,'publication-date'))" />

        <!-- Attempt to get a journal title for the article -->
        <xsl:variable name="publicationVenueTitle" select="svfn:selectJournalTitle(.)" />

        <!-- Generate a publication venue object URI from the journal title -->
        <xsl:variable name="publicationVenueURI" select="concat($baseURI, 'journal-', svfn:stringToURI($publicationVenueTitle))" />

        <!-- Generate the publication venue object. Custom XSLT 2 function that takes the current object, journal URI and journal title. -->
        <xsl:variable name="publicationVenueObject" select="svfn:renderPublicationVenueObject(.,$publicationVenueURI,$publicationVenueTitle)" />

        <!-- Render an RDF object -->
        <xsl:call-template name="render_rdf_object">
            <!-- Generate a URI for the current publication -->
            <xsl:with-param name="objectURI" select="svfn:objectURI(.)" />
            <!--
                Generate the RDF property statements for the publication object.
                Uses the getTypesForPublication custom XSLT function to retrieve all the type statements (including mostSpecificType)
                The renderPropertyFromField function creates a property from the specified Elements data field, restricted to your configured preferred records
                - it takes the full namespace/element name for the resulting property, and the field name in Elements
                  The resulting property comes from an apply-templates on the selected field, so you can override how the data is rendered
                  by overloading those templates
                The renderPropertyFromFieldOrFirst operates the same way, except if none of you preferred data sources are present, it takes the field from the first record
                - this is used only for the title, so that there will always be some information about the items that could not be transferred fully, because of the records available
                Also, the properties to reference the publication date and venue are output, if the objects have been created (test for child nodes)
            -->
            <xsl:with-param name="rdfNodes">
                <xsl:copy-of select="svfn:getTypesForPublication(@type)" />
                <xsl:copy-of select="svfn:renderPropertyFromFieldOrFirst(.,'rdfs:label','title')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:abstract','abstract')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:doi','doi')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:issue','issue')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:pageStart','pagination')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:pageEnd','pagination')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:volume','volume')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'vivo:freetextKeyword','keywords')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:authors','authors')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:language','language')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:location','location')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:notes','notes')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:pii','pii')" />
                <xsl:if test="$publicationDateObject/*"><vivo:dateTimeValue rdf:resource="{$publicationDateURI}" /></xsl:if>
                <xsl:if test="$publicationVenueObject/*"><vivo:hasPublicationVenue rdf:resource="{$publicationVenueURI}" /></xsl:if>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Output the publication date and venue objects. If they are empty, nothing will be output -->
        <xsl:copy-of select="$publicationDateObject" />
        <xsl:copy-of select="$publicationVenueObject" />
    </xsl:template>

    <!-- ====================================================
         XSLT Function Library
         ==================================================== -->

    <!-- Read the publication types XML configuration file -->
    <xsl:variable name="publication-types" select="document('elements-to-vivo-publication-types.xml')//config:publication-types" />

    <!-- Get publication type statements from the XML configuration (for the type supplied as a parameter) -->
    <xsl:function name="svfn:getTypesForPublication">
        <xsl:param name="type" as="xs:string" />

        <!-- Copy the publication type statements from the XML into a variable -->
        <xsl:variable name="publication-type">
            <xsl:choose>
                <xsl:when test="$publication-types/config:publication-type[@type=$type]"><xsl:copy-of select="$publication-types/config:publication-type[@type=$type]/*" /></xsl:when>
                <xsl:when test="$publication-types/config:publication-type[@type='z-default']"><xsl:copy-of select="$publication-types/config:publication-type[@type='z-default']/*" /></xsl:when>
                <xsl:otherwise><xsl:copy-of select="$publication-types/config:publication-type[1]/*" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Determine most specific type -->
        <xsl:choose>
            <!-- if the configuration specifies a most specific type, copy that -->
            <xsl:when test="$publication-type/vitro:mostSpecificType">
                <vitro:mostSpecificType rdf:resource="{$publication-type/vitro:mostSpecificType/@rdf:resource}" />
            </xsl:when>
            <!-- no most specific type designated, so use the first type listed -->
            <xsl:otherwise>
                <vitro:mostSpecificType rdf:resource="{$publication-type/rdf:type[1]/@rdf:resource}" />
            </xsl:otherwise>
        </xsl:choose>
        <!-- Copy all of the rdf:type statements from the selected configuration to the output -->
        <xsl:for-each select="$publication-type/rdf:type">
            <rdf:type rdf:resource="{@rdf:resource}" />
        </xsl:for-each>
        <!-- Everything is a 'Thing' and an 'Information Resource', so add these anyway (so they don't need to be in the configuration -->
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
    </xsl:function>

    <!-- Select the journal title for the publication. Delegates to an internal function for iteration -->
    <xsl:function name="svfn:selectJournalTitle">
        <xsl:param name="object" />
        <xsl:copy-of select="string(svfn:_selectJournalTitle($object, 1))" />
    </xsl:function>

    <!-- Iterating function to select the most appropriate journal title -->
    <xsl:function name="svfn:_selectJournalTitle">
        <xsl:param name="object" />
        <xsl:param name="position" as="xs:integer" />

        <!-- Ensure that we haven't reached the end of the precedence settings -->
        <xsl:if test="$journal-precedence[$position]">
            <xsl:choose>
                <!-- If the precedence is for an authority record, and we have that authority record in the API feed, use the journal title in the preferred authority source -->
                <xsl:when test="$journal-precedence[$position]/@type='authority' and $object/api:journal/api:records/api:record[@source-name=$journal-precedence[$position]]/api:title">
                    <xsl:value-of select="$object/api:journal/api:records/api:record[@source-name=$journal-precedence[$position]]/api:title" />
                </xsl:when>
                <!-- If the precedence if for a record value from a specific field, use that value if it exists -->
                <xsl:when test="$journal-precedence[$position]/@type='record' and $object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name=$journal-precedence[$position]/@field]/api:text">
                    <xsl:value-of select="$object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name=$journal-precedence[$position]/@field]/api:text" />
                </xsl:when>
                <!-- If the precedence if for a record value from a field called 'journal', use that value if it exists -->
                <xsl:when test="$journal-precedence[$position]/@type='record' and $object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name='journal']/api:text">
                    <xsl:value-of select="$object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name='journal']/api:text" />
                </xsl:when>
                <!-- No title found for the current precedence setting, try again with the next entry -->
                <xsl:otherwise>
                    <xsl:copy-of select="svfn:_selectJournalTitle($object, $position+1)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:function>

    <!-- Render the RDF object for the publication venue (journal) -->
    <xsl:function name="svfn:renderPublicationVenueObject">
        <xsl:param name="object" />
        <xsl:param name="journalObjectURI" as="xs:string" />
        <xsl:param name="journalTitle" as="xs:string" />

        <!-- Only create the object if we have a journal title -->
        <xsl:if test="$journalTitle">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$journalObjectURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Periodical"/>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal"/>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Collection"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
                    <rdfs:label><xsl:value-of select="$journalTitle" /></rdfs:label>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:function>
</xsl:stylesheet>
