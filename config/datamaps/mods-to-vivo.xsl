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

<!--
KNOWN ISSUE: relatedItem can be nested recursively.  Also, they can be of different genres such as Book.  Current MODS mapping calls for assuming only
             one (non-nested) relatedItem and assuming it is always a Journal.  That is how this is currently implemented.  Need to talk with Nick about
             this issue. 
-->


<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
	xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/mods/</xsl:variable>

	<xsl:template match="/modsCollection">
		<rdf:RDF>
			<xsl:apply-templates select="mods" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="mods">
		<xsl:variable name="modsId" select="encode-for-uri(@ID)" />
		<xsl:variable name="isbn" select="identifier[@type='isbn']" />
		<xsl:variable name="tokenizedTopics" select="tokenize(subject/topic, ',')" />

		<xsl:variable name="title">
			<xsl:call-template name="titleVariable" />
		</xsl:variable>

		<xsl:if test="typeOfResource='text'">
			<rdf:description>
				<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
				<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
				<rdfs:label><xsl:value-of select="$title" /></rdfs:label>

				<xsl:choose>
					<xsl:when test="originInfo/issuance='monographic'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:otherwise>
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
							<xsl:otherwise>
								<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>

				<core:dateTimeValue><xsl:value-of select="originInfo/dateIssued"/></core:dateTimeValue>
				<core:supplementalInformation><xsl:value-of select="note" /></core:supplementalInformation>
				<xsl:if test="string-length(replace($isbn, '-', '')) = 10">
					<bibo:isbn10><xsl:value-of select="$isbn" /></bibo:isbn10>
				</xsl:if>
				<xsl:if test="string-length($isbn) != 10">
					<bibo:isbn13><xsl:value-of select="$isbn" /></bibo:isbn13>
				</xsl:if>
				<ufVivo:language><xsl:value-of select="language" /></ufVivo:language>

				<xsl:for-each select="$tokenizedTopics">
					<core:freetextKeyword><xsl:value-of select="normalize-space(.)" /></core:freetextKeyword>
				</xsl:for-each>

				<bibo:volume><xsl:value-of select="part/detail[@type='volume']/number" /></bibo:volume>
				<bibo:issue><xsl:value-of select="part/detail[@type='number']/number" /></bibo:issue>
				<bibo:pageStart><xsl:value-of select="extent[@unit='page']/start" /></bibo:pageStart>
				<bibo:pageEnd><xsl:value-of select="extent[@unit='page']/end" /></bibo:pageEnd>
				<bibo:abstract><xsl:value-of select="abstract"></xsl:value-of></bibo:abstract>

				<core:publisher><xsl:value-of select="originInfo/publisher" /></core:publisher>
				<core:placeOfPublication><xsl:value-of select="originInfo/place/placeTerm" /></core:placeOfPublication>

				<xsl:apply-templates select="name" mode="withinPub">
					<xsl:with-param name="modsId" select="$modsId" />
				</xsl:apply-templates>
				<xsl:apply-templates select="originInfo/publisher" mode="withinPub">
					<xsl:with-param name="modsId" select="$modsId" />
				</xsl:apply-templates>
				<xsl:apply-templates select="originInfo/place/placeTerm" mode="withinPub">
					<xsl:with-param name="modsId" select="$modsId" />
				</xsl:apply-templates>
				<xsl:apply-templates select="relatedItem[@type='host']" mode="withinPub">
					<xsl:with-param name="modsId" select="$modsId" />
				</xsl:apply-templates>
				<xsl:apply-templates select="location/url" mode="withinPub">
					<xsl:with-param name="modsId" select="$modsId" />
				</xsl:apply-templates>
			</rdf:description>

			<xsl:apply-templates select="name" mode="standAlone">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
			<xsl:apply-templates select="originInfo/publisher" mode="standAlone">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
			<xsl:apply-templates select="originInfo/place/placeTerm" mode="standAlone">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
			<xsl:apply-templates select="relatedItem[@type='host']" mode="standAlone">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
			<xsl:apply-templates select="location/url" mode="standAlone">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>


	<xsl:template match="location/url" mode="withinPub">
		<xsl:param name='modsId' />

		<vitro:primaryLink>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'hyperlink/', $modsId)" /></xsl:attribute>
		</vitro:primaryLink>
	</xsl:template>
	
	
	<xsl:template match="location/url" mode="standAlone">
		<xsl:param name='modsId' />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'hyperlink/', $modsId)" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Link"></rdf:type>
			<rdfs:label><xsl:value-of select="." /></rdfs:label>
			<vitro:linkAnchor>Link</vitro:linkAnchor>
			<vitro:linkURL><xsl:value-of select="." /></vitro:linkURL>
		</rdf:description>
	</xsl:template>



	<xsl:template name="titleVariable">
		<xsl:if test="normalize-space(titleInfo[not(@type='abbreviated')]/title)=''"> 
			<xsl:value-of select="'(untitled)'" />
		</xsl:if>
		<xsl:if test="normalize-space(titleInfo[not(@type='abbreviated')]/title)!=''"> 
			<xsl:value-of select="concat(titleInfo[not(@type='abbreviated')]/title, ' ', titleInfo/subTitle)" />
		</xsl:if>
	</xsl:template>


	<xsl:template match="relatedItem[@type='host']" mode="withinPub">
		<xsl:param name='modsId' />

		<xsl:variable name="title">
			<xsl:call-template name="titleVariable" />
		</xsl:variable>

		<xsl:variable name="label" select="$title" />
		<xsl:variable name="uriLabel" select="encode-for-uri($label)" />

		<core:hasPublicationVenue>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'journal/', $uriLabel)" /></xsl:attribute>
		</core:hasPublicationVenue>
	</xsl:template>

	<xsl:template match="relatedItem[@type='host']" mode="standAlone">
		<xsl:param name='modsId' />

		<xsl:variable name="title">
			<xsl:call-template name="titleVariable" />
		</xsl:variable>

		<xsl:variable name="label" select="$title" />
		<xsl:variable name="uriLabel" select="encode-for-uri($label)" />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'journal/', $uriLabel)" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal"></rdf:type>
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:dateTimeValue><xsl:value-of select="originInfo/dateIssued"/></core:dateTimeValue>
			<core:publicationVenueFor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
			</core:publicationVenueFor>
			
			<xsl:apply-templates select="originInfo/publisher" mode="withinJournal">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
			<xsl:apply-templates select="originInfo/place/placeTerm" mode="withinJournal">
				<xsl:with-param name="modsId" select="$modsId" />
			</xsl:apply-templates>
		</rdf:description>

		<xsl:apply-templates select="originInfo/publisher" mode="standAloneForJournal">
			<xsl:with-param name="modsId" select="$modsId" />
			<xsl:with-param name="journalUriLabel" select="$uriLabel" />
		</xsl:apply-templates>
		<xsl:apply-templates select="originInfo/place/placeTerm" mode="standAloneForJournal">
			<xsl:with-param name="modsId" select="$modsId" />
			<xsl:with-param name="journalUriLabel" select="$uriLabel" />
		</xsl:apply-templates>

	</xsl:template>


	<xsl:template match="originInfo/place/placeTerm" mode="withinJournal">
		<xsl:param name='modsId' />

		<xsl:variable name="label" select="." />

		<core:hasGeographicLocation>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'geo/', encode-for-uri($label))" /></xsl:attribute>
		</core:hasGeographicLocation>
	</xsl:template>

	<xsl:template match="originInfo/place/placeTerm" mode="standAloneForJournal">
		<xsl:param name='modsId' />
		<xsl:param name='journalUriLabel' />

		<xsl:variable name="label" select="." />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'geo/', encode-for-uri($label))" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#GeographicLocation"></rdf:type>
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:geographicLocationOf>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'journal/', $journalUriLabel)" /></xsl:attribute>
			</core:geographicLocationOf>
		</rdf:description>
	</xsl:template>

	<xsl:template match="originInfo/publisher" mode="withinJournal">
		<xsl:param name='modsId' />

		<xsl:variable name="label" select="." />

		<core:publisher>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri($label))" /></xsl:attribute>
		</core:publisher>
	</xsl:template>

	<xsl:template match="originInfo/publisher" mode="standAloneForJournal">
		<xsl:param name='modsId' />
		<xsl:param name='journalUriLabel' />

		<xsl:variable name="label" select="." />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri($label))" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:publisherOf>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'journal/', $journalUriLabel)" /></xsl:attribute>
			</core:publisherOf>
		</rdf:description>

	</xsl:template>




	<xsl:template match="originInfo/place/placeTerm" mode="withinPub">
		<xsl:param name='modsId' />

		<xsl:variable name="label" select="." />

		<core:hasGeographicLocation>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'geo/', encode-for-uri($label))" /></xsl:attribute>
		</core:hasGeographicLocation>
	</xsl:template>

	<xsl:template match="originInfo/place/placeTerm" mode="standAlone">
		<xsl:param name='modsId' />

		<xsl:variable name="label" select="." />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'geo/', encode-for-uri($label))" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#GeographicLocation"></rdf:type>
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:geographicLocationOf>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
			</core:geographicLocationOf>
		</rdf:description>
	</xsl:template>

	<xsl:template match="originInfo/publisher" mode="withinPub">
		<xsl:param name='modsId' />

		<xsl:variable name="label" select="." />

		<core:publisher>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri($label))" /></xsl:attribute>
		</core:publisher>
	</xsl:template>

	<xsl:template match="originInfo/publisher" mode="standAlone">
		<xsl:param name='modsId' />

		<xsl:variable name="label" select="." />

		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri($label))" /></xsl:attribute>
			<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:publisherOf>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
			</core:publisherOf>
		</rdf:description>

	</xsl:template>


	<xsl:template match="name" mode="withinPub">
		<xsl:param name='modsId' />

		<xsl:variable name="type" select="@type" />
		<xsl:variable name="role" select="role/roleTerm" />
		<xsl:variable name="firstName" select="namePart[@type='given']" />
		<xsl:variable name="lastName" select="namePart[@type='family']" />
		<xsl:variable name="allFirstNames" select="string-join($firstName, ' ')" />


		<xsl:if test="$role='author'">
			<xsl:if test="$type='personal'">
				<core:informationResourceInAuthorship>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', encode-for-uri(concat($allFirstNames, '_', $lastName)))" /></xsl:attribute>
				</core:informationResourceInAuthorship>
			</xsl:if>
			<xsl:if test="$type='corporate'">
				<core:informationResourceInAuthorship>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri(namePart))" /></xsl:attribute>
				</core:informationResourceInAuthorship>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$role='editor'">
			<bibo:editor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', encode-for-uri(concat($allFirstNames, '_', $lastName)))" /></xsl:attribute>
			</bibo:editor>
		</xsl:if>
	</xsl:template>


	<xsl:template match="mods/name" mode="standAlone">
		<xsl:param name='modsId' />

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
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', encode-for-uri(concat($allFirstNames, '_', $lastName)))" /></xsl:attribute>
				</xsl:if>
				<xsl:if test="$type='corporate'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', encode-for-uri(namePart))" /></xsl:attribute>
				</xsl:if>
				<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
				<core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></core:authorRank>			
				<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
				<core:linkedInformationResource>
					<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'pub/modsId_', $modsId)" /></xsl:attribute>
				</core:linkedInformationResource>
				<core:linkedAuthor>
					<xsl:if test="$type='personal'">
						<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', encode-for-uri(concat($allFirstNames, '_', $lastName)))" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="$type='corporate'">
						<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri(namePart))" /></xsl:attribute>
					</xsl:if>
				</core:linkedAuthor>
			</rdf:description>
		</xsl:if>

		<xsl:if test="$role='author' or $role='editor'">
			<rdf:description>
				<xsl:if test="$type='personal'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'author/modsId_', $modsId, '_', encode-for-uri(concat($allFirstNames, '_', $lastName)))" /></xsl:attribute>
					<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/excludeEntity" />
					<foaf:firstName><xsl:value-of select="$firstName" /></foaf:firstName>
					<foaf:lastName><xsl:value-of select="$lastName" /></foaf:lastName>
				</xsl:if>
				<xsl:if test="$type='corporate'">
					<xsl:attribute name="rdf:about"><xsl:value-of select="concat($baseURI, 'org/modsId_', $modsId, '_', encode-for-uri($label))" /></xsl:attribute>
					<ufVivo:harvestedBy>MODS RefWorks harvest</ufVivo:harvestedBy>
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
				</xsl:if>
				<rdfs:label><xsl:value-of select="$label" /></rdfs:label>

				<xsl:if test="$role='author'">
		 			<core:authorInAuthorship>
						<xsl:if test="$type='personal'">
							<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', encode-for-uri(concat($allFirstNames, '_', $lastName)))" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="$type='corporate'">
							<xsl:attribute name="rdf:resource"><xsl:value-of select="concat($baseURI, 'authorship/modsId_', $modsId, '_', encode-for-uri(namePart))" /></xsl:attribute>
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
