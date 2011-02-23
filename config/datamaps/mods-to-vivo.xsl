<!--
  Copyright (c) 2010 Christopher Haines, James Pence, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, James Pence, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
-->
<!-- <?xml version="1.0"?> -->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/">

	<xsl:output method="xml" indent="yes" />

	<xsl:template match="/modsCollection">
		<rdf:RDF>
			<xsl:apply-templates select="mods" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="mods">
		<xsl:variable name="modsId" select="@ID" />
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			<rdfs:label><xsl:value-of select="concat(titleInfo/title, ' ', titleInfo/subTitle)" /></rdfs:label>
			<core:title><xsl:value-of select="concat(titleInfo/title, ' ', titleInfo/subTitle)" /></core:title>
			<bibo:abstract><xsl:value-of select="abstract"></xsl:value-of></bibo:abstract>
			<bibo:volume><xsl:value-of select="part/detail[@type='volume']"/></bibo:volume>
			<bibo:identity><xsl:value-of select="$modsId"></xsl:value-of></bibo:identity>
			<bibo:isbn-13><xsl:value-of select="identifier[@type='isbn']"></xsl:value-of></bibo:isbn-13>
			<bibo:doi><xsl:value-of select="identifier[@type='doi']"></xsl:value-of></bibo:doi>

			<xsl:choose>
				<xsl:when test="contains(originInfo/dateIssued, '-')">
					<core:year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="originInfo/dateIssued"/></core:year>
				</xsl:when>
				<xsl:otherwise>
					<core:yearMonth rdf:datatype="http://www.w3.org/2001/XMLSchema#gYearMonth">
						<xsl:value-of select="substring(originInfo/dateIssued, 1, 4)"/>-<xsl:copy-of select="substring(originInfo/dateIssued, 6, 2)" />
					</core:yearMonth>
				</xsl:otherwise>
			</xsl:choose>

			<xsl:apply-templates select="name" />

<!--			<core:hasPublicationVenue> //journal-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsJournal/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:hasPublicationVenue>-->
		</rdf:description>
	</xsl:template>

	<xsl:template match="name">
		<xsl:variable name="modsId" select="../@ID" />
		<core:informationResource>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthorship/modsId_', $modsId)" /></xsl:attribute>
		</core:informationResource>
	</xsl:template>


	<xsl:template match="mods/name">
		<xsl:variable name="modsId" select="../../@ID" />
		<xsl:variable name="firstName" select="namePart[@type='given']" />
		<xsl:variable name="lastName" select="namePart[@type='family']" />
		<xsl:variable name="allFirstNames" select="string-join($firstName, ' ')" />

 		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthorship/modsId_', $modsId)" /></xsl:attribute>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
			<core:linkedInformationResource>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			</core:linkedInformationResource>
			<core:linkedAuthor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthor/modsId_', $modsId)" /></xsl:attribute>
			</core:linkedAuthor>
		</rdf:description>
		
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthor/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<foaf:firstName><xsl:value-of select="$firstName" /></foaf:firstName>
			<foaf:lastName><xsl:value-of select="$lastName" /></foaf:lastName>
 			<core:authorInAuthorship>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthorship/modsId_', $modsId)" /></xsl:attribute>
 			</core:authorInAuthorship>
		</rdf:description>
	</xsl:template>



<!--	<xsl:template match="mods/originInfo/place">-->
<!--		<xsl:variable name="modsId" select="../../@ID" />-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPubVenue/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<core:placeOfPublication><xsl:value-of select="placeTerm" />-->
<!--			</core:placeOfPublication>-->
<!--			<core:publicationVenueFor>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:publicationVenueFor>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!---->
<!--	<xsl:template match="mods/originInfo">-->
<!--		<xsl:variable name="modsId" select="../@ID" />-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPublisher/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<attribution><xsl:value-of select="publisher" /></attribution>-->
<!--			<core:publisherOf>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:publisherOf>-->
<!--		</rdf:description>-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPubDate/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<core:date><xsl:value-of select="dateIssued" /></core:date>-->
<!--			<core:timeIntervalFor>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:timeIntervalFor>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!--	-->
</xsl:stylesheet>
