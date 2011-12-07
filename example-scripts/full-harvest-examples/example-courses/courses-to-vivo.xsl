<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:datetime="http://exslt.org/dates-and-times"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:public='http://vitro.mannlib.cornell.edu/ns/vitro/public#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
	xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
	xmlns:db-COURSES_CSV="jdbc:h2:data/csv/store/fields/COURSES_CSV/">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI">http://vivo.ufl.edu/harvested/ufl/</xsl:variable>

	<xsl:template match="rdf:RDF">
		<rdf:RDF xmlns:public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
			xmlns:core="http://vivoweb.org/ontology/core#" xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
			xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
			xmlns:db-COURSES_CSV="jdbc:h2:data/csv/store/fields/COURSES_CSV/">
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		
		<xsl:variable name="term">
			<xsl:value-of select="db-COURSES_CSV:TERM" />
		</xsl:variable>
		
		<xsl:variable name="courseName">
			<xsl:value-of select="db-COURSES_CSV:CRS" />
		</xsl:variable>
		
		<xsl:variable name="sectionNumber">
			<xsl:value-of select="db-COURSES_CSV:SECT" />
		</xsl:variable>
		
		<xsl:variable name="ufid">
			<xsl:value-of select="db-COURSES_CSV:INS_UFID" />
		</xsl:variable>
		
		<xsl:variable name="instructorName">
			<xsl:value-of select="db-COURSES_CSV:INSTRUCTOR" />
		</xsl:variable>
		
		<xsl:variable name="month">
			<xsl:value-of select="substring($term,5,5)" />
		</xsl:variable>
		
		<xsl:variable name="year">
			<xsl:value-of select="substring($term,1,4)" />
		</xsl:variable>

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

		<!--Course Node -->
		<rdf:Description rdf:about="{$baseURI}courses/{$courseName}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>

			<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course" />
			<ufVivo:mostSpecificType
				rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course" />
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
			<!--  <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event" /> -->
			
			<rdfs:label xml:lang="en-US">
				<xsl:value-of select="$courseName" />
			</rdfs:label>

			<!-- Relation to Course Section Node -->
			<ufVivo:courseForSection
				rdf:resource="{$baseURI}courseSection/{$courseName}-{$sectionNumber}-{$semester}-{$year}" />
			<!-- Relation to Course Section Node End -->


			<!-- Relation to Webpage Node -->
			<core:webpage
				rdf:resource="{$baseURI}webPage/cdesc.php?crs={$courseName}" />
			<!-- Relation to Webpage Node End -->



		</rdf:Description>
		<!--Course Node End -->



		<!--  Course Web-page node-->
		<rdf:Description rdf:about="{$baseURI}webPage/cdesc.php?crs={$courseName}">
			<ufVivo:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#URLLink"/>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink"/>
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
			<core:webpageOf rdf:resource="{$baseURI}courses/{$courseName}"/>
			<core:rank rdf:datatype="http://www.w3.org/2001/XMLSchema#int">1</core:rank>
			<core:linkURI rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">
                        http://www.registrar.ufl.edu/cdesc.php?crs=<xsl:value-of select="$courseName" />

			</core:linkURI>
    		</rdf:Description>
	    <!--  Course Web-page END-->
	
	


		<!-- UNIQUE -->
		<!-- Course Section -->
		<rdf:Description
			rdf:about="{$baseURI}courseSection/{$courseName}-{$sectionNumber}-{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourseSection" />
			<ufVivo:mostSpecificType
				rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourseSection" />
			<!--  <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event" /> -->
			
			<rdfs:label xml:lang="en-US">
				<xsl:value-of select="$courseName" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$sectionNumber" />
	        	<xsl:text> </xsl:text>
				<xsl:value-of select="$semester" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$year" />
			</rdfs:label>
			
			<ufVivo:sectionNum>
				<xsl:value-of select="$sectionNumber" />
			</ufVivo:sectionNum>

			<!-- Relation to Course Node -->
			<ufVivo:sectionForCourse rdf:resource="{$baseURI}courses/{$courseName}" />
			<!-- Relation to Course Node End -->

			<!-- Relation to Academic Term -->
			<core:dateTimeInterval rdf:resource="{$baseURI}academicTerm/{$semester}-{$year}" />
			<!-- Relation to Academic Term End -->

			<!-- Relation to Teacher Role Node -->
			<core:relatedRole
				rdf:resource="{$baseURI}teacherRole/{$courseName}-{$sectionNumber}-{$semester}-{$year}" />
			<!-- Relation to Teacher Role Node End -->

		</rdf:Description>
		<!--Course Section ENDS -->

		<!-- NOT UNIQUE -->
		<!-- Academic Term Node -->
		<rdf:Description rdf:about="{$baseURI}academicTerm/{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicTerm" />

			<!-- Relation to Start Node -->
			<core:start rdf:resource="{$baseURI}academicTerm/start/{$semester}-{$year}" />
			<!-- Relation to Start Node End -->

		</rdf:Description>
		<!-- Academic Term Node End -->

		<!-- NOT UNIQUE -->
		<!-- Start Node -->
		<rdf:Description rdf:about="{$baseURI}academicTerm/start/{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			<core:mostSpecificType
				rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
			<core:dateTimePrecision
				rdf:resource="http://vivoweb.org/ontology/core#yearMonthDayPrecision" />
			
			<core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
				<xsl:value-of select="$semester" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$year" />
			</core:dateTime>
		</rdf:Description>

		<rdf:Description
			rdf:about="http://vivoweb.org/ontology/core#yearMonthDayPrecision">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<rdfs:label xml:lang="en-US">yearMonthDayPrecision</rdfs:label>
		</rdf:Description>
		<!-- Start Node End -->

		<!-- NOT UNIQUE -->
		<!--Teacher Role Node -->
		<rdf:Description
			rdf:about="{$baseURI}teacherRole/{$courseName}-{$sectionNumber}-{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			<core:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#TeacherRole" />
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role" />

			<!-- Relation To Person -->
			<core:teacherRoleOf rdf:resource="{$baseURI}person/{$ufid}" />
			<!-- Relation To Person End -->

			<!-- Relation TO Course Section -->
			<core:roleIn
				rdf:resource="{$baseURI}courseSection/{$courseName}-{$sectionNumber}-{$semester}-{$year}" />
			<!-- Relation TO Course Section End -->

		</rdf:Description>
		<!--Teacher Role Node End -->

		<!-- NOT UNIQUE -->
		<!--Person Node -->
		<rdf:Description rdf:about="{$baseURI}person/{$ufid}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />

			<ufVivo:ufid>
				<xsl:value-of select="$ufid" />
			</ufVivo:ufid>
			
			<!--<rdfs:label>
				<xsl:value-of select="$instructorName" />
			</rdfs:label>
			-->

			<!-- Relation to Teacher Role -->
			<core:hasTeacherRole
				rdf:resource="{$baseURI}teacherRole/{$courseName}-{$sectionNumber}-{$semester}-{$year}" />
			<!-- Relation to Teacher Role End -->
		</rdf:Description>
		<!--Person Node End -->
		
	</xsl:template>
</xsl:stylesheet>
