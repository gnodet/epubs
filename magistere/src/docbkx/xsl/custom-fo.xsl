<?xml version="1.0"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:db="http://docbook.org/ns/docbook"
    version="1.0">
  <xsl:import href="urn:docbkx:stylesheet"/>

  <xsl:param name="title.height">32</xsl:param>
  <!--	<xsl:param name="body.font.master">16</xsl:param>-->
  <xsl:param name="page.height">21cm</xsl:param>
  <xsl:param name="page.width">14.85cm</xsl:param>

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

  <xsl:template name="user.pagemasters">
    <fo:simple-page-master master-name="cover-first" page-width="{$page.width}" page-height="{$page.height}"
                           margin-top="0" margin-bottom="0" margin-left="0" margin-right="0">
      <fo:region-body margin-bottom="0" margin-top="0" margin-left="0" margin-right="0" column-gap="0"
                      column-count="1"/>
      <fo:region-before region-name="xsl-region-before-first"
                        extent="0" precedence="false" display-align="before"/>
      <fo:region-after region-name="xsl-region-after-first"
                       extent="0" precedence="false" display-align="after"/>
      <xsl:call-template name="region.inner">
        <xsl:with-param name="sequence">first</xsl:with-param>
        <xsl:with-param name="pageclass">cover</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="region.outer">
        <xsl:with-param name="sequence">first</xsl:with-param>
        <xsl:with-param name="pageclass">cover</xsl:with-param>
      </xsl:call-template>
    </fo:simple-page-master>
    <fo:simple-page-master master-name="cover-odd" page-width="{$page.width}" page-height="{$page.height}"
                           margin-top="0" margin-bottom="0" margin-left="0" margin-right="0">
      <fo:region-body margin-bottom="0" margin-top="0" margin-left="0" margin-right="0" column-gap="0"
                      column-count="1"/>
      <fo:region-before region-name="xsl-region-before-first"
                        extent="0" precedence="false" display-align="before"/>
      <fo:region-after region-name="xsl-region-after-first"
                       extent="0" precedence="false" display-align="after"/>
      <xsl:call-template name="region.inner">
        <xsl:with-param name="sequence">odd</xsl:with-param>
        <xsl:with-param name="pageclass">cover</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="region.outer">
        <xsl:with-param name="sequence">odd</xsl:with-param>
        <xsl:with-param name="pageclass">cover</xsl:with-param>
      </xsl:call-template>
    </fo:simple-page-master>
    <fo:simple-page-master master-name="cover-even" page-width="{$page.width}" page-height="{$page.height}"
                           margin-top="0" margin-bottom="0" margin-left="0" margin-right="0">
      <fo:region-body margin-bottom="0" margin-top="0" margin-left="0" margin-right="0" column-gap="0"
                      column-count="1"/>
      <fo:region-before region-name="xsl-region-before-first"
                        extent="0" precedence="false" display-align="before"/>
      <fo:region-after region-name="xsl-region-after-first"
                       extent="0" precedence="false" display-align="after"/>
      <xsl:call-template name="region.outer">
        <xsl:with-param name="sequence">even</xsl:with-param>
        <xsl:with-param name="pageclass">cover</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="region.inner">
        <xsl:with-param name="sequence">even</xsl:with-param>
        <xsl:with-param name="pageclass">cover</xsl:with-param>
      </xsl:call-template>
    </fo:simple-page-master>
    <fo:page-sequence-master master-name="cover">
      <fo:repeatable-page-master-alternatives>
        <fo:conditional-page-master-reference master-reference="blank" blank-or-not-blank="blank"/>
        <xsl:if test="$force.blank.pages != 0">
          <fo:conditional-page-master-reference master-reference="cover-first" page-position="first"/>
        </xsl:if>
        <fo:conditional-page-master-reference master-reference="cover-odd" odd-or-even="odd"/>
        <fo:conditional-page-master-reference odd-or-even="even">
          <xsl:attribute name="master-reference">
            <xsl:choose>
              <xsl:when test="$double.sided != 0">cover-even</xsl:when>
              <xsl:otherwise>cover-odd</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </fo:conditional-page-master-reference>
      </fo:repeatable-page-master-alternatives>
    </fo:page-sequence-master>
  </xsl:template>

  <xsl:template name="front.cover">
    <xsl:param name="ref" select="db:info/db:mediaobject/db:imageobject/db:imagedata/@fileref"/>
    <xsl:if test="$ref">
      <fo:page-sequence master-reference="cover">
        <fo:flow flow-name="xsl-region-body">
          <fo:block overflow="visible" text-align="center">
            <fo:external-graphic src="{concat('url(', $ref, ')')}"
                                 content-width="scale-to-fit" content-height="scale-to-fit"
                                 width="141%" height="141%" scaling="uniform"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </xsl:if>
  </xsl:template>

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
							<fo:block text-align="center" space-before.minimum="48pt">
								<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="db:info/db:author"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-body>
			</fo:table>
		</fo:block>
	</xsl:template>

	<xsl:template name="book.titlepage.verso">
  </xsl:template>

  <xsl:template name="acknowledgements.titlepage.recto">
  </xsl:template>

  <!--
  <xsl:template match="processing-instruction('linebreak')">
    <fo:block/>
  </xsl:template>
  -->

  <xsl:template match="db:phrase[@*[local-name() = 'role' and .='smallcaps']]">
    <fo:inline font-family="CharisSC">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="db:phrase[@*[local-name() = 'role' and .='chapter']]">
    <fo:inline font-family="CharisSC">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="db:surname">
    <fo:inline font-family="CharisSC">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="db:phrase[@*[local-name() = 'href']]">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="db:citetitle[@*[local-name() = 'href']]">
    <fo:inline font-style="italic">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="db:para[@*[local-name()='role' and (.='verse')] and @*[local-name()='lang' and (.='el')]]">
    <fo:block font-family="GalatiaSIL">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="db:phrase[@*[local-name()='lang' and (.='el')]]">
    <fo:inline font-family="GalatiaSIL">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="db:phrase[@*[local-name()='lang' and (.='he')]]">
    <fo:inline font-family="EzraSIL">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="processing-instruction('linebreak')">
    <xsl:value-of select="'&#x2028;'"/>
  </xsl:template>
  <xsl:template match="db:phrase[@*[local-name()='role' and (.='numpara')]]">
    <fo:inline font-family="Verdana" font-size="smaller"
               font-weight="bold" color="#6495ed">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <!--
  <xsl:template match="db:phrase[@*[local-name()='role' and (.='numsection')]]">
    <fo:inline>
      <xsl:apply-templates />
    </fo:inline>
  </xsl:template>
  -->
  <xsl:template match="db:phrase[@*[local-name()='role' and (.='ss')]]">
    <fo:inline color="#0000cd">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="db:phrase[@*[local-name()='role' and (.='ssc')]]">
    <fo:inline color="#6495ed" font-family="CharisSC" font-weight="bolder">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="db:phrase[@*[local-name()='role' and (.='ssv')]]">
    <fo:inline color="#00bfff" font-size="80%" font-weight="bolder">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>

</xsl:stylesheet>
