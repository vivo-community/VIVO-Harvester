<!--
  Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
  All rights reserved.
  This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
-->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->

<xsl:stylesheet version = "2.0"
	xmlns:xsl = 'http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
	xmlns:core = 'http://vivoweb.org/ontology/core#'
	xmlns:score = 'http://vivoweb.org/ontology/score#'
    xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
    xmlns:bibo = 'http://purl.org/ontology/bibo/'
    xmlns:db-CSV='jdbc:h2:data/csv/store/fields/CSV2/'>
	
	<xsl:output method = "xml" indent = "yes"/>
	<xsl:variable name = "baseURI">http://vivoweb.org/harvest/csvfile/</xsl:variable>
	
	<xsl:template match = "rdf:RDF">
		<rdf:RDF xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
            xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
    		xmlns:core = 'http://vivoweb.org/ontology/core#'
            xmlns:score = 'http://vivoweb.org/ontology/score#'
            xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
            xmlns:bibo = 'http://purl.org/ontology/bibo/'>
			<xsl:apply-templates select = "rdf:Description" />		
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match = "rdf:Description">
		<xsl:variable name = "this" select = "." />
		<xsl:call-template name = "t_People">
		  <xsl:with-param name = "this" select = "$this" />
          <xsl:with-param name = "personid" select = "$this/db-CSV:PERSONID" />
		</xsl:call-template>
	</xsl:template>
		
        <xsl:template name="t_People"
           xmlns:authortools = "java:org.vivoweb.harvester.util.xslt.AuthorTools" 
           extension-element-prefixes = "authortools" >

	    <xsl:param name = 'personid' />
	    <xsl:param name = 'this' />

            <xsl:variable name="lastName" >
            <xsl:choose>
            <xsl:when test="normalize-space( $this/db-CSV:LASTNAME )">
            <xsl:value-of select="$this/db-CSV:LASTNAME" />
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
    
            <xsl:variable name="firstName" >
            <xsl:choose>
            <xsl:when test="normalize-space( $this/db-CSV:FIRSTNAME )">
            <xsl:value-of select="$this/db-CSV:FIRSTNAME" />
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
    
            <xsl:variable name="middleName" >
            <xsl:choose>
            <xsl:when test="normalize-space( $this/db-CSV:MIDNAME )">
            <xsl:value-of select="$this/db-CSV:MIDNAME" />
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
    
	    <rdf:Description rdf:about = "{$baseURI}person/person{$personid}">
            <rdf:type rdf:resource = "http://xmlns.com/foaf/0.1/Person"/>
	    <score:personID><xsl:value-of select = "$personid" /></score:personID>
            <xsl:if test="normalize-space($lastName)" >
                <foaf:lastName><xsl:value-of select = "$lastName"/></foaf:lastName>
            </xsl:if>

            <xsl:if test="normalize-space($firstName)" >
                <foaf:firstName><xsl:value-of select = "$firstName"/></foaf:firstName>
            </xsl:if>

            <xsl:if test="normalize-space($middleName)" >
                <core:middleName><xsl:value-of select = "$middleName"/></core:middleName>
            </xsl:if>

           <xsl:if test="normalize-space($lastName)" >
           <score:authorName><xsl:value-of select="authortools:normalizeAuthorName($lastName, $firstName, $middleName)" /></score:authorName>
           </xsl:if>
        
			
            
	    </rdf:Description>
	</xsl:template>
</xsl:stylesheet>
