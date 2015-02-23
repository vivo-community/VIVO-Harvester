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
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'
	xmlns:ns2='http://woksearchlite.cxf.wokmws.thomsonreuters.com'
	xmlns:core='http://vivoweb.org/ontology/core#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
    xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/wos/</xsl:variable>

	<!-- The root of the XML returned by WOS.  This serves as the header of the RDF file produced. -->
	<xsl:template match="/Description">
		<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
		         xmlns:core='http://vivoweb.org/ontology/core#'
		         xmlns:foaf="http://xmlns.com/foaf/0.1/"
		         xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
		         xmlns:bibo='http://purl.org/ontology/bibo/'
    			 xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'>
			<xsl:apply-templates select="records" />
			<xsl:apply-templates select="map[@name='WOS']"/>
		</rdf:RDF>
	</xsl:template>

	<!-- Each publication record is contained in a "records" node -->
	<xsl:template match="records">
		<rdf:Description rdf:about="{$baseURI}pub/wosid{uid}">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
			<ufVivo:thomsonReutersUID><xsl:value-of select="uid" /></ufVivo:thomsonReutersUID>
			<rdfs:label><xsl:value-of select="title/value" /></rdfs:label>
			<bibo:volume><xsl:value-of select="source[label='Volume']/value" /></bibo:volume>
			<bibo:issue><xsl:value-of select="source[label='Issue']/value" /></bibo:issue>
			<bibo:pageStart><xsl:value-of select="substring-before(source[label='Pages']/value, '-')" /></bibo:pageStart>
			<bibo:pageEnd><xsl:value-of select="substring-after(source[label='Pages']/value, '-')" /></bibo:pageEnd>
			
			<!-- Authors must be handled both within the publication node (to link to the Authorship
			     node), and outside the publication node (to create the Author and Authorship nodes).
			     This is handled by selecting "authors/values" with modes "withinPub" and "standAlone",
			     respectively.
			     
			     Similar mechanics work for journals and keywords.
			-->
			<xsl:apply-templates select="authors/value" mode="withinPub"/>
			<xsl:apply-templates select="source[label='SourceTitle']/value" mode="withinPub"/>
			<xsl:apply-templates select="keywords/value" mode="withinPub"/>
		</rdf:Description>

		<xsl:apply-templates select="authors/value" mode="standAlone" />
		<xsl:apply-templates select="source[label='SourceTitle']/value" mode="standAlone" />
		<xsl:apply-templates select="keywords/value" mode="standAlone" />
	</xsl:template>
	
	<!-- Match authors and place resulting XML outside of the publication node. -->
	<xsl:template match="authors/value" mode="standAlone">
		
		<!-- Sample output from WOS: <values>Elinder, CG</values> -->
		<xsl:variable name="label" select="." />
		<xsl:variable name="firstName" select="substring(substring-after(., ', '), 1, 1)" />
		<xsl:variable name="middleName" select="substring(substring-after(., ', '), 2, 1)" />
		<xsl:variable name="lastName" select="substring-before(., ', ')" />

		<!-- Create the Author node -->
		<rdf:Description rdf:about="{$baseURI}author/wosid{ancestor::records/uid}author{position()}">
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<foaf:firstName><xsl:value-of select="$firstName" /></foaf:firstName>
			<core:middleName><xsl:value-of select="$middleName" /></core:middleName>
			<foaf:lastName><xsl:value-of select="$lastName" /></foaf:lastName>
			<core:relatedBy rdf:resource="{$baseURI}authorship/wosid{ancestor::records/uid}authorship{position()}" />
		</rdf:Description>
		
		<!-- Create the Authorship node -->
		<rdf:Description rdf:about="{$baseURI}authorship/wosid{ancestor::records/uid}authorship{position()}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:authorRank><xsl:value-of select="position()" /></core:authorRank>
			<core:relates rdf:resource="{$baseURI}author/wosid{ancestor::records/uid}author{position()}" />
			<core:relates rdf:resource="{$baseURI}pub/wosid{ancestor::records/uid}" />
		</rdf:Description>
		
	</xsl:template>

	
	<!-- Match authors and place resulting XML inside the publication node. -->
	<xsl:template match="authors/value" mode="withinPub">
		<core:relatedBy rdf:resource="{$baseURI}authorship/wosid{ancestor::records/uid}authorship{position()}" />
	</xsl:template>


	<!-- Match keywords and place resulting XML outside of the publication node. -->
	<xsl:template match="keywords/value" mode="standAlone">
		
		<xsl:variable name="label" select="." />

		<!-- Create the Subject Area node -->
		<rdf:Description rdf:about="{$baseURI}subjectarea/wosid{ancestor::records/uid}subjectarea{position()}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#SubjectArea" />
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:subjectAreaFor rdf:resource="{$baseURI}pub/wosid{ancestor::records/uid}" />
		</rdf:Description>
		
	</xsl:template>

	
	<!-- Match keywords and place resulting XML inside the publication node. -->
	<xsl:template match="keywords/value" mode="withinPub">
		<core:hasSubjectArea rdf:resource="{$baseURI}subjectarea/wosid{ancestor::records/uid}subjectarea{position()}" />
	</xsl:template>
	



	<!-- Match journals and place resulting XML outside of the publication node. -->
	<xsl:template match="source[label='SourceTitle']/value" mode="standAlone">
		
		<xsl:variable name="label" select="." />

		<!-- Create the Journal node -->
		<rdf:Description rdf:about="{$baseURI}journal/wosid{ancestor::records/uid}journal{position()}">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal" />
			<rdfs:label><xsl:value-of select="$label" /></rdfs:label>
			<core:publicationVenueFor rdf:resource="{$baseURI}pub/wosid{ancestor::records/uid}" />
		</rdf:Description>
		
	</xsl:template>

	
	<!-- Match journals and place resulting XML inside the publication node. -->
	<xsl:template match="source[label='SourceTitle']/value" mode="withinPub">
		<core:hasPublicationVenue rdf:resource="{$baseURI}journal/wosid{ancestor::records/uid}journal{position()}" />
	</xsl:template>
	
	
		<!-- Each publication record is contained in a map name="WOS" node -->
	<xsl:template match="map[@name='WOS']">
		<rdf:Description rdf:about="{$baseURI}pub/wosid{val[@name='uid']}">
			<xsl:if test="val[@name='doi']">
				<bibo:doi><xsl:value-of select="val[@name='doi']"/></bibo:doi>
			</xsl:if>
			<xsl:if test="val[@name='pmid']">
				<bibo:pmid><xsl:value-of select="val[@name='pmid']"/></bibo:pmid>
			</xsl:if>
			<xsl:if test="val[@name='relatedRecordsURL']">
				<core:webpage>
					<rdf:Description rdf:about="{$baseURI}webpage/relatedURLforwosid{val[@name='uid']}">
						<core:linkAnchorText>Related Records Page</core:linkAnchorText>
						<core:linkURI><xsl:value-of select="val[@name='relatedRecordsURL']"/></core:linkURI>
						<core:webpageOf rdf:resource="{$baseURI}pub/wosid{val[@name='uid']}" />
					</rdf:Description>
				</core:webpage>
			</xsl:if>
			
			<xsl:if test="val[@name='sourceURL']">
				<core:webpage>
					<rdf:Description rdf:about="{$baseURI}webpage/sourceURLforwosid{val[@name='uid']}">
						<core:linkAnchorText>Source Page</core:linkAnchorText>
						<core:linkURI><xsl:value-of select="val[@name='sourceURL']"/></core:linkURI>
						<core:webpageOf rdf:resource="{$baseURI}pub/wosid{val[@name='uid']}" />
					</rdf:Description>
				</core:webpage>
			</xsl:if>
			
			<xsl:if test="val[@name='citingArticlesURL']">
				<core:webpage>
					<rdf:Description rdf:about="{$baseURI}webpage/citingURLforwosid{val[@name='uid']}">
						<core:linkAnchorText>Citing Page</core:linkAnchorText>
						<core:linkURI><xsl:value-of select="val[@name='citingArticlesURL']"/></core:linkURI>
						<core:webpageOf rdf:resource="{$baseURI}pub/wosid{val[@name='uid']}" />
					</rdf:Description>
				</core:webpage>
			</xsl:if>
		</rdf:Description>
	</xsl:template>


</xsl:stylesheet>
	