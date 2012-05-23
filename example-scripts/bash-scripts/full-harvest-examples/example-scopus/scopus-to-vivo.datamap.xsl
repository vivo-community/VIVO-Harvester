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
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
	xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/pubmed/</xsl:variable>
	<xsl:variable name="customURI">[Enter your institutional namespace here]</xsl:variable>

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
			<xsl:apply-templates select="PubmedBookArticle" />
		</rdf:RDF>
	</xsl:template>
	
	<!-- The Article -->
	<xsl:template match="PubmedArticle">
		<xsl:variable name="emailAddress">
			<!--
			Possible better email match regex.
			Needs tested. Not sure if &quot will work for "
			- -Dale
			-->
			<xsl:if test="normalize-space( MedlineCitation/Article/Affiliation )">
				<xsl:analyze-string select="MedlineCitation/Article/Affiliation" regex="\s*([a-zA-Z\d\.]*@[a-zA-Z\d\.]*)">
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
			</xsl:if>
		</xsl:variable>
		<rdf:Description rdf:about="{$customURI}pubid{child::MedlineCitation/PMID}">
			<xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ '"/>
			<xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz '"/>
			<xsl:variable name="status"><xsl:value-of select="translate(MedlineCitation/@Status,$up,$lo)" /></xsl:variable>
			<xsl:apply-templates select="MedlineCitation/Article/PublicationTypeList/PublicationType" mode="supportRef">
				<xsl:with-param name="pbStatus" select="$status" />
			</xsl:apply-templates>

			<xsl:variable name="lastPubType">
				<xsl:value-of select="MedlineCitation/Article/PublicationTypeList/PublicationType[last()]"/>
			</xsl:variable>
			
			<!-- Determine if it's multiple journal types or simply a journal article  -->
			<xsl:choose>
				<xsl:when test="$status!='in-process' and $status!='publisher' and $status!='in-data-review'">
					<xsl:choose>
						<xsl:when test="translate(string($lastPubType),$up,$lo)!='introductory journal article' and (translate(string($lastPubType),$up,$lo)='journal article' or contains(translate(string($lastPubType),$up,$lo), 'research support'))">
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
							<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
						</xsl:when>
					<!-- 
						<xsl:when test="translate(string($lastPubType),$up,$lo)='introductory journal article' or (translate(string($lastPubType),$up,$lo)!='journal article' and not(contains(translate(string($lastPubType),$up,$lo), 'research support')))">
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
						</xsl:when>
						<xsl:otherwise>
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
							<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
						</xsl:otherwise>
						-->
					</xsl:choose>
				</xsl:when>
			</xsl:choose>

			<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
			<score:pubId><xsl:value-of select="MedlineCitation/PMID" /></score:pubId>
			<bibo:pmid><xsl:value-of select="MedlineCitation/PMID" /></bibo:pmid>
			<rdfs:label><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></rdfs:label>
			<core:Title><xsl:value-of select="MedlineCitation/Article/ArticleTitle" /></core:Title>
			<score:Affiliation><xsl:value-of select="MedlineCitation/Article/Affiliation" /></score:Affiliation>
			<bibo:volume><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Volume"/></bibo:volume>
			<bibo:number><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Issue"/></bibo:number>
			<bibo:abstract><xsl:value-of select="MedlineCitation/Article/Abstract"/></bibo:abstract>
			<xsl:apply-templates select="MedlineCitation/ChemicalList/Chemical" mode="subjectAreaRef" />
			<xsl:apply-templates select="MedlineCitation/KeywordList/Keyword" mode="subjectAreaRef" />
			<xsl:apply-templates select="MedlineCitation/MeshHeadingList/MeshHeading" mode="subjectAreaRef" />
			
			<!-- Status: BEGIN -->
			<xsl:choose>
				<xsl:when test="translate($status,$up,$lo)='publisher'">
					<bibo:status rdf:resource="http://vivoweb.org/ontology/core#inPress"/>
					<core:freetextKeyword>In press</core:freetextKeyword>
				</xsl:when>
				<xsl:when test="translate($status,$up,$lo)='in-data-review'">
					<bibo:status rdf:resource="http://vivoweb.org/ontology/core#inPress"/>
					<core:freetextKeyword>In press</core:freetextKeyword>
				</xsl:when>
				<xsl:when test="translate($status,$up,$lo)='in-process'">
					<bibo:status rdf:resource="http://vivoweb.org/ontology/core#inPress"/>
					<core:freetextKeyword>In press</core:freetextKeyword>
				</xsl:when>
				<xsl:when test="translate($status,$up,$lo)='medline'">
					<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
				</xsl:when>
				<xsl:when test="translate($status,$up,$lo)='pubmed-not-medline'">
					<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
				</xsl:when>
			</xsl:choose>
			<!-- Status: END -->

			<!-- doi -->
			<xsl:choose>
				<xsl:when test='string(PubmedData/ArticleIdList/ArticleId[@IdType="doi"])'>
					<bibo:doi><xsl:value-of select='PubmedData/ArticleIdList/ArticleId[@IdType="doi"]' /></bibo:doi>
				</xsl:when>
			</xsl:choose>

			<!-- pmc -->
			<xsl:choose>
				<xsl:when test='string(PubmedData/ArticleIdList/ArticleId[@IdType="pmc"])'>
					<core:pmcid><xsl:value-of select='PubmedData/ArticleIdList/ArticleId[@IdType="pmc"]' /></core:pmcid>
				</xsl:when>
			</xsl:choose>

			<xsl:variable name="MonthNumber">
				<xsl:choose>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Jan'">01</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Feb'">02</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Mar'">03</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Apr'">04</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='May'">05</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Jun'">06</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Jul'">07</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Aug'">08</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Sep'">09</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Oct'">10</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Nov'">11</xsl:when>
					<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)='Dec'">12</xsl:when>
				</xsl:choose>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Day) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
					<core:dateTimeValue>
						<xsl:variable name="modDay" select='format-number(MedlineCitation/Article/Journal/JournalIssue/PubDate/Day, "00")'/>
                        <rdf:Description rdf:about="{$baseURI}pub/daymonthyear{$modDay}{$MonthNumber}{MedlineCitation/Article/Journal/JournalIssue/PubDate/Year}">
	                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
	                        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
	                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber"/>-<xsl:value-of select="$modDay"/>T00:00:00</core:dateTime>
                        </rdf:Description>
                    </core:dateTimeValue>
					<!-- <core:date rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" />-<xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Day"/></core:date> -->
				</xsl:when>
				<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month) and string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
                    <core:dateTimeValue>
                        <rdf:Description rdf:about="{$baseURI}pub/monthyear{$MonthNumber}{MedlineCitation/Article/Journal/JournalIssue/PubDate/Year}">
	                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
	                        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthPrecision"/>
	                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber"/>-01T00:00:00</core:dateTime>
                        </rdf:Description>
                    </core:dateTimeValue>
					<!-- <core:yearMonth rdf:datatype="http://www.w3.org/2001/XMLSchema#gYearMonth"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" /></core:yearMonth> -->
				</xsl:when>
				<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Year)">
				    <core:dateTimeValue>
                        <rdf:Description rdf:about="{$baseURI}pub/year{MedlineCitation/Article/Journal/JournalIssue/PubDate/Year}">
					        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
					        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
	                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/>-01-01T00:00:00</core:dateTime>
				        </rdf:Description>
                    </core:dateTimeValue>
					<!-- <core:year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/></core:year> -->
				</xsl:when>
			</xsl:choose>
			<!-- parse page numbers: START -->
			<bibo:pageStart><xsl:value-of select="substring-before(MedlineCitation/Article/Pagination/MedlinePgn, '-')" /></bibo:pageStart>
			<xsl:variable name="pageStart"><xsl:value-of select="substring-before(MedlineCitation/Article/Pagination/MedlinePgn, '-')" /></xsl:variable>
			<xsl:variable name="pageEnd"><xsl:value-of select="substring-after(MedlineCitation/Article/Pagination/MedlinePgn, '-')" /></xsl:variable>
			<xsl:choose>
				<xsl:when test="string-length($pageStart) &gt; string-length($pageEnd)">
					<xsl:variable name="cutoff"><xsl:value-of select="(string-length($pageStart))-(string-length($pageEnd))+1" /></xsl:variable>
					<bibo:pageEnd><xsl:value-of select="concat(substring($pageStart,0,$cutoff),$pageEnd)"/></bibo:pageEnd>
				</xsl:when>
				<xsl:when test="string-length($pageStart) &lt;= string-length($pageEnd)">
					<bibo:pageEnd><xsl:value-of select="$pageEnd"/></bibo:pageEnd>
				</xsl:when>
			</xsl:choose>
			<!-- parse page numbers: END -->
			
			<xsl:apply-templates select="MedlineCitation/Article/AuthorList/Author" mode="authorRef" />
			<xsl:apply-templates select="MedlineCitation/Article/Journal" mode="journalRef"/>
		</rdf:Description>

		<xsl:apply-templates select="MedlineCitation/Article/AuthorList/Author" mode="fullAuthor">
			<xsl:with-param name="email" select="$emailAddress" />
		</xsl:apply-templates>
		<xsl:apply-templates select="MedlineCitation/Article/Journal" mode="fullJournal" />
		<xsl:apply-templates select="MedlineCitation/ChemicalList/Chemical" mode="fullSubjectArea">
			<xsl:with-param name="pubId" select="MedlineCitation/PMID" />
		</xsl:apply-templates>
		<xsl:apply-templates select="MedlineCitation/KeywordList/Keyword" mode="fullSubjectArea">
			<xsl:with-param name="pubId" select="MedlineCitation/PMID" />
		</xsl:apply-templates>
		<xsl:apply-templates select="MedlineCitation/MeshHeadingList/MeshHeading" mode="fullSubjectArea">
			<xsl:with-param name="pubId" select="MedlineCitation/PMID" />
		</xsl:apply-templates>
		<xsl:apply-templates select="MedlineCitation/Article/PublicationTypeList/PublicationType" mode="supportFull">
			<xsl:with-param name="pubId" select="MedlineCitation/PMID" />
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- The Book Article -->
	<xsl:template match="PubmedBookArticle">
		<rdf:Description rdf:about="{$customURI}pubid{child::BookDocument/PMID}">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
			<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
			<score:pubId><xsl:value-of select="BookDocument/PMID" /></score:pubId>
			<bibo:pmid><xsl:value-of select="BookDocument/PMID" /></bibo:pmid>
			<rdfs:label><xsl:value-of select="BookDocument/Book/BookTitle" /></rdfs:label>
			<core:Title><xsl:value-of select="BookDocument/Book/BookTitle" /></core:Title>
			<score:Affiliation><xsl:value-of select="BookDocument/Book/CollectionTitle" /></score:Affiliation>
			<bibo:abstract><xsl:value-of select="BookDocument/Abstract"/></bibo:abstract>
			<xsl:apply-templates select="BookDocument/KeywordList/Keyword" mode="subjectAreaRef" />
			
			<!-- doi: BEGIN -->
			<xsl:choose>
				<xsl:when test='string(PubmedData/ArticleIdList/ArticleId[@IdType="doi"])'>
					<bibo:doi><xsl:value-of select='PubmedData/ArticleIdList/ArticleId[@IdType="doi"]' /></bibo:doi>
				</xsl:when>
			</xsl:choose>

			<xsl:choose>
				<xsl:when test='string(PubmedBookData/ArticleIdList/ArticleId[@IdType="doi"])'>
					<bibo:doi><xsl:value-of select='PubmedBookData/ArticleIdList/ArticleId[@IdType="doi"]' /></bibo:doi>
				</xsl:when>
			</xsl:choose>
			<!-- doi: END -->
						
			<!-- pmc: BEGIN -->
			<xsl:choose>
				<xsl:when test='string(PubmedData/ArticleIdList/ArticleId[@IdType="pmc"])'>
					<core:pmcid><xsl:value-of select='PubmedData/ArticleIdList/ArticleId[@IdType="pmc"]' /></core:pmcid>
				</xsl:when>
			</xsl:choose>

			<xsl:choose>
				<xsl:when test='string(PubmedBookData/ArticleIdList/ArticleId[@IdType="pmc"])'>
					<core:pmcid><xsl:value-of select='PubmedBookData/ArticleIdList/ArticleId[@IdType="pmc"]' /></core:pmcid>
				</xsl:when>
			</xsl:choose>
			<!-- pmc: END -->

			<xsl:variable name="MonthNumber">
				<xsl:choose>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Jan'">01</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Feb'">02</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Mar'">03</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Apr'">04</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='May'">05</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Jun'">06</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Jul'">07</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Aug'">08</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Sep'">09</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Oct'">10</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Nov'">11</xsl:when>
					<xsl:when test="string(BookDocument/Book/PubDate/Month)='Dec'">12</xsl:when>
				</xsl:choose>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="string(BookDocument/Book/PubDate/Year)">
                    <core:dateTimeValue>
                        <rdf:Description rdf:about="{$baseURI}pub/year{BookDocument/Book/PubDate/Year}">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                            <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
                            <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="BookDocument/Book/PubDate/Year"/>-01-01T00:00:00</core:dateTime>
                        </rdf:Description>
                    </core:dateTimeValue>
