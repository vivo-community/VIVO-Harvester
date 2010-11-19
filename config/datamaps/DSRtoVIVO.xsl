<!--
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
      James Pence
-->
<!-- <?xml version="1.0"?> -->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->
<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
	xmlns:db-dbo.vwVIVO='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwVIVO/'
	xmlns:db-dbo.vwContracts='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwContracts/'
	xmlns:db-dbo.vwProjectTeam='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwProjectTeam/'>
	
	<xsl:output method="xml" indent="yes"/>  
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    		xmlns:core="http://vivoweb.org/ontology/core#"
    		xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    		xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
    		xmlns:score='http://vivoweb.org/ontology/score#'
			xmlns:db-dbo.vwVIVO='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwVIVO/'
			xmlns:db-dbo.vwContracts='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwContracts/'
			xmlns:db-dbo.vwProjectTeam='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwProjectTeam/' > 
			<xsl:apply-templates select="rdf:Description" />		
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match="rdf:Description">
		<xsl:variable name="this" select="." />
		<xsl:analyze-string select="../@xml:base" regex="^.*/([^/]+?)$">
			<xsl:matching-substring>
				<xsl:variable name="table" select="regex-group(1)" />
				<xsl:variable name="rdfid" select="$this/@rdf:ID" />
				
				<xsl:analyze-string select="$rdfid" regex="^id_-_(.*?)(_-_.+)*?$">
					<xsl:matching-substring>
						<xsl:choose>
	<!--						<xsl:when test="$table = 'dbo.vwVIVO'">-->
	<!--							<xsl:call-template name="t_vwVIVO">-->
	<!--								<xsl:with-param name="ufid" select="$ufid" />-->
	<!--								<xsl:with-param name="this" select="$this" />-->
	<!--							</xsl:call-template>-->
	<!--						</xsl:when>-->
							<xsl:when test="$table = 'dbo.vwContracts'">
								<xsl:call-template name="t_vwContracts">
									<xsl:with-param name="grantid" select="regex-group(1)" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$table = 'dbo.vwProjectTeam'">
								<xsl:call-template name="t_vwProjectTeam">
									<xsl:with-param name="grantid" select="regex-group(1)" />
									<xsl:with-param name="this" select="$this" />
								</xsl:call-template>
							</xsl:when>
						</xsl:choose>
					</xsl:matching-substring>		
				</xsl:analyze-string>
			</xsl:matching-substring>
		</xsl:analyze-string>
	
	</xsl:template>
		
	<xsl:template name="t_vwVIVO">
	</xsl:template>
		
	<xsl:template name="t_vwContracts">
		<xsl:param name='grantid' />
		<xsl:param name='this' />
		<rdf:Description rdf:about="http://vivoweb.org/harvest/dsr/grant/grant{$grantid}">
		
			<ufVivo:harvestedBy>DSR-Harvester</ufVivo:harvestedBy>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Grant"/>
			<ufVivo:psContractNumber><xsl:value-of select="$grantid" /></ufVivo:psContractNumber>
			<core:hasPrincipalInvestigatorRole><xsl:value-of select="$this/db-dbo.vwContracts:ContractPI"/></core:hasPrincipalInvestigatorRole>
			<rdfs:label><xsl:value-of select="$this/db-dbo.vwContracts:Title"/></rdfs:label>
			<core:administeredBy><xsl:value-of select="$this/db-dbo.vwContracts:ContractDeptID"/></core:administeredBy>
			<core:totalawardamount><xsl:value-of select="$this/db-dbo.vwContracts:TotalAwarded"/></core:totalawardamount>
			<ufVivo:ufid><xsl:value-of select="$this/db-dbo.vwContracts:ContractUFID"/></ufVivo:ufid>
			<core:sponsorawardID><xsl:value-of select="$this/db-dbo.vwContracts:SponsorID"/></core:sponsorawardID>
			
						<xsl:choose>
							<xsl:when test="$this/db-dbo.vwProjectTeam:FlowThruSponsorID = '-'">
								<core:grantAwardedBy><xsl:value-of select="$this/db-dbo.vwContracts:Sponsor"/></core:grantAwardedBy>
							</xsl:when>
							<xsl:otherwise>
								<core:grantSubcontractedThrough><xsl:value-of select="$this/db-dbo.vwContracts:Sponsor"/></core:grantSubcontractedThrough>
								<core:grantAwardedBy><xsl:value-of select="$this/db-dbo.vwContracts:FlowThruSponsorID"/></core:grantAwardedBy>
								<core:grantAwardedBy><xsl:value-of select="$this/db-dbo.vwContracts:FlowThruSponsor"/></core:grantAwardedBy>
							</xsl:otherwise>
						</xsl:choose>
			
			<core:startDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date">
				<xsl:value-of select="$this/db-dbo.vwContracts:BEGIN_DT" />
			</core:startDate>
			<core:endDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date">
				<xsl:value-of select="$this/db-dbo.vwContracts:END_DT" />
			</core:endDate>
		</rdf:Description>
	</xsl:template>
		
	<xsl:template name="t_vwProjectTeam">
		<xsl:param name='grantid' />
		<xsl:param name='this' />
			<rdf:Description rdf:about="http://vivoweb.org/harvest/dsr/role/inGrant{$grantid}for{$this/db-dbo.vwProjectTeam:InvestigatorID}">
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#PrincipleInvestigatorRole"/>
				<score:ufid><xsl:value-of select="$this/db-dbo.vwProjectTeam:InvestigatorID"/></score:ufid>
				<core:roleIn>
					<rdf:Description rdf:about="http://vivoweb.org/harvest/dsr/grant/grant{$grantid}">
						<xsl:choose>
							<xsl:when test="$this/db-dbo.vwProjectTeam:isPI = 'Y'">
								<core:hasPrincipalInvestigatorRole rdf:description="http://vivoweb.org/harvest/dsr/role/inGrant{$grantid}for{$this/db-dbo.vwProjectTeam:InvestigatorID}"/>
							</xsl:when>
							<xsl:otherwise>
								<core:hasCo-PrincipalInvestigatorRole rdf:description="http://vivoweb.org/harvest/dsr/role/inGrant{$grantid}for{$this/db-dbo.vwProjectTeam:InvestigatorID}"/>
							</xsl:otherwise>
						</xsl:choose>
					</rdf:Description>
				</core:roleIn>
			</rdf:Description>
	</xsl:template>
	
</xsl:stylesheet>