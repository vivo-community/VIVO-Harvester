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
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:localVivo="http://vivo.sample.edu/ontology/"
	xmlns:db-people="http://vivo.example.com/harvest/example/jdbc/fields/people/"
	xmlns:db-positions="http://vivo.example.com/harvest/example/jdbc/fields/positions/"
	xmlns:db-organizations="http://vivo.example.com/harvest/example/jdbc/fields/organizations/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>
<!--
	xmlns:db-people="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/people/"
	xmlns:db-positions="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/positions/"
-->
	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/example/jdbc/</xsl:variable>

	<!-- The main node of the record loaded 
		This serves as the header of the RDF file produced
	 -->
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			xmlns:localVivo="http://vivo.sample.edu/ontology/"
			xmlns:foaf="http://xmlns.com/foaf/0.1/"
			xmlns:owl="http://www.w3.org/2002/07/owl#"
			xmlns:core="http://vivoweb.org/ontology/core#"
			xmlns:score="http://vivoweb.org/ontology/score#"
			xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" >
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match="rdf:Description">
		<xsl:variable name="this" select="." />
		<xsl:variable name="table">
			<xsl:analyze-string select="../@xml:base" regex="^.*/([^/]+?)$">
				<xsl:matching-substring>
					<xsl:value-of select="regex-group(1)" />
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$table = 'people'">
				<xsl:call-template name="people">
					<xsl:with-param name="this" select="$this" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$table = 'positions'">
				<xsl:call-template name="positions">
					<xsl:with-param name="this" select="$this" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$table = 'organizations'">
				<xsl:call-template name="organizations">
					<xsl:with-param name="this" select="$this" />
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="people">
		<xsl:param name='this' />
		<xsl:variable name="uid" select="$this/db-people:UID"/>
		<rdf:Description rdf:about="{$baseURI}person/uid{$uid}">
			<localVivo:uId rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$uid"/></localVivo:uId>
			<localVivo:harvestedBy>Example.JDBCFetch-Harvester</localVivo:harvestedBy>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
			<core:email><xsl:value-of select="$this/db-people:EMAIL" /></core:email>
			<xsl:if test="normalize-space( $this/db-people:PHONE )">
				<core:phoneNumber><xsl:value-of select="$this/db-people:PHONE"/></core:phoneNumber>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:FAX )">
				<core:faxNumber><xsl:value-of select="$this/db-people:FAX"/></core:faxNumber>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:FNAME )">
				<foaf:firstName><xsl:value-of select="$this/db-people:FNAME"/></foaf:firstName>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:LNAME )">
				<foaf:lastName><xsl:value-of select="$this/db-people:LNAME"/></foaf:lastName>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:MNAME )">
				<core:middleName><xsl:value-of select="$this/db-people:MNAME"/></core:middleName>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:PRENAME )">
				<bibo:prefixName><xsl:value-of select="$this/db-people:PRENAME"/></bibo:prefixName>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:SUFNAME )">
				<bibo:suffixName><xsl:value-of select="$this/db-people:SUFNAME"/></bibo:suffixName>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:FULLNAME )">
				<rdfs:label><xsl:value-of select="$this/db-people:FULLNAME"/></rdfs:label>
			</xsl:if>
			<xsl:if test="normalize-space( $this/db-people:NETID )">
				<localVivo:netId rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-people:NETID"/></localVivo:netId>
			</xsl:if>
		    <xsl:if test="normalize-space( $this/db-people:TITLE )">
				<core:overview><xsl:value-of select="$this/db-people:TITLE"/></core:overview>
			</xsl:if>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="positions">
		<xsl:param name='this' />
		<xsl:variable name="uid" select="$this/db-positions:UID"/>
		<xsl:variable name="fullorgnum" select="$this/db-positions:DEPTID"/>
		<xsl:variable name="typeCode" select="$this/db-positions:TYPE"/>
		<xsl:variable name="startDate" select="$this/db-positions:STARTDATE"/>
		<xsl:variable name="startYear">
			<xsl:analyze-string select="$this/db-positions:STARTDATE" regex="^(....).*?$">
				<xsl:matching-substring>
					<xsl:value-of select="regex-group(1)"/>
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:variable>
		<rdf:Description rdf:about="{$baseURI}person/uid{$uid}">
			<localVivo:harvestedBy>Example.JDBCFetch-Harvester</localVivo:harvestedBy>
			<xsl:choose>
				<xsl:when test="$typeCode=121">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
					<core:currentlyHeadOf rdf:resource="{$baseURI}org/org{$fullorgnum}"/>
				</xsl:when>
				<xsl:when test="$typeCode=122">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
				</xsl:when>
				<xsl:when test="$typeCode=254">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
				</xsl:when>
				<xsl:when test="$typeCode=392">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic"/>
				</xsl:when>
				<xsl:when test="$typeCode=393">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic"/>
				</xsl:when>
			</xsl:choose>
			<core:personInPosition>
				<rdf:Description rdf:about="{$baseURI}position/positionFor{$uid}in{$fullorgnum}as{$typeCode}from{$startDate}">
					<localVivo:harvestedBy>Example.JDBCFetch-Harvester</localVivo:harvestedBy>
					<rdfs:label><xsl:value-of select="$this/db-positions:WORKTITLE"/></rdfs:label>
					<xsl:choose>
						<xsl:when test="$typeCode=121">
							<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyAdministrativePosition"/>
						</xsl:when>
						<xsl:when test="$typeCode=122">
							<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyAdministrativePosition"/>
						</xsl:when>
						<xsl:when test="$typeCode=254">
							<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
						</xsl:when>
						<xsl:when test="$typeCode=392">
							<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
						</xsl:when>
						<xsl:when test="$typeCode=393">
							<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
						</xsl:when>
					</xsl:choose>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DependentResource"/>
					<localVivo:positionDeptId><xsl:value-of select="$fullorgnum"/></localVivo:positionDeptId>
					<core:positionInOrganization>
						<rdf:Description rdf:about="{$baseURI}org/org{$fullorgnum}">
							<localVivo:harvestedBy>Example.JDBCFetch-Harvester</localVivo:harvestedBy>
							<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
							<xsl:choose>
								<xsl:when test="$typeCode=121">
									<core:currentlyHeadedBy rdf:resource="{$baseURI}person/uid{$uid}"/>
								</xsl:when>
							</xsl:choose>
				    		<localVivo:deptId><xsl:value-of select="$fullorgnum"/></localVivo:deptId>
							<core:organizationForPosition rdf:resource="{$baseURI}position/positionFor{$uid}in{$fullorgnum}as{$typeCode}from{$startDate}"/>
						</rdf:Description>
					</core:positionInOrganization>
					<core:positionForPerson rdf:resource="{$baseURI}person/uid{$uid}"/>
					<core:startYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$startYear"/></core:startYear>
				</rdf:Description>
			</core:personInPosition>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="organizations">
		<xsl:param name='this' />
		<xsl:variable name="deptid" select="$this/db-organizations:DEPTID"/>
		<xsl:variable name="typeCode" select="$this/db-organizations:TYPE"/>
		<rdf:Description rdf:about="{$baseURI}org/org{$deptid}">
			<localVivo:deptId><xsl:value-of select="$deptid"/></localVivo:deptId>
			<localVivo:harvestedBy>Example.JDBCFetch-Harvester</localVivo:harvestedBy>
			<rdfs:label><xsl:value-of select="$this/db-organizations:NAME"/></rdfs:label>
			<xsl:choose>
				<xsl:when test="$typeCode=401">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#University"/>
				</xsl:when>
				<xsl:when test="$typeCode=402">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Division"/>
				</xsl:when>
				<xsl:when test="$typeCode=403">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Department"/>
				</xsl:when>
				<xsl:when test="$typeCode=404">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#College"/>
				</xsl:when>
				<xsl:when test="$typeCode=405">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Program"/>
				</xsl:when>
				<xsl:when test="$typeCode=406">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Institute"/>
				</xsl:when>
			</xsl:choose>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
			<xsl:if test="normalize-space( $this/db-organizations:SUPERDEPTID )">
				<xsl:variable name="superdeptid" select="$this/db-organizations:SUPERDEPTID"/>
				<core:subOrganizationWithin>
					<rdf:Description rdf:about="{$baseURI}org/org{$superdeptid}">
						<localVivo:deptId><xsl:value-of select="$superdeptid"/></localVivo:deptId>
						<localVivo:harvestedBy>Example.JDBCFetch-Harvester</localVivo:harvestedBy>
						<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
						<core:hasSubOrganization rdf:resource="{$baseURI}org/org{$deptid}"/>
					</rdf:Description>
				</core:subOrganizationWithin>
			</xsl:if>
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
