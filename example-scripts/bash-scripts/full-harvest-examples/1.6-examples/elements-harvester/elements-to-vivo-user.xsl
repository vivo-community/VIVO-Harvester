<?xml version="1.0" encoding="UTF-8"?>
<!--
 | Copyright (c) 2012 Symplectic Limited. All rights reserved.
 | This Source Code Form is subject to the terms of the Mozilla Public
 | License, v. 2.0. If a copy of the MPL was not distributed with this
 | file, You can obtain one at http://mozilla.org/MPL/2.0/.
 -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:bibo="http://purl.org/ontology/bibo/"
                xmlns:vivo="http://vivoweb.org/ontology/core#"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:symp="http://www.symplectic.co.uk/vivo/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                xmlns:obo="http://purl.obolibrary.org/obo/"
                exclude-result-prefixes="rdf rdfs bibo vivo foaf score ufVivo vitro api symp svfn config xs"
        >

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match Elements objects of category 'user' -->
    <xsl:template match="api:object[@category='user']">

        <!-- Create a URI/Object Variables -->
        <xsl:variable name="userId"><xsl:value-of select="@username"/></xsl:variable>
        <xsl:variable name="employeeType"><xsl:value-of select="api:organisation-defined-data[@field-name='Employee Type']"/></xsl:variable>
        <xsl:variable name="vcardEmail"><xsl:value-of select="api:email-address" /></xsl:variable>
        <xsl:variable name="vcardPhone"><xsl:value-of select="api:organisation-defined-data[@field-name='Work Telephone']"/></xsl:variable>

        <xsl:variable name="vcardURI" select="concat($baseURI, 'vcard-', $userId)" />
        <xsl:variable name="vcardEmailURI" select="concat($baseURI, 'vcardEmail-', $userId)" />
        <xsl:variable name="vcardNameURI" select="concat($baseURI, 'vcardName-', $userId)" />
        <xsl:variable name="vcardPhoneURI" select="concat($baseURI, 'vcardTelephone-', $userId)" />
        <xsl:variable name="preferredTitle"><xsl:value-of select="api:title" /></xsl:variable>

        <!-- Ouput the RDF object for a user -->
        <xsl:call-template name="render_rdf_object">
            <!-- Generate the user URI from the object -->
            <xsl:with-param name="objectURI" select="svfn:userURI(.)" />
            <!-- Generate the property statements for the user object -->
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
                <xsl:if test="$employeeType = 'Faculty'">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember"/>
                </xsl:if>
                <xsl:if test="$employeeType != 'Faculty'">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Staff"/>
                </xsl:if>
                <rdfs:label><xsl:value-of select="api:last-name" />, <xsl:value-of select="api:first-name" /></rdfs:label>
                <xsl:if test="preferredTitle">
                <vivo:preferredTitle><xsl:value-of select="$preferredTitle"/></vivo:preferredTitle>
                </xsl:if>
                <obo:ARG_2000028 rdf:resource="{$vcardURI}"/>
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001"/>
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002"/>
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000004"/>
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
                <rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
            </xsl:with-param>
        </xsl:call-template>



        <!-- Create the V-Card Object for the User -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual" />
                <vcard:hasName rdf:resource="{$vcardNameURI}"/>
                <xsl:if test="$vcardEmail">
                <vcard:hasEmail rdf:resource="{$vcardEmailURI}"/>
                </xsl:if>
                <xsl:if test="$vcardPhone">
                <vcard:hasTelephone rdf:resource="{$vcardPhoneURI}"/>
                </xsl:if>
                <obo:ARG_2000029 rdf:resource="{svfn:userURI(.)}"/>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Create the Objects for the Related Contact / Identifiable Elements -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardNameURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Name"/>
                <vcard:givenName><xsl:value-of select="api:first-name"/></vcard:givenName>
                <vcard:familyName><xsl:value-of select="api:last-name"/></vcard:familyName>
            </xsl:with-param>
        </xsl:call-template>

        <xsl:if test="$vcardEmail">
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardEmailURI" />
            <xsl:with-param name="rdfNodes">
                <!-- Making the Email both an "Email" and "Work" type will render the field in the "Primary" section -->
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Email"/>
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Work"/>
                <vcard:email><xsl:value-of select="$vcardEmail"/></vcard:email>
            </xsl:with-param>
        </xsl:call-template>
        </xsl:if>

        <xsl:if test="$vcardPhone">
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardPhoneURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Telephone"/>
                <vcard:telephone>
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="substring($vcardPhone,1,3)"/>
                    <xsl:text>)</xsl:text>
                    <xsl:value-of select="substring($vcardPhone,4,3)"/>
                    <xsl:text>-</xsl:text>
                    <xsl:value-of select="substring($vcardPhone,7,4)"/>
                </vcard:telephone>
            </xsl:with-param>
        </xsl:call-template>
        </xsl:if>

    </xsl:template>

</xsl:stylesheet>
