<!--
  Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
  All rights reserved.
  This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
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
    xmlns:obo = "http://purl.obolibrary.org/obo/"
    xmlns:vitro = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
    xmlns:vcard = "http://www.w3.org/2006/vcard/ns#"
    xmlns:db-CSV='jdbc:h2:data/csv/store/fields/CSV2/'>
	
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
		
	    <xsl:variable name="namePrefix" select = "$this/db-CSV:NAMEPREFIX" />
	    <xsl:variable name="lastName" select="$this/db-CSV:LASTNAME"/>
	    <xsl:variable name="firstName" select="$this/db-CSV:FIRSTNAME"/>
	    <xsl:variable name="midName" select="$this/db-CSV:MIDNAME"/> 
	    <xsl:variable name="nameSuffix" select = "$this/db-CSV:NAMESUFFIX" />	       
	    <xsl:variable name="fullName"><xsl:value-of select = "$lastName" />, <xsl:value-of select = "$firstName" /></xsl:variable>
	    
	    <xsl:variable name="email"><xsl:value-of select="$this/db-CSV:EMAIL" /></xsl:variable>
	    <xsl:variable name="phone" select="$this/db-CSV:PHONE"/> 
	    <xsl:variable name="fax" select="$this/db-CSV:FAX"/>   
	    
	    <xsl:variable name="organization" select="$this/db-CSV:DEPARTMENTNAME"/>
	    <xsl:variable name="orgid" select="encode-for-uri($this/db-CSV:DEPARTMENTID)"/>
	    
	    <!-- this data source does not have roles or job titles, make each person an Affiliate of their organization -->
	    <xsl:variable name="rolename" select="$this/db-CSV:TITLE"/>
	    <xsl:variable name="roleid" select="$this/db-CSV:TITLE"/>
	    <xsl:variable name="positionType" select="$this/db-CSV:POSITIONTYPE"/>
	    <xsl:variable name="startDate" select="$this/db-CSV:STARTDATE"/>
	    <xsl:variable name="endDate" select="$this/db-CSV:ENDDATE"/>
	     
	    
	    
