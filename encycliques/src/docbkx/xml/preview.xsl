<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:db="http://docbook.org/ns/docbook"
		exclude-result-prefixes='xsl xi db'
		version="2.0">

		<xsl:template match="db:book">
			<html>
				<head>
					<title><xsl:value-of select="db:info/db:title/text()"/></title>
					<style>
						h1 { text-align: center; }
						h2 { text-align: center; width: 50%; margin-left: 25%; }
						h3 { margin: 2em;  }
						h4 { margin: 2em;  }
						p { text-align: justify; margin: 2em; text-indent: 2em; }
						.ss { color:#0000cd; font-style: italic; }
						.sc { color:#6495ed; font-variant:small-caps; font-weight:bolder; }
						.sv { color:#00bfff; font-size:80%; font-weight:bolder; }
						.numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }
						#notes p { margin: 0 2em; text-indent: 0; }
						sup { position: relative; top: -0.25em; font-size: 60%; }
						.sign { text-align: center; font-style: italic; font-size: bigger; font-weight: bolder; }
					</style>
				</head>
				<body>
					<xsl:apply-templates select="node()"/>
					<hr/>
					<div id="notes">
						<xsl:apply-templates select="//db:footnote" mode="footnote"/>
					</div>
				</body>
			</html>
		</xsl:template>
		<xsl:template match="db:footnote">
			<xsl:variable name="id" select="count(preceding::db:footnote) + 1"/>
			<xsl:element name="sup">
				<xsl:value-of select="'['"/>
				<xsl:element name="a">
					<xsl:attribute name="id">
						<xsl:value-of select="concat('fnr', $id)"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="concat('#fn', $id)"/>
					</xsl:attribute>
					<xsl:value-of select="$id"/>
				</xsl:element>
				<xsl:value-of select="']'"/>
			</xsl:element>
		</xsl:template>
		<xsl:template match="db:footnote" mode="footnote">
			<xsl:variable name="id" select="count(preceding::db:footnote) + 1"/>
			<xsl:element name="p">
				<xsl:element name="a">
					<xsl:attribute name="id">
						<xsl:value-of select="concat('fn', $id)"/>
					</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="concat('#fnr', $id)"/>
					</xsl:attribute>
					<xsl:value-of select="concat('[', $id, ']')"/>
				</xsl:element>
				<xsl:value-of select="' '"/>
				<xsl:apply-templates select="db:para/node()|db:p/node()"/>
			</xsl:element>
		</xsl:template>

		<xsl:template match="db:p|db:para">
			<p><xsl:apply-templates select="@*|node()"/></p>
		</xsl:template>
		<xsl:template match="db:br">
			<br/>
		</xsl:template>
		<xsl:template match="db:np">
			<xsl:element name="a">
				<xsl:attribute name="class">
					<xsl:value-of select="'numpara'"/>
				</xsl:attribute>
				<xsl:attribute name="id">
					<xsl:value-of select="concat('p', text())"/>
				</xsl:attribute>
				<xsl:value-of select="concat(text(), '.')"/>
			</xsl:element>
			<xsl:value-of select="' '"/>
		</xsl:template>
		<xsl:template match="db:i|db:emphasis|db:em">
			<i><xsl:apply-templates select="@*|node()"/></i>
		</xsl:template>
		<xsl:template match="db:b">
			<b><xsl:apply-templates select="@*|node()"/></b>
		</xsl:template>
		<xsl:template match="db:sup|db:superscript">
			<sup><xsl:apply-templates select="@*|node()"/></sup>
		</xsl:template>
	<xsl:template match="db:phrase">
		<xsl:element name="span">
			<xsl:if test="@role">
				<xsl:attribute name="class">
					<xsl:value-of select="@role"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="processing-instruction('linebreak')">
		<br/>
	</xsl:template>

	<xsl:template match="db:title" mode="chapter">
		<h1><xsl:apply-templates select="@*|node()"/></h1>
	</xsl:template>
	<xsl:template match="db:subtitle" mode="chapter">
		<h2><xsl:apply-templates select="@*|node()"/></h2>
	</xsl:template>

	<xsl:template match="db:title" mode="section">
		<h3><xsl:apply-templates select="@*|node()"/></h3>
	</xsl:template>
	<xsl:template match="db:subtitle" mode="section">
		<h4><xsl:apply-templates select="@*|node()"/></h4>
	</xsl:template>

	<xsl:template match="db:ss">
		<span class="ss"><xsl:apply-templates select="@*|node()"/></span>
	</xsl:template>
	<xsl:template match="db:sc">
		<span class="sc"><xsl:apply-templates select="@*|node()"/></span>
	</xsl:template>
	<xsl:template match="db:sv">
		<span class="sv"><xsl:apply-templates select="@*|node()"/></span>
	</xsl:template>

		<xsl:template match="db:info" />
		<xsl:template match="db:title" />
		<xsl:template match="db:subtitle" />
		<xsl:template match="db:mediaobject" />
		
		<xsl:template match="db:chapter">
			<xsl:apply-templates select="db:title" mode="chapter"/>
			<xsl:apply-templates select="db:subtitle" mode="chapter"/>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:template>
		
		<xsl:template match="db:section">
			<xsl:apply-templates select="db:title" mode="section"/>
			<xsl:apply-templates select="db:subtitle" mode="section"/>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:template>
		
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
		
</xsl:stylesheet>