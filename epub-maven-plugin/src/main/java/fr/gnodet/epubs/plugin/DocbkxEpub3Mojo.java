package fr.gnodet.epubs.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Target;

import javax.xml.transform.Transformer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


/**
 * A Maven plugin for generating epub3 output from DocBook documents, using version
 * 1.79.1 of the DocBook XSL stylesheets. See
 * <a href="http://docbook.sourceforge.net/">http://docbook.sourceforge.net/</a>.
 *
 * @goal generate-epub3
 * @configurator override
 */
public class DocbkxEpub3Mojo
  extends AbstractEpub3Mojo
{

    /**
     * The plugin dependencies.
     *
     * @parameter property="plugin.artifacts"
     * @required
     * @readonly
     */
    List artifacts;

    /**
     * Ant tasks to be executed before the transformation. Comparable
     * to the tasks property in the maven-antrun-plugin.
     *
     * @parameter
     */
    private Target preProcess;

    /**
     * Ant tasks to be executed after the transformation. Comparable
     * to the tasks property in the maven-antrun-plugin.
     *
     * @parameter
     */
    private Target postProcess;

    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * A list of entities to be substituted in the source
     * documents. Note that you can <em>only</em> specify entities if
     * your source DocBook document contains a DTD
     * declaration. Otherwise it will not have any effect at all.
     *
     * @parameter
     */
    private List entities;

    /**
     * A list of additional XSL parameters to give to the XSLT engine.
     * These parameters overrides regular docbook ones as they are last
     * configured.<br/>
     * For regular docbook parameters perfer the use of this plugin facilities
     * offering nammed paramters.<br/>
     * These parameters feet well for custom properties you may have defined
     * within your customization layer.
     *
     * @parameter
     */
    private List customizationParameters = new ArrayList();

    /**
     * List of additional System properties.
     *
     * @parameter
     */
    private Properties systemProperties;

    /**
     * The pattern of the files to be included.
     *
     * @parameter default-value="*.xml"
     */
    private String includes;

    /**
     * A boolean, indicating if XInclude should be supported.
     *
     * @parameter default="false"
     */
     private boolean xincludeSupported;

    /**
     * The location of the stylesheet customization.
     *
     * @parameter
     */
    private File epub3Customization;

    /**
     * The extension of the target file name.
     *
     * @parameter default-value="epub"
     */
    private String targetFileExtension;

    /**
     * The target directory to which all output will be written.
     *
     * @parameter default-value="${basedir}/target/docbkx/epub3"
     */
    private File targetDirectory;

    /**
     * The directory containing the source DocBook files.
     *
     * @parameter default-value="${basedir}/src/docbkx"
     */
    private File sourceDirectory;

    /**
     * The directory containing the resolved DocBook source before given to the transformer.
     *
     * @parameter
     */
    private File generatedSourceDirectory;

    private boolean useStandardOutput = false;

    /**
     * If the XSLT engine should print xsl:messages to standard output.
     *
     * @parameter property="docbkx.showXslMessages"
     */
    private boolean showXslMessages = false;

    /**
    * Skip the execution of the plugin.
    *
    * @parameter property="docbkx.skip" default-value="false"
    * @since 2.0.15
    */
    private boolean skip;

    /**
     * If you are re-using XML content modules in multiple documents, you may want to redirect some of your olinks.
     * (Original XSL attribute: <code>prefer.internal.olink</code>.)
     *
     * @parameter property="docbkx.preferInternalOlink"
     */
    public String preferInternalOlink;

    /**
     * This parameter specifies the width of the navigation pane (containing TOC and other navigation tabs) in pixels.
     * (Original XSL attribute: <code>htmlhelp.hhc.width</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhcWidth"
     */
    public String htmlhelpHhcWidth;

    /**
     * Specifies the maximal depth of TOC on all levels.
     * (Original XSL attribute: <code>toc.max.depth</code>.)
     *
     * @parameter property="docbkx.tocMaxDepth"
     */
    public String tocMaxDepth;

    /**
     * Sets the filename extension to use on navigational graphics used in the headers and footers of chunked HTML.
     * (Original XSL attribute: <code>navig.graphics.extension</code>.)
     *
     * @parameter property="docbkx.navigGraphicsExtension"
     */
    public String navigGraphicsExtension;

    /**
     * If true, callouts are presented with graphics (e.
     * (Original XSL attribute: <code>callout.graphics</code>.)
     *
     * @parameter property="docbkx.calloutGraphics"
     */
    public String calloutGraphics;

    /**
     * Glossaries maintained independently across a set of documents are likely to become inconsistent unless considerable effort is expended to keep them in sync.
     * (Original XSL attribute: <code>glossary.collection</code>.)
     *
     * @parameter property="docbkx.glossaryCollection"
     */
    public String glossaryCollection;

    /**
     * Set to true to have an application menu bar in your HTML Help window.
     * (Original XSL attribute: <code>htmlhelp.show.menu</code>.)
     *
     * @parameter property="docbkx.htmlhelpShowMenu"
     */
    public String htmlhelpShowMenu;

    /**
     * 
     * (Original XSL attribute: <code>xref.with.number.and.title</code>.)
     *
     * @parameter property="docbkx.xrefWithNumberAndTitle"
     */
    public String xrefWithNumberAndTitle;

    /**
     * 
     * (Original XSL attribute: <code>refentry.xref.manvolnum</code>.)
     *
     * @parameter property="docbkx.refentryXrefManvolnum"
     */
    public String refentryXrefManvolnum;

    /**
     * 
     * (Original XSL attribute: <code>webhelp.default.topic</code>.)
     *
     * @parameter property="docbkx.webhelpDefaultTopic"
     */
    public String webhelpDefaultTopic;

    /**
     * 
     * (Original XSL attribute: <code>epub.include.guide</code>.)
     *
     * @parameter property="docbkx.epubIncludeGuide"
     */
    public String epubIncludeGuide;

    /**
     * In DocBook documents that conform to a schema older than V4.
     * (Original XSL attribute: <code>use.role.as.xrefstyle</code>.)
     *
     * @parameter property="docbkx.useRoleAsXrefstyle"
     */
    public String useRoleAsXrefstyle;

    /**
     * Sets direction of text flow and text alignment based on locale.
     * (Original XSL attribute: <code>writing.mode</code>.)
     *
     * @parameter property="docbkx.writingMode"
     */
    public String writingMode;

    /**
     * This parameter permits you to override the text to insert between the two numbers of a page range in an index.
     * (Original XSL attribute: <code>index.range.separator</code>.)
     *
     * @parameter property="docbkx.indexRangeSeparator"
     */
    public String indexRangeSeparator;

    /**
     * 
     * (Original XSL attribute: <code>biblioentry.alt.primary.seps</code>.)
     *
     * @parameter property="docbkx.biblioentryAltPrimarySeps"
     */
    public String biblioentryAltPrimarySeps;

    /**
     * If you want Locate button shown on toolbar, turn this parameter to 1.
     * (Original XSL attribute: <code>htmlhelp.button.locate</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonLocate"
     */
    public String htmlhelpButtonLocate;

    /**
     * 
     * (Original XSL attribute: <code>epub.meta.identifier.id</code>.)
     *
     * @parameter property="docbkx.epubMetaIdentifierId"
     */
    public String epubMetaIdentifierId;

    /**
     * 
     * (Original XSL attribute: <code>bibliography.style</code>.)
     *
     * @parameter property="docbkx.bibliographyStyle"
     */
    public String bibliographyStyle;

    /**
     * 
     * (Original XSL attribute: <code>part.autolabel</code>.)
     *
     * @parameter property="docbkx.partAutolabel"
     */
    public String partAutolabel;

    /**
     * This parameter sets the depth of section chunking.
     * (Original XSL attribute: <code>chunk.section.depth</code>.)
     *
     * @parameter property="docbkx.chunkSectionDepth"
     */
    public String chunkSectionDepth;

    /**
     * 
     * (Original XSL attribute: <code>profile.attribute</code>.)
     *
     * @parameter property="docbkx.profileAttribute"
     */
    public String profileAttribute;

    /**
     * 
     * (Original XSL attribute: <code>ebnf.statement.terminator</code>.)
     *
     * @parameter property="docbkx.ebnfStatementTerminator"
     */
    public String ebnfStatementTerminator;

    /**
     * Set to true to include a Favorites tab in the navigation pane  of the help window.
     * (Original XSL attribute: <code>htmlhelp.show.favorities</code>.)
     *
     * @parameter property="docbkx.htmlhelpShowFavorities"
     */
    public String htmlhelpShowFavorities;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.userlevel</code>.)
     *
     * @parameter property="docbkx.profileUserlevel"
     */
    public String profileUserlevel;

    /**
     * 
     * (Original XSL attribute: <code>callout.unicode.number.limit</code>.)
     *
     * @parameter property="docbkx.calloutUnicodeNumberLimit"
     */
    public String calloutUnicodeNumberLimit;

    /**
     * 
     * (Original XSL attribute: <code>onechunk</code>.)
     *
     * @parameter property="docbkx.onechunk"
     */
    public String onechunk;

    /**
     * If line numbering is enabled, line numbers will appear right justified in a field "width" characters wide.
     * (Original XSL attribute: <code>linenumbering.width</code>.)
     *
     * @parameter property="docbkx.linenumberingWidth"
     */
    public String linenumberingWidth;

    /**
     * 
     * (Original XSL attribute: <code>stylesheet.result.type</code>.)
     *
     * @parameter property="docbkx.stylesheetResultType"
     */
    public String stylesheetResultType;

    /**
     * 
     * (Original XSL attribute: <code>chunk.append</code>.)
     *
     * @parameter property="docbkx.chunkAppend"
     */
    public String chunkAppend;

    /**
     * 
     * (Original XSL attribute: <code>epub.include.metadata.dcterms</code>.)
     *
     * @parameter property="docbkx.epubIncludeMetadataDcterms"
     */
    public String epubIncludeMetadataDcterms;

    /**
     * 
     * (Original XSL attribute: <code>index.prefer.titleabbrev</code>.)
     *
     * @parameter property="docbkx.indexPreferTitleabbrev"
     */
    public String indexPreferTitleabbrev;

    /**
     * Sets the filename extension to use on admonition graphics.
     * (Original XSL attribute: <code>admon.graphics.extension</code>.)
     *
     * @parameter property="docbkx.admonGraphicsExtension"
     */
    public String admonGraphicsExtension;

    /**
     * 
     * (Original XSL attribute: <code>graphicsize.use.img.src.path</code>.)
     *
     * @parameter property="docbkx.graphicsizeUseImgSrcPath"
     */
    public String graphicsizeUseImgSrcPath;

    /**
     * URL address of page accessible by Home button.
     * (Original XSL attribute: <code>htmlhelp.button.home.url</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonHomeUrl"
     */
    public String htmlhelpButtonHomeUrl;

    /**
     * If true, the headers and footers of chunked HTML display the titles of the next and previous chunks, along with the words 'Next' and 'Previous' (or the equivalent graphical icons if navig.
     * (Original XSL attribute: <code>navig.showtitles</code>.)
     *
     * @parameter property="docbkx.navigShowtitles"
     */
    public String navigShowtitles;

    /**
     * 
     * (Original XSL attribute: <code>menuchoice.menu.separator</code>.)
     *
     * @parameter property="docbkx.menuchoiceMenuSeparator"
     */
    public String menuchoiceMenuSeparator;

    /**
     * Set to true to include the Home button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.home</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonHome"
     */
    public String htmlhelpButtonHome;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.status</code>.)
     *
     * @parameter property="docbkx.profileStatus"
     */
    public String profileStatus;

    /**
     * 
     * (Original XSL attribute: <code>empty.local.l10n.xml</code>.)
     *
     * @parameter property="docbkx.emptyLocalL10nXml"
     */
    public String emptyLocalL10nXml;

    /**
     * 
     * (Original XSL attribute: <code>arg.rep.def.str</code>.)
     *
     * @parameter property="docbkx.argRepDefStr"
     */
    public String argRepDefStr;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.button.back</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonBack"
     */
    public String htmlhelpButtonBack;

    /**
     * 
     * (Original XSL attribute: <code>epub.dc.creator.id</code>.)
     *
     * @parameter property="docbkx.epubDcCreatorId"
     */
    public String epubDcCreatorId;

    /**
     * 
     * (Original XSL attribute: <code>othercredit.like.author.enabled</code>.)
     *
     * @parameter property="docbkx.othercreditLikeAuthorEnabled"
     */
    public String othercreditLikeAuthorEnabled;

    /**
     * 
     * (Original XSL attribute: <code>link.to.self.for.mediaobject</code>.)
     *
     * @parameter property="docbkx.linkToSelfForMediaobject"
     */
    public String linkToSelfForMediaobject;

    /**
     * If line numbering is enabled, everyNth line will be numbered.
     * (Original XSL attribute: <code>linenumbering.everyNth</code>.)
     *
     * @parameter property="docbkx.linenumberingEveryNth"
     */
    public String linenumberingEveryNth;

    /**
     * 
     * (Original XSL attribute: <code>qanda.inherit.numeration</code>.)
     *
     * @parameter property="docbkx.qandaInheritNumeration"
     */
    public String qandaInheritNumeration;

    /**
     * If true, then each olink will generate several messages about how it is being resolved during processing.
     * (Original XSL attribute: <code>olink.debug</code>.)
     *
     * @parameter property="docbkx.olinkDebug"
     */
    public String olinkDebug;

    /**
     * Specifies the border color of table frames.
     * (Original XSL attribute: <code>table.frame.border.color</code>.)
     *
     * @parameter property="docbkx.tableFrameBorderColor"
     */
    public String tableFrameBorderColor;

    /**
     * Specifies the thickness of the border on the table's frame.
     * (Original XSL attribute: <code>table.frame.border.thickness</code>.)
     *
     * @parameter property="docbkx.tableFrameBorderThickness"
     */
    public String tableFrameBorderThickness;

    /**
     * If true, the HTML stylesheet will generate ID attributes on containers.
     * (Original XSL attribute: <code>generate.id.attributes</code>.)
     *
     * @parameter property="docbkx.generateIdAttributes"
     */
    public String generateIdAttributes;

    /**
     * 
     * (Original XSL attribute: <code>toc.entry.default.text</code>.)
     *
     * @parameter property="docbkx.tocEntryDefaultText"
     */
    public String tocEntryDefaultText;

    /**
     * This parameter has effect only when Saxon 6 is used (version 6.
     * (Original XSL attribute: <code>saxon.character.representation</code>.)
     *
     * @parameter property="docbkx.saxonCharacterRepresentation"
     */
    public String saxonCharacterRepresentation;

    /**
     * The stylesheets are capable of generating a default CSS stylesheet file.
     * (Original XSL attribute: <code>docbook.css.link</code>.)
     *
     * @parameter property="docbkx.docbookCssLink"
     */
    public String docbookCssLink;

    /**
     * When true this parameter enables enhanced decompilation of CHM.
     * (Original XSL attribute: <code>htmlhelp.enhanced.decompilation</code>.)
     *
     * @parameter property="docbkx.htmlhelpEnhancedDecompilation"
     */
    public String htmlhelpEnhancedDecompilation;

    /**
     * 
     * (Original XSL attribute: <code>emphasis.propagates.style</code>.)
     *
     * @parameter property="docbkx.emphasisPropagatesStyle"
     */
    public String emphasisPropagatesStyle;

    /**
     * Specifies where formal object titles should occur.
     * (Original XSL attribute: <code>formal.title.placement</code>.)
     *
     * @parameter property="docbkx.formalTitlePlacement"
     */
    public String formalTitlePlacement;

    /**
     * 
     * (Original XSL attribute: <code>l10n.lang.value.rfc.compliant</code>.)
     *
     * @parameter property="docbkx.l10nLangValueRfcCompliant"
     */
    public String l10nLangValueRfcCompliant;

    /**
     * 
     * (Original XSL attribute: <code>editedby.enabled</code>.)
     *
     * @parameter property="docbkx.editedbyEnabled"
     */
    public String editedbyEnabled;

    /**
     * 
     * (Original XSL attribute: <code>html.stylesheet</code>.)
     *
     * @parameter property="docbkx.htmlStylesheet"
     */
    public String htmlStylesheet;

    /**
     * This parameter defines a list of lang values to search among to resolve olinks.
     * (Original XSL attribute: <code>olink.lang.fallback.sequence</code>.)
     *
     * @parameter property="docbkx.olinkLangFallbackSequence"
     */
    public String olinkLangFallbackSequence;

    /**
     * 
     * (Original XSL attribute: <code>epub.package.pathname</code>.)
     *
     * @parameter property="docbkx.epubPackagePathname"
     */
    public String epubPackagePathname;

    /**
     * 
     * (Original XSL attribute: <code>admon.style</code>.)
     *
     * @parameter property="docbkx.admonStyle"
     */
    public String admonStyle;

    /**
     * If set to zero, there will be no entry for the root element in the  ToC.
     * (Original XSL attribute: <code>htmlhelp.hhc.show.root</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhcShowRoot"
     */
    public String htmlhelpHhcShowRoot;

    /**
     * 
     * (Original XSL attribute: <code>epub.container.pathname</code>.)
     *
     * @parameter property="docbkx.epubContainerPathname"
     */
    public String epubContainerPathname;

    /**
     * 
     * (Original XSL attribute: <code>webhelp.start.filename</code>.)
     *
     * @parameter property="docbkx.webhelpStartFilename"
     */
    public String webhelpStartFilename;

    /**
     * 
     * (Original XSL attribute: <code>use.embed.for.svg</code>.)
     *
     * @parameter property="docbkx.useEmbedForSvg"
     */
    public String useEmbedForSvg;

    /**
     * 
     * (Original XSL attribute: <code>footnote.number.symbols</code>.)
     *
     * @parameter property="docbkx.footnoteNumberSymbols"
     */
    public String footnoteNumberSymbols;

    /**
     * 
     * (Original XSL attribute: <code>insert.xref.page.number.para</code>.)
     *
     * @parameter property="docbkx.insertXrefPageNumberPara"
     */
    public String insertXrefPageNumberPara;

    /**
     * 
     * (Original XSL attribute: <code>epub.dc.language.id</code>.)
     *
     * @parameter property="docbkx.epubDcLanguageId"
     */
    public String epubDcLanguageId;

    /**
     * 
     * (Original XSL attribute: <code>chunk.first.sections</code>.)
     *
     * @parameter property="docbkx.chunkFirstSections"
     */
    public String chunkFirstSections;

    /**
     * Specify which characters are to be counted as punctuation.
     * (Original XSL attribute: <code>runinhead.title.end.punct</code>.)
     *
     * @parameter property="docbkx.runinheadTitleEndPunct"
     */
    public String runinheadTitleEndPunct;

    /**
     * If true, header navigation will be suppressed.
     * (Original XSL attribute: <code>suppress.header.navigation</code>.)
     *
     * @parameter property="docbkx.suppressHeaderNavigation"
     */
    public String suppressHeaderNavigation;

    /**
     * 
     * (Original XSL attribute: <code>epub.meta.language.id</code>.)
     *
     * @parameter property="docbkx.epubMetaLanguageId"
     */
    public String epubMetaLanguageId;

    /**
     * If true, header and footer navigation will be suppressed.
     * (Original XSL attribute: <code>suppress.navigation</code>.)
     *
     * @parameter property="docbkx.suppressNavigation"
     */
    public String suppressNavigation;

    /**
     * This parameter specifies the punctuation that should be added after an honorific in a personal name.
     * (Original XSL attribute: <code>punct.honorific</code>.)
     *
     * @parameter property="docbkx.punctHonorific"
     */
    public String punctHonorific;

    /**
     * 
     * (Original XSL attribute: <code>epub.cover.linear</code>.)
     *
     * @parameter property="docbkx.epubCoverLinear"
     */
    public String epubCoverLinear;

    /**
     * Sets the background color for EBNF tables (a pale brown).
     * (Original XSL attribute: <code>ebnf.table.bgcolor</code>.)
     *
     * @parameter property="docbkx.ebnfTableBgcolor"
     */
    public String ebnfTableBgcolor;

    /**
     * 
     * (Original XSL attribute: <code>annotation.css</code>.)
     *
     * @parameter property="docbkx.annotationCss"
     */
    public String annotationCss;

    /**
     * 
     * (Original XSL attribute: <code>kindle.extensions</code>.)
     *
     * @parameter property="docbkx.kindleExtensions"
     */
    public String kindleExtensions;

    /**
     * This parameter specifies the list of elements that should be escaped as CDATA sections by the chunking stylesheet.
     * (Original XSL attribute: <code>chunker.output.cdata-section-elements</code>.)
     *
     * @parameter property="docbkx.chunkerOutputCdataSectionElements"
     */
    public String chunkerOutputCdataSectionElements;

    /**
     * 
     * (Original XSL attribute: <code>img.src.path</code>.)
     *
     * @parameter property="docbkx.imgSrcPath"
     */
    public String imgSrcPath;

    /**
     * When cross reference data is collected for resolving olinks, it may be necessary to prepend a base URI to each target's href.
     * (Original XSL attribute: <code>olink.base.uri</code>.)
     *
     * @parameter property="docbkx.olinkBaseUri"
     */
    public String olinkBaseUri;

    /**
     * 
     * (Original XSL attribute: <code>epub.ncx.depth</code>.)
     *
     * @parameter property="docbkx.epubNcxDepth"
     */
    public String epubNcxDepth;

    /**
     * 
     * (Original XSL attribute: <code>make.year.ranges</code>.)
     *
     * @parameter property="docbkx.makeYearRanges"
     */
    public String makeYearRanges;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.plain.open.str</code>.)
     *
     * @parameter property="docbkx.argChoicePlainOpenStr"
     */
    public String argChoicePlainOpenStr;

    /**
     * Set to true to display texts under toolbar buttons, zero to switch off displays.
     * (Original XSL attribute: <code>htmlhelp.show.toolbar.text</code>.)
     *
     * @parameter property="docbkx.htmlhelpShowToolbarText"
     */
    public String htmlhelpShowToolbarText;

    /**
     * This parameter allows you to control the punctuation of certain types of generated cross reference text.
     * (Original XSL attribute: <code>xref.title-page.separator</code>.)
     *
     * @parameter property="docbkx.xrefTitlePageSeparator"
     */
    public String xrefTitlePageSeparator;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.security</code>.)
     *
     * @parameter property="docbkx.profileSecurity"
     */
    public String profileSecurity;

    /**
     * 
     * (Original XSL attribute: <code>insert.olink.page.number</code>.)
     *
     * @parameter property="docbkx.insertOlinkPageNumber"
     */
    public String insertOlinkPageNumber;

    /**
     * 
     * (Original XSL attribute: <code>webhelp.base.dir</code>.)
     *
     * @parameter property="docbkx.webhelpBaseDir"
     */
    public String webhelpBaseDir;

    /**
     * 
     * (Original XSL attribute: <code>qanda.nested.in.toc</code>.)
     *
     * @parameter property="docbkx.qandaNestedInToc"
     */
    public String qandaNestedInToc;

    /**
     * 
     * (Original XSL attribute: <code>profile.value</code>.)
     *
     * @parameter property="docbkx.profileValue"
     */
    public String profileValue;

    /**
     * When the stylesheet assigns an id value to an output element, the generate-id() function may be used.
     * (Original XSL attribute: <code>generate.consistent.ids</code>.)
     *
     * @parameter property="docbkx.generateConsistentIds"
     */
    public String generateConsistentIds;

    /**
     * 
     * (Original XSL attribute: <code>epub.meta.creator.id</code>.)
     *
     * @parameter property="docbkx.epubMetaCreatorId"
     */
    public String epubMetaCreatorId;

    /**
     * 
     * (Original XSL attribute: <code>html.script.type</code>.)
     *
     * @parameter property="docbkx.htmlScriptType"
     */
    public String htmlScriptType;

    /**
     * This location has precedence over the corresponding Java property.
     * (Original XSL attribute: <code>highlight.xslthl.config</code>.)
     *
     * @parameter property="docbkx.highlightXslthlConfig"
     */
    public String highlightXslthlConfig;

    /**
     * 
     * (Original XSL attribute: <code>epub.dc.identifier.id</code>.)
     *
     * @parameter property="docbkx.epubDcIdentifierId"
     */
    public String epubDcIdentifierId;

    /**
     * 
     * (Original XSL attribute: <code>section.autolabel.max.depth</code>.)
     *
     * @parameter property="docbkx.sectionAutolabelMaxDepth"
     */
    public String sectionAutolabelMaxDepth;

    /**
     * Eclipse Help plugin name.
     * (Original XSL attribute: <code>eclipse.plugin.name</code>.)
     *
     * @parameter property="docbkx.eclipsePluginName"
     */
    public String eclipsePluginName;

    /**
     * 
     * (Original XSL attribute: <code>make.clean.html</code>.)
     *
     * @parameter property="docbkx.makeCleanHtml"
     */
    public String makeCleanHtml;

    /**
     * 
     * (Original XSL attribute: <code>para.propagates.style</code>.)
     *
     * @parameter property="docbkx.paraPropagatesStyle"
     */
    public String paraPropagatesStyle;

    /**
     * 
     * (Original XSL attribute: <code>index.links.to.section</code>.)
     *
     * @parameter property="docbkx.indexLinksToSection"
     */
    public String indexLinksToSection;

    /**
     * 
     * (Original XSL attribute: <code>component.label.includes.part.label</code>.)
     *
     * @parameter property="docbkx.componentLabelIncludesPartLabel"
     */
    public String componentLabelIncludesPartLabel;

    /**
     * String used to separate labels and titles in a table of contents.
     * (Original XSL attribute: <code>autotoc.label.separator</code>.)
     *
     * @parameter property="docbkx.autotocLabelSeparator"
     */
    public String autotocLabelSeparator;

    /**
     * 
     * (Original XSL attribute: <code>chunked.filename.prefix</code>.)
     *
     * @parameter property="docbkx.chunkedFilenamePrefix"
     */
    public String chunkedFilenamePrefix;

    /**
     * Set to true to include the Prev button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.prev</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonPrev"
     */
    public String htmlhelpButtonPrev;

    /**
     * 
     * (Original XSL attribute: <code>html.append</code>.)
     *
     * @parameter property="docbkx.htmlAppend"
     */
    public String htmlAppend;

    /**
     * 
     * (Original XSL attribute: <code>textdata.default.encoding</code>.)
     *
     * @parameter property="docbkx.textdataDefaultEncoding"
     */
    public String textdataDefaultEncoding;

    /**
     * Sets the path to the directory holding the callout graphics.
     * (Original XSL attribute: <code>callout.graphics.path</code>.)
     *
     * @parameter property="docbkx.calloutGraphicsPath"
     */
    public String calloutGraphicsPath;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.lang</code>.)
     *
     * @parameter property="docbkx.profileLang"
     */
    public String profileLang;

    /**
     * 
     * (Original XSL attribute: <code>epub.embedded.fonts</code>.)
     *
     * @parameter property="docbkx.epubEmbeddedFonts"
     */
    public String epubEmbeddedFonts;

    /**
     * If true (true), admonitions are presented in an alternate style that uses a graphic.
     * (Original XSL attribute: <code>admon.graphics</code>.)
     *
     * @parameter property="docbkx.admonGraphics"
     */
    public String admonGraphics;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.outputformat</code>.)
     *
     * @parameter property="docbkx.profileOutputformat"
     */
    public String profileOutputformat;

    /**
     * 
     * (Original XSL attribute: <code>epub.version</code>.)
     *
     * @parameter property="docbkx.epubVersion"
     */
    public String epubVersion;

    /**
     * 
     * (Original XSL attribute: <code>manifest.in.base.dir</code>.)
     *
     * @parameter property="docbkx.manifestInBaseDir"
     */
    public String manifestInBaseDir;

    /**
     * If true, CSS will be used to draw table borders.
     * (Original XSL attribute: <code>table.borders.with.css</code>.)
     *
     * @parameter property="docbkx.tableBordersWithCss"
     */
    public String tableBordersWithCss;

    /**
     * 
     * (Original XSL attribute: <code>reference.autolabel</code>.)
     *
     * @parameter property="docbkx.referenceAutolabel"
     */
    public String referenceAutolabel;

    /**
     * 
     * (Original XSL attribute: <code>abstract.notitle.enabled</code>.)
     *
     * @parameter property="docbkx.abstractNotitleEnabled"
     */
    public String abstractNotitleEnabled;

    /**
     * This parameter allows you to control the punctuation of certain types of generated cross reference text.
     * (Original XSL attribute: <code>xref.label-title.separator</code>.)
     *
     * @parameter property="docbkx.xrefLabelTitleSeparator"
     */
    public String xrefLabelTitleSeparator;

    /**
     * Name of auxiliary file for TeX equations.
     * (Original XSL attribute: <code>tex.math.file</code>.)
     *
     * @parameter property="docbkx.texMathFile"
     */
    public String texMathFile;

    /**
     * This parameter specifies the value of the omit-xml-declaration specification for generated pages.
     * (Original XSL attribute: <code>chunker.output.omit-xml-declaration</code>.)
     *
     * @parameter property="docbkx.chunkerOutputOmitXmlDeclaration"
     */
    public String chunkerOutputOmitXmlDeclaration;

    /**
     * 
     * (Original XSL attribute: <code>epub.container.filename</code>.)
     *
     * @parameter property="docbkx.epubContainerFilename"
     */
    public String epubContainerFilename;

    /**
     * 
     * (Original XSL attribute: <code>epub.cover.filename</code>.)
     *
     * @parameter property="docbkx.epubCoverFilename"
     */
    public String epubCoverFilename;

    /**
     * If true, the scaling attributes on graphics and media objects are ignored.
     * (Original XSL attribute: <code>ignore.image.scaling</code>.)
     *
     * @parameter property="docbkx.ignoreImageScaling"
     */
    public String ignoreImageScaling;

    /**
     * In order to resolve olinks efficiently, the stylesheets can generate an external data file containing information about all potential cross reference endpoints in a document.
     * (Original XSL attribute: <code>collect.xref.targets</code>.)
     *
     * @parameter property="docbkx.collectXrefTargets"
     */
    public String collectXrefTargets;

    /**
     * 
     * (Original XSL attribute: <code>annotation.js</code>.)
     *
     * @parameter property="docbkx.annotationJs"
     */
    public String annotationJs;

    /**
     * 
     * (Original XSL attribute: <code>highlight.source</code>.)
     *
     * @parameter property="docbkx.highlightSource"
     */
    public String highlightSource;

    /**
     * 
     * (Original XSL attribute: <code>shade.verbatim</code>.)
     *
     * @parameter property="docbkx.shadeVerbatim"
     */
    public String shadeVerbatim;

    /**
     * 
     * (Original XSL attribute: <code>firstterm.only.link</code>.)
     *
     * @parameter property="docbkx.firsttermOnlyLink"
     */
    public String firsttermOnlyLink;

    /**
     * When olinks between documents are resolved, the generated text may not make it clear that the reference is to another document.
     * (Original XSL attribute: <code>olink.doctitle</code>.)
     *
     * @parameter property="docbkx.olinkDoctitle"
     */
    public String olinkDoctitle;

    /**
     * This image is used on popup annotations as the “x” that the user can click to dismiss the popup.
     * (Original XSL attribute: <code>annotation.graphic.close</code>.)
     *
     * @parameter property="docbkx.annotationGraphicClose"
     */
    public String annotationGraphicClose;

    /**
     * 
     * (Original XSL attribute: <code>epub.include.optional.metadata.dc.elements</code>.)
     *
     * @parameter property="docbkx.epubIncludeOptionalMetadataDcElements"
     */
    public String epubIncludeOptionalMetadataDcElements;

    /**
     * Title of Jump2 button.
     * (Original XSL attribute: <code>htmlhelp.button.jump2.title</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonJump2Title"
     */
    public String htmlhelpButtonJump2Title;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.def.open.str</code>.)
     *
     * @parameter property="docbkx.argChoiceDefOpenStr"
     */
    public String argChoiceDefOpenStr;

    /**
     * Set to true to include the Jump2 button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.jump2</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonJump2"
     */
    public String htmlhelpButtonJump2;

    /**
     * 
     * (Original XSL attribute: <code>chunk.fast</code>.)
     *
     * @parameter property="docbkx.chunkFast"
     */
    public String chunkFast;

    /**
     * 
     * (Original XSL attribute: <code>link.mailto.url</code>.)
     *
     * @parameter property="docbkx.linkMailtoUrl"
     */
    public String linkMailtoUrl;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.button.jump1</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonJump1"
     */
    public String htmlhelpButtonJump1;

    /**
     * 
     * (Original XSL attribute: <code>variablelist.as.table</code>.)
     *
     * @parameter property="docbkx.variablelistAsTable"
     */
    public String variablelistAsTable;

    /**
     * Specify if an index should be generated.
     * (Original XSL attribute: <code>generate.index</code>.)
     *
     * @parameter property="docbkx.generateIndex"
     */
    public String generateIndex;

    /**
     * Content of this parameter will be used as a title for generated HTML Help.
     * (Original XSL attribute: <code>htmlhelp.title</code>.)
     *
     * @parameter property="docbkx.htmlhelpTitle"
     */
    public String htmlhelpTitle;

    /**
     * 
     * (Original XSL attribute: <code>use.role.for.mediaobject</code>.)
     *
     * @parameter property="docbkx.useRoleForMediaobject"
     */
    public String useRoleForMediaobject;

    /**
     * If true, a separator will be generated between consecutive reference pages.
     * (Original XSL attribute: <code>refentry.separator</code>.)
     *
     * @parameter property="docbkx.refentrySeparator"
     */
    public String refentrySeparator;

    /**
     * Selects the border on EBNF tables.
     * (Original XSL attribute: <code>ebnf.table.border</code>.)
     *
     * @parameter property="docbkx.ebnfTableBorder"
     */
    public String ebnfTableBorder;

    /**
     * 
     * (Original XSL attribute: <code>function.parens</code>.)
     *
     * @parameter property="docbkx.functionParens"
     */
    public String functionParens;

    /**
     * Sets the path to the directory containing the admonition graphics (caution.
     * (Original XSL attribute: <code>admon.graphics.path</code>.)
     *
     * @parameter property="docbkx.admonGraphicsPath"
     */
    public String admonGraphicsPath;

    /**
     * When cross reference data is collected for use by olinks, the data for each potential target includes one field containing a completely assembled cross reference string, as if it were an xref generated in that document.
     * (Original XSL attribute: <code>use.local.olink.style</code>.)
     *
     * @parameter property="docbkx.useLocalOlinkStyle"
     */
    public String useLocalOlinkStyle;

    /**
     * If true, unlabeled qandadivs will be enumerated.
     * (Original XSL attribute: <code>qandadiv.autolabel</code>.)
     *
     * @parameter property="docbkx.qandadivAutolabel"
     */
    public String qandadivAutolabel;

    /**
     * 
     * (Original XSL attribute: <code>epub.oebps.dir</code>.)
     *
     * @parameter property="docbkx.epubOebpsDir"
     */
    public String epubOebpsDir;

    /**
     * 
     * (Original XSL attribute: <code>blurb.on.titlepage.enabled</code>.)
     *
     * @parameter property="docbkx.blurbOnTitlepageEnabled"
     */
    public String blurbOnTitlepageEnabled;

    /**
     * 
     * (Original XSL attribute: <code>callouts.extension</code>.)
     *
     * @parameter property="docbkx.calloutsExtension"
     */
    public String calloutsExtension;

    /**
     * 
     * (Original XSL attribute: <code>make.graphic.viewport</code>.)
     *
     * @parameter property="docbkx.makeGraphicViewport"
     */
    public String makeGraphicViewport;

    /**
     * 
     * (Original XSL attribute: <code>linenumbering.extension</code>.)
     *
     * @parameter property="docbkx.linenumberingExtension"
     */
    public String linenumberingExtension;

    /**
     * 
     * (Original XSL attribute: <code>default.image.width</code>.)
     *
     * @parameter property="docbkx.defaultImageWidth"
     */
    public String defaultImageWidth;

    /**
     * 
     * (Original XSL attribute: <code>label.from.part</code>.)
     *
     * @parameter property="docbkx.labelFromPart"
     */
    public String labelFromPart;

    /**
     * This parameter specifies the output method to be used in files generated by the chunking stylesheet.
     * (Original XSL attribute: <code>chunker.output.method</code>.)
     *
     * @parameter property="docbkx.chunkerOutputMethod"
     */
    public String chunkerOutputMethod;

    /**
     * Title of Jump1 button.
     * (Original XSL attribute: <code>htmlhelp.button.jump1.title</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonJump1Title"
     */
    public String htmlhelpButtonJump1Title;

    /**
     * 
     * (Original XSL attribute: <code>nominal.image.depth</code>.)
     *
     * @parameter property="docbkx.nominalImageDepth"
     */
    public String nominalImageDepth;

    /**
     * 
     * (Original XSL attribute: <code>html.head.legalnotice.link.multiple</code>.)
     *
     * @parameter property="docbkx.htmlHeadLegalnoticeLinkMultiple"
     */
    public String htmlHeadLegalnoticeLinkMultiple;

    /**
     * 
     * (Original XSL attribute: <code>rootid</code>.)
     *
     * @parameter property="docbkx.rootid"
     */
    public String rootid;

    /**
     * If true, a rule will be drawn above the page footers.
     * (Original XSL attribute: <code>footer.rule</code>.)
     *
     * @parameter property="docbkx.footerRule"
     */
    public String footerRule;

    /**
     * 
     * (Original XSL attribute: <code>appendix.autolabel</code>.)
     *
     * @parameter property="docbkx.appendixAutolabel"
     */
    public String appendixAutolabel;

    /**
     * Graphic widths expressed as a percentage are problematic.
     * (Original XSL attribute: <code>nominal.image.width</code>.)
     *
     * @parameter property="docbkx.nominalImageWidth"
     */
    public String nominalImageWidth;

    /**
     * 
     * (Original XSL attribute: <code>qanda.in.toc</code>.)
     *
     * @parameter property="docbkx.qandaInToc"
     */
    public String qandaInToc;

    /**
     * 
     * (Original XSL attribute: <code>html.longdesc.link</code>.)
     *
     * @parameter property="docbkx.htmlLongdescLink"
     */
    public String htmlLongdescLink;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.conformance</code>.)
     *
     * @parameter property="docbkx.profileConformance"
     */
    public String profileConformance;

    /**
     * Normally first chunk of document is displayed when you open HTML Help file.
     * (Original XSL attribute: <code>htmlhelp.default.topic</code>.)
     *
     * @parameter property="docbkx.htmlhelpDefaultTopic"
     */
    public String htmlhelpDefaultTopic;

    /**
     * 
     * (Original XSL attribute: <code>segmentedlist.as.table</code>.)
     *
     * @parameter property="docbkx.segmentedlistAsTable"
     */
    public String segmentedlistAsTable;

    /**
     * 
     * (Original XSL attribute: <code>graphic.default.extension</code>.)
     *
     * @parameter property="docbkx.graphicDefaultExtension"
     */
    public String graphicDefaultExtension;

    /**
     * Set the section depth in the left pane of HTML Help viewer.
     * (Original XSL attribute: <code>htmlhelp.hhc.section.depth</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhcSectionDepth"
     */
    public String htmlhelpHhcSectionDepth;

    /**
     * 
     * (Original XSL attribute: <code>simplesect.in.toc</code>.)
     *
     * @parameter property="docbkx.simplesectInToc"
     */
    public String simplesectInToc;

    /**
     * 
     * (Original XSL attribute: <code>table.footnote.number.symbols</code>.)
     *
     * @parameter property="docbkx.tableFootnoteNumberSymbols"
     */
    public String tableFootnoteNumberSymbols;

    /**
     * 
     * (Original XSL attribute: <code>epub.meta.title.id</code>.)
     *
     * @parameter property="docbkx.epubMetaTitleId"
     */
    public String epubMetaTitleId;

    /**
     * 
     * (Original XSL attribute: <code>epub.mimetype.pathname</code>.)
     *
     * @parameter property="docbkx.epubMimetypePathname"
     */
    public String epubMimetypePathname;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.plain.close.str</code>.)
     *
     * @parameter property="docbkx.argChoicePlainCloseStr"
     */
    public String argChoicePlainCloseStr;

    /**
     * 
     * (Original XSL attribute: <code>docbook.css.source</code>.)
     *
     * @parameter property="docbkx.docbookCssSource"
     */
    public String docbookCssSource;

    /**
     * If true title of document is shown before ToC/LoT in separate chunk.
     * (Original XSL attribute: <code>chunk.tocs.and.lots.has.title</code>.)
     *
     * @parameter property="docbkx.chunkTocsAndLotsHasTitle"
     */
    public String chunkTocsAndLotsHasTitle;

    /**
     * 
     * (Original XSL attribute: <code>show.revisionflag</code>.)
     *
     * @parameter property="docbkx.showRevisionflag"
     */
    public String showRevisionflag;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.def.close.str</code>.)
     *
     * @parameter property="docbkx.argChoiceDefCloseStr"
     */
    public String argChoiceDefCloseStr;

    /**
     * If true, section labels are prefixed with the label of the component that contains them.
     * (Original XSL attribute: <code>section.label.includes.component.label</code>.)
     *
     * @parameter property="docbkx.sectionLabelIncludesComponentLabel"
     */
    public String sectionLabelIncludesComponentLabel;

    /**
     * 
     * (Original XSL attribute: <code>graphicsize.extension</code>.)
     *
     * @parameter property="docbkx.graphicsizeExtension"
     */
    public String graphicsizeExtension;

    /**
     * 
     * (Original XSL attribute: <code>manifest</code>.)
     *
     * @parameter property="docbkx.manifest"
     */
    public String manifest;

    /**
     * 
     * (Original XSL attribute: <code>runinhead.default.title.end.punct</code>.)
     *
     * @parameter property="docbkx.runinheadDefaultTitleEndPunct"
     */
    public String runinheadDefaultTitleEndPunct;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.display.progress</code>.)
     *
     * @parameter property="docbkx.htmlhelpDisplayProgress"
     */
    public String htmlhelpDisplayProgress;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.force.map.and.alias</code>.)
     *
     * @parameter property="docbkx.htmlhelpForceMapAndAlias"
     */
    public String htmlhelpForceMapAndAlias;

    /**
     * If html.
     * (Original XSL attribute: <code>html.base</code>.)
     *
     * @parameter property="docbkx.htmlBase"
     */
    public String htmlBase;

    /**
     * Set this parameter to 0 to suppress the search tab from webhelp output.
     * (Original XSL attribute: <code>webhelp.include.search.tab</code>.)
     *
     * @parameter property="docbkx.webhelpIncludeSearchTab"
     */
    public String webhelpIncludeSearchTab;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.chm</code>.)
     *
     * @parameter property="docbkx.htmlhelpChm"
     */
    public String htmlhelpChm;

    /**
     * If true, a web link will be generated, presumably to an online man-&gt;HTML gateway.
     * (Original XSL attribute: <code>citerefentry.link</code>.)
     *
     * @parameter property="docbkx.citerefentryLink"
     */
    public String citerefentryLink;

    /**
     * The webhelp output does not use a frameset.
     * (Original XSL attribute: <code>webhelp.tree.cookie.id</code>.)
     *
     * @parameter property="docbkx.webhelpTreeCookieId"
     */
    public String webhelpTreeCookieId;

    /**
     * 
     * (Original XSL attribute: <code>html.script</code>.)
     *
     * @parameter property="docbkx.htmlScript"
     */
    public String htmlScript;

    /**
     * This parameter lets you select which method to use for sorting and grouping  index entries in an index.
     * (Original XSL attribute: <code>index.method</code>.)
     *
     * @parameter property="docbkx.indexMethod"
     */
    public String indexMethod;

    /**
     * 
     * (Original XSL attribute: <code>index.on.type</code>.)
     *
     * @parameter property="docbkx.indexOnType"
     */
    public String indexOnType;

    /**
     * 
     * (Original XSL attribute: <code>epub.output.epub.types</code>.)
     *
     * @parameter property="docbkx.epubOutputEpubTypes"
     */
    public String epubOutputEpubTypes;

    /**
     * 
     * (Original XSL attribute: <code>epub.ncx.manifest.id</code>.)
     *
     * @parameter property="docbkx.epubNcxManifestId"
     */
    public String epubNcxManifestId;

    /**
     * 
     * (Original XSL attribute: <code>epub.cover.pathname</code>.)
     *
     * @parameter property="docbkx.epubCoverPathname"
     */
    public String epubCoverPathname;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.opt.open.str</code>.)
     *
     * @parameter property="docbkx.argChoiceOptOpenStr"
     */
    public String argChoiceOptOpenStr;

    /**
     * Value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.audience</code>.)
     *
     * @parameter property="docbkx.profileAudience"
     */
    public String profileAudience;

    /**
     * 
     * (Original XSL attribute: <code>epub.namespace</code>.)
     *
     * @parameter property="docbkx.epubNamespace"
     */
    public String epubNamespace;

    /**
     * When an automatically generated Table of Contents (or List of Titles) is produced, this HTML element will be used to make the list.
     * (Original XSL attribute: <code>toc.list.type</code>.)
     *
     * @parameter property="docbkx.tocListType"
     */
    public String tocListType;

    /**
     * 
     * (Original XSL attribute: <code>epub.xhtml.mediatype</code>.)
     *
     * @parameter property="docbkx.epubXhtmlMediatype"
     */
    public String epubXhtmlMediatype;

    /**
     * If true, the language of the target will be used when generating cross reference text.
     * (Original XSL attribute: <code>l10n.gentext.use.xref.language</code>.)
     *
     * @parameter property="docbkx.l10nGentextUseXrefLanguage"
     */
    public String l10nGentextUseXrefLanguage;

    /**
     * 
     * (Original XSL attribute: <code>generate.legalnotice.link</code>.)
     *
     * @parameter property="docbkx.generateLegalnoticeLink"
     */
    public String generateLegalnoticeLink;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.os</code>.)
     *
     * @parameter property="docbkx.profileOs"
     */
    public String profileOs;

    /**
     * Specifies the border style of table cells.
     * (Original XSL attribute: <code>table.cell.border.style</code>.)
     *
     * @parameter property="docbkx.tableCellBorderStyle"
     */
    public String tableCellBorderStyle;

    /**
     * 
     * (Original XSL attribute: <code>table.footnote.number.format</code>.)
     *
     * @parameter property="docbkx.tableFootnoteNumberFormat"
     */
    public String tableFootnoteNumberFormat;

    /**
     * 
     * (Original XSL attribute: <code>biblioentry.primary.count</code>.)
     *
     * @parameter property="docbkx.biblioentryPrimaryCount"
     */
    public String biblioentryPrimaryCount;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.req.open.str</code>.)
     *
     * @parameter property="docbkx.argChoiceReqOpenStr"
     */
    public String argChoiceReqOpenStr;

    /**
     * 
     * (Original XSL attribute: <code>html.cleanup</code>.)
     *
     * @parameter property="docbkx.htmlCleanup"
     */
    public String htmlCleanup;

    /**
     * If you want advanced search features in your help, turn this parameter to 1.
     * (Original XSL attribute: <code>htmlhelp.show.advanced.search</code>.)
     *
     * @parameter property="docbkx.htmlhelpShowAdvancedSearch"
     */
    public String htmlhelpShowAdvancedSearch;

    /**
     * This parameter specifies the system identifier that should be used by the chunking stylesheet in the document type declaration of chunked pages.
     * (Original XSL attribute: <code>chunker.output.doctype-system</code>.)
     *
     * @parameter property="docbkx.chunkerOutputDoctypeSystem"
     */
    public String chunkerOutputDoctypeSystem;

    /**
     * 
     * (Original XSL attribute: <code>preface.autolabel</code>.)
     *
     * @parameter property="docbkx.prefaceAutolabel"
     */
    public String prefaceAutolabel;

    /**
     * If you want type math directly in TeX notation in equations, this parameter specifies notation used.
     * (Original XSL attribute: <code>tex.math.in.alt</code>.)
     *
     * @parameter property="docbkx.texMathInAlt"
     */
    public String texMathInAlt;

    /**
     * The stylesheets are capable of generating both default and custom CSS stylesheet files.
     * (Original XSL attribute: <code>generate.css.header</code>.)
     *
     * @parameter property="docbkx.generateCssHeader"
     */
    public String generateCssHeader;

    /**
     * If true, the navigational headers and footers in chunked HTML are presented in an alternate style that uses graphical icons for Next, Previous, Up, and Home.
     * (Original XSL attribute: <code>navig.graphics</code>.)
     *
     * @parameter property="docbkx.navigGraphics"
     */
    public String navigGraphics;

    /**
     * If non-zero, this value will be used as the default cellpadding value in HTML tables.
     * (Original XSL attribute: <code>html.cellpadding</code>.)
     *
     * @parameter property="docbkx.htmlCellpadding"
     */
    public String htmlCellpadding;

    /**
     * 
     * (Original XSL attribute: <code>contrib.inline.enabled</code>.)
     *
     * @parameter property="docbkx.contribInlineEnabled"
     */
    public String contribInlineEnabled;

    /**
     * When olinks between documents are resolved for HTML output, the stylesheet can compute the relative path between the current document and the target document.
     * (Original XSL attribute: <code>current.docid</code>.)
     *
     * @parameter property="docbkx.currentDocid"
     */
    public String currentDocid;

    /**
     * 
     * (Original XSL attribute: <code>keep.relative.image.uris</code>.)
     *
     * @parameter property="docbkx.keepRelativeImageUris"
     */
    public String keepRelativeImageUris;

    /**
     * 
     * (Original XSL attribute: <code>local.l10n.xml</code>.)
     *
     * @parameter property="docbkx.localL10nXml"
     */
    public String localL10nXml;

    /**
     * 
     * (Original XSL attribute: <code>opf.namespace</code>.)
     *
     * @parameter property="docbkx.opfNamespace"
     */
    public String opfNamespace;

    /**
     * 
     * (Original XSL attribute: <code>custom.css.source</code>.)
     *
     * @parameter property="docbkx.customCssSource"
     */
    public String customCssSource;

    /**
     * Specifies the depth to which recursive sections should appear in the TOC.
     * (Original XSL attribute: <code>toc.section.depth</code>.)
     *
     * @parameter property="docbkx.tocSectionDepth"
     */
    public String tocSectionDepth;

    /**
     * Selects draft mode.
     * (Original XSL attribute: <code>draft.mode</code>.)
     *
     * @parameter property="docbkx.draftMode"
     */
    public String draftMode;

    /**
     * The image to be used for draft watermarks.
     * (Original XSL attribute: <code>draft.watermark.image</code>.)
     *
     * @parameter property="docbkx.draftWatermarkImage"
     */
    public String draftWatermarkImage;

    /**
     * 
     * (Original XSL attribute: <code>epub.ncx.mediatype</code>.)
     *
     * @parameter property="docbkx.epubNcxMediatype"
     */
    public String epubNcxMediatype;

    /**
     * 
     * (Original XSL attribute: <code>epub.cover.filename.id</code>.)
     *
     * @parameter property="docbkx.epubCoverFilenameId"
     */
    public String epubCoverFilenameId;

    /**
     * 
     * (Original XSL attribute: <code>component.heading.level</code>.)
     *
     * @parameter property="docbkx.componentHeadingLevel"
     */
    public String componentHeadingLevel;

    /**
     * 
     * (Original XSL attribute: <code>procedure.step.numeration.formats</code>.)
     *
     * @parameter property="docbkx.procedureStepNumerationFormats"
     */
    public String procedureStepNumerationFormats;

    /**
     * Set to true for folder-like icons or zero for book-like icons in the ToC.
     * (Original XSL attribute: <code>htmlhelp.hhc.folders.instead.books</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhcFoldersInsteadBooks"
     */
    public String htmlhelpHhcFoldersInsteadBooks;

    /**
     * 
     * (Original XSL attribute: <code>bridgehead.in.toc</code>.)
     *
     * @parameter property="docbkx.bridgeheadInToc"
     */
    public String bridgeheadInToc;

    /**
     * Formal procedures are numbered and always have a title.
     * (Original XSL attribute: <code>formal.procedures</code>.)
     *
     * @parameter property="docbkx.formalProcedures"
     */
    public String formalProcedures;

    /**
     * 
     * (Original XSL attribute: <code>insert.xref.page.number</code>.)
     *
     * @parameter property="docbkx.insertXrefPageNumber"
     */
    public String insertXrefPageNumber;

    /**
     * 
     * (Original XSL attribute: <code>epub.html.toc.id</code>.)
     *
     * @parameter property="docbkx.epubHtmlTocId"
     */
    public String epubHtmlTocId;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.use.hhk</code>.)
     *
     * @parameter property="docbkx.htmlhelpUseHhk"
     */
    public String htmlhelpUseHhk;

    /**
     * When lengths are converted to pixels, this value is used to determine the size of a pixel.
     * (Original XSL attribute: <code>pixels.per.inch</code>.)
     *
     * @parameter property="docbkx.pixelsPerInch"
     */
    public String pixelsPerInch;

    /**
     * 
     * (Original XSL attribute: <code>phrase.propagates.style</code>.)
     *
     * @parameter property="docbkx.phrasePropagatesStyle"
     */
    public String phrasePropagatesStyle;

    /**
     * 
     * (Original XSL attribute: <code>css.decoration</code>.)
     *
     * @parameter property="docbkx.cssDecoration"
     */
    public String cssDecoration;

    /**
     * 
     * (Original XSL attribute: <code>editor.property</code>.)
     *
     * @parameter property="docbkx.editorProperty"
     */
    public String editorProperty;

    /**
     * Set to true if you want to play with various HTML Help parameters and you don't need to regenerate all HTML files.
     * (Original XSL attribute: <code>htmlhelp.only</code>.)
     *
     * @parameter property="docbkx.htmlhelpOnly"
     */
    public String htmlhelpOnly;

    /**
     * If true, a rule will be drawn below the page headers.
     * (Original XSL attribute: <code>header.rule</code>.)
     *
     * @parameter property="docbkx.headerRule"
     */
    public String headerRule;

    /**
     * 
     * (Original XSL attribute: <code>ulink.target</code>.)
     *
     * @parameter property="docbkx.ulinkTarget"
     */
    public String ulinkTarget;

    /**
     * 
     * (Original XSL attribute: <code>email.delimiters.enabled</code>.)
     *
     * @parameter property="docbkx.emailDelimitersEnabled"
     */
    public String emailDelimitersEnabled;

    /**
     * If true, SVG will be considered an acceptable image format.
     * (Original XSL attribute: <code>use.svg</code>.)
     *
     * @parameter property="docbkx.useSvg"
     */
    public String useSvg;

    /**
     * If true, each of the ToC and LoTs (List of Examples, List of Figures, etc.
     * (Original XSL attribute: <code>chunk.separate.lots</code>.)
     *
     * @parameter property="docbkx.chunkSeparateLots"
     */
    public String chunkSeparateLots;

    /**
     * Set to true to include the Next button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.next</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonNext"
     */
    public String htmlhelpButtonNext;

    /**
     * This parameter allows you to control the punctuation of certain types of generated cross reference text.
     * (Original XSL attribute: <code>xref.label-page.separator</code>.)
     *
     * @parameter property="docbkx.xrefLabelPageSeparator"
     */
    public String xrefLabelPageSeparator;

    /**
     * Maintaining bibliography entries across a set of documents is tedious, time consuming, and error prone.
     * (Original XSL attribute: <code>bibliography.collection</code>.)
     *
     * @parameter property="docbkx.bibliographyCollection"
     */
    public String bibliographyCollection;

    /**
     * 
     * (Original XSL attribute: <code>refentry.generate.name</code>.)
     *
     * @parameter property="docbkx.refentryGenerateName"
     */
    public String refentryGenerateName;

    /**
     * If non-zero, this value will be used as the default cellspacing value in HTML tables.
     * (Original XSL attribute: <code>html.cellspacing</code>.)
     *
     * @parameter property="docbkx.htmlCellspacing"
     */
    public String htmlCellspacing;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.revision</code>.)
     *
     * @parameter property="docbkx.profileRevision"
     */
    public String profileRevision;

    /**
     * 
     * (Original XSL attribute: <code>menuchoice.separator</code>.)
     *
     * @parameter property="docbkx.menuchoiceSeparator"
     */
    public String menuchoiceSeparator;

    /**
     * JavaHelp crashes on some characters when written as character references.
     * (Original XSL attribute: <code>javahelp.encoding</code>.)
     *
     * @parameter property="docbkx.javahelpEncoding"
     */
    public String javahelpEncoding;

    /**
     * 
     * (Original XSL attribute: <code>html.ext</code>.)
     *
     * @parameter property="docbkx.htmlExt"
     */
    public String htmlExt;

    /**
     * Name of default window.
     * (Original XSL attribute: <code>htmlhelp.hhp.window</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhpWindow"
     */
    public String htmlhelpHhpWindow;

    /**
     * 
     * (Original XSL attribute: <code>ebnf.assignment</code>.)
     *
     * @parameter property="docbkx.ebnfAssignment"
     */
    public String ebnfAssignment;

    /**
     * 
     * (Original XSL attribute: <code>chunk.toc</code>.)
     *
     * @parameter property="docbkx.chunkToc"
     */
    public String chunkToc;

    /**
     * In order to resolve olinks efficiently, the stylesheets can generate an external data file containing information about all potential cross reference endpoints in a document.
     * (Original XSL attribute: <code>targets.filename</code>.)
     *
     * @parameter property="docbkx.targetsFilename"
     */
    public String targetsFilename;

    /**
     * 
     * (Original XSL attribute: <code>qanda.defaultlabel</code>.)
     *
     * @parameter property="docbkx.qandaDefaultlabel"
     */
    public String qandaDefaultlabel;

    /**
     * The fixed value used for calculations based upon the size of a character.
     * (Original XSL attribute: <code>points.per.em</code>.)
     *
     * @parameter property="docbkx.pointsPerEm"
     */
    public String pointsPerEm;

    /**
     * If true, footer navigation will be suppressed.
     * (Original XSL attribute: <code>suppress.footer.navigation</code>.)
     *
     * @parameter property="docbkx.suppressFooterNavigation"
     */
    public String suppressFooterNavigation;

    /**
     * If true (true), admonitions are presented with a generated text label such as Note or Warning in the appropriate language.
     * (Original XSL attribute: <code>admon.textlabel</code>.)
     *
     * @parameter property="docbkx.admonTextlabel"
     */
    public String admonTextlabel;

    /**
     * 
     * (Original XSL attribute: <code>make.valid.html</code>.)
     *
     * @parameter property="docbkx.makeValidHtml"
     */
    public String makeValidHtml;

    /**
     * Eclipse Help plugin id.
     * (Original XSL attribute: <code>eclipse.plugin.id</code>.)
     *
     * @parameter property="docbkx.eclipsePluginId"
     */
    public String eclipsePluginId;

    /**
     * 
     * (Original XSL attribute: <code>refentry.generate.title</code>.)
     *
     * @parameter property="docbkx.refentryGenerateTitle"
     */
    public String refentryGenerateTitle;

    /**
     * 
     * (Original XSL attribute: <code>process.empty.source.toc</code>.)
     *
     * @parameter property="docbkx.processEmptySourceToc"
     */
    public String processEmptySourceToc;

    /**
     * If this parameter is set to any value other than the empty string, its value will be used as the value for the language when generating text.
     * (Original XSL attribute: <code>l10n.gentext.language</code>.)
     *
     * @parameter property="docbkx.l10nGentextLanguage"
     */
    public String l10nGentextLanguage;

    /**
     * Set to true to include the Print button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.print</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonPrint"
     */
    public String htmlhelpButtonPrint;

    /**
     * 
     * (Original XSL attribute: <code>annotation.support</code>.)
     *
     * @parameter property="docbkx.annotationSupport"
     */
    public String annotationSupport;

    /**
     * This language is used when there is no language attribute on programlisting.
     * (Original XSL attribute: <code>highlight.default.language</code>.)
     *
     * @parameter property="docbkx.highlightDefaultLanguage"
     */
    public String highlightDefaultLanguage;

    /**
     * This parameter specifies the value of the standalone   specification for generated pages.
     * (Original XSL attribute: <code>chunker.output.standalone</code>.)
     *
     * @parameter property="docbkx.chunkerOutputStandalone"
     */
    public String chunkerOutputStandalone;

    /**
     * When true, additional, empty paragraphs are inserted in several contexts (for example, around informal figures), to create a more pleasing visual appearance in many browsers.
     * (Original XSL attribute: <code>spacing.paras</code>.)
     *
     * @parameter property="docbkx.spacingParas"
     */
    public String spacingParas;

    /**
     * This parameter specifies initial position of help window.
     * (Original XSL attribute: <code>htmlhelp.window.geometry</code>.)
     *
     * @parameter property="docbkx.htmlhelpWindowGeometry"
     */
    public String htmlhelpWindowGeometry;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.output</code>.)
     *
     * @parameter property="docbkx.htmlhelpOutput"
     */
    public String htmlhelpOutput;

    /**
     * 
     * (Original XSL attribute: <code>html.stylesheet.type</code>.)
     *
     * @parameter property="docbkx.htmlStylesheetType"
     */
    public String htmlStylesheetType;

    /**
     * 
     * (Original XSL attribute: <code>epub.vocabulary.profile.content</code>.)
     *
     * @parameter property="docbkx.epubVocabularyProfileContent"
     */
    public String epubVocabularyProfileContent;

    /**
     * 
     * (Original XSL attribute: <code>html.extra.head.links</code>.)
     *
     * @parameter property="docbkx.htmlExtraHeadLinks"
     */
    public String htmlExtraHeadLinks;

    /**
     * To resolve olinks between documents, the stylesheets use a master database document that identifies the target datafiles for all the documents within the scope of the olinks.
     * (Original XSL attribute: <code>target.database.document</code>.)
     *
     * @parameter property="docbkx.targetDatabaseDocument"
     */
    public String targetDatabaseDocument;

    /**
     * If you want to include chapter and section numbers into ToC in,  set this parameter to 1.
     * (Original XSL attribute: <code>epub.autolabel</code>.)
     *
     * @parameter property="docbkx.epubAutolabel"
     */
    public String epubAutolabel;

    /**
     * Set to true to include the  Forward button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.forward</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonForward"
     */
    public String htmlhelpButtonForward;

    /**
     * 
     * (Original XSL attribute: <code>activate.external.olinks</code>.)
     *
     * @parameter property="docbkx.activateExternalOlinks"
     */
    public String activateExternalOlinks;

    /**
     * 
     * (Original XSL attribute: <code>callout.list.table</code>.)
     *
     * @parameter property="docbkx.calloutListTable"
     */
    public String calloutListTable;

    /**
     * 
     * (Original XSL attribute: <code>l10n.gentext.default.language</code>.)
     *
     * @parameter property="docbkx.l10nGentextDefaultLanguage"
     */
    public String l10nGentextDefaultLanguage;

    /**
     * 
     * (Original XSL attribute: <code>autolink.index.see</code>.)
     *
     * @parameter property="docbkx.autolinkIndexSee"
     */
    public String autolinkIndexSee;

    /**
     * 
     * (Original XSL attribute: <code>l10n.xml</code>.)
     *
     * @parameter property="docbkx.l10nXml"
     */
    public String l10nXml;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.wordsize</code>.)
     *
     * @parameter property="docbkx.profileWordsize"
     */
    public String profileWordsize;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.vendor</code>.)
     *
     * @parameter property="docbkx.profileVendor"
     */
    public String profileVendor;

    /**
     * URL address of page accessible by Jump1 button.
     * (Original XSL attribute: <code>htmlhelp.button.jump1.url</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonJump1Url"
     */
    public String htmlhelpButtonJump1Url;

    /**
     * If true (true), unlabeled sections will be enumerated.
     * (Original XSL attribute: <code>section.autolabel</code>.)
     *
     * @parameter property="docbkx.sectionAutolabel"
     */
    public String sectionAutolabel;

    /**
     * 
     * (Original XSL attribute: <code>footnote.number.format</code>.)
     *
     * @parameter property="docbkx.footnoteNumberFormat"
     */
    public String footnoteNumberFormat;

    /**
     * For compatibility with DSSSL based DBTeXMath from Allin Cottrell you should set this parameter to 0.
     * (Original XSL attribute: <code>tex.math.delims</code>.)
     *
     * @parameter property="docbkx.texMathDelims"
     */
    public String texMathDelims;

    /**
     * URL address of page accessible by Jump2 button.
     * (Original XSL attribute: <code>htmlhelp.button.jump2.url</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonJump2Url"
     */
    public String htmlhelpButtonJump2Url;

    /**
     * A mediaobject may contain several objects such as imageobjects.
     * (Original XSL attribute: <code>preferred.mediaobject.role</code>.)
     *
     * @parameter property="docbkx.preferredMediaobjectRole"
     */
    public String preferredMediaobjectRole;

    /**
     * 
     * (Original XSL attribute: <code>cmdsynopsis.hanging.indent</code>.)
     *
     * @parameter property="docbkx.cmdsynopsisHangingIndent"
     */
    public String cmdsynopsisHangingIndent;

    /**
     * 
     * (Original XSL attribute: <code>epub.dc.title.id</code>.)
     *
     * @parameter property="docbkx.epubDcTitleId"
     */
    public String epubDcTitleId;

    /**
     * 
     * (Original XSL attribute: <code>dc.namespace</code>.)
     *
     * @parameter property="docbkx.dcNamespace"
     */
    public String dcNamespace;

    /**
     * If true, TOCs will be annotated.
     * (Original XSL attribute: <code>annotate.toc</code>.)
     *
     * @parameter property="docbkx.annotateToc"
     */
    public String annotateToc;

    /**
     * Set to true to remember help window position between starts.
     * (Original XSL attribute: <code>htmlhelp.remember.window.position</code>.)
     *
     * @parameter property="docbkx.htmlhelpRememberWindowPosition"
     */
    public String htmlhelpRememberWindowPosition;

    /**
     * This parameter specifies the media type that should be used by the chunking stylesheet.
     * (Original XSL attribute: <code>chunker.output.media-type</code>.)
     *
     * @parameter property="docbkx.chunkerOutputMediaType"
     */
    public String chunkerOutputMediaType;

    /**
     * This parameter has a structured value.
     * (Original XSL attribute: <code>generate.toc</code>.)
     *
     * @parameter property="docbkx.generateToc"
     */
    public String generateToc;

    /**
     * 
     * (Original XSL attribute: <code>callout.defaultcolumn</code>.)
     *
     * @parameter property="docbkx.calloutDefaultcolumn"
     */
    public String calloutDefaultcolumn;

    /**
     * 
     * (Original XSL attribute: <code>chapter.autolabel</code>.)
     *
     * @parameter property="docbkx.chapterAutolabel"
     */
    public String chapterAutolabel;

    /**
     * 
     * (Original XSL attribute: <code>use.id.as.filename</code>.)
     *
     * @parameter property="docbkx.useIdAsFilename"
     */
    public String useIdAsFilename;

    /**
     * 
     * (Original XSL attribute: <code>variablelist.term.break.after</code>.)
     *
     * @parameter property="docbkx.variablelistTermBreakAfter"
     */
    public String variablelistTermBreakAfter;

    /**
     * This value will be used when there is no frame attribute on the table.
     * (Original XSL attribute: <code>default.table.frame</code>.)
     *
     * @parameter property="docbkx.defaultTableFrame"
     */
    public String defaultTableFrame;

    /**
     * set the name of the index file.
     * (Original XSL attribute: <code>htmlhelp.hhk</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhk"
     */
    public String htmlhelpHhk;

    /**
     * 
     * (Original XSL attribute: <code>autotoc.label.in.hyperlink</code>.)
     *
     * @parameter property="docbkx.autotocLabelInHyperlink"
     */
    public String autotocLabelInHyperlink;

    /**
     * Change this parameter if you want different name of project file than htmlhelp.
     * (Original XSL attribute: <code>htmlhelp.hhp</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhp"
     */
    public String htmlhelpHhp;

    /**
     * 
     * (Original XSL attribute: <code>arg.rep.norepeat.str</code>.)
     *
     * @parameter property="docbkx.argRepNorepeatStr"
     */
    public String argRepNorepeatStr;

    /**
     * Set the name of the TOC file.
     * (Original XSL attribute: <code>htmlhelp.hhc</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhc"
     */
    public String htmlhelpHhc;

    /**
     * The stylesheets can use either an image of the numbers one to ten, or the single Unicode character which represents the numeral, in white on a black background.
     * (Original XSL attribute: <code>callout.unicode</code>.)
     *
     * @parameter property="docbkx.calloutUnicode"
     */
    public String calloutUnicode;

    /**
     * 
     * (Original XSL attribute: <code>epub.include.metadata.dc.elements</code>.)
     *
     * @parameter property="docbkx.epubIncludeMetadataDcElements"
     */
    public String epubIncludeMetadataDcElements;

    /**
     * 
     * (Original XSL attribute: <code>html.longdesc</code>.)
     *
     * @parameter property="docbkx.htmlLongdesc"
     */
    public String htmlLongdesc;

    /**
     * 
     * (Original XSL attribute: <code>bibliography.numbered</code>.)
     *
     * @parameter property="docbkx.bibliographyNumbered"
     */
    public String bibliographyNumbered;

    /**
     * Specifies the filename of the alias file (used for context-sensitive help).
     * (Original XSL attribute: <code>htmlhelp.alias.file</code>.)
     *
     * @parameter property="docbkx.htmlhelpAliasFile"
     */
    public String htmlhelpAliasFile;

    /**
     * 
     * (Original XSL attribute: <code>html.head.legalnotice.link.types</code>.)
     *
     * @parameter property="docbkx.htmlHeadLegalnoticeLinkTypes"
     */
    public String htmlHeadLegalnoticeLinkTypes;

    /**
     * Set this to true to include chapter and section numbers into ToC in the left panel.
     * (Original XSL attribute: <code>htmlhelp.autolabel</code>.)
     *
     * @parameter property="docbkx.htmlhelpAutolabel"
     */
    public String htmlhelpAutolabel;

    /**
     * The table columns extension function adjusts the widths of table columns in the HTML result to more accurately reflect the specifications in the CALS table.
     * (Original XSL attribute: <code>tablecolumns.extension</code>.)
     *
     * @parameter property="docbkx.tablecolumnsExtension"
     */
    public String tablecolumnsExtension;

    /**
     * 
     * (Original XSL attribute: <code>index.on.role</code>.)
     *
     * @parameter property="docbkx.indexOnRole"
     */
    public String indexOnRole;

    /**
     * If true, year ranges that span a single year will be printed in range notation (1998-1999) instead of discrete notation (1998, 1999).
     * (Original XSL attribute: <code>make.single.year.ranges</code>.)
     *
     * @parameter property="docbkx.makeSingleYearRanges"
     */
    public String makeSingleYearRanges;

    /**
     * Set to true to include the Zoom button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.zoom</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonZoom"
     */
    public String htmlhelpButtonZoom;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.arch</code>.)
     *
     * @parameter property="docbkx.profileArch"
     */
    public String profileArch;

    /**
     * Specifies the border style of table frames.
     * (Original XSL attribute: <code>table.frame.border.style</code>.)
     *
     * @parameter property="docbkx.tableFrameBorderStyle"
     */
    public String tableFrameBorderStyle;

    /**
     * 
     * (Original XSL attribute: <code>callout.graphics.number.limit</code>.)
     *
     * @parameter property="docbkx.calloutGraphicsNumberLimit"
     */
    public String calloutGraphicsNumberLimit;

    /**
     * Sets the filename extension to use on callout graphics.
     * (Original XSL attribute: <code>callout.graphics.extension</code>.)
     *
     * @parameter property="docbkx.calloutGraphicsExtension"
     */
    public String calloutGraphicsExtension;

    /**
     * In order to convert CALS column widths into HTML column widths, it is sometimes necessary to have an absolute table width to use for conversion of mixed absolute and relative widths.
     * (Original XSL attribute: <code>nominal.table.width</code>.)
     *
     * @parameter property="docbkx.nominalTableWidth"
     */
    public String nominalTableWidth;

    /**
     * This parameter specifies the value of the indent specification for generated pages.
     * (Original XSL attribute: <code>chunker.output.indent</code>.)
     *
     * @parameter property="docbkx.chunkerOutputIndent"
     */
    public String chunkerOutputIndent;

    /**
     * Set the name of map file.
     * (Original XSL attribute: <code>htmlhelp.map.file</code>.)
     *
     * @parameter property="docbkx.htmlhelpMapFile"
     */
    public String htmlhelpMapFile;

    /**
     * 
     * (Original XSL attribute: <code>generate.revhistory.link</code>.)
     *
     * @parameter property="docbkx.generateRevhistoryLink"
     */
    public String generateRevhistoryLink;

    /**
     * 
     * (Original XSL attribute: <code>process.source.toc</code>.)
     *
     * @parameter property="docbkx.processSourceToc"
     */
    public String processSourceToc;

    /**
     * If true, extensions may be used.
     * (Original XSL attribute: <code>use.extensions</code>.)
     *
     * @parameter property="docbkx.useExtensions"
     */
    public String useExtensions;

    /**
     * 
     * (Original XSL attribute: <code>get</code>.)
     *
     * @parameter property="docbkx.get"
     */
    public String get;

    /**
     * If you want Options button shown on toolbar, turn this parameter to 1.
     * (Original XSL attribute: <code>htmlhelp.button.options</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonOptions"
     */
    public String htmlhelpButtonOptions;

    /**
     * If true, ToC and LoT (List of Examples, List of Figures, etc.
     * (Original XSL attribute: <code>chunk.tocs.and.lots</code>.)
     *
     * @parameter property="docbkx.chunkTocsAndLots"
     */
    public String chunkTocsAndLots;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.revisionflag</code>.)
     *
     * @parameter property="docbkx.profileRevisionflag"
     */
    public String profileRevisionflag;

    /**
     * If you want to include some additional parameters into project file, store appropriate part of project file into this parameter.
     * (Original XSL attribute: <code>htmlhelp.hhp.tail</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhpTail"
     */
    public String htmlhelpHhpTail;

    /**
     * 
     * (Original XSL attribute: <code>refclass.suppress</code>.)
     *
     * @parameter property="docbkx.refclassSuppress"
     */
    public String refclassSuppress;

    /**
     * 
     * (Original XSL attribute: <code>epub.ncx.toc.id</code>.)
     *
     * @parameter property="docbkx.epubNcxTocId"
     */
    public String epubNcxTocId;

    /**
     * 
     * (Original XSL attribute: <code>glossterm.auto.link</code>.)
     *
     * @parameter property="docbkx.glosstermAutoLink"
     */
    public String glosstermAutoLink;

    /**
     * 
     * (Original XSL attribute: <code>arg.or.sep</code>.)
     *
     * @parameter property="docbkx.argOrSep"
     */
    public String argOrSep;

    /**
     * Content of this parameter is placed at the end of [WINDOWS] section of project file.
     * (Original XSL attribute: <code>htmlhelp.hhp.windows</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhpWindows"
     */
    public String htmlhelpHhpWindows;

    /**
     * If you want Stop button shown on toolbar, turn this parameter to 1.
     * (Original XSL attribute: <code>htmlhelp.button.stop</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonStop"
     */
    public String htmlhelpButtonStop;

    /**
     * If zero (the default), the XSL processor emits a message naming each separate chunk filename as it is being output.
     * (Original XSL attribute: <code>chunk.quietly</code>.)
     *
     * @parameter property="docbkx.chunkQuietly"
     */
    public String chunkQuietly;

    /**
     * 
     * (Original XSL attribute: <code>glossentry.show.acronym</code>.)
     *
     * @parameter property="docbkx.glossentryShowAcronym"
     */
    public String glossentryShowAcronym;

    /**
     * Sets the path, probably relative to the directory where the HTML files are created, to the navigational graphics used in the headers and footers of chunked HTML.
     * (Original XSL attribute: <code>navig.graphics.path</code>.)
     *
     * @parameter property="docbkx.navigGraphicsPath"
     */
    public String navigGraphicsPath;

    /**
     * If true, comments will be displayed, otherwise they are suppressed.
     * (Original XSL attribute: <code>show.comments</code>.)
     *
     * @parameter property="docbkx.showComments"
     */
    public String showComments;

    /**
     * Set the color of table cell borders.
     * (Original XSL attribute: <code>table.cell.border.color</code>.)
     *
     * @parameter property="docbkx.tableCellBorderColor"
     */
    public String tableCellBorderColor;

    /**
     * 
     * (Original XSL attribute: <code>epub.cover.image.id</code>.)
     *
     * @parameter property="docbkx.epubCoverImageId"
     */
    public String epubCoverImageId;

    /**
     * This parameter specifies the public identifier that should be used by the chunking stylesheet in the document type declaration of chunked pages.
     * (Original XSL attribute: <code>chunker.output.doctype-public</code>.)
     *
     * @parameter property="docbkx.chunkerOutputDoctypePublic"
     */
    public String chunkerOutputDoctypePublic;

    /**
     * If you want to include chapter and section numbers into ToC in the left panel, set this parameter to 1.
     * (Original XSL attribute: <code>eclipse.autolabel</code>.)
     *
     * @parameter property="docbkx.eclipseAutolabel"
     */
    public String eclipseAutolabel;

    /**
     * 
     * (Original XSL attribute: <code>entry.propagates.style</code>.)
     *
     * @parameter property="docbkx.entryPropagatesStyle"
     */
    public String entryPropagatesStyle;

    /**
     * 
     * (Original XSL attribute: <code>generate.manifest</code>.)
     *
     * @parameter property="docbkx.generateManifest"
     */
    public String generateManifest;

    /**
     * Eclipse Help plugin provider name.
     * (Original XSL attribute: <code>eclipse.plugin.provider</code>.)
     *
     * @parameter property="docbkx.eclipsePluginProvider"
     */
    public String eclipsePluginProvider;

    /**
     * 
     * (Original XSL attribute: <code>default.table.width</code>.)
     *
     * @parameter property="docbkx.defaultTableWidth"
     */
    public String defaultTableWidth;

    /**
     * 
     * (Original XSL attribute: <code>funcsynopsis.style</code>.)
     *
     * @parameter property="docbkx.funcsynopsisStyle"
     */
    public String funcsynopsisStyle;

    /**
     * Set to true to generate a binary TOC.
     * (Original XSL attribute: <code>htmlhelp.hhc.binary</code>.)
     *
     * @parameter property="docbkx.htmlhelpHhcBinary"
     */
    public String htmlhelpHhcBinary;

    /**
     * 
     * (Original XSL attribute: <code>generate.meta.abstract</code>.)
     *
     * @parameter property="docbkx.generateMetaAbstract"
     */
    public String generateMetaAbstract;

    /**
     * 
     * (Original XSL attribute: <code>ncx.namespace</code>.)
     *
     * @parameter property="docbkx.ncxNamespace"
     */
    public String ncxNamespace;

    /**
     * 
     * (Original XSL attribute: <code>epub.package.id.prefix</code>.)
     *
     * @parameter property="docbkx.epubPackageIdPrefix"
     */
    public String epubPackageIdPrefix;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.condition</code>.)
     *
     * @parameter property="docbkx.profileCondition"
     */
    public String profileCondition;

    /**
     * 
     * (Original XSL attribute: <code>generate.section.toc.level</code>.)
     *
     * @parameter property="docbkx.generateSectionTocLevel"
     */
    public String generateSectionTocLevel;

    /**
     * 
     * (Original XSL attribute: <code>manual.toc</code>.)
     *
     * @parameter property="docbkx.manualToc"
     */
    public String manualToc;

    /**
     * The value of this parameter specifies profiles which should be included in the output.
     * (Original XSL attribute: <code>profile.role</code>.)
     *
     * @parameter property="docbkx.profileRole"
     */
    public String profileRole;

    /**
     * 
     * (Original XSL attribute: <code>callout.unicode.start.character</code>.)
     *
     * @parameter property="docbkx.calloutUnicodeStartCharacter"
     */
    public String calloutUnicodeStartCharacter;

    /**
     * 
     * (Original XSL attribute: <code>epub.ncx.filename</code>.)
     *
     * @parameter property="docbkx.epubNcxFilename"
     */
    public String epubNcxFilename;

    /**
     * 
     * (Original XSL attribute: <code>inherit.keywords</code>.)
     *
     * @parameter property="docbkx.inheritKeywords"
     */
    public String inheritKeywords;

    /**
     * If true, then the exsl:node-set() function is available to be used in the stylesheet.
     * (Original XSL attribute: <code>exsl.node.set.available</code>.)
     *
     * @parameter property="docbkx.exslNodeSetAvailable"
     */
    public String exslNodeSetAvailable;

    /**
     * 
     * (Original XSL attribute: <code>formal.object.break.after</code>.)
     *
     * @parameter property="docbkx.formalObjectBreakAfter"
     */
    public String formalObjectBreakAfter;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.req.close.str</code>.)
     *
     * @parameter property="docbkx.argChoiceReqCloseStr"
     */
    public String argChoiceReqCloseStr;

    /**
     * 
     * (Original XSL attribute: <code>id.warnings</code>.)
     *
     * @parameter property="docbkx.idWarnings"
     */
    public String idWarnings;

    /**
     * The textinsert extension element inserts the contents of       a file into the result tree (as text).
     * (Original XSL attribute: <code>textinsert.extension</code>.)
     *
     * @parameter property="docbkx.textinsertExtension"
     */
    public String textinsertExtension;

    /**
     * 
     * (Original XSL attribute: <code>arg.rep.repeat.str</code>.)
     *
     * @parameter property="docbkx.argRepRepeatStr"
     */
    public String argRepRepeatStr;

    /**
     * 
     * (Original XSL attribute: <code>epub.ncx.pathname</code>.)
     *
     * @parameter property="docbkx.epubNcxPathname"
     */
    public String epubNcxPathname;

    /**
     * To include chapter and section numbers the table of contents pane, set this parameter to 1.
     * (Original XSL attribute: <code>webhelp.autolabel</code>.)
     *
     * @parameter property="docbkx.webhelpAutolabel"
     */
    public String webhelpAutolabel;

    /**
     * Set to true to include the Stop button  on the toolbar.
     * (Original XSL attribute: <code>htmlhelp.button.refresh</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonRefresh"
     */
    public String htmlhelpButtonRefresh;

    /**
     * The HTML Help Compiler is not UTF-8 aware, so you should always use an appropriate single-byte encoding here.
     * (Original XSL attribute: <code>htmlhelp.encoding</code>.)
     *
     * @parameter property="docbkx.htmlhelpEncoding"
     */
    public String htmlhelpEncoding;

    /**
     * 
     * (Original XSL attribute: <code>arg.choice.opt.close.str</code>.)
     *
     * @parameter property="docbkx.argChoiceOptCloseStr"
     */
    public String argChoiceOptCloseStr;

    /**
     * Separator character used for compound profile values.
     * (Original XSL attribute: <code>profile.separator</code>.)
     *
     * @parameter property="docbkx.profileSeparator"
     */
    public String profileSeparator;

    /**
     * 
     * (Original XSL attribute: <code>epub.vocabulary.profile.package</code>.)
     *
     * @parameter property="docbkx.epubVocabularyProfilePackage"
     */
    public String epubVocabularyProfilePackage;

    /**
     * This parameter specifies the encoding to be used in files generated by the chunking stylesheet.
     * (Original XSL attribute: <code>chunker.output.encoding</code>.)
     *
     * @parameter property="docbkx.chunkerOutputEncoding"
     */
    public String chunkerOutputEncoding;

    /**
     * 
     * (Original XSL attribute: <code>htmlhelp.button.hideshow</code>.)
     *
     * @parameter property="docbkx.htmlhelpButtonHideshow"
     */
    public String htmlhelpButtonHideshow;

    /**
     * 
     * (Original XSL attribute: <code>biblioentry.item.separator</code>.)
     *
     * @parameter property="docbkx.biblioentryItemSeparator"
     */
    public String biblioentryItemSeparator;

    /**
     * 
     * (Original XSL attribute: <code>author.othername.in.middle</code>.)
     *
     * @parameter property="docbkx.authorOthernameInMiddle"
     */
    public String authorOthernameInMiddle;

    /**
     * To support stemming in the client-side webhelp stemmer, you must provide the language code.
     * (Original XSL attribute: <code>webhelp.indexer.language</code>.)
     *
     * @parameter property="docbkx.webhelpIndexerLanguage"
     */
    public String webhelpIndexerLanguage;

    /**
     * If non-zero, specifies the thickness of borders on table cells.
     * (Original XSL attribute: <code>table.cell.border.thickness</code>.)
     *
     * @parameter property="docbkx.tableCellBorderThickness"
     */
    public String tableCellBorderThickness;

    /**
     * 
     * (Original XSL attribute: <code>variablelist.term.separator</code>.)
     *
     * @parameter property="docbkx.variablelistTermSeparator"
     */
    public String variablelistTermSeparator;

    /**
     * 
     * (Original XSL attribute: <code>funcsynopsis.decoration</code>.)
     *
     * @parameter property="docbkx.funcsynopsisDecoration"
     */
    public String funcsynopsisDecoration;

    /**
     * Set to true if you insert images into your documents as external binary entities or if you are using absolute image paths.
     * (Original XSL attribute: <code>htmlhelp.enumerate.images</code>.)
     *
     * @parameter property="docbkx.htmlhelpEnumerateImages"
     */
    public String htmlhelpEnumerateImages;

    /**
     * By default, webhelp creates a common directory containing resources such as JavaScript files, css, common images, etc.
     * (Original XSL attribute: <code>webhelp.common.dir</code>.)
     *
     * @parameter property="docbkx.webhelpCommonDir"
     */
    public String webhelpCommonDir;

    /**
     * The separator is inserted between line numbers and lines in the verbatim environment.
     * (Original XSL attribute: <code>linenumbering.separator</code>.)
     *
     * @parameter property="docbkx.linenumberingSeparator"
     */
    public String linenumberingSeparator;

    /**
     * If true, then the glossentry elements within a glossary, glossdiv, or glosslist are sorted on the glossterm, using the current lang setting.
     * (Original XSL attribute: <code>glossary.sort</code>.)
     *
     * @parameter property="docbkx.glossarySort"
     */
    public String glossarySort;

    /**
     * This parameter permits you to override the text to insert between the end of an index term and its list of page references.
     * (Original XSL attribute: <code>index.term.separator</code>.)
     *
     * @parameter property="docbkx.indexTermSeparator"
     */
    public String indexTermSeparator;

    /**
     * 
     * (Original XSL attribute: <code>epub.include.ncx</code>.)
     *
     * @parameter property="docbkx.epubIncludeNcx"
     */
    public String epubIncludeNcx;

    /**
     * Selects the direction in which a float should be placed.
     * (Original XSL attribute: <code>default.float.class</code>.)
     *
     * @parameter property="docbkx.defaultFloatClass"
     */
    public String defaultFloatClass;

    /**
     * 
     * (Original XSL attribute: <code>insert.olink.pdf.frag</code>.)
     *
     * @parameter property="docbkx.insertOlinkPdfFrag"
     */
    public String insertOlinkPdfFrag;

    /**
     * This image is used inline to identify the location of annotations.
     * (Original XSL attribute: <code>annotation.graphic.open</code>.)
     *
     * @parameter property="docbkx.annotationGraphicOpen"
     */
    public String annotationGraphicOpen;

    /**
     * This parameter permits you to override the text to insert between page references in a formatted index entry.
     * (Original XSL attribute: <code>index.number.separator</code>.)
     *
     * @parameter property="docbkx.indexNumberSeparator"
     */
    public String indexNumberSeparator;

    protected void configure(Transformer transformer) {
        getLog().debug("Configure the transformer.");
        if (preferInternalOlink != null) {
            transformer.setParameter("prefer.internal.olink",
                convertBooleanToXsltParam(preferInternalOlink));
        }
        if (htmlhelpHhcWidth != null) {
            transformer.setParameter("htmlhelp.hhc.width",
                convertStringToXsltParam(htmlhelpHhcWidth));
        }
        if (tocMaxDepth != null) {
            transformer.setParameter("toc.max.depth",
                convertStringToXsltParam(tocMaxDepth));
        }
        if (navigGraphicsExtension != null) {
            transformer.setParameter("navig.graphics.extension",
                convertStringToXsltParam(navigGraphicsExtension));
        }
        if (calloutGraphics != null) {
            transformer.setParameter("callout.graphics",
                convertBooleanToXsltParam(calloutGraphics));
        }
        if (glossaryCollection != null) {
            transformer.setParameter("glossary.collection",
                convertStringToXsltParam(glossaryCollection));
        }
        if (htmlhelpShowMenu != null) {
            transformer.setParameter("htmlhelp.show.menu",
                convertBooleanToXsltParam(htmlhelpShowMenu));
        }
        if (xrefWithNumberAndTitle != null) {
            transformer.setParameter("xref.with.number.and.title",
                convertBooleanToXsltParam(xrefWithNumberAndTitle));
        }
        if (refentryXrefManvolnum != null) {
            transformer.setParameter("refentry.xref.manvolnum",
                convertBooleanToXsltParam(refentryXrefManvolnum));
        }
        if (webhelpDefaultTopic != null) {
            transformer.setParameter("webhelp.default.topic",
                convertStringToXsltParam(webhelpDefaultTopic));
        }
        if (epubIncludeGuide != null) {
            transformer.setParameter("epub.include.guide",
                convertStringToXsltParam(epubIncludeGuide));
        }
        if (useRoleAsXrefstyle != null) {
            transformer.setParameter("use.role.as.xrefstyle",
                convertBooleanToXsltParam(useRoleAsXrefstyle));
        }
        if (writingMode != null) {
            transformer.setParameter("writing.mode",
                convertStringToXsltParam(writingMode));
        }
        if (indexRangeSeparator != null) {
            transformer.setParameter("index.range.separator",
                convertStringToXsltParam(indexRangeSeparator));
        }
        if (biblioentryAltPrimarySeps != null) {
            transformer.setParameter("biblioentry.alt.primary.seps",
                convertStringToXsltParam(biblioentryAltPrimarySeps));
        }
        if (htmlhelpButtonLocate != null) {
            transformer.setParameter("htmlhelp.button.locate",
                convertBooleanToXsltParam(htmlhelpButtonLocate));
        }
        if (epubMetaIdentifierId != null) {
            transformer.setParameter("epub.meta.identifier.id",
                convertStringToXsltParam(epubMetaIdentifierId));
        }
        if (bibliographyStyle != null) {
            transformer.setParameter("bibliography.style",
                convertStringToXsltParam(bibliographyStyle));
        }
        if (partAutolabel != null) {
            transformer.setParameter("part.autolabel",
                convertStringToXsltParam(partAutolabel));
        }
        if (chunkSectionDepth != null) {
            transformer.setParameter("chunk.section.depth",
                convertStringToXsltParam(chunkSectionDepth));
        }
        if (profileAttribute != null) {
            transformer.setParameter("profile.attribute",
                convertStringToXsltParam(profileAttribute));
        }
        if (ebnfStatementTerminator != null) {
            transformer.setParameter("ebnf.statement.terminator",
                convertStringToXsltParam(ebnfStatementTerminator));
        }
        if (htmlhelpShowFavorities != null) {
            transformer.setParameter("htmlhelp.show.favorities",
                convertBooleanToXsltParam(htmlhelpShowFavorities));
        }
        if (profileUserlevel != null) {
            transformer.setParameter("profile.userlevel",
                convertStringToXsltParam(profileUserlevel));
        }
        if (calloutUnicodeNumberLimit != null) {
            transformer.setParameter("callout.unicode.number.limit",
                convertStringToXsltParam(calloutUnicodeNumberLimit));
        }
        if (onechunk != null) {
            transformer.setParameter("onechunk",
                convertStringToXsltParam(onechunk));
        }
        if (linenumberingWidth != null) {
            transformer.setParameter("linenumbering.width",
                convertStringToXsltParam(linenumberingWidth));
        }
        if (stylesheetResultType != null) {
            transformer.setParameter("stylesheet.result.type",
                convertStringToXsltParam(stylesheetResultType));
        }
        if (chunkAppend != null) {
            transformer.setParameter("chunk.append",
                convertStringToXsltParam(chunkAppend));
        }
        if (epubIncludeMetadataDcterms != null) {
            transformer.setParameter("epub.include.metadata.dcterms",
                convertStringToXsltParam(epubIncludeMetadataDcterms));
        }
        if (indexPreferTitleabbrev != null) {
            transformer.setParameter("index.prefer.titleabbrev",
                convertBooleanToXsltParam(indexPreferTitleabbrev));
        }
        if (admonGraphicsExtension != null) {
            transformer.setParameter("admon.graphics.extension",
                convertStringToXsltParam(admonGraphicsExtension));
        }
        if (graphicsizeUseImgSrcPath != null) {
            transformer.setParameter("graphicsize.use.img.src.path",
                convertBooleanToXsltParam(graphicsizeUseImgSrcPath));
        }
        if (htmlhelpButtonHomeUrl != null) {
            transformer.setParameter("htmlhelp.button.home.url",
                convertStringToXsltParam(htmlhelpButtonHomeUrl));
        }
        if (navigShowtitles != null) {
            transformer.setParameter("navig.showtitles",
                convertBooleanToXsltParam(navigShowtitles));
        }
        if (menuchoiceMenuSeparator != null) {
            transformer.setParameter("menuchoice.menu.separator",
                convertStringToXsltParam(menuchoiceMenuSeparator));
        }
        if (htmlhelpButtonHome != null) {
            transformer.setParameter("htmlhelp.button.home",
                convertBooleanToXsltParam(htmlhelpButtonHome));
        }
        if (profileStatus != null) {
            transformer.setParameter("profile.status",
                convertStringToXsltParam(profileStatus));
        }
        if (emptyLocalL10nXml != null) {
            transformer.setParameter("empty.local.l10n.xml",
                convertStringToXsltParam(emptyLocalL10nXml));
        }
        if (argRepDefStr != null) {
            transformer.setParameter("arg.rep.def.str",
                convertStringToXsltParam(argRepDefStr));
        }
        if (htmlhelpButtonBack != null) {
            transformer.setParameter("htmlhelp.button.back",
                convertBooleanToXsltParam(htmlhelpButtonBack));
        }
        if (epubDcCreatorId != null) {
            transformer.setParameter("epub.dc.creator.id",
                convertStringToXsltParam(epubDcCreatorId));
        }
        if (othercreditLikeAuthorEnabled != null) {
            transformer.setParameter("othercredit.like.author.enabled",
                convertBooleanToXsltParam(othercreditLikeAuthorEnabled));
        }
        if (linkToSelfForMediaobject != null) {
            transformer.setParameter("link.to.self.for.mediaobject",
                convertBooleanToXsltParam(linkToSelfForMediaobject));
        }
        if (linenumberingEveryNth != null) {
            transformer.setParameter("linenumbering.everyNth",
                convertStringToXsltParam(linenumberingEveryNth));
        }
        if (qandaInheritNumeration != null) {
            transformer.setParameter("qanda.inherit.numeration",
                convertBooleanToXsltParam(qandaInheritNumeration));
        }
        if (olinkDebug != null) {
            transformer.setParameter("olink.debug",
                convertBooleanToXsltParam(olinkDebug));
        }
        if (tableFrameBorderColor != null) {
            transformer.setParameter("table.frame.border.color",
                convertStringToXsltParam(tableFrameBorderColor));
        }
        if (tableFrameBorderThickness != null) {
            transformer.setParameter("table.frame.border.thickness",
                convertStringToXsltParam(tableFrameBorderThickness));
        }
        if (generateIdAttributes != null) {
            transformer.setParameter("generate.id.attributes",
                convertBooleanToXsltParam(generateIdAttributes));
        }
        if (tocEntryDefaultText != null) {
            transformer.setParameter("toc.entry.default.text",
                convertStringToXsltParam(tocEntryDefaultText));
        }
        if (saxonCharacterRepresentation != null) {
            transformer.setParameter("saxon.character.representation",
                convertStringToXsltParam(saxonCharacterRepresentation));
        }
        if (docbookCssLink != null) {
            transformer.setParameter("docbook.css.link",
                convertBooleanToXsltParam(docbookCssLink));
        }
        if (htmlhelpEnhancedDecompilation != null) {
            transformer.setParameter("htmlhelp.enhanced.decompilation",
                convertBooleanToXsltParam(htmlhelpEnhancedDecompilation));
        }
        if (emphasisPropagatesStyle != null) {
            transformer.setParameter("emphasis.propagates.style",
                convertBooleanToXsltParam(emphasisPropagatesStyle));
        }
        if (formalTitlePlacement != null) {
            transformer.setParameter("formal.title.placement",
                convertStringToXsltParam(formalTitlePlacement));
        }
        if (l10nLangValueRfcCompliant != null) {
            transformer.setParameter("l10n.lang.value.rfc.compliant",
                convertBooleanToXsltParam(l10nLangValueRfcCompliant));
        }
        if (editedbyEnabled != null) {
            transformer.setParameter("editedby.enabled",
                convertBooleanToXsltParam(editedbyEnabled));
        }
        if (htmlStylesheet != null) {
            transformer.setParameter("html.stylesheet",
                convertStringToXsltParam(htmlStylesheet));
        }
        if (olinkLangFallbackSequence != null) {
            transformer.setParameter("olink.lang.fallback.sequence",
                convertStringToXsltParam(olinkLangFallbackSequence));
        }
        if (epubPackagePathname != null) {
            transformer.setParameter("epub.package.pathname",
                convertStringToXsltParam(epubPackagePathname));
        }
        if (admonStyle != null) {
            transformer.setParameter("admon.style",
                convertStringToXsltParam(admonStyle));
        }
        if (htmlhelpHhcShowRoot != null) {
            transformer.setParameter("htmlhelp.hhc.show.root",
                convertBooleanToXsltParam(htmlhelpHhcShowRoot));
        }
        if (epubContainerPathname != null) {
            transformer.setParameter("epub.container.pathname",
                convertStringToXsltParam(epubContainerPathname));
        }
        if (webhelpStartFilename != null) {
            transformer.setParameter("webhelp.start.filename",
                convertStringToXsltParam(webhelpStartFilename));
        }
        if (useEmbedForSvg != null) {
            transformer.setParameter("use.embed.for.svg",
                convertBooleanToXsltParam(useEmbedForSvg));
        }
        if (footnoteNumberSymbols != null) {
            transformer.setParameter("footnote.number.symbols",
                convertStringToXsltParam(footnoteNumberSymbols));
        }
        if (insertXrefPageNumberPara != null) {
            transformer.setParameter("insert.xref.page.number.para",
                convertStringToXsltParam(insertXrefPageNumberPara));
        }
        if (epubDcLanguageId != null) {
            transformer.setParameter("epub.dc.language.id",
                convertStringToXsltParam(epubDcLanguageId));
        }
        if (chunkFirstSections != null) {
            transformer.setParameter("chunk.first.sections",
                convertBooleanToXsltParam(chunkFirstSections));
        }
        if (runinheadTitleEndPunct != null) {
            transformer.setParameter("runinhead.title.end.punct",
                convertStringToXsltParam(runinheadTitleEndPunct));
        }
        if (suppressHeaderNavigation != null) {
            transformer.setParameter("suppress.header.navigation",
                convertBooleanToXsltParam(suppressHeaderNavigation));
        }
        if (epubMetaLanguageId != null) {
            transformer.setParameter("epub.meta.language.id",
                convertStringToXsltParam(epubMetaLanguageId));
        }
        if (suppressNavigation != null) {
            transformer.setParameter("suppress.navigation",
                convertBooleanToXsltParam(suppressNavigation));
        }
        if (punctHonorific != null) {
            transformer.setParameter("punct.honorific",
                convertStringToXsltParam(punctHonorific));
        }
        if (epubCoverLinear != null) {
            transformer.setParameter("epub.cover.linear",
                convertStringToXsltParam(epubCoverLinear));
        }
        if (ebnfTableBgcolor != null) {
            transformer.setParameter("ebnf.table.bgcolor",
                convertStringToXsltParam(ebnfTableBgcolor));
        }
        if (annotationCss != null) {
            transformer.setParameter("annotation.css",
                convertStringToXsltParam(annotationCss));
        }
        if (kindleExtensions != null) {
            transformer.setParameter("kindle.extensions",
                convertStringToXsltParam(kindleExtensions));
        }
        if (chunkerOutputCdataSectionElements != null) {
            transformer.setParameter("chunker.output.cdata-section-elements",
                convertStringToXsltParam(chunkerOutputCdataSectionElements));
        }
        if (imgSrcPath != null) {
            transformer.setParameter("img.src.path",
                convertStringToXsltParam(imgSrcPath));
        }
        if (olinkBaseUri != null) {
            transformer.setParameter("olink.base.uri",
                convertStringToXsltParam(olinkBaseUri));
        }
        if (epubNcxDepth != null) {
            transformer.setParameter("epub.ncx.depth",
                convertStringToXsltParam(epubNcxDepth));
        }
        if (makeYearRanges != null) {
            transformer.setParameter("make.year.ranges",
                convertBooleanToXsltParam(makeYearRanges));
        }
        if (argChoicePlainOpenStr != null) {
            transformer.setParameter("arg.choice.plain.open.str",
                convertStringToXsltParam(argChoicePlainOpenStr));
        }
        if (htmlhelpShowToolbarText != null) {
            transformer.setParameter("htmlhelp.show.toolbar.text",
                convertBooleanToXsltParam(htmlhelpShowToolbarText));
        }
        if (xrefTitlePageSeparator != null) {
            transformer.setParameter("xref.title-page.separator",
                convertStringToXsltParam(xrefTitlePageSeparator));
        }
        if (profileSecurity != null) {
            transformer.setParameter("profile.security",
                convertStringToXsltParam(profileSecurity));
        }
        if (insertOlinkPageNumber != null) {
            transformer.setParameter("insert.olink.page.number",
                convertStringToXsltParam(insertOlinkPageNumber));
        }
        if (webhelpBaseDir != null) {
            transformer.setParameter("webhelp.base.dir",
                convertStringToXsltParam(webhelpBaseDir));
        }
        if (qandaNestedInToc != null) {
            transformer.setParameter("qanda.nested.in.toc",
                convertBooleanToXsltParam(qandaNestedInToc));
        }
        if (profileValue != null) {
            transformer.setParameter("profile.value",
                convertStringToXsltParam(profileValue));
        }
        if (generateConsistentIds != null) {
            transformer.setParameter("generate.consistent.ids",
                convertBooleanToXsltParam(generateConsistentIds));
        }
        if (epubMetaCreatorId != null) {
            transformer.setParameter("epub.meta.creator.id",
                convertStringToXsltParam(epubMetaCreatorId));
        }
        if (htmlScriptType != null) {
            transformer.setParameter("html.script.type",
                convertStringToXsltParam(htmlScriptType));
        }
        if (highlightXslthlConfig != null) {
            transformer.setParameter("highlight.xslthl.config",
                convertStringToXsltParam(highlightXslthlConfig));
        }
        if (epubDcIdentifierId != null) {
            transformer.setParameter("epub.dc.identifier.id",
                convertStringToXsltParam(epubDcIdentifierId));
        }
        if (sectionAutolabelMaxDepth != null) {
            transformer.setParameter("section.autolabel.max.depth",
                convertStringToXsltParam(sectionAutolabelMaxDepth));
        }
        if (eclipsePluginName != null) {
            transformer.setParameter("eclipse.plugin.name",
                convertStringToXsltParam(eclipsePluginName));
        }
        if (makeCleanHtml != null) {
            transformer.setParameter("make.clean.html",
                convertBooleanToXsltParam(makeCleanHtml));
        }
        if (paraPropagatesStyle != null) {
            transformer.setParameter("para.propagates.style",
                convertBooleanToXsltParam(paraPropagatesStyle));
        }
        if (indexLinksToSection != null) {
            transformer.setParameter("index.links.to.section",
                convertBooleanToXsltParam(indexLinksToSection));
        }
        if (componentLabelIncludesPartLabel != null) {
            transformer.setParameter("component.label.includes.part.label",
                convertBooleanToXsltParam(componentLabelIncludesPartLabel));
        }
        if (autotocLabelSeparator != null) {
            transformer.setParameter("autotoc.label.separator",
                convertStringToXsltParam(autotocLabelSeparator));
        }
        if (chunkedFilenamePrefix != null) {
            transformer.setParameter("chunked.filename.prefix",
                convertStringToXsltParam(chunkedFilenamePrefix));
        }
        if (htmlhelpButtonPrev != null) {
            transformer.setParameter("htmlhelp.button.prev",
                convertBooleanToXsltParam(htmlhelpButtonPrev));
        }
        if (htmlAppend != null) {
            transformer.setParameter("html.append",
                convertStringToXsltParam(htmlAppend));
        }
        if (textdataDefaultEncoding != null) {
            transformer.setParameter("textdata.default.encoding",
                convertStringToXsltParam(textdataDefaultEncoding));
        }
        if (calloutGraphicsPath != null) {
            transformer.setParameter("callout.graphics.path",
                convertStringToXsltParam(calloutGraphicsPath));
        }
        if (profileLang != null) {
            transformer.setParameter("profile.lang",
                convertStringToXsltParam(profileLang));
        }
        if (epubEmbeddedFonts != null) {
            transformer.setParameter("epub.embedded.fonts",
                convertStringToXsltParam(epubEmbeddedFonts));
        }
        if (admonGraphics != null) {
            transformer.setParameter("admon.graphics",
                convertBooleanToXsltParam(admonGraphics));
        }
        if (profileOutputformat != null) {
            transformer.setParameter("profile.outputformat",
                convertStringToXsltParam(profileOutputformat));
        }
        if (epubVersion != null) {
            transformer.setParameter("epub.version",
                convertStringToXsltParam(epubVersion));
        }
        if (manifestInBaseDir != null) {
            transformer.setParameter("manifest.in.base.dir",
                convertBooleanToXsltParam(manifestInBaseDir));
        }
        if (tableBordersWithCss != null) {
            transformer.setParameter("table.borders.with.css",
                convertBooleanToXsltParam(tableBordersWithCss));
        }
        if (referenceAutolabel != null) {
            transformer.setParameter("reference.autolabel",
                convertStringToXsltParam(referenceAutolabel));
        }
        if (abstractNotitleEnabled != null) {
            transformer.setParameter("abstract.notitle.enabled",
                convertBooleanToXsltParam(abstractNotitleEnabled));
        }
        if (xrefLabelTitleSeparator != null) {
            transformer.setParameter("xref.label-title.separator",
                convertStringToXsltParam(xrefLabelTitleSeparator));
        }
        if (texMathFile != null) {
            transformer.setParameter("tex.math.file",
                convertStringToXsltParam(texMathFile));
        }
        if (chunkerOutputOmitXmlDeclaration != null) {
            transformer.setParameter("chunker.output.omit-xml-declaration",
                convertStringToXsltParam(chunkerOutputOmitXmlDeclaration));
        }
        if (epubContainerFilename != null) {
            transformer.setParameter("epub.container.filename",
                convertStringToXsltParam(epubContainerFilename));
        }
        if (epubCoverFilename != null) {
            transformer.setParameter("epub.cover.filename",
                convertStringToXsltParam(epubCoverFilename));
        }
        if (ignoreImageScaling != null) {
            transformer.setParameter("ignore.image.scaling",
                convertBooleanToXsltParam(ignoreImageScaling));
        }
        if (collectXrefTargets != null) {
            transformer.setParameter("collect.xref.targets",
                convertStringToXsltParam(collectXrefTargets));
        }
        if (annotationJs != null) {
            transformer.setParameter("annotation.js",
                convertStringToXsltParam(annotationJs));
        }
        if (highlightSource != null) {
            transformer.setParameter("highlight.source",
                convertBooleanToXsltParam(highlightSource));
        }
        if (shadeVerbatim != null) {
            transformer.setParameter("shade.verbatim",
                convertBooleanToXsltParam(shadeVerbatim));
        }
        if (firsttermOnlyLink != null) {
            transformer.setParameter("firstterm.only.link",
                convertBooleanToXsltParam(firsttermOnlyLink));
        }
        if (olinkDoctitle != null) {
            transformer.setParameter("olink.doctitle",
                convertStringToXsltParam(olinkDoctitle));
        }
        if (annotationGraphicClose != null) {
            transformer.setParameter("annotation.graphic.close",
                convertStringToXsltParam(annotationGraphicClose));
        }
        if (epubIncludeOptionalMetadataDcElements != null) {
            transformer.setParameter("epub.include.optional.metadata.dc.elements",
                convertStringToXsltParam(epubIncludeOptionalMetadataDcElements));
        }
        if (htmlhelpButtonJump2Title != null) {
            transformer.setParameter("htmlhelp.button.jump2.title",
                convertStringToXsltParam(htmlhelpButtonJump2Title));
        }
        if (argChoiceDefOpenStr != null) {
            transformer.setParameter("arg.choice.def.open.str",
                convertStringToXsltParam(argChoiceDefOpenStr));
        }
        if (htmlhelpButtonJump2 != null) {
            transformer.setParameter("htmlhelp.button.jump2",
                convertBooleanToXsltParam(htmlhelpButtonJump2));
        }
        if (chunkFast != null) {
            transformer.setParameter("chunk.fast",
                convertStringToXsltParam(chunkFast));
        }
        if (linkMailtoUrl != null) {
            transformer.setParameter("link.mailto.url",
                convertStringToXsltParam(linkMailtoUrl));
        }
        if (htmlhelpButtonJump1 != null) {
            transformer.setParameter("htmlhelp.button.jump1",
                convertBooleanToXsltParam(htmlhelpButtonJump1));
        }
        if (variablelistAsTable != null) {
            transformer.setParameter("variablelist.as.table",
                convertBooleanToXsltParam(variablelistAsTable));
        }
        if (generateIndex != null) {
            transformer.setParameter("generate.index",
                convertBooleanToXsltParam(generateIndex));
        }
        if (htmlhelpTitle != null) {
            transformer.setParameter("htmlhelp.title",
                convertStringToXsltParam(htmlhelpTitle));
        }
        if (useRoleForMediaobject != null) {
            transformer.setParameter("use.role.for.mediaobject",
                convertBooleanToXsltParam(useRoleForMediaobject));
        }
        if (refentrySeparator != null) {
            transformer.setParameter("refentry.separator",
                convertBooleanToXsltParam(refentrySeparator));
        }
        if (ebnfTableBorder != null) {
            transformer.setParameter("ebnf.table.border",
                convertBooleanToXsltParam(ebnfTableBorder));
        }
        if (functionParens != null) {
            transformer.setParameter("function.parens",
                convertBooleanToXsltParam(functionParens));
        }
        if (admonGraphicsPath != null) {
            transformer.setParameter("admon.graphics.path",
                convertStringToXsltParam(admonGraphicsPath));
        }
        if (useLocalOlinkStyle != null) {
            transformer.setParameter("use.local.olink.style",
                convertBooleanToXsltParam(useLocalOlinkStyle));
        }
        if (qandadivAutolabel != null) {
            transformer.setParameter("qandadiv.autolabel",
                convertBooleanToXsltParam(qandadivAutolabel));
        }
        if (epubOebpsDir != null) {
            transformer.setParameter("epub.oebps.dir",
                convertStringToXsltParam(epubOebpsDir));
        }
        if (blurbOnTitlepageEnabled != null) {
            transformer.setParameter("blurb.on.titlepage.enabled",
                convertBooleanToXsltParam(blurbOnTitlepageEnabled));
        }
        if (calloutsExtension != null) {
            transformer.setParameter("callouts.extension",
                convertBooleanToXsltParam(calloutsExtension));
        }
        if (makeGraphicViewport != null) {
            transformer.setParameter("make.graphic.viewport",
                convertBooleanToXsltParam(makeGraphicViewport));
        }
        if (linenumberingExtension != null) {
            transformer.setParameter("linenumbering.extension",
                convertBooleanToXsltParam(linenumberingExtension));
        }
        if (defaultImageWidth != null) {
            transformer.setParameter("default.image.width",
                convertStringToXsltParam(defaultImageWidth));
        }
        if (labelFromPart != null) {
            transformer.setParameter("label.from.part",
                convertBooleanToXsltParam(labelFromPart));
        }
        if (chunkerOutputMethod != null) {
            transformer.setParameter("chunker.output.method",
                convertStringToXsltParam(chunkerOutputMethod));
        }
        if (htmlhelpButtonJump1Title != null) {
            transformer.setParameter("htmlhelp.button.jump1.title",
                convertStringToXsltParam(htmlhelpButtonJump1Title));
        }
        if (nominalImageDepth != null) {
            transformer.setParameter("nominal.image.depth",
                convertStringToXsltParam(nominalImageDepth));
        }
        if (htmlHeadLegalnoticeLinkMultiple != null) {
            transformer.setParameter("html.head.legalnotice.link.multiple",
                convertBooleanToXsltParam(htmlHeadLegalnoticeLinkMultiple));
        }
        if (rootid != null) {
            transformer.setParameter("rootid",
                convertStringToXsltParam(rootid));
        }
        if (footerRule != null) {
            transformer.setParameter("footer.rule",
                convertBooleanToXsltParam(footerRule));
        }
        if (appendixAutolabel != null) {
            transformer.setParameter("appendix.autolabel",
                convertStringToXsltParam(appendixAutolabel));
        }
        if (nominalImageWidth != null) {
            transformer.setParameter("nominal.image.width",
                convertStringToXsltParam(nominalImageWidth));
        }
        if (qandaInToc != null) {
            transformer.setParameter("qanda.in.toc",
                convertBooleanToXsltParam(qandaInToc));
        }
        if (htmlLongdescLink != null) {
            transformer.setParameter("html.longdesc.link",
                convertBooleanToXsltParam(htmlLongdescLink));
        }
        if (profileConformance != null) {
            transformer.setParameter("profile.conformance",
                convertStringToXsltParam(profileConformance));
        }
        if (htmlhelpDefaultTopic != null) {
            transformer.setParameter("htmlhelp.default.topic",
                convertStringToXsltParam(htmlhelpDefaultTopic));
        }
        if (segmentedlistAsTable != null) {
            transformer.setParameter("segmentedlist.as.table",
                convertBooleanToXsltParam(segmentedlistAsTable));
        }
        if (graphicDefaultExtension != null) {
            transformer.setParameter("graphic.default.extension",
                convertStringToXsltParam(graphicDefaultExtension));
        }
        if (htmlhelpHhcSectionDepth != null) {
            transformer.setParameter("htmlhelp.hhc.section.depth",
                convertStringToXsltParam(htmlhelpHhcSectionDepth));
        }
        if (simplesectInToc != null) {
            transformer.setParameter("simplesect.in.toc",
                convertBooleanToXsltParam(simplesectInToc));
        }
        if (tableFootnoteNumberSymbols != null) {
            transformer.setParameter("table.footnote.number.symbols",
                convertStringToXsltParam(tableFootnoteNumberSymbols));
        }
        if (epubMetaTitleId != null) {
            transformer.setParameter("epub.meta.title.id",
                convertStringToXsltParam(epubMetaTitleId));
        }
        if (epubMimetypePathname != null) {
            transformer.setParameter("epub.mimetype.pathname",
                convertStringToXsltParam(epubMimetypePathname));
        }
        if (argChoicePlainCloseStr != null) {
            transformer.setParameter("arg.choice.plain.close.str",
                convertStringToXsltParam(argChoicePlainCloseStr));
        }
        if (docbookCssSource != null) {
            transformer.setParameter("docbook.css.source",
                convertStringToXsltParam(docbookCssSource));
        }
        if (chunkTocsAndLotsHasTitle != null) {
            transformer.setParameter("chunk.tocs.and.lots.has.title",
                convertBooleanToXsltParam(chunkTocsAndLotsHasTitle));
        }
        if (showRevisionflag != null) {
            transformer.setParameter("show.revisionflag",
                convertBooleanToXsltParam(showRevisionflag));
        }
        if (argChoiceDefCloseStr != null) {
            transformer.setParameter("arg.choice.def.close.str",
                convertStringToXsltParam(argChoiceDefCloseStr));
        }
        if (sectionLabelIncludesComponentLabel != null) {
            transformer.setParameter("section.label.includes.component.label",
                convertBooleanToXsltParam(sectionLabelIncludesComponentLabel));
        }
        if (graphicsizeExtension != null) {
            transformer.setParameter("graphicsize.extension",
                convertBooleanToXsltParam(graphicsizeExtension));
        }
        if (manifest != null) {
            transformer.setParameter("manifest",
                convertStringToXsltParam(manifest));
        }
        if (runinheadDefaultTitleEndPunct != null) {
            transformer.setParameter("runinhead.default.title.end.punct",
                convertStringToXsltParam(runinheadDefaultTitleEndPunct));
        }
        if (htmlhelpDisplayProgress != null) {
            transformer.setParameter("htmlhelp.display.progress",
                convertBooleanToXsltParam(htmlhelpDisplayProgress));
        }
        if (htmlhelpForceMapAndAlias != null) {
            transformer.setParameter("htmlhelp.force.map.and.alias",
                convertBooleanToXsltParam(htmlhelpForceMapAndAlias));
        }
        if (htmlBase != null) {
            transformer.setParameter("html.base",
                convertStringToXsltParam(htmlBase));
        }
        if (webhelpIncludeSearchTab != null) {
            transformer.setParameter("webhelp.include.search.tab",
                convertBooleanToXsltParam(webhelpIncludeSearchTab));
        }
        if (htmlhelpChm != null) {
            transformer.setParameter("htmlhelp.chm",
                convertStringToXsltParam(htmlhelpChm));
        }
        if (citerefentryLink != null) {
            transformer.setParameter("citerefentry.link",
                convertBooleanToXsltParam(citerefentryLink));
        }
        if (webhelpTreeCookieId != null) {
            transformer.setParameter("webhelp.tree.cookie.id",
                convertStringToXsltParam(webhelpTreeCookieId));
        }
        if (htmlScript != null) {
            transformer.setParameter("html.script",
                convertStringToXsltParam(htmlScript));
        }
        if (indexMethod != null) {
            transformer.setParameter("index.method",
                convertStringToXsltParam(indexMethod));
        }
        if (indexOnType != null) {
            transformer.setParameter("index.on.type",
                convertBooleanToXsltParam(indexOnType));
        }
        if (epubOutputEpubTypes != null) {
            transformer.setParameter("epub.output.epub.types",
                convertStringToXsltParam(epubOutputEpubTypes));
        }
        if (epubNcxManifestId != null) {
            transformer.setParameter("epub.ncx.manifest.id",
                convertStringToXsltParam(epubNcxManifestId));
        }
        if (epubCoverPathname != null) {
            transformer.setParameter("epub.cover.pathname",
                convertStringToXsltParam(epubCoverPathname));
        }
        if (argChoiceOptOpenStr != null) {
            transformer.setParameter("arg.choice.opt.open.str",
                convertStringToXsltParam(argChoiceOptOpenStr));
        }
        if (profileAudience != null) {
            transformer.setParameter("profile.audience",
                convertStringToXsltParam(profileAudience));
        }
        if (epubNamespace != null) {
            transformer.setParameter("epub.namespace",
                convertStringToXsltParam(epubNamespace));
        }
        if (tocListType != null) {
            transformer.setParameter("toc.list.type",
                convertStringToXsltParam(tocListType));
        }
        if (epubXhtmlMediatype != null) {
            transformer.setParameter("epub.xhtml.mediatype",
                convertStringToXsltParam(epubXhtmlMediatype));
        }
        if (l10nGentextUseXrefLanguage != null) {
            transformer.setParameter("l10n.gentext.use.xref.language",
                convertBooleanToXsltParam(l10nGentextUseXrefLanguage));
        }
        if (generateLegalnoticeLink != null) {
            transformer.setParameter("generate.legalnotice.link",
                convertBooleanToXsltParam(generateLegalnoticeLink));
        }
        if (profileOs != null) {
            transformer.setParameter("profile.os",
                convertStringToXsltParam(profileOs));
        }
        if (tableCellBorderStyle != null) {
            transformer.setParameter("table.cell.border.style",
                convertStringToXsltParam(tableCellBorderStyle));
        }
        if (tableFootnoteNumberFormat != null) {
            transformer.setParameter("table.footnote.number.format",
                convertStringToXsltParam(tableFootnoteNumberFormat));
        }
        if (biblioentryPrimaryCount != null) {
            transformer.setParameter("biblioentry.primary.count",
                convertStringToXsltParam(biblioentryPrimaryCount));
        }
        if (argChoiceReqOpenStr != null) {
            transformer.setParameter("arg.choice.req.open.str",
                convertStringToXsltParam(argChoiceReqOpenStr));
        }
        if (htmlCleanup != null) {
            transformer.setParameter("html.cleanup",
                convertBooleanToXsltParam(htmlCleanup));
        }
        if (htmlhelpShowAdvancedSearch != null) {
            transformer.setParameter("htmlhelp.show.advanced.search",
                convertBooleanToXsltParam(htmlhelpShowAdvancedSearch));
        }
        if (chunkerOutputDoctypeSystem != null) {
            transformer.setParameter("chunker.output.doctype-system",
                convertStringToXsltParam(chunkerOutputDoctypeSystem));
        }
        if (prefaceAutolabel != null) {
            transformer.setParameter("preface.autolabel",
                convertStringToXsltParam(prefaceAutolabel));
        }
        if (texMathInAlt != null) {
            transformer.setParameter("tex.math.in.alt",
                convertStringToXsltParam(texMathInAlt));
        }
        if (generateCssHeader != null) {
            transformer.setParameter("generate.css.header",
                convertBooleanToXsltParam(generateCssHeader));
        }
        if (navigGraphics != null) {
            transformer.setParameter("navig.graphics",
                convertBooleanToXsltParam(navigGraphics));
        }
        if (htmlCellpadding != null) {
            transformer.setParameter("html.cellpadding",
                convertStringToXsltParam(htmlCellpadding));
        }
        if (contribInlineEnabled != null) {
            transformer.setParameter("contrib.inline.enabled",
                convertBooleanToXsltParam(contribInlineEnabled));
        }
        if (currentDocid != null) {
            transformer.setParameter("current.docid",
                convertStringToXsltParam(currentDocid));
        }
        if (keepRelativeImageUris != null) {
            transformer.setParameter("keep.relative.image.uris",
                convertBooleanToXsltParam(keepRelativeImageUris));
        }
        if (localL10nXml != null) {
            transformer.setParameter("local.l10n.xml",
                convertStringToXsltParam(localL10nXml));
        }
        if (opfNamespace != null) {
            transformer.setParameter("opf.namespace",
                convertStringToXsltParam(opfNamespace));
        }
        if (customCssSource != null) {
            transformer.setParameter("custom.css.source",
                convertStringToXsltParam(customCssSource));
        }
        if (tocSectionDepth != null) {
            transformer.setParameter("toc.section.depth",
                convertStringToXsltParam(tocSectionDepth));
        }
        if (draftMode != null) {
            transformer.setParameter("draft.mode",
                convertStringToXsltParam(draftMode));
        }
        if (draftWatermarkImage != null) {
            transformer.setParameter("draft.watermark.image",
                convertStringToXsltParam(draftWatermarkImage));
        }
        if (epubNcxMediatype != null) {
            transformer.setParameter("epub.ncx.mediatype",
                convertStringToXsltParam(epubNcxMediatype));
        }
        if (epubCoverFilenameId != null) {
            transformer.setParameter("epub.cover.filename.id",
                convertStringToXsltParam(epubCoverFilenameId));
        }
        if (componentHeadingLevel != null) {
            transformer.setParameter("component.heading.level",
                convertStringToXsltParam(componentHeadingLevel));
        }
        if (procedureStepNumerationFormats != null) {
            transformer.setParameter("procedure.step.numeration.formats",
                convertStringToXsltParam(procedureStepNumerationFormats));
        }
        if (htmlhelpHhcFoldersInsteadBooks != null) {
            transformer.setParameter("htmlhelp.hhc.folders.instead.books",
                convertBooleanToXsltParam(htmlhelpHhcFoldersInsteadBooks));
        }
        if (bridgeheadInToc != null) {
            transformer.setParameter("bridgehead.in.toc",
                convertBooleanToXsltParam(bridgeheadInToc));
        }
        if (formalProcedures != null) {
            transformer.setParameter("formal.procedures",
                convertBooleanToXsltParam(formalProcedures));
        }
        if (insertXrefPageNumber != null) {
            transformer.setParameter("insert.xref.page.number",
                convertStringToXsltParam(insertXrefPageNumber));
        }
        if (epubHtmlTocId != null) {
            transformer.setParameter("epub.html.toc.id",
                convertStringToXsltParam(epubHtmlTocId));
        }
        if (htmlhelpUseHhk != null) {
            transformer.setParameter("htmlhelp.use.hhk",
                convertBooleanToXsltParam(htmlhelpUseHhk));
        }
        if (pixelsPerInch != null) {
            transformer.setParameter("pixels.per.inch",
                convertStringToXsltParam(pixelsPerInch));
        }
        if (phrasePropagatesStyle != null) {
            transformer.setParameter("phrase.propagates.style",
                convertBooleanToXsltParam(phrasePropagatesStyle));
        }
        if (cssDecoration != null) {
            transformer.setParameter("css.decoration",
                convertBooleanToXsltParam(cssDecoration));
        }
        if (editorProperty != null) {
            transformer.setParameter("editor.property",
                convertStringToXsltParam(editorProperty));
        }
        if (htmlhelpOnly != null) {
            transformer.setParameter("htmlhelp.only",
                convertBooleanToXsltParam(htmlhelpOnly));
        }
        if (headerRule != null) {
            transformer.setParameter("header.rule",
                convertBooleanToXsltParam(headerRule));
        }
        if (ulinkTarget != null) {
            transformer.setParameter("ulink.target",
                convertStringToXsltParam(ulinkTarget));
        }
        if (emailDelimitersEnabled != null) {
            transformer.setParameter("email.delimiters.enabled",
                convertBooleanToXsltParam(emailDelimitersEnabled));
        }
        if (useSvg != null) {
            transformer.setParameter("use.svg",
                convertBooleanToXsltParam(useSvg));
        }
        if (chunkSeparateLots != null) {
            transformer.setParameter("chunk.separate.lots",
                convertBooleanToXsltParam(chunkSeparateLots));
        }
        if (htmlhelpButtonNext != null) {
            transformer.setParameter("htmlhelp.button.next",
                convertBooleanToXsltParam(htmlhelpButtonNext));
        }
        if (xrefLabelPageSeparator != null) {
            transformer.setParameter("xref.label-page.separator",
                convertStringToXsltParam(xrefLabelPageSeparator));
        }
        if (bibliographyCollection != null) {
            transformer.setParameter("bibliography.collection",
                convertStringToXsltParam(bibliographyCollection));
        }
        if (refentryGenerateName != null) {
            transformer.setParameter("refentry.generate.name",
                convertBooleanToXsltParam(refentryGenerateName));
        }
        if (htmlCellspacing != null) {
            transformer.setParameter("html.cellspacing",
                convertStringToXsltParam(htmlCellspacing));
        }
        if (profileRevision != null) {
            transformer.setParameter("profile.revision",
                convertStringToXsltParam(profileRevision));
        }
        if (menuchoiceSeparator != null) {
            transformer.setParameter("menuchoice.separator",
                convertStringToXsltParam(menuchoiceSeparator));
        }
        if (javahelpEncoding != null) {
            transformer.setParameter("javahelp.encoding",
                convertStringToXsltParam(javahelpEncoding));
        }
        if (htmlExt != null) {
            transformer.setParameter("html.ext",
                convertStringToXsltParam(htmlExt));
        }
        if (htmlhelpHhpWindow != null) {
            transformer.setParameter("htmlhelp.hhp.window",
                convertStringToXsltParam(htmlhelpHhpWindow));
        }
        if (ebnfAssignment != null) {
            transformer.setParameter("ebnf.assignment",
                convertStringToXsltParam(ebnfAssignment));
        }
        if (chunkToc != null) {
            transformer.setParameter("chunk.toc",
                convertStringToXsltParam(chunkToc));
        }
        if (targetsFilename != null) {
            transformer.setParameter("targets.filename",
                convertStringToXsltParam(targetsFilename));
        }
        if (qandaDefaultlabel != null) {
            transformer.setParameter("qanda.defaultlabel",
                convertStringToXsltParam(qandaDefaultlabel));
        }
        if (pointsPerEm != null) {
            transformer.setParameter("points.per.em",
                convertStringToXsltParam(pointsPerEm));
        }
        if (suppressFooterNavigation != null) {
            transformer.setParameter("suppress.footer.navigation",
                convertBooleanToXsltParam(suppressFooterNavigation));
        }
        if (admonTextlabel != null) {
            transformer.setParameter("admon.textlabel",
                convertBooleanToXsltParam(admonTextlabel));
        }
        if (makeValidHtml != null) {
            transformer.setParameter("make.valid.html",
                convertBooleanToXsltParam(makeValidHtml));
        }
        if (eclipsePluginId != null) {
            transformer.setParameter("eclipse.plugin.id",
                convertStringToXsltParam(eclipsePluginId));
        }
        if (refentryGenerateTitle != null) {
            transformer.setParameter("refentry.generate.title",
                convertBooleanToXsltParam(refentryGenerateTitle));
        }
        if (processEmptySourceToc != null) {
            transformer.setParameter("process.empty.source.toc",
                convertBooleanToXsltParam(processEmptySourceToc));
        }
        if (l10nGentextLanguage != null) {
            transformer.setParameter("l10n.gentext.language",
                convertStringToXsltParam(l10nGentextLanguage));
        }
        if (htmlhelpButtonPrint != null) {
            transformer.setParameter("htmlhelp.button.print",
                convertBooleanToXsltParam(htmlhelpButtonPrint));
        }
        if (annotationSupport != null) {
            transformer.setParameter("annotation.support",
                convertBooleanToXsltParam(annotationSupport));
        }
        if (highlightDefaultLanguage != null) {
            transformer.setParameter("highlight.default.language",
                convertStringToXsltParam(highlightDefaultLanguage));
        }
        if (chunkerOutputStandalone != null) {
            transformer.setParameter("chunker.output.standalone",
                convertStringToXsltParam(chunkerOutputStandalone));
        }
        if (spacingParas != null) {
            transformer.setParameter("spacing.paras",
                convertBooleanToXsltParam(spacingParas));
        }
        if (htmlhelpWindowGeometry != null) {
            transformer.setParameter("htmlhelp.window.geometry",
                convertStringToXsltParam(htmlhelpWindowGeometry));
        }
        if (htmlhelpOutput != null) {
            transformer.setParameter("htmlhelp.output",
                convertStringToXsltParam(htmlhelpOutput));
        }
        if (htmlStylesheetType != null) {
            transformer.setParameter("html.stylesheet.type",
                convertStringToXsltParam(htmlStylesheetType));
        }
        if (epubVocabularyProfileContent != null) {
            transformer.setParameter("epub.vocabulary.profile.content",
                convertStringToXsltParam(epubVocabularyProfileContent));
        }
        if (htmlExtraHeadLinks != null) {
            transformer.setParameter("html.extra.head.links",
                convertBooleanToXsltParam(htmlExtraHeadLinks));
        }
        if (targetDatabaseDocument != null) {
            transformer.setParameter("target.database.document",
                convertStringToXsltParam(targetDatabaseDocument));
        }
        if (epubAutolabel != null) {
            transformer.setParameter("epub.autolabel",
                convertBooleanToXsltParam(epubAutolabel));
        }
        if (htmlhelpButtonForward != null) {
            transformer.setParameter("htmlhelp.button.forward",
                convertBooleanToXsltParam(htmlhelpButtonForward));
        }
        if (activateExternalOlinks != null) {
            transformer.setParameter("activate.external.olinks",
                convertBooleanToXsltParam(activateExternalOlinks));
        }
        if (calloutListTable != null) {
            transformer.setParameter("callout.list.table",
                convertBooleanToXsltParam(calloutListTable));
        }
        if (l10nGentextDefaultLanguage != null) {
            transformer.setParameter("l10n.gentext.default.language",
                convertStringToXsltParam(l10nGentextDefaultLanguage));
        }
        if (autolinkIndexSee != null) {
            transformer.setParameter("autolink.index.see",
                convertBooleanToXsltParam(autolinkIndexSee));
        }
        if (l10nXml != null) {
            transformer.setParameter("l10n.xml",
                convertStringToXsltParam(l10nXml));
        }
        if (profileWordsize != null) {
            transformer.setParameter("profile.wordsize",
                convertStringToXsltParam(profileWordsize));
        }
        if (profileVendor != null) {
            transformer.setParameter("profile.vendor",
                convertStringToXsltParam(profileVendor));
        }
        if (htmlhelpButtonJump1Url != null) {
            transformer.setParameter("htmlhelp.button.jump1.url",
                convertStringToXsltParam(htmlhelpButtonJump1Url));
        }
        if (sectionAutolabel != null) {
            transformer.setParameter("section.autolabel",
                convertBooleanToXsltParam(sectionAutolabel));
        }
        if (footnoteNumberFormat != null) {
            transformer.setParameter("footnote.number.format",
                convertStringToXsltParam(footnoteNumberFormat));
        }
        if (texMathDelims != null) {
            transformer.setParameter("tex.math.delims",
                convertBooleanToXsltParam(texMathDelims));
        }
        if (htmlhelpButtonJump2Url != null) {
            transformer.setParameter("htmlhelp.button.jump2.url",
                convertStringToXsltParam(htmlhelpButtonJump2Url));
        }
        if (preferredMediaobjectRole != null) {
            transformer.setParameter("preferred.mediaobject.role",
                convertStringToXsltParam(preferredMediaobjectRole));
        }
        if (cmdsynopsisHangingIndent != null) {
            transformer.setParameter("cmdsynopsis.hanging.indent",
                convertStringToXsltParam(cmdsynopsisHangingIndent));
        }
        if (epubDcTitleId != null) {
            transformer.setParameter("epub.dc.title.id",
                convertStringToXsltParam(epubDcTitleId));
        }
        if (dcNamespace != null) {
            transformer.setParameter("dc.namespace",
                convertStringToXsltParam(dcNamespace));
        }
        if (annotateToc != null) {
            transformer.setParameter("annotate.toc",
                convertBooleanToXsltParam(annotateToc));
        }
        if (htmlhelpRememberWindowPosition != null) {
            transformer.setParameter("htmlhelp.remember.window.position",
                convertBooleanToXsltParam(htmlhelpRememberWindowPosition));
        }
        if (chunkerOutputMediaType != null) {
            transformer.setParameter("chunker.output.media-type",
                convertStringToXsltParam(chunkerOutputMediaType));
        }
        if (generateToc != null) {
            transformer.setParameter("generate.toc",
                convertStringToXsltParam(generateToc));
        }
        if (calloutDefaultcolumn != null) {
            transformer.setParameter("callout.defaultcolumn",
                convertStringToXsltParam(calloutDefaultcolumn));
        }
        if (chapterAutolabel != null) {
            transformer.setParameter("chapter.autolabel",
                convertStringToXsltParam(chapterAutolabel));
        }
        if (useIdAsFilename != null) {
            transformer.setParameter("use.id.as.filename",
                convertBooleanToXsltParam(useIdAsFilename));
        }
        if (variablelistTermBreakAfter != null) {
            transformer.setParameter("variablelist.term.break.after",
                convertBooleanToXsltParam(variablelistTermBreakAfter));
        }
        if (defaultTableFrame != null) {
            transformer.setParameter("default.table.frame",
                convertStringToXsltParam(defaultTableFrame));
        }
        if (htmlhelpHhk != null) {
            transformer.setParameter("htmlhelp.hhk",
                convertStringToXsltParam(htmlhelpHhk));
        }
        if (autotocLabelInHyperlink != null) {
            transformer.setParameter("autotoc.label.in.hyperlink",
                convertBooleanToXsltParam(autotocLabelInHyperlink));
        }
        if (htmlhelpHhp != null) {
            transformer.setParameter("htmlhelp.hhp",
                convertStringToXsltParam(htmlhelpHhp));
        }
        if (argRepNorepeatStr != null) {
            transformer.setParameter("arg.rep.norepeat.str",
                convertStringToXsltParam(argRepNorepeatStr));
        }
        if (htmlhelpHhc != null) {
            transformer.setParameter("htmlhelp.hhc",
                convertStringToXsltParam(htmlhelpHhc));
        }
        if (calloutUnicode != null) {
            transformer.setParameter("callout.unicode",
                convertBooleanToXsltParam(calloutUnicode));
        }
        if (epubIncludeMetadataDcElements != null) {
            transformer.setParameter("epub.include.metadata.dc.elements",
                convertStringToXsltParam(epubIncludeMetadataDcElements));
        }
        if (htmlLongdesc != null) {
            transformer.setParameter("html.longdesc",
                convertBooleanToXsltParam(htmlLongdesc));
        }
        if (bibliographyNumbered != null) {
            transformer.setParameter("bibliography.numbered",
                convertBooleanToXsltParam(bibliographyNumbered));
        }
        if (htmlhelpAliasFile != null) {
            transformer.setParameter("htmlhelp.alias.file",
                convertStringToXsltParam(htmlhelpAliasFile));
        }
        if (htmlHeadLegalnoticeLinkTypes != null) {
            transformer.setParameter("html.head.legalnotice.link.types",
                convertStringToXsltParam(htmlHeadLegalnoticeLinkTypes));
        }
        if (htmlhelpAutolabel != null) {
            transformer.setParameter("htmlhelp.autolabel",
                convertBooleanToXsltParam(htmlhelpAutolabel));
        }
        if (tablecolumnsExtension != null) {
            transformer.setParameter("tablecolumns.extension",
                convertBooleanToXsltParam(tablecolumnsExtension));
        }
        if (indexOnRole != null) {
            transformer.setParameter("index.on.role",
                convertBooleanToXsltParam(indexOnRole));
        }
        if (makeSingleYearRanges != null) {
            transformer.setParameter("make.single.year.ranges",
                convertBooleanToXsltParam(makeSingleYearRanges));
        }
        if (htmlhelpButtonZoom != null) {
            transformer.setParameter("htmlhelp.button.zoom",
                convertBooleanToXsltParam(htmlhelpButtonZoom));
        }
        if (profileArch != null) {
            transformer.setParameter("profile.arch",
                convertStringToXsltParam(profileArch));
        }
        if (tableFrameBorderStyle != null) {
            transformer.setParameter("table.frame.border.style",
                convertStringToXsltParam(tableFrameBorderStyle));
        }
        if (calloutGraphicsNumberLimit != null) {
            transformer.setParameter("callout.graphics.number.limit",
                convertStringToXsltParam(calloutGraphicsNumberLimit));
        }
        if (calloutGraphicsExtension != null) {
            transformer.setParameter("callout.graphics.extension",
                convertStringToXsltParam(calloutGraphicsExtension));
        }
        if (nominalTableWidth != null) {
            transformer.setParameter("nominal.table.width",
                convertStringToXsltParam(nominalTableWidth));
        }
        if (chunkerOutputIndent != null) {
            transformer.setParameter("chunker.output.indent",
                convertStringToXsltParam(chunkerOutputIndent));
        }
        if (htmlhelpMapFile != null) {
            transformer.setParameter("htmlhelp.map.file",
                convertStringToXsltParam(htmlhelpMapFile));
        }
        if (generateRevhistoryLink != null) {
            transformer.setParameter("generate.revhistory.link",
                convertBooleanToXsltParam(generateRevhistoryLink));
        }
        if (processSourceToc != null) {
            transformer.setParameter("process.source.toc",
                convertBooleanToXsltParam(processSourceToc));
        }
        if (useExtensions != null) {
            transformer.setParameter("use.extensions",
                convertBooleanToXsltParam(useExtensions));
        }
        if (get != null) {
            transformer.setParameter("get",
                convertStringToXsltParam(get));
        }
        if (htmlhelpButtonOptions != null) {
            transformer.setParameter("htmlhelp.button.options",
                convertBooleanToXsltParam(htmlhelpButtonOptions));
        }
        if (chunkTocsAndLots != null) {
            transformer.setParameter("chunk.tocs.and.lots",
                convertBooleanToXsltParam(chunkTocsAndLots));
        }
        if (profileRevisionflag != null) {
            transformer.setParameter("profile.revisionflag",
                convertStringToXsltParam(profileRevisionflag));
        }
        if (htmlhelpHhpTail != null) {
            transformer.setParameter("htmlhelp.hhp.tail",
                convertStringToXsltParam(htmlhelpHhpTail));
        }
        if (refclassSuppress != null) {
            transformer.setParameter("refclass.suppress",
                convertBooleanToXsltParam(refclassSuppress));
        }
        if (epubNcxTocId != null) {
            transformer.setParameter("epub.ncx.toc.id",
                convertStringToXsltParam(epubNcxTocId));
        }
        if (glosstermAutoLink != null) {
            transformer.setParameter("glossterm.auto.link",
                convertBooleanToXsltParam(glosstermAutoLink));
        }
        if (argOrSep != null) {
            transformer.setParameter("arg.or.sep",
                convertStringToXsltParam(argOrSep));
        }
        if (htmlhelpHhpWindows != null) {
            transformer.setParameter("htmlhelp.hhp.windows",
                convertStringToXsltParam(htmlhelpHhpWindows));
        }
        if (htmlhelpButtonStop != null) {
            transformer.setParameter("htmlhelp.button.stop",
                convertBooleanToXsltParam(htmlhelpButtonStop));
        }
        if (chunkQuietly != null) {
            transformer.setParameter("chunk.quietly",
                convertBooleanToXsltParam(chunkQuietly));
        }
        if (glossentryShowAcronym != null) {
            transformer.setParameter("glossentry.show.acronym",
                convertStringToXsltParam(glossentryShowAcronym));
        }
        if (navigGraphicsPath != null) {
            transformer.setParameter("navig.graphics.path",
                convertStringToXsltParam(navigGraphicsPath));
        }
        if (showComments != null) {
            transformer.setParameter("show.comments",
                convertBooleanToXsltParam(showComments));
        }
        if (tableCellBorderColor != null) {
            transformer.setParameter("table.cell.border.color",
                convertStringToXsltParam(tableCellBorderColor));
        }
        if (epubCoverImageId != null) {
            transformer.setParameter("epub.cover.image.id",
                convertStringToXsltParam(epubCoverImageId));
        }
        if (chunkerOutputDoctypePublic != null) {
            transformer.setParameter("chunker.output.doctype-public",
                convertStringToXsltParam(chunkerOutputDoctypePublic));
        }
        if (eclipseAutolabel != null) {
            transformer.setParameter("eclipse.autolabel",
                convertBooleanToXsltParam(eclipseAutolabel));
        }
        if (entryPropagatesStyle != null) {
            transformer.setParameter("entry.propagates.style",
                convertBooleanToXsltParam(entryPropagatesStyle));
        }
        if (generateManifest != null) {
            transformer.setParameter("generate.manifest",
                convertBooleanToXsltParam(generateManifest));
        }
        if (eclipsePluginProvider != null) {
            transformer.setParameter("eclipse.plugin.provider",
                convertStringToXsltParam(eclipsePluginProvider));
        }
        if (defaultTableWidth != null) {
            transformer.setParameter("default.table.width",
                convertStringToXsltParam(defaultTableWidth));
        }
        if (funcsynopsisStyle != null) {
            transformer.setParameter("funcsynopsis.style",
                convertStringToXsltParam(funcsynopsisStyle));
        }
        if (htmlhelpHhcBinary != null) {
            transformer.setParameter("htmlhelp.hhc.binary",
                convertBooleanToXsltParam(htmlhelpHhcBinary));
        }
        if (generateMetaAbstract != null) {
            transformer.setParameter("generate.meta.abstract",
                convertBooleanToXsltParam(generateMetaAbstract));
        }
        if (ncxNamespace != null) {
            transformer.setParameter("ncx.namespace",
                convertStringToXsltParam(ncxNamespace));
        }
        if (epubPackageIdPrefix != null) {
            transformer.setParameter("epub.package.id.prefix",
                convertStringToXsltParam(epubPackageIdPrefix));
        }
        if (profileCondition != null) {
            transformer.setParameter("profile.condition",
                convertStringToXsltParam(profileCondition));
        }
        if (generateSectionTocLevel != null) {
            transformer.setParameter("generate.section.toc.level",
                convertStringToXsltParam(generateSectionTocLevel));
        }
        if (manualToc != null) {
            transformer.setParameter("manual.toc",
                convertStringToXsltParam(manualToc));
        }
        if (profileRole != null) {
            transformer.setParameter("profile.role",
                convertStringToXsltParam(profileRole));
        }
        if (calloutUnicodeStartCharacter != null) {
            transformer.setParameter("callout.unicode.start.character",
                convertStringToXsltParam(calloutUnicodeStartCharacter));
        }
        if (epubNcxFilename != null) {
            transformer.setParameter("epub.ncx.filename",
                convertStringToXsltParam(epubNcxFilename));
        }
        if (inheritKeywords != null) {
            transformer.setParameter("inherit.keywords",
                convertBooleanToXsltParam(inheritKeywords));
        }
        if (exslNodeSetAvailable != null) {
            transformer.setParameter("exsl.node.set.available",
                convertBooleanToXsltParam(exslNodeSetAvailable));
        }
        if (formalObjectBreakAfter != null) {
            transformer.setParameter("formal.object.break.after",
                convertStringToXsltParam(formalObjectBreakAfter));
        }
        if (argChoiceReqCloseStr != null) {
            transformer.setParameter("arg.choice.req.close.str",
                convertStringToXsltParam(argChoiceReqCloseStr));
        }
        if (idWarnings != null) {
            transformer.setParameter("id.warnings",
                convertBooleanToXsltParam(idWarnings));
        }
        if (textinsertExtension != null) {
            transformer.setParameter("textinsert.extension",
                convertBooleanToXsltParam(textinsertExtension));
        }
        if (argRepRepeatStr != null) {
            transformer.setParameter("arg.rep.repeat.str",
                convertStringToXsltParam(argRepRepeatStr));
        }
        if (epubNcxPathname != null) {
            transformer.setParameter("epub.ncx.pathname",
                convertStringToXsltParam(epubNcxPathname));
        }
        if (webhelpAutolabel != null) {
            transformer.setParameter("webhelp.autolabel",
                convertBooleanToXsltParam(webhelpAutolabel));
        }
        if (htmlhelpButtonRefresh != null) {
            transformer.setParameter("htmlhelp.button.refresh",
                convertBooleanToXsltParam(htmlhelpButtonRefresh));
        }
        if (htmlhelpEncoding != null) {
            transformer.setParameter("htmlhelp.encoding",
                convertStringToXsltParam(htmlhelpEncoding));
        }
        if (argChoiceOptCloseStr != null) {
            transformer.setParameter("arg.choice.opt.close.str",
                convertStringToXsltParam(argChoiceOptCloseStr));
        }
        if (profileSeparator != null) {
            transformer.setParameter("profile.separator",
                convertStringToXsltParam(profileSeparator));
        }
        if (epubVocabularyProfilePackage != null) {
            transformer.setParameter("epub.vocabulary.profile.package",
                convertStringToXsltParam(epubVocabularyProfilePackage));
        }
        if (chunkerOutputEncoding != null) {
            transformer.setParameter("chunker.output.encoding",
                convertStringToXsltParam(chunkerOutputEncoding));
        }
        if (htmlhelpButtonHideshow != null) {
            transformer.setParameter("htmlhelp.button.hideshow",
                convertBooleanToXsltParam(htmlhelpButtonHideshow));
        }
        if (biblioentryItemSeparator != null) {
            transformer.setParameter("biblioentry.item.separator",
                convertStringToXsltParam(biblioentryItemSeparator));
        }
        if (authorOthernameInMiddle != null) {
            transformer.setParameter("author.othername.in.middle",
                convertBooleanToXsltParam(authorOthernameInMiddle));
        }
        if (webhelpIndexerLanguage != null) {
            transformer.setParameter("webhelp.indexer.language",
                convertStringToXsltParam(webhelpIndexerLanguage));
        }
        if (tableCellBorderThickness != null) {
            transformer.setParameter("table.cell.border.thickness",
                convertStringToXsltParam(tableCellBorderThickness));
        }
        if (variablelistTermSeparator != null) {
            transformer.setParameter("variablelist.term.separator",
                convertStringToXsltParam(variablelistTermSeparator));
        }
        if (funcsynopsisDecoration != null) {
            transformer.setParameter("funcsynopsis.decoration",
                convertBooleanToXsltParam(funcsynopsisDecoration));
        }
        if (htmlhelpEnumerateImages != null) {
            transformer.setParameter("htmlhelp.enumerate.images",
                convertBooleanToXsltParam(htmlhelpEnumerateImages));
        }
        if (webhelpCommonDir != null) {
            transformer.setParameter("webhelp.common.dir",
                convertStringToXsltParam(webhelpCommonDir));
        }
        if (linenumberingSeparator != null) {
            transformer.setParameter("linenumbering.separator",
                convertStringToXsltParam(linenumberingSeparator));
        }
        if (glossarySort != null) {
            transformer.setParameter("glossary.sort",
                convertBooleanToXsltParam(glossarySort));
        }
        if (indexTermSeparator != null) {
            transformer.setParameter("index.term.separator",
                convertStringToXsltParam(indexTermSeparator));
        }
        if (epubIncludeNcx != null) {
            transformer.setParameter("epub.include.ncx",
                convertStringToXsltParam(epubIncludeNcx));
        }
        if (defaultFloatClass != null) {
            transformer.setParameter("default.float.class",
                convertStringToXsltParam(defaultFloatClass));
        }
        if (insertOlinkPdfFrag != null) {
            transformer.setParameter("insert.olink.pdf.frag",
                convertBooleanToXsltParam(insertOlinkPdfFrag));
        }
        if (annotationGraphicOpen != null) {
            transformer.setParameter("annotation.graphic.open",
                convertStringToXsltParam(annotationGraphicOpen));
        }
        if (indexNumberSeparator != null) {
            transformer.setParameter("index.number.separator",
                convertStringToXsltParam(indexNumberSeparator));
        }
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public File getGeneratedSourceDirectory() {
        return generatedSourceDirectory;
    }

	public String getDefaultStylesheetLocation() {
        return "docbook/epub3/chunk.xsl";
	}

	public String getType() {
	    return "epub3";
	}

    public String getStylesheetLocation() {
        if (epub3Customization != null) {
            int foundOffcet = epub3Customization.toString().indexOf("classpath:");

            if(foundOffcet != -1) {
                final String withinPath = epub3Customization.toString().substring(foundOffcet + 11);
                getLog().debug("User Customization changed to classpath: " + withinPath);
                return withinPath;
            } else {
                getLog().debug("User Customization provided: " + epub3Customization.getAbsolutePath());
                return epub3Customization.getAbsolutePath();
             }
        } else if (getNonDefaultStylesheetLocation() == null) {
            getLog().debug("Using default Customization: " + getDefaultStylesheetLocation());
            return getDefaultStylesheetLocation();
        } else {
            getLog().debug("Using non-default Customization: " + getNonDefaultStylesheetLocation());
            return getNonDefaultStylesheetLocation();
        }
    }

    public String getTargetFileExtension() {
        return targetFileExtension;
    }

    public void setTargetFileExtension(String extension) {
        targetFileExtension = extension;
    }

    public String[] getIncludes() {
        String[] results = includes.split(",");
        for (int i = 0; i < results.length; i++) {
            results[i] = results[i].trim();
        }
        return results;
    }

    public List getEntities() {
        return entities;
    }

    public List getCustomizationParameters()
    {
    	return customizationParameters;
    }

    public Properties getSystemProperties()
    {
        return systemProperties;
    }

    public Target getPreProcess() {
        return preProcess;
    }

    public Target getPostProcess() {
        return postProcess;
    }

    public MavenProject getMavenProject() {
        return project;
    }

    public List getArtifacts() {
        return artifacts;
    }

    protected boolean getXIncludeSupported() {
        return xincludeSupported;
    }

    /**
     * Returns false if the stylesheet is responsible to create the output file(s) using its own naming scheme.
     *
     * @return If using the standard output.
     */
    protected boolean isUseStandardOutput() {
        return useStandardOutput;
    }

    protected boolean isShowXslMessages() {
        return showXslMessages;
    }

    protected void setShowXslMessages(boolean showXslMessages) {
        this.showXslMessages = showXslMessages;
    }

    protected void setUseStandardOutput(boolean useStandardOutput) {
        this.useStandardOutput = useStandardOutput;
    }

    protected void setSkip(boolean skip) {
        this.skip = skip;
    }

    protected boolean isSkip() {
        return this.skip;
    }

    /**
     * @parameter
     */
    public String customCss;
    /**
     * @parameter
     */
    public String coverDirectory;
    /**
     * @parameter
     */
    public String coverName;
    /**
     * @parameter
     */
    protected File outputDirectory;
    /**
     * @parameter
     */
    protected File svgDirectory;
    /**
     * @parameter
     */
    public String svgName;


    /**
     * @parameter
     */
    protected boolean failNever;
    /**
     * @parameter
     */
    protected boolean failAtEnd;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Path source = sourceDirectory.toPath();
            Path target = targetDirectory.toPath();
            Path output = outputDirectory != null ? outputDirectory.toPath() : null;
            Path svg = svgDirectory != null ? svgDirectory.toPath() : null;
            Path css = customCss != null ? Paths.get(customCss) : null;
            Path covers = Paths.get(coverDirectory);
            List<Path> xmls = Files.list(source)
                    .filter( p -> p.getFileName().toString().endsWith(".xml"))
                    .collect(Collectors.toList());
            Files.createDirectories(target);
            for (Path xml : xmls) {
                Path dir = Files.createTempDirectory(target, "epub-");
                Path src = dir.resolve("src");
                Path tgt = dir.resolve("build");
                Files.createDirectories(src);
                Files.createDirectories(tgt);
                Files.copy(xml, src.resolve(xml.getFileName()));
                Files.copy(covers.resolve(xml.getFileName().toString().replace(".xml", ".png")), tgt.resolve(coverName));
                if (css != null && Files.exists(css)) {
                    Files.copy(css, tgt.resolve(css.getFileName()));
                }
                if (svg != null) {
                    Path tsvg = svg.resolve(xml.getFileName().toString().replace(".xml", ".svg"));
                    if (Files.exists(tsvg)) {
                        Files.copy(tsvg, tgt.resolve(svgName));
                    }
                }
                sourceDirectory = src.toFile();
                targetDirectory = tgt.toFile();
                super.execute();

                if (output != null) {
                    Files.createDirectories(output);
                    Files.move(dir.resolve(xml.getFileName().toString().replace(".xml", ".epub")),
                            output.resolve(xml.getFileName().toString().replace(".xml", ".epub")),
                            StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(dir.resolve(xml.getFileName().toString().replace(".xml", ".epub")),
                            target.resolve(xml.getFileName().toString().replace(".xml", ".epub")),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error", e);
        }
    }

    private Properties originalSystemProperties;

    /**
     * Allows subclasses to add their own specific pre-processing logic.
     *
     * @throws MojoExecutionException If the Mojo fails to pre-process the results.
     */
    public void preProcess() throws MojoExecutionException {
        // save system properties
        originalSystemProperties = (Properties) System.getProperties().clone();
        // set the new properties
        if (getSystemProperties() != null) {
            final Enumeration props = getSystemProperties().keys();
            while (props.hasMoreElements()) {
                final String key = (String) props.nextElement();
                System.setProperty(key, getSystemProperties().getProperty(key));
            }
        }
    }

    /**
     * Allows classes to add their own specific post-processing logic.
     *
     * @throws MojoExecutionException If the Mojo fails to post-process the results.
     */
    public void postProcess() throws MojoExecutionException {
        // restore system properties
        if (originalSystemProperties != null) {
            System.setProperties(originalSystemProperties);
        }
    }
}