<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns="http://docbook.org/ns/docbook"
		exclude-result-prefixes='xsl xi fn'
		version="1.0">

	<xsl:template match="*[local-name()='bible']">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="*[local-name()='ss']">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ss</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name()='sc']">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ssc</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name()='sv']">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ssv</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*[local-name()='biblio']">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="*[local-name()='edition']">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ed</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name()='pages']">
		<xsl:element name="phrase">
			<xsl:attribute name="role">opc</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template name="do-copy-verse">
		<xsl:param name="file"/>
		<xsl:param name="ids"/>
		<xsl:param name="lang"/>
		<xsl:choose>
			<xsl:when test="contains($ids, '_')">
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="substring-before($ids, '_')"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
				<xsl:value-of select="' '"/>
				<xsl:processing-instruction name="linebreak"/>
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="substring-after($ids, '_')"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="document($file)//*[local-name()='span' and @id=$ids]/text()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="copy-verse">
		<xsl:param name="file"/>
		<xsl:param name="ids"/>
		<xsl:param name="lang"/>
		<xsl:element name="para">
			<xsl:attribute name="role">
				<xsl:value-of select="'verse'"/>
			</xsl:attribute>
			<xsl:attribute name="lang" namespace="http://www.w3.org/XML/1998/namespace">
				<xsl:value-of select="$lang"/>
			</xsl:attribute>
			<xsl:element name="blockquote">
			<xsl:element name="emphasis">
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="$ids"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:element>
		</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*[local-name()='para' and starts-with(@xml:id, 'matthieu-')]">
		<xsl:variable name="ids" select="@xml:id"/>
		<xsl:element name="para"/>
			<xsl:call-template name="copy-verse">
				<xsl:with-param name="file" select="'../xml/matthieu-francais.xml'"/>
				<xsl:with-param name="ids" select="$ids"/>
				<xsl:with-param name="lang" select="'fr'"/>
			</xsl:call-template>
			<xsl:call-template name="copy-verse">
				<xsl:with-param name="file" select="'../xml/matthieu-latin.xml'"/>
				<xsl:with-param name="ids" select="$ids"/>
				<xsl:with-param name="lang" select="'la'"/>
			</xsl:call-template>
			<xsl:call-template name="copy-verse">
				<xsl:with-param name="file" select="'../xml/matthieu-grec.xml'"/>
				<xsl:with-param name="ids" select="$ids"/>
				<xsl:with-param name="lang" select="'el'"/>
			</xsl:call-template>
		<xsl:element name="para">
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*[local-name()='phrase' and @*[local-name()='href' and not(starts-with(., '#'))]]">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>

	<xsl:template match="xi:include[@href]">
	 <xsl:apply-templates select="document(@href)" />
	</xsl:template>

	<xsl:template match="xi:include" />

</xsl:stylesheet>
