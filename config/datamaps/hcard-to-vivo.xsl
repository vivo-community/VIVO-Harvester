<!------------------------------------------------------------------------------
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
------------------------------------------------------------------------------->
<?xml version="1.0"?>
<!-- 
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams
  
  This file is part of VIVO.
  
  VIVO is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  VIVO is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with VIVO.  If not, see <http://www.gnu.org/licenses/gpl-3.0.txt>.
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial implementation
      
  hCard to VIVO Translation File
  This file will denote the elements in an hCard file and thier translation into
  the VIVO ontology's schema.  hCard is a microformat found at http://microformats.org/wiki/hcard
  microformats use xhtml to store thier data and are thus transformable using
  xsl.  NOTE hCard has a 1:1 relationship with elements from vCard
  
  TODO:  Flesh out this translation
 -->
<!-- Header information for the Style Sheet
	The style sheet requires xmlns, xml namespace, for each prefix you use in constructing
	the new elements
-->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:score='http://vivoweb.org/ontology/score#'
	xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>

	<!-- This will create indenting in xml readers -->
	<xsl:output method="xml" indent="yes"/>  
	
</xsl:stylesheet>
