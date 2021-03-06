<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:db="http://docbook.org/ns/docbook"
		xmlns:gn="http://gnodet.fr/ns/docbook"
		xmlns="http://www.w3.org/1999/xhtml"
		version="1.0">
	<xsl:import href="urn:docbkx:stylesheet"/>
	<xsl:template name="book.titlepage.verso"/>
	<xsl:template name="svg.property"/>

	<xsl:template name="acknowledgements.titlepage.recto">
	</xsl:template>
	<xsl:template match="processing-instruction('linebreak')">
		<br xmlns="http://www.w3.org/1999/xhtml"/>
	</xsl:template>

</xsl:stylesheet>
