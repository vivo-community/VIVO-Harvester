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
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
	xmlns:core = 'http://vivoweb.org/ontology/core#'
	xmlns:score = 'http://vivoweb.org/ontology/score#'
	xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
	xmlns:bibo = 'http://purl.org/ontology/bibo/'
	xmlns:ctsaip = 'http://vivo.ufl.edu/ontology/ctsaip/'
	xmlns:vitroApp = 'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'>
	
	<xsl:output method = "xml" indent = "yes"/>
	<xsl:variable name = "baseURI">http://vivoweb.org/harvest/ip/</xsl:variable>
	
	<xsl:template match = "all-technology">
		<rdf:RDF xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
            		xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
    				xmlns:core = 'http://vivoweb.org/ontology/core#'
            		xmlns:score = 'http://vivoweb.org/ontology/score#'
            		xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
            		xmlns:bibo = 'http://purl.org/ontology/bibo/'
            		xmlns:ctsaip = 'http://vivo.ufl.edu/ontology/ctsaip/'
            		xmlns:vitroApp = 'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'>
			<xsl:apply-templates select = "technology" />		
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match = "technology">
		<xsl:variable name="ctsai_id" >
			 <xsl:if test="normalize-space( ctsaip-link )">
                                <xsl:analyze-string select="ctsaip-link" regex="\d+">
                                        <xsl:matching-substring>
                                                <xsl:value-of select="regex-group(0)" />
                                        </xsl:matching-substring>
                                </xsl:analyze-string>
                        </xsl:if>
		</xsl:variable>
		<xsl:variable name="summary-var" select="summary" />
		<rdf:Description rdf:about="{$baseURI}tech/{$ctsai_id}">
			<xsl:choose>
				<xsl:when test='type="Technology"'>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Technology" />
					<vitroApp:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Technology" />
				</xsl:when>
				<xsl:when test='type="Research Tool"'>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/ResearchTool" />
					<vitroApp:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/ResearchTool" />
				</xsl:when>
				<xsl:when test='type="Material"'>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Material" />
					<vitroApp:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Material" />
				</xsl:when>
				<xsl:otherwise>
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Thing" />
					<vitroApp:mostSpecificType rdf:resource="http://xmlns.com/foaf/0.1/Thing" />
				</xsl:otherwise>
			</xsl:choose>
			<rdfs:label><xsl:value-of select="title" /></rdfs:label>
			<core:webpage><xsl:value-of select="insitution-link" /></core:webpage>
			<core:webpage><xsl:value-of select="ctsaip-link" /></core:webpage>
			<bibo:abstract><xsl:value-of select="replace($summary-var,'&lt;/? ?[a-xA-X0-9]*/?&gt;','')" /></bibo:abstract>
			<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Innovation" />

			<!-- Listed as assignees for now are organizations and people assigned to this project -->
			<core:assignee rdf:resource="{$baseURI}casemngr/{$ctsai_id}" />

		</rdf:Description>
		<rdf:Description>
			<rdf:label><xsl:value-of select="instituion" /></rdf:label>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
		</rdf:Description>
		<rdf:Description rdf:about="{$baseURI}casemngr/{$ctsai_id}" >
			<rdfs:label><xsl:value-of select="contact-name" /></rdfs:label>		
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<core:email><xsl:value-of select="contact-email" /></core:email>
			<core:assigneeFor rdf:resource="{$baseURI}tech/{$ctsai_id}" />
		</rdf:Description>
	</xsl:template>
		
</xsl:stylesheet>
