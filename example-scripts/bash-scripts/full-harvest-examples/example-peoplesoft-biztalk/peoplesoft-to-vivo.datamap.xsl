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
	xmlns:datetime="http://exslt.org/dates-and-times"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:public='http://vitro.mannlib.cornell.edu/ns/vitro/public#'
	xmlns:ns0="http://uf.biztalk.shibperson" >
	
	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivo.ufl.edu/harvested/</xsl:variable>

	<!-- This is the main person being translated - will serve as the primary import -->
	<xsl:template match="ns0:PERSON">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			xmlns:ufl="http://vivo.ufl.edu/ontology/vivo-ufl/"
			xmlns:foaf="http://xmlns.com/foaf/0.1/"
			xmlns:owl="http://www.w3.org/2002/07/owl#"
			xmlns:core="http://vivoweb.org/ontology/core#"
			xmlns:score="http://vivoweb.org/ontology/score#"
			xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" >
			<xsl:variable name="ufid" select="UFID"/>
			<rdf:Description rdf:about="{$baseURI}person/{$ufid}">
				<ufVivo:ufid><xsl:value-of select="$ufid"/></ufVivo:ufid>
				<ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy>
				<xsl:if test="normalize-space( NAME[@type=35] )">
					<foaf:firstName><xsl:value-of select="NAME[@type=35]"/></foaf:firstName>
				</xsl:if>
				<xsl:if test="normalize-space( NAME[@type=36] )">
					<foaf:lastName><xsl:value-of select="NAME[@type=36]"/></foaf:lastName>
				</xsl:if>
				<xsl:if test="normalize-space( NAME[@type=37] )">
					<core:middleName><xsl:value-of select="NAME[@type=37]"/></core:middleName>
				</xsl:if>
				<xsl:if test="normalize-space( NAME[@type=40] )">
					<bibo:prefixName><xsl:value-of select="NAME[@type=40]"/></bibo:prefixName>
				</xsl:if>
				<xsl:if test="normalize-space( NAME[@type=39] )">
					<bibo:suffixName><xsl:value-of select="NAME[@type=39]"/></bibo:suffixName>
				</xsl:if>
				<xsl:if test="normalize-space( NAME[@type=21] )">
					<rdfs:label><xsl:value-of select="NAME[@type=21]"/></rdfs:label>
				</xsl:if>
				<xsl:if test="normalize-space( GLID )">
					<ufVivo:gatorlink rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="GLID" /></ufVivo:gatorlink>
				</xsl:if>
				<xsl:if test="normalize-space( WORKINGTITLE )">
					<core:preferredTitle><xsl:value-of select="WORKINGTITLE" /></core:preferredTitle>
				</xsl:if>
				<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
				<ufVivo:Deceased><xsl:value-of select="DECEASED" /></ufVivo:Deceased>
				<ufVivo:HomeDeptid><xsl:value-of select="DEPTID" /></ufVivo:HomeDeptid>			
				<xsl:choose>
					<xsl:when test='PROTECT="N"'>
						<ufVivo:privacy><xsl:value-of select="PROTECT"/></ufVivo:privacy>
						<core:primaryEmail><xsl:value-of select="EMAIL[@type=1]" /></core:primaryEmail>
						<core:primaryPhoneNumber><xsl:value-of select="PHONE[@type=10]" /></core:primaryPhoneNumber>
						<core:faxNumber><xsl:value-of select="PHONE[@type=11]" /></core:faxNumber>
					</xsl:when>
					<xsl:otherwise>
						<ufVivo:privacy><xsl:value-of select="PROTECT"/></ufVivo:privacy>
						<core:primaryEmail />
						<core:primaryPhoneNumber />
						<core:faxNumber />
					 <!--<public:mainImage rdf:resource="{$baseURI}mainImg/ufid{$ufid}"/>	
						
						<rdf:Description rdf:about="{$baseURI}mainImg/ufid{$ufid}">
							<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
				  			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
				  			<public:downloadLocation rdf:resource="{$baseURI}fullDirDownload/ufid{$ufid}"/>
				  			<public:thumbnailImage rdf:resource="{$baseURI}thumbImg/ufid{$ufid}"/>
				  			<public:filename rdf:datatype="http://www.w3.org/2001/XMLSchema#string">person.thumbnail.jpg</public:filename>
				  			<public:mimeType rdf:datatype="http://www.w3.org/2001/XMLSchema#string">image/jpeg</public:mimeType>
						</rdf:Description>
					
						<rdf:Description rdf:about="{$baseURI}thumbImg/ufid{$ufid}">
							<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
				 			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
							<public:downloadLocation rdf:resource="{$baseURI}thumbDirDownload/ufid{$ufid}"/>
							<public:filename rdf:datatype="http://www.w3.org/2001/XMLSchema#string">person.thumbnail.jpg</public:filename>
							<public:mimeType rdf:datatype="http://www.w3.org/2001/XMLSchema#string">image/jpeg</public:mimeType>
						</rdf:Description>
							
						<rdf:Description rdf:about="{$baseURI}thumbDirDownload/ufid{$ufid}">
				  			<public:directDownloadUrl>/file/person.thumbnail.jpg</public:directDownloadUrl>
				  			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
				  			<vitro:modTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="datetime:dateTime()" /></vitro:modTime>
						</rdf:Description>
						
						<rdf:Description rdf:about="{$baseURI}fullDirDownload/ufid{$ufid}">
							<public:directDownloadUrl>/file/person.thumbnail.jpg</public:directDownloadUrl>
				  			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
				 			<vitro:modTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="datetime:dateTime()" /></vitro:modTime>
						</rdf:Description>	
	  -->	
					</xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="RELATIONSHIP[@type=200]">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#EmeritusProfessor"/>
					</xsl:when>
					<xsl:when test="RELATIONSHIP[@type=236]">
						<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Consultant"/>
					</xsl:when>
					<xsl:when test="RELATIONSHIP[@type=192]">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
					</xsl:when>
					<xsl:when test="RELATIONSHIP[@type=219]">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
					</xsl:when>
					<xsl:when test="RELATIONSHIP[@type=197]">
						<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourtesyFaculty"/>
					</xsl:when>
					<xsl:when test="RELATIONSHIP[@type=195]">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic"/>
					</xsl:when>
					<xsl:when test="RELATIONSHIP[@type=221]">
						<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Consultant"/>
					</xsl:when>
				</xsl:choose>
				
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>
