<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		version="1.0">

	<xsl:import href="urn:docbkx:stylesheet"/>
	
	<xsl:param name="title.height" />

    <xsl:attribute-set name="component.title.properties">
      <xsl:attribute name="padding-top">100pt</xsl:attribute>
    </xsl:attribute-set>

	<xsl:template name="book.titlepage.recto">
	  <fo:block>
	    <fo:table inline-progression-dimension="100%" table-layout="fixed">
	      <fo:table-column column-width="100%"/>
	      <fo:table-body>
	        <fo:table-row>
	          <fo:table-cell display-align="center" height="{$title.height}">
  	            <fo:block text-align="center">
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