<!-- 					<core:year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="BookDocument/Book/PubDate/Year"/></core:year> -->
				</xsl:when>
				<xsl:when test="string(BookDocument/Book/PubDate/Month) and string(BookDocument/Book/PubDate/Year)">
                    <core:dateTimeValue>
                        <rdf:Description rdf:about="{$baseURI}pub/monthyear{$MonthNumber}{BookDocument/Book/PubDate/Year}">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                            <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthPrecision"/>
                            <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="BookDocument/Book/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber"/>-01T00:00:00</core:dateTime>
                        </rdf:Description>
                    </core:dateTimeValue>
<!-- 					<core:yearMonth rdf:datatype="http://www.w3.org/2001/XMLSchema#gYearMonth"><xsl:value-of select="BookDocument/Book/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" /></core:yearMonth> -->
				</xsl:when>
				<xsl:when test="string(BookDocument/Book/PubDate/Day) and string(BookDocument/Book/PubDate/Month) and string(BookDocument/Book/PubDate/Year)">
                    <core:dateTimeValue>
                        <rdf:Description rdf:about="{$baseURI}pub/daymonthyear{MedlineCitation/Article/Journal/JournalIssue/PubDate/Day}{$MonthNumber}{BookDocument/Book/PubDate/Year}">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                            <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
                            <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="BookDocument/Book/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber"/>-<xsl:value-of select="BookDocument/Book/PubDate/Day"/>T00:00:00</core:dateTime>
                        </rdf:Description>
                    </core:dateTimeValue>
