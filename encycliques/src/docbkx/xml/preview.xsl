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
						p { text-align: justify; margin: 2em; text-indent: 2em; }
					</style>
				</head>
				<body>
					<xsl:apply-templates select="node()"/>
				</body>
			</html>
		</xsl:template>

		<xsl:template match="db:p">
			<p><xsl:apply-templates select="@*|node()"/></p>
		</xsl:template>
		<xsl:template match="db:i">
			<i><xsl:apply-templates select="@*|node()"/></i>
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

	<xsl:template match="db:subtitle" />

	<xsl:template match="db:footnote" />
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
			<h3><xsl:value-of select="db:title/text()"/></h3>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:template>
		
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
		
</xsl:stylesheet>