<xsl:stylesheet version="2.0"
                xmlns:xsl = "http://www.w3.org/1999/XSL/Transform"
                xmlns:xs = "http://www.w3.org/2001/XMLSchema"
                xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
                xmlns:rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs = "http://www.w3.org/2000/01/rdf-schema#"
                xmlns:vivo = "http://vivoweb.org/ontology/core#"
                xmlns:score = "http://vivoweb.org/ontology/score#"
                xmlns:foaf = "http://xmlns.com/foaf/0.1/"
                xmlns:bibo = "http://purl.org/ontology/bibo/"
                xmlns:obo = "http://purl.obolibrary.org/obo/"
                xmlns:vitro = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:vcard = "http://www.w3.org/2006/vcard/ns#"
                xmlns:geo = "http://aims.fao.org/aos/geopolitical.owl#"
                xmlns:mets = "http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:node-oai = "http://vivo.example.com/harvest/fields/oai/"
                xmlns:localVivo = "http://vivo.sample.edu/ontology/"
                xmlns:oai_dc = "http://www.openarchives.org/OAI/2.0/oai_dc/"
                xmlns:dc = "http://purl.org/dc/elements/1.1/"
                xmlns:core = 'http://vivoweb.org/ontology/core#'
                xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dcterms.xsdhttp://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd"
                xmlns:stringhash="java:org.vivoweb.harvester.util.xslt.StringHash"
                extension-element-prefixes = "stringhash"
>

    <xsl:output method="xml" indent="yes"/>

    <!-- Base URI for resources -->
    <xsl:variable name="baseURI">http://vivo.example.com/harvest/</xsl:variable>

    <!-- Root Template -->
    <xsl:template match="/oai_dc:dc">
        <rdf:RDF>
            <rdf:Description rdf:about="{$baseURI}oai/{encode-for-uri(dc:identifier[1])}">
                <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
                <xsl:apply-templates select="*[not(self::dc:creator or self::dc:contributor)]" />
            </rdf:Description>

            <xsl:apply-templates select="dc:creator" />
            <xsl:apply-templates select="dc:contributor" />
        </rdf:RDF>
    </xsl:template>

    <!-- Title -->
    <xsl:template match="dc:title">
        <rdfs:label><xsl:value-of select="normalize-space(.)"/></rdfs:label>
    </xsl:template>

    <!-- Creator -->
    <xsl:template match="dc:creator">
        <!-- Output Authorship and Person separately -->
        <xsl:variable name="authorId" select="encode-for-uri(normalize-space(.))"/>
        <xsl:variable name="authorshipId" select="concat('authorship_', $authorId)"/>

        <!-- Authorship -->
        <rdf:Description rdf:about="{$baseURI}{$authorshipId}">
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
            <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship" />
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
            <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000020" />
            <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001" />
            <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002" />
            <rdfs:label>Authorship for <xsl:value-of select="normalize-space(.)"/></rdfs:label>

            <core:relates rdf:resource="{$baseURI}author/{$authorId}" />
            <core:relates rdf:resource="{$baseURI}oai/{encode-for-uri(dc:identifier[1])}" />

            <core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int">
                <xsl:value-of select="position()" />
            </core:authorRank>
        </rdf:Description>

        <!-- Person -->
        <rdf:Description rdf:about="{$baseURI}author/{$authorId}">
            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
            <rdfs:label><xsl:value-of select="normalize-space(.)"/></rdfs:label>
        </rdf:Description>
    </xsl:template>

    <!-- Contributor -->
    <xsl:template match="dc:contributor">
        <!-- Output Authorship and Person separately -->
        <xsl:variable name="contributorId" select="encode-for-uri(normalize-space(.))"/>
        <xsl:variable name="authorshipId" select="concat('authorship_', contributorId)"/>

        <!-- Authorship -->
        <rdf:Description rdf:about="{$baseURI}{$authorshipId}">
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
            <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship" />
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
            <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000020" />
            <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001" />
            <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002" />
            <rdfs:label>Authorship for <xsl:value-of select="normalize-space(.)"/></rdfs:label>

            <core:relates rdf:resource="{$baseURI}author/{$contributorId}" />
            <core:relates rdf:resource="{$baseURI}oai/{encode-for-uri(dc:identifier[1])}" />

            <core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int">
                <xsl:value-of select="position()" />
            </core:authorRank>
        </rdf:Description>

        <!-- Person -->
        <rdf:Description rdf:about="{$baseURI}author/{$contributorId}">
            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
            <rdfs:label><xsl:value-of select="normalize-space(.)"/></rdfs:label>
        </rdf:Description>
    </xsl:template>

    <!-- Subject -->
    <xsl:template match="dc:subject">
        <vivo:hasSubjectArea>
            <rdf:Description rdf:about="{$baseURI}contributor/{encode-for-uri(.)}">
                <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept" />
                <rdfs:label><xsl:value-of select="normalize-space(.)"/></rdfs:label>
            </rdf:Description>
        </vivo:hasSubjectArea>
    </xsl:template>

    <!-- Description -->
    <xsl:template match="dc:description">
        <bibo:abstract><xsl:value-of select="normalize-space(.)"/></bibo:abstract>
    </xsl:template>

    <!-- Date -->
    <xsl:template match="dc:date">
        <vivo:dateTimeValue>
            <rdf:Description rdf:about="{$baseURI}oai/{encode-for-uri(dc:identifier[1])}">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#date">
                    <xsl:value-of select="normalize-space(.)"/>
                </vivo:dateTime>
            </rdf:Description>
        </vivo:dateTimeValue>
    </xsl:template>

    <!-- Type -->
    <xsl:template match="dc:type">
        <xsl:choose>
            <xsl:when test="ends-with(., 'article')">
                <rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
            </xsl:when>

            <xsl:when test="ends-with(., 'publishedVersion')">
                <rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- Identifier -->
    <xsl:template match="dc:identifier">
        <bibo:uri><xsl:value-of select="normalize-space(.)"/></bibo:uri>
    </xsl:template>

    <!-- Publisher -->
    <xsl:template match="dc:publisher">
        <vivo:publisher>
            <rdf:Description rdf:about="{$baseURI}publisher/{encode-for-uri(.)}">
                <rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal" />
                <rdfs:label><xsl:value-of select="normalize-space(.)"/></rdfs:label>
            </rdf:Description>
        </vivo:publisher>
    </xsl:template>

    <!-- Language -->
    <xsl:template match="dc:language">
        <vivo:language><xsl:value-of select="normalize-space(.)"/></vivo:language>
    </xsl:template>

    <!-- Relation -->
    <xsl:template match="dc:relation">
        <vivo:relatedLink>
            <rdf:Description>
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#URL" />
                <vcard:url><xsl:value-of select="normalize-space(.)"/></vcard:url>
            </rdf:Description>
        </vivo:relatedLink>
    </xsl:template>

    <!-- Rights -->
    <xsl:template match="dc:rights">
        <bibo:rights><xsl:value-of select="normalize-space(.)"/></bibo:rights>
    </xsl:template>

    <!-- Format -->
    <xsl:template match="dc:format">
        <vivo:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
            <xsl:value-of select="normalize-space(.)"/>
        </vivo:format>
    </xsl:template>

    <!-- Source -->
    <xsl:template match="dc:source">
        <bibo:source><xsl:value-of select="normalize-space(.)"/></bibo:source>
    </xsl:template>

    <!-- Coverage -->
    <xsl:template match="dc:coverage">
        <vivo:coverage><xsl:value-of select="normalize-space(.)"/></vivo:coverage>
    </xsl:template>
</xsl:stylesheet>