<!-- 					<core:date rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="BookDocument/Book/PubDate/Year"/>-<xsl:copy-of select="$MonthNumber" />-<xsl:value-of select="BookDocument/Book/PubDate/Day"/></core:date> -->
				</xsl:when>
			</xsl:choose>

			<xsl:apply-templates select="BookDocument/Book/AuthorList/Author" mode="authorRef" />
		</rdf:Description>
		<xsl:apply-templates select="BookDocument/Book/AuthorList/Author" mode="fullAuthor" />
		<xsl:apply-templates select="BookDocument/KeywordList/Keyword" mode="fullSubjectArea">
			<xsl:with-param name="pubId" select="BookDocument/PMID" />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="MedlineCitation/Article/Journal" mode="journalRef">
		<core:hasPublicationVenue rdf:resource="{$customURI}journal{child::ISSN}" />
	</xsl:template>

<!-- The Main Journal Entity -->
	<xsl:template match="MedlineCitation/Article/Journal" mode="fullJournal">
		<rdf:Description rdf:about="{$customURI}journal{child::ISSN}" >
			<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal" />
			<core:Title><xsl:value-of select="Title" /></core:Title>
			<rdfs:label><xsl:value-of select="Title" /></rdfs:label>
			<bibo:issn><xsl:value-of select="ISSN"/></bibo:issn>
			<core:publicationVenueFor rdf:resource="{$customURI}pubid{ancestor::MedlineCitation/PMID}"/>
		</rdf:Description>	
	</xsl:template>

	<!-- Scopus Linked Author List Navigation --> 
	<xsl:template match="scopusAuthorList/author" mode="authorRef">
		<xsl:param name='pubId' />
		<core:informationResourceInAuthorship rdf:resource="{$customURI}pubid{$pubId}authorship{position()}" />
	</xsl:template>
	<xsl:template match="author" mode="authorRef">
		<xsl:param name='pubId' />
		<core:informationResourceInAuthorship rdf:resource="{$customURI}pubid{$pubId}authorship{position()}" />
	</xsl:template>

	<!-- Scopus Author List Navigation --> 
	<xsl:template match="author" mode="fullAuthor">
		<xsl:param name='pubId' />
		<rdf:Description rdf:about="{$customURI}pubid{$pubId}authorship{position()}">
			<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:linkedAuthor rdf:resource="{$baseURI}author/pubid{$pubId}author{position()}" />
			<core:linkedInformationResource rdf:resource="{$customURI}pubid{$pubId}"/>
			<rdfs:label>Authorship for <xsl:value-of select="authname" /></rdfs:label>
			<core:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></core:authorRank>			
		</rdf:Description>
		<rdf:Description rdf:about="{$baseURI}author/pubid{$pubId}author{position()}">
			<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
			<rdfs:label><xsl:value-of select="authname" /></rdfs:label>
			<foaf:lastName><xsl:value-of select="substring-before(authname, ',')" /></foaf:lastName>
			<core:scopusId><xsl:value-of select="authid" /></core:scopusId>
			<core:authorInAuthorship rdf:resource="{$customURI}pubid{$pubId}authorship{position()}" />
		</rdf:Description>
	</xsl:template>

	<!-- Links to From the Article to the Terms and Authors -->
	<xsl:template match="MedlineCitation/Article/AuthorList/Author" mode="authorRef">
	<xsl:choose>
			<xsl:when test="string(MedlineCitation/Article/Journal/JournalIssue/PubDate/Month)=Jan">
			</xsl:when>
	</xsl:choose>		
		<core:informationResourceInAuthorship rdf:resource="{$customURI}pubid{ancestor::MedlineCitation/PMID}authorship{position()}" />
	</xsl:template>

	
	<!-- Article Author List Navigation --> 
	<xsl:template match="MedlineCitation/Article/AuthorList/Author" mode="fullAuthor">
			<xsl:param name='email' />
		<rdf:Description rdf:about="{$customURI}pubid{ancestor::MedlineCitation/PMID}authorship{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:linkedAuthor rdf:resource="{$baseURI}author/pubid{ancestor::MedlineCitation/PMID}author{position()}" />
			<core:linkedInformationResource rdf:resource="{$customURI}pubid{ancestor::MedlineCitation/PMID}"/>
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
		<rdf:Description rdf:about="{$baseURI}author/pubid{ancestor::MedlineCitation/PMID}author{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<core:scopusId><xsl:value-of select="authid" /></core:scopusId>
			<score:email><xsl:value-of select="$email" /></score:email>
			<xsl:choose>
				<xsl:when test="string(ForeName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdfs:label><xsl:value-of select="LastName" />, <xsl:value-of select="ForeName"/></rdfs:label>
					<foaf:lastName><xsl:value-of select="LastName" /></foaf:lastName>
					<score:foreName><xsl:value-of select="ForeName" /></score:foreName>
					<score:initials><xsl:value-of select="Initials" /></score:initials>
					<score:suffix><xsl:value-of select="Suffix" /></score:suffix>
					<!-- Parse out possible middle name -->
					<xsl:analyze-string select="string(ForeName)" regex="^\s*(\S+)\s*(.*)$">
						<xsl:matching-substring>
							<foaf:firstName><xsl:value-of select="regex-group(1)" /></foaf:firstName>
							<xsl:choose>
								<xsl:when test="normalize-space(regex-group(2))">
									<core:middleName><xsl:value-of select="regex-group(2)" /></core:middleName>
								</xsl:when>
							</xsl:choose>
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
					<xsl:analyze-string select="string(ForeName)" regex="^\s*(\S+)\s*(.*)$">
					    <xsl:matching-substring>
							<foaf:firstName><xsl:value-of select="regex-group(1)" /></foaf:firstName>
							<xsl:choose>
								<xsl:when test="normalize-space(regex-group(2))">
									<core:middleName><xsl:value-of select="regex-group(2)" /></core:middleName>
								</xsl:when>
							</xsl:choose>
						</xsl:matching-substring>
					</xsl:analyze-string>			
				</xsl:when>
				<xsl:when test="string(CollectiveName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
					<rdfs:label><xsl:value-of select="CollectiveName" /></rdfs:label>
				</xsl:when>
			</xsl:choose>
			<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
			<core:authorInAuthorship rdf:resource="{$customURI}pubid{ancestor::MedlineCitation/PMID}authorship{position()}" />
		</rdf:Description>
	</xsl:template>

	<!-- Links to From the Book to the Terms and Authors -->
	<xsl:template match="BookDocument/Book/AuthorList/Author" mode="authorRef">
		<core:informationResourceInAuthorship rdf:resource="{$customURI}pubid{ancestor::MedlineCitation/PMID}authorship{position()}" />
	</xsl:template>

	<!-- Book Author List Navigation --> 
	<xsl:template match="BookDocument/Book/AuthorList/Author" mode="fullAuthor">
			<rdf:Description rdf:about="{$customURI}pubid{ancestor::BookDocument/PMID}authorship{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:linkedAuthor rdf:resource="{$baseURI}author/pubid{ancestor::BookDocument/PMID}author{position()}" />
			<core:linkedInformationResource rdf:resource="{$customURI}pubid{ancestor::BookDocument/PMID}"/>
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
		<rdf:Description rdf:about="{$baseURI}author/pubid{ancestor::BookDocument/PMID}author{position()}">
			<ufVivo:harvestedBy>PubMed-Harvester</ufVivo:harvestedBy>
			<xsl:choose>
				<xsl:when test="string(ForeName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
					<rdfs:label><xsl:value-of select="LastName" />, <xsl:value-of select="ForeName"/></rdfs:label>
					<foaf:lastName><xsl:value-of select="LastName" /></foaf:lastName>
					<score:foreName><xsl:value-of select="ForeName" /></score:foreName>
					<score:initials><xsl:value-of select="Initials" /></score:initials>
					<score:suffix><xsl:value-of select="Suffix" /></score:suffix>
					<!-- Parse out possible middle name -->
					<xsl:analyze-string select="string(ForeName)" regex="^\s*(\S+)\s*(.*)$">
						<xsl:matching-substring>
							<foaf:firstName><xsl:value-of select="regex-group(1)" /></foaf:firstName>
							<xsl:choose>
								<xsl:when test="normalize-space(regex-group(2))">
									<core:middleName><xsl:value-of select="regex-group(2)" /></core:middleName>
								</xsl:when>
							</xsl:choose>
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
					<xsl:analyze-string select="string(ForeName)" regex="^\s*(\S+)\s*(.*)$">
					    <xsl:matching-substring>
							<foaf:firstName><xsl:value-of select="regex-group(1)" /></foaf:firstName>
							<xsl:choose>
								<xsl:when test="normalize-space(regex-group(2))">
									<core:middleName><xsl:value-of select="regex-group(2)" /></core:middleName>
								</xsl:when>
							</xsl:choose>
						</xsl:matching-substring>
					</xsl:analyze-string>			
				</xsl:when>
				<xsl:when test="string(CollectiveName)">
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
					<rdfs:label><xsl:value-of select="CollectiveName" /></rdfs:label>
				</xsl:when>
			</xsl:choose>
			<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
			<core:authorInAuthorship rdf:resource="{$customURI}pubid{ancestor::BookDocument/PMID}authorship{position()}" />
		</rdf:Description>
	</xsl:template>

	<!-- MeshHeading -->
	<xsl:template match="MedlineCitation/MeshHeadingList/MeshHeading" mode="subjectAreaRef">
		<xsl:choose>
			<xsl:when test="string(DescriptorName)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(DescriptorName),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<core:hasSubjectArea rdf:resource="http://vivo.med.cornell.edu/individual/concept-{$subjectId}"/>
			</xsl:when>
		</xsl:choose>	
	</xsl:template>	
	
	<xsl:template match="MedlineCitation/MeshHeadingList/MeshHeading" mode="fullSubjectArea">
		<xsl:param name='pubId' />
		<xsl:choose>
			<xsl:when test="string(DescriptorName)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(DescriptorName),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<rdf:Description rdf:about="http://vivo.med.cornell.edu/individual/concept-{$subjectId}">
					<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
					<vitro:mostSpecificType rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
    				<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
    					<xsl:value-of select="string(DescriptorName)"/>
    				</rdfs:label>
 					<core:subjectAreaFor rdf:resource="{$customURI}pubid{$pubId}"/> 				
  				</rdf:Description>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- Chemical List -->
	<xsl:template match="MedlineCitation/ChemicalList/Chemical" mode="subjectAreaRef">
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<core:hasSubjectArea rdf:resource="http://vivo.med.cornell.edu/individual/concept-{$subjectId}"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="MedlineCitation/ChemicalList/Chemical" mode="fullSubjectArea">
		<xsl:param name='pubId' />
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<rdf:Description rdf:about="http://vivo.med.cornell.edu/individual/concept-{$subjectId}">
					<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
					<vitro:mostSpecificType rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
    				<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
    					<xsl:value-of select="string(MedlineCitation/ChemicalList/Chemical/NameOfSubstance)"/>
    				</rdfs:label>
 					<core:subjectAreaFor rdf:resource="{$customURI}pubid{$pubId}"/> 				
  				</rdf:Description>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- Article Keyword -->
	<xsl:template match="MedlineCitation/KeywordList/Keyword" mode="subjectAreaRef">
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/KeywordList/Keyword)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(MedlineCitation/KeywordList/Keyword),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<core:hasSubjectArea rdf:resource="http://vivo.med.cornell.edu/individual/concept-{$subjectId}"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="MedlineCitation/KeywordList/Keyword" mode="fullSubjectArea">
		<xsl:param name='pubId' />
		<xsl:choose>
			<xsl:when test="string(MedlineCitation/KeywordList/Keyword)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(MedlineCitation/KeywordList/Keyword),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<rdf:Description rdf:about="http://vivo.med.cornell.edu/individual/concept-{$subjectId}">
					<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
					<vitro:mostSpecificType rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
    				<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
    					<xsl:value-of select="string(MedlineCitation/KeywordList/Keyword)"/>
    				</rdfs:label>
 					<core:subjectAreaFor rdf:resource="{$customURI}pubid{$pubId}"/> 				
  				</rdf:Description>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- Book Keyword -->
	<xsl:template match="BookDocument/KeywordList/Keyword" mode="subjectAreaRef">
		<xsl:choose>
			<xsl:when test="string(BookDocument/KeywordList/Keyword)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(BookDocument/KeywordList/Keyword),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<core:hasSubjectArea rdf:resource="http://vivo.med.cornell.edu/individual/concept-{$subjectId}"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="BookDocument/KeywordList/Keyword" mode="fullSubjectArea">
		<xsl:param name='pubId' />
		<xsl:choose>
			<xsl:when test="string(BookDocument/KeywordList/Keyword)">
				<xsl:variable name="subjectId">
					<xsl:value-of select="replace(string(BookDocument/KeywordList/Keyword),'[^a-zA-Z0-9]','')"/>
				</xsl:variable>
				<rdf:Description rdf:about="http://vivo.med.cornell.edu/individual/concept-{$subjectId}">
					<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
					<vitro:mostSpecificType rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
    				<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">
    					<xsl:value-of select="string(BookDocument/KeywordList/Keyword)"/>
    				</rdfs:label>
 					<core:subjectAreaFor rdf:resource="{$customURI}pubid{$pubId}"/> 				
  				</rdf:Description>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- Supported by -->
	<xsl:template match="MedlineCitation/Article/PublicationTypeList/PublicationType" mode="supportFull">
		<xsl:param name='pubId' />
		<xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ '"/>
		<xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz '"/>
		<xsl:variable name="pbType" select="string(self::PublicationType)" />
		<xsl:choose>
			<xsl:when test="contains(translate(string($pbType),$up,$lo), 'research support')">
				<xsl:variable name="supportedByLink" select="replace(translate(string($pbType),$up,$lo),'[^a-zA-Z0-9]','')" />
				<rdf:Description rdf:about="http://vivo.med.cornell.edu/individual/org-{$supportedByLink}">
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#FundingOrganization"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FundingOrganization"/>
					<core:supportedInformationResource rdf:resource="{$customURI}pubid{$pubId}"/>
    				<rdfs:label><xsl:value-of select="$pbType"/></rdfs:label>
				</rdf:Description>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- Types -->
	<xsl:template match="MedlineCitation/Article/PublicationTypeList/PublicationType" mode="supportRef">
		<xsl:param name='pbStatus' />
		<xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ '"/>
		<xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz '"/>
		<xsl:variable name="pbType" select="string(self::PublicationType)" />
		<xsl:choose>
			<xsl:when test="$pbStatus!='in-process' and $pbStatus!='publisher' and $pbStatus!='in-data-review'">
			
			<xsl:choose>
				
				<!-- Research articles  -->
				<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase i'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase ii'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase iii'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='clinical trial, phase iv'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='controlled clinical trial'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='randomized controlled trial'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='twin study'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='validation studies'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='case reports'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='comparative study'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
				</xsl:when>
				
				<!-- Reviews -->
				<xsl:when test="translate(string($pbType),$up,$lo)='meta-analysis'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Review" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='practice guideline'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Review" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='book reviews'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Review" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='classical article'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Review" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='review'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Review" />
				</xsl:when>
			
				<!-- Conference papers -->
				<xsl:when test="translate(string($pbType),$up,$lo)='addresses'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='clinical conference'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='congresses'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='consensus development conference'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
				</xsl:when>
				<xsl:when test="translate(string($pbType),$up,$lo)='consensus development conference, nih'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
				</xsl:when>
		
				<!-- Editorials -->
				<xsl:when test="translate(string($pbType),$up,$lo)='editorial'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#EditorialArticle" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#EditorialArticle" />
				</xsl:when>
				
				<!-- Comments -->
				<xsl:when test="translate(string($pbType),$up,$lo)='comment'">
					<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Comment" />
					<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Comment" />
				</xsl:when>
				
				<!-- Reference Sources -->
				<xsl:when test="translate(string($pbType),$up,$lo)='atlases'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='dictionary'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='directory'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='encyclopedias'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>
	
				<xsl:when test="translate(string($pbType),$up,$lo)='guidebooks'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='handbooks'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='indexes'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='periodical index'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='pharmacopoeias'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
				</xsl:when>

				<!-- Image -->
				<xsl:when test="translate(string($pbType),$up,$lo)='charts'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Image" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='drawings'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Image" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='portraits'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Image" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Image" />
				</xsl:when>
				
				<!-- Book -->
				<xsl:when test="translate(string($pbType),$up,$lo)='cookbooks'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Book" />
				</xsl:when>
	
				<xsl:when test="translate(string($pbType),$up,$lo)='diaries'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Book" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='textbooks'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Book" />
				</xsl:when>

				<!-- Misc -->
				<xsl:when test="translate(string($pbType),$up,$lo)='database'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Database" />
					<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Database" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='documentaries and factual films'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Film" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Film" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='guideline'">
					<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Guideline" />
					<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Guideline" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='legal cases'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/LegalCaseDocument" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/LegalCaseDocument" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='legislation'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Legislation" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Legislation" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='maps'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Map" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Map" />
				</xsl:when>
	
				<xsl:when test="translate(string($pbType),$up,$lo)='meeting abstracts'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/ConferenceProceedings" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/ConferenceProceedings" />
				</xsl:when>
	
				<xsl:when test="translate(string($pbType),$up,$lo)='patents'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Patent" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Patent" />
				</xsl:when>

				<xsl:when test="translate(string($pbType),$up,$lo)='periodicals'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Periodicals" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Periodicals" />
				</xsl:when>
				
				<xsl:when test="translate(string($pbType),$up,$lo)='technical report'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Report" />
					<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Report" />
				</xsl:when>

				<!-- informationResourceSupportedBy -->
				<xsl:when test="contains(translate(string($pbType),$up,$lo), 'research support')">
					<xsl:variable name="supportedByLink" select="replace(translate(string($pbType),$up,$lo),'[^a-zA-Z0-9]','')" />
					<core:informationResourceSupportedBy rdf:resource="http://vivo.med.cornell.edu/individual/org-{$supportedByLink}"/>
				</xsl:when>

				<!-- Does not belong to journal article or any of the above  -->
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="not(contains(translate($pbType,$up,$lo), 'journal article')) and not(contains(translate(string($pbType),$up,$lo), 'research support'))">
							<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
							<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Article" />
						</xsl:when>
					</xsl:choose>
				</xsl:otherwise>
				
			</xsl:choose>
				
			<xsl:choose>
				<xsl:when test="not(contains(translate(string($pbType),$up,$lo), 'research support'))">
					<core:freetextKeyword><xsl:value-of select="$pbType"/></core:freetextKeyword>
				</xsl:when>
			</xsl:choose>

			</xsl:when>
			
			<!-- In press article -->
			<xsl:otherwise>
				<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#InPress" />
				<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#InPress" />
			</xsl:otherwise>

		</xsl:choose>
	</xsl:template>

	<!-- The main Entry of all scopus citations loaded 
		This serves as the header of the RDF file produced
	 -->
	<xsl:template match="/entry">
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
			xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
			xmlns:prism='http://prismstandard.org/namespaces/basic/2.0/'
			xmlns:wcmc='http://weill.cornell.edu/vivo/ontology/wcmc#'>
			<xsl:variable name="scopusId"><xsl:value-of select="translate(dc:identifier, 'SCOPUS_ID:', '')" /></xsl:variable>
			<xsl:variable name="modIssn"><xsl:value-of select="concat(substring(prism:issn, 0, 5), '-', substring(prism:issn, 5))" /></xsl:variable>
			
			<!-- Publication -->
			<rdf:Description rdf:about="{$customURI}pubid{$scopusId}">
				<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
				<!-- publication type: START -->
				<xsl:variable name="pbType" select="subtype"/>
				<xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ '"/>
				<xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz '"/>
				<xsl:choose>
					<xsl:when test="translate($pbType,$up,$lo)='ip'">
						<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#InPress" />
						<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#InPress" />
						<bibo:status rdf:resource="http://vivoweb.org/ontology/core#inPress"/>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='ar'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Research article</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='cp'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
						<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Conference paper</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='ed'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#EditorialArticle" />
						<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#EditorialArticle" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Editorial</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='le'">
						<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Letter" />
						<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Letter" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Letter</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='no'">
						<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Comment" />
						<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Comment" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Comment</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='pr'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NewsRelease" />
						<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#NewsRelease" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Press release</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='re'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Report" />
						<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Report" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Report</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='bk'">
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
						<vitro:mostSpecificType rdf:resource="http://purl.org/ontology/bibo/Book" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Book</core:freetextKeyword>
					</xsl:when>
					<xsl:when test="translate($pbType,$up,$lo)='er'">
						<rdf:type rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Erratum" />
						<vitro:mostSpecificType rdf:resource="http://weill.cornell.edu/vivo/ontology/wcmc#Erratum" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
						<core:freetextKeyword>Erratum</core:freetextKeyword>
					</xsl:when>
					<xsl:otherwise>
						<rdf:type rdf:resource="http://purl.org/ontology/bibo/Article" />
						<bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>

						<xsl:choose>
							<xsl:when test="translate($pbType,$up,$lo)='sh'">
								<core:freetextKeyword>Short Survey</core:freetextKeyword>
							</xsl:when>
							<xsl:when test="translate($pbType,$up,$lo)='ab'">
								<core:freetextKeyword>Abstract Report</core:freetextKeyword>
							</xsl:when>
							<xsl:when test="translate($pbType,$up,$lo)='bz'">
								<core:freetextKeyword>Business Article</core:freetextKeyword>
							</xsl:when>
							<xsl:when test="translate($pbType,$up,$lo)='cr'">
								<core:freetextKeyword>Conference Review</core:freetextKeyword>
							</xsl:when>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>

				<!-- publication type: END -->
				<score:pubId><xsl:value-of select="$scopusId" /></score:pubId>
				<wcmc:scopusDocId><xsl:value-of select="$scopusId" /></wcmc:scopusDocId>
				<bibo:doi><xsl:value-of select="prism:doi" /></bibo:doi>
				<rdfs:label><xsl:value-of select="dc:title" /></rdfs:label>
				<core:Title><xsl:value-of select="dc:title" /></core:Title>
				<bibo:volume><xsl:value-of select="prism:volume"/></bibo:volume>
				<bibo:pageStart><xsl:value-of select="substring-before(prism:pageRange, '-')" /></bibo:pageStart>
				<bibo:pageEnd><xsl:value-of select="substring-after(prism:pageRange, '-')" /></bibo:pageEnd>
				<core:hasPublicationVenue rdf:resource="{$customURI}journal{$modIssn}" />
				<!-- scopus author ref: START -->
				<xsl:apply-templates select="author" mode="authorRef">
					<xsl:with-param name="pubId" select="$scopusId" />
				</xsl:apply-templates>
				<!-- scopus author ref: END -->
				<core:dateTimeValue>
				<xsl:variable name="pubdate"><xsl:value-of select="translate(prism:coverDate, '-', '')" /></xsl:variable>
				<rdf:Description rdf:about="{$baseURI}pub/datetime{$pubdate}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
					<core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
					<core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="prism:coverDate"/>T00:00:00</core:dateTime>
				</rdf:Description>
				</core:dateTimeValue>
			</rdf:Description>
			
			<!-- Journal -->
			<rdf:Description rdf:about="{$customURI}journal{$modIssn}" >
				<ufVivo:harvestedBy>Scopus-Pubmed-Harvester</ufVivo:harvestedBy>
				<rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal" />
				<core:Title><xsl:value-of select="prism:publicationName" /></core:Title>
				<rdfs:label><xsl:value-of select="prism:publicationName" /></rdfs:label>
				<bibo:issn><xsl:value-of select="$modIssn"/></bibo:issn>
				<core:publicationVenueFor rdf:resource="{$customURI}pubid{$scopusId}"/>
			</rdf:Description>	
			
			<!-- scopus author full: START -->
			<xsl:apply-templates select="author" mode="fullAuthor">
				<xsl:with-param name="pubId" select="$scopusId" />
			</xsl:apply-templates>
			<!-- scopus author full: START -->
		</rdf:RDF>
	</xsl:template>

</xsl:stylesheet>
