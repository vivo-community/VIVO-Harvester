<xsl:stylesheet version="2.0" 
    xmlns:geo="http://aims.fao.org/aos/geopolitical.owl#"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:ero="http://purl.obolibrary.org/obo/"
    xmlns:event="http://purl.org/NET/c4dm/event.owl#"
    xmlns:pvs="http://vivoweb.org/ontology/provenance-support#"
    xmlns:dcelem="http://purl.org/dc/elements/1.1/"
    xmlns:core="http://vivoweb.org/ontology/core#"
    xmlns:swrlb="http://www.w3.org/2003/11/swrlb#"
    xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
    xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:bibo="http://purl.org/ontology/bibo/"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:scires="http://vivoweb.org/ontology/scientific-research#"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
    xmlns:swrl="http://www.w3.org/2003/11/swrl#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" >
    
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivo.ufl.edu/harvested/ufl/</xsl:variable>	
        <xsl:variable name="term"><xsl:value-of select="TERM" /></xsl:variable>
	<xsl:variable name="courseName"><xsl:value-of select="CRS" /></xsl:variable>
	<xsl:variable name="sectionNumber"><xsl:value-of select="SECT" /></xsl:variable>
	<xsl:variable name="ufid"><xsl:value-of select="INS_UFID" /></xsl:variable>
	<xsl:variable name="instructorName"><xsl:value-of select="INSTRUCTOR" /></xsl:variable>

	<xsl:template match="/">
		<rdf:RDF xmlns:public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			 xmlns:core="http://vivoweb.org/ontology/core#"
    		         xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
   		 	 xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
    		         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    		         xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
    		         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" >	
    		<xsl:apply-templates select="row" />	
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="row">
	
		<!--Course Node-->
			<rdf:Description rdf:about="{$baseURI}/courses/{$courseName}">
				<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course"/>
				<ufVivo:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course"/>
				<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
				<rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
				<rdfs:label xml:lang="en-US">{$courseName}</rdfs:label>
				<!-- Relation to Course Section Node-->
				<ufVivo:courseForSection rdf:resource="{$baseURI}/courseSection/{$courseName}{$term}"/>
				<!-- Relation to Course Section Node End-->
			</rdf:Description>
		<!--Course Node End-->
		
		
	</xsl:template>


</xsl:stylesheet>
