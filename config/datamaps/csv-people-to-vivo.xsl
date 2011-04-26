<!--
  Copyright (c) 2010 Christopher Haines, James Pence, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
      James Pence
-->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->

<xsl:stylesheet version = "2.0"
	xmlns:xsl = 'http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
	xmlns:core = 'http://vivoweb.org/ontology/core#'
	xmlns:score = 'http://vivoweb.org/ontology/score#'
    xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
    xmlns:bibo = 'http://purl.org/ontology/bibo/'>
	
	<xsl:output method = "xml" indent = "yes"/>
	<xsl:variable name = "baseURI">http://vivoweb.org/harvest/csvfile/</xsl:variable>
	
	<xsl:template match = "rdf:RDF">
		<rdf:RDF xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
            xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
    		xmlns:core = 'http://vivoweb.org/ontology/core#'
            xmlns:score = 'http://vivoweb.org/ontology/score#'
            xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
            xmlns:bibo = 'http://purl.org/ontology/bibo/'>
			<xsl:apply-templates select = "rdf:Description" />		
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match = "rdf:Description">
		<xsl:variable name = "this" select = "." />
		<xsl:call-template name = "t_People">
		  <xsl:with-param name = "this" select = "$this" />
          <xsl:with-param name = "personid" select = "$this/PeopleCSVFile:PersonID" />
		</xsl:call-template>
	</xsl:template>
		
	<xsl:template name = "t_People">
		<xsl:param name = 'personid' />
		<xsl:param name = 'this' />
<!--	Creating a Grant-->
		<rdf:Description rdf:about = "{$baseURI}person/person{$personid}">
            <rdf:type rdf:resource = "http://xmlns.com/foaf/0.1/Person"/>
			<score:personID><xsl:value-of select = "$personid" /></score:personID>
            <core:workEmail><xsl:value-of select = "$this/PeopleCSVFile:Email" /></core:workEmail>
            <core:workPhone><xsl:value-of select = "$this/PeopleCSVFile:Phone"/></core:workPhone>
            <core:workFax><xsl:value-of select = "$this/PeopleCSVFile:Fax"/></core:workFax>
            <foaf:firstName><xsl:value-of select = "$this/PeopleCSVFile:FirstName"/></foaf:firstName>
            <foaf:lastName><xsl:value-of select = "$this/PeopleCSVFile:LastName"/></foaf:lastName>
            <core:middleName><xsl:value-of select = "$this/PeopleCSVFile:MidName"/></core:middleName>
            <bibo:prefixName><xsl:value-of select = "$this/PeopleCSVFile:NamePrefix"/></bibo:prefixName>
            <bibo:suffixName><xsl:value-of select = "$this/PeopleCSVFile:NameSuffix"/></bibo:suffixName>
            <rdfs:label><xsl:value-of select = "$this/PeopleCSVFile:FullName"/></rdfs:label>
            <core:preferredTitle><xsl:value-of select = "$this/PeopleCSVFile:Title"/></core:preferredTitle>
            <xsl:variable name = "startDate" select = "$this/PeopleCSVFile:StartDate" />
            <xsl:variable name = "endDate" select = "$this/PeopleCSVFile:EndDate" />
    		<core:startDate rdf:datatype = "http://www.w3.org/2001/XMLSchema#date">
				<xsl:analyze-string select = "$this/PeopleCSVFile:StartDate" regex = "^(....-..-..).*?$">
					<xsl:matching-substring>
						<xsl:value-of select = "regex-group(1)"/>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</core:startDate>
			<core:endDate rdf:datatype = "http://www.w3.org/2001/XMLSchema#date">
				<xsl:analyze-string select = "$this/PeopleCSVFile:EndDate" regex = "^(....-..-..).*?$">
					<xsl:matching-substring>
						<xsl:value-of select = "regex-group(1)"/>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</core:endDate>
			
            <xsl:variable name = "type" select = "$this/PeopleCSVFile:PositionType" />
            <xsl:choose>
                <xsl:when test = "$type = 'faculty'">
                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyMember"/>
                </xsl:when>
                <xsl:when test = "$type = 'non academic">
                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#NonAcademic"/>
                </xsl:when>
                <xsl:when test = "$type = 'emeritus professor">
                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#EmeritusProfessor"/>
                </xsl:when>
            </xsl:choose>
            
            <xsl:variable name = "orgID" select = "$this/PeopleCSVFile:DepartmentID" />
            <core:personInPosition>
                <rdf:Description rdf:about="{$baseURI}position/positionFor{$personid}in{$fullorgnum}as{$type}from{$startDate}">
                    <core:positionInOrganization>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DependentResource"/>
                        <xsl:choose>
			                <xsl:when test = "$type = 'faculty'">
			                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyPosition"/>
			                </xsl:when>
			                <xsl:when test = "$type = 'non academic">
			                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#NonAcademicPosition"/>
			                </xsl:when>
			                <xsl:when test = "$type = 'emeritus professor">
			                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyPosition"/>
			                </xsl:when>
			            </xsl:choose>
                    </core:positionInOrganization>
                
                </rdf:Description>
            </core:personInPosition>
		</rdf:Description>
	
</xsl:stylesheet>