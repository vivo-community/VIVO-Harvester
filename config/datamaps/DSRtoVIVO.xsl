<!--
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
-->
<!-- <?xml version="1.0"?> -->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns for each prefix you use in constructing
	the new elements
-->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
	xmlns:db-dbo.vwVIVO='jdbc:jtds:sqlserver://10.241.46.60:1433/DSR/fields/dbo.vwVIVO/'>
	<!--
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	 -->
	
	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>  
	
	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    		xmlns:core="http://vivoweb.org/ontology/core#"
    		xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    		xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
    		xmlns:score='http://vivoweb.org/ontology/score#' > 
			<xsl:apply-templates select="rdf:Description" />		
		</rdf:RDF>
	</xsl:template>
	<xsl:template match="rdf:Description">
		<xsl:variable name="rdfid" select="$this/@rdf:ID" />
		<xsl:analyze-string select="$rdfid" regex="^id_-_(.*?)(_-_.+)*?$">
			<xsl:matching-substring>
				<xsl:variable name="grantid" select="regex-group(1)" />
				<rdf:Description>
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Grant"/>
					<rdfs:label><xsl:value-of select="db-dbo.vwVIVO:Title" /></rdfs:label>
					<core:startDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="db-dbo.vwVIVO:ProjectBegin"/></core:startDate>
					<core:endDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="db-dbo.vwVIVO:ProjectEnd"/></core:endDate>
					<core:totalAwardAmount><xsl:value-of select="db-dbo.vwVIVO:TotalAwarded"/></core:totalAwardAmount>
					<ufVivo:ufid><xsl:value-of select="db-dbo.vwVIVO:PI_UFID"/></ufVivo:ufid>
					<score:AdministeredBy><xsl:value-of select="db-dbo.vwVIVO:PI__Dept"/></score:AdministeredBy>
					<score:AdminDeptID><xsl:value-of select="db-dbo.vwVIVO:PI_DeptID"/></score:AdminDeptID>
					<core:sponsorAwardId><xsl:value-of select="db-dbo.vwVIVO:PS__Contract"/></core:sponsorAwardId>
					<ufVivo:dsrNumber><xsl:value-of select="$grantid"/></ufVivo:dsrNumber>
				</rdf:Description>
			</xsl:matching-substring>
		</xsl:analyze-string>
	</xsl:template>
</xsl:stylesheet>