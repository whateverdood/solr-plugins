<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/> 
 
<xsl:strip-space elements="*"/> 
 
<!-- identity template --> 
<xsl:template match="/"> 
    <div> 
        <style type="text/css">
        body { font-size: small; }
        ul { 
            background-color: inherit; 
            color: Purple; 
            list-style: none; 
            margin-left: -2em; 
        } 
        li { 
            background-color: inherit; 
            display: inline; 
        } 
        span.tag { 
            color: Purple; 
        } 
        span.attr-name { 
            color: Red; 
        } 
        span.attr-value { 
            color: Blue; 
        } 
        span.cdata { 
            color: Black; 
        } 
        .hit { 
            background-color: LightGoldenRodYellow; 
        } 
        </style> 
        <xsl:apply-templates /> 
    </div> 
</xsl:template> 
 
<xsl:template match="*"> 
    <ul> 
        <li>
            <span class="tag">
                <xsl:text>&lt;</xsl:text><xsl:value-of select="local-name(.)"/><xsl:apply-templates select="@*[not(local-name(.)='class')]"/><xsl:text>&gt;</xsl:text>
            </span> 
            <xsl:apply-templates select="node()"/> 
            <span class="tag"> 
                <xsl:text>&lt;/</xsl:text><xsl:value-of select="local-name(.)"/><xsl:text>&gt;</xsl:text> 
            </span> 
        </li>
    </ul> 
</xsl:template> 
 
<xsl:template match="@*"> 
    <xsl:text> </xsl:text> 
    <span class="attr-name"><xsl:value-of select="local-name(.)"/></span> 
    <xsl:text>="</xsl:text><span class="attr-value"><xsl:value-of select="."/></span><xsl:text>"</xsl:text> 
</xsl:template> 
 
<xsl:template match="text()"> 
    <span class="cdata"> 
        <xsl:value-of select="."/> 
    </span> 
</xsl:template> 
 
</xsl:stylesheet>