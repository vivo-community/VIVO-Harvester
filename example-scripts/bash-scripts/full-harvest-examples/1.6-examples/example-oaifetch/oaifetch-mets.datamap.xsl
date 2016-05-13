  <!--
    Copyright (c) 2010-2011 VIVO Harvester Team. For full list of
    contributors, please see the AUTHORS file provided. All rights
    reserved. This program and the accompanying materials are made
    available under the terms of the new BSD license which accompanies
    this distribution, and is available at
    http://www.opensource.org/licenses/bsd-license.html
  -->
 
  <!--
    Header information for the Style Sheet The style sheet requires xmlns
    for each prefix you use in constructing the new elements
  -->
<xsl:stylesheet version="2.0"
    xmlns:xsl = "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs = "http://www.w3.org/2001/XMLSchema"
    xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
    xmlns:rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs = "http://www.w3.org/2000/01/rdf-schema#"
    xmlns:vivo = "http://vivoweb.org/ontology/core#" 
    xmlns:score = "http://vivoweb.org/ontology/score#"
    xmlns:foaf = "http://xmlns.com/foaf/0.1/"
    xmlns:bibo = "http://purl.org/ontology/bibo/"
    xmlns:obo = "http://purl.obolibrary.org/obo/"
    xmlns:vitro = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
    xmlns:vcard = "http://www.w3.org/2006/vcard/ns#"
    xmlns:geo = "http://aims.fao.org/aos/geopolitical.owl#"
    xmlns:mets = "http://www.loc.gov/METS/"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:node-oai = "http://vivo.example.com/harvest/fields/oai/"
    xmlns:localVivo = "http://vivo.sample.edu/ontology/" 
    xmlns:oai_dc = "http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:dc = "http://purl.org/dc/elements/1.1/" 
    xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dcterms.xsdhttp://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd"  
    xmlns:stringhash="java:org.vivoweb.harvester.util.xslt.StringHash"
    extension-element-prefixes = "stringhash"

  >

  <!-- This will create indenting in xml readers -->
  <xsl:output method="xml" indent="yes" />
  <xsl:variable name="baseURI">http://vivo.example.com/harvest/</xsl:variable>
    
  <!--
    The main node of the record loaded This serves as the header of the
    RDF file produced
  -->
  
  
  <xsl:template match="mets:mets" >
    <xsl:variable name="this" select="." />
    <xsl:variable name="identifier" select="@OBJID" />
    
    <xsl:comment>   
    identifier: <xsl:value-of select = "$identifier" />
    <xsl:text>&#xa;</xsl:text>
    </xsl:comment>
    <xsl:text>&#10;</xsl:text>
    <rdf:RDF  >
     <xsl:variable name="title" select="normalize-space($this/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:titleInfo/mods:title)" />
     <xsl:variable name="docType" select="$this/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:genre" />
     <xsl:variable name="pubDate" select="$this/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:originInfo/mods:dateIssued" />
     <xsl:variable name="abstract" select="normalize-space($this/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:abstract)" />
     <xsl:variable name="authors" select="$this/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:name" />
     <xsl:variable name="publisher" select="$this/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:originInfo/mods:publisher" />
     <xsl:variable name="publisher_enc" select="encode-for-uri($publisher)" />
     
     <rdf:Description  rdf:about="{$baseURI}oai/pubid{$identifier}">
      <localVivo:harvestedBy>oaifetch_Harvester</localVivo:harvestedBy>
      
      
      <xsl:if test="normalize-space($publisher)">
      <vivo:hasPublisherVenue rdf:resource="{$baseURI}publisher/{$publisher_enc}" />
      </xsl:if>
      
      <bibo:abstract><xsl:value-of select="$abstract" /> </bibo:abstract>
       
       
      <rdfs:label><xsl:value-of select="$title" /> </rdfs:label> 
      <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
      <rdf:type rdf:resource="http://purl.obolibrary.org/obo/IAO_0000030" />
      
      <xsl:if test="normalize-space($pubDate)">
      <vivo:dateTimeValue rdf:resource="{$baseURI}dateTime/dateTime{$pubDate}" />
      </xsl:if>  
      
      <localVivo:docType><xsl:value-of select="$docType" /> </localVivo:docType>
      <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document" />
      <rdf:type rdf:resource="http://purl.obolibrary.org/obo/IAO_0000030" />
      <xsl:call-template name="documentType">
         <xsl:with-param name="type" select="lower-case($docType)" />
      </xsl:call-template>
       
      <xsl:apply-templates select="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:classification"  />
      <xsl:apply-templates select="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:subject"  />
      <xsl:apply-templates select="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:location"  />
      <xsl:apply-templates select="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:name" mode="authorRef" />
      
      
    </rdf:Description> 
    
    <xsl:apply-templates select="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:name" mode="authorFull" >
    <xsl:with-param name="pubid" select="$identifier" />
    </xsl:apply-templates>
    
    <xsl:if test="normalize-space($pubDate)" >
    <xsl:call-template name="dateTime">
       <xsl:with-param name="datestring" select="$pubDate" />
    </xsl:call-template>
    </xsl:if>
    
    <xsl:if test="normalize-space($publisher)" > 
    <xsl:call-template name="publisher">
       <xsl:with-param name="publisher" select="$publisher" />
       <xsl:with-param name="publisher_enc" select="$publisher_enc" />
       <xsl:with-param name="pubid" select="$identifier" />
    </xsl:call-template>
    </xsl:if>
    
    <!-- apply templates to ignore elements we don't need --> 
    <xsl:apply-templates />
    </rdf:RDF>
  </xsl:template>
  
  <!-- tempates to hide unused elements -->
  <xsl:template match="mets:metsHdr">
  </xsl:template>
  <xsl:template match="mets:dmdSec">
  </xsl:template>
  <xsl:template match="mets:amdSec">
  </xsl:template>
  <xsl:template match="mets:fileSec">
  </xsl:template>
  <xsl:template match="mets:structMap">
  </xsl:template>
  
  <!-- template for classification ...usually lch -->
  <xsl:template match="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:classification">
        <xsl:variable name="authority" select='@authority' />
        <localVivo:authority><xsl:value-of select="$authority" /></localVivo:authority>
	    <xsl:if test="normalize-space(.)" > 
		<vivo:freetextKeyword><xsl:value-of select="." /></vivo:freetextKeyword>
		</xsl:if>
  </xsl:template>
  
  <!-- template for language -->
  <xsl:template match="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:language">
	    <xsl:if test="normalize-space(mods:languageTerm)" > 
		<localVivo:language><xsl:value-of select="mods:languageTerm" /></localVivo:language>
		</xsl:if>
  </xsl:template>
  
  <!-- template for language -->
  <xsl:template match="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:location">
	     
		<localVivo:location><xsl:value-of select="." /></localVivo:location>
		 
  </xsl:template>
  
  <!-- template for language -->
  <xsl:template match="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:subject">
        <xsl:variable name="authority" select='@authority' />
        <localVivo:authority><xsl:value-of select="$authority" /></localVivo:authority>
	    <xsl:if test="normalize-space(mods:geographic)" > 
		<localVivo:modsGeographic><xsl:value-of select="mods:languageTerm" /></localVivo:modsGeographic>
		</xsl:if>
  </xsl:template>
  
  <!-- template for authorship reference -->
  <xsl:template match="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:name" mode="authorRef">
     <vivo:relatedBy rdf:resource="{$baseURI}authorship/authorship{position()}" />
  </xsl:template>
  
  <!-- Template for including authorships, authors and vcards -->
  <xsl:template match="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:name" mode="authorFull">
     
     <xsl:param name="pubid" /> 
      
     <xsl:variable name="givenName" select='mods:namePart[@type="given"]' />
     <xsl:variable name="familyName" select='mods:namePart[@type="family"]' />   
     <xsl:variable name="nameRole" select="mods:role/mods:roleTerm" />
      
      <xsl:variable name="fullName"><xsl:value-of select = "$familyName" />, <xsl:value-of select = "$givenName" /></xsl:variable>
      
      
      <!-- authorship -->
		<rdf:Description rdf:about="{$baseURI}authorship/authorship{position()}">
		   <localVivo:harvestedBy>oaifetch_Harvester</localVivo:harvestedBy>
		   
		   <!-- <xsl:if test="normalize-space($pubid)" >
           <localVivo:pubid><xsl:value-of select="$pubid" /></localVivo:pubid>
           </xsl:if>
		   <xsl:if test="normalize-space($givenName)" >
           <localVivo:givenName><xsl:value-of select="$givenName" /></localVivo:givenName>
           </xsl:if>
           <xsl:if test="normalize-space($familyName)" >
           <localVivo:familyName><xsl:value-of select="$familyName" /></localVivo:familyName>
           </xsl:if>
           <xsl:if test="normalize-space($nameRole)" >
           <localVivo:nameRole><xsl:value-of select="$nameRole" /></localVivo:nameRole>
           </xsl:if> -->
           
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship" />
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
			<rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000020" />
			<rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001" />
			<rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002" />
			<xsl:choose>
				<xsl:when test="$givenName">
					<rdfs:label>Authorship for <xsl:value-of select = "$givenName" />, <xsl:value-of select = "$familyName" /></rdfs:label>
				</xsl:when>
				<xsl:when test="$familyName">
					<rdfs:label>Authorship for <xsl:value-of select="$familyName" /></rdfs:label>
				</xsl:when> 
			</xsl:choose>
			<vivo:relates rdf:resource="{$baseURI}author/pubid{$pubid}author{position()}" />
			<vivo:relates rdf:resource="{$baseURI}oai/pubid{$pubid}" />
			<vivo:authorRank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></vivo:authorRank>
		</rdf:Description>
		
		<!-- author -->
		<rdf:Description rdf:about="{$baseURI}author/pubid{$pubid}author{position()}">
		   <rdf:type rdf:resource = "http://xmlns.com/foaf/0.1/Person"/>
           <vitro:mostSpecificType rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
           <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$fullName" /></rdfs:label>
           <obo:ARG_2000028 rdf:resource="{$baseURI}vcard/pubid{$pubid}vcard{position()}"/>
		</rdf:Description>
		
		<!-- vcard -->
		<rdf:Description rdf:about="{$baseURI}vcard/pubid{$pubid}vcard{position()}">
		   <obo:ARG_2000029 rdf:resource="{$baseURI}author/pubid{$pubid}author{position()}"/>
           <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#Individual"/>
           <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual"/>
           <rdf:type rdf:resource="http://purl.obolibrary.org/obo/ARG_2000379"/>
           <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard for: <xsl:value-of select = "$fullName" /></rdfs:label>
           <vcard:hasName rdf:resource="{$baseURI}vcardName/pubid{$pubid}vcardName{position()}"/>
            
		</rdf:Description>
		
		<!-- vcard name -->
		<rdf:Description rdf:about="{$baseURI}vcardName/pubid{$pubid}vcardName{position()}">
		   <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Name"/> 
           <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#Name"/>
           <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">vCard name for: <xsl:value-of select = "$fullName" /></rdfs:label>
           <vcard:givenName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$givenName" /></vcard:givenName>     
           <vcard:familyName rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select = "$familyName" /></vcard:familyName>
		</rdf:Description> 
  </xsl:template>
  
  <xsl:template name="dateTime">
    <xsl:param name="datestring"/> 
    <xsl:variable name="len" select='string-length($datestring)' />
    
    <xsl:choose>
    <xsl:when test="$len=10">
    <rdf:Description rdf:about="{$baseURI}dateTime/dateTime{$datestring}">
       <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
       <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
       <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$datestring" />T00:00:00</vivo:dateTime>
    </rdf:Description>
    </xsl:when>
    <xsl:when test="$len=7">
    <rdf:Description rdf:about="{$baseURI}dateTime/dateTime{$datestring}">
       <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
       <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthPrecision"/>
       <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$datestring" />-01T00:00:00</vivo:dateTime>
    </rdf:Description>
    </xsl:when>
    <xsl:when test="$len=4">
    <rdf:Description rdf:about="{$baseURI}dateTime/dateTime{$datestring}">
       <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
       <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
       <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$datestring" />-01-01T00:00:00</vivo:dateTime>
    </rdf:Description>
    </xsl:when>
   
    <xsl:otherwise>
			
	</xsl:otherwise>
	 </xsl:choose>   
  </xsl:template>
  
   <xsl:template name="publisher">
     <xsl:param name="publisher"/>
     <xsl:param name="publisher_enc"/> 
     <xsl:param name="pubid"/>
     
     <rdf:Description rdf:about="{$baseURI}publisher/{$publisher_enc}"> 
     <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Publisher" /> 
     <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
     <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000004" />
     <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002" />
     <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$publisher" /></rdfs:label>
     <vivo:publisherOf rdf:resource="{$baseURI}oai/pubid{$pubid}" />
     </rdf:Description>
   </xsl:template>
   
   <xsl:template name="documentType">
    <xsl:param name="type" />
    <xsl:choose>
       <xsl:when test="$type='article'">
	      <rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
	   </xsl:when>
	   <xsl:when test="$type='article'">
	      <rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
	   </xsl:when>
       <xsl:when test="starts-with($type, 'journal article')">
	      <rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
	   </xsl:when>
	   <xsl:when test="$type='bibliography'">
	      <rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
	   </xsl:when> 
	   <xsl:when test="$type='conference abstract'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
	   </xsl:when>
	   <xsl:when test="$type='conference paper'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
	   </xsl:when>
	   <xsl:when test="$type='conference poster'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePoster" />
	   </xsl:when>
	   <xsl:when test="$type='book'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
	   </xsl:when>
	   <xsl:when test="$type='book chapter'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
	   </xsl:when>
	   <xsl:when test="$type='data set'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Dataset" />
	   </xsl:when>
	   <xsl:when test="$type='report'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/Report" />
	   </xsl:when>
	   <xsl:when test="$type='lecture speech'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Speech" />
	   </xsl:when>
	   <xsl:when test="$type='dissertation'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Thesis" />
	   </xsl:when>
	   <xsl:when test="$type='directory'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
	   </xsl:when>
	   <xsl:when test="$type='manual'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
	   </xsl:when>
	   <xsl:when test="$type='dictionary entry'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
	   </xsl:when>
	   <xsl:when test="$type='encyclopedia entry'">
			<rdf:type rdf:resource="http://purl.org/ontology/bibo/ReferenceSource" />
	   </xsl:when>
	   <xsl:when test="$type='review'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
	   </xsl:when> 
	   <xsl:when test="$type='book review'">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Review" />
	   </xsl:when>
	   <xsl:otherwise>
			
	   </xsl:otherwise>
	   
			
	</xsl:choose> 
  
  </xsl:template>
  
  
  
  

  
  <xsl:template name="concept">
  <xsl:param name="label" />
  <xsl:param name="pubid" />
  <rdf:Description rdf:about="{$baseURI}concept/{$label}">
       <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/> 
       <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$label"/></rdfs:label> 
       <vivo:subjectAreaOf rdf:resource="{$baseURI}oai/pubid{$pubid}" />
  </rdf:Description>
  </xsl:template>
  
  <xsl:template name="conceptRef">
  <xsl:param name="label" />  
  <vivo:hasSubjectArea rdf:resource="{$baseURI}concept/{$label}" />
  </xsl:template>
	 
</xsl:stylesheet>
