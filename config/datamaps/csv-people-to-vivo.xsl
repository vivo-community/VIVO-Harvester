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
    xmlns:bibo = 'http://purl.org/ontology/bibo/'
    xmlns:db-csv='nullfields/csv/'>
	
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
          <xsl:with-param name = "personid" select = "$this/db-csv:PERSONID" />
		</xsl:call-template>
	</xsl:template>
		
	<xsl:template name = "t_People">
		<xsl:param name = 'personid' />
		<xsl:param name = 'this' />
<!--	Creating a Grant-->
		<rdf:Description rdf:about = "{$baseURI}person/person{$personid}">
            <rdf:type rdf:resource = "http://xmlns.com/foaf/0.1/Person"/>
			<score:personID><xsl:value-of select = "$personid" /></score:personID>
            <xsl:if test="not( $this/db-csv:EMAIL = '' or $this/db-csv:EMAIL = 'null' )">
                <core:email><xsl:value-of select = "$this/db-csv:EMAIL" /></core:email>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:PHONE = '' or $this/db-csv:PHONE = 'null' )">
                <core:phoneNumber><xsl:value-of select = "$this/db-csv:PHONE"/></core:phoneNumber>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:FAX = '' or $this/db-csv:FAX = 'null' )">
                <core:faxNumber><xsl:value-of select = "$this/db-csv:FAX"/></core:faxNumber>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:FIRSTNAME = '' or $this/db-csv:FIRSTNAME = 'null' )">
                <foaf:firstName><xsl:value-of select = "$this/db-csv:FIRSTNAME"/></foaf:firstName>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:LASTNAME = '' or $this/db-csv:LASTNAME = 'null' )">
                <foaf:lastName><xsl:value-of select = "$this/db-csv:LASTNAME"/></foaf:lastName>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:MIDNAME = '' or $this/db-csv:MIDNAME = 'null' )">
                <core:middleName><xsl:value-of select = "$this/db-csv:MIDNAME"/></core:middleName>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:NAMEPREFIX = '' or $this/db-csv:NAMEPREFIX = 'null' )">
                <bibo:prefixName><xsl:value-of select = "$this/db-csv:NAMEPREFIX"/></bibo:prefixName>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:NAMESUFFIX = '' or $this/db-csv:NAMESUFFIX = 'null' )">
                <bibo:suffixName><xsl:value-of select = "$this/db-csv:NAMESUFFIX"/></bibo:suffixName>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:FULLNAME = '' or $this/db-csv:FULLNAME = 'null' )">
                <rdfs:label><xsl:value-of select = "$this/db-csv:FULLNAME"/></rdfs:label>
            </xsl:if>
            <xsl:if test="not( $this/db-csv:TITLE = '' or $this/db-csv:TITLE = 'null' )">
                <core:preferredTitle><xsl:value-of select = "$this/db-csv:TITLE"/></core:preferredTitle>
            </xsl:if>
            

			
            <xsl:if test="not( $this/db-csv:POSITIONTYPE = '' or $this/db-csv:POSITIONTYPE = 'null' )">
	            <xsl:variable name = "type" select = "$this/db-csv:POSITIONTYPE" />
	            <xsl:choose>
	                <xsl:when test = "$type = 'faculty'">
	                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyMember"/>
	                </xsl:when>
	                <xsl:when test = "$type = 'non academic'">
	                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#NonAcademic"/>
	                </xsl:when>
	                <xsl:when test = "$type = 'emeritus professor'">
	                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#EmeritusProfessor"/>
	                </xsl:when>
	            </xsl:choose>
            
                <core:personInPosition rdf:resource="{$baseURI}position/positionFor{$personid}from{$this/db-csv:STARTDATE}"/>
                
            </xsl:if>
		</rdf:Description>
		<xsl:if test="not( $this/db-csv:POSITIONTYPE = '' or $this/db-csv:POSITIONTYPE = 'null' )">
            <rdf:Description rdf:about="{$baseURI}position/positionFor{$personid}from{$this/db-csv:STARTDATE}">
                      <xsl:variable name = "orgID" select = "$this/db-csv:DEPARTMENTID" />
                      <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
                      <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DependentResource"/>
                      
		                <xsl:variable name = "type" select = "$this/db-csv:POSITIONTYPE" />
		                <xsl:choose>
		                    <xsl:when test = "$type = 'faculty'">
		                        <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyMember"/>
		                    </xsl:when>
		                    <xsl:when test = "$type = 'non academic'">
		                        <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#NonAcademic"/>
		                    </xsl:when>
		                    <xsl:when test = "$type = 'emeritus professor'">
		                        <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#EmeritusProfessor"/>
		                    </xsl:when>
		                </xsl:choose>
		                
                     <core:positionForPerson rdf:resource="{$baseURI}person/person{$personid}"/>
                     <core:positionInOrganization>
                        <rdf:Description rdf:about="{$baseURI}org/org{$orgID}">
                            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                            <xsl:if test="not( $this/db-csv:DEPARTMENTID = '' or $this/db-csv:DEPARTMENTID = 'null' )">
                            <score:orgID><xsl:value-of select="$orgID"/></score:orgID>
                            </xsl:if>
                            <xsl:if test="not( $this/db-csv:DEPARTMENTNAME = '' or $this/db-csv:DEPARTMENTNAME = 'null' )">
                            <rdfs:label><xsl:value-of select="$this/db-csv:DEPARTMENTNAME"/></rdfs:label>
                            </xsl:if>
                            <core:organizationForPosition rdf:resource="{$baseURI}position/positionFor{$personid}from{$this/db-csv:STARTDATE}"/>
                        </rdf:Description>
                    </core:positionInOrganization>
                    
                    <xsl:if test="not( $this/db-csv:STARTDATE = '' or $this/db-csv:STARTDATE = 'null' )">
		                <core:startDate rdf:datatype = "http://www.w3.org/2001/XMLSchema#date">
		                    <xsl:analyze-string select = "$this/db-csv:STARTDATE" regex = "^(....-..-..).*?$">
		                        <xsl:matching-substring>
		                            <xsl:value-of select = "regex-group(1)"/>
		                        </xsl:matching-substring>
		                    </xsl:analyze-string>
		                </core:startDate>
		            </xsl:if>
		            
		            <xsl:if test="not( $this/db-csv:ENDDATE = '' or $this/db-csv:ENDDATE = 'null' )">
		                <core:endDate rdf:datatype = "http://www.w3.org/2001/XMLSchema#date">
		                    <xsl:analyze-string select = "$this/db-csv:ENDDATE" regex = "^(....-..-..).*?$">
		                        <xsl:matching-substring>
		                            <xsl:value-of select = "regex-group(1)"/>
		                        </xsl:matching-substring>
		                    </xsl:analyze-string>
		                </core:endDate>
		            </xsl:if>
             </rdf:Description>
         </xsl:if>
	</xsl:template>
</xsl:stylesheet>