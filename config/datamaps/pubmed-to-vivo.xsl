<!--
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
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
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>

	<!-- The main Article Set of all pubmed citations loaded 
		This serves as the header of the RDF file produced
	 -->
	<xsl:template match="/PubmedArticleSet">
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
			xmlns:xs='http://www.w3.org/2001/XMLSchema#'
			xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'>
			<xsl:apply-templates select="PubmedArticle" />			
		</rdf:RDF>
	</xsl:template>
	
	<!-- The Article -->
	<xsl:template match="PubmedArticle">
		<xsl:variable name="elValue" select="MedlineCitation/Article/Affiliation"/>
		<xsl:variable name="emailAddress">
			<!--
			Possible better email match regex.
			Needs tested. Not sure if &quot will work for "
			- -Dale
			-->
			<xsl:analyze-string select="$elValue" regex="\s*([a-zA-Z\d\.]*@[a-zA-Z\d\.]*)">
			<!-- <xsl:analyze-string select="$elValue" regex="(?:[a-z0-9!#$%&amp;'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&amp;'*+/=?^_`{|}~-]+)*|&quot;(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*&quot;)@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])">	-->
			<!--
			Here's the old expression
			\s*([a-zA-Z\d\.]*@[a-z\.]*)
			It was in quotes. Like the one above. 
			-->
				<xsl:matching-substring>
					<xsl:value-of select="regex-group(1)" />
				</xsl:matching-substring>			
			</xsl:analyze-string>
		</xsl:variable>
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedPub/pmid{child::MedlineCitation/PMID}">
			<xsl:apply-templates select='MedlineCitation/Article/PublicationTypeList/PublicationType' />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<bibo:pmid><xsl:value-of select="MedlineCitation/PMID" /></bibo:pmid>
			<rdfs:label><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></rdfs:label>
			<core:Title><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></core:Title>
			<score:Affiliation><xsl:value-of select="MedlineCitation/Article/Affiliation" /></score:Affiliation>
			<bibo:volume><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Volume"/></bibo:volume>
			<bibo:number><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Issue"/></bibo:number>
			<bibo:abstract><xsl:value-of select="MedlineCitation/Article/Abstract"/></bibo:abstract>
			<xsl:apply-templates select="MedlineCitation/ChemicalList/Chemical" />
			<xsl:apply-templates select="MedlineCitation/KeywordList/Keyword" />
			<xsl:apply-templates  select="MedlineCitation/MeshHeadingList/MeshHeading" mode="termAsKeyword" />
			<xsl:choose>
				<xsl:when test='string(PubmedData/ArticleIdList/ArticleId[@IdType="doi"])'>
					<bibo:doi><xsl:value-of select='PubmedData/ArticleIdList/ArticleId[@IdType="doi"]' /></bibo:doi>
				</xsl:when>
			</xsl:choose>
			<xsl:variable name="MonthNumber">
				<xsl:choose>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Jan">01</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Feb">02</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Mar">03</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Apr">04</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=May">05</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Jun">06</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Jul">07</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Aug">08</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Sep">09</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Oct">10</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Nov">11</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Dec">12</xsl:when>
				</xsl:choose>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
					<core:year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/></core:year>
				</xsl:when>
				<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
					<core:yearMonth rdf:datatype="http://www.w3.org/2001/XMLSchema#gYearMonth"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" /></core:yearMonth>
				</xsl:when>
				<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Day) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
					<core:date rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" />-<xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Day"/></core:date>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="MedlineCitation/Article/AuthorList/Author" mode="authorRef" />
			<xsl:apply-templates select="MedlineCitation/Article/Journal" mode="journalRef"/>
		</rdf:Description>
		<xsl:apply-templates select="MedlineCitation/Article/AuthorList" mode="fullAuthor">
			<xsl:with-param name="email" select="$emailAddress" />
		</xsl:apply-templates>
		<xsl:apply-templates select="MedlineCitation/Article/Journal" mode="fullJournal" />
	</xsl:template>

	<xsl:template match="MedlineCitation/Article/Journal" mode="journalRef">
		<core:hasPublicationVenue rdf:resource="http://vivoweb.org/harvest/pubmedJournal/journal{child::ISSN}" />
	</xsl:template>

