<!-- Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, 
	please see the AUTHORS file provided. All rights reserved. This program and 
	the accompanying materials are made available under the terms of the new 
	BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html -->
<!-- Header information for the Style Sheet The style sheet requires xmlns 
	for each prefix you use in constructing the new elements -->

<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:core='http://vivoweb.org/ontology/core#' xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:ctsaip='http://vivo.ufl.edu/ontology/ctsaip/' xmlns:vitroApp='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
	xmlns:pvs='http://vivoweb.org/ontology/provenance-support#'>
	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/ip/</xsl:variable>

	<xsl:template match="all-technology">
		<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
			xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:core='http://vivoweb.org/ontology/core#'
			xmlns:score='http://vivoweb.org/ontology/score#' xmlns:foaf='http://xmlns.com/foaf/0.1/'
			xmlns:bibo='http://purl.org/ontology/bibo/' xmlns:ctsaip='http://vivo.ufl.edu/ontology/ctsaip/'
			xmlns:vitroApp='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'>
			<xsl:apply-templates select="technology" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="technology">
		<xsl:variable name="ctsai_id">
			<xsl:if test="normalize-space( ctsaip-link )">
				<xsl:analyze-string select="ctsaip-link" regex="\d+">
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(0)" />
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:if>
		</xsl:variable>

		<xsl:variable name="summary-var" select="summary" />
		<xsl:variable name="description-var" select="description" />
		<xsl:variable name="institution" select="instituion" />
		<xsl:variable name="email" select="contact-email" />
		<xsl:variable name="advantage-var" select="advantage" />
						
		<!-- Parse Inventor -->			
		<xsl:variable name="firstName">
			<xsl:call-template name ="parseFirstName">						
				<xsl:with-param name ="first" select="inventor-first-name"/>				
				<xsl:with-param name ="last" select="inventor-last-name"/>
			</xsl:call-template>					
		</xsl:variable>	
		
		<xsl:variable name="middleName">
			<xsl:call-template name ="parseMiddleName">						
				<xsl:with-param name ="first" select="inventor-first-name"/>				
				<xsl:with-param name ="last" select="inventor-last-name"/>
			</xsl:call-template>					
		</xsl:variable>	
		
		<xsl:variable name="lastName">
				<xsl:call-template name ="parseLastName">						
				<xsl:with-param name ="first" select="inventor-first-name"/>				
				<xsl:with-param name ="last" select="inventor-last-name"/>
			</xsl:call-template>					
		</xsl:variable>	 
		
		<xsl:variable name="inventor-var">
			<xsl:choose>
				<xsl:when test="$firstName !=''">
					<xsl:value-of select="$lastName" />,<xsl:value-of select="$firstName" />
					<xsl:text> </xsl:text><xsl:value-of select="$middleName" />		
				</xsl:when>
				<xsl:otherwise>					
					<xsl:call-template name ="extractInventors">
						<xsl:with-param name ="org" select="instituion"/>
						<xsl:with-param name ="summary" select="summary"/>
						<xsl:with-param name ="description" select="description"/>
					</xsl:call-template>					
				</xsl:otherwise>
			</xsl:choose>	
		</xsl:variable>	
		
		<!-- Innovations -->
		<rdf:Description rdf:about="{$baseURI}tech/{$ctsai_id}">
			<xsl:choose>
				<xsl:when test='type="Technology"'>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Technology" />
					<vitroApp:mostSpecificType
						rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Technology" />
				</xsl:when>
				<xsl:when test='type="Research Tool"'>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/ResearchTool" />
					<vitroApp:mostSpecificType
						rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/ResearchTool" />
				</xsl:when>
				<xsl:when test='type="Material"'>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Material" />
					<vitroApp:mostSpecificType
						rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Material" />
				</xsl:when>
				<xsl:otherwise>
					<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Innovation" />
					<vitroApp:mostSpecificType
						rdf:resource="http://vivo.ufl.edu/ontology/ctsaip/Innovation" />
				</xsl:otherwise>
			</xsl:choose>

			<rdfs:label><xsl:value-of select="title" /></rdfs:label>

			<!-- We test on normalize space to keep from adding blank properties where 
				there is no information present -->
			<xsl:if test="normalize-space( institution-tech-id )">
				<ctsaip:internalCaseNo><xsl:value-of select="institution-tech-id" /></ctsaip:internalCaseNo>
			</xsl:if>
			<xsl:if test="normalize-space( institution-link )">
				<core:webpage rdf:resource="{$baseURI}tech/{$ctsai_id}/inslink" />
			</xsl:if>
			<xsl:if test="normalize-space( ctsaip-link )">
				<core:webpage><xsl:value-of select="ctsaip-link" /></core:webpage>
			</xsl:if>
			<xsl:if test="normalize-space( advantage )">
				<ctsaip:advantages><xsl:value-of select="replace($advantage-var,'&lt;/? ?[a-xA-X0-9]*/?&gt;','')" /></ctsaip:advantages>
			</xsl:if>
			<xsl:if test="normalize-space( status )">
				<bibo:status><xsl:value-of select="status" /></bibo:status>
			</xsl:if>
			<xsl:if test="normalize-space( keywords )">			
				<xsl:for-each select="tokenize(keywords, ',')">				
					<core:freetextKeyword><xsl:value-of select="."/></core:freetextKeyword>
				</xsl:for-each>
			</xsl:if>
						
			<bibo:abstract><xsl:value-of select="replace($summary-var,'&lt;/? ?[a-xA-X0-9]*/?&gt;','')" /></bibo:abstract>
			<core:description><xsl:value-of select="replace($description-var,'&lt;/? ?[a-xA-X0-9]*/?&gt;','')" /></core:description>

			<!-- Link organization, case manager, and inventor assigned 
				to this project -->
			<xsl:if test="normalize-space(contact-name)">
				<ctsaip:caseManager rdf:resource="{$baseURI}casemngr/{$email}" />
			</xsl:if>
			<ctsaip:originatingInstitution rdf:resource="{$baseURI}institution/{$institution}" />
			<core:informationResourceInAuthorship rdf:resource="{$baseURI}authorship/{$ctsai_id}"/>				
		</rdf:Description>
		
		<!-- Web Links (new Structure in VIVO) -->
		<xsl:if test="normalize-space( institution-link )">
			<rdf:Description rdf:about="{$baseURI}tech/{$ctsai_id}/inslink">
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
				<core:linkAnchorText><xsl:value-of select="instituion" /> Page</core:linkAnchorText>
				<core:linkURI><xsl:value-of select="institution-link" /></core:linkURI>
				<core:rank>1</core:rank>
				<core:webpageOf rdf:resource="{$baseURI}tech/{$ctsai_id}"/>
			</rdf:Description>
		</xsl:if>

		<!-- The Institution -->
		<rdf:Description rdf:about="{$baseURI}institution/{$institution}">
			<rdfs:label>
				<xsl:value-of select="instituion" />
			</rdfs:label>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
			<ctsaip:originatingInstitutionFor
				rdf:resource="{$baseURI}tech/{$ctsai_id}" />
		</rdf:Description>

		<!-- Case Manager -->
		<xsl:if test="normalize-space(contact-name)">
			<rdf:Description rdf:about="{$baseURI}casemngr/{$email}">
				<rdfs:label>
					<xsl:value-of select="contact-name" />
				</rdfs:label>
				<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
				<core:email>
					<xsl:value-of select="contact-email" />
				</core:email>
				<ctsaip:caseManagerFor rdf:resource="{$baseURI}tech/{$ctsai_id}" />
			</rdf:Description>
		</xsl:if>
		
		<!-- Inventor (Authorship) -->	
		<xsl:if test="normalize-space( $inventor-var )">
			<rdf:Description rdf:about="{$baseURI}authorship/{$ctsai_id}">
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
				<core:linkedAuthor rdf:resource="{$baseURI}author/{$ctsai_id}"/>
				<core:linkedInformationResource rdf:resource="{$baseURI}tech/{$ctsai_id}"/>
				<rdfs:label>Authorship for <xsl:value-of select="$inventor-var" />
				</rdfs:label>
			</rdf:Description>		
			
			<!-- Inventor (Author) -->
			<rdf:Description rdf:about="{$baseURI}author/{$ctsai_id}">	
				<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
				<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
				<rdfs:label><xsl:value-of select="$inventor-var" /></rdfs:label>
				<foaf:firstName>
					<xsl:value-of select="$firstName" />
				</foaf:firstName>
				<xsl:if test="$middleName != ''">
				<core:middleName>
					<xsl:value-of select="$middleName" />
				</core:middleName>
				</xsl:if>
				<foaf:lastName>
					<xsl:value-of select="$lastName" />
				</foaf:lastName>
				<core:linkedInformationResourceInAuthorship
					rdf:resource="{$baseURI}tech/{$ctsai_id}" />
				<core:authorInAuthorship rdf:resource="{$baseURI}authorship/{$ctsai_id}"/>
			</rdf:Description>
		</xsl:if>
	</xsl:template>


	<!-- Template to Extract Inventors -->
	<xsl:template name="extractInventors">
		<xsl:param name="org" />
		<xsl:param name="summary" />
		<xsl:param name="description" />

		<xsl:choose>
		<!-- Tufts University -->
			<xsl:when test="$org = 'Tufts University'">
				<xsl:variable name="match" >
					<xsl:analyze-string select="$summary"
						regex="[I|i]nventor(s)?:\s*([^&lt;]*)">
						<xsl:matching-substring>	
						<xsl:value-of select="regex-group(2)" />
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:variable>		
							
				<xsl:if test="$match != ''">
					<xsl:value-of select="$match"/>
				</xsl:if>	
				 <xsl:if test="$match = ''">
					<xsl:analyze-string select="$summary"
					regex="[I|i]nventor(s)?:[&lt;]BR[&gt;]\s*([^&lt;]*)">
						<xsl:matching-substring>
						<xsl:value-of select="regex-group(2)" />
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:if>
			</xsl:when>		
			<!-- Columbia University -->
			 <xsl:when test="$org = 'Columbia University'"> 
				 <xsl:analyze-string 
					select="$description" regex="[I|i]nventor[s]?:\s*(.*?)[&lt;]br"> 
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(1)" />
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:when>	
					
			<!-- Vanderbilt University -->
			 <xsl:when test="$org = 'Vanderbilt University'"> 
				 <xsl:analyze-string 
					select="$summary" regex="Inventors[&lt;]/b[&gt;][&lt;]br[&gt;](.*?)[&lt;]br[&gt;][&lt;]/span"> 
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(1)" />
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:when>					
		</xsl:choose>
	</xsl:template>	
	
	<!-- Template to Parse First Name -->
	<xsl:template name="parseFirstName">
		<xsl:param name="first" />
		<xsl:param name="last" />
		
		<!-- remove parenthesized words -->
		<xsl:variable name="first" select="replace($first,' \(.*\)','')"/> 
		<!-- remove "et al." -->
		<xsl:variable name="first" select="replace($first, ' et al.','')"/>
		<!-- remove a href links -->
		<xsl:variable name="first" select="replace($first,'&lt;(.*)','')"/>
		
		
		<xsl:if test="($first != '') and not(contains($first, ',')) and not(contains($first, 'and'))">
			<xsl:choose>
				<xsl:when test="$last != ''">
					<xsl:value-of select="$first" />
				</xsl:when>
				<xsl:otherwise>
				 <xsl:analyze-string select="$first" regex="(\w+)\s(\w+[.]?)(\s(\w+))?"> 
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(1)" />
					</xsl:matching-substring>
				</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>	
	</xsl:template>
	
		
	<!-- Template to Parse Middle Name -->
	<xsl:template name="parseMiddleName">
		<xsl:param name="first" />
		<xsl:param name="last" />
		
		<!-- remove parenthesized words -->
		<xsl:variable name="first" select="replace($first,' \(.*\)','')"/> 
		<!-- remove "et al." -->
		<xsl:variable name="first" select="replace($first, ' et al.','')"/>
		<!-- remove a href links -->
		<xsl:variable name="first" select="replace($first,'&lt;(.*)','')"/>
		
		<xsl:if test="($first != '') and not(contains($first, ',')) and not(contains($first, 'and'))">
			<xsl:if test="$last = ''">
				 <xsl:analyze-string select="$first" regex="(\w+)\s(\w+[.]?)(\s(\w+))?"> 
					<xsl:matching-substring>
						<xsl:if test="regex-group(4) != ''">
							<xsl:value-of select="regex-group(2)" />
						</xsl:if>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:if>
		</xsl:if>	
	</xsl:template>						
	
	<!-- Template to Parse Last Name -->
	<xsl:template name="parseLastName">
		<xsl:param name="last" />
		<xsl:param name="first"/>
		
		<!-- remove parenthesized words -->
		<xsl:variable name="first" select="replace($first,' \(.*\)','')"/> 
		<!-- remove "et al." -->
		<xsl:variable name="first" select="replace($first, ' et al.','')"/>
		<!-- remove a href links -->
		<xsl:variable name="first" select="replace($first,'&lt;(.*)','')"/>
		
		<xsl:if test="($first != '') and not(contains($first, ',')) and not(contains($first, 'and'))">
			<xsl:choose>
				<xsl:when test="$last != ''">
					<xsl:value-of select="$last" />
				</xsl:when>
				<xsl:otherwise>
				 <xsl:analyze-string select="$first" regex=".*?\s([^ ]*)$"> 
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(1)" />
					</xsl:matching-substring>
				</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>	
	</xsl:template>


</xsl:stylesheet>
