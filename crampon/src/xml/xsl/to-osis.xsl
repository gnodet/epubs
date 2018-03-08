<?xml version="1.0"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns="http://www.bibletechnologies.net/2003/OSIS/namespace"
        version="1.0">

    <xsl:template match="book">
        <osis
                xmlns="http://www.bibletechnologies.net/2003/OSIS/namespace"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.bibletechnologies.net/2003/OSIS/namespace http://www.bibletechnologies.net/osisCore.2.1.1.xsd">
            <osisText osisIDWork="FreCramponMt" osisRefWork="defaultReferenceScheme" xml:lang="fr" canonical="true">
                <header>
                    <work osisWork="FreCramponMt">
                        <title>La Bible Augustin Crampon 1923</title>
                        <identifier type="OSIS">Bible.FreCramponMt</identifier>
                        <scope>Matt</scope>
                        <refSystem>Bible.KJV</refSystem>
                    </work>
                </header>
                <div type="book" osisID="Matt">
                    <xsl:apply-templates/>
                    <!--
                        <div type="introduction">
                            <title>Introduction</title>
                            <xsl:for-each select="//section[title/text()='Introduction']/p">
                                <p><xsl:apply-templates mode="text"/></p>
                            </xsl:for-each>
                        </div>
                        <xsl:for-each select="//verse[@vs=1]">
                            <xsl:variable name="ch" select="@ch"/>
                            <chapter osisID="Matt.{$ch}">
                                <xsl:for-each select="//verse[@ch=$ch]">
                                    <xsl:variable name="vs" select="@vs"/>
                                    <verse osisID="Matt.{$ch}.{$vs}">
                                        <xsl:copy-of select="text()"/>
                                    </verse>
                                </xsl:for-each>
                            </chapter>
                        </xsl:for-each>
                    -->
                </div>
            </osisText>
        </osis>
    </xsl:template>

    <xsl:template match="section[title/text()='Introduction']">
        <chapter osisID="Matt.0" chapterTitle="Introduction">
            <div type="introduction">
                <xsl:apply-templates/>
            </div>
        </chapter>
    </xsl:template>

    <xsl:template match="section">
        <div type="section">
            <title type="section">
                <xsl:value-of select="title/text()"/>
            </title>
            <xsl:for-each select="section">
                <div type="subsection">
                    <title type="subsection">
                        <xsl:value-of select="title/text()"/>
                    </title>
                    <xsl:for-each select="section">
                        <div type="chapter">
                            <title type="chapter">
                                <xsl:value-of select="title/text()"/>
                            </title>
                            <xsl:call-template name="verses"/>
                        </div>
                    </xsl:for-each>
                    <xsl:call-template name="verses"/>
                </div>
            </xsl:for-each>
            <xsl:call-template name="verses"/>
        </div>
    </xsl:template>
    <!--<xsl:template match="section">-->
    <!--<xsl:variable name="nbr" select="count(br)"/>-->
    <!--<div type="section">-->
    <!--<xsl:apply-templates select="title"/>-->
    <!--<xsl:if test="$nbr > 0">-->
    <!--<xsl:for-each select="br">-->
    <!--<xsl:variable name="cur" select="."/>-->
    <!--<xsl:element name="p">-->
    <!--<xsl:for-each select="../*[self::verse or self::br][count(preceding-sibling::br) = count($cur/preceding-sibling::br)]">-->
    <!--<xsl:apply-templates select="." />-->
    <!--<xsl:text> </xsl:text>-->
    <!--</xsl:for-each>-->
    <!--</xsl:element>-->
    <!--</xsl:for-each>-->
    <!--<xsl:element name="p">-->
    <!--<xsl:for-each select="br[last()]/following-sibling::verse">-->
    <!--<xsl:apply-templates select="." />-->
    <!--<xsl:text> </xsl:text>-->
    <!--</xsl:for-each>-->
    <!--</xsl:element>-->
    <!--</xsl:if>-->
    <!--<xsl:if test="$nbr = 0">-->
    <!--<xsl:element name="p">-->
    <!--<xsl:attribute name="class">verses</xsl:attribute>-->
    <!--<xsl:for-each select="verse">-->
    <!--<xsl:apply-templates select="." />-->
    <!--<xsl:text> </xsl:text>-->
    <!--</xsl:for-each>-->
    <!--</xsl:element>-->
    <!--</xsl:if>-->
    <!--</div>-->
    <!--</xsl:template>-->
    <xsl:template name="verses">
        <xsl:variable name="nbr" select="count(br)"/>
        <xsl:if test="$nbr > 0">
            <xsl:for-each select="br">
                <xsl:variable name="cur" select="."/>
                <xsl:element name="p">
                    <xsl:for-each
                            select="../*[self::verse or self::br][count(preceding-sibling::br) = count($cur/preceding-sibling::br)]">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                </xsl:element>
            </xsl:for-each>
            <xsl:element name="p">
                <xsl:for-each select="br[last()]/following-sibling::verse">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
        <xsl:if test="$nbr = 0">
            <xsl:element name="p">
                <xsl:attribute name="class">verses</xsl:attribute>
                <xsl:for-each select="verse">
                    <xsl:apply-templates select="."/>
                    <xsl:text> </xsl:text>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>
    <xsl:template match="verse">
        <xsl:variable name="ch" select="@ch"/>
        <xsl:variable name="vs" select="@vs"/>
        <verse osisID="Matth.{$ch}.{$vs}">
            <xsl:apply-templates/>
        </verse>
    </xsl:template>

    <xsl:template match="title">
        <title>
            <xsl:copy-of select="text()"/>
        </title>
    </xsl:template>

    <xsl:template match="p">
        <p>
            <xsl:apply-templates/>
        </p>
    </xsl:template>
    <xsl:template match="verse" mode="section">
        <xsl:variable name="ch" select="@ch"/>
        <xsl:variable name="chp" select="@ch - 1"/>
        <xsl:variable name="vs" select="@vs"/>
        <xsl:if test="@vs = 1 and @ch > 1">
            <chapter eID="cr-mt-{$chp}"/>
        </xsl:if>
        <xsl:if test="@vs = 1">
            <chapter osisID="Matth.{$ch}" sID="cr-mt-{$ch}"/>
        </xsl:if>
        <verse osisID="Matth.{$ch}.{$vs}">
            <xsl:apply-templates/>
        </verse>
    </xsl:template>
    <xsl:template name="title">
        <title>
            <xsl:copy-of select="text()"/>
        </title>
    </xsl:template>
    <xsl:template name="footnote">
        <!--
        <div class="footnote">
            <div class="note">
                <xsl:apply-templates select="."/>
            </div>
        </div>
        -->
    </xsl:template>

    <!-- Inside paragraph templates -->
    <xsl:template
            match="*[self::ul or self::ol or self::li or self::em or self::i or self::sup or self::table or self::tr or self::td or self::th]">
        <xsl:variable name="name" select="local-name()"/>
        <xsl:element name="{$name}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="marginnote">
        <!--
        <xsl:variable name="index" select="count(preceding::marginnote) - count(ancestor::section/preceding::marginnote) + 1" />
        <xsl:text>&#8239;</xsl:text>
        <xsl:element name="note">
            <xsl:attribute name="n"><xsl:number value="$index" format="a"/></xsl:attribute>
            <xsl:attribute name="class">marginnote</xsl:attribute>
            <xsl:element name="span">
                <xsl:attribute name="class">tooltip</xsl:attribute>
                <xsl:number value="$index" format="a"/>
            </xsl:element>
            <xsl:text> </xsl:text>
            <xsl:apply-templates />
        </xsl:element>
        <xsl:text>&#8239;</xsl:text>
        -->
    </xsl:template>
    <xsl:template match="bible">
        <span class="bible">
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="bible/ss">
        <span class="ss">
            <xsl:value-of select="text()"/>
        </span>
    </xsl:template>
    <xsl:template match="bible/sc">
        <span class="sc">
            <xsl:number value="text()" format="i"/>
        </span>
    </xsl:template>
    <xsl:template match="bible/sv">
        <span class="sv">
            <xsl:value-of select="text()"/>
        </span>
    </xsl:template>
    <xsl:template match="br">
    </xsl:template>
    <xsl:template match="footnote">
    </xsl:template>
    <xsl:template match="footnotes">
    </xsl:template>

    <xsl:template match="processing-instruction('xml-stylesheet')"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>