<!------------------------------------------------------------------------------
  Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the new BSD license
  which accompanies this distribution, and is available at
  http://www.opensource.org/licenses/bsd-license.html
  
  Contributors:
      Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
------------------------------------------------------------------------------->
<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (c) 2010, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:mf="http://vitro.mannlib.cornell.edu/XSL/functions"
    xmlns:xtor="http://ingest.mannlib.cornell.edu/generalizedXMLtoRDF/0.1/"
    xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    version="2.0">    
<xsl:output method="xml" indent="yes"/>    
    <!--
        XSLT to produce RDF/XML from XML.  This template does not handle
        narative content.
        
        This uses XSLT 2.0 and XPath 2.0 so use Saxon.
        Here is an example of how to do use XSLT 2.0 in ant:
        
        <xslt style="${ingest.dir}/xsl/activityInsight2n3Rdf.xsl" 
            in ="${ingest.dir}/ai_xml/document1.xml"
            out="${ingest.dir}/ai_xml/document1.n3"      
            <classpath location="${ingest.dir}/lib/saxon8.jar" /> 
        
        Here is an example of how to run saxon from the command line:
        java -Xmx512m -jar /home/bdc34/bin/saxon9he.jar \
        -s:filteredbjltest1.xml  -xsl:xsl/activityInsight2n3Rdf.xsl  -o:filteredbjltest2.rdf
        
        2009 Mann Library, Brian Caruso.
    -->
    
    <!-- This XSL will attempt to use URIs from namespaces that are found
        in the XML document.  If elements lack namespaces this string is used. 
        Important: If you use a prefix here it should exist in the NS declerations -->
    <xsl:variable name="defaultNS">xtor:</xsl:variable>
    
    <!-- This is a regular expression used to test then QNames of nodes.  Any node 
         with a QName that matches this regex will have position dataproperties on each of 
         their children nodes -->
    <xsl:param name="positionParentRegex" select="''"/>

    <xsl:template match="/">
      <rdf:RDF
         xmlns:xtor="http://ingest.mannlib.cornell.edu/generalizedXMLtoRDF/0.1/"
         xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#">
        <xsl:apply-templates mode="rdfDescription" select="*"/>
      </rdf:RDF>
    </xsl:template>
    
    <!-- create description elements -->
    <xsl:template mode="rdfDescription" match="*">        
      <rdf:Description>        
        <xsl:element name="rdf:type">
          <xsl:attribute name="rdf:resource" 
                         select="concat((if(namespace-uri())then namespace-uri() else $defaultNS),local-name())"/>
        </xsl:element>
        <xsl:apply-templates select="*|@*"/>
      </rdf:Description>
    </xsl:template>   
    
    <!-- recursive tempate to turn elements into bnodes -->
    <xsl:template match="*">
        <xsl:element name="{if(namespace-uri()) then namespace-uri() else $defaultNS}{local-name()}" >
            <rdf:Description>
            <xsl:apply-templates select="*|@*"/>
                
            <xsl:if test="not(*) and string-length(text())>0">
                    <vitro:value><xsl:copy-of select="text()"/></vitro:value>
            </xsl:if>
                                                            
            <xsl:if test="matches(../name(),$positionParentRegex) or 
                          matches(concat(../namespace-uri(),../local-name()),$positionParentRegex)">                            
                <xtor:position><xsl:value-of select="position()"/></xtor:position>
            </xsl:if>

            </rdf:Description>
        </xsl:element>
    </xsl:template>
    
    <!-- Match all leaf elements that have attributes and turn them into bnodes -->
    <xsl:template  match="*[not(*) and @* and string-length(text())>0]">        
        <xsl:element name="{if(namespace-uri()) then namespace-uri() else $defaultNS}{name()}">
            <rdf:Description>
                <xsl:apply-templates  select="@*"/>
                <vitro:value>
                    <xsl:value-of select="."/>
                </vitro:value>
            </rdf:Description>
        </xsl:element>
    </xsl:template>
    
    <!-- Match all leaf elements and attributes and turn them into data properties. -->
    <xsl:template match="@*|*[not(*) and not(@*) and string-length(text())>0]">        
        <xsl:element name="{if(namespace-uri()) then namespace-uri() else $defaultNS}{name()}">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
   
    
</xsl:stylesheet>
