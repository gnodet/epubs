<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<items> 
			<xsl:apply-templates />
		</items>
	</xsl:template>
	<xsl:template match="li">
		<xsl:element name="item">
			<xsl:attribute name="href">
				<xsl:value-of select="span/a/@href" />
			</xsl:attribute>
			<xsl:attribute name="text">
				<xsl:value-of select="span/a/text()" />
			</xsl:attribute>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	<xsl:template match="*">
		<xsl:apply-templates />
	</xsl:template>
</xsl:stylesheet>
