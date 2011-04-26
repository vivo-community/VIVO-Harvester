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

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>
	
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivoweb.org/harvest/csvfile/</xsl:variable>
	
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    		xmlns:core="http://vivoweb.org/ontology/core#"
    		xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    		xmlns:score='http://vivoweb.org/ontology/score#'>
			<xsl:apply-templates select="rdf:Description" />		
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match="rdf:Description">
		<xsl:variable name="this" select="." />
		<xsl:call-template name="t_Grant">
		  <xsl:with-param name="this" select="$this" />
          <xsl:with-param name="grantid" select="$this/GrantCSVFile:GrantID" />
		</xsl:call-template>
	</xsl:template>
		
	<xsl:template name="t_Grant">
		<xsl:param name='grantid' />
		<xsl:param name='this' />
<!--	Creating a Grant-->
		<rdf:Description rdf:about="{$baseURI}grant/grant{$grantid}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Grant"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Agreement"/>
			<score:GrantID><xsl:value-of select="$grantid" /></score:GrantID>
			<rdfs:label><xsl:value-of select="$this/GrantCSVFile:GrantName"/></rdfs:label>
			<core:administeredBy>
<!--			Creating a department to match with or a stub if no match-->
				<rdf:Description rdf:about="{$baseURI}org/org{$this/GrantCSVFile:AdminDepartmentID}">
					<score:deptID><xsl:value-of select="$this/GrantCSVFile:AdminDepartmentID"/></score:deptID>
					<core:administers rdf:resource="{$baseURI}grant/grant{$grantid}" />
					<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
				</rdf:Description>
			</core:administeredBy>
			<core:totalAwardAmount><xsl:value-of select="$this/GrantCSVFile:Amount"/></core:totalAwardAmount>
            <core:sponsorAwardId><xsl:value-of select="$this/GrantCSVFile:SponAwardID"/></core:sponsorAwardId>
			
			<xsl:choose>
				<xsl:when test="string($this/GrantCSVFile:FlowThruOrg) = ''">
					<core:grantAwardedBy>
						<rdf:Description rdf:about="{$baseURI}sponsor/sponsor{$this/GrantCSVFile:AwardingOrgID}For{$grantid}">
							<rdfs:label><xsl:value-of select="$this/GrantCSVFile:AwardingOrg"/></rdfs:label>
                            <score:orgID><xsl:value-of select="$this/GrantCSVFile:AwardingOrgID"/></score:orgID>
							<core:awardsGrant rdf:resource="{$baseURI}grant/grant{$grantid}"/>
							<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
						</rdf:Description>
					</core:grantAwardedBy>
				</xsl:when>
				<xsl:otherwise>
					<core:grantSubcontractedThrough>
						<rdf:Description rdf:about="{$baseURI}sponsor/sponsor{$this/GrantCSVFile:AwardingOrgID}For{$grantid}">
							<rdfs:label><xsl:value-of select="$this/GrantCSVFile:AwardingOrg"/></rdfs:label>
                            <score:orgID><xsl:value-of select="$this/GrantCSVFile:AwardingOrgID"/></score:orgID>
							<core:subcontractsGrant rdf:resource="{$baseURI}grant/grant{$grantid}"/>
							<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
						</rdf:Description>
					</core:grantSubcontractedThrough>
					<core:grantAwardedBy>
						<rdf:Description rdf:about="{$baseURI}sponsor/sponsor{$this/GrantCSVFile:FlowThruOrgID}For{$grantid}">
							<rdfs:label><xsl:value-of select="$this/GrantCSVFile:FlowThruOrg"/></rdfs:label>
                            <score:orgID><xsl:value-of select="$this/GrantCSVFile:FlowThruOrgID"/></score:orgID>
							<core:awardsGrant rdf:resource="{$baseURI}grant/grant{$grantid}"/>
							<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
						</rdf:Description>
					</core:grantAwardedBy>
				</xsl:otherwise>
			</xsl:choose>
			
			<core:startDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date">
				<xsl:analyze-string select="$this/GrantCSVFile:StartDate" regex="^(....-..-..).*?$">
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(1)"/>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</core:startDate>
			<core:endDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date">
				<xsl:analyze-string select="$this/GrantCSVFile:EndDate" regex="^(....-..-..).*?$">
					<xsl:matching-substring>
						<xsl:value-of select="regex-group(1)"/>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</core:endDate>
		</rdf:Description>
        <xsl:when test="$this/GrantCSVFile:PIID != ''">
<!--            Creating the PI-->
	        <rdf:Description rdf:about="{$baseURI}piRole/inGrant{$grantid}For{$this/GrantCSVFile:PIID}">
	            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#PrincipalInvestigatorRole"/>
	            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InvestigatorRole"/>
	            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#ResearcherRole"/>
				<core:roleIn>
				    <rdf:Description rdf:about="{$baseURI}grant/grant{$grantid}">
				        <core:relatedRole rdf:resource="{$baseURI}piRole/inGrant{$grantid}For{$this/GrantCSVFile:PIID}"/>
				    </rdf:Description>
				</core:roleIn>
	            <core:principalInvestigatorRoleOf>
	                <rdf:Description rdf:about="{$baseURI}person/person{$this/GrantCSVFile:PIID}">
	                    <rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
	                    <score:personID><xsl:value-of select="$this/GrantCSVFile:PIID"/></score:personID>
	                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
	                    <core:hasPrincipalInvestigatorRole rdf:resource="{$baseURI}piRole/inGrant{$grantid}For{$this/GrantCSVFile:PIID}"/>
	                </rdf:Description>
	            </core:principalInvestigatorRoleOf>
			</rdf:Description>
        </xsl:when>
        
        <xsl:when test="$this/GrantCSVFile:CoPIID != ''">
<!--            Creating the CoPI-->
            <rdf:Description rdf:about="{$baseURI}piRole/inGrant{$grantid}For{$this/GrantCSVFile:CoPIID}">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#PrincipalInvestigatorRole"/>
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InvestigatorRole"/>
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#ResearcherRole"/>
                <core:roleIn>
                    <rdf:Description rdf:about="{$baseURI}grant/grant{$grantid}">
                        <core:relatedRole rdf:resource="{$baseURI}piRole/inGrant{$grantid}For{$this/GrantCSVFile:CoPIID}"/>
                    </rdf:Description>
                </core:roleIn>
                <core:co-PrincipalInvestigatorRoleOf>
                    <rdf:Description rdf:about="{$baseURI}person/person{$this/GrantCSVFile:CoPIID}">
                        <rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
                        <score:personID><xsl:value-of select="$this/GrantCSVFile:CoPIID"/></score:personID>
                        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
                        <core:hasCo-PrincipalInvestigatorRole rdf:resource="{$baseURI}piRole/inGrant{$grantid}For{$this/GrantCSVFile:CoPIID}"/>
                    </rdf:Description>
                </core:co-PrincipalInvestigatorRoleOf>
            </rdf:Description>
        </xsl:when>
	</xsl:template>
	
</xsl:stylesheet>