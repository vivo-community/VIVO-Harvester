
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
			<xsl:apply-templates select="mods/titleInfo" />
			<xsl:apply-templates select="mods/name" />
			<xsl:apply-templates select="mods/originInfo/place" />
			<xsl:apply-templates select="mods/originInfo" />
			<xsl:apply-templates select="mods/note" />
			<xsl:apply-templates select="mods/subject" />
			<xsl:apply-templates select="mods/physicalDescription" />
			<xsl:apply-templates select="mods/language" />
			<xsl:apply-templates select="mods/classification" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="mods/titleInfo">
		<xsl:variable name="modsId" select="../@ID" />
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			<core:title><xsl:value-of select="concat(title, ' ', subTitle)" /></core:title>
			<core:informationResource>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthorship/modsId_', $modsId)" /></xsl:attribute>
			</core:informationResource>
			<core:hasPublicationVenue rdf:resource="orginInfo/place node">
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPubVenue/modsId_', $modsId)" /></xsl:attribute>
			</core:hasPublicationVenue>
			<core:publisher rdf:resource="originInfo publisher node">
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPublisher/modsId_', $modsId)" /></xsl:attribute>
			</core:publisher>
			<core:hasTimeInterval rdf:resource="originInfo date node">
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPubDate/modsId_', $modsId)" /></xsl:attribute>
			</core:hasTimeInterval>
<!--			<core:hasSubjectArea rdf:resource="subject area node">-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsSubject/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:hasSubjectArea>-->
		</rdf:description>
	</xsl:template>

	<xsl:template match="mods/name">
		<xsl:variable name="modsId" select="../@ID" />
 		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthorship/modsId_', $modsId)" /></xsl:attribute>
			<core:authorNameAsListed><xsl:value-of select="namePart" /></core:authorNameAsListed>
			<core:linkedInformationResource>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			</core:linkedInformationResource>
			<core:linkedAuthor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthor/modsId_', $modsId)" /></xsl:attribute>
			</core:linkedAuthor>
		</rdf:description>
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthor/modsId_', $modsId)" /></xsl:attribute>
			<foaf:firstName><xsl:value-of select="namePart[@type='given']" /></foaf:firstName>
			<foaf:lastName><xsl:value-of select="namePart[@type='family']" /></foaf:lastName>
<!--			<xsl:analyze-string select="namePart" regex="(.*),(.*)">-->
<!--				<xsl:matching-substring>-->
<!--					<foaf:firstName><xsl:value-of select="regex-group(2)" /></foaf:firstName>-->
<!--					<foaf:lastName><xsl:value-of select="regex-group(1)" /></foaf:lastName>-->
<!--				</xsl:matching-substring>-->
<!--			</xsl:analyze-string>-->
			
 			<core:authorInAuthorship>
				<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsAuthorship/modsId_', $modsId)" /></xsl:attribute>
 			</core:authorInAuthorship>
		</rdf:description>
	</xsl:template>

	<xsl:template match="mods/originInfo/place">
		<xsl:variable name="modsId" select="../../@ID" />
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPubVenue/modsId_', $modsId)" /></xsl:attribute>
			<core:placeOfPublication><xsl:value-of select="placeTerm" />
			</core:placeOfPublication>
			<core:publicationVenueFor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			</core:publicationVenueFor>
		</rdf:description>
	</xsl:template>

	<xsl:template match="mods/originInfo">
		<xsl:variable name="modsId" select="../@ID" />
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPublisher/modsId_', $modsId)" /></xsl:attribute>
			<attribution><xsl:value-of select="publisher" /></attribution>
			<core:publisherOf>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			</core:publisherOf>
		</rdf:description>
		<rdf:description>
			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPubDate/modsId_', $modsId)" /></xsl:attribute>
			<core:date><xsl:value-of select="dateIssued" /></core:date>
			<core:timeIntervalFor>
				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>
			</core:timeIntervalFor>
		</rdf:description>
	</xsl:template>
<!---->
<!--	<xsl:template match="mods/subject">-->
<!--		<xsl:variable name="modsId" select="../@ID" />-->
<!--		<rdf:description>-->
<!--			<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsSubject/modsId_', $modsId)" /></xsl:attribute>-->
<!--			<core:description><xsl:value-of select="concat('topic: ', topic)" /></core:description>-->
<!--			<core:description><xsl:value-of select="concat('geographic: ', geographic)" /></core:description>-->
<!--			<core:description><xsl:value-of select="concat('temporal: ', temporal)" /></core:description>-->
<!--			<core:subjectAreaFor>-->
<!--				<xsl:attribute name="rdf:resource"><xsl:value-of select="concat('http://vivoweb.org/harvest/modsPub/modsId_', $modsId)" /></xsl:attribute>-->
<!--			</core:subjectAreaFor>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!---->
<!--	<xsl:template match="mods/note">-->
<!--		<rdf:description>-->
<!--			<core:supplementalInformation><xsl:value-of select="." /></core:supplementalInformation>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!---->
<!--	<xsl:template match="mods/physicalDescription">-->
<!--		<rdf:description>-->
<!--			<core:description><xsl:value-of select="concat('form: ', form, ', extent: ', extent)" /></core:description>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!---->
<!--	<xsl:template match="mods/language">-->
<!--		<rdf:description>-->
<!--			<core:description><xsl:value-of select="languageTerm" /></core:description>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->
<!---->
<!--	<xsl:template match="mods/classification">-->
<!--		<rdf:description>-->
<!--			<bibo:number><xsl:value-of select="." /></bibo:number>-->
<!--		</rdf:description>-->
<!--	</xsl:template>-->

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


