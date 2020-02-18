<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns:gn="http://gnodet.fr/ns/docbook"
		exclude-result-prefixes='xsl xi fn'
		version="1.0">

	<xsl:param name="output"/>
	<xsl:param name="siteDirectory"/>

	<xsl:template match="gn:a">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="gn:author">
		<xsl:element name="author">
			<xsl:element name="personname">
				<xsl:value-of select="text()"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:np">
		<xsl:element name="phrase">
			<xsl:attribute name="role">
				<xsl:value-of select="'numpara'"/>
			</xsl:attribute>
			<xsl:value-of select="concat(text(), '.')"/>
		</xsl:element>
		<xsl:value-of select="' '"/>
	</xsl:template>
	<xsl:template match="gn:ns">
		<xsl:element name="phrase">
			<xsl:attribute name="role">
				<xsl:value-of select="'numsection'"/>
			</xsl:attribute>
			<xsl:value-of select="concat(text(), ' : ')"/>
		</xsl:element>
		<xsl:value-of select="' '"/>
	</xsl:template>
	<xsl:template match="gn:biblio">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="gn:p">
		<xsl:element name="para">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:sup">
		<xsl:element name="superscript">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:i">
		<xsl:element name="emphasis">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:b">
		<xsl:element name="emphasis">
			<xsl:attribute name="role">bold</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:br">
		<xsl:processing-instruction name="linebreak"/>
	</xsl:template>
	<xsl:template match="gn:edition">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ed</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:pages">
		<xsl:element name="phrase">
			<xsl:attribute name="role">opc</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:bible">
		<xsl:element name="emphasis">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:footnote//gn:author">
		<xsl:element name="phrase">
			<xsl:attribute name="role">author</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:ss">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ss</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:sc">
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
	<xsl:template match="text()[. = '.'][preceding-sibling::gn:sc]">
		<xsl:text>, </xsl:text>
	</xsl:template>
	<xsl:template match="gn:sv">
		<xsl:element name="phrase">
			<xsl:attribute name="role">ssv</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:pa">
		<xsl:element name="phrase">
			<xsl:attribute name="role">spa</xsl:attribute>
			<xsl:value-of select="'('"/><xsl:apply-templates/><xsl:value-of select="')'"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="gn:phrase[@*[local-name()='href' and not(starts-with(., '#'))]]">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="gn:phrase[@role='verse-number']">
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





<!--

	<xsl:template match="db:np">
		<xsl:element name="phrase">
			<xsl:attribute name="role">
				<xsl:value-of select="'numpara'"/>
			</xsl:attribute>
			<xsl:value-of select="concat(text(), '.')"/>
		</xsl:element>
		<xsl:value-of select="' '"/>
	</xsl:template>
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
	-->
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
	<!--
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
	-->

	<xsl:template match="gn:cover">
		<xsl:if test="$output = 'epub3'">
			<xsl:element name="mediaobject">
				<xsl:attribute name="role">cover</xsl:attribute>
				<xsl:attribute name="xml:id">coverd</xsl:attribute>
				<xsl:element name="imageobject">
					<xsl:attribute name="role">front-large</xsl:attribute>
					<xsl:attribute name="remap">lrg</xsl:attribute>
					<xsl:element name="imagedata">
						<xsl:attribute name="format">PNG</xsl:attribute>
						<xsl:attribute name="fileref">cover.png</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="$output = 'pdf'">
			<xsl:element name="mediaobject">
				<xsl:element name="imageobject">
					<xsl:element name="imagedata">
						<xsl:attribute name="format">SVG</xsl:attribute>
						<xsl:attribute name="fileref"><xsl:value-of select="concat($siteDirectory, 'svgs/', @name, '.svg')"/></xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<xsl:template match="gn:imagedata">
		<xsl:element name="imagedata">
			<xsl:if test="$output = 'epub3'">
				<xsl:copy-of select="@*[local-name()!='fileref']"/>
				<xsl:attribute name="fileref">coa-bw.svg</xsl:attribute>
			</xsl:if>
			<xsl:if test="$output = 'pdf'">
				<xsl:copy-of select="@*[local-name()!='fileref']"/>
				<xsl:attribute name="fileref"><xsl:value-of select="concat($siteDirectory, @fileref)"/></xsl:attribute>
			</xsl:if>
		</xsl:element>
	</xsl:template>

	<xsl:template match="gn:*">
		<xsl:element name="{local-name()}">
			<xsl:apply-templates select="@*|node()" />
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
