<!--
  Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
  All rights reserved.
  This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
-->
<!-- <?xml version="1.0"?> -->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:ns2="http://woksearchlite.cxf.wokmws.thomsonreuters.com"
	xmlns:core='http://vivoweb.org/ontology/core#'
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/wos/</xsl:variable>

	<!-- The root of the XML returned by WOS.  This serves as the header of the RDF file produced. -->
	<xsl:template match="/soap:Envelope/soap:Body/ns2:searchResponse/return">
		<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
		         xmlns:core='http://vivoweb.org/ontology/core#'
		         xmlns:foaf="http://xmlns.com/foaf/0.1/"
		         xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>
			<xsl:apply-templates select="records" />
		</rdf:RDF>
	</xsl:template>

	<!-- Each publication record is contained in a "records" node -->
	<xsl:template match="records">
		<rdf:Description rdf:about="{$baseURI}pub/wosid{UT}pub{position()}">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
			<rdfs:label><xsl:value-of select="title/values" /></rdfs:label>
			
			<!-- Authors must be handled both within the publication node (to link to the Authorship
			     node), and outside the publication node (to create the Author and Authorship nodes).
			     This is handled by selecting "authors/values" with modes "withinPub" and "standAlone",
			     respectively. 
			-->
			<xsl:apply-templates select="authors/values" mode="withinPub"/>
		</rdf:Description>

		<xsl:apply-templates select="authors/values" mode="standAlone" />
	</xsl:template>
	
	<!-- Match authors and place resulting XML outside of the publication node. -->
	<xsl:template match="authors/values" mode="standAlone">
		
		<!-- Sample output from WOS: <values>Elinder, CG</values> -->
		<xsl:variable name="label" select="." />
		<xsl:variable name="firstName" select="substring(substring-after(., ', '), 1, 1)" />
		<xsl:variable name="middleName" select="substring(substring-after(., ', '), 2, 1)" />
		<xsl:variable name="lastName" select="substring-before(., ', ')" />

		<!-- Create the Author node -->
		<rdf:Description rdf:about="{$baseURI}author/wosid{ancestor::records/UT}author{position()}">
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<foaf:firstName><xsl:value-of select="$firstName" /></foaf:firstName>
			<xsl:if test="$middleName != ''">
				<core:middleName><xsl:value-of select="$middleName" /></core:middleName>
			</xsl:if>
			<foaf:lastName><xsl:value-of select="$lastName" /></foaf:lastName>
			<core:authorInAuthorship rdf:resource="{$baseURI}authorship/wosid{ancestor::records/UT}authorship{position()}" />
		</rdf:Description>
		
		<!-- Create the Authorship node -->
		<rdf:Description rdf:about="{$baseURI}authorship/wosid{ancestor::records/UT}authorship{position()}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:authorRank><xsl:value-of select="position()" /></core:authorRank>
			<core:linkedAuthor rdf:resource="{$baseURI}author/wosid{ancestor::records/UT}author{position()}" />
			<core:linkedInformationResource rdf:resource="{$baseURI}pub/wosid{UT}pub{position()}" />
		</rdf:Description>
		
	</xsl:template>

	
	<!-- Match authors and place resulting XML inside the publication node. -->
	<xsl:template match="authors/values" mode="withinPub">
		<core:informationResourceInAuthorship rdf:resource="{$baseURI}authorship/wosid{ancestor::records/UT}authorship{position()}" />
	</xsl:template>
	
</xsl:stylesheet>
	