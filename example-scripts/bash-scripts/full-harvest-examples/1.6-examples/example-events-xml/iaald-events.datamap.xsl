  <!--
    Copyright (c) 2010-2011 VIVO Harvester Team. For full list of
    contributors, please see the AUTHORS file provided. All rights
    reserved. This program and the accompanying materials are made
    available under the terms of the new BSD license which accompanies
    this distribution, and is available at
    http://www.opensource.org/licenses/bsd-license.html
  -->
  <!-- <?xml version="1.0"?> -->
  <!--
    Header information for the Style Sheet The style sheet requires xmlns
    for each prefix you use in constructing the new elements
  -->
<xsl:stylesheet version="2.0" 
  xmlns:atom="http://www.w3.org/2005/Atom"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:core="http://vivoweb.org/ontology/core#"
  xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:localVivo="http://vivo.sample.edu/ontology/"
  xmlns:node-resource="http://vivo.example.com/harvest/iaald_events/fields/item/"
  xmlns:score="http://vivoweb.org/ontology/score#" 
  xmlns:gCal="http://schemas.google.com/gCal/2005"
  xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
  xmlns:obo="http://purl.obolibrary.org/obo/"
  xmlns:gd="http://schemas.google.com/g/2005" xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:agrivivo= "http://www.agrivivo.net/ontology/agrivivo#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:placedata="java:edu.cornell.mannlib.harvester.xslt.DisambiguatePlace" extension-element-prefixes="placedata">

  <!-- This will create indenting in xml readers -->
  <xsl:output method="xml" indent="yes" />
  <xsl:variable name="baseURI">http://vivo.example.com/harvest/iaald_events/</xsl:variable>

  <!--
    The main node of the record loaded This serves as the header of the
    RDF file produced
  -->
  <xsl:template match="rdf:RDF">
    <rdf:RDF
      xmlns:atom="http://www.w3.org/2005/Atom"
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:localVivo="http://vivo.sample.edu/ontology/"
      xmlns:dc="http://purl.org/dc/elements/1.1/"
      xmlns:owl="http://www.w3.org/2002/07/owl"
      xmlns:score="http://vivoweb.org/ontology/score#"
      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
      xmlns:gCal="http://schemas.google.com/gCal/2005"
      xmlns:gd="http://schemas.google.com/g/2005"
      xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
      xmlns:obo="http://purl.obolibrary.org/obo/"
    >
    <xsl:apply-templates select="rdf:Description" />
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="rdf:Description">
    <xsl:variable name="this" select="." />
    <xsl:variable name="node">
      <xsl:analyze-string select="../@xml:base" regex="^.*/([^/]+?)$">
        <xsl:matching-substring>
          <xsl:value-of select="regex-group(1)" />
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$node = 'entry'">
        <xsl:call-template name="item">
          <xsl:with-param name="this" select="$this" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="item" 
     xmlns:placedata="java:edu.cornell.mannlib.harvester.xslt.DisambiguatePlace"
     extension-element-prefixes="placedata"
  >

     <xsl:param name='this' />
     <xsl:variable name="url" select="$this/atom:id" />
     <xsl:variable name="uid"><xsl:value-of select="$this/gCal:uid/@value" /></xsl:variable>
     <xsl:variable name="startDate" select="$this/gd:when/@startTime" />
     <xsl:variable name="endDate" select="$this/gd:when/@endTime" />
     
     <xsl:variable name="location" select="$this/gd:where/@valueString" />
     <!--
     <xsl:variable name="countryName" select="placedata:lookup($location)" />
    --> 

   <rdf:Description rdf:about="{$baseURI}event/id-{$uid}">
      <localVivo:uId rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="$uid" /></localVivo:uId>
      <localVivo:harvestedBy>iaald_events-Harvester </localVivo:harvestedBy>
      <localVivo:harvestedFrom rdf:resource="{$baseURI}org/iaald-events" />
      <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
      <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
      <rdfs:label><xsl:value-of select="$this/atom:title" /></rdfs:label>
      <core:currentMemberOf rdf:resource="{$baseURI}org/iaald-events"  />
      <core:webpage><xsl:value-of select="$this/atom:content" /></core:webpage>
      <core:dateTimeInterval rdf:resource="{$baseURI}timeInterval/start{$startDate}toEnd{$endDate}" />
      <!--
      <core:hasGeographicLocation rdf:resource="{$countryName}" />
      -->
   </rdf:Description>

    <!-- The beginning of the dateTimeInterval subgroup -->
    <!-- using the dates as part of the identifier allows dates to automatically align with each other based on that value. -->
    <rdf:Description rdf:about="{$baseURI}timeInterval/start{$startDate}toEnd{$endDate}">
       <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
       <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
       <core:start rdf:resource="{$baseURI}timeInterval/date{$startDate}"/>
       <core:end rdf:resource="{$baseURI}timeInterval/date{$endDate}"/>
    </rdf:Description>

    <rdf:Description rdf:about="{$baseURI}timeInterval/date{$startDate}">
        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$startDate" />T00:00:00</core:dateTime>
    </rdf:Description>

    <rdf:Description rdf:about="{$baseURI}timeInterval/date{$endDate}">
        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$endDate" />T00:00:00</core:dateTime>
    </rdf:Description>
    <!-- The end of the dateTimeInterval subgroup -->

    <!-- the beginning of the geographicLocation -->
    <!--
    <rdf:Description rdf:about="{$baseURI}location/{$countryName}">
      <rdfs:label><xsl:value-of select="$countryName" /></rdfs:label>
      <localVivo:harvestedBy>iaald_events-Harvester </localVivo:harvestedBy> 
      <core:geographicLocationOf rdf:resource="{$baseURI}event/id-{$uid}"/>
    </rdf:Description>
    -->

    <rdf:Description rdf:about="{$baseURI}org/iaald-events">
      <localVivo:orgId>1-iaald-events</localVivo:orgId>
      <localVivo:harvestedBy>iaald_events-Harvester</localVivo:harvestedBy>
      <localVivo:harvesterFor rdf:resource="{$baseURI}event/id-{$uid}"/>
      <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
      <rdf:type rdf:resource="http://www.agrivivo.net/ontology/agrivivo#DataProvider"/>
      <rdfs:label>Iaald Events</rdfs:label>
      <core:overview>For over 50 years, the International Association of Agricultural Information Specialists has been the leading global Community of practice for information specialists contributing to a more productive and sustainable use of the worlds land, water, renewable resources, and improved livelihoods of rural communities. </core:overview>
      <core:abbreviation>IaaldEvents</core:abbreviation>
      <core:email>info.iaald@gmail.com</core:email>
      <core:hasCurrentMember rdf:resource="{$baseURI}event/id-{$uid}"/>   
      <core:webpage>
         <rdf:Description rdf:about="{$baseURI}webpage/iaald-events">
	    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink"></rdf:type>
            <core:linkAnchorText>Iaald Events Web Page</core:linkAnchorText>
            <core:linkURI>http://www.iaald.org/googleevents</core:linkURI>
            <core:webpageOf rdf:resource="{$baseURI}org/iaald-events"/>
         </rdf:Description>
      </core:webpage>
   </rdf:Description>

  </xsl:template>

</xsl:stylesheet>
