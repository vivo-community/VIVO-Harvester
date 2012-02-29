<!--
  Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
  All rights reserved.
  This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
-->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:db-csv='jdbc:h2:data/csv/store/fields/CSV2/'>
	
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/ip/</xsl:variable>
	
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    		xmlns:core="http://vivoweb.org/ontology/core#"
    		xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    		xmlns:score='http://vivoweb.org/ontology/score#'>
			<xsl:apply-templates select="rdf:Description" />		
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match="rdf:Description">
		<xsl:call-template name="t_Grant">
          <xsl:with-param name="grantid" select="./db-csv:GRANTID" />
		</xsl:call-template>
	</xsl:template>
		
	<xsl:template name="t_Grant">
		<xsl:param name='grantid' />
		
		<rdf:Description rdf:about="{$baseURI}patent/{$grantid}">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Patent"/>
			<xsl:if test="not( ./db-csv:GRANTNAME = '' or ./db-csv:GRANTNAME = 'null' )">
				<rdfs:label><xsl:value-of select="title"/></rdfs:label>
			</xsl:if>
			<bibo:abstract><xsl:value-of select="description" /></bibo:abstract>
		</rdf:Description>
	</xsl:template>
	
</xsl:stylesheet>
