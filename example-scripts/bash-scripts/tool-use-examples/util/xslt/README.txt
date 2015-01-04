
This example shows how an external function, implemented in a java class, can be called as an xslt extension.

Note in the people.datamap.xsl file where the "t_People" template defines AuthorTool class using "java:" as
a prefix when specifying the namespace and then provides an extension-element-prefix called "authortools". 

<xsl:template name="t_People"
   xmlns:authortools = "java:org.vivoweb.harvester.util.xslt.AuthorTools"
   extension-element-prefixes = "authortools" >

Then, within that template a "normalizeAuthorName" method may be called to obtain the results (as a string) 
from that method, for example:

<xsl:value-of select="authortools:normalizeAuthorName($lastName, $firstName, $middleName)" />


