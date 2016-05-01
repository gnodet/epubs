<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns="http://docbook.org/ns/docbook"
		xmlns:db="http://docbook.org/ns/docbook"
		exclude-result-prefixes='xsl xi fn'
		version="1.0">
	<xsl:template match="db:biblio">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="db:p">
		<xsl:element name="para">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:sup">
		<xsl:element name="superscript">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:i">
		<xsl:element name="emphasis">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:b">
		<xsl:element name="emphasis">
			<xsl:attribute name="role">bold</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:br">
		<xsl:processing-instruction name="linebreak"/>
	</xsl:template>
	<xsl:template match="db:edition">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ed</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:pages">
		<xsl:element name="phrase">
			<xsl:attribute name="role">opc</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:bible">
		<xsl:element name="emphasis">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:footnote//db:author">
		<xsl:element name="phrase">
			<xsl:attribute name="role">author</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:ss">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ss</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:sc">
		<xsl:variable name="num">
			<xsl:number value="text()" format="i"/>
		</xsl:variable>
		<xsl:element name="phrase">
			<xsl:attribute name="role">ssc</xsl:attribute>
			<xsl:choose>
				<xsl:when test="$num = 'NaN'">
					<xsl:apply-templates/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$num"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	<xsl:template match="text()[. = '.'][preceding-sibling::db:sc]">
		<xsl:text>, </xsl:text>
	</xsl:template>
	<xsl:template match="db:sv">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ssv</xsl:attribute>
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
			<xsl:element name="para">
			<xsl:element name="phrase">
				<xsl:attribute name="role">
					<xsl:value-of select="'verse'"/>
				</xsl:attribute>
				<xsl:call-template name="do-copy-verse">
					<xsl:with-param name="file" select="$file"/>
					<xsl:with-param name="ids" select="$ids"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:element>
			</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name()='href' and not(starts-with(., '#'))]]">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="db:phrase[@role='verse-number']">
		<xsl:variable name="ch">
			<xsl:value-of select="substring-before(text(), '.')"/>
		</xsl:variable>
		<xsl:variable name="vr">
			<xsl:value-of select="substring-before(substring(text(), string-length($ch) + 2), '.')"/>
		</xsl:variable>
		<xsl:element name="phrase">
			<xsl:attribute name="role">verse-number</xsl:attribute>
			<xsl:element name="phrase">
				<xsl:attribute name="role">ch</xsl:attribute>
				<xsl:copy-of select="$ch"/>
			</xsl:element>
			<xsl:value-of select="'.'"/>
			<xsl:element name="phrase">
				<xsl:attribute name="role">vr</xsl:attribute>
				<xsl:copy-of select="$vr"/>
			</xsl:element>
			<xsl:value-of select="'.'"/>
		</xsl:element>
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
