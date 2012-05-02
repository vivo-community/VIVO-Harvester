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

			<rdfs:label>
				<xsl:value-of select="title" />
			</rdfs:label>


			<!-- We test on normalize space to keep from adding blank properties where 
				there is no information present -->
			<xsl:if test="normalize-space( institution-tech-id )">
				<ctsaip:internalCaseNo>
					<xsl:value-of select="institution-tech-id" />
				</ctsaip:internalCaseNo>
			</xsl:if>
			<xsl:if test="normalize-space( institution-link )">
				<core:webpage>
					<xsl:value-of select="institution-link" />
				</core:webpage>
			</xsl:if>
			<xsl:if test="normalize-space( ctsaip-link )">
				<core:webpage>
					<xsl:value-of select="ctsaip-link" />
				</core:webpage>
			</xsl:if>
			<xsl:if test="normalize-space( advantage )">
				<ctsaip:advantages>
					<xsl:value-of select="advantage" />
				</ctsaip:advantages>
			</xsl:if>
			<xsl:if test="normalize-space( status )">
				<bibo:status>
					<xsl:value-of select="status" />
				</bibo:status>
			</xsl:if>
			<xsl:if test="normalize-space( inventor-first-name )">
				<bibo:status>
					<xsl:value-of select="inventor-first-name" />
				</bibo:status>
			</xsl:if>

			<bibo:abstract><xsl:value-of select="replace($summary-var,'&lt;/? ?[a-xA-X0-9]*/?&gt;','')" /></bibo:abstract>
			<core:description><xsl:value-of select="replace($description-var,'&lt;/? ?[a-xA-X0-9]*/?&gt;','')" /></core:description>

			<!-- Link organization, case manager, and inventor assigned 
				to this project -->
			<xsl:if test="normalize-space(contact-name)">
				<ctsaip:caseManager rdf:resource="{$baseURI}casemngr/{$email}" />
			</xsl:if>
			<ctsaip:originatingInstitution
				rdf:resource="{$baseURI}institution/{$institution}" />
			<core:informationResourceInAuthorship rdf:resource="{$baseURI}authorship/{$ctsai_id}"/>
	</rdf:Description>

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
		<rdf:Description rdf:about="{$baseURI}authorship/{$ctsai_id}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<core:linkedAuthor rdf:resource="{$baseURI}author/{$ctsai_id}"/>
			<core:linkedInformationResource rdf:resource="{$baseURI}tech/{$ctsai_id}"/>
			<rdfs:label>Authorship for 
				<xsl:choose>
					<xsl:when test="inventor-first-name !=''">
						<xsl:value-of select="inventor-first-name" /> <xsl:value-of select="inventor-last-name" />
					</xsl:when>
					<xsl:otherwise>						
						<xsl:call-template name ="extractInventors">
							<xsl:with-param name ="org" select="instituion"/>
							<xsl:with-param name ="text" select="summary"/>
						</xsl:call-template>					
					</xsl:otherwise>
				</xsl:choose>	
			</rdfs:label>
		</rdf:Description>		
		
		
		<!-- Inventor (Author) -->
		<rdf:Description rdf:about="{$baseURI}author/{$ctsai_id}">	
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
			<rdfs:label>
				<xsl:choose>
					<xsl:when test="inventor-first-name !=''">
						<xsl:value-of select="inventor-first-name" /> <xsl:value-of select="inventor-last-name" />
					</xsl:when>
					<xsl:otherwise>
				 	<!--	<xsl:analyze-string select="description"
				  			regex="Lead Inventors:\s*(&lt;a href=&quot;((.*)&quot;&gt;(.*))Problem|([^&lt;]*))">											
								<xsl:matching-substring>
									<xsl:value-of select="regex-group(0)" />
								<xsl:value-of select="regex-group(5)" />										
								</xsl:matching-substring>									
						</xsl:analyze-string>	-->
					</xsl:otherwise>
				</xsl:choose>	 		
			</rdfs:label>
			<foaf:firstName>
				<xsl:value-of select="inventor-first-name" />
			</foaf:firstName>
			<foaf:lastName>
				<xsl:value-of select="inventor-last-name" />
			</foaf:lastName>
			<core:linkedInformationResourceInAuthorship
				rdf:resource="{$baseURI}tech/{$ctsai_id}" />
			<core:authorInAuthorship rdf:resource="{$baseURI}authorship/{$ctsai_id}"/>
		</rdf:Description>
	</xsl:template>

	<!-- Template to Extract Inventors -->
	<xsl:template name="extractInventors">
		<xsl:param name="text" />
		<xsl:param name="org" />

	<xsl:choose>
		<xsl:when test="$org = 'Washington University in St. Louis'">
			<xsl:analyze-string select="$text"
				regex="[I|i]nventor[|s]:\s*(.*)Technology">
				<xsl:matching-substring>
					<xsl:value-of select="regex-group(1)" />
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:when>
		<xsl:when test="$org = 'Tufts University'">
			<xsl:analyze-string select="summary"
				regex="[I|i]nventor(|s):\s*((.*)(Problem|Background|Opportunity|Intellectual))">
				<xsl:matching-substring>
					<xsl:value-of select="regex-group(5)" />
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:when>
		
		<!-- Columbia University
				 Stopping Criteria: Tech|Description|Reference|Circuits|Problem|The|Technology 
		 <xsl:when test="$org = 'Columbia University'"> <xsl:analyze-string 
			select="$text" regex="[I|i]nventor(|s):\s*(((&lt;a href=&quot;((.*)&quot;&gt;(.*)))|(.*))(Tech|Problem$))"> 
			 (.*)&lt;br&gt;)|[^&lt;]*)"> <xsl:matching-substring> <xsl:choose> <xsl:when 
			test="regex-group(8) != ''"> <xsl:value-of select="regex-group(8)" /> </xsl:when> 
			<xsl:otherwise> <xsl:value-of select="regex-group(7)" /> </xsl:otherwise> 
			</xsl:choose> </xsl:matching-substring>
		</xsl:analyze-string> -->
		
	</xsl:choose>
	</xsl:template>	
</xsl:stylesheet>
