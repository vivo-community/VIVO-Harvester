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
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:ufl="http://vivo.ufl.edu/ontology/vivo-ufl/"
	xmlns:db-t_UF_DIR_EMP_STU_1="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_1/"
	xmlns:db-t_UF_DIR_EMP_STU_2="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_2/"
	xmlns:db-t_UF_DIR_EMP_STU_3="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_3/"
	xmlns:db-t_UF_DIR_EMP_STU_4="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_4/"
	xmlns:db-t_UF_DIR_EMP_STU_5="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_5/"
	xmlns:db-t_UF_DIR_EMP_STU_6="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_6/"
	xmlns:db-t_UF_PA_GL_ACCT="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PA_GL_ACCT/"
	xmlns:db-t_UF_PER_UFAU="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PER_UFAU/"
	xmlns:db-t_PS_H_UF_ACAD_ORG="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_PS_H_UF_ACAD_ORG/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivo.ufl.edu/individual/</xsl:variable>

	<!-- The main node of the record loaded 
		This serves as the header of the RDF file produced
	 -->
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			xmlns:ufl="http://vivo.ufl.edu/ontology/vivo-ufl/"
			xmlns:db-t_UF_DIR_EMP_STU_1="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_1/"
			xmlns:db-t_UF_DIR_EMP_STU_2="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_2/"
			xmlns:db-t_UF_DIR_EMP_STU_3="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_3/"
			xmlns:db-t_UF_DIR_EMP_STU_4="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_4/"
			xmlns:db-t_UF_DIR_EMP_STU_5="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_5/"
			xmlns:db-t_UF_DIR_EMP_STU_6="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_6/"
			xmlns:db-t_UF_PA_GL_ACCT="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PA_GL_ACCT/"
			xmlns:db-t_UF_PER_UFAU="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PER_UFAU/"
			xmlns:db-t_PS_H_UF_ACAD_ORG="jdbc:jtds:sqlserver://erp-prod-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_PS_H_UF_ACAD_ORG/"
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
		<xsl:analyze-string select="../@xml:base" regex="^.*/([^/]+?)$">
			<xsl:matching-substring>
				<xsl:variable name="table" select="regex-group(1)" />
				<xsl:variable name="rdfid" select="$this/@rdf:ID" />
				<xsl:analyze-string select="$rdfid" regex="^id_-_(.*?)(_-_.+)*?$">
					<xsl:matching-substring>
						<xsl:variable name="ufid" select="regex-group(1)" />
						<xsl:choose>
							<xsl:when test="$table = 't_UF_DIR_EMP_STU_1'">
								<xsl:call-template name="t_UF_DIR_EMP_STU_1">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_DIR_EMP_STU_2'">
								<xsl:call-template name="t_UF_DIR_EMP_STU_2">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_DIR_EMP_STU_3'">
								<xsl:call-template name="t_UF_DIR_EMP_STU_3">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_DIR_EMP_STU_4'">
								<xsl:call-template name="t_UF_DIR_EMP_STU_4">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_DIR_EMP_STU_5'">
								<xsl:call-template name="t_UF_DIR_EMP_STU_5">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_DIR_EMP_STU_6'">
								<xsl:call-template name="t_UF_DIR_EMP_STU_6">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_PA_GL_ACCT'">
								<xsl:call-template name="t_UF_PA_GL_ACCT">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 't_UF_PER_UFAU'">
								<xsl:call-template name="t_UF_PER_UFAU">
									<xsl:with-param name="ufid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<!-- <xsl:when test="$table = 't_PS_H_UF_ACAD_ORG'">
								<xsl:call-template name="t_PS_H_UF_ACAD_ORG">
									<xsl:with-param name="orgid" select="$ufid" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when> -->
						</xsl:choose>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:matching-substring>
		</xsl:analyze-string>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_1">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
			<rdfs:label xml:lang="en-US"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_NAME_TXT" /></rdfs:label>
			<ufl:businessName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_NAME_TXT" /></ufl:businessName>
			<core:workEmail><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_EMAIL" /></core:workEmail>
			<ufl:ufid rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$ufid" /></ufl:ufid>
			<!-- <core:preferredTitle rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_WORK_TITLE" /></core:preferredTitle> -->
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_2">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<xsl:if test="$this/db-t_UF_DIR_EMP_STU_2:UF_TYPE_CD = 10">
				<core:workPhone><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_AREA_CD"/><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_PHONE_NO"/></core:workPhone>
			</xsl:if>
			<xsl:if test="$this/db-t_UF_DIR_EMP_STU_2:UF_TYPE_CD = 242">
				<core:workFax><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_AREA_CD"/><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_PHONE_NO"/></core:workFax>
			</xsl:if>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_3">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<core:mailingAddress>
				<xsl:variable name="typeCode" select="$this/db-t_UF_DIR_EMP_STU_3:UF_TYPE_CD"/>
				<rdf:Description rdf:about="{$baseURI}addressFor{$ufid}type{$typeCode}">
					<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Address"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#AbstractInformation"/>
					<core:mailingAddressFor rdf:resource="{$baseURI}ufid{$ufid}"/>
				    <rdfs:label>
				    	<xsl:if test="string($this/db-t_UF_DIR_EMP_STU_3:UF_ADDR_LINE1)">
				    		<xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_3:UF_ADDR_LINE1" />
				    		<xsl:text> </xsl:text>
				    	</xsl:if>
				    	<xsl:if test="string($this/db-t_UF_DIR_EMP_STU_3:UF_ADDR_LINE2)">
				    		<xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_3:UF_ADDR_LINE2" />
				    		<xsl:text> </xsl:text>
				    	</xsl:if>
				    	<xsl:if test="string($this/db-t_UF_DIR_EMP_STU_3:UF_ADDR_LINE3)">
				    		<xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_3:UF_ADDR_LINE3" />
				    	</xsl:if>
				    </rdfs:label>
				    <core:addressState><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_3:UF_STATE_CD" /></core:addressState>
				    <core:addressPostalCode><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_3:UF_ZIP_CD" /></core:addressPostalCode>
				    <core:addressCity><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_3:UF_CITY" /></core:addressCity>
				</rdf:Description>
			</core:mailingAddress>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_4">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<xsl:variable name="typeCode" select="$this/db-t_UF_DIR_EMP_STU_4:UF_TYPE_CD"/>
			<xsl:choose>
				<xsl:when test="$typeCode=35">
					<foaf:firstName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></foaf:firstName>
				</xsl:when>
				<xsl:when test="$typeCode=36">
					<foaf:lastName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></foaf:lastName>
				</xsl:when>
				<xsl:when test="$typeCode=38">
					<bibo:prefixName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></bibo:prefixName>
				</xsl:when>
				<xsl:when test="$typeCode=232">
					<ufl:activeDirName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></ufl:activeDirName>
				</xsl:when>
			</xsl:choose>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_5">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<xsl:variable name="fullorgnum" select="$this/db-t_UF_DIR_EMP_STU_5:PS_DEPTID"/>
		<xsl:variable name="typeCode" select="$this/db-t_UF_DIR_EMP_STU_5:UF_TYPE_CD"/>
		<xsl:variable name="startYear"><xsl:analyze-string select="$this/db-t_UF_DIR_EMP_STU_5:UF_BEGIN_TS" regex="^(....).*?$"><xsl:matching-substring><xsl:value-of select="regex-group(1)"/></xsl:matching-substring></xsl:analyze-string></xsl:variable>
		<xsl:variable name="startDate"><xsl:analyze-string select="$this/db-t_UF_DIR_EMP_STU_5:UF_BEGIN_TS" regex="^(....-..-..).*?$"><xsl:matching-substring><xsl:value-of select="regex-group(1)"/></xsl:matching-substring></xsl:analyze-string></xsl:variable>
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<xsl:choose>
				<xsl:when test="$typeCode=192">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
				</xsl:when>
				<xsl:when test="$typeCode=193">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic"/>
				</xsl:when>
				<xsl:when test="$typeCode=194">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic"/>
				</xsl:when>
				<xsl:when test="$typeCode=195">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic"/>
				</xsl:when>
				<xsl:when test="$typeCode=197">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
				</xsl:when>
				<xsl:when test="$typeCode=200">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#EmeritusProfessor"/>
				</xsl:when>
				<xsl:when test="$typeCode=219">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
				</xsl:when>
			</xsl:choose>
			<core:personInPosition rdf:resource="{$baseURI}positionFor{$ufid}in{$fullorgnum}as{$typeCode}from{$startDate}"/>
		</rdf:Description>
		<!-- <rdf:Description rdf:about="{$baseURI}org{$fullorgnum}">
			<core:organizationForPosition rdf:resource="{$baseURI}positionFor{$ufid}in{$fullorgnum}as{$typeCode}from{$startDate}"/>
		</rdf:Description> -->
		<rdf:Description rdf:about="{$baseURI}positionFor{$ufid}in{$fullorgnum}as{$typeCode}from{$startDate}">
			<xsl:choose>
				<xsl:when test="$typeCode=192">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=193">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
					<rdfs:label>Staff</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=194">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
					<rdfs:label>Staff</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=195">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
					<rdfs:label>Staff</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=197">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=200">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=219">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:otherwise>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
					<rdfs:label>Employee</rdfs:label>
				</xsl:otherwise>
			</xsl:choose>
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
			<ufl:deptIDofPosition><xsl:value-of select="$fullorgnum"/></ufl:deptIDofPosition>
			<!-- <core:positionInOrganization rdf:resource="{$baseURI}org{$fullorgnum}"/> -->
			<core:positionForPerson rdf:resource="{$baseURI}ufid{$ufid}"/>
			<core:startYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$startYear"/></core:startYear>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_6">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<xsl:variable name="fullorgnum" select="$this/db-t_UF_DIR_EMP_STU_6:PS_DEPTID"/>
		<xsl:variable name="typeCode" select="$this/db-t_UF_DIR_EMP_STU_6:UF_TYPE_CD"/>
		<xsl:variable name="startYear"><xsl:analyze-string select="$this/db-t_UF_DIR_EMP_STU_6:UF_BEGIN_TS" regex="^(....).*?$"><xsl:matching-substring><xsl:value-of select="regex-group(1)"/></xsl:matching-substring></xsl:analyze-string></xsl:variable>
		<xsl:variable name="endYear"><xsl:analyze-string select="$this/db-t_UF_DIR_EMP_STU_6:UF_END_TS" regex="^(....).*?$"><xsl:matching-substring><xsl:value-of select="regex-group(1)"/></xsl:matching-substring></xsl:analyze-string></xsl:variable>
		<xsl:variable name="startDate"><xsl:analyze-string select="$this/db-t_UF_DIR_EMP_STU_6:UF_BEGIN_TS" regex="^(....-..-..).*?$"><xsl:matching-substring><xsl:value-of select="regex-group(1)"/></xsl:matching-substring></xsl:analyze-string></xsl:variable>
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<core:personInPosition rdf:resource="{$baseURI}positionFor{$ufid}in{$fullorgnum}as{$typeCode}from{$startDate}"/>
		</rdf:Description>
		<!-- <rdf:Description rdf:about="{$baseURI}org{$fullorgnum}">
			<core:organizationForPosition rdf:resource="{$baseURI}positionFor{$ufid}in{$fullorgnum}as{$typeCode}from{$startDate}"/>
		</rdf:Description> -->
		<rdf:Description rdf:about="{$baseURI}positionFor{$ufid}in{$fullorgnum}as{$typeCode}from{$startDate}">
			<xsl:choose>
				<xsl:when test="$typeCode=192">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=193">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
					<rdfs:label>Staff</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=194">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
					<rdfs:label>Staff</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=195">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition"/>
					<rdfs:label>Staff</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=197">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=200">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:when test="$typeCode=219">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
					<rdfs:label>Faculty</rdfs:label>
				</xsl:when>
				<xsl:otherwise>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
					<rdfs:label>Employee</rdfs:label>
				</xsl:otherwise>
			</xsl:choose>
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
			<ufl:deptIDofPosition><xsl:value-of select="$fullorgnum"/></ufl:deptIDofPosition>
			<!-- <core:positionInOrganization rdf:resource="{$baseURI}org{$fullorgnum}"/> -->
			<core:positionForPerson rdf:resource="{$baseURI}ufid{$ufid}"/>
			<core:startYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$startYear"/></core:startYear>
			<core:endYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$endYear"/></core:endYear>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_PA_GL_ACCT">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
			<ufl:gatorlink rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-t_UF_PA_GL_ACCT:USERIDALIAS"/></ufl:gatorlink>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_PER_UFAU">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}ufid{$ufid}">
		    <core:overview><xsl:value-of select="$this/db-t_UF_PER_UFAU:UF_JOB_LONG"/></core:overview>
		    <ufl:deptID><xsl:value-of select="$this/db-t_UF_PER_UFAU:UF_PS_DEPTID"/></ufl:deptID>
		</rdf:Description>
	</xsl:template>
	
	<!-- <xsl:template name="t_PS_H_UF_ACAD_ORG">
		<xsl:param name='orgid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="{$baseURI}org{$orgid}">
			<rdfs:label xml:lang="en-US"><xsl:value-of select="$this/db-t_PS_H_UF_ACAD_ORG:DTL_DESC"/></rdfs:label>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
		    <ufl:deptID><xsl:value-of select="$orgid"/></ufl:deptID>
			<xsl:analyze-string select="$orgid" regex="^(..)(..)(..)(..).*?$">
				<xsl:matching-substring>
					<xsl:variable name="p1" select="regex-group(1)"/>
					<xsl:variable name="p2" select="regex-group(2)"/>
					<xsl:variable name="p3" select="regex-group(3)"/>
					<xsl:variable name="p4" select="regex-group(4)"/>
					<xsl:variable name="L2desc" select="$this/db-t_PS_H_UF_ACAD_ORG:L2_DESC"/>
					<xsl:variable name="L3desc" select="$this/db-t_PS_H_UF_ACAD_ORG:L3_DESC"/>
					<xsl:variable name="L4desc" select="$this/db-t_PS_H_UF_ACAD_ORG:L4_DESC"/>
					<xsl:variable name="L2id" select="$this/db-t_PS_H_UF_ACAD_ORG:L2_ID"/>
					<xsl:variable name="L3id" select="$this/db-t_PS_H_UF_ACAD_ORG:L3_ID"/>
					<xsl:variable name="L4id" select="$this/db-t_PS_H_UF_ACAD_ORG:L4_ID"/>
					<xsl:variable name="p1xs"><xsl:value-of select="$p1"/>XXXXXX</xsl:variable>
					<xsl:variable name="p1p2xs"><xsl:value-of select="$p1"/><xsl:value-of select="$p2"/>XXXX</xsl:variable>
					<xsl:variable name="p1p2p3xs"><xsl:value-of select="$p1"/><xsl:value-of select="$p2"/><xsl:value-of select="$p3"/>XX</xsl:variable>
					<xsl:variable name="p1zeros">org<xsl:value-of select="$p1"/>000000</xsl:variable>
					<xsl:variable name="p1p2zeros">org<xsl:value-of select="$p1"/><xsl:value-of select="$p2"/>0000</xsl:variable>
					<xsl:variable name="p1p2p3zeros">org<xsl:value-of select="$p1"/><xsl:value-of select="$p2"/><xsl:value-of select="$p3"/>00</xsl:variable>
					<xsl:variable name="uf">UniversityofFlorida</xsl:variable>
					<xsl:variable name="level">
						<xsl:choose><xsl:when test="$p4!='00'">
							<xsl:value-of select="4"/>
						</xsl:when><xsl:when test="$p3!='00'">
							<xsl:value-of select="3"/>
						</xsl:when><xsl:when test="$p2!='00'">
							<xsl:value-of select="2"/>
						</xsl:when><xsl:otherwise>
							<xsl:value-of select="1"/>
						</xsl:otherwise></xsl:choose>
					</xsl:variable>
					<xsl:variable name="superorg">
						<xsl:choose><! - - <xsl:when test="$level=4">
							<xsl:choose><xsl:when test="$L4id=$p1p2p3xs">
								<xsl:value-of select="$p1p2p3zeros"/>
							</xsl:when><xsl:when test="$L3id=$p1p2xs">
								<xsl:value-of select="$p1p2zeros"/>
							</xsl:when><xsl:when test="$L2id=$p1xs">
								<xsl:value-of select="$p1zeros"/>
							</xsl:when><xsl:otherwise>
								<xsl:value-of select="$uf"/>
							</xsl:otherwise></xsl:choose>
						</xsl:when><xsl:when test="$level=3">
							</xsl:when><xsl:when test="$L3id=$p1p2xs">
								<xsl:value-of select="$p1p2zeros"/>
							</xsl:when><xsl:when test="$L2id=$p1xs">
								<xsl:value-of select="$p1zeros"/>
							</xsl:when><xsl:otherwise>
								<xsl:value-of select="$uf"/>
							</xsl:otherwise></xsl:choose>
						</xsl:when>
						
						
						
						<xsl:when test="$level=3 and $L4id!=$p1p2p3xs and $L3id=$p1p2xs">
							<xsl:value-of select="$p1p2zeros"/>
						</xsl:when><xsl:when test="$level=2 and $L4id!=$p1p2p3xs and $L3id!=$p1p2xs and $L2id=$p1xs">
							<xsl:value-of select="$p1zeros"/>
						</xsl:when>- - >
						<xsl:when test="$level!=1">
							<xsl:value-of select="$p1zeros"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$uf"/>
						</xsl:otherwise></xsl:choose>
					</xsl:variable>
					<core:subOrganizationWithin>
						<rdf:Description rdf:about="{$baseURI}{$superorg}">
							<core:hasSubOrganization rdf:resource="{$baseURI}org{$orgid}"/>
						</rdf:Description>
					</core:subOrganizationWithin>
				</xsl:matching-substring>
			</xsl:analyze-string>
		</rdf:Description>
	</xsl:template> -->
</xsl:stylesheet>
