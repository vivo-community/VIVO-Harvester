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
	xmlns:db-t_UF_DIR_EMP_STU_1="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_1/"
	xmlns:db-t_UF_DIR_EMP_STU_2="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_2/"
	xmlns:db-t_UF_DIR_EMP_STU_3="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_3/"
	xmlns:db-t_UF_DIR_EMP_STU_4="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_4/"
	xmlns:db-t_UF_DIR_EMP_STU_5="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_5/"
	xmlns:db-t_UF_DIR_EMP_STU_6="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_6/"
	xmlns:db-t_UF_PA_GL_ACCT="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PA_GL_ACCT/"
	xmlns:db-t_UF_PER_UFAU="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PER_UFAU/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>  

	<!-- The main Article Set of all pubmed citations loaded 
		This serves as the header of the RDF file produced
	 -->
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			xmlns:ufl="http://vivo.ufl.edu/ontology/vivo-ufl/"
			xmlns:db-t_UF_DIR_EMP_STU_1="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_1/"
			xmlns:db-t_UF_DIR_EMP_STU_2="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_2/"
			xmlns:db-t_UF_DIR_EMP_STU_3="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_3/"
			xmlns:db-t_UF_DIR_EMP_STU_4="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_4/"
			xmlns:db-t_UF_DIR_EMP_STU_5="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_5/"
			xmlns:db-t_UF_DIR_EMP_STU_6="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_DIR_EMP_STU_6/"
			xmlns:db-t_UF_PA_GL_ACCT="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PA_GL_ACCT/"
			xmlns:db-t_UF_PER_UFAU="jdbc:jtds:sqlserver://erp-beta-odbc.ad.ufl.edu:1433/ODBCWH;domain=UFAD/fields/t_UF_PER_UFAU/"
			xmlns:foaf="http://xmlns.com/foaf/0.1/"
			xmlns:owl="http://www.w3.org/2002/07/owl#"
			xmlns:core="http://vivoweb.org/ontology/core#"
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
						</xsl:choose>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:matching-substring>
		</xsl:analyze-string>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_1">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<rdfs:label xml:lang="en-US"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_NAME_TXT" /></rdfs:label>
			<ufl:businessName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_NAME_TXT" /></ufl:businessName>
			<core:workEmail><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_EMAIL" /></core:workEmail>
			<ufl:ufid rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$ufid" /></ufl:ufid>
			<core:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_1:UF_WORK_TITLE" /></core:description>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_2">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<xsl:if test="$this/db-t_UF_DIR_EMP_STU_2:UF_TYPE_CD = 10">
				<core:workPhone><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_AREA_CD"/><xsl:text> </xsl:text><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_PHONE_NO"/></core:workPhone>
			</xsl:if>
			<xsl:if test="$this/db-t_UF_DIR_EMP_STU_2:UF_TYPE_CD = 242">
				<core:workFax><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_AREA_CD"/><xsl:text> </xsl:text><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_2:UF_PHONE_NO"/></core:workFax>
			</xsl:if>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_3">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<core:mailingAddress>
				<rdf:Description>
					<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Address"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#AbstractInformation"/>
					<core:mailingAddressFor rdf:resource="http://vivo.ufl.edu/person/{$ufid}"/>
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
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<xsl:variable name="typeCode" select="$this/db-t_UF_DIR_EMP_STU_4:UF_TYPE_CD"/>
			<xsl:choose>
				<xsl:when test="$typeCode=35">
					<foaf:firstName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></foaf:firstName>
				</xsl:when>
				<xsl:when test="$typeCode=36">
					<foaf:lastName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></foaf:lastName>
				</xsl:when>
				<xsl:when test="$typeCode=37">
					<core:middleName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></core:middleName>
				</xsl:when>
				<xsl:when test="$typeCode=38">
					<bibo:prefixName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></bibo:prefixName>
				</xsl:when>
				<xsl:when test="$typeCode=232">
					<ufl:activeDirName><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_4:UF_NAME_TXT"/></ufl:activeDirName>
				</xsl:when>
			</xsl:choose>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_5">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<core:personInPosition>
				<rdf:Description>
					<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
					<core:positionForPerson rdf:resource="http://vivo.ufl.edu/person/{$ufid}"/>
					<core:startYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_5:UF_BEGIN_TS"/></core:startYear>
				</rdf:Description>
			</core:personInPosition>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_DIR_EMP_STU_6">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<core:personInPosition>
				<rdf:Description>
					<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
					<core:positionForPerson rdf:resource="http://vivo.ufl.edu/person/{$ufid}"/>
					<core:startYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_6:UF_BEGIN_TS"/></core:startYear>
					<core:endYear rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="$this/db-t_UF_DIR_EMP_STU_6:UF_END_TS"/></core:endYear>
				</rdf:Description>
			</core:personInPosition>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_PA_GL_ACCT">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
			<ufl:gatorlink><xsl:value-of select="$this/db-t_UF_PA_GL_ACCT:USERIDALIAS"/></ufl:gatorlink>
		</rdf:Description>
	</xsl:template>
	
	<xsl:template name="t_UF_PER_UFAU">
		<xsl:param name='ufid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivo.ufl.edu/person/{$ufid}">
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>