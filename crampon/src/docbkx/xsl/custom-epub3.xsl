<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns:d="http://docbook.org/ns/docbook"
		xmlns="http://www.w3.org/1999/xhtml"
		exclude-result-prefixes='db d'
		version="1.0">

	<xsl:import href="urn:docbkx:stylesheet"/>

	<xsl:template name="book.titlepage.verso"/>
	
	<xsl:template name="acknowledgements.titlepage.recto">
	</xsl:template>

	<xsl:template match="processing-instruction('linebreak')">
		<br/>
	</xsl:template>
	<xsl:template match="d:br">
		<br/>
	</xsl:template>
	<xsl:template match="para[@role='toc']">
		<xsl:element name="div">
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="d:book|
	d:article|
	d:part|
	d:reference|
	d:preface|
	d:chapter|
	d:bibliography|
	d:appendix|
	d:glossary|
	d:section|
	d:sect1|
	d:sect2|
	d:sect3|
	d:sect4|
	d:sect5|
	d:refentry|
	d:colophon|
	d:bibliodiv[d:title]|
	d:setindex|
	d:index"
	mode="ncx">
	<xsl:variable name="depth" select="count(ancestor::*)"/>
	<xsl:variable name="title">
	<xsl:if test="$epub.autolabel != 0">
	<xsl:variable name="label.markup">
	<xsl:apply-templates select="." mode="label.markup" />
	</xsl:variable>
	<xsl:if test="normalize-space($label.markup)">
	<xsl:value-of
	select="concat($label.markup,$autotoc.label.separator)" />
	</xsl:if>
	</xsl:if>
	<xsl:apply-templates select="." mode="title.markup" />
	</xsl:variable>
	<xsl:if test="$depth &lt;= $toc.max.depth">
	<xsl:variable name="href">
	<xsl:call-template name="href.target.with.base.dir">
	<xsl:with-param name="context" select="/" />
	<!-- Generate links relative to the location of root file/toc.xml file -->
	</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="id">
	<xsl:value-of select="generate-id(.)"/>
	</xsl:variable>
	<xsl:variable name="order">
	<xsl:value-of select="$depth +
	count(preceding::d:part[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:reference[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:book[parent::d:set][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:preface[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:chapter[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:bibliography[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:appendix[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:article[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:glossary[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:section[not(parent::d:partintro)][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:sect1[not(parent::d:partintro)][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:sect2[not(ancestor::d:partintro)][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:sect3[not(ancestor::d:partintro)][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:sect4[not(ancestor::d:partintro)][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:sect5[not(ancestor::d:partintro)][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:refentry[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:colophon[count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:bibliodiv[d:title][count(ancestor::*) &lt;= $toc.max.depth]|
	preceding::d:index[count(ancestor::*) &lt;= $toc.max.depth])"/>
	</xsl:variable>
	<xsl:element name="navPoint" namespace="http://www.daisy.org/z3986/2005/ncx/">
	<xsl:attribute name="id">
	<xsl:value-of select="$id"/>
	</xsl:attribute>
	<xsl:attribute name="playOrder">
	<xsl:choose>
	<xsl:when test="/*[self::d:set]">
	<xsl:value-of select="$order"/>
	</xsl:when>
	<xsl:when test="$root.is.a.chunk != '0'">
	<xsl:value-of select="$order + 1"/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select="$order - 0"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:attribute>
	<xsl:element name="navLabel" namespace="http://www.daisy.org/z3986/2005/ncx/">
	<xsl:element name="text" namespace="http://www.daisy.org/z3986/2005/ncx/"><xsl:value-of select="normalize-space($title)"/> </xsl:element>
	</xsl:element>
	<xsl:element name="content" namespace="http://www.daisy.org/z3986/2005/ncx/">
	<xsl:attribute name="src">
	<xsl:value-of select="$href"/>
	</xsl:attribute>
	</xsl:element>
	<xsl:apply-templates select="d:book[parent::d:set]|d:part|d:reference|d:preface|d:chapter|d:bibliography|d:appendix|d:article|d:glossary|d:section|d:sect1|d:sect2|d:sect3|d:sect4|d:sect5|d:refentry|d:colophon|d:bibliodiv[d:title]|d:setindex|d:index" mode="ncx"/>
	</xsl:element>
	</xsl:if>
	</xsl:template>	

</xsl:stylesheet>
