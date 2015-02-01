<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		version="1.0">

	<xsl:import href="urn:docbkx:stylesheet"/>

	<!--xsl:template name="header.content">
	</xsl:template-->

	<xsl:template match="processing-instruction('linebreak')">
	  <fo:block/>
	</xsl:template>
	
	<xsl:template match="db:phrase[@*[local-name() = 'role' and .='smallcaps']]">
		<fo:inline font-variant="small-caps">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name() = 'role' and .='chapter']]">
		<fo:inline font-variant="small-caps">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name() = 'role' and .='liturgy']]">
		<fo:inline font-family="liturgy">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	
	<xsl:template match="db:surname">
		<fo:inline font-variant="small-caps">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	
	<xsl:template match="db:phrase[@*[local-name() = 'href']]">
		<xsl:apply-templates />
	</xsl:template>
	<xsl:template match="db:citetitle[@*[local-name() = 'href']]">
		<fo:inline font-style="italic">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="db:para[@*[local-name()='role' and (.='verse')] and @*[local-name()='lang' and (.='el')]]">
		<fo:block font-family="Galatia">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name()='lang' and (.='el')]]">
		<fo:inline font-family="Galatia">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name()='lang' and (.='he')]]">
		<fo:inline font-family="Ezra">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

</xsl:stylesheet>
