<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
  All rights reserved.
  This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
-->

<xsl:stylesheet version = "2.0"
	xmlns:xsl = 'http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
	xmlns:core = 'http://vivoweb.org/ontology/core#'
	xmlns:score = 'http://vivoweb.org/ontology/score#'
    xmlns:foaf = 'http://xmlns.com/foaf/0.1/'
    xmlns:bibo = 'http://purl.org/ontology/bibo/'
    xmlns:db-CSV='nullfields/CSV/'>
	
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
          <xsl:with-param name = "personid" select = "$this/db-CSV:PERSONID" />
		</xsl:call-template>
	</xsl:template>
		
	<xsl:template name = "t_People">
		<xsl:param name = 'personid' />
		<xsl:param name = 'this' />
<!--	Creating a Grant-->
		<rdf:Description rdf:about = "{$baseURI}person/person{$personid}">
            <rdf:type rdf:resource = "http://xmlns.com/foaf/0.1/Person"/>
			<score:personID><xsl:value-of select = "$personid" /></score:personID>
            <xsl:if test="not( $this/db-CSV:EMAIL = '' or $this/db-CSV:EMAIL = 'null' )">
                <core:primaryEmail><xsl:value-of select = "$this/db-CSV:EMAIL" /></core:primaryEmail>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:PHONE = '' or $this/db-CSV:PHONE = 'null' )">
                <core:phoneNumber><xsl:value-of select = "$this/db-CSV:PHONE"/></core:phoneNumber>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:FAX = '' or $this/db-CSV:FAX = 'null' )">
                <core:faxNumber><xsl:value-of select = "$this/db-CSV:FAX"/></core:faxNumber>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:FIRSTNAME = '' or $this/db-CSV:FIRSTNAME = 'null' )">
                <foaf:firstName><xsl:value-of select = "$this/db-CSV:FIRSTNAME"/></foaf:firstName>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:LASTNAME = '' or $this/db-CSV:LASTNAME = 'null' )">
                <foaf:lastName><xsl:value-of select = "$this/db-CSV:LASTNAME"/></foaf:lastName>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:MIDNAME = '' or $this/db-CSV:MIDNAME = 'null' )">
                <core:middleName><xsl:value-of select = "$this/db-CSV:MIDNAME"/></core:middleName>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:NAMEPREFIX = '' or $this/db-CSV:NAMEPREFIX = 'null' )">
                <bibo:prefixName><xsl:value-of select = "$this/db-CSV:NAMEPREFIX"/></bibo:prefixName>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:NAMESUFFIX = '' or $this/db-CSV:NAMESUFFIX = 'null' )">
                <bibo:suffixName><xsl:value-of select = "$this/db-CSV:NAMESUFFIX"/></bibo:suffixName>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:FULLNAME = '' or $this/db-CSV:FULLNAME = 'null' )">
                <rdfs:label><xsl:value-of select = "$this/db-CSV:FULLNAME"/></rdfs:label>
            </xsl:if>
            <xsl:if test="not( $this/db-CSV:TITLE = '' or $this/db-CSV:TITLE = 'null' )">
                <core:preferredTitle><xsl:value-of select = "$this/db-CSV:TITLE"/></core:preferredTitle>
            </xsl:if>
            

			
            <xsl:if test="not( $this/db-CSV:POSITIONTYPE = '' or $this/db-CSV:POSITIONTYPE = 'null' )">
	            <xsl:variable name = "type" select = "$this/db-CSV:POSITIONTYPE" />
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
            
                <core:personInPosition rdf:resource="{$baseURI}position/positionFor{$personid}from{$this/db-CSV:STARTDATE}"/>
                
            </xsl:if>
		</rdf:Description>
		<xsl:if test="not( $this/db-CSV:POSITIONTYPE = '' or $this/db-CSV:POSITIONTYPE = 'null' )">
            <rdf:Description rdf:about="{$baseURI}position/positionFor{$personid}from{$this/db-CSV:STARTDATE}">
                      <xsl:variable name = "orgID" select = "$this/db-CSV:DEPARTMENTID" />
                      <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
                      <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DependentResource"/>
                      
		                <xsl:variable name = "type" select = "$this/db-CSV:POSITIONTYPE" />
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
                            <xsl:if test="not( $this/db-CSV:DEPARTMENTID = '' or $this/db-CSV:DEPARTMENTID = 'null' )">
                            <score:orgID><xsl:value-of select="$orgID"/></score:orgID>
                            </xsl:if>
                            <xsl:if test="not( $this/db-CSV:DEPARTMENTNAME = '' or $this/db-CSV:DEPARTMENTNAME = 'null' )">
                            <rdfs:label><xsl:value-of select="$this/db-CSV:DEPARTMENTNAME"/></rdfs:label>
                            </xsl:if>
                            <core:organizationForPosition rdf:resource="{$baseURI}position/positionFor{$personid}from{$this/db-CSV:STARTDATE}"/>
                        </rdf:Description>
                    </core:positionInOrganization>
                   
                    <xsl:if test="not( not(normalize-space($this/db-CSV:STARTDATE) ) or $this/db-CSV:STARTDATE = 'null' or $this/db-CSV:STARTDATE = 'YYYY-MM-DD' )">
		                <core:startDate rdf:datatype = "http://www.w3.org/2001/XMLSchema#date">
		                    <xsl:analyze-string select = "$this/db-CSV:STARTDATE" regex = "^(....-..-..).*?$">
		                        <xsl:matching-substring>
		                            <xsl:value-of select = "regex-group(1)"/>
		                        </xsl:matching-substring>
		                    </xsl:analyze-string>
		                </core:startDate>
		            </xsl:if>
		            
		            <xsl:if test="not( not(normalize-space($this/db-CSV:ENDDATE) ) or $this/db-CSV:ENDDATE = 'null' or $this/db-CSV:ENDDATE = 'YYYY-MM-DD' )">
		                <core:endDate rdf:datatype = "http://www.w3.org/2001/XMLSchema#date">
		                    <xsl:analyze-string select = "$this/db-CSV:ENDDATE" regex = "^(....-..-..).*?$">
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
