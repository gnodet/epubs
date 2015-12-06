<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns="http://www.w3.org/1999/xhtml"
		version="1.0">

	<xsl:import href="urn:docbkx:stylesheet"/>

	<xsl:template name="book.titlepage.verso"/>
	
	<xsl:template name="acknowledgements.titlepage.recto">
	</xsl:template>

	<xsl:template match="*[local-name()='bible']">
		<xsl:element name="i">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template name="do-copy-verse">
		<xsl:param name="file"/>
		<xsl:param name="ids"/>
		<xsl:choose>
			<xsl:when test="contains($ids, '_')">
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="substring-before($ids, '_')"/>
				</xsl:call-template>
				<xsl:element name="br"/>
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="substring-after($ids, '_')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="document($file)//*[local-name()='span' and @id=$ids]"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="copy-verse">
		<xsl:param name="file"/>
		<xsl:param name="ids"/>
		<xsl:param name="lang"/>
		<xsl:element name="p">
			<xsl:attribute name="lang" namespace="http://www.w3.org/XML/1998/namespace">
				<xsl:value-of select="$lang"/>
			</xsl:attribute>
			<xsl:element name="i">
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="$ids"/>
				</xsl:call-template>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*[local-name()='para' and starts-with(@xml:id, 'marc-')]">
		<xsl:variable name="ids" select="@xml:id"/>
		<xsl:element name="blockquote">
			<xsl:call-template name="copy-verse">
				<xsl:with-param name="file" select="'../xml/marc-francais.xml'"/>
				<xsl:with-param name="ids" select="$ids"/>
				<xsl:with-param name="lang" select="'fr'"/>
			</xsl:call-template>
			<xsl:call-template name="copy-verse">
				<xsl:with-param name="file" select="'../xml/marc-latin.xml'"/>
				<xsl:with-param name="ids" select="$ids"/>
				<xsl:with-param name="lang" select="'la'"/>
			</xsl:call-template>
			<xsl:call-template name="copy-verse">
				<xsl:with-param name="file" select="'../xml/marc-grec.xml'"/>
				<xsl:with-param name="ids" select="$ids"/>
				<xsl:with-param name="lang" select="'el'"/>
			</xsl:call-template>
		</xsl:element>
		<xsl:apply-templates />
	</xsl:template>

</xsl:stylesheet>