<!--	Creating a Person -->
	<rdf:Description rdf:about = "{$baseURI}person/person{$personid}">
            <rdf:type rdf:resource = "http://xmlns.com/foaf/0.1/Person"/>
            <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$fullName" /></rdfs:label>
	    <score:personid><xsl:value-of select = "$personid" /></score:personid>
	    <obo:ARG_2000028 rdf:resource="{$baseURI}vcard/vcardFor{$personid}"/> 
	    <xsl:if test="($orgid !='') and ($roleid !='')" >
               <core:relatedBy rdf:resource="{$baseURI}position/positionFor{$orgid}-{$roleid}"/>
            </xsl:if>      
      
            <xsl:if test="normalize-space( $rolename )">
               <vcard:hasTitle rdf:resource="{$baseURI}vcard/vcardTitleFor{$personid}"/>
            </xsl:if> 
	        <xsl:if test="normalize-space( $positionType )">     
	            <xsl:choose>
	                <xsl:when test = "$positionType = 'faculty'">
	                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyMember"/>
	                </xsl:when>
	                <xsl:when test = "$positionType = 'non academic'">
	                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#NonAcademic"/>
	                </xsl:when>
	                <xsl:when test = "$positionType = 'emeritus professor'">
	                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#EmeritusProfessor"/>
	                </xsl:when>
	            </xsl:choose> 
            </xsl:if>
	</rdf:Description>
		
     <!-- vcard for person -->
     <rdf:Description rdf:about="{$baseURI}vcard/vcardFor{$personid}"> 

       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual"/>
       <rdf:type rdf:resource="http://purl.obolibrary.org/obo/ARG_2000379"/>
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard for: <xsl:value-of select = "$fullName" /></rdfs:label>
       <xsl:if test="normalize-space( $fullName )">
       <vcard:hasName rdf:resource="{$baseURI}vcard/vcardNameFor{$personid}"/>
       </xsl:if>
       <xsl:if test="normalize-space( $email )">
       <vcard:hasEmail rdf:resource="{$baseURI}vcard/vcardEmailFor{$personid}"/>
       </xsl:if>
       <xsl:if test="normalize-space( $phone )">
       <vcard:hasTelephone rdf:resource="{$baseURI}vcard/vcardPhoneFor{$personid}"/>
       </xsl:if>
       <xsl:if test="normalize-space( $fax )">
       <vcard:hasTelephone rdf:resource="{$baseURI}vcard/vcardFaxFor{$personid}"/>
       </xsl:if>
       
       <xsl:if test="normalize-space( $rolename )">
       <vcard:hasTitle rdf:resource="{$baseURI}vcard/vcardTitleFor{$personid}"/>
       </xsl:if> 
       <obo:ARG_2000029 rdf:resource="{$baseURI}person/person{$personid}"/> 
     </rdf:Description>
     
     <!-- vcard name -->
     <xsl:if test="normalize-space( $fullName )">
     <rdf:Description rdf:about="{$baseURI}vcard/vcardNameFor{$personid}">
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Name"/> 
       <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#Name"/>
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard name for: <xsl:value-of select = "$fullName" /></rdfs:label>
       <xsl:if test="normalize-space( $firstName )">
       <vcard:givenName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$firstName" /></vcard:givenName> 
       </xsl:if>
       <xsl:if test="normalize-space( $lastName )">    
       <vcard:familyName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$lastName" /></vcard:familyName>
       </xsl:if>
       <xsl:if test="normalize-space( $namePrefix )">
       <vcard:honorificPrefix rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$namePrefix" /> </vcard:honorificPrefix> 
       </xsl:if> 
       <xsl:if test="normalize-space( $nameSuffix )">
       <vcard:honorificSuffix rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$nameSuffix" /> </vcard:honorificSuffix>
       </xsl:if> 
     </rdf:Description>
     </xsl:if>
     
     <!-- vcard email -->
     <xsl:if test="normalize-space( $email )">
     <rdf:Description rdf:about="{$baseURI}vcard/vcardEmailFor{$personid}">
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Email"/>
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Work"/>
       <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#Email"/>
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard email for: <xsl:value-of select = "$fullName" /></rdfs:label>
       <vcard:email rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$email" /></vcard:email>  
     </rdf:Description>
     </xsl:if>
     
     <!-- vcard telephone -->
     <xsl:if test="normalize-space( $phone )">
     <rdf:Description rdf:about="{$baseURI}vcard/vcardPhoneFor{$personid}">
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Telephone"/> 
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard telephone for: <xsl:value-of select = "$fullName" /></rdfs:label>
       <vcard:telephone rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$phone" /></vcard:telephone>  
     </rdf:Description>
     </xsl:if>
     
     <!-- vcard telephone/fax -->
     <xsl:if test="normalize-space( $fax )">
     <rdf:Description rdf:about="{$baseURI}vcard/vcardFaxFor{$personid}">
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Telephone"/>
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Fax"/>  
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard fax for: <xsl:value-of select = "$fullName" /></rdfs:label>
       <vcard:telephone rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$fax" /></vcard:telephone>  
     </rdf:Description>
     </xsl:if>
     
     <!-- vcard title -->
     <xsl:if test="normalize-space( $rolename )">
     <rdf:Description rdf:about="{$baseURI}vcard/vcardTitleFor{$personid}">
       <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Title"/>
       <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#Title"/>
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard title for: <xsl:value-of select = "$fullName" /></rdfs:label>
       <vcard:title rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$rolename" /></vcard:title>  
     </rdf:Description>
     </xsl:if>
     
     <!-- position -->

     <xsl:if test="($orgid!='') and ($roleid!='')" >
     <rdf:Description rdf:about="{$baseURI}position/positionFor{$orgid}-{$roleid}" >
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship"/>
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$rolename" /></rdfs:label>
        <xsl:if test="normalize-space( $positionType )">     
            <xsl:choose>
                <xsl:when test = "$positionType = 'faculty'">
                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#FacultyMember"/>
                </xsl:when>
                <xsl:when test = "$positionType = 'non academic'">
                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#NonAcademic"/>
                </xsl:when>
                <xsl:when test = "$positionType = 'emeritus professor'">
                    <rdf:type rdf:resource = "http://vivoweb.org/ontology/core#EmeritusProfessor"/>
                </xsl:when>
            </xsl:choose> 
        </xsl:if>
        
        <xsl:if test="not( not(normalize-space($startDate) ) or $startDate = 'null' or $startDate = 'YYYY-MM-DD' )">
        <core:dateTimeInterval rdf:resource="{$baseURI}timeInterval/start{$startDate}toEnd{$endDate}" />
        </xsl:if>
        
   
        <core:relates rdf:resource="{$baseURI}person/person{$personid}" />
        <core:relates rdf:resource="{$baseURI}org/{$orgid}" />

     </rdf:Description>
     </xsl:if> 
     
     <!--  Organization/affiliation -->
     <xsl:if test="normalize-space( $orgid )">
     <rdf:Description rdf:about="{$baseURI}org/{$orgid}"> 
        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
        <xsl:if test="normalize-space( $roleid )">
        <core:relatedBy rdf:resource="{$baseURI}position/positionFor{$orgid}-{$roleid}" />
        </xsl:if>
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$organization" /></rdfs:label>
     </rdf:Description>
     </xsl:if>
     
     <xsl:if test="not( not(normalize-space($startDate) ) or $startDate = 'null' or $startDate = 'YYYY-MM-DD' )">
     <rdf:Description rdf:about="{$baseURI}timeInterval/start{$startDate}toEnd{$endDate}">
       <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
       <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
       <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
       <core:start rdf:resource="{$baseURI}timeInterval/date{$startDate}"/>
       <xsl:if test="normalize-space($endDate)">
       <core:end rdf:resource="{$baseURI}timeInterval/date{$endDate}"/>
       </xsl:if>
    </rdf:Description>
    </xsl:if>
    
    <xsl:if test="not( not(normalize-space($startDate) ) or $startDate = 'null' or $startDate = 'YYYY-MM-DD' )">
    <rdf:Description rdf:about="{$baseURI}timeInterval/date{$startDate}">
        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$startDate" />T00:00:00</core:dateTime>
    </rdf:Description>
    </xsl:if>
    
    <xsl:if test="normalize-space($endDate)">
    <rdf:Description rdf:about="{$baseURI}timeInterval/date{$endDate}">
        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$endDate" />T00:00:00</core:dateTime>
    </rdf:Description>
    </xsl:if> 
     
	</xsl:template>
</xsl:stylesheet>
