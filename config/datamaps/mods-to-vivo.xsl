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
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/mods/</xsl:variable>

	<xsl:template match="/modsCollection">
		<rdf:RDF>
			<xsl:apply-templates select="mods" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="mods">
		<xsl:variable name="modsId" select="@ID" />
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
			<rdfs:label><xsl:value-of select="concat(titleInfo/title, ' ', titleInfo/subTitle)" /></rdfs:label>
			<core:title><xsl:value-of select="concat(titleInfo/title, ' ', titleInfo/subTitle)" /></core:title>
			<bibo:abstract><xsl:value-of select="abstract"></xsl:value-of></bibo:abstract>
			<bibo:volume><xsl:value-of select="part/detail[@type='volume']"/></bibo:volume>
			<bibo:identity><xsl:value-of select="$modsId"></xsl:value-of></bibo:identity>
			<bibo:isbn-13><xsl:value-of select="identifier[@type='isbn']"></xsl:value-of></bibo:isbn-13>
			<bibo:doi><xsl:value-of select="identifier[@type='doi']"></xsl:value-of></bibo:doi>
			<core:publisher><xsl:value-of select="originInfo/publisher" /></core:publisher>
			<core:placeOfPublication><xsl:value-of select="originInfo/place/placeTerm" /></core:placeOfPublication>

			<xsl:choose>
				<xsl:when test="contains(originInfo/dateIssued, '-')">
					<core:yearMonth rdf:datatype="http://www.w3.org/2001/XMLSchema#gYearMonth">
						<xsl:value-of select="substring(originInfo/dateIssued, 1, 4)"/>-<xsl:copy-of select="substring(originInfo/dateIssued, 6, 2)" />
					</core:yearMonth>
				</xsl:when>
				<xsl:otherwise>
					<core:year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="originInfo/dateIssued"/></core:year>
				</xsl:otherwise>
			</xsl:choose>

			<xsl:apply-templates select="name" mode="withinPub" />
			<xsl:apply-templates select="typeOfResource" />
		</rdf:description>

		<xsl:apply-templates select="name" mode="standAlone" />
	</xsl:template>

	<xsl:template match="name" mode="withinPub">
		<xsl:variable name="modsId" select="../@ID" />
		<xsl:variable name="type" select="@type" />
		<xsl:variable name="role" select="role/roleTerm" />
		<xsl:variable name="firstName" select="namePart[@type='given']" />
		<xsl:variable name="lastName" select="namePart[@type='family']" />
		<xsl:variable name="allFirstNames" select="string-join($firstName, ' ')" />

		<xsl:if test="$role='author'">
			<xsl:if test="$type='personal'">
				<core:informationResourceInAuthorship>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
				</core:informationResourceInAuthorship>
			</xsl:if>
			<xsl:if test="$type='corporate'">
				<core:informationResourceInAuthorship>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', namePart)" /></xsl:attribute>
				</core:informationResourceInAuthorship>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$role='editor'">
			<bibo:editor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
			</bibo:editor>
		</xsl:if>
	</xsl:template>


	<xsl:template match="mods/name" mode="standAlone">
		<xsl:variable name="modsId" select="../@ID" />
		<xsl:variable name="type" select="@type" />
		<xsl:variable name="role" select="role/roleTerm" />
		<xsl:variable name="firstName" select="namePart[@type='given']" />
		<xsl:variable name="lastName" select="namePart[@type='family']" />
		<xsl:variable name="allFirstNames" select="string-join($firstName, ' ')" />
		
		<xsl:variable name="label">
			<xsl:if test="$type='personal'">
				<xsl:value-of select="concat($lastName, ', ', $allFirstNames)" />
			</xsl:if>
			<xsl:if test="$type='corporate'">
				<xsl:value-of select="namePart" />
			</xsl:if>
		</xsl:variable>


		<xsl:if test="$role='author'">
	 		<rdf:description>
				<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
				<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
				<core:linkedInformationResource>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
				</core:linkedInformationResource>
				<core:linkedAuthor>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
				</core:linkedAuthor>
			</rdf:description>
		</xsl:if>

		<rdf:description>
			<xsl:if test="$type='personal'">
				<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
				<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
				<foaf:firstName><xsl:value-of select="$firstName" /></foaf:firstName>
				<foaf:lastName><xsl:value-of select="$lastName" /></foaf:lastName>
			</xsl:if>
			<xsl:if test="$type='corporate'">
				<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', $label)" /></xsl:attribute>
				<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
			</xsl:if>
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			
			<xsl:if test="$role='author'">
	 			<core:authorInAuthorship>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
	 			</core:authorInAuthorship>
			</xsl:if>
			<xsl:if test="$role='editor'">
				<core:editorOf>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
				</core:editorOf>
			</xsl:if>
		</rdf:description>
	</xsl:template>


	<xsl:template match="typeOfResource">
		<xsl:variable name="typeOfResource" select="." />
		<xsl:variable name="genre" select="../genre" />
		<xsl:choose>
					<xsl:when test="$typeOfResource='text'">
						<xsl:choose>
							<xsl:when test="$genre='article'">
								<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
							</xsl:when>
							<xsl:when test="$genre='book'">
								<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
							</xsl:when>
							<xsl:when test="$genre='conference publication'">
								<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
							</xsl:when>
							<xsl:when test="$genre='encyclopedia'">
								<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
							</xsl:when>
							<xsl:when test="$genre='patent'">
								<rdf:type rdf:resource="http://purl.org/ontology/bibo/Patent" />
							</xsl:when>
							<xsl:otherwise>
								<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="$typeOfResource='cartographic'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Map" />
					</xsl:when>
					<xsl:when test="$typeOfResource='notated music'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:when>
					<xsl:when test="$typeOfResource='sound recording'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioDocument" />
					</xsl:when>
					<xsl:when test="$typeOfResource='sound recording-musical'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioDocument" />
					</xsl:when>
					<xsl:when test="$typeOfResource='sound recording-nonmusical'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioDocument" />
					</xsl:when>
					<xsl:when test="$typeOfResource='still image'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="$typeOfResource='moving image'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioVisualDocument" />
					</xsl:when>
					<xsl:when test="$typeOfResource='three dimensional object'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:when>
					<xsl:when test="$typeOfResource='software, multimedia'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:when>
					<xsl:when test="$typeOfResource='mixed material'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:when>
					<xsl:otherwise>
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


<!--	<xsl:template match="mods/originInfo/place">-->
<!--		<xsl:variable name="modsId" select="../../@ID" />-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'PubVenue/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<core:placeOfPublication><xsl:value-of select="placeTerm" />-->
<!--			</core:placeOfPublication>-->
<!--			<core:publicationVenueFor>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'Pub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:publicationVenueFor>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!---->
<!--	<xsl:template match="mods/originInfo">-->
<!--		<xsl:variable name="modsId" select="../@ID" />-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'Publisher/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<attribution><xsl:value-of select="publisher" /></attribution>-->
<!--			<core:publisherOf>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'Pub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:publisherOf>-->
<!--		</rdf:description>-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'PubDate/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<core:date><xsl:value-of select="dateIssued" /></core:date>-->
<!--			<core:timeIntervalFor>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'Pub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:timeIntervalFor>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!--	-->
</xsl:stylesheet>
