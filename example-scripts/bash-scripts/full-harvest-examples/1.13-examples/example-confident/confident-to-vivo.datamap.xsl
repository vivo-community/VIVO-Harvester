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
                xmlns:xs = 'http://www.w3.org/2001/XMLSchema'
                xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
                xmlns:core = 'http://vivoweb.org/ontology/core#'
                xmlns:obo = 'http://purl.obolibrary.org/obo/'
                xmlns:vitro = 'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
                xmlns:vcard = 'http://www.w3.org/2006/vcard/ns#'
                xmlns:bibo = 'http://purl.org/ontology/bibo/'>

    <xsl:output method = "xml" indent = "yes"/>
    <xsl:variable name = "baseURI">https://forschungsatlas.fid-bau.de/individual/</xsl:variable>

    <xsl:template match = "rdf:RDF">
        <rdf:RDF xmlns:rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                 xmlns:rdfs = 'http://www.w3.org/2000/01/rdf-schema#'
                 xmlns:core = 'http://vivoweb.org/ontology/core#'>
            <xsl:apply-templates select = "rdf:Description" />
        </rdf:RDF>
    </xsl:template>

    <xsl:template match = "rdf:Description">
        <xsl:variable name = "this" select = "." />
        <xsl:variable name = "fulltext" select = "$this/node-event/fulltext" />
        <xsl:variable name = "timestamp" select="$this/node-event/printouts/Start_Date/Start_Date_0/timestamp"/>
        <xsl:variable name = "startDate" select="$this/node-event/printouts/Start_Date/Start_Date_0/raw"/>
        <xsl:variable name = "endDate" select="$this/node-event/printouts/End_Date/End_Date_0/raw"/>
        <xsl:variable name = "displayTitle" select="$this/node-event/displaytitle"/>
        <xsl:variable name = "title" select="$this/node-event/printouts/Title/Title_0"/>
        <xsl:variable name = "url" select="$this/node-event/fullurl"/>
        <xsl:variable name = "doi" select="$this/node-event/printouts/DOI/DOI_0"/>
        <xsl:variable name = "eventSeriesUrl" select="$this/node-event/printouts/In_Event_Series/In_Event_Serie_0/fullurl"/>
        <xsl:variable name = "eventSeriesId" select="substring-after($this/node-event/printouts/In_Event_Series/In_Event_Serie_0/fulltext, ':')"/>
        <xsl:variable name = "eventSeriesTitle" select="$this/node-event/printouts/In_Event_Series/In_Event_Serie_0/displaytitle"/>
        <xsl:variable name = "concept" select="substring-after($this/node-event/printouts/Academic_Field/Academic_Field_0/fulltext, ':')"/>
        <xsl:variable name = "city" select="$this/node-event/printouts/City/City_0"/>

        <xsl:call-template name="t_Event">
            <xsl:with-param name="confId" select="replace(substring-after($fulltext, ':'),' ','_')"/>
            <xsl:with-param name="startDate">
                <xsl:call-template name="formatdate">
                    <xsl:with-param name="DateTimeStr" select="$startDate"/>
                </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="endDate">
                <xsl:call-template name="formatdate">
                    <xsl:with-param name="DateTimeStr" select="$endDate"/>
                </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="title" select="$title" />
            <xsl:with-param name="url" select="$url"/>
            <xsl:with-param name="doi" select="$doi"/>
            <xsl:with-param name="eventSeriesId" select="replace($eventSeriesId, ' ', '_')"/>
            <xsl:with-param name="eventSeriesTitle" select="$eventSeriesTitle"/>
            <xsl:with-param name="concept" select="$concept"/>
            <xsl:with-param name="city" select="$city"/>
        </xsl:call-template>
    </xsl:template>

    <!--    Creating an Event   -->
    <xsl:template name="t_Event">
        <xsl:param name = 'confId'/>
        <xsl:param name = "title"/>
        <xsl:param name = "url"/>
        <xsl:param name = "doi"/>
        <xsl:param name = "eventSeriesId"/>
        <xsl:param name = "eventSeriesTitle"/>
        <xsl:param name = "concept"/>
        <xsl:param name = "startDate"/>
        <xsl:param name = "endDate"/>
        <xsl:param name = "city"/>

        <rdf:Description rdf:about="{$baseURI}{$confId}">
            <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
            <rdfs:label><xsl:value-of select="$title"/></rdfs:label>
            <core:dateTimeInterval rdf:resource="{$baseURI}dti_{$confId}"/>
            <obo:ARG_2000029 rdf:resource="{$baseURI}vcard_{$confId}"/>
            <xsl:if test="$eventSeriesId">
                <obo:BFO_0000051 rdf:resource="{$baseURI}es_{$eventSeriesId}"/>
            </xsl:if>
            <xsl:if test="$doi">
                <bibo:doi><xsl:value-of select = "$doi" /></bibo:doi>
            </xsl:if>
            <xsl:if test="$city">
                <obo:RO_0001025 rdf:resource="{$baseURI}{replace($city,' ','_')}"/>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="$concept='Architecture'">
                    <core:hasSubjectArea rdf:resource="https://terminology.fraunhofer.de/voc/raum#R21144849761_2"/>
                </xsl:when>
                <xsl:when test="$concept='Civil_Engineering'">
                    <core:hasSubjectArea rdf:resource="https://terminology.fraunhofer.de/voc/bau#B10379666933_2"/>
                </xsl:when>
                <xsl:when test="$concept='Urban Studies'">
                    <core:hasSubjectArea rdf:resource="https://terminology.fraunhofer.de/voc/raum#R11164872567_4"/>
                </xsl:when>
            </xsl:choose>
        </rdf:Description>

        <!-- date time value start-->
        <xsl:if test="normalize-space( $startDate )">
            <rdf:Description rdf:about="{$baseURI}dtvs_{$confId}">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                    <xsl:value-of select="$startDate"/></core:dateTime>
                <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
            </rdf:Description>
        </xsl:if>

        <!-- date time value end-->
        <xsl:if test="normalize-space( $endDate )">
            <rdf:Description rdf:about="{$baseURI}dtve_{$confId}">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                    <xsl:value-of select="$endDate"/></core:dateTime>
                <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
            </rdf:Description>
        </xsl:if>

        <!-- date time interval -->
        <xsl:if test="normalize-space( $startDate )">
            <rdf:Description rdf:about="{$baseURI}dti_{$confId}">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
                <core:start rdf:resource="{$baseURI}dtvs_{$confId}"/>
                <core:end rdf:resource="{$baseURI}dtve_{$confId}"/>
            </rdf:Description>
        </xsl:if>

        <!-- vcard -->
        <rdf:Description rdf:about="{$baseURI}vcard_{$confId}">
            <obo:ARG_2000029 rdf:resource="{$baseURI}{$confId}"/>
            <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#Kind"/>
            <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual"/>
            <vcard:hasURL rdf:resource="{$baseURI}vcardURL_{$confId}"/>
        </rdf:Description>

        <!-- vcard URL -->
        <rdf:Description rdf:about="{$baseURI}vcardURL_{$confId}">
            <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#URL"/>
            <vitro:mostSpecificType rdf:resource="http://www.w3.org/2006/vcard/ns#URL"/>
            <vcard:url><xsl:value-of select="$url"/></vcard:url>
        </rdf:Description>

        <!-- event series -->
        <rdf:Description rdf:about="{$baseURI}es_{$eventSeriesId}">
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#EventSeries"/>
            <rdfs:label><xsl:value-of select="$eventSeriesTitle"/></rdfs:label>
        </rdf:Description>

        <!-- geographic location -->
        <rdf:Description rdf:about="{$baseURI}{replace($city,' ','_')}">
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#GeographicLocation"/>
            <rdfs:label><xsl:value-of select="$city"/></rdfs:label>
        </rdf:Description>

    </xsl:template>

    <xsl:template name="formatdate">
        <xsl:param name="DateTimeStr" /> <!-- date comes as 1/2018/7/23 -->
        <xsl:variable name="datestr">
            <xsl:value-of select="substring-after($DateTimeStr,'/')" />
        </xsl:variable>

        <xsl:variable name="yyyy">
            <xsl:value-of select="substring($datestr,1,4)" />
        </xsl:variable>
        
        <xsl:variable name="mm">
            <xsl:call-template name="formatmonth">
                <xsl:with-param name="month" select="substring-before(substring-after($datestr, '/'), '/')"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="dd">
            <xsl:call-template name="formatday">
                <xsl:with-param name="day" select="substring-after(substring-after($datestr, '/'), '/')"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:value-of select="concat($yyyy,'-',$mm,'-', $dd)" />
    </xsl:template>

    <xsl:template name="formatmonth">
        <xsl:param name="month" />
        <xsl:choose>
            <xsl:when test="string-length($month) = 1">
                <xsl:value-of select="concat('0',$month)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$month"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="formatday">
        <xsl:param name="day" />
        <xsl:choose>
            <xsl:when test="string-length($day) = 1">
                <xsl:value-of select="concat('0',$day)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$day"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
