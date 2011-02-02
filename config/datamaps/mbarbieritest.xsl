
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">

	<xsl:output method="xml" indent="yes" />

	<xsl:template match="/mods">
		<rdf:RDF>
			<xsl:apply-templates select="titleInfo" />
			<xsl:apply-templates select="name" />
			<xsl:apply-templates select="originInfo/place" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="titleInfo">
		<rdf:description>
			<core:title><xsl:value-of select="concat(title, ' ', subTitle)" /></core:title>
			<core:informationResource rdf:resource="authorship node" />
		</rdf:description>
	</xsl:template>

	<xsl:template match="name">
		<rdf:description>
			<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
			<core:linkedInformationResource rdf:resource="article (title) node" />
			<core:linkedAuthor rdf:resource="person (label) node" />
		</rdf:description>
		<rdf:description rdf:about="">
			<rdfs:label><xsl:value-of select="namePart" /></rdfs:label>
			<core:authorInAuthorship rdf:resource="authorship node"></core:authorInAuthorship>
		</rdf:description>
	</xsl:template>
	
	<xsl:template match="originInfo/place">
		<rdf:description>
			<core:placeOfPublication><xsl:value-of select="placeTerm" /></core:placeOfPublication>
		</rdf:description>
	</xsl:template>
	
</xsl:stylesheet>


<!-- 
	<xsl:output method="xml" indent="yes" />

	<xsl:template match="/mods/titleInfo">
		<xsl:variable name="identifier" select="./mods/recordInfo/recordIdentifier" />
		<xsl:variable name="title" select="./mods/titleInfo/title" />
		<xsl:variable name="subTitle" select="./mods/titleInfo/subTitle" />
		
		<rdf:description rdf:about="http://vivoweb.org/ontology/core#$identifier">
			<core:title><xsl:value-of select="$title" /></core:title>
			<core:informationResource rdf:resource="authorship node" />
		</rdf:description>
	</xsl:template>

	<xsl:template match="/mods/name">
		<rdf:description rdf:about="">
			<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
			<core:linkedInformationResource rdf:resource="article (title) node" />
			<core:linkedAuthor rdf:resource="person (label) node" />
		</rdf:description>
		<rdf:description rdf:about="">
			<rdfs:label><xsl:value-of select="namePart" /></rdfs:label>
			<core:authorInAuthorship rdf:resource="authorship node"></core:authorInAuthorship>
		</rdf:description>
	</xsl:template>
-->


