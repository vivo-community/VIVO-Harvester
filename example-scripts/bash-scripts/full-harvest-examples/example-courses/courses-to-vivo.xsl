<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:datetime="http://exslt.org/dates-and-times"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:public='http://vitro.mannlib.cornell.edu/ns/vitro/public#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
	xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
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
	
	<!-- Functions required to handle padding of 0's on UF IDs Start -->
	<xsl:function name="ufVivo:repeat-string" as="xs:string" >
	  <xsl:param name="stringToRepeat" as="xs:string?"/> 
	  <xsl:param name="count" as="xs:integer"/> 
	 
	  <xsl:sequence select="string-join((for $i in 1 to $count return $stringToRepeat),'')"/>
	   
	</xsl:function>

	<xsl:function name="ufVivo:pad-integer-to-length" as="xs:string">
	  <xsl:param name="integerToPad" as="xs:string"/> 
	  <xsl:param name="length" as="xs:integer"/> 
	  <xsl:sequence select="if ($length &lt; string-length(string($integerToPad)))
	  then 
	  	error(xs:QName('ufVivo:Integer_Longer_Than_Length'))
	  else 
	  	concat(ufVivo:repeat-string('0',$length - string-length(string($integerToPad))),string($integerToPad))"/>
	</xsl:function>
	<!-- Functions required to handle padding of 0's on UF IDs End -->


	<xsl:template match="rdf:Description">
		
		<xsl:variable name="term">
			<xsl:value-of select="db-COURSES_CSV:TERM" />
		</xsl:variable>
		
		<xsl:variable name="meet_no">
			<xsl:value-of select="db-COURSES_CSV:MEET_NO" />
		</xsl:variable>
		
		<xsl:variable name="courseName">
			<xsl:value-of select="db-COURSES_CSV:CRS" />
		</xsl:variable>
		
		<xsl:variable name="title">
			<xsl:call-template name="ToUpperLowerCase">
				<xsl:with-param name="inputString" select="db-COURSES_CSV:CRS_TITLE"/>
				<xsl:with-param name="index" select="0"/>
				<xsl:with-param name="loopCount" select="string-length(db-COURSES_CSV:CRS_TITLE)+1"/>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:variable name="sectionNumber">
			<xsl:value-of select="db-COURSES_CSV:SECT" />
		</xsl:variable>
		
		<xsl:variable name="unclean_ufid">
			<xsl:value-of select="db-COURSES_CSV:INS_UFID" />
		</xsl:variable>
		
		<xsl:variable name="ins_ufid">
			<xsl:value-of select="ufVivo:pad-integer-to-length($unclean_ufid,8)" />
		</xsl:variable>
		
		<xsl:variable name="instructorName">
			<xsl:value-of select="db-COURSES_CSV:INSTRUCTOR" />
		</xsl:variable>
		
		<xsl:variable name="month_tmp">
			<xsl:value-of select="substring($term,5,5)" />
		</xsl:variable>
		
		<xsl:variable name="year">
			<xsl:value-of select="substring($term,1,4)" />
		</xsl:variable>

		<xsl:variable name="month">		
			<xsl:choose>
				<xsl:when test="$month_tmp = 1">
					<xsl:text>01</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 2">
					<xsl:text>02</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 3">
					<xsl:text>03</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 4">
					<xsl:text>04</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 5">
					<xsl:text>05</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 6">
					<xsl:text>06</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 7">
					<xsl:text>07</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 8">
					<xsl:text>08</xsl:text>
				</xsl:when>
				<xsl:when test="$month_tmp = 9">
					<xsl:text>09</xsl:text>
				</xsl:when>	
				<xsl:otherwise>
   					<xsl:value-of select="$month_tmp" />
   				</xsl:otherwise>				
			</xsl:choose>																										
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

			<ufVivo:courseNum>
			        <xsl:value-of select="$courseName" />
			</ufVivo:courseNum>

			<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course" />
			<ufVivo:mostSpecificType
				rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/Course" />
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
			<!--  <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event" /> -->
			
			<rdfs:label xml:lang="en-US">
				<xsl:value-of select="$courseName"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="$title" />
			</rdfs:label>

			<!-- Relation to Course Section Node -->
			<ufVivo:courseForSection rdf:resource="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}-{$sectionNumber}" />
			<!-- Relation to Course Section Node End -->

			<!-- Relation to Webpage Node -->
			<core:webpage rdf:resource="{$baseURI}webPage/cdesc.php?crs={$courseName}" />
			<!-- Relation to Webpage Node End -->

			<!-- Relation to Course Teacher Role Node -->
			<core:relatedRole rdf:resource="{$baseURI}teacherRole/{$courseName}-{$ins_ufid}" />
			<!-- Relation to Course Teacher Role Node End -->

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
			<!-- Relation to Course Node -->
			<core:webpageOf rdf:resource="{$baseURI}courses/{$courseName}" />
			<!-- Relation to Course Node End -->

   		</rdf:Description>
		<!--  Course Web-page END-->
	
		<!-- UNIQUE -->
		<!-- Course Section -->
		<rdf:Description rdf:about="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}-{$sectionNumber}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>

			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>

			<rdf:type rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourseSection" />

			<ufVivo:mostSpecificType rdf:resource="http://vivo.ufl.edu/ontology/vivo-ufl/CourseSection" />

			<rdfs:label xml:lang="en-US">
				<xsl:value-of select="$courseName" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$semester" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$year" />
	        	<xsl:text> </xsl:text>
				<xsl:value-of select="$sectionNumber" />
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
			<core:relatedRole rdf:resource="{$baseURI}teacherRole/{$courseName}-{$semester}-{$year}-{$sectionNumber}-{$ins_ufid}" />
			<!-- Relation to Teacher Role Node End -->

		</rdf:Description>
		<!--Course Section ENDS -->
		<!-- NOT UNIQUE -->
		
		<!--Academic Term Node -->
		<rdf:Description rdf:about="{$baseURI}academicTerm/{$semester}-{$year}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>

			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicTerm" />

			<rdfs:label xml:lang="en-US">
				<xsl:value-of select="$semester" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$year" />
			</rdfs:label>
			
			<!-- Relation to Course Section Node -->
			<ufVivo:dateTimeIntervalFor rdf:resource="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}-{$sectionNumber}" />
       		<!-- Relation to Course Section Node End -->
			
		</rdf:Description>
		<!-- Academic Term Node End -->

		

		<!-- NOT UNIQUE -->
		<!--Teacher Role Node -->
		<rdf:Description
			rdf:about="{$baseURI}teacherRole/{$courseName}-{$semester}-{$year}-{$sectionNumber}-{$ins_ufid}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>

			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>

			<core:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#TeacherRole" />

			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole" />
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role" />

			<rdf:label xml:lang="en-US">
				<xsl:value-of select="$courseName" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$sectionNumber" />
	        	<xsl:text> </xsl:text>
				<xsl:value-of select="$semester" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$year" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="$ins_ufid" />
				<xsl:text>TR</xsl:text>
			</rdf:label>
						
			<!-- Relation To Person -->
			<core:teacherRoleOf rdf:resource="{$baseURI}person/{$ins_ufid}" />
			<!-- Relation To Person End -->

			<!-- Relation TO Course Section -->
			<core:roleIn rdf:resource="{$baseURI}courseSection/{$courseName}-{$semester}-{$year}-{$sectionNumber}" />
			<!-- Relation TO Course Section End -->

		</rdf:Description>
		<!--Teacher Role Node End -->
		<!-- NOT UNIQUE -->

    	<!-- NOT UNIQUE -->
		<!--COURSE Teacher Role Node -->
		<rdf:Description rdf:about="{$baseURI}teacherRole/{$courseName}-{$ins_ufid}">
	        <ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>

	        <ufVivo:dateHarvested>
	                <xsl:value-of select="current-date()" />
	        </ufVivo:dateHarvested>

	        <core:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#TeacherRole" />

	        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
	        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole" />
	        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role" />
	
	        <rdf:label xml:lang="en-US">
	                <xsl:value-of select="$courseName" />
	                <xsl:text> </xsl:text>
	                <xsl:value-of select="$ins_ufid" />
	                <xsl:text>TR</xsl:text>
	        </rdf:label>
		
			<!-- Relation To Person -->
			<ufVivo:courseRoleOf rdf:resource="{$baseURI}person/{$ins_ufid}" />
			<!-- Relation To Person End -->
	
			<!-- Relation TO Course Section -->
			<core:roleIn rdf:resource="{$baseURI}courses/{$courseName}" />
			<!-- Relation TO Course Section End -->
	

        </rdf:Description>
		<!--COURSE Teacher Role Node End -->
		<!-- NOT UNIQUE -->

		<!--Person Node -->
		<rdf:Description rdf:about="{$baseURI}person/{$ins_ufid}">
			<ufVivo:harvestedBy>Course-Harvester</ufVivo:harvestedBy>
			<ufVivo:dateHarvested>
				<xsl:value-of select="current-date()" />
			</ufVivo:dateHarvested>
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<ufVivo:ufid>
				<xsl:value-of select="$ins_ufid" />
			</ufVivo:ufid>
			<rdfs:label>
				<xsl:value-of select="$instructorName" />
			</rdfs:label>
			
			<!-- Relation to Course Role Node-->
			<ufVivo:hasCourseRole rdf:resource="{$baseURI}teacherRole/{$courseName}-{$ins_ufid}" />
			<!-- Relation to Teacher Role Node End -->
			
			<!-- Relation to Teacher Role Node-->
			<core:hasTeacherRole rdf:resource="{$baseURI}teacherRole/{$courseName}-{$semester}-{$year}-{$sectionNumber}-{$ins_ufid}" />
			<!-- Relation to Teacher Role Node End -->
	
		</rdf:Description>
		<!--Person Node End -->
	</xsl:template>

	<xsl:template name="ToUpperLowerCase">
                <xsl:param name="inputString"/>
                <xsl:param name="index"/>
                <xsl:param name="loopCount"/>
                <xsl:if test="$index &lt; $loopCount">
                        <xsl:choose>
                                <xsl:when test="substring($inputString, ($index)-1,1) = ' ' or $index = 1">
                                        <xsl:call-template name="ToUpper">
                                                <xsl:with-param name="inputString" select="substring($inputString,$index,1)"/>
                                        </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                        <xsl:call-template name="ToLower">
                                                <xsl:with-param name="inputString" select="substring($inputString,$index,1)"/>
                                        </xsl:call-template>
                                </xsl:otherwise>
                        </xsl:choose>
                        <xsl:call-template name="ToUpperLowerCase">
                                <xsl:with-param name="inputString" select="$inputString"/>
                                <xsl:with-param name="index" select="($index)+1"/>
                                <xsl:with-param name="loopCount" select="$loopCount"/>
                        </xsl:call-template>
                </xsl:if>
        </xsl:template>
 
        <xsl:template name="ToLower">
                <xsl:param name="inputString"/>
                <xsl:variable name="smallCase" select="'abcdefghijklmnopqrstuvwxyz'"/>
 
                <xsl:variable name="upperCase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
 
                <xsl:value-of select="translate($inputString,$upperCase,$smallCase)"/>
        </xsl:template>
 
        <xsl:template name="ToUpper">
                <xsl:param name="inputString"/>
                <xsl:variable name="smallCase" select="'abcdefghijklmnopqrstuvwxyz'"/>
 
                <xsl:variable name="upperCase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
 
                <xsl:value-of select="translate($inputString,$smallCase,$upperCase)"/>
 
        </xsl:template>
        
</xsl:stylesheet>
