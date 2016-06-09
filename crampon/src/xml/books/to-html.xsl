<?xml version="1.0"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns="http://www.w3.org/1999/xhtml"
		version="1.0">

		<xsl:template match="/*">
			<html>
				<head>
					<style>
						body {
							margin-left: 12em;
							margin-right: 12em;
						}
						body > h1 {
							font-family: 'GregorianFLF';
							font-size: 300%;
							font-stretch: semi-condensed;
							font-weight: bold;
							border: 5px double ;
							margin-bottom: 1px;						
							text-align: center;
						}
						body > section > h1 {
							font-size: 250%;
							text-align: center;
							counter-reset: h1;
						}
						body > section > section > h1 {
							font-size: 150%;
							text-transform: uppercase;
							text-align: center;
							counter-reset: h2;
						}
						body > section > section > h1::before {
							counter-increment: h1;
							content: counter(h1, upper-roman) "° — ";
						}
						body > section > section > section > h1 {
							font-style: italic;
							font-size: 125%;
							text-align: center;
							counter-reset: h3;
						}
						body > section > section > section > h1::before {
							counter-increment: h2;
							content: counter(h2, upper-alpha) ". — ";
						}
						body > section > section > section > section > h1 {
							font-size: 100%;
							text-align: center;
						}
						body > section > section > section > section > h1::before {
							counter-increment: h3;
							content: counter(h3, decimal) ". — ";
						}
						body > section {
							text-align: justify;
						}
						p {
							text-indent: 2em;
						}
						sup, sub {
						  vertical-align: baseline;
						  position: relative;
						  top: -0.4em;
						}
						sub { 
						  top: 0.4em; 
						}
						div.section p:first-of-type {
							text-indent: 0em;
						}
						.verse-number {
							vertical-align: baseline; 
							position: relative; 
							top: -0.4em; 
							font-size: 75%; 
							color: #2554C7; 
							font-weight: bold; 
							margin-right: 0.2em; 
						}
						/*
						.section::first-letter {
							font-family: 'Goudy Initialen';
							font-size: 550%;
							float: left;
						}
						*/
						.pre-lettrine {
							float: left;
						}
						.lettrine {
							float: left;
							font-size: 550%;
							font-family: 'Goudy Initialen';
						}
						.section {
							/* column-count: 2; */
							margin-top: 1em;
							position: relative;
						}
						.bible {font-style:italic;}
						.ss {color:#0000cd;} 
						.sc {color:#6495ed;font-variant:small-caps;font-weight:bolder;}
						.sv {color:#00bfff;font-size:80%;font-weight:bolder;}

						.poplink {
							vertical-align: baseline; 
							position: relative; 
							top: -0.4em; 
							font-size: 75%; 
							font-style: italic;
							color: #2554C7; 
							cursor: pointer; 
						}
						.overlay {
						  position: fixed;
						  top: 0;
						  bottom: 0;
						  left: 0;
						  right: 0;
						  background: rgba(0, 0, 0, 0.7);
						  transition: opacity 500ms;
						  visibility: hidden;
						  opacity: 0;
						}
						.overlay:target {
						  visibility: visible;
						  opacity: 1;
						}
						.popup {
						  margin: 70px auto;
						  padding: 20px;
						  background: #fff;
						  border-radius: 5px;
						  width: 30%;
						  position: relative;
						  transition: all 1s ease-in-out;
						}
						.popup h2 {
						  margin-top: 0;
						  color: #333;
						  font-family: Tahoma, Arial, sans-serif;
						}
						.popup .close {
						  position: absolute;
						  top: 20px;
						  right: 30px;
						  transition: all 200ms;
						  font-size: 30px;
						  font-weight: bold;
						  text-decoration: none;
						  color: #333;
						}
						.popup .close:hover {
						  color: #06D85F;
						}
						.popup .content {
						  max-height: 30%;
						  overflow: auto;
						}
						.overlay-checkbox {
							position: absolute;
							top: -9999px;
							left: -9999px;
						}
						input[type=checkbox]:checked ~ div {
						   background: red;
						}

						.tooltip {
							vertical-align: baseline; 
							position: relative; 
							top: -0.4em; 
							font-size: 75%; 
							font-style: italic;
							color: #2554C7; 
							cursor: pointer; 
						}
						.tooltip .tooltiptext {
						    visibility: hidden;
						    width: 120px;
						    background-color: black;
						    color: #fff;
						    text-align: center;
						    border-radius: 6px;
						    padding: 5px 0;

						    /* Position the tooltip */
						    position: absolute;
						    z-index: 1;
						}

						.tooltip:hover .tooltiptext {
						    visibility: visible;
						}

						.marginnote {
							float: right;
							margin-right: -18em;
							font-size: 50%;
							text-indent: 0;
							text-align: left;
							padding: 0;
							clear: right;
							width: 16em;
						}
						.marginnote:hover {
							font-size: 100%;
							margin-right: -9em;
							width: 8em;
						}
						
						.chapter-number {
							float: left;
							margin-left: -8em;
							font-size: 100%;
							text-indent: 0;
							text-align: right;
							padding: 0;
							clear: left;
							width: 6em;
						}
						
						.intro {
							text-align: center;
							<!-- padding: 0 3em; -->
							margin:0 3em;
						}
						
						ol.upper-roman {
							list-style-type: upper-roman;
						}
						ol.decimal {
							list-style-type: decimal;
						}
						ol.decimal-deg {
							counter-reset: li;
							margin-left:0;
							padding-left:0;
						}
						ol.decimal-deg > li {
							position:relative;
							margin:0 0 0 2em;
							padding-left: 1em;
							list-style:none;
						}
						ol.decimal-deg > li:before {
							content:counter(li) "°";
							counter-increment:li;
							width:2em;
							text-align: right;
							margin-left: -2em;
							margin-right: 8px;
							padding:4px;
						}
						ol.lower-alpha-par {
							counter-reset: li;
							margin-left:0;
							padding-left:0;
						}
						ol.lower-alpha-par > li {
							position:relative;
							margin:0 0 0 2em;
							padding-left: 1em;
							list-style:none;
						}
						ol.lower-alpha-par > li:before {
							content:counter(li, lower-alpha) ")";
							counter-increment:li;
							width:2em;
							text-align: right;
							margin-left: -2em;
							margin-right:8px;
							padding:4px;
						}
						ol.lower-alpha-par-it {
							counter-reset: li;
							margin-left:0;
							padding-left:0;
						}
						ol.lower-alpha-par-it > li {
							position:relative;
							margin:0 0 0 2em;
							padding-left: 1em;
							list-style:none;
						}
						ol.lower-alpha-par-it > li:before {
							content:counter(li, lower-alpha) ")";
							counter-increment:li;
							font-style: italic;
							width:2em;
							text-align: right;
							margin-left: -2em;
							margin-right:8px;
							padding:4px;
						}
						
						i > i {
							font-style: normal;
						}
						cite {
							font-style: italic;
						}
						
						span > .footnote {
							position:fixed;
							font-size: 100%;
							text-indent: 2em;
							text-align: justify;
							width: auto;
						    position: fixed;
						       bottom: 0;
							   left: 0;
							   background: white;
   							transition: visibility 0s, opacity 0.25s linear;
							opacity: 0;
							z-index: 1000;
							padding: 15px;
							margin: 5px;
							border: solid 1px black;
							height: 0;
						}
						span > .footnote > .note {
						}
						span:hover > .footnote {
							visibility: visible;
							opacity: 1;
							height: auto;
						}
						
					</style>
				</head>
				<body>
					<xsl:apply-templates />
				</body>
			</html>
		</xsl:template>

		<!-- Main templates -->
		<xsl:template match="title">
			<h1><xsl:apply-templates /></h1>
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
										<xsl:attribute name="class">verses</xsl:attribute>
										<xsl:for-each select="../*[self::verse or self::br][count(preceding-sibling::br) = count($cur/preceding-sibling::br)]">
											<xsl:apply-templates select="." mode="section"/>
											<xsl:text> </xsl:text>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
								<xsl:element name="p">
									<xsl:attribute name="class">verses</xsl:attribute>
									<xsl:for-each select="br[last()]/following-sibling::verse">
										<xsl:apply-templates select="." mode="section"/>
										<xsl:text> </xsl:text>
									</xsl:for-each>
								</xsl:element>
							</xsl:if>
							<xsl:if test="$nbr = 0">
								<xsl:element name="p">
									<xsl:attribute name="class">verses</xsl:attribute>
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
						<xsl:if test="count(title)=0">
							<h1 style="visibility: hidden; height: 0;"/>
						</xsl:if>
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
			<h1>
				<xsl:apply-templates />
			</h1>
		</xsl:template>
		<xsl:template match="title" mode="section">
			<h1><xsl:apply-templates /></h1>
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
			<xsl:variable name="ch" select="@ch"/>
			<xsl:variable name="vs" select="@vs"/>
			<xsl:choose>
				<xsl:when test="count(preceding::verse) = count(../preceding::verse)">
					<div class="chapter-number">
						<xsl:value-of select="'Ch. '"/><span class="sc"><xsl:number value="@ch" format="1"/></span>
						<xsl:if test="@vs > 1">
							<xsl:text>, </xsl:text>
							<span class="sv"><xsl:value-of select="@vs"/></span>
						</xsl:if>
						<xsl:value-of select="'.'"/>
					</div>
					<span>
						<xsl:apply-templates />
						<xsl:for-each select="//footnotes/p[@ch = $ch and @vs = $vs]">
							<xsl:call-template name="footnote"/>
						</xsl:for-each>
					</span>
				</xsl:when>
				<xsl:when test="@vs = 1">
					<span class="chapter-number">
						<xsl:value-of select="'Ch. '"/><span class="sc"><xsl:number value="@ch" format="1"/></span>
						<xsl:value-of select="'.'"/>
					</span>
					<span class="verse-number">
						<xsl:value-of select="@vs"/>
					</span>
					<span>
						<xsl:apply-templates />
						<xsl:for-each select="//footnotes/p[@ch = $ch and @vs = $vs]">
							<xsl:call-template name="footnote"/>
						</xsl:for-each>
					</span>
				</xsl:when>
				<xsl:otherwise>
					<span class="verse-number">
						<xsl:value-of select="@vs"/>
					</span>
					<span>
						<xsl:apply-templates />
						<xsl:for-each select="//footnotes/p[@ch = $ch and @vs = $vs]">
							<xsl:call-template name="footnote"/>
						</xsl:for-each>
					</span>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>
		<xsl:template name="footnote">
			<div class="footnote">
				<div class="note">
					<xsl:apply-templates select="."/>
				</div>
			</div>
		</xsl:template>
		<xsl:template match="//section/verse[position()=1]/text()[position()=1]">
			<xsl:choose>
				<xsl:when test="starts-with(., '« ')">
					<span class="pre-lettrine">« </span><span class="lettrine"><xsl:value-of select="translate(substring(., 3, 1), 'ÉÀ', 'EA')"/></span><xsl:value-of select="substring(., 4)"/>
				</xsl:when>
				<xsl:otherwise>
					<span class="lettrine"><xsl:value-of select="translate(substring(., 1, 1), 'ÉÀ', 'EA')"/></span><xsl:value-of select="substring(., 2)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>

		<!-- Text templates -->
		<xsl:template match="title" mode="text">
			<h1><xsl:apply-templates/></h1>
		</xsl:template>
		<xsl:template match="p" mode="text">
			<xsl:element name="p">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:template>
		

		<!-- Inside paragraph templates -->
		<xsl:template match="*[self::ul or self::ol or self::li or self::em or self::i or self::sup or self::table or self::tr or self::td or self::br or self::th]">
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
			<span class="sc"><xsl:number value="text()" format="1"/></span>
		</xsl:template>
		<xsl:template match="bible/sv">
			<span class="sv"><xsl:value-of select="text()"/></span>
		</xsl:template>
		<xsl:template match="br">
		</xsl:template>
		<xsl:template match="footnote">
		</xsl:template>
		<xsl:template match="footnotes">
		</xsl:template>

		<xsl:template match="@*|node()">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()"/>
			</xsl:copy>
		</xsl:template>

</xsl:stylesheet>