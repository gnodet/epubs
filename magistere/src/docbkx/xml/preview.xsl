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
					<style type="text/css">
						h1 { text-align: center; }
						h2 { text-align: center; width: 50%; margin-left: 25%; }
						h3 { margin: 2em;  }
						h4 { margin: 2em;  }
						h5 { display: block; text-align: center; font-style: italic; width: 100%; font-size:120%; font-weight: bold; }
						h7 { display: block; text-align: center; font-style: italic; width: 100%; }
						p { text-align: justify; margin: 2em; text-indent: 2em; }
						.blockquote p { margin: 1em 4em; text-indent: 0; }
						.ss { color:#0000cd; font-style: italic; }
						.sc { color:#6495ed; font-variant:small-caps; font-weight:bolder; }
						.sv { color:#00bfff; font-size:80%; font-weight:bolder; }
						.pa { font-size:80%; }
						.pa::before { content: '('; }
						.pa::after { content: ')'; }
						.numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }
						#notes p { margin: 0 2em; text-indent: 0; }
						sup { position: relative; top: -0.25em; font-size: 60%; }
						.sign { text-align: center; font-style: italic; font-size: bigger; font-weight: bolder; }
					</style>
				</head>
				<body>
					<xsl:apply-templates select="node()"/>
					<xsl:if test="count(//db:footnote) > 0">
						<hr/>
						<div id="notes">
							<xsl:apply-templates select="//db:footnote" mode="footnote"/>
						</div>
					</xsl:if>
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

		<xsl:template match="db:blockquote">
			<xsl:element name="div">
				<xsl:attribute name="class">
					<xsl:value-of select="'blockquote'"/>
				</xsl:attribute>
				<xsl:apply-templates select="@*|node()"/>
			</xsl:element>
		</xsl:template>
		<xsl:template match="db:p|db:para">
			<xsl:element name="p">
				<xsl:apply-templates select="@*|node()"/>
			</xsl:element>
		</xsl:template>
		<xsl:template match="db:br">
			<br/>
		</xsl:template>
		<xsl:template match="db:ns">
			<xsl:element name="a">
				<xsl:attribute name="class">
					<xsl:value-of select="'numsect'"/>
				</xsl:attribute>
				<xsl:attribute name="id">
					<xsl:value-of select="concat('p', text())"/>
				</xsl:attribute>
				<xsl:value-of select="concat(text(), '.')"/>
			</xsl:element>
			<xsl:value-of select="' '"/>
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
			<xsl:element name="i"><xsl:apply-templates select="@*|node()"/></xsl:element>
		</xsl:template>
		<xsl:template match="db:b">
			<xsl:element name="b"><xsl:apply-templates select="@*|node()"/></xsl:element>
		</xsl:template>
		<xsl:template match="db:sup|db:superscript">
			<xsl:element name="sup"><xsl:apply-templates select="@*|node()"/></xsl:element>
		</xsl:template>
		<xsl:template match="db:ul">
			<xsl:element name="ul"><xsl:apply-templates select="@*|node()"/></xsl:element>
		</xsl:template>
		<xsl:template match="db:li">
			<xsl:element name="li"><xsl:apply-templates select="@*|node()"/></xsl:element>
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
	
	<xsl:template match="@role">
		<xsl:attribute name="class">
			<xsl:value-of select="@role"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="processing-instruction('linebreak')">
		<br/>
	</xsl:template>

	<xsl:template match="db:title" mode="chapter">
		<xsl:element name="h1"><xsl:apply-templates select="@*|node()"/></xsl:element>
	</xsl:template>
	<xsl:template match="db:subtitle" mode="chapter">
		<xsl:element name="h2"><xsl:apply-templates select="@*|node()"/></xsl:element>
	</xsl:template>

	<xsl:template match="db:title" mode="section">
		<xsl:variable name="name" select="concat('h', count(./ancestor::db:section) * 2 + 1)"/>
		<xsl:element name="{$name}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="db:subtitle" mode="section">
		<xsl:variable name="name" select="concat('h', count(./ancestor::db:section) * 2 + 2)"/>
		<xsl:element name="{$name}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="db:bible|db:ss|db:sc|db:sv|db:pa">
		<xsl:element name="span">
			<xsl:attribute name="class">
				<xsl:value-of select="local-name()"/>
			</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

		<xsl:template match="db:info" />
		<!--<xsl:template match="db:title" />-->
		<!--<xsl:template match="db:subtitle" />-->
		<xsl:template match="db:mediaobject" />

	<xsl:template match="db:title">
		<xsl:element name="h1"><xsl:apply-templates select="@*|node()"/></xsl:element>
	</xsl:template>
	<xsl:template match="db:subtitle">
		<xsl:element name="h2"><xsl:apply-templates select="@*|node()"/></xsl:element>
	</xsl:template>
		<xsl:template match="db:chapter|db:section">
			<xsl:element name="section">
				<xsl:apply-templates select="@*|node()"/>
			</xsl:element>
			<!--<xsl:apply-templates select="db:title" mode="chapter"/>-->
			<!--<xsl:apply-templates select="db:subtitle" mode="chapter"/>-->
		</xsl:template>
		
		<!--<xsl:template match="db:section">-->
			<!--<xsl:apply-templates select="db:title" mode="section"/>-->
			<!--<xsl:apply-templates select="db:subtitle" mode="section"/>-->
			<!--<xsl:apply-templates select="@*|node()"/>-->
		<!--</xsl:template>-->

	<xsl:template match="processing-instruction('xml-stylesheet')">
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
		
</xsl:stylesheet>