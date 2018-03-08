<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" 
		xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0" 
		xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" 
		version="1.0">

		<xsl:template match="/*">
			<office:document-content office:version="1.2"> 
				<office:body>
					<xsl:apply-templates />
				</office:body>
			</office:document-content >
		</xsl:template>

		<!-- Main templates -->
		<xsl:template match="title">
			verse ch="1" vs><xsl:apply-templates /></h1>
		</xsl:template>
		<xsl:template match="section" name="section">
			<xsl:variable name="nsecsec" select="count(section/section)"/>
			<xsl:variable name="nsec" select="count(section)"/>
			<xsl:variable name="nbr" select="count(br)"/>
			<xsl:choose>
				<xsl:when test="count(verse) > 0 and $nsec = 0">
					<section>
						<xsl:apply-templates select="title" mode="section"/>
						<xsl:apply-templates select="p" mode="section"/>
						<div class="section">
							<xsl:if test="$nbr > 0">
								<xsl:for-each select="br">
									<xsl:variable name="cur" select="."/>
									<xsl:element name="p">
										<xsl:for-each select="../*[self::verse or self::br][count(preceding-sibling::br) = count($cur/preceding-sibling::br)]">
											<xsl:apply-templates select="." mode="section"/>
											<xsl:text> </xsl:text>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
								<xsl:element name="p">
									<xsl:for-each select="br[last()]/following-sibling::verse">
										<xsl:apply-templates select="." mode="section"/>
										<xsl:text> </xsl:text>
									</xsl:for-each>
								</xsl:element>
							</xsl:if>
							<xsl:if test="$nbr = 0">
								<xsl:element name="p">
									<xsl:for-each select="verse">
										<xsl:apply-templates select="." mode="section"/>
										<xsl:text> </xsl:text>
									</xsl:for-each>
								</xsl:element>
							</xsl:if>
						</div>
					</section>
				</xsl:when>
				<xsl:when test="count(.//verse) > 0">
					<section>
						<xsl:apply-templates mode="section"/>
					</section>
				</xsl:when>
				<xsl:otherwise>
					<section>
						<xsl:apply-templates mode="text"/>
					</section>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>

		<!-- Section templates -->
		<xsl:template match="title" mode="section">
			verse ch="1" vs>
				<xsl:apply-templates />
			</h1>
		</xsl:template>
		<xsl:template match="title" mode="section">
			verse ch="1" vs><xsl:apply-templates /></h1>
		</xsl:template>
		<xsl:template match="section" mode="section">
			<xsl:call-template name="section"/>
		</xsl:template>
		<xsl:template match="p" mode="section">
			<p class="intro">
				<xsl:apply-templates />
			</p>
		</xsl:template>
		<xsl:template match="verse" mode="section">
			<xsl:choose>
				<xsl:when test="count(preceding::verse) = count(../preceding::verse)">
					<div class="chapter-number">
						<xsl:value-of select="'Ch. '"/><span class="sc"><xsl:number value="@ch" format="i"/></span>
						<xsl:if test="@vs > 1">
							<xsl:text>, </xsl:text>
							<span class="sv"><xsl:value-of select="@vs"/></span>
						</xsl:if>
						<xsl:value-of select="'.'"/>
					</div>
					<span>
						<xsl:apply-templates />
					</span>
				</xsl:when>
				<xsl:when test="@vs = 1">
					<span class="chapter-number">
						<xsl:value-of select="'Ch. '"/><span class="sc"><xsl:number value="@ch" format="i"/></span>
						<xsl:value-of select="'.'"/>
					</span>
					<span class="verse-number">
						<xsl:value-of select="@vs"/>
					</span>
					<span>
						<xsl:apply-templates />
					</span>
				</xsl:when>
				<xsl:otherwise>
					<span class="verse-number">
						<xsl:value-of select="@vs"/>
					</span>
					<span>
						<xsl:apply-templates />
					</span>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>

		<!-- Text templates -->
		<xsl:template match="title" mode="text">
			verse ch="1" vs><xsl:apply-templates/></h1>
		</xsl:template>
		<xsl:template match="p" mode="text">
			<xsl:element name="p">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:template>
		

		<!-- Inside paragraph templates -->
		<xsl:template match="*[self::ul or self::ol or self::li or self::em or self::i or self::sup]">
			<xsl:variable name="name" select="local-name()"/>
			<xsl:element name="{$name}">
				<xsl:copy-of select="@*"/>
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:template>
		<xsl:template match="marginnote">
			<xsl:variable name="index" select="count(preceding::marginnote) - count(ancestor::section/preceding::marginnote) + 1" />
			<xsl:text>&#8239;</xsl:text>
			<xsl:element name="span">
				<xsl:attribute name="class">tooltip</xsl:attribute>
				<xsl:number value="$index" format="a"/>
			</xsl:element>
			<xsl:element name="div">
				<xsl:attribute name="class">marginnote</xsl:attribute>
				<xsl:element name="span">
					<xsl:attribute name="class">tooltip</xsl:attribute>
					<xsl:number value="$index" format="a"/>
				</xsl:element>
				<xsl:text> </xsl:text>
				<xsl:apply-templates />
			</xsl:element>
			<xsl:text>&#8239;</xsl:text>
		</xsl:template>
		<xsl:template match="bible">
			<span class="bible"><xsl:apply-templates/></span>
		</xsl:template>
		<xsl:template match="bible/ss">
			<span class="ss"><xsl:value-of select="text()"/></span>
		</xsl:template>
		<xsl:template match="bible/sc">
			<span class="sc"><xsl:number value="text()" format="i"/></span>
		</xsl:template>
		<xsl:template match="bible/sv">
			<span class="sv"><xsl:value-of select="text()"/></span>
		</xsl:template>
		<xsl:template match="br">
		</xsl:template>
		<xsl:template match="footnote">
		</xsl:template>

		<xsl:template match="@*|node()">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()"/>
			</xsl:copy>
		</xsl:template>

</xsl:stylesheet>