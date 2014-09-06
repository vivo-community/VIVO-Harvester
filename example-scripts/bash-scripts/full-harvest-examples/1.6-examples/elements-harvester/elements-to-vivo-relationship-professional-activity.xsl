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
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:symp="http://www.symplectic.co.uk/vivo/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                xmlns:obo="http://purl.obolibrary.org/obo/"
                exclude-result-prefixes="rdf rdfs bibo vivo foaf score ufVivo vitro api symp svfn config xs"
        >

    <!--
        Template for handling relationships between users and professional activities.
    -->

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-activity.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <xsl:template match="api:relationship[@type='activity-user-association']">

        <xsl:variable name="associationURI" select="svfn:relationshipURI(.,'activity-user-association')" />

        <!-- Get the activity object reference from the relationship -->
        <xsl:variable name="activity" select="api:related/api:object[@category='activity']" />

        <!-- Get the user object reference from the relationship -->
        <xsl:variable name="user" select="api:related/api:object[@category='user']" />

        <xsl:variable name="fullUserObj" select="svfn:fullObject($user)" />

        <xsl:variable name="fullActivityObj" select="svfn:fullObject($activity)" />

        <!-- Let's define some variables that will be used across all Professional Activities -->

        <xsl:variable name="userId"><xsl:value-of select="$fullUserObj/symp:entry/api:object/@username"/></xsl:variable>

        <xsl:variable name="usStateName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-us-state-province']/api:text"/>

        <xsl:variable name="countryNameRaw" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-country-name']/api:text"/>

        <xsl:variable name="countryName" select="concat(upper-case(substring($countryNameRaw,1,1)),lower-case(substring($countryNameRaw,2)))"/>

        <xsl:variable name="organization" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-institution-organization-agency-name']/api:text"/>


        <xsl:variable name="organizationURI">
         <xsl:if test="$organization">
           <xsl:value-of select="concat($baseURI, 'organization-', svfn:stringToURI($organization))" />
         </xsl:if>
        </xsl:variable>

        <xsl:variable name="startDate" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-start-date']/api:date/api:year"/>

        <xsl:variable name="endDate" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-end-date']/api:date/api:year"/>

        <xsl:variable name="orgStateURI" select="concat('http://dbpedia.org/resource/',$usStateName)"/>

        <xsl:variable name="countryURI" select="concat('http://aims.fao.org/aos/geopolitical.owl#',$countryName)"/>

        <!--




        You are now Entering the "Specific Activity Processing" Zone




        -->


        <!-- We need to establish this is an honor-award relationship we are processing before proceeding further -->

        <xsl:if test="$fullActivityObj/symp:entry/api:object[@type='distinction']">

            <xsl:variable name="honorAwardName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='title']/api:text"/>

            <xsl:variable name="awardingOrganization" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='institution']/api:addresses/api:address/api:line[@type='organisation']"/>

            <xsl:variable name="awardingOrganizationURI">
                <xsl:if test="$awardingOrganization">
                    <xsl:value-of select="concat($baseURI, 'organization-', svfn:stringToURI($awardingOrganization))" />
                </xsl:if>
            </xsl:variable>


            <xsl:variable name="honorAwardURI">
                <xsl:if test="$honorAwardName">
                    <xsl:value-of select="concat($baseURI, 'award-', svfn:stringToURI($honorAwardName))" />
                </xsl:if>
            </xsl:variable>

            <xsl:variable name="honorAwardDate" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='start-date']"/>

            <!--Add a reference to the association object to the activity object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$honorAwardURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Award"/>
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
                    <rdfs:label><xsl:value-of select="$honorAwardName"/></rdfs:label>
                    <vivo:relatedBy rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>

            <!--Add a reference to the association object to the user object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="svfn:userURI($user)" />
                <xsl:with-param name="rdfNodes">
                    <vivo:relatedBy rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Output the Awarding Organization Object, if there is one -->
            <xsl:if test="$awardingOrganization">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$awardingOrganizationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
                    <rdfs:label><xsl:value-of select="$awardingOrganization"/></rdfs:label>
                    <vivo:assigns rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>


            <!-- Create a URI for the associated award date, and create the award date object (if there is one) -->
            <xsl:variable name="honorAwardBeginDateObjectURI" select="concat($associationURI, '-award-date')" />
            <xsl:variable name="honorAwardBeginDateObject" select="svfn:renderDateObject(.,$honorAwardBeginDateObjectURI,$honorAwardDate)" />

            <!-- Output the association object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$associationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardReceipt"/>
                    <vivo:relates rdf:resource="{$honorAwardURI}"/>
                    <vivo:relates rdf:resource="{svfn:userURI($user)}"/>

                    <!-- If the date object exists (check for child nodes), output a reference to it -->
                    <xsl:if test="$honorAwardBeginDateObject/*" >
                        <vivo:dateTimeValue rdf:resource="{$honorAwardBeginDateObjectURI}"/>
                    </xsl:if>
                    <xsl:if test="$awardingOrganization">
                        <vivo:assignedBy rdf:resource="{$awardingOrganizationURI}"/>
                    </xsl:if>

                    <!-- Grab the User and Activity Object values to Label the Relationship Object Appropriately. -->
                    <rdfs:label>
                        <xsl:value-of select="$honorAwardName"/>
                        <xsl:text> (</xsl:text>
                        <xsl:value-of select="$fullUserObj/symp:entry/api:object/api:first-name"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="$fullUserObj/symp:entry/api:object/api:last-name"/>
                        <xsl:text>)</xsl:text>

                        <!-- Add an award date to the Relationship Label, if there is one. -->
                        <!--<xsl:if test="$honorAwardBeginDateObject/*" >-->
                            <!--<xsl:text> - </xsl:text>-->
                            <!--<xsl:value-of select="$honorAwardDate"/>-->
                        <!--</xsl:if>-->
                    </rdfs:label>

                </xsl:with-param>
            </xsl:call-template>

            <xsl:copy-of select="$honorAwardBeginDateObject" />
        </xsl:if>

        <!-- We need to establish this is an education relationship we are processing before proceeding further -->

        <xsl:if test="$fullActivityObj/symp:entry/api:object[@type='c-education']">

            <!-- Establish the many variables we will need to facilitate the mapping -->

            <xsl:variable name="degreeName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-degree']/api:text"/>

            <xsl:variable name="degreeBaseURI"><xsl:text>http://vivoweb.org/ontology/degree/academicDegree</xsl:text></xsl:variable>

            <xsl:variable name="degreeFocusName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-area-major-specialization-specialty-name']/api:text"/>

            <xsl:variable name="departmentName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-department-division-society-name']/api:text"/>

            <xsl:variable name="educationalProcessName" select="concat($userId,'-',$degreeName,'-',$departmentName,'-',$organization)"/>

            <xsl:variable name="educationalProcessURI" select="concat($baseURI, 'educationalProcess-',svfn:stringToURI($educationalProcessName))"/>

            <xsl:variable name="startDateURI" select="concat($educationalProcessURI,'-startDate')"/>

            <xsl:variable name="endDateURI" select="concat($educationalProcessURI,'-endDate')"/>

            <xsl:variable name="dateIntervalURI" select="concat($educationalProcessURI,'-dateInterval-',$startDate,'-',$endDate)"/>

            <xsl:variable name="customDegreeType">
                <xsl:choose>
                <xsl:when test="$degreeName='BMSC' or
                                          $degreeName='BSE' or
                                          $degreeName='BSN' or
                                          $degreeName='DPT' or
                                          $degreeName='MBChB' or
                                          $degreeName='MDP' or
                                          $degreeName='MHA' or
                                          $degreeName='MMSc' or
                                          $degreeName='MMSc-PA' or
                                          $degreeName='MSc' or
                                          $degreeName='MSCR' or
                                          $degreeName='MSN' or
                                          $degreeName='PsyD'">
                        <xsl:text>yes</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>no</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>


            </xsl:variable>

            <xsl:variable name="degreeURI">
            <xsl:if test="$degreeName">
            <xsl:choose>
                <xsl:when test="$degreeName='BA' or $degreeName='B.A.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'77')"/>
                </xsl:when>
                <xsl:when test="$degreeName='BBA' or $degreeName='B.B.A.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'13')"/>
                </xsl:when>
                <xsl:when test="$degreeName='BS' or $degreeName='B.S.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'33')"/>
                </xsl:when>
                <xsl:when test="$degreeName='DO' or $degreeName='D.O.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'47')"/>
                </xsl:when>
                <xsl:when test="$degreeName='DPhil'">
                    <xsl:copy-of select="concat($degreeBaseURI,'98')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MA' or $degreeName='M.A.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'71')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MBA' or $degreeName='M.B.A.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'73')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MD' or $degreeName='M.D.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'77')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MHA' or $degreeName='M.H.A.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'83')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MPH' or $degreeName='M.P.H.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'113')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MS' or $degreeName='M.S.'">
                    <xsl:copy-of select="concat($degreeBaseURI,'91')"/>
                </xsl:when>
                <xsl:when test="$degreeName='MSPH'">
                    <xsl:copy-of select="concat($degreeBaseURI,'114')"/>
                </xsl:when>
                <xsl:when test="$degreeName='PHD' or $degreeName='Ph.D.' or $degreeName='PhD'">
                    <xsl:copy-of select="concat($degreeBaseURI,'98')"/>
                </xsl:when>
                <!-- If Degree Value doesn't exist in Standard Ontology, Need to Create a new Record with that Degree and attach User to It -->
                <xsl:otherwise>
                    <xsl:copy-of select="concat($degreeBaseURI,'-',svfn:stringToURI($degreeName))"/>
                </xsl:otherwise>
            </xsl:choose>
            </xsl:if>
            </xsl:variable>

            <!--Add a reference to the Educational Process object to the User object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="svfn:userURI($user)" />
                <xsl:with-param name="rdfNodes">
                    <!-- Relate the User Object to the Educational Process -->
                    <obo:RO_0000056 rdf:resource="{$educationalProcessURI}"/>
                    <vivo:relatedBy rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Output the Organization Object -->
            <xsl:if test="$organization">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$organizationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <rdfs:label><xsl:value-of select="$organization"/></rdfs:label>
                    <obo:RO_0000056 rdf:resource="{$educationalProcessURI}"/>
                    <xsl:if test="$usStateName">
                    <obo:RO_0001025 rdf:resource="{$orgStateURI}"/>
                    </xsl:if>
                    <xsl:if test="$countryName">
                    <obo:RO_0001025 rdf:resource="{$countryURI}"/>
                    </xsl:if>

                </xsl:with-param>
            </xsl:call-template>


            <xsl:if test="$usStateName and $organization">
            <!-- Add a reference to the Organization to the US State -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$orgStateURI" />
                <xsl:with-param name="rdfNodes">
                    <obo:RO_0001015 rdf:resource="{$organizationURI}"/>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <xsl:if test="$countryName and $organization">
                <!-- Add a reference to the Organization to the US State -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$countryURI" />
                    <xsl:with-param name="rdfNodes">
                        <obo:RO_0001015 rdf:resource="{$organizationURI}"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            </xsl:if>

            <!-- Output the Educational Process Object -->
            <xsl:call-template name="render_rdf_object">
                 <xsl:with-param name="objectURI" select="$educationalProcessURI" />
                 <xsl:with-param name="rdfNodes">
                     <rdf:type rdf:resource="http://vivoweb.org/ontology/core#EducationalProcess"/>
                     <xsl:if test="$organization">
                     <obo:RO_0000057 rdf:resource="{$organizationURI}"/>
                     </xsl:if>
                     <obo:RO_0000057 rdf:resource="{svfn:userURI($user)}"/>
                     <xsl:if test="$departmentName">
                     <vivo:departmentOrSchool><xsl:value-of select="$departmentName"/></vivo:departmentOrSchool>
                     </xsl:if>
                     <xsl:if test="$degreeFocusName">
                     <vivo:majorField><xsl:value-of select="$degreeFocusName"/></vivo:majorField>
                     </xsl:if>
                     <obo:RO_0002234 rdf:resource="{$associationURI}"/>
                     <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}"/>
                  </xsl:with-param>
            </xsl:call-template>

            <!-- Output the Start Date Object -->
            <xsl:if test="$startDate">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$startDateURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                    <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate,'-01-01T00:00:00')"/></vivo:dateTime>
                    <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <!-- Output the End Date Object -->
            <xsl:if test="$endDate">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$endDateURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                    <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate,'-01-01T00:00:00')"/></vivo:dateTime>
                    <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision"/>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <!-- Output the Date Interval Object -->
            <xsl:if test="$startDate or $endDate">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
                    <xsl:if test="$startDate">
                    <vivo:start rdf:resource="{$startDateURI}"/>
                    </xsl:if>
                    <xsl:if test="$endDate">
                    <vivo:end rdf:resource="{$endDateURI}"/>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <!-- Output the Degree Type Object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$degreeURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicDegree"/>
                    <xsl:if test="$customDegreeType='yes'">
                    <rdfs:label>
                    <xsl:value-of select="$degreeName"/>
                    </rdfs:label>
                    </xsl:if>
                    <vivo:relatedBy rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Last but (certainly) not least(?), output the Awarded Degree (Relationship/Association) Object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$associationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardedDegree"/>
                    <rdfs:label>
                        <xsl:value-of select="$fullUserObj/symp:entry/api:object/api:last-name"/>
                        <xsl:text>,</xsl:text>
                        <xsl:value-of select="$fullUserObj/symp:entry/api:object/api:first-name"/>
                        <xsl:text>: </xsl:text>
                        <xsl:value-of select="$degreeName"/>
                    </rdfs:label>
                    <vivo:relates rdf:resource="{svfn:userURI($user)}"/>
                    <xsl:if test="$degreeName">
                    <vivo:relates rdf:resource="{$degreeURI}"/>
                    </xsl:if>
                    <xsl:if test="$organization">
                    <vivo:assignedBy rdf:resource="{$organizationURI}"/>
                    </xsl:if>
                 </xsl:with-param>
            </xsl:call-template>

        </xsl:if>

        <xsl:if test="$fullActivityObj/symp:entry/api:object[@type='c-professional-work-experience']">


            <xsl:variable name="appointmentCategory" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-appointment-category']/api:text"/>

            <xsl:variable name="appointmentType" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-appointment-type']/api:text"/>

            <xsl:variable name="positionTitle" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-position-role-title-name']/api:text"/>

            <xsl:variable name="startDateURI" select="concat($associationURI,'-startDate')"/>

            <xsl:variable name="endDateURI" select="concat($associationURI,'-endDate')"/>

            <xsl:variable name="dateIntervalURI" select="concat($associationURI,'-dateInterval-',$startDate,'-',$endDate)"/>

            <xsl:variable name="vcardURI" select="concat($baseURI, 'vcard-', $userId)" />

            <xsl:variable name="vcardTitleURI" select="concat($baseURI, 'vcardTitle-', $userId)" />

            <xsl:variable name="workOrganization">
                <xsl:choose>
                    <xsl:when test="not($organization)">
                        <xsl:value-of select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='organisation']/api:addresses/api:address/api:line[@type='organisation']"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$organization"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>



            <xsl:variable name="organizationURI">
                <xsl:if test="$workOrganization">
                    <xsl:value-of select="concat($baseURI, 'organization-', svfn:stringToURI($workOrganization))" />
                </xsl:if>
            </xsl:variable>

            <!--Output the Association Object-->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$associationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyPosition"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000020"/>
                    <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship"/>
                    <vivo:relates rdf:resource="{svfn:userURI($user)}"/>
                    <xsl:if test="$workOrganization">
                        <vivo:relates rdf:resource="{$organizationURI}"/>
                    </xsl:if>
                    <rdfs:label><xsl:value-of select="$positionTitle"/></rdfs:label>
                    <xsl:if test="$startDate">
                    <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}"/>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>

            <!--Add a reference to the association object to the user object -->
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="svfn:userURI($user)" />
                <xsl:with-param name="rdfNodes">
                    <vivo:relatedBy rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Output the Organization Object, if there is one -->
            <xsl:if test="$workOrganization">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$organizationURI" />
                <xsl:with-param name="rdfNodes">
                    <rdfs:label><xsl:value-of select="$workOrganization"/></rdfs:label>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <vivo:relatedBy rdf:resource="{$associationURI}"/>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <!-- If the Appointment Category is "Current" and the Type Is "Primary", change the User's Preferred Title to this partciular Position Title -->
            <xsl:if test="$appointmentCategory='Current Titles and Affiliations' and $appointmentType='Primary Academic Appointment'">

                <!-- Output VCard Title Object -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardTitleURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Title"/>
                        <vcard:title><xsl:value-of select="$positionTitle"/></vcard:title>
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Create (Preferred) Title reference to Existing VCard Record for the User -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardURI" />
                    <xsl:with-param name="rdfNodes">
                        <vcard:hasTitle rdf:resource="{$vcardTitleURI}"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <!-- Output the Date Interval Object -->
            <xsl:if test="$startDate">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
                    <vivo:start rdf:resource="{$startDateURI}"/>
                    <xsl:if test="$endDate">
                    <vivo:end rdf:resource="{$endDateURI}"/>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>
            </xsl:if>

            <!-- Add a reference to the Organization to the US State -->
            <xsl:if test="$usStateName and $organization">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$orgStateURI" />
                    <xsl:with-param name="rdfNodes">
                        <obo:RO_0001015 rdf:resource="{$organizationURI}"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <!-- Add a reference to the Organization to the US State -->
            <xsl:if test="$countryName and $organization">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$countryURI" />
                    <xsl:with-param name="rdfNodes">
                        <obo:RO_0001015 rdf:resource="{$organizationURI}"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>


        </xsl:if>

    </xsl:template>
</xsl:stylesheet>