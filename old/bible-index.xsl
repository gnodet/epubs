<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes"/>
	<xsl:template match="/items">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title>Bible de Louis Segond</title>
			</head>
			<body>
				<h2>Bible de Louis Segond</h2>
				<ul>
				<xsl:for-each select="item">
					<li>
						<h3><xsl:element name="a">
							<xsl:attribute name="href">
								<xsl:value-of select="descendant-or-self::item[@href][1]/@href"/>
							</xsl:attribute>
							<xsl:value-of select="@text"/>
						</xsl:element></h3>
						<ul>
						<xsl:for-each select="item">
							<li>
								<xsl:element name="a">
									<xsl:attribute name="href">
										<xsl:value-of select="descendant-or-self::item[@href][1]/@href"/>
									</xsl:attribute>
									<xsl:value-of select="@text"/>
								</xsl:element>
							</li>
						</xsl:for-each>
					</ul>
					</li>
				</xsl:for-each>
			</ul>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
