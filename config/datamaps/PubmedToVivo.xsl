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
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedPub/pmid{child::MedlineCitation/PMID}">
				<xsl:choose>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType=Addresses]">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Atlases']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Bibliography']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Biobibliography']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Biography']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Book Reviews']">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Case Reports']">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#CaseStudy" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Charts']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Classical Article']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Clinical Conference']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Clinical Trial']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Clinical Trial, Phase I']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Clinical Trial, Phase II']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Clinical Trial, Phase III']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Clinical Trial, Phase IV']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Collected Correspondene']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Collected Works']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Comment']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Comparative Study']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Congresses']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Consensus Development Conference']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Consensus Development Conference, NIH']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Proceedings" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Controlled Clinical Trial']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Cookbooks']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Corrected and Republished Article']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Database']">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Database" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Diaries']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Dictionary']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Directory']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Documentaries and Factual Films']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Film" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Drawings']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Duplicate Publication']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Editorial']">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#EditorialArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Encyclopedias']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Essays']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>			
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Evaluation Studies']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Academic Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Festschrift']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Fictional Work']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Formularies']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<!-- Government Publications -->
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='GuideBooks']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Guideline']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Standard" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Handbooks']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Historical Article']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Incunabula']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Indexes']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Instruction']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioVisualDocument" />
					</xsl:when>
					<!-- Interactive Tutorial -->
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Interview']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Introductory Journal Article']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Journal Article']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<!-- Lectures -->
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Legal Cases']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/LegalCaseDocument" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Legislation']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Legislation" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Maps']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Map" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Meeting Abstracts']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ConferenceProceedings" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Meta-Analysis']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Multicenter Study']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='News']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Newspaper Article']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Patents']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Patent" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Patient Education Handout']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Periodical Index']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Periodicals']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Periodicals" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Pharmacopoeias']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Pictorial Works']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Portraits']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Practice Guideline']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Standard" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Randomized Controlled Trial']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Review']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Statistics']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Study Characteristics']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Technical Report']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Report" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Textbooks']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Twin Study']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PublicationTypeList[PublicationType='Validation Studies']">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					</xsl:when>
					<!--  Webcasts -->
					<xsl:otherwise>
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
					</xsl:otherwise>
				</xsl:choose>
				<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
				<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
				<bibo:pmid><xsl:value-of select="MedlineCitation/PMID" /></bibo:pmid>
				<rdfs:label><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></rdfs:label>
				<core:Title><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></core:Title>
				<score:Affiliation><xsl:value-of select="MedlineCitation/Article/Affiliation" /></score:Affiliation>
				<bibo:volume><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Volume"/></bibo:volume>
				<bibo:number><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Issue"/></bibo:number>
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
						<core:Year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/></core:Year>
					</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
						<core:yearMonth rdf:datatype="http://www.w3.org/2001/XMLSchema#gYearMonth"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" /></core:yearMonth>
					</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Day) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
						<core:date rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" />-<xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Day"/></core:date>
					</xsl:when>
				</xsl:choose>
				<xsl:apply-templates select="MedlineCitation/Article/Affiliation" />
				<xsl:apply-templates select="MedlineCitation/Article/AuthorList/Author" mode="authorRef" />
				<!-- <xsl:apply-templates select="MedlineCitation/MeshHeadingList/MeshHeading" mode="termRef" /> -->
				<xsl:apply-templates select="MedlineCitation/Article/Journal" mode="journalRef"/>
				<!-- <xsl:apply-templates select="MedlineCitation/DateCreated" mode="createdRef" />
				<xsl:apply-templates select="MedlineCitation/DateCompleted"  mode="completedRef" />
				<xsl:apply-templates select="MedlineCitation/DateRevised"  mode="revisedRef" /> -->		
			</rdf:Description>
			<xsl:apply-templates select="MedlineCitation/Article/AuthorList" mode="fullAuthor" />
			<xsl:apply-templates select="MedlineCitation/MeshHeadingList" mode="fullTerm" />	
			<xsl:apply-templates select="MedlineCitation/Article" mode="fullJournal" />
			<xsl:apply-templates select="MedlineCitation/DateCreated" mode="fullCreated" />
			<xsl:apply-templates select="MedlineCitation/DateCompleted"  mode="fullCompleted" />
			<xsl:apply-templates select="MedlineCitation/DateRevised"  mode="fullRevised" />
	</xsl:template>
	
	
	<!-- Email Extraction Regular Expression -->
	<xsl:template match="MedlineCitation/Article/Affiliation">
		<xsl:variable name="elValue" select="."/>
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
				<score:workEmail>
					<xsl:value-of select="regex-group(1)" />
				</score:workEmail>
			</xsl:matching-substring>			
		</xsl:analyze-string>
	</xsl:template>
	
	<!-- Links to From the Paper to the Terms and Authors -->
	<xsl:template match="MedlineCitation/Article/AuthorList/Author" mode="authorRef">
		<core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/harvest/pubmedAuthorship/pmid{ancestor::MedlineCitation/PMID}authorship{position()}" />
	</xsl:template>
	<!-- <xsl:template match="MedlineCitation/MeshHeadingList/MeshHeading" mode="termRef">
		<core:hasSubjectArea rdf:resource="http://vivoweb.org/harvest/pubmedMesh/pmid{ancestor::MedlineCitation/PMID}mesh{position()}" />  
	</xsl:template> -->
	<xsl:template match="MedlineCitation/Article/Journal" mode="journalRef">
		<core:hasPublicationVenue rdf:resource="http://vivoweb.org/harvest/pubmedJournal/journal{child::ISSN}" />
	</xsl:template>
	<!-- <xsl:template match="MedlineCitation/DateCreated" mode="createdRef">
		<score:hasCreateDate rdf:resource="http://vivoweb.org/harvest/pubmedDate/pmid{ancestor::MedlineCitation/PMID}dateCreated" />
	</xsl:template>
	<xsl:template match="MedlineCitation/DateCompleted"  mode="completedRef">
		<score:hasCompleteDate rdf:resource="http://vivoweb.org/harvest/pubmedDate/pmid{ancestor::MedlineCitation/PMID}dateCompleted" />
	</xsl:template>
	<xsl:template match="MedlineCitation/DateRevised"  mode="revisedRef">
		<score:hasReviseDate rdf:resource="http://vivoweb.org/harvest/pubmedDate/pmid{ancestor::MedlineCitation/PMID}dateRevised" />
	</xsl:template>  -->
	
	<!-- Author List Navigation --> 
	<xsl:template match="MedlineCitation/Article/AuthorList" mode="fullAuthor">
		<xsl:apply-templates select="Author" mode="fullAuthor" />
	</xsl:template>
	
	<!-- The Authors -->
	<xsl:template match="Author" mode="fullAuthor">
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
			<xsl:choose>
				<xsl:when test="string(ForeName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdfs:label><xsl:value-of select="LastName" />, <xsl:value-of select="ForeName"/></rdfs:label>
					<foaf:lastName><xsl:value-of select="LastName" /></foaf:lastName>
					<score:foreName><xsl:value-of select="ForeName" /></score:foreName>
					<score:initials><xsl:value-of select="Initials" /></score:initials>
					<score:suffix><xsl:value-of select="Suffix" /></score:suffix>
				</xsl:when>
				<xsl:when test="string(LastName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdfs:label><xsl:value-of select="LastName" /></rdfs:label>
					<foaf:lastName><xsl:value-of select="LastName" /></foaf:lastName>
					<score:foreName><xsl:value-of select="ForeName" /></score:foreName>
					<score:initials><xsl:value-of select="Initials" /></score:initials>
					<score:suffix><xsl:value-of select="Suffix" /></score:suffix>			
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

	
	<!-- The Mesh List -->
	<!-- <xsl:template match="MedlineCitation/MeshHeadingList" mode="fullTerm">
		<xsl:apply-templates select="MeshHeading" mode="fullTerm" />
	</xsl:template>
	 -->
	<!-- The Mesh Terms -->
	<!-- <xsl:template match="MeshHeading" mode="fullTerm">
		<rdf:Description rdf:resource="http://vivoweb.org/harvest/pubmedMesh/pmid{ancestor::MedlineCitation/PMID}mesh{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/score#MeshTerm" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#SubjectArea" />
			<core:SubjectAreaFor rdf:resource="http://vivoweb.org/harvest/pubmedPub/pmid{ancestor::MedlineCitation/PMID}" />
			<rdfs:label><xsl:value-of select="DescriptorName"/></rdfs:label>
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<score:meshTermOf rdf:resource="http://vivoweb.org/harvest/pubmedPub/pmid{ancestor::MedlineCitation/PMID}" />
			<score:Descriptor><xsl:value-of select="DescriptorName"/></score:Descriptor>
			<score:DescriptorIsMajorTerm><xsl:value-of select="DescriptorName/@MajorTopicYN"/></score:DescriptorIsMajorTerm>
			<score:Qualifier><xsl:value-of select="QualifierName"/></score:Qualifier>
			<score:QualifierIsMajorTerm><xsl:value-of select="QualifierName/@MajorTopicYN"/></score:QualifierIsMajorTerm>			
		</rdf:Description>		
	</xsl:template> -->
	
	<xsl:template match="MedlineCitation/MeshHeadingList/MeshHeading" mode="termAsKeyword">
		<xsl:choose>
			<xsl:when test="string(DescriptorName)">
				<core:freetextKeyword><xsl:value-of select="DescriptorName" /></core:freetextKeyword>  
			</xsl:when>
		</xsl:choose>	
		<!-- <core:hasSubjectArea rdf:resource="http://vivoweb.org/harvest/pubmedMesh/pmid{ancestor::MedlineCitation/PMID}mesh{position()}" />  -->
	</xsl:template>
	
	
	<!-- Chemical List -->
	<xsl:template match="MedlineCitation/ChemicalList/Chemical">
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance)">
				<core:freetextKeyword><xsl:value-of select="MedlineCitation/ChemicalList/Chemical/NameOfSubstance" /></core:freetextKeyword>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- Keyword -->
	<xsl:template match="MedlineCitation/KeywordList/Keyword">
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/KeywordList/Keyword)">
				<core:freetextKeyword><xsl:value-of select="MedlineCitation/KeywordList/Keyword" /></core:freetextKeyword>  
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- Date Section:
		Dates in Pubmed can be used for scoring, such as checking to make
		sure that the publication date is not before the authors birth date.
	-->
	<!-- <xsl:template match="MedlineCitation/DateCreated" mode="fullCreated">
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedDate/pmid{ancestor::MedlineCitation/PMID}dateCreated" >
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<core:Year>"<xsl:value-of select="Year"/>"</core:Year>
			<core:Month>"<xsl:value-of select="Month"/>"</core:Month>
			<core:Day>"<xsl:value-of select="Day"/>"</core:Day>
		</rdf:Description>
	</xsl:template>
	<xsl:template match="MedlineCitation/DateCompleted"  mode="fullCompleted">
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedDate/pmid{ancestor::MedlineCitation/PMID}dateCompleted" >
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<core:Year><xsl:value-of select="Year"/></core:Year>
			<core:Month><xsl:value-of select="Month"/></core:Month>
			<core:Day><xsl:value-of select="Day"/></core:Day>
		</rdf:Description>
	</xsl:template>
	<xsl:template match="MedlineCitation/DateRevised"  mode="fullRevised">
		<rdf:Description rdf:about="http://vivoweb.org/harvest/pubmedDate/pmid{ancestor::MedlineCitation/PMID}dateRevised" >
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<core:Year><xsl:value-of select="Year"/></core:Year>
			<core:Month><xsl:value-of select="Month"/></core:Month>
			<core:Day><xsl:value-of select="Day"/></core:Day>
		</rdf:Description>
	</xsl:template>
	-->
	
	<!-- The Main Article Entity -->
	<xsl:template match="MedlineCitation/Article" mode="fullJournal">
		<xsl:apply-templates select="Journal" mode="fullJournal"/>
	</xsl:template>
	
		
	<!-- The Main Journal Entity -->
	<xsl:template match="Journal" mode="fullJournal">
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
	
	
</xsl:stylesheet>