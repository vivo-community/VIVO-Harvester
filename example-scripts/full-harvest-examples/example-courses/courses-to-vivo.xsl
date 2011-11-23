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
		        <xsl:variable name="term"><xsl:value-of select="TERM" /></xsl:variable>
		        <xsl:variable name="courseName"><xsl:value-of select="CRS" /></xsl:variable>
		        <xsl:variable name="sectionNumber"><xsl:value-of select="SECT" /></xsl:variable>
		        <xsl:variable name="ufid"><xsl:value-of select="INS_UFID" /></xsl:variable>
		        <xsl:variable name="instructorName"><xsl:value-of select="INSTRUCTOR" /></xsl:variable>	
			<xsl:variable name="month"><xsl:value-of select="substring($term,5,5)" /></xsl:variable>
			<xsl:variable name="year"><xsl:value-of select="substring($term,1,4)" /></xsl:variable>		

			 <xsl:variable name="semester">
			<xsl:choose>
  				<xsl:when test="$month = 1">
					<xsl:text>Spring</xsl:text>	
 				</xsl:when>
  				<xsl:when test="$month = 5">
					<xsl:text>Summer</xsl:text>
  				</xsl:when>
				 <xsl:when test="$month = 8">
					<xsl:text>Fall</xsl:text>
                                </xsl:when>  	
			</xsl:choose>
			</xsl:variable>

		<!--Course Node-->
			<rdf:Description rdf:about="{$baseURI}courses/{$courseName}">
				<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
				<ufVivo:dateHarvested><xsl:value-of select="current-date()" /></ufVivo:dateHarvested>	
				
				<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course"/>
				<ufVivo:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course"/>
				<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
				<rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
				<rdfs:label xml:lang="en-US"> <xsl:value-of select="$courseName" /></rdfs:label>
				
				<!-- Relation to Course Section Node-->
				<ufVivo:courseForSection rdf:resource="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}"/>
				<!-- Relation to Course Section Node End-->
				
			</rdf:Description>
		<!--Course Node End-->
	
	<!-- UNIQUE-->
	<!-- Course Section -->
		<rdf:Description rdf:about="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested><xsl:value-of select="current-date()" /></ufVivo:dateHarvested>
				
			<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourseSection"/>
			<ufVivo:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourseSection"/>	
			<rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
			<rdfs:label xml:lang="en-US"> <xsl:value-of select="$courseName" /><xsl:text> </xsl:text><xsl:value-of select="$semester" /><xsl:text> </xsl:text><xsl:value-of select="$year" /> </rdfs:label>
			<ufVivo:sectionNum> <xsl:value-of select="$sectionNumber" /></ufVivo:sectionNum>
			
			<!-- Relation to Course Node -->
			<ufVivo:sectionForCourse rdf:resource="{$baseURI}courses/{$courseName}"/>
			<!-- Relation to Course Node End-->

			<!-- Relation to Academic Term -->
			<core:dateTimeInterval rdf:resource="{$baseURI}academicTerm/{$semester}-{$year}"/>
			<!-- Relation to Academic Term End -->

			<!-- Relation to Teacher Role Node -->
			<core:relatedRole rdf:resource="{$baseURI}teacherRole/{$courseName}-{$semester}-{$year}"/>	
			<!-- Relation to Teacher Role Node End-->
			
		</rdf:Description>
	<!--Course Section ENDS -->			

	<!-- NOT UNIQUE-->
	<!-- Academic Term Node-->
		<rdf:Description rdf:about="{$baseURI}academicTerm/{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested><xsl:value-of select="current-date()" /></ufVivo:dateHarvested>
			
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicTerm"/>
			
			<!-- Relation to Start Node -->
			<core:start rdf:resource="{$baseURI}academicTerm/start/{$semester}-{$year}"/>
			<!-- Relation to Start Node End-->
			
		</rdf:Description>
	<!-- Academic Term Node End-->

	<!-- NOT UNIQUE-->
	<!-- Start Node -->
	<rdf:Description rdf:about="{$baseURI}academicTerm/start/{$semester}-{$year}">
		<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
		<ufVivo:dateHarvested><xsl:value-of select="current-date()" /></ufVivo:dateHarvested>
		
		<core:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
		<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
		<core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision"/>
		<core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$semester" /><xsl:text> </xsl:text><xsl:value-of select="$year" /></core:dateTime>
	</rdf:Description>

	<rdf:Description rdf:about="http://vivoweb.org/ontology/core#yearMonthDayPrecision">
		<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
		<rdfs:label xml:lang="en-US">yearMonthDayPrecision</rdfs:label>
	</rdf:Description>
	<!-- Start Node End -->

	<!-- NOT UNIQUE-->
	<!--Teacher Role Node-->
		<rdf:Description rdf:about="{$baseURI}teacherRole/{$courseName}-{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested><xsl:value-of select="current-date()" /></ufVivo:dateHarvested>
			
			<core:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#TeacherRole"/>
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
			
			<!-- Relation To Person -->
			<core:teacherRoleOf rdf:resource="{$baseURI}person/{$ufid}"/>
			<!-- Relation To Person End-->
			
			<!-- Relation TO Course Section -->
			<core:roleIn rdf:resource="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}"/>
			<!-- Relation TO Course Section End-->
			
			</rdf:Description>
	<!--Teacher Role Node End-->

	<!-- NOT UNIQUE-->
	<!--Person Node-->
	<rdf:Description rdf:about="{$baseURI}person/{$ufid}">
		<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
		<ufVivo:dateHarvested><xsl:value-of select="current-date()" /></ufVivo:dateHarvested>
				
		<ufVivo:ufid> <xsl:value-of select="$ufid" /></ufVivo:ufid>
		<rdfs:label><xsl:value-of select="$instructorName" /></rdfs:label>
		
		<!-- Relation to Teacher Role -->
		<core:hasTeacherRole rdf:resource="{$baseURI}teacherRole/{$courseName}-{$semester}-{$year}"/>
		<!-- Relation to Teacher Role End-->
		</rdf:Description>
	<!--Person Node End-->
	
  </xsl:template>
</xsl:stylesheet>
