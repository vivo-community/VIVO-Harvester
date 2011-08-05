<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:datetime="http://exslt.org/dates-and-times"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:public='http://vitro.mannlib.cornell.edu/ns/vitro/public#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
	xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'  >
    
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="baseURI">http://vivo.ufl.edu/harvested/</xsl:variable>
	
	<xsl:template match="/">
		<rdf:RDF xmlns:public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			xmlns:core="http://vivoweb.org/ontology/core#"
    		xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
   		 	xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
    		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    		xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
    		xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" >	
    		<xsl:apply-templates select="imageName" />	
		</rdf:RDF>
	</xsl:template>
	
	<xsl:template match="imageName">
		<rdf:Description rdf:about="{$baseURI}peopleImage/ufid{self::*}">  		
		 	 <public:mainImage rdf:resource="{$baseURI}mainImg/ufid{self::*}"/>	
 			 <ufVivo:ufid><xsl:value-of select="substring(current(),1,8)" /></ufVivo:ufid>
		</rdf:Description>		
		
		<rdf:Description rdf:about="{$baseURI}mainImg/ufid{self::*}">
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
  			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
  			<public:downloadLocation rdf:resource="{$baseURI}fullDirDownload/ufid{self::*}"/>
  			<public:thumbnailImage rdf:resource="{$baseURI}thumbImg/ufid{self::*}"/>
  			<public:filename rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="." /></public:filename>
  			<public:mimeType rdf:datatype="http://www.w3.org/2001/XMLSchema#string">image/<xsl:value-of select="substring(current(),10)" /></public:mimeType>
		</rdf:Description>
	
		<rdf:Description rdf:about="{$baseURI}thumbImg/ufid{self::*}">
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
 			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
			<public:downloadLocation rdf:resource="{$baseURI}thumbDirDownload/ufid{self::*}"/>
			<public:filename rdf:datatype="http://www.w3.org/2001/XMLSchema#string">thumbnail.<xsl:value-of select="." /></public:filename>
			<public:mimeType rdf:datatype="http://www.w3.org/2001/XMLSchema#string">image/<xsl:value-of select="substring(current(),10)" /></public:mimeType>
		</rdf:Description>
			
		<rdf:Description rdf:about="{$baseURI}thumbDirDownload/ufid{self::*}">
  			<public:directDownloadUrl>/harvestedImages/thumbnails/thumbnail.<xsl:value-of select="." /></public:directDownloadUrl>
  			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
  			<vitro:modTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="datetime:dateTime()" /></vitro:modTime>
		</rdf:Description>
		
		<rdf:Description rdf:about="{$baseURI}fullDirDownload/ufid{self::*}">
			<public:directDownloadUrl>/harvestedImages/fullImages/<xsl:value-of select="." /></public:directDownloadUrl>
  			<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
 			<vitro:modTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="datetime:dateTime()" /></vitro:modTime>
		</rdf:Description>	
				
	</xsl:template>	
</xsl:stylesheet>