<!-- The Main Journal Entity -->
	<xsl:template match="MedlineCitation/Article/Journal" mode="fullJournal">
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedJournal/journal{child::ISSN}" >
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<core:Title><xsl:value-of select="Title" /></core:Title>
			<rdfs:label><xsl:value-of select="Title" /></rdfs:label>
			<bibo:ISSN><xsl:value-of select="ISSN"/></bibo:ISSN>
			<core:publicationVenueFor rdf:resource="http://vivoweb.org/harvest/pubmedPub/pmid{ancestor::MedlineCitation/PMID}"/>
		</rdf:Description>	
	</xsl:template>	

	<!-- Links to From the Paper to the Terms and Authors -->
	<xsl:template match="MedlineCitation/Article/AuthorList/Author" mode="authorRef">
		<core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/harvest/pubmedAuthorship/pmid{ancestor::MedlineCitation/PMID}authorship{position()}" />
	</xsl:template>

	<!-- Author List Navigation --> 
	<xsl:template match="MedlineCitation/Article/AuthorList" mode="fullAuthor">
		<xsl:param name='email' />
		<xsl:apply-templates select="Author" mode="fullAuthor">
			<xsl:with-param name="email" select="$email" />
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- The Authors -->
	<xsl:template match="Author" mode="fullAuthor">
		<xsl:param name='email' />
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedAuthorship/pmid{ancestor::MedlineCitation/PMID}authorship{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DependentResource" />
			<core:linkedAuthor rdf:resource="http://vivoweb.org/harvest/pubmedAuthor/pmid{ancestor::MedlineCitation/PMID}author{position()}" />
			<core:linkedInformationResource rdf:resource="http://vivoweb.org/harvest/pubmedPub/pmid{ancestor::MedlineCitation/PMID}"/>
			<xsl:choose>
				<xsl:when test="string(ForeName)">
					<rdfs:label>Authorship for <xsl:value-of select="LastName" />, <xsl:value-of select="ForeName"/></rdfs:label>
				</xsl:when>
				<xsl:when test="string(LastName)">
					<rdfs:label>Authorship for <xsl:value-of select="LastName" /></rdfs:label>
				</xsl:when>
				<xsl:when test="string(CollectiveName)">
					<rdfs:label>Authorship for <xsl:value-of select="CollectiveName" /></rdfs:label>
				</xsl:when>
			</xsl:choose>
			<core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></core:authorRank>			
		</rdf:Description>
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedAuthor/pmid{ancestor::MedlineCitation/PMID}author{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<score:workEmail><xsl:value-of select="$email" /></score:workEmail>
			<xsl:choose>
				<xsl:when test="string(ForeName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdfs:label><xsl:value-of select="LastName" />, <xsl:value-of select="ForeName"/></rdfs:label>
					<foaf:lastName><xsl:value-of select="LastName" /></foaf:lastName>
					<score:foreName><xsl:value-of select="ForeName" /></score:foreName>
					<score:initials><xsl:value-of select="Initials" /></score:initials>
					<score:suffix><xsl:value-of select="Suffix" /></score:suffix>
					<!-- Parse out possible middle name -->
					<xsl:analyze-string select="string(ForeName)" regex="(.*) (.*)">
						<xsl:matching-substring>
							<foaf:firstName><xsl:value-of select="regex-group(1)" /></foaf:firstName>
							<core:middleName><xsl:value-of select="regex-group(2)" /></core:middleName>
						</xsl:matching-substring>
					</xsl:analyze-string>			
				</xsl:when>
				<xsl:when test="string(LastName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdfs:label><xsl:value-of select="LastName" /></rdfs:label>
					<foaf:lastName><xsl:value-of select="LastName" /></foaf:lastName>
					<score:foreName><xsl:value-of select="ForeName" /></score:foreName>
					<score:initials><xsl:value-of select="Initials" /></score:initials>
					<score:suffix><xsl:value-of select="Suffix" /></score:suffix>	
					<!-- Parse out possible middle name -->
					<xsl:analyze-string select="string(ForeName)" regex="(.*) (.*)">
					    <xsl:matching-substring>
							<foaf:firstName><xsl:value-of select="regex-group(1)" /></foaf:firstName>
							<core:middleName><xsl:value-of select="regex-group(2)" /></core:middleName>
						</xsl:matching-substring>
					</xsl:analyze-string>			
				</xsl:when>
				<xsl:when test="string(CollectiveName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
					<rdfs:label><xsl:value-of select="CollectiveName" /></rdfs:label>
				</xsl:when>
			</xsl:choose>
			<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<core:authorInAuthorship rdf:resource="http://vivoweb.org/harvest/pubmedAuthorship/pmid{ancestor::MedlineCitation/PMID}authorship{position()}" />
		</rdf:Description>
	</xsl:template>
	
	<xsl:template match="MedlineCitation/MeshHeadingList/MeshHeading" mode="termAsKeyword">
		<xsl:choose>
			<xsl:when test="string(DescriptorName)">
				<core:freetextKeyword><xsl:value-of select="string(DescriptorName)" /></core:freetextKeyword>  
			</xsl:when>
		</xsl:choose>	
	</xsl:template>	
	
	<!-- Chemical List -->
	<xsl:template match="MedlineCitation/ChemicalList/Chemical">
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance)">
				<core:freetextKeyword><xsl:value-of select="string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance)" /></core:freetextKeyword>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- Keyword -->
	<xsl:template match="MedlineCitation/KeywordList/Keyword">
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/KeywordList/Keyword)">
				<core:freetextKeyword><xsl:value-of select="string(MedlineCitation/KeywordList/Keyword)" /></core:freetextKeyword>  
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- Types -->
	<xsl:template match="MedlineCitation/Article/PublicationTypeList/PublicationType">
		<xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ '"/>
		<xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz '"/>
		<xsl:variable name="pbType" select="string(self::PublicationType)" />
		<xsl:choose>
					<xsl:when test="translate(string($pbType),$up,$lo)='addresses'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='atlases'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='bibliography'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='biobibliography'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='biography'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='book reviews'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='case reports'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#CaseStudy" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='charts'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='classical article'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='clinical conference'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase i'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase ii'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase iii'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase iv'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='collected correspondence'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='collected works'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>					<xsl:when test="translate(string($pbType),$up,$lo)='comment'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='comparative study'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='congresses'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='consensus development conference'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='consensus development conference, nih'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='controlled clinical trial'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='cookbooks'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='corrected and republished article'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='database'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Database" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='diaries'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='dictionary'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='directory'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='documentaries and factual films'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Film" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='drawings'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='duplicate publication'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='editorial'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#EditorialArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='encyclopedias'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='essays'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>			
					<xsl:when test="translate(string($pbType),$up,$lo)='evaluation studies'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Academic Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='festschrift'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='fictional work'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='formularies'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<!-- Government Publications -->
					<xsl:when test="translate(string($pbType),$up,$lo)='guideBooks'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='guideline'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Standard" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='handbooks'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='historical article'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='incunabula'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='indexes'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='instruction'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioVisualDocument" />
					</xsl:when>
					<!-- Interactive Tutorial -->
					<xsl:when test="translate(string($pbType),$up,$lo)='interview'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='introductory journal article'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='journal article'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<!-- Lectures -->
					<xsl:when test="translate(string($pbType),$up,$lo)='legal cases'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/LegalCaseDocument" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='legislation'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Legislation" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='maps'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Map" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='meeting abstracts'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ConferenceProceedings" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='meta-analysis'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='multicenter study'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='news'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='newspaper article'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='patents'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Patent" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='patient education handout'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='periodical index'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='periodicals'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Periodicals" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='pharmacopoeias'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='pictorial works'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='portraits'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='practice guideline'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Standard" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='randomized controlled trial'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='review'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='statistics'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='study characteristics'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='technical report'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Report" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='textbooks'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='twin study'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="translate(string($pbType),$up,$lo)='validation studies'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<!--  Webcasts -->
					<xsl:otherwise>
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:otherwise>
				</xsl:choose>
	</xsl:template>

</xsl:stylesheet>