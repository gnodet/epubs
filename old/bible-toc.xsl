<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes"/>
	<xsl:template match="/items">
		<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1" xml:lang="eng">
			<head>
				<meta content="1" name="dtb:depth"/>
			</head>
			<docTitle>
				<text>Doctrine Sociale de l'&#xC9;glise</text>
			</docTitle>
			<navMap>
				<xsl:for-each select="item">
					<xsl:variable name="node1" select="."/>
					<xsl:variable name="number1">
						<xsl:number level="any" count="//item"/>
					</xsl:variable>
					<xsl:element name="navPoint">
						<xsl:attribute name="id">
							<xsl:value-of select="$number1"/>
						</xsl:attribute>
						<xsl:attribute name="playOrder">
							<xsl:value-of select="$number1"/>
						</xsl:attribute>
						<navLabel>
							<text>
								<xsl:value-of select="@text"/>
							</text>
						</navLabel>
						<xsl:element name="content">
							<xsl:attribute name="src">
								<xsl:value-of select="concat('OEBPS/', descendant-or-self::item[@href][1]/@href)"/>
							</xsl:attribute>
						</xsl:element>                   
						<xsl:for-each select="item">
							<xsl:variable name="node2" select="."/>
							<xsl:variable name="number2">
								<xsl:number level="any" count="//item"/>
							</xsl:variable>
							<xsl:element name="navPoint">
								<xsl:attribute name="id">
									<xsl:value-of select="$number2"/>
								</xsl:attribute>
								<xsl:attribute name="playOrder">
									<xsl:value-of select="$number2"/>
								</xsl:attribute>
								<navLabel>
									<text>
										<xsl:value-of select="@text"/>
									</text>
								</navLabel>
								<xsl:element name="content">
									<xsl:attribute name="src">
										<xsl:value-of select="concat('OEBPS/', descendant-or-self::item[@href][1]/@href)"/>
									</xsl:attribute>
								</xsl:element>                   
							</xsl:element>
						</xsl:for-each>
					</xsl:element>
				</xsl:for-each>
			</navMap>
		</ncx>
	</xsl:template>                       
</xsl:stylesheet>
