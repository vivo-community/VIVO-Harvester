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
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>

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
			xmlns:xs="http://www.w3.org/2001/XMLSchema#">
			<xsl:apply-templates select="PubmedArticle" />			
		</rdf:RDF>
	</xsl:template>
	
	<!-- The Article -->
	<xsl:template match="PubmedArticle">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/article/pmid{child::MedlineCitation/PMID}">
				<rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
				<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
				<bibo:pmid><xsl:value-of select="MedlineCitation/PMID" /></bibo:pmid>
				<rdfs:label><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></rdfs:label>
				<core:Title><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></core:Title>
				<score:Affiliation><xsl:value-of select="MedlineCitation/Article/Affiliation" /></score:Affiliation>
				<bibo:volume><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Volume"/></bibo:volume>
				<bibo:number><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Issue"/></bibo:number>
				<xsl:choose>
					<xsl:when test="MedlineCitation/Article/PubDate/Year">
						<core:Year><xsl:value-of select="MedlineCitation/Article/PubDate/Year"/></core:Year>
					</xsl:when>
					<!-- 
					<xsl:when test="MedlineCitation/Article/PubDate/Month">
					</xsl:when>
					<xsl:when test="MedlineCitation/Article/PubDate/Day">
					</xsl:when>
					-->
				</xsl:choose>
				
				
				
				
				<xsl:apply-templates select="MedlineCitation/Article/Affiliation" />
				<xsl:apply-templates select="MedlineCitation/Article/AuthorList/Author" mode="authorRef" />
				<xsl:apply-templates select="MedlineCitation/MeshHeadingList/MeshHeading" mode="termRef" />
				<xsl:apply-templates select="MedlineCitation/Article/Journal" mode="journalRef"/>
				<xsl:apply-templates select="MedlineCitation/DateCreated" mode="createdRef" />
				<xsl:apply-templates select="MedlineCitation/DateCompleted"  mode="completedRef" />
				<xsl:apply-templates select="MedlineCitation/DateRevised"  mode="revisedRef" />				
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
		<xsl:analyze-string select="$elValue" regex="\s*([a-z\.]*@[a-z\.]*)">			
			<xsl:matching-substring>
				<score:workEmail>
					<xsl:value-of select="regex-group(1)" />
				</score:workEmail>
			</xsl:matching-substring>			
		</xsl:analyze-string>
	</xsl:template>
	
	<!-- Links to From the Paper to the Terms and Authors -->
	<xsl:template match="MedlineCitation/Article/AuthorList/Author" mode="authorRef">
		<core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/authorship{position()}" />
	</xsl:template>
	<xsl:template match="MedlineCitation/MeshHeadingList/MeshHeading" mode="termRef">
		<core:hasSubjectArea rdf:resource="http://vivoweb.org/pubMed/mesh/m{self::DescriptorName}" />
	</xsl:template>
	<xsl:template match="MedlineCitation/Article/Journal" mode="journalRef">
		<core:hasPublicationVenue rdf:resource="http://vivoweb.org/pubMed/journal/j{child::ISSN}" />
	</xsl:template>
	<xsl:template match="MedlineCitation/DateCreated" mode="createdRef">
		<score:hasCreateDate rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/dateCreated" />
	</xsl:template>
	<xsl:template match="MedlineCitation/DateCompleted"  mode="completedRef">
		<score:hasCompleteDate rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/dateCompleted" />
	</xsl:template>
	<xsl:template match="MedlineCitation/DateRevised"  mode="revisedRef">
		<score:hasReviseDate rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/dateRevised" />
	</xsl:template>
	
	<!-- Author List Navigation --> 
	<xsl:template match="MedlineCitation/Article/AuthorList" mode="fullAuthor">
		<xsl:apply-templates select="Author" mode="fullAuthor" />
	</xsl:template>
	
	<!-- The Authors -->
	<xsl:template match="Author" mode="fullAuthor">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/authorship{position()}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DependentResource" />
			<core:linkedAuthor rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/author{position()}" />
			<core:linkedInformationResource rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}"/>
			<core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></core:authorRank>			
		</rdf:Description>
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/author{position()}">
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
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<core:authorInAuthorship rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/authorship{position()}" />
		</rdf:Description>
	</xsl:template>	

	
	<!-- The Mesh List -->
	<xsl:template match="MedlineCitation/MeshHeadingList" mode="fullTerm">
		<xsl:apply-templates select="MeshHeading" mode="fullTerm" />
	</xsl:template>
	
	<!-- The Mesh Terms -->
	<xsl:template match="MeshHeading" mode="fullTerm">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/mesh/m{self::DescriptorName}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/score#MeshTerm" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#SubjectArea" />
			<core:SubjectAreaFor rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}" />
			<rdfs:label><xsl:value-of select="DescriptorName"/></rdfs:label>
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<score:meshTermOf rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}" />
			<score:Descriptor><xsl:value-of select="DescriptorName"/></score:Descriptor>
			<score:DescriptorIsMajorTerm><xsl:value-of select="DescriptorName/@MajorTopicYN"/></score:DescriptorIsMajorTerm>
			<score:Qualifier><xsl:value-of select="QualifierName"/></score:Qualifier>
			<score:QualifierIsMajorTerm><xsl:value-of select="QualifierName/@MajorTopicYN"/></score:QualifierIsMajorTerm>			
		</rdf:Description>		
	</xsl:template>
	
	<!-- Date Section:
		Dates in Pubmed can be used for scoring, such as checking to make
		sure that the publication date is not before the authors birth date.
	-->
	<xsl:template match="MedlineCitation/DateCreated" mode="fullCreated">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/dateCreated" >
			<core:Year>"<xsl:value-of select="Year"/>"</core:Year>
			<core:Month>"<xsl:value-of select="Month"/>"</core:Month>
			<core:Day>"<xsl:value-of select="Day"/>"</core:Day>
		</rdf:Description>
	</xsl:template>
	<xsl:template match="MedlineCitation/DateCompleted"  mode="fullCompleted">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/dateCompleted" >
			<core:Year><xsl:value-of select="Year"/></core:Year>
			<core:Month><xsl:value-of select="Month"/></core:Month>
			<core:Day><xsl:value-of select="Day"/></core:Day>
		</rdf:Description>
	</xsl:template>
	<xsl:template match="MedlineCitation/DateRevised"  mode="fullRevised">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}/dateRevised" >
			<core:Year><xsl:value-of select="Year"/></core:Year>
			<core:Month><xsl:value-of select="Month"/></core:Month>
			<core:Day><xsl:value-of select="Day"/></core:Day>
		</rdf:Description>
	</xsl:template>
	
	
	<!-- The Main Article Entity -->
	<xsl:template match="MedlineCitation/Article" mode="fullJournal">
		<xsl:apply-templates select="Journal" mode="fullJournal"/>
	</xsl:template>
	
		
	<!-- The Main Journal Entity -->
	<xsl:template match="Journal" mode="fullJournal">
		<rdf:Description rdf:about="http://vivoweb.org/pubMed/journal/j{child::ISSN}" >
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal" />
			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
			<core:Title><xsl:value-of select="Title" /></core:Title>
			<rdfs:label><xsl:value-of select="Title" /></rdfs:label>
			<bibo:ISSN><xsl:value-of select="ISSN"/></bibo:ISSN>
			<core:publicationVenueFor rdf:resource="http://vivoweb.org/pubMed/article/pmid{ancestor::MedlineCitation/PMID}"/>
		</rdf:Description>	
	</xsl:template>	
	
	
</xsl:stylesheet>