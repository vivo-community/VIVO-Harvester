<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
                version="1.0">
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes"/>

    <!-- Root template -->
    <xsl:template match="/">
        <dim:dim xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.dspace.org/xmlns/dspace/dim http://www.dspace.org/schema/dim.xsd">
            <xsl:apply-templates select="//dim:field"/>
        </dim:dim>
    </xsl:template>

    <!-- Process each dim:field -->
    <xsl:template match="dim:field">
        <dim:field>
            <!-- Metadata Schema -->
            <xsl:attribute name="mdschema">
                <xsl:value-of select="@mdschema"/>
            </xsl:attribute>

            <!-- Element -->
            <xsl:attribute name="element">
                <xsl:value-of select="@element"/>
            </xsl:attribute>

            <!-- Qualifier (if available) -->
            <xsl:if test="@qualifier">
                <xsl:attribute name="qualifier">
                    <xsl:value-of select="@qualifier"/>
                </xsl:attribute>
            </xsl:if>

            <!-- Language (if available) -->
            <xsl:if test="@lang">
                <xsl:attribute name="lang">
                    <xsl:value-of select="@lang"/>
                </xsl:attribute>
            </xsl:if>

            <!-- Authority (if available) -->
            <xsl:if test="following-sibling::dim:field[@name='authority'][1]">
                <xsl:attribute name="authority">
                    <xsl:value-of select="following-sibling::dim:field[@name='authority'][1]"/>
                </xsl:attribute>
            </xsl:if>

            <!-- Confidence (if available) -->
            <xsl:if test="following-sibling::dim:field[@name='confidence'][1]">
                <xsl:attribute name="confidence">
                    <xsl:value-of select="following-sibling::dim:field[@name='confidence'][1]"/>
                </xsl:attribute>
            </xsl:if>

            <!-- Value -->
            <xsl:value-of select="."/>
        </dim:field>
    </xsl:template>

</xsl:stylesheet>
