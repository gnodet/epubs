<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns="http://docbook.org/ns/docbook"
		xmlns:db="http://docbook.org/ns/docbook"
		exclude-result-prefixes='xsl xi fn'
		version="1.0">

	<xsl:template match="processing-instruction('xml-stylesheet')">
	</xsl:template>

	<xsl:template match="*[local-name()='i']">
		<xsl:element name="emphasis">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name()='em']">
		<xsl:element name="emphasis">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name()='u']">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="*[local-name()='span'][@class='br']">
		<xsl:element name="br"/><xsl:element name="br"/>
	</xsl:template>
	<xsl:template match="*[local-name()='br']">
		<xsl:element name="br"/>
	</xsl:template>
	
	<xsl:template match="*[local-name()='phrase' and @*[local-name()='href' and not(starts-with(., '#'))]]">
		<xsl:apply-templates />
	</xsl:template>

	<!-- <xsl:template match="db:chapter/db:title">
		<xsl:copy-of select='.'/>
		<xsl:if test="count(../db:section) > 1">
			<xsl:element name="para">
				<xsl:attribute name="role"><xsl:value-of select="'toc'"/></xsl:attribute>
				<xsl:for-each select="..//db:section">
					<xsl:element name="link">
						<xsl:attribute name="xlink:href">
							<xsl:value-of select="concat('#', @id)"/>
						</xsl:attribute>
						<xsl:value-of select=".//*[local-name()='div' and @class='verse' and @vs='1']/@ch"/>
					</xsl:element>
				</xsl:for-each>
			</xsl:element>
		</xsl:if>
 	</xsl:template> -->
	
	<xsl:template match="*[local-name()='verse']">
		<xsl:variable name="ch">
			<xsl:value-of select="@ch"/>
		</xsl:variable>
		<xsl:variable name="vr">
			<xsl:value-of select="@vs"/>
		</xsl:variable>
		<xsl:if test="$vr = '1'">
			<xsl:element name="phrase">
				<xsl:attribute name="role">chapter-number</xsl:attribute>
				<xsl:copy-of select="$ch"/>
			</xsl:element>
		</xsl:if>
		<xsl:element name="phrase">
			<xsl:attribute name="role">verse-number</xsl:attribute>
			<xsl:copy-of select="$vr"/>
		</xsl:element>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="/*[local-name()='bible']">
		<xsl:element name="book">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="/*[local-name()='bible']/*[local-name()='section']">
		<xsl:element name="part">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="/*[local-name()='bible']/*[local-name()='section']/*[local-name()='section']">
		<xsl:element name="part">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="*[local-name()='book']">
		<xsl:element name="chapter">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<!-- <xsl:template match="*[local-name()='book']/*[local-name()='section']">
		<xsl:element name="section">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template> -->
	<xsl:template match="*[local-name()='book']/*[local-name()='section']/*[local-name()='title']">
	</xsl:template>
	<xsl:template match="*[local-name()='title']">
		<xsl:element name="title">
			<xsl:copy-of select="text()"/>
		</xsl:element>
	</xsl:template>

	<!-- <xsl:template match="*[local-name()='section']">
		<xsl:variable name="id">
			<xsl:value-of select="@id"/>
		</xsl:variable>
		<xsl:element name="section">
			<xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
	  	    <xsl:apply-templates />
		</xsl:element>
	</xsl:template>	 -->
	<xsl:template match="@id">
		<xsl:copy-of select="."/>
	</xsl:template>
	<!-- <xsl:template match="db:section/db:title"> -->
		<!-- <xsl:element name="title">
			<xsl:value-of select="..//*[local-name()='div' and @class='verse' and @vs='1']/@ch"/>
		</xsl:element> -->
	<!-- </xsl:template> -->
	
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
