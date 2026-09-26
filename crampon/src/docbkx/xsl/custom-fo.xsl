<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		version="1.0">

	<xsl:import href="urn:docbkx:stylesheet"/>
	
	<xsl:variable name="toc.section.depth">1</xsl:variable>

	<xsl:param name="title.height" />

	<xsl:attribute-set name="section.title.level1.properties">
	  <xsl:attribute name="font-weight">normal</xsl:attribute>
	  <xsl:attribute name="font-style">italic</xsl:attribute>
	  <xsl:attribute name="color">blue</xsl:attribute>
	</xsl:attribute-set>
	
    <xsl:attribute-set name="component.title.properties">
      <xsl:attribute name="padding-top">100pt</xsl:attribute>
    </xsl:attribute-set>

	<xsl:attribute-set name="list.block.properties">
	  <xsl:attribute name="margin-left">
	    <xsl:choose>
	      <xsl:when test="count(ancestor::listitem)">inherit</xsl:when>
	      <xsl:otherwise>1cm</xsl:otherwise>
	    </xsl:choose>
	  </xsl:attribute>
	</xsl:attribute-set>

	<xsl:template name="book.titlepage.recto">
	  <fo:block>
	    <fo:table inline-progression-dimension="100%" table-layout="fixed">
	      <fo:table-column column-width="100%"/>
	      <fo:table-body>
	        <fo:table-row>
	          <fo:table-cell display-align="center" height="{$title.height}">
  	            <fo:block text-align="center" space-before.minimum="48pt">
  			        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="db:info/db:title"/>
  	            </fo:block>
	            <fo:block text-align="center" space-before.minimum="48pt">
			        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="db:info/db:subtitle"/>
	            </fo:block>
				<fo:block space-before.minimum="48pt">
				    <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="db:info/db:author"/>
	            </fo:block>
	          </fo:table-cell>
	        </fo:table-row >  
	      </fo:table-body> 
	    </fo:table>
	  </fo:block>
	</xsl:template>
	
	<xsl:template match="db:subtitle" mode="article.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format" >
			<xsl:apply-templates select="." mode="article.titlepage.recto.mode"/>
		</fo:block>
	</xsl:template>
	
	<xsl:template name="book.titlepage.verso"/>
	
	<xsl:template name="acknowledgements.titlepage.recto">
	</xsl:template>
	
	<xsl:template match="processing-instruction('linebreak')">
	  <fo:block/>
	</xsl:template>
	
	<xsl:template match="*[local-name() = 'br']">
  	  <fo:block space-before.minimum="12pt"/>
	</xsl:template>
	
	<xsl:template name="section.heading">
	</xsl:template>
	
	<xsl:template match="*[local-name()='div'][@class='verse'][@vs='1']">
		<fo:float float="start">
			<fo:block margin="0pt"
				margin-right="6pt"
			          color="#2554C7"
			          text-depth="0pt"
			          font-size="40pt"
			          line-height="40pt"
			          font-weight="bold"><xsl:value-of select="@ch"/></fo:block>
		</fo:float>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="*[local-name()='div'][@class='verse'][@vs>'1']">
		<fo:inline vertical-align="super" font-size="8pt" color="#2554C7"> 
		    <xsl:value-of select="concat(@vs, ' ')"/>
		</fo:inline>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="*[local-name()='div'][not(@class='verse')]">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="db:phrase[@*[local-name() = 'role' and .='smallcaps']]">
		<fo:inline font-family="CharisSC">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name() = 'role' and .='chapter']]">
		<fo:inline font-family="CharisSC">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="db:surname">
		<fo:inline font-family="CharisSC">
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
		<fo:block font-family="GalatiaSIL">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name()='lang' and (.='el')]]">
		<fo:inline font-family="GalatiaSIL">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>
	<xsl:template match="db:phrase[@*[local-name()='lang' and (.='he')]]">
		<fo:inline font-family="EzraSIL">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

</xsl:stylesheet>
