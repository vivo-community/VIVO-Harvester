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
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/mods/</xsl:variable>
	<xsl:variable name="isbn" select="identifier[@type='isbn']" />

	<xsl:template match="/modsCollection">
		<rdf:RDF>
			<xsl:apply-templates select="mods" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="mods">
		<xsl:variable name="modsId" select="@ID" />
		
		<xsl:if test="typeOfResource='text'">
			<rdf:description>
				<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
				<ufVivo:harvestedBy>MODS-Harvester</ufVivo:harvestedBy>
				<rdfs:label><xsl:value-of select="concat(titleInfo/title, ' ', titleInfo/subTitle)" /></rdfs:label>
	
				<xsl:if test="originInfo/issuance='monographic'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
				</xsl:if>
				<xsl:if test="originInfo/issuance!='monographic'">
					<xsl:choose>
						<xsl:when test="genre='book'" >
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						</xsl:when>
						<xsl:when test="genre='periodical'" >
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						</xsl:when>
						<xsl:when test="genre='academic journal'" >
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
						</xsl:when>
						<xsl:when test="genre='conference publication'" >
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
						</xsl:when>
					</xsl:choose>
				</xsl:if>
	
				<core:dateTimeValue><xsl:value-of select="originInfo/dateIssued"/></core:dateTimeValue>
				<core:supplementalInformation><xsl:value-of select="note" /></core:supplementalInformation>
				<xsl:if test="string-length(replace($isbn, '-', '')) = 10">
					<bibo:isbn10><xsl:value-of select="$isbn" /></bibo:isbn10>
				</xsl:if>
				<xsl:if test="string-length($isbn) != 10">
					<bibo:isbn13><xsl:value-of select="$isbn" /></bibo:isbn13>
				</xsl:if>
				<ufVivo:language><xsl:value-of select="language" /></ufVivo:language>
				<core:freetextKeyword><xsl:value-of select="subject/topic" /></core:freetextKeyword>
				<bibo:volume><xsl:value-of select="part/detail[@type='volume']/number" /></bibo:volume>
				<bibo:issue><xsl:value-of select="part/detail[@type='number']/number" /></bibo:issue>
				<bibo:pageStart><xsl:value-of select="extent[@unit='page']/start" /></bibo:pageStart>
				<bibo:pageEnd><xsl:value-of select="extent[@unit='page']/end" /></bibo:pageEnd>
				<bibo:abstract><xsl:value-of select="abstract"></xsl:value-of></bibo:abstract>
	
				<!-- 
				*foaf:organization linked via core:publisher = publisher
				  if publisher has placeTerm, use to match core:hasGeographicLocation on core:publisher
	
				core:hasPublicationVenue linked to bibo:journal = relatedItem w/ type=host -> titleInfo->title
				-->
	
				<core:publisher><xsl:value-of select="originInfo/publisher" /></core:publisher>
				<core:placeOfPublication><xsl:value-of select="originInfo/place/placeTerm" /></core:placeOfPublication>
	
				<xsl:apply-templates select="name" mode="withinPub" />
				<xsl:apply-templates select="originInfo/publisher" mode="withinPub" />
			</rdf:description>
	
			<xsl:apply-templates select="name" mode="standAlone" />
			<xsl:apply-templates select="originInfo/publisher" mode="standAlone" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="originInfo/publisher" mode="withinPub">
		<xsl:variable name="modsId" select="../../@ID" />
		<xsl:variable name="label" select="." />

		<core:publisher>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'journal/modsId_', $modsId, '_', $label)" /></xsl:attribute>
		</core:publisher>
	</xsl:template>

	<xsl:template match="originInfo/publisher" mode="standAlone">
		<xsl:variable name="modsId" select="../../@ID" />
		<xsl:variable name="label" select="." />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'journal/modsId_', $modsId, '_', $label)" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
			<rdf:label><xsl:value-of select="$label" /></rdf:label>
			<core:publisherOf>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
			</core:publisherOf>
		</rdf:description>

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
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'journal/modsId_', $modsId, '_', namePart)" /></xsl:attribute>
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
				<xsl:if test="$type='personal'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
				</xsl:if>
				<xsl:if test="$type='corporate'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', namePart)" /></xsl:attribute>
				</xsl:if>
				<ufVivo:harvestedBy>MODS-Harvester</ufVivo:harvestedBy>
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
				<core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></core:authorRank>			
				<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
				<core:linkedInformationResource>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
				</core:linkedInformationResource>
				<core:linkedAuthor>
					<xsl:if test="$type='personal'">
						<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="$type='corporate'">
						<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'journal/modsId_', $modsId, '_', namePart)" /></xsl:attribute>
					</xsl:if>
				</core:linkedAuthor>
			</rdf:description>
		</xsl:if>

		<xsl:if test="$role='author' or $role='editor'">
			<rdf:description>
				<xsl:if test="$type='personal'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
					<ufVivo:harvestedBy>MODS-Harvester</ufVivo:harvestedBy>
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
					<foaf:firstName><xsl:value-of select="$firstName" /></foaf:firstName>
					<foaf:lastName><xsl:value-of select="$lastName" /></foaf:lastName>
				</xsl:if>
				<xsl:if test="$type='corporate'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'journal/modsId_', $modsId, '_', $label)" /></xsl:attribute>
					<ufVivo:harvestedBy>MODS-Harvester</ufVivo:harvestedBy>
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
				</xsl:if>
				<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
	
				<xsl:if test="$role='author'">
		 			<core:authorInAuthorship>
						<xsl:if test="$type='personal'">
							<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', $allFirstNames, '_', $lastName)" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="$type='corporate'">
							<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', namePart)" /></xsl:attribute>
						</xsl:if>
		 			</core:authorInAuthorship>
				</xsl:if>
				<xsl:if test="$role='editor'">
					<core:editorOf>
						<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
					</core:editorOf>
				</xsl:if>
			</rdf:description>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
