<?xml version="1.0"?>
<!--
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
-->
<!-- 
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams
  
  This file is part of VIVO.
  
  VIVO is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  VIVO is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with VIVO.  If not, see <http://www.gnu.org/licenses/gpl-3.0.txt>.
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial implementation
      
  OAI to VIVO Translation File
  This file describes the translation of elements returned from a query to an OAI compliant
  repository.  These repositories return XML, the specifications of which are found at
  http://www.openarchives.org/OAI/openarchivesprotocol.html#harvester.  The university of 
  florida hosts an OAI compliant repository
  
  TODO:  Flesh out this translation
 -->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns, xml namespace, for each prefix you use in constructing
	the new elements
-->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:template match="/ListRecords">
		<rdf:RDF xmlns:owlPlus='http://www.w3.org/2006/12/owl2-xml#'
			xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
			xmlns:skos='http://www.w3.org/2008/05/skos#'
			xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
			xmlns:owl='http://www.w3.org/2002/07/owl#'
			xmlns:vocab='http://purl.org/vocab/vann/'
			xmlns:swvocab='http://www.w3.org/2003/06/sw-vocab-status/ns#'
			xmlns:dc='http://purl.org/dc/elements/1.1/'
			xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#' 
			xmlns:core='http://vivoweb.org/ontology/core#'
			xmlns:foaf='http://xmlns.com/foaf/0.1/'
			xmlns:score='http://vivoweb.org/ontology/score#'
			xmlns:xs="http://www.w3.org/2001/XMLSchema#">
			<xsl:apply-templates select="record" />			
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match="record">
		<rdf:Description rdf:about="http://vivoweb.org/oaiDublinCore/record/{child::Header/identifier}">
			<xsl:apply-templates select="metadata/oai_dc:dc" />
		</rdf:Description>	
	</xsl:template>
	
	<xsl:template match="metadata/oai_dc:dc">
		<rdfs:label><xsl:value-of select="dc:title" /></rdfs:label>
		<core:Title><xsl:value-of select="dc:title" /></core:Title>
		<bibo:contributorList><xsl:value-of select="dc:creator" /></bibo:contributorList>
		<score:SubjectString><xsl:value-of select="dc:subject" /></score:SubjectString>
		<core:Description><xsl:value-of select="dc:description" /></core:Description>
		<bibo:identifier><xsl:value-of select="dc:identifier" /></bibo:identifier>
	</xsl:template>
	
</xsl:stylesheet>
