package fr.gnodet.epubs.enc;

import com.adobe.epubcheck.api.EpubCheck;
import org.w3c.dom.*;
import org.w3c.tidy.Tidy;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Quotes.fixQuotesInParagraph;

public class Main {

    static final javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String[] args) throws Exception {

        dbf.setNamespaceAware(true);

        Document enclist = dbf.newDocumentBuilder().parse(Main.class.getResourceAsStream("enc-list.xml"));
        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {
//        for (int i = 17; i < books.getLength(); i++) {
//        int i = 19; {
            Element book = (Element) books.item(i);
            String file = book.getAttribute("file");
            String title = book.getAttribute("title");
            String creator = book.getAttribute("creator");
            String date = book.getAttribute("date");
            String full = getFull(creator);
            URL url = new URL("http://www.vatican.va/holy_father/" + full + "/encyclicals/documents/" + file);
            try {
                process(url, "target/cache/" + file, "target/html/" + file);
                createEpub(new File("target/html/" + file), new File("target/epub/" + file.substring(0, file.lastIndexOf('.')) + ".epub"), title, creator);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void process(URL url, String cache, String output) throws Exception {
        System.out.println("Processing: " + url);

        //
        // Grab html
        //

        // Load URL text content
        String document = loadTextContent(url, cache);

        // Add doctype if not present
        if (!document.startsWith("<!DOCTYPE")) {
            document = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + document;
        }
        // Fix bad html
        document = document.replaceAll("<p align=\"left\"<a", "<p align=\"left\"><a");
        // Delete office elements
        document = document.replaceAll("<o:p>[\\S\\s]*?</o:p>", "");
        // Fix encoding
        document = document.replaceAll("<meta[^>]*http-equiv=\"Content-Type\"[^>]*/>", "");
        document = document.replace("<head>", "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
        // Delete bad attributes
        document = document.replaceAll("<a title ", "<a ");
        // Fix footnotes with missing paragraph and missing closing div tag
        // Tidy Html
        document = tidyHtml(document);
        // Fix encoding
        document = document.replaceAll("<meta[^>]*http-equiv=\"Content-Type\"[^>]*/>", "");
        document = document.replace("<head>", "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
        // Translate entities
        document = translateEntities(document);

        //
        // Html clean up
        //

        // Remove new lines to simplify
        document = document.replaceAll("[\r\n]+", " ");
        // Delete inlined javascript
        document = document.replaceAll("<!\\[if !supportFootnotes\\]>", "");
        document = document.replaceAll("<!\\[endif\\]>", "");
        // Delete scripts tags
        document = document.replaceAll("<script[\\S\\s]*?</script>", "");
        document = document.replaceAll("<SCRIPT[\\S\\s]*?</SCRIPT>", "");
        // Delete image tags
        document = document.replaceAll("<img[^>]*>", "");
        document = document.replaceAll("<IMG[^>]*>", "");
        // Delete unwanted attributes
        document = document.replaceAll(" \\b(width|topmargin|border|marginwidth|marginheight|cellspacing|" +
                "cellpadding|hspacing|lang|xml:lang|alink|vlink|link|background|" +
                "text|height|hspace|alt|bgcolor|rowspan|valign)=[^\\s>]*", "");
        document = document.replaceAll(" style=\"[^\"]*\"", "");
        document = document.replaceAll(" style='[^']*'", "");
        document = document.replaceAll(" clear=\"all\"| align=\"left\"| align=\"justify\"| class=\"[^\"]*\"", "");
        // Remove unwanted meta tags
        document = document.replaceAll("<meta name=\"generator\".*?/>", "");
        // Remove unwanted anchor attributes
        document = document.replaceAll("(<a[^>]*?)\\s*name=\"[^\"]*\"\\s*([^>]*?>)", "$1$2");
        document = document.replaceAll("(<a[^>]*?)\\s*title=\"\"\\s*([^>]*?>)", "$1$2");
        document = document.replaceAll("(<a[^>]*\\s)\\s+([^>]*>)", "$1$2");
        document = document.replaceAll("<a id=\"top\"></a>", "");
        document = document.replaceAll("<a href=\"#top\"></a>", "");
        document = document.replaceAll("<basefont.*?/>", "");
        document = document.replaceAll("<tbody>|</tbody>", "");
        // Remove font styling
        document = document.replaceAll("<font (?:face=\"[^\"]*\" )?size=\"3\">([\\s\\S]*?)</font>", "$1");
        // Clean unneeded span
        document = document.replaceAll("<p><span>([\\s\\S]*?)</span>([\\s\\S]*?)</p>", "<p>$1 $2</p>");
        // Clean paragraphs
        document = document.replaceAll("\\s*<br />\\s*<br />\\s*", "</p><p>");

        // Delete first table
        document = document.replaceFirst("<table[\\s\\S]*?</table>", "");

        // Fix for extraction
        document = document.replaceAll("(<p>NOTES</p>|<p><b><br />\\s*NOTES</b></p>)", "<hr /><p><b>$1</b></p>");
        document = document.replaceAll("Sollicitudo rei socialis</i><br />", "Sollicitudo rei socialis</i> ");
        document = document.replaceAll("n\\.</i> 5 <i>1\\.<br />", "n.</i> 5 <i>1.");
        document = document.replaceAll("<p><b>(<p><b><br /> NOTES</b></p>)</b></p>", "<p><b>NOTES</b></p>");
        // Distribute <center> tags
        document = distribute(document, "center", "p");

        String head = extract(document, "<head>.*?</head>", 0);
        String title = extract(document, ".*<td[^>]*>(.*?LETTRE ENCYCLIQUE.*?)(<p><i>|<p><em>|<p><b>|<p[^>]*>(<center>)?\u00a0|</td>|<[^>]*>\\s*INTRO)", 1);
        String bened = extractFollowingParaContaining(document, ".*[Bb]énédiction.*", document.indexOf(title) + title.length());
        String footnotes = extract(document, "<hr[^>]*>(.*?<p.*?)(<p[^>]*>[\\s\u00a0]*</p>|<p><br />|</td>)", 1);
        String copyright = extract(document, ">\\s*(©[^<]*?)\\s*<", 1);
        String main = document.substring(document.indexOf(bened != null ? bened : title) + (bened != null ? bened : title).length(),
                                         document.indexOf(footnotes != null ? footnotes : (copyright != null ? copyright : "</body>")));

        document = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\">" +
                cleanHead(head) + "<body>" +
                "<div id=\"title\">" + cleanTitle(title) + "</div>" +
                (bened != null ? "<div id=\"bened\">" + cleanBened(bened) + "</div>" : "") +
                "<div id=\"main\">" + cleanMain(main) + "</div>" +
                (footnotes != null ? "<div id=\"notes\">" + cleanNotes(footnotes) + "</div>" : "") +
                (copyright != null ? "<div id=\"copyright\">" + cleanCopyright(copyright) + "</div>" : "") +
                "</body></html>";

        writeToFile(document, output);
    }

    private static String distribute(String document, String outer, String inner) {
        Matcher paragraph = Pattern.compile("<" + outer + ">([\\s\\S]*?)</" + outer + ">").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(paragraph.group(1).replaceAll("(<" + inner +"[^>]*>)([\\s\\S]*?)(</" + inner + ">)", "$1<" + outer + ">$2</" + outer + ">$3"));
            start = paragraph.end();
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    private static String cleanHead(String document) {
        // Remove meta tags with spaces
        document = document.replaceAll("<meta[^>]*name=\"[^\"]* [^\"]*\"[^>]*/>", "");
        // Remove style elements
        document = document.replaceAll("<style.*?</style>", "");
        // Add our style
        document = document.replaceAll("</head>",
                "<style type=\"text/css\">\n" +
                        " #title { color: #663300; text-align: center; }\n" +
                        " #title .title { font-style:italic; font-size: larger; font-weight:bold; }\n" +
                        " #title .author { font-weight:bold; }\n" +
                        " #bened { font-style:italic; } \n" +
                        " #main .numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        " #main .footnote { vertical-align: super; font-size: 70%; line-height: 80%; }\n" +
                        " #main .center { text-align: center; }\n" +
                        " #notes p { margin: 0; padding: 0; font-size: smaller; }\n" +
                        " #notes .ref { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        " #copyright { color: #663300; text-align: center; font-size: smaller; }\n" +
                "</style>" +
                "</head>");
        return document;
    }

    private static String cleanTitle(String document) {
        document = document.trim();
        document = fixTypos(document);
        document = fixQuotesInParagraph(document);
        document = document.replaceAll("<center>|</center>|<span[^>]*>|<font[^>]*>|</span>|</font>| align=\"center\"|" +
                                       "<strong>|</strong>|<em>|</em>|<b>|</b>|<i>|</i>|" +
                                       "<td>|</td>|<tr>|</tr>|<table>|</table>", "");
        document = document.replaceAll("</p>\\s*<p>", "<br /><br />");
        document = document.replaceAll("<p[^>]*>", "<p>");
        document = document.replaceAll("[\u00a0\\s]*<br />[\u00a0\\s]*", "<br />");
        document = document.replaceAll("<p><br />", "<p>");
        document = document.replaceAll("(<br />)+</p>", "<p>");
        document = document.replaceAll("([^>])<br />SUR", "$1<br /><br />SUR");
        document = document.replaceAll("VOLONTÉ<br />", "VOLONTÉ<br /><br />");
        document = document.replaceAll("<br />À L’OCCASION", "<br /><br />À L’OCCASION");
        document = document.replaceAll("PAUL VI", "<br />PAUL VI");
        document = document.replaceAll("(BENOÎT XVI|JEAN-PAUL II|PAUL VI)", "<span class=\"author\">$1</span><br />");
        if (document.indexOf("LETTRE ENCYCLIQUE") > 4) {
            document = document.replaceAll("<p>(.*?)<br />", "<p><span class=\"title\">$1</span><br />");
        } else {
            document = document.replaceAll("(LETTRE ENCYCLIQUE<br />)(.*?)<br />", "<span class=\"title\">$2</span><br /><br />$1");
        }
        document = document.replaceAll("</p>\\s*</p>", "</p>");
        document = document.replaceAll("<br />\\s*(<br /><br />|</p>)", "$1");
        if (document.endsWith("<p>")) {
            document = document.substring(0, document.length() - 3);
        }
        if (!document.endsWith("</p>")) {
            document = document + "</p>";
        }
        if (!document.startsWith("<p>")) {
            document = "<p>" + document;
        }
        return document;
    }

    private static String cleanBened(String document) {
        document = document.trim();
        document = document.replaceAll("<i>|</i>", "");
        document = document.replaceAll("<p align=\"center\">", "<p class=\"center\">");
        document = fixTypos(document);
        document = fixQuotesInParagraph(document);
        return document;
    }

    private static String cleanMain(String document) {
        document = document.substring(document.indexOf("<p"), document.lastIndexOf("</p>") + 4);
        document = document.replaceAll("<font[^>]*>(.*?)</font>", "$1");
        document = document.replaceAll("<p[^>]*>[\\s\u00a0]*</p>", "");
        document = document.replaceAll("<p align=\"center\">", "<p class=\"center\">");
        document = document.replaceAll("<p>\\s*<center>(.*?)</center>\\s*</p>", "<p class=\"center\">$1</p>");
        document = document.replaceAll("</p><p></b>", "</b></p><p>");
        document = document.replaceAll("</p></b><p>", "</b></p><p>");
        document = document.replaceAll("<blockquote>\\s*</blockquote>", "");
        document = document.replaceAll("<table[^>]*>|</table>|<tr[^>]*>|</tr>|<td[^>]*>|</td>", "");

        // Simplify tags
        document = document.replaceAll("<strong>(.*?)</strong>", "<b>$1</b>");
        document = document.replaceAll("<em>(.*?)</em>", "<i>$1</i>");

        document = document.replaceAll("\\b([IXV][IXV]*)(e|er|ème)\\b", "<span style=\"font-size: smaller\">$1</span><sup>e</sup>");
        document = document.replaceAll(" 1<sup>er</sup> s\\.", "<span style=\"font-size: smaller\">I</span><sup>er</sup> s.");
        // Fix dashes
        document = document.replaceAll("(\\S)_ ", "$1 - ");
        document = document.replaceAll(" _(\\S)", " - $1");
        document = document.replaceAll(" _ ", " - ");
        document = document.replaceAll(" -- ", " - ");
        document = document.replaceAll(" - </i>", "</i> - ");
        document = document.replaceAll("(</[^>]*>)-", "$1 - ");
        document = document.replaceAll("-un et trine-", "- un et trine -");
        document = document.replaceAll("-et je tiens à le faire remarquer d['’]emblée-", "- et je tiens à le faire remarquer d’emblée -");
        document = document.replaceAll("privées- correspondant", "privées - correspondant");
        document = document.replaceAll("Incarnation- et", "Incarnation - et");
        document = document.replaceAll("Mère- et", "Mère - et");
        document = document.replaceAll("qui- suivant la consigne de l['’]ange-", "qui - suivant la consigne de l’ange -");
        document = document.replaceAll("Pères -comme le rappelle aussi le Concile- ", "Pères - comme le rappelle aussi le Concile - ");
        document = document.replaceAll("hommes -à tous et à chacun- ", "hommes - à tous et à chacun - ");
        document = document.replaceAll("présente -par la volonté du Fils et par l['’]Esprit Saint- dans", "présente - par la volonté du Fils et par l’Esprit Saint - dans");
        document = document.replaceAll("-celle de Jésus Christ- est", "- celle de Jésus Christ - est");
        document = document.replaceAll("expriment- comme je l['’]ai dit- la", "expriment - comme je l’ai dit - la");
        document = document.replaceAll("effet -et cette prière en témoigne- la", "effet - et cette prière en témoigne - la");
        document = document.replaceAll("Christ -accomplissement des prophéties messianiques- rend", "Christ - accomplissement des prophéties messianiques - rend");
        document = document.replaceAll("-«qu['’]il m['’]advienne»-", "- « qu’il m’advienne » -");
        document = document.replaceAll("-le Fils du Très-Haut \\(cf. Lc 1, 32\\)-", "- le Fils du Très-Haut (cf. Lc 1, 32) -");
        document = document.replaceAll("-comme l'enseigne le Concile Vatican II-", "- comme l'enseigne le Concile Vatican II -");
        document = document.replaceAll("- célébration liturgique du mystère de la Rédemption-", " - célébration liturgique du mystère de la Rédemption -");
        // Fix quotes problems
        document = document.replaceAll("Recevez le Saint-Esprit&raquo;.", "Recevez le Saint-Esprit&raquo; 153.");
        document = document.replaceAll("Cyrille déclare: Ne ", "Cyrille déclare: \"Ne ");
        document = document.replaceAll("Seigneur,\" parle", "Seigneur, parle");
        document = document.replaceAll(" plus profonde \".\"", " plus profonde \".");
        document = document.replaceAll("que me manque-t-il encore \\? J", "que me manque-t-il encore ?” J");
        document = document.replaceAll("pesants M M1 Jn", "pesants\" (cf. 1 Jn");
        document = document.replaceAll("âme \".e", "âme \".");
        document = document.replaceAll("coeur", "cœur");
        document = document.replaceAll("<b>:</b>", ":");
        document = document.replaceAll("<b>\" </b>", "&quot; ");
        document = document.replaceAll("transsubstantiation\\.,", "transsubstantiation.");
        document = document.replaceAll("par lui \" (71)", "par lui \" (71).");
        document = document.replace("observe les commandements 1. \"", "observe les commandements. [...] \"");
        document = document.replaceAll("cette formule: \"Tu aimeras", "cette formule : Tu aimeras");
        document = document.replaceAll("à a ceux", "« à ceux");
        document = document.replaceAll("forte avec \"œuvre du", "forte avec l'œuvre du");
        document = document.replaceAll("au fond de sa conscience, l'homme découvre la présence", "au fond de sa conscience, « l'homme découvre la présence");
        document = document.replaceAll("reste de la population\" pauvre", "reste de la population, pauvre");
        document = document.replaceAll("commun de l['’]humanité \\(47\\)", "commun de l’humanité\" (47)");
        document = document.replaceAll("il faut<i>", "il faut <i>");
        document = document.replaceAll("discernementsur", "discernement sur");
        document = document.replaceAll("progrèstechnologique", "progrès technologique");
        document = document.replaceAll("personnelle et sociale de l['’]homme", "personnelle et sociale de l’homme\"");
        document = document.replaceAll("L['’]esprit, rendu ainsi «", "« L’esprit, rendu ainsi \"");
        document = document.replaceAll("ontolo<i>gique\\.</i>", "ontologique.");
        document = document.replaceAll("Abel et le tua.</i></p>\\s*<p><i>Le Seigneur", "Abel et le tua.<br/><br/>Le Seigneur");
        document = document.replaceAll("6, 28\\.35", "6, 28-35");
        document = document.replaceAll("Mettez\\. vous", "Mettez-vous");

        // Clean up spaces / tags
        document = document.replaceAll("<b>[\\s\u00a0]*\"[\\s\u00a0]*</b>", "\"");
        document = document.replaceAll("<i[\\s\u00a0]*\"[\\s\u00a0]*</i>", "\"");
        document = document.replaceAll("<b>[\\s\u00a0]*</b>", "");
        document = document.replaceAll("<i>[\\s\u00a0]*</i>", "");
        document = document.replaceAll("<p>Si<i>", "<p>Si <i>");
        document = document.replaceAll("</i> <i>", " ");
        document = document.replaceAll("<i>(\\s+)", "$1<i>");
        document = document.replaceAll("(\\s+)</i>", "</i>$1");
        document = document.replaceAll("<i><br />", "<br /><i>");
        document = document.replaceAll("<br /></i>", "</i><br />");
        document = document.replaceAll("<i><br /></i>", "<br />");
        document = document.replaceAll("</b> <b>", " ");
        document = document.replaceAll("<b>(\\s+)", "$1<b>");
        document = document.replaceAll("(\\s+)</b>", "</b>$1");
        document = document.replaceAll("<b><br /></b>", "<br />");
        document = document.replaceAll("<br /></b>", "</b><br />");

        // Clean up italics
        document = document.replaceAll("</i> <i>", " ");
        document = document.replaceAll("cf<i>\\.", "cf.<i>");
        document = document.replaceAll("<i>(\\s+)", "$1<i>");
        document = document.replaceAll("(\\s+)</i>", "</i>$1");
        document = document.replaceAll("([.;:,?!])</i>", "</i>$1");


        // Process
        document = fixTypos(document);
        document = fixQuotes(document);
        document = fixBibleRefs(document);
        document = fixFootNotes(document);
        document = fixNumberedParagraphs(document);
        document = fixWhitespaces(document);

        // Clean hrefs
        int len;
        do {
            len = document.length();
            document = document.replaceAll("(href=\"[^\"\\s\u00a0]*)[\\s\u00a0]*", "$1");
        } while (document.length() != len);
        // Clean end paras
        document = document.replaceAll("</p>\\s*</p>", "</p>");
        document = document.replaceAll("<b>\\s*</p>\\s*<p>\\s*</b>", "</p><p>");
        return document;
    }

    private static String cleanNotes(String document) {
        document = document.replaceAll("Acta Leonis XIII, t\\. XI\"", "Acta Leonis XIII, t. XI");
        document = document.replaceAll("Paoline, 1966\" p", "Paoline, 1966, p");
        document = document.replaceAll(" ler novembre 1885\"", " ler novembre 1885,");
        document = document.replaceAll("423\\. \\(31\\) Cf", "423.<br />(31) Cf");

        document = document.trim();
        document = document.replaceAll("<div[^>]*>|</div>|<span[^>]*>|</span>|<font[^>]*>|</font>", "");
        document = document.replaceAll("<table[^>]*>|</table>|<tr[^>]*>|</tr>|<td[^>]*>|</td>", "");
        document = document.replaceAll("<p>\\s*<b>\\s*<p>(.*?)</p>\\s*</b>\\s*</p>", "<p><b>$1</b></p>");
        document = "<p>" + document + "</p>";
        document = document.replaceAll("<br />", "</p><p>");
        document = document.replaceAll("<p[^>]*>©.*?</p>", "");
        document = document.replaceAll("<p>", "</p><p>");
        document = document.replaceAll("</p>\\s*<a", "</p><p><a");
        document = document.replaceAll("<p>\\s*<p>", "<p>");
        document = document.replaceAll("</p>\\s*</p>", "</p>");
        document = document.replaceAll("<p>\\s*</p>", "");
        if (document.startsWith("</p>")) {
            document = document.substring(4);
        }
        if (document.endsWith("<p>")) {
            document = document.substring(3);
        }
        document = document.replaceAll("<p>[\\s\u00a0]+", "<p>");
        document = document.replaceAll("<p><b><a[^>]*>([0-9]+)</a></b>", "<p><a>$1</a>");
        document = document.replaceAll("(<p><a[^>]*>)([0-9]+)(</a>)", "$1[$2]$3");
        document = document.replaceAll("<p>\\((<a[^>]*>)([0-9]+)(</a>)\\)", "<p>$1[$2]$3");
        document = document.replaceAll("<p>\\s*<a[^>]*>\\(</a><a[^>]*>([0-9]+)</a>\\)", "<p><a>[$1]</a>");
        document = document.replaceAll("<p>\\s*([0-9]+)\\. ", "<p><a>[$1]</a> ");
        document = document.replaceAll("(<p><a[^>]*>\\[[0-9]+\\]</a>)(\\S)", "$1 $2");
        document = document.replaceAll("\\.\\.\\.", "…");
        document = fixTypos(document);
        document = fixQuotes(document);
        document = fixBibleRefs(document);
        document = fixWhitespaces(document);
        document = document.replaceAll("[\\s\u00a0]+</p>", "</p>");
        document = document.replaceAll("<p>\\s*</p>", "");
        document = document.replaceAll("<p>\\(([0-9]+)\\) ", "<p><a>[$1]</a> ");
        document = document.replaceAll("<p><a[^>]*>\\[([0-9]+)\\]</a>", "<p id=\"fn$1\"><a class=\"ref\" href=\"#fnr$1\">[$1]</a>");
        document = document.replaceAll("(href=\"[^\"\\s]*)\\s*([^\"\\s]*)\\s*([^\"\\s]*)\\s*", "$1$2$3");
        document = document.replaceAll("(href=\"[^\"\\s]*)\\s*([^\"\\s]*)\\s*", "$1$2");
        document = document.replaceAll("(href=\"[^\"\\s]*)\\s*", "$1");
        document = document.replaceAll("http\u00a0://", "http://");
        document = document.replaceAll("<i>loc. cit.</i> ,", "<i>loc. cit.</i>,");
        document = document.replaceAll("<i>ibid.\\s+</i>,", "<i>ibid.</i>,");
        document = document.replaceAll("<i>Ibid.\\s+</i>,", "<i>Ibid.</i>,");
        document = document.replaceAll("<i>Ibid</i>.,", "<i>Ibid.</i>,");
        document = document.replaceAll("\\bIbid\\.,", "<i>Ibid.</i>,");
        document = document.replaceAll("\\bibid\\.,", "<i>ibid.</i>,");
        document = document.replaceAll("\\bibid,", "<i>ibid.</i>,");
        document = document.replaceAll("\\bIbid,", "<i>Ibid.</i>,");
        document = document.replaceAll("\\bIbid n", "<i>Ibid.</i>, n");
        document = document.replaceAll("<i>(\\s+)", "$1<i>");
        document = document.replaceAll("(\\s+)</i>", "</i>$1");
        document = document.replaceAll("<i>(<a[^>]*?>)ibid\\s*</a></i>\\s*<a[^>]*?>\\.\\s*</a>", "<i>$1ibid.</a></i>");
        document = document.replaceAll("( *\u00A0 *)( *\u00A0 *)*", "\u00A0");
        document = document.replaceAll("<p[^>]*>NOTES</p>", "");
        document = document.replaceAll(" +", " ");
        document = document.replaceAll("pp\\. 959- 960", "pp. 959-960");
        document = document.replaceAll("p\\. 674- 675", "p. 674-675");
        return document.trim();
    }

    private static String cleanCopyright(String document) {
        document = "<p>" + document + "</p>";
        return document;
    }

    private static String extract(String document, String pattern, int group) {
        return extract(document, pattern, group, 0);
    }

    private static String extract(String document, String pattern, int group, int start) {
        Matcher matcher = Pattern.compile(pattern).matcher(document);
        return matcher.find(start) ? matcher.group(group) : null;
    }

    private static String extractFollowingParaContaining(String document, String containing, int begin) {
        Matcher paragraph = Pattern.compile("<p[\\s\\S]*?</p>").matcher(document);
        int start = begin;
        int first = 0;
        int last = start;
        while (paragraph.find(start)) {
            start = paragraph.end();
            if (paragraph.group().matches("<p[^>]*>[\\s\u00a0]*</p>")) {
                continue;
            }
            if (!paragraph.group().matches(containing)) {
                break;
            }
            if (first == 0) {
                first = paragraph.start();
            }
            last = paragraph.end();
        }
        return first != 0 ? document.substring(first, last) : null;

    }

    private static String loadTextContent(URL url, String cache) throws IOException {
        File file = new File(cache);
        file.getParentFile().mkdirs();

        if (!file.exists()) {
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            if (encoding == null) {
                String type = connection.getContentType();
                int idx = type.indexOf("charset=");
                if (idx > 0) {
                    encoding = type.substring(idx + "charset=".length());
                }
            }
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }
            Reader reader;
            {
                BufferedInputStream bufIn = new BufferedInputStream(is);
                bufIn.mark(3);
                boolean utf8 = bufIn.read() == 0xEF && bufIn.read() == 0xBB && bufIn.read() == 0xBF;
                if (utf8) {
                    reader = new BufferedReader(new InputStreamReader(bufIn, "UTF-8"));
                } else {
                    bufIn.reset();
                    reader = new BufferedReader(new InputStreamReader(bufIn, encoding));
                }
            }
            Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
            copy(reader, writer);
        }
        Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8");
        Writer writer = new StringWriter();
        copy(reader, writer);
        return writer.toString();
    }

    private static String tidyHtml(String document) throws IOException {
        Writer writer;
        writer = new StringWriter();
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.parse(new StringReader(document), writer);
        writer.close();
        document = writer.toString();
        return document;
    }

    static final Map<Pattern, String> ENTITIES;

    static {
        // Init map
        ENTITIES = new LinkedHashMap<Pattern, String>();
        //  C0 Controls and Basic Latin
        ENTITIES.put(Pattern.compile("&#34;"), "&quot;");
        ENTITIES.put(Pattern.compile("&#38;"), "&amp;");
        ENTITIES.put(Pattern.compile("&#60;"), "&lt;");
        ENTITIES.put(Pattern.compile("&#62;"), "&gt;");
        ENTITIES.put(Pattern.compile("&#133;"), "&hellip;");
        ENTITIES.put(Pattern.compile("&#134;"), "&dagger;");
        ENTITIES.put(Pattern.compile("&#140;"), "&OElig;");
        ENTITIES.put(Pattern.compile("&#145;"), "&lsquo;");
        ENTITIES.put(Pattern.compile("&#146;"), "&rsquo;");
        ENTITIES.put(Pattern.compile("&#147;"), "&ldquo;");
        ENTITIES.put(Pattern.compile("&#148;"), "&rdquo;");
        ENTITIES.put(Pattern.compile("&#150;"), "&ndash;");
        ENTITIES.put(Pattern.compile("&#151;"), "&mdash;");
        ENTITIES.put(Pattern.compile("&#156;"), "&oelig;");
        // Standard
        ENTITIES.put(Pattern.compile("&#160;"), "&nbsp;"); //  no-break space
        ENTITIES.put(Pattern.compile("&#161;"), "&iexcl;"); //  inverted exclamation mark
        ENTITIES.put(Pattern.compile("&#162;"), "&cent;"); //  cent sign
        ENTITIES.put(Pattern.compile("&#163;"), "&pound;"); //  pound sterling sign
        ENTITIES.put(Pattern.compile("&#164;"), "&curren;"); //  general currency sign
        ENTITIES.put(Pattern.compile("&#165;"), "&yen;"); //  yen sign
        ENTITIES.put(Pattern.compile("&#166;"), "&brvbar;"); //  broken (vertical) bar
        ENTITIES.put(Pattern.compile("&#167;"), "&sect;"); //  section sign
        ENTITIES.put(Pattern.compile("&#168;"), "&uml;"); //  umlaut (dieresis)
        ENTITIES.put(Pattern.compile("&#169;"), "&copy;"); //  copyright sign
        ENTITIES.put(Pattern.compile("&#170;"), "&ordf;"); //  ordinal indicator, feminine
        ENTITIES.put(Pattern.compile("&#171;"), "&laquo;"); //  angle quotation mark, left
        ENTITIES.put(Pattern.compile("&#172;"), "&not;"); //  not sign
        ENTITIES.put(Pattern.compile("&#173;"), "&shy;"); //  soft hyphen
        ENTITIES.put(Pattern.compile("&#174;"), "&reg;"); //  registered sign
        ENTITIES.put(Pattern.compile("&#175;"), "&macr;"); //  macron
        ENTITIES.put(Pattern.compile("&#176;"), "&deg;"); //  degree sign
        ENTITIES.put(Pattern.compile("&#177;"), "&plusmn;"); //  plus-or-minus sign
        ENTITIES.put(Pattern.compile("&#178;"), "&sup2;"); //  superscript two
        ENTITIES.put(Pattern.compile("&#179;"), "&sup3;"); //  superscript three
        ENTITIES.put(Pattern.compile("&#180;"), "&acute;"); //  acute accent
        ENTITIES.put(Pattern.compile("&#181;"), "&micro;"); //  micro sign
        ENTITIES.put(Pattern.compile("&#182;"), "&para;"); //  pilcrow (paragraph sign)
        ENTITIES.put(Pattern.compile("&#183;"), "&middot;"); //  middle dot
        ENTITIES.put(Pattern.compile("&#184;"), "&cedil;"); //  cedilla
        ENTITIES.put(Pattern.compile("&#185;"), "&sup1;"); //  superscript one
        ENTITIES.put(Pattern.compile("&#186;"), "&ordm;"); //  ordinal indicator, masculine
        ENTITIES.put(Pattern.compile("&#187;"), "&raquo;"); //  angle quotation mark, right
        ENTITIES.put(Pattern.compile("&#188;"), "&frac14;"); //  fraction one-quarter
        ENTITIES.put(Pattern.compile("&#189;"), "&frac12;"); //  fraction one-half
        ENTITIES.put(Pattern.compile("&#190;"), "&frac34;"); //  fraction three-quarters
        ENTITIES.put(Pattern.compile("&#191;"), "&iquest;"); //  inverted question mark
        ENTITIES.put(Pattern.compile("&#192;"), "&Agrave;"); //  capital A, grave accent
        ENTITIES.put(Pattern.compile("&#193;"), "&Aacute;"); //  capital A, acute accent
        ENTITIES.put(Pattern.compile("&#194;"), "&Acirc;"); //  capital A, circumflex accent
        ENTITIES.put(Pattern.compile("&#195;"), "&Atilde;"); //  capital A, tilde
        ENTITIES.put(Pattern.compile("&#196;"), "&Auml;"); //  capital A, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#197;"), "&Aring;"); //  capital A, ring
        ENTITIES.put(Pattern.compile("&#198;"), "&AElig;"); //  capital AE diphthong (ligature)
        ENTITIES.put(Pattern.compile("&#199;"), "&Ccedil;"); //  capital C, cedilla
        ENTITIES.put(Pattern.compile("&#200;"), "&Egrave;"); //  capital E, grave accent
        ENTITIES.put(Pattern.compile("&#201;"), "&Eacute;"); //  capital E, acute accent
        ENTITIES.put(Pattern.compile("&#202;"), "&Ecirc;"); //  capital E, circumflex accent
        ENTITIES.put(Pattern.compile("&#203;"), "&Euml;"); //  capital E, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#204;"), "&Igrave;"); //  capital I, grave accent
        ENTITIES.put(Pattern.compile("&#205;"), "&Iacute;"); //  capital I, acute accent
        ENTITIES.put(Pattern.compile("&#206;"), "&Icirc;"); //  capital I, circumflex accent
        ENTITIES.put(Pattern.compile("&#207;"), "&Iuml;"); //  capital I, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#208;"), "&ETH;"); //  capital Eth, Icelandic
        ENTITIES.put(Pattern.compile("&#209;"), "&Ntilde;"); //  capital N, tilde
        ENTITIES.put(Pattern.compile("&#210;"), "&Ograve;"); //  capital O, grave accent
        ENTITIES.put(Pattern.compile("&#211;"), "&Oacute;"); //  capital O, acute accent
        ENTITIES.put(Pattern.compile("&#212;"), "&Ocirc;"); //  capital O, circumflex accent
        ENTITIES.put(Pattern.compile("&#213;"), "&Otilde;"); //  capital O, tilde
        ENTITIES.put(Pattern.compile("&#214;"), "&Ouml;"); //  capital O, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#215;"), "&times;"); //  multiply sign
        ENTITIES.put(Pattern.compile("&#216;"), "&Oslash;"); //  capital O, slash
        ENTITIES.put(Pattern.compile("&#217;"), "&Ugrave;"); //  capital U, grave accent
        ENTITIES.put(Pattern.compile("&#218;"), "&Uacute;"); //  capital U, acute accent
        ENTITIES.put(Pattern.compile("&#219;"), "&Ucirc;"); //  capital U, circumflex accent
        ENTITIES.put(Pattern.compile("&#220;"), "&Uuml;"); //  capital U, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#221;"), "&Yacute;"); //  capital Y, acute accent
        ENTITIES.put(Pattern.compile("&#222;"), "&THORN;"); //  capital THORN, Icelandic
        ENTITIES.put(Pattern.compile("&#223;"), "&szlig;"); //  small sharp s, German (sz ligature)
        ENTITIES.put(Pattern.compile("&#224;"), "&agrave;"); //  small a, grave accent
        ENTITIES.put(Pattern.compile("&#225;"), "&aacute;"); //  small a, acute accent
        ENTITIES.put(Pattern.compile("&#226;"), "&acirc;"); //  small a, circumflex accent
        ENTITIES.put(Pattern.compile("&#227;"), "&atilde;"); //  small a, tilde
        ENTITIES.put(Pattern.compile("&#228;"), "&auml;"); //  small a, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#229;"), "&aring;"); //  small a, ring
        ENTITIES.put(Pattern.compile("&#230;"), "&aelig;"); //  small ae diphthong (ligature)
        ENTITIES.put(Pattern.compile("&#231;"), "&ccedil;"); //  small c, cedilla
        ENTITIES.put(Pattern.compile("&#232;"), "&egrave;"); //  small e, grave accent
        ENTITIES.put(Pattern.compile("&#233;"), "&eacute;"); //  small e, acute accent
        ENTITIES.put(Pattern.compile("&#234;"), "&ecirc;"); //  small e, circumflex accent
        ENTITIES.put(Pattern.compile("&#235;"), "&euml;"); //  small e, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#236;"), "&igrave;"); //  small i, grave accent
        ENTITIES.put(Pattern.compile("&#237;"), "&iacute;"); //  small i, acute accent
        ENTITIES.put(Pattern.compile("&#238;"), "&icirc;"); //  small i, circumflex accent
        ENTITIES.put(Pattern.compile("&#239;"), "&iuml;"); //  small i, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#240;"), "&eth;"); //  small eth, Icelandic
        ENTITIES.put(Pattern.compile("&#241;"), "&ntilde;"); //  small n, tilde
        ENTITIES.put(Pattern.compile("&#242;"), "&ograve;"); //  small o, grave accent
        ENTITIES.put(Pattern.compile("&#243;"), "&oacute;"); //  small o, acute accent
        ENTITIES.put(Pattern.compile("&#244;"), "&ocirc;"); //  small o, circumflex accent
        ENTITIES.put(Pattern.compile("&#245;"), "&otilde;"); //  small o, tilde
        ENTITIES.put(Pattern.compile("&#246;"), "&ouml;"); //  small o, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#247;"), "&divide;"); //  divide sign
        ENTITIES.put(Pattern.compile("&#248;"), "&oslash;"); //  small o, slash
        ENTITIES.put(Pattern.compile("&#249;"), "&ugrave;"); //  small u, grave accent
        ENTITIES.put(Pattern.compile("&#250;"), "&uacute;"); //  small u, acute accent
        ENTITIES.put(Pattern.compile("&#251;"), "&ucirc;"); //  small u, circumflex accent
        ENTITIES.put(Pattern.compile("&#252;"), "&uuml;"); //  small u, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#253;"), "&yacute;"); //  small y, acute accent
        ENTITIES.put(Pattern.compile("&#254;"), "&thorn;"); //  small thorn, Icelandic
        ENTITIES.put(Pattern.compile("&#255;"), "&yuml;"); //  small y, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#263;"), "&cacute;");
        //  Latin Extended-A
        ENTITIES.put(Pattern.compile("&#338;"), "&OElig;"); //  latin capital ligature oe, u+0152 ISOlat2
        ENTITIES.put(Pattern.compile("&#339;"), "&oelig;"); //  latin small ligature oe, u+0153 ISOlat2
        ENTITIES.put(Pattern.compile("&#352;"), "&Scaron;"); //  latin capital letter s with caron, u+0160 ISOlat2
        ENTITIES.put(Pattern.compile("&#353;"), "&scaron;"); //  latin small letter s with caron, u+0161 ISOlat2
        ENTITIES.put(Pattern.compile("&#376;"), "&Yuml;"); //  latin capital letter y with diaeresis, u+0178 ISOlat2
        //  Latin Extended-B
        ENTITIES.put(Pattern.compile("&#402;"), "&fnof;"); //  latin small f with hook, =function, =florin, u+0192 ISOtech
        //  Spacing Modifier Letters
        ENTITIES.put(Pattern.compile("&#710;"), "&circ;"); //  modifier letter circumflex accent, u+02C6 ISOpub
        ENTITIES.put(Pattern.compile("&#732;"), "&tilde;"); //  small tilde, u+02DC ISOdia
        //  Greek
        ENTITIES.put(Pattern.compile("&#913;"), "&Alpha;"); //  greek capital letter alpha,  u+0391
        ENTITIES.put(Pattern.compile("&#914;"), "&Beta;"); //  greek capital letter beta,  u+0392
        ENTITIES.put(Pattern.compile("&#915;"), "&Gamma;"); //  greek capital letter gamma,  u+0393 ISOgrk3
        ENTITIES.put(Pattern.compile("&#916;"), "&Delta;"); //  greek capital letter delta,  u+0394 ISOgrk3
        ENTITIES.put(Pattern.compile("&#917;"), "&Epsilon;"); //  greek capital letter epsilon,  u+0395
        ENTITIES.put(Pattern.compile("&#918;"), "&Zeta;"); //  greek capital letter zeta,  u+0396
        ENTITIES.put(Pattern.compile("&#919;"), "&Eta;"); //  greek capital letter eta,  u+0397
        ENTITIES.put(Pattern.compile("&#920;"), "&Theta;"); //  greek capital letter theta,  u+0398 ISOgrk3
        ENTITIES.put(Pattern.compile("&#921;"), "&Iota;"); //  greek capital letter iota,  u+0399
        ENTITIES.put(Pattern.compile("&#922;"), "&Kappa;"); //  greek capital letter kappa,  u+039A
        ENTITIES.put(Pattern.compile("&#923;"), "&Lambda;"); //  greek capital letter lambda,  u+039B ISOgrk3
        ENTITIES.put(Pattern.compile("&#924;"), "&Mu;"); //  greek capital letter mu,  u+039C
        ENTITIES.put(Pattern.compile("&#925;"), "&Nu;"); //  greek capital letter nu,  u+039D
        ENTITIES.put(Pattern.compile("&#926;"), "&Xi;"); //  greek capital letter xi,  u+039E ISOgrk3
        ENTITIES.put(Pattern.compile("&#927;"), "&Omicron;"); //  greek capital letter omicron,  u+039F
        ENTITIES.put(Pattern.compile("&#928;"), "&Pi;"); //  greek capital letter pi,  u+03A0 ISOgrk3
        ENTITIES.put(Pattern.compile("&#929;"), "&Rho;"); //  greek capital letter rho,  u+03A1
        ENTITIES.put(Pattern.compile("&#931;"), "&Sigma;"); //  greek capital letter sigma,  u+03A3 ISOgrk3
        ENTITIES.put(Pattern.compile("&#932;"), "&Tau;"); //  greek capital letter tau,  u+03A4
        ENTITIES.put(Pattern.compile("&#933;"), "&Upsilon;"); //  greek capital letter upsilon,  u+03A5 ISOgrk3
        ENTITIES.put(Pattern.compile("&#934;"), "&Phi;"); //  greek capital letter phi,  u+03A6 ISOgrk3
        ENTITIES.put(Pattern.compile("&#935;"), "&Chi;"); //  greek capital letter chi,  u+03A7
        ENTITIES.put(Pattern.compile("&#936;"), "&Psi;"); //  greek capital letter psi,  u+03A8 ISOgrk3
        ENTITIES.put(Pattern.compile("&#937;"), "&Omega;"); //  greek capital letter omega,  u+03A9 ISOgrk3
        ENTITIES.put(Pattern.compile("&#945;"), "&alpha;"); //  greek small letter alpha, u+03B1 ISOgrk3
        ENTITIES.put(Pattern.compile("&#946;"), "&beta;"); //  greek small letter beta,  u+03B2 ISOgrk3
        ENTITIES.put(Pattern.compile("&#947;"), "&gamma;"); //  greek small letter gamma,  u+03B3 ISOgrk3
        ENTITIES.put(Pattern.compile("&#948;"), "&delta;"); //  greek small letter delta,  u+03B4 ISOgrk3
        ENTITIES.put(Pattern.compile("&#949;"), "&epsilon;"); //  greek small letter epsilon,  u+03B5 ISOgrk3
        ENTITIES.put(Pattern.compile("&#950;"), "&zeta;"); //  greek small letter zeta,  u+03B6 ISOgrk3
        ENTITIES.put(Pattern.compile("&#951;"), "&eta;"); //  greek small letter eta,  u+03B7 ISOgrk3
        ENTITIES.put(Pattern.compile("&#952;"), "&theta;"); //  greek small letter theta,  u+03B8 ISOgrk3
        ENTITIES.put(Pattern.compile("&#953;"), "&iota;"); //  greek small letter iota,  u+03B9 ISOgrk3
        ENTITIES.put(Pattern.compile("&#954;"), "&kappa;"); //  greek small letter kappa,  u+03BA ISOgrk3
        ENTITIES.put(Pattern.compile("&#955;"), "&lambda;"); //  greek small letter lambda,  u+03BB ISOgrk3
        ENTITIES.put(Pattern.compile("&#956;"), "&mu;"); //  greek small letter mu,  u+03BC ISOgrk3
        ENTITIES.put(Pattern.compile("&#957;"), "&nu;"); //  greek small letter nu,  u+03BD ISOgrk3
        ENTITIES.put(Pattern.compile("&#958;"), "&xi;"); //  greek small letter xi,  u+03BE ISOgrk3
        ENTITIES.put(Pattern.compile("&#959;"), "&omicron;"); //  greek small letter omicron,  u+03BF NEW
        ENTITIES.put(Pattern.compile("&#960;"), "&pi;"); //  greek small letter pi,  u+03C0 ISOgrk3
        ENTITIES.put(Pattern.compile("&#961;"), "&rho;"); //  greek small letter rho,  u+03C1 ISOgrk3
        ENTITIES.put(Pattern.compile("&#962;"), "&sigmaf;"); //  greek small letter final sigma,  u+03C2 ISOgrk3
        ENTITIES.put(Pattern.compile("&#963;"), "&sigma;"); //  greek small letter sigma,  u+03C3 ISOgrk3
        ENTITIES.put(Pattern.compile("&#964;"), "&tau;"); //  greek small letter tau,  u+03C4 ISOgrk3
        ENTITIES.put(Pattern.compile("&#965;"), "&upsilon;"); //  greek small letter upsilon,  u+03C5 ISOgrk3
        ENTITIES.put(Pattern.compile("&#966;"), "&phi;"); //  greek small letter phi,  u+03C6 ISOgrk3
        ENTITIES.put(Pattern.compile("&#967;"), "&chi;"); //  greek small letter chi,  u+03C7 ISOgrk3
        ENTITIES.put(Pattern.compile("&#968;"), "&psi;"); //  greek small letter psi,  u+03C8 ISOgrk3
        ENTITIES.put(Pattern.compile("&#969;"), "&omega;"); //  greek small letter omega,  u+03C9 ISOgrk3
        ENTITIES.put(Pattern.compile("&#977;"), "&thetasym;"); //  greek small letter theta symbol,  u+03D1 NEW
        ENTITIES.put(Pattern.compile("&#978;"), "&upsih;"); //  greek upsilon with hook symbol,  u+03D2 NEW
        ENTITIES.put(Pattern.compile("&#982;"), "&piv;"); //  greek pi symbol,  u+03D6 ISOgrk3
        // General punctuation
        ENTITIES.put(Pattern.compile("&#8194;"), "&ensp;"); //  en space, u+2002 ISOpub
        ENTITIES.put(Pattern.compile("&#8195;"), "&emsp;"); //  em space, u+2003 ISOpub
        ENTITIES.put(Pattern.compile("&#8201;"), "&thinsp;"); //  thin space, u+2009 ISOpub
        ENTITIES.put(Pattern.compile("&#8204;"), "&zwnj;"); //  zero width non-joiner, u+200C NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8205;"), "&zwj;"); //  zero width joiner, u+200D NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8206;"), "&lrm;"); //  left-to-right mark, u+200E NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8207;"), "&rlm;"); //  right-to-left mark, u+200F NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8211;"), "&ndash;"); //  en dash, u+2013 ISOpub
        ENTITIES.put(Pattern.compile("&#8212;"), "&mdash;"); //  em dash, u+2014 ISOpub
        ENTITIES.put(Pattern.compile("&#8216;"), "&lsquo;"); //  left single quotation mark, u+2018 ISOnum
        ENTITIES.put(Pattern.compile("&#8217;"), "&rsquo;"); //  right single quotation mark, u+2019 ISOnum
        ENTITIES.put(Pattern.compile("&#8218;"), "&sbquo;"); //  single low-9 quotation mark, u+201A NEW
        ENTITIES.put(Pattern.compile("&#8220;"), "&ldquo;"); //  left double quotation mark, u+201C ISOnum
        ENTITIES.put(Pattern.compile("&#8221;"), "&rdquo;"); //  right double quotation mark, u+201D ISOnum
        ENTITIES.put(Pattern.compile("&#8222;"), "&bdquo;"); //  double low-9 quotation mark, u+201E NEW
        ENTITIES.put(Pattern.compile("&#8224;"), "&dagger;"); //  dagger, u+2020 ISOpub
        ENTITIES.put(Pattern.compile("&#8225;"), "&Dagger;"); //  double dagger, u+2021 ISOpub
        ENTITIES.put(Pattern.compile("&#8226;"), "&bull;"); //  bullet, =black small circle, u+2022 ISOpub
        ENTITIES.put(Pattern.compile("&#8230;"), "&hellip;");
        ENTITIES.put(Pattern.compile("&#8240;"), "&permil;"); //  per mille sign, u+2030 ISOtech
        ENTITIES.put(Pattern.compile("&#8242;"), "&prime;"); //  prime, =minutes, =feet, u+2032 ISOtech
        ENTITIES.put(Pattern.compile("&#8243;"), "&Prime;"); //  double prime, =seconds, =inches, u+2033 ISOtech
        ENTITIES.put(Pattern.compile("&#8249;"), "&lsaquo;"); //  single left-pointing angle quotation mark, u+2039 ISO proposed
        ENTITIES.put(Pattern.compile("&#8250;"), "&rsaquo;"); //  single right-pointing angle quotation mark, u+203A ISO proposed
        ENTITIES.put(Pattern.compile("&#8254;"), "&oline;"); //  overline, =spacing overscore, u+203E NEW
        ENTITIES.put(Pattern.compile("&#8260;"), "&frasl;"); //  fraction slash, u+2044 NEW
        ENTITIES.put(Pattern.compile("&#8364;"), "&euro;");
        //  Letterlike Symbols
        ENTITIES.put(Pattern.compile("&#8472;"), "&weierp;"); //  script capital P, =power set, =Weierstrass p, u+2118 ISOamso
        ENTITIES.put(Pattern.compile("&#8465;"), "&image;"); //  blackletter capital I, =imaginary part, u+2111 ISOamso
        ENTITIES.put(Pattern.compile("&#8476;"), "&real;"); //  blackletter capital R, =real part symbol, u+211C ISOamso
        ENTITIES.put(Pattern.compile("&#8482;"), "&trade;"); //  trade mark sign, u+2122 ISOnum
        ENTITIES.put(Pattern.compile("&#8501;"), "&alefsym;"); //  alef symbol, =first transfinite cardinal, u+2135 NEW
        //  Arrows
        ENTITIES.put(Pattern.compile("&#8592;"), "&larr;"); //  leftwards arrow, u+2190 ISOnum
        ENTITIES.put(Pattern.compile("&#8593;"), "&uarr;"); //  upwards arrow, u+2191 ISOnum
        ENTITIES.put(Pattern.compile("&#8594;"), "&rarr;"); //  rightwards arrow, u+2192 ISOnum
        ENTITIES.put(Pattern.compile("&#8595;"), "&darr;"); //  downwards arrow, u+2193 ISOnum
        ENTITIES.put(Pattern.compile("&#8596;"), "&harr;"); //  left right arrow, u+2194 ISOamsa
        ENTITIES.put(Pattern.compile("&#8629;"), "&crarr;"); //  downwards arrow with corner leftwards, =carriage return, u+21B5 NEW
        ENTITIES.put(Pattern.compile("&#8656;"), "&lArr;"); //  leftwards double arrow, u+21D0 ISOtech
        ENTITIES.put(Pattern.compile("&#8657;"), "&uArr;"); //  upwards double arrow, u+21D1 ISOamsa
        ENTITIES.put(Pattern.compile("&#8658;"), "&rArr;"); //  rightwards double arrow, u+21D2 ISOtech
        ENTITIES.put(Pattern.compile("&#8659;"), "&dArr;"); //  downwards double arrow, u+21D3 ISOamsa
        ENTITIES.put(Pattern.compile("&#8660;"), "&hArr;"); //  left right double arrow, u+21D4 ISOamsa
        //  Mathematical Operators
        ENTITIES.put(Pattern.compile("&#8704;"), "&forall;"); //  for all, u+2200 ISOtech
        ENTITIES.put(Pattern.compile("&#8706;"), "&part;"); //  partial differential, u+2202 ISOtech
        ENTITIES.put(Pattern.compile("&#8707;"), "&exist;"); //  there exists, u+2203 ISOtech
        ENTITIES.put(Pattern.compile("&#8709;"), "&empty;"); //  empty set, =null set, =diameter, u+2205 ISOamso
        ENTITIES.put(Pattern.compile("&#8711;"), "&nabla;"); //  nabla, =backward difference, u+2207 ISOtech
        ENTITIES.put(Pattern.compile("&#8712;"), "&isin;"); //  element of, u+2208 ISOtech
        ENTITIES.put(Pattern.compile("&#8713;"), "&notin;"); //  not an element of, u+2209 ISOtech
        ENTITIES.put(Pattern.compile("&#8715;"), "&ni;"); //  contains as member, u+220B ISOtech
        ENTITIES.put(Pattern.compile("&#8719;"), "&prod;"); //  n-ary product, =product sign, u+220F ISOamsb
        ENTITIES.put(Pattern.compile("&#8721;"), "&sum;"); //  n-ary sumation, u+2211 ISOamsb
        ENTITIES.put(Pattern.compile("&#8722;"), "&minus;"); //  minus sign, u+2212 ISOtech
        ENTITIES.put(Pattern.compile("&#8727;"), "&lowast;"); //  asterisk operator, u+2217 ISOtech
        ENTITIES.put(Pattern.compile("&#8730;"), "&radic;"); //  square root, =radical sign, u+221A ISOtech
        ENTITIES.put(Pattern.compile("&#8733;"), "&prop;"); //  proportional to, u+221D ISOtech
        ENTITIES.put(Pattern.compile("&#8734;"), "&infin;"); //  infinity, u+221E ISOtech
        ENTITIES.put(Pattern.compile("&#8736;"), "&ang;"); //  angle, u+2220 ISOamso
        ENTITIES.put(Pattern.compile("&#8869;"), "&and;"); //  logical and, =wedge, u+2227 ISOtech
        ENTITIES.put(Pattern.compile("&#8870;"), "&or;"); //  logical or, =vee, u+2228 ISOtech
        ENTITIES.put(Pattern.compile("&#8745;"), "&cap;"); //  intersection, =cap, u+2229 ISOtech
        ENTITIES.put(Pattern.compile("&#8746;"), "&cup;"); //  union, =cup, u+222A ISOtech
        ENTITIES.put(Pattern.compile("&#8747;"), "&int;"); //  integral, u+222B ISOtech
        ENTITIES.put(Pattern.compile("&#8756;"), "&there4;"); //  therefore, u+2234 ISOtech
        ENTITIES.put(Pattern.compile("&#8764;"), "&sim;"); //  tilde operator, =varies with, =similar to, u+223C ISOtech
        ENTITIES.put(Pattern.compile("&#8773;"), "&cong;"); //  approximately equal to, u+2245 ISOtech
        ENTITIES.put(Pattern.compile("&#8776;"), "&asymp;"); //  almost equal to, =asymptotic to, u+2248 ISOamsr
        ENTITIES.put(Pattern.compile("&#8800;"), "&ne;"); //  not equal to, u+2260 ISOtech
        ENTITIES.put(Pattern.compile("&#8801;"), "&equiv;"); //  identical to, u+2261 ISOtech
        ENTITIES.put(Pattern.compile("&#8804;"), "&le;"); //  less-than or equal to, u+2264 ISOtech
        ENTITIES.put(Pattern.compile("&#8805;"), "&ge;"); //  greater-than or equal to, u+2265 ISOtech
        ENTITIES.put(Pattern.compile("&#8834;"), "&sub;"); //  subset of, u+2282 ISOtech
        ENTITIES.put(Pattern.compile("&#8835;"), "&sup;"); //  superset of, u+2283 ISOtech
        ENTITIES.put(Pattern.compile("&#8836;"), "&nsub;"); //  not a subset of, u+2284 ISOamsn
        ENTITIES.put(Pattern.compile("&#8838;"), "&sube;"); //  subset of or equal to, u+2286 ISOtech
        ENTITIES.put(Pattern.compile("&#8839;"), "&supe;"); //  superset of or equal to, u+2287 ISOtech
        ENTITIES.put(Pattern.compile("&#8853;"), "&oplus;"); //  circled plus, =direct sum, u+2295 ISOamsb
        ENTITIES.put(Pattern.compile("&#8855;"), "&otimes;"); //  circled times, =vector product, u+2297 ISOamsb
        ENTITIES.put(Pattern.compile("&#8869;"), "&perp;"); //  up tack, =orthogonal to, =perpendicular, u+22A5 ISOtech
        ENTITIES.put(Pattern.compile("&#8901;"), "&sdot;"); //  dot operator, u+22C5 ISOamsb
        //  Miscellaneous Technical
        ENTITIES.put(Pattern.compile("&#8968;"), "&lceil;"); //  left ceiling, =apl upstile, u+2308, ISOamsc
        ENTITIES.put(Pattern.compile("&#8969;"), "&rceil;"); //  right ceiling, u+2309, ISOamsc
        ENTITIES.put(Pattern.compile("&#8970;"), "&lfloor;"); //  left floor, =apl downstile, u+230A, ISOamsc
        ENTITIES.put(Pattern.compile("&#8971;"), "&rfloor;"); //  right floor, u+230B, ISOamsc
        ENTITIES.put(Pattern.compile("&#9001;"), "&lang;"); //  left-pointing angle bracket, =bra, u+2329 ISOtech
        ENTITIES.put(Pattern.compile("&#9002;"), "&rang;"); //  right-pointing angle bracket, =ket, u+232A ISOtech
        //  Geometric Shapes
        ENTITIES.put(Pattern.compile("&#9674;"), "&loz;"); //  lozenge, u+25CA ISOpub
        //  Miscellaneous Symbols
        ENTITIES.put(Pattern.compile("&#9824;"), "&spades;"); //  black spade suit, u+2660 ISOpub
        ENTITIES.put(Pattern.compile("&#9827;"), "&clubs;"); //  black club suit, =shamrock, u+2663 ISOpub
        ENTITIES.put(Pattern.compile("&#9829;"), "&hearts;"); //  black heart suit, =valentine, u+2665 ISOpub
        ENTITIES.put(Pattern.compile("&#9830;"), "&diams;"); //  black diamond suit, u+2666 ISOpub
        // Other entities
        ENTITIES.put(Pattern.compile("&Aacute;"), "Á");
        ENTITIES.put(Pattern.compile("&Agrave;"), "À");
        ENTITIES.put(Pattern.compile("&Acirc;"), "Â");
        ENTITIES.put(Pattern.compile("&Auml;"), "Ä");
        ENTITIES.put(Pattern.compile("&Aring;"), "Å");
        ENTITIES.put(Pattern.compile("&AElig;"), "Æ");
        ENTITIES.put(Pattern.compile("&aacute;"), "á");
        ENTITIES.put(Pattern.compile("&agrave;"), "à");
        ENTITIES.put(Pattern.compile("&acirc;"), "â");
        ENTITIES.put(Pattern.compile("&auml;"), "ä");
        ENTITIES.put(Pattern.compile("&aring;"), "å");
        ENTITIES.put(Pattern.compile("&aelig;"), "æ");
        ENTITIES.put(Pattern.compile("&Eacute;"), "É");
        ENTITIES.put(Pattern.compile("&Egrave;"), "È");
        ENTITIES.put(Pattern.compile("&Ecirc;"), "Ê");
        ENTITIES.put(Pattern.compile("&Euml;"), "Ë");
        ENTITIES.put(Pattern.compile("&eacute;"), "é");
        ENTITIES.put(Pattern.compile("&egrave;"), "è");
        ENTITIES.put(Pattern.compile("&ecirc;"), "ê");
        ENTITIES.put(Pattern.compile("&euml;"), "ë");
        ENTITIES.put(Pattern.compile("&Iacute;"), "Í");
        ENTITIES.put(Pattern.compile("&Igrave;"), "Ì");
        ENTITIES.put(Pattern.compile("&Icirc;"), "Î");
        ENTITIES.put(Pattern.compile("&Iuml;"), "Ï");
        ENTITIES.put(Pattern.compile("&iacute;"), "í");
        ENTITIES.put(Pattern.compile("&igrave;"), "ì");
        ENTITIES.put(Pattern.compile("&icirc;"), "î");
        ENTITIES.put(Pattern.compile("&iuml;"), "ï");
        ENTITIES.put(Pattern.compile("&Oacute;"), "Ó");
        ENTITIES.put(Pattern.compile("&Ograve;"), "Ò");
        ENTITIES.put(Pattern.compile("&Ocirc;"), "Ô");
        ENTITIES.put(Pattern.compile("&Ouml;"), "Ö");
        ENTITIES.put(Pattern.compile("&OElig;"), "Œ");
        ENTITIES.put(Pattern.compile("&oacute;"), "ó");
        ENTITIES.put(Pattern.compile("&ograve;"), "ò");
        ENTITIES.put(Pattern.compile("&ocirc;"), "ô");
        ENTITIES.put(Pattern.compile("&ouml;"), "ö");
        ENTITIES.put(Pattern.compile("&oelig;"), "œ");
        ENTITIES.put(Pattern.compile("&Uacute;"), "Ú");
        ENTITIES.put(Pattern.compile("&Ugrave;"), "Ù");
        ENTITIES.put(Pattern.compile("&Ucirc;"), "Û");
        ENTITIES.put(Pattern.compile("&Uuml;"), "Ü");
        ENTITIES.put(Pattern.compile("&uacute;"), "ú");
        ENTITIES.put(Pattern.compile("&ugrave;"), "ù");
        ENTITIES.put(Pattern.compile("&ucirc;"), "û");
        ENTITIES.put(Pattern.compile("&uuml;"), "ü");
        ENTITIES.put(Pattern.compile("&Ccedil;"), "Ç");
        ENTITIES.put(Pattern.compile("&Cacute;"), "Ć");
        ENTITIES.put(Pattern.compile("&ccedil;"), "ç");
        ENTITIES.put(Pattern.compile("&cacute;"), "ć");
        ENTITIES.put(Pattern.compile("&quot;"), "\"");
        ENTITIES.put(Pattern.compile("&nbsp;"), "\u00a0");
        ENTITIES.put(Pattern.compile("&rsquo;"), "’");
        ENTITIES.put(Pattern.compile("&lsquo;"), "‘");
        ENTITIES.put(Pattern.compile("&laquo;"), "«");
        ENTITIES.put(Pattern.compile("&raquo;"), "»");
        ENTITIES.put(Pattern.compile("&ldquo;"), "“");
        ENTITIES.put(Pattern.compile("&rdquo;"), "”");
        ENTITIES.put(Pattern.compile("&ndash;"), "\u2013");
        ENTITIES.put(Pattern.compile("&mdash;"), "\u2014");
        ENTITIES.put(Pattern.compile("&dagger;"), "†");
        ENTITIES.put(Pattern.compile("&hellip;"), "…");
        ENTITIES.put(Pattern.compile("&copy;"), "©");
        ENTITIES.put(Pattern.compile("&beta;"), "β");
        ENTITIES.put(Pattern.compile("&sect;"), "§");
        ENTITIES.put(Pattern.compile("&para;"), "¶");
        ENTITIES.put(Pattern.compile("&deg;"), "°");
    }

    private static String translateEntities(String document) {
        for (Map.Entry<Pattern, String> typo : ENTITIES.entrySet()) {
            document = typo.getKey().matcher(document).replaceAll(typo.getValue());
        }
        return document;
    }

    private static void writeToFile(String document, String file) throws IOException {
        document = document.replaceAll("(</tr>|</head>|</p>|</div>|<br />)\\s*", "$1\n");
//        document = document.replaceAll("\u00a0", "&nbsp;");
        Reader reader = new StringReader(document);
        new File(file).getParentFile().mkdirs();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        copy(reader, writer);
    }

    private static boolean isInside(Matcher matcher, int pos) {
        int start = Math.max(0, pos - 100);
        while (start < pos) {
            if (!matcher.find(start)) {
                return false;
            }
            if (matcher.start() <= pos && pos < matcher.end()) {
                return true;
            }
            start = matcher.end();
        }
        return false;
    }


    private static String fixBibleRefs(String document) {
        document = document.replaceAll("\\(cf\\. 1 Pierre,", "(cf. <i>1 Pierre</i>,");
        document = document.replaceAll("\\((cf\\. )?(([12] )?[A-Za-z]+\\.?)((\\s*[1-9][0-9]*[,.;-]?)+)\\s*\\)", "($1<i>$2</i>$4)");
        document = document.replaceAll("<i>(([12] )?[A-Za-zé]+)</i>\\.", "<i>$1.</i>");
        document = document.replaceAll("([12] )<i>([A-Za-zé]+\\.?)</i>", "<i>$1$2</i>");
        document = document.replaceAll("([12])<i>([A-Za-zé]+\\.?)</i>", "<i>$1 $2</i>");
        document = document.replaceAll("(\\((cf\\. )?<i>[^<]*?</i>,? [0-9]+, [0-9]+)\\.\\)", "$1).");
        return document;
    }

    private static String fixFootNotes(String document) {
        // Check the kind of foot notes we have so far
        boolean parenthesis = document.indexOf("(1)") >= 0 && document.indexOf("(2)") >= 0 && document.indexOf("(3)") >= 0;
        boolean brackets    = document.indexOf("[1]") >= 0 && document.indexOf("[2]") >= 0 && document.indexOf("[3]") >= 0;
        boolean link        = document.indexOf(">1</a>") >= 0 && document.indexOf(">2</a>") >= 0 && document.indexOf(">3</a>") >= 0;
        boolean linkBracket = document.indexOf(">[1]</a>") >= 0 && document.indexOf(">[2]</a>") >= 0 && document.indexOf(">[3]</a>") >= 0;
        // Add parentheses around plain numbers corresponding to foot notes calls
        // All consecutive numbers are considered excluding those in attributes and bible citations
        if (!parenthesis && !brackets && !link && !linkBracket) {
            StringBuilder newDoc = new StringBuilder();
            Matcher matcher = Pattern.compile("([\\s\u00a0])([1-9][0-9]*)([\\s\u00a0,.:;!<])").matcher(document);
            Matcher biblCit = Pattern.compile("\\((cf\\. )?(<i>[^<]*?</i>,? ([1-9][0-9]*[,.;-]?\\s*(s?s\\.\\s*)?)+)+\\)").matcher(document);
            Matcher attribute = Pattern.compile(" [a-z\\-]+=\".*?\"").matcher(document);
            int body = document.indexOf("<body");
            int hr = document.indexOf("<hr");
            int start = 0;
            int appstart = 0;
            int nb = 1;
            while (matcher.find(start)) {
                if (matcher.start() > body && (matcher.start() < hr || hr == -1)) {
                    boolean isInBibleCit = isInside(biblCit, matcher.start());
                    boolean isInAttribute = isInside(attribute, matcher.start());
                    if (!isInBibleCit && !isInAttribute && Integer.parseInt(matcher.group(2)) == nb) {
                        newDoc.append(document.substring(appstart, matcher.start()));
                        newDoc.append(matcher.group(1));
                        newDoc.append("(");
                        newDoc.append(matcher.group(2));
                        newDoc.append(")");
                        newDoc.append(matcher.group(3));
                        nb++;
                        appstart = matcher.end();
                    }
                }
                start = matcher.end();
            }
            newDoc.append(document.substring(appstart, document.length()));
            document = newDoc.toString();
        }
        // Fix foot notes
        document = document.replaceAll("\\(([12][0-9][0-9]|[1-9][0-9]|[1-9])\\)(?!([^<]*<br />[^<]*)*</span>)", "<a id=\"fnr$1\" href=\"#fn$1\">$1</a>");
        document = document.replaceAll("(<a [^>]*>)\\[([1-9][0-9]*)\\](</a>)", "$1$2$3");
        document = document.replaceAll("<font[^>]*><sup>(<a[^>]*>[1-9][0-9]*</a>)</sup></font>", "$1");
        document = document.replaceAll("<a[^>]*>\\(</a><a[^>]*>([1-9][0-9]*)</a>\\)", "<a href=\"#fn$1\">$1</a>");
        document = document.replaceAll("[\\[\\(]?<a[^>]*>([1-9][0-9]*)</a>[\\]\\)]?", "<span class=\"footnote\" id=\"fnr$1\"><a href=\"#fn$1\">$1</a></span>");
        document = document.replaceAll("\\s+(<span class=\"footnote\">.*?</span>)", "$1");
        document = document.replaceAll("([\\.,;]\\s*)(<span class=\"footnote\".*?</span>)", "$2$1");
        return document;
    }

    private static String fixNumberedParagraphs(String document) {
        boolean hasDot = contains(document, "<p[^>]*>7\\. ") && contains(document, "<p[^>]*>8\\. ") && contains(document, "<p[^>]*>9\\. ");
        boolean hasDash = contains(document, "<p[^>]*>7 - ") && contains(document, "<p[^>]*>8 - ") && contains(document, "<p[^>]*>9 - ");
        boolean hasLink = contains(document, "<a id=\"7\\.\">") && contains(document, "<a id=\"8\\.\">") && contains(document, "<a id=\"9\\.\">");
        if (hasDash) {
            document = document.replaceAll("<p>([1-9][0-9]*) - ", "<p><a class=\"numpara\" id=\"p$1\">$1.</a> ");
        } else if (hasDot) {
            document = document.replaceAll("(<p[^>]*>)([1-9][0-9]*)\\. ", "$1<a class=\"numpara\" id=\"p$2\">$2.</a> ");
        } else if (hasLink) {
            document = document.replaceAll("<a id=\"([1-9][0-9]*)\\.\">", "<a class=\"numpara\" id=\"p$1\">");
        }
        return document;
    }

    private static boolean contains(String document, String regexp) {
        Matcher matcher = Pattern.compile(regexp).matcher(document);
        return matcher.find();
    }

    private static int indexOf(String document, String regexp) {
        Matcher matcher = Pattern.compile(regexp).matcher(document);
        if (matcher.find()) {
            return matcher.start();
        } else {
            return -1;
        }
    }

    static final Map<Pattern, String> TYPOS;

    static {
        TYPOS = new LinkedHashMap<Pattern, String>();

        TYPOS.put(Pattern.compile("CHRETIEN"), "CHRÉTIEN");
        TYPOS.put(Pattern.compile("EGLISE"), "ÉGLISE");
        TYPOS.put(Pattern.compile("Eglise"), "Église");
        TYPOS.put(Pattern.compile("Ecriture"), "Écriture");
        TYPOS.put(Pattern.compile("Egypte"), "Égypte");
        TYPOS.put(Pattern.compile("Etant"), "Étant");
        TYPOS.put(Pattern.compile("Etat"), "État");
        TYPOS.put(Pattern.compile("Epoux"), "Époux");
        TYPOS.put(Pattern.compile("Eternel"), "Éternel");
        TYPOS.put(Pattern.compile("Etendant"), "Étendant");
        TYPOS.put(Pattern.compile("Evêque"), "Évêque");
        TYPOS.put(Pattern.compile("Elisabeth"), "Élisabeth");
        TYPOS.put(Pattern.compile("EVANGILE"), "ÉVANGILE");
        TYPOS.put(Pattern.compile("Evangile"), "Évangile");
        TYPOS.put(Pattern.compile("EVEQUE"), "ÉVÊQUE");
        TYPOS.put(Pattern.compile("PRETRES"), "PRÊTRES");
        TYPOS.put(Pattern.compile("ONZIEME"), "ONZIÈME");
        TYPOS.put(Pattern.compile("EVANGELISATION"), "ÉVANGÉLISATION");
        TYPOS.put(Pattern.compile("METHODE"), "MÉTHODE");
        TYPOS.put(Pattern.compile("PORTEE"), "PORTÉE");
        TYPOS.put(Pattern.compile("MILLENAIRE"), "MILLÉNAIRE");
        TYPOS.put(Pattern.compile("HERAUTS"), "HÉRAUTS");
        TYPOS.put(Pattern.compile("MERE"), "MÈRE");
        TYPOS.put(Pattern.compile("PERE"), "PÈRE");
        TYPOS.put(Pattern.compile("MEDIATION"), "MÉDIATION");
        TYPOS.put(Pattern.compile("MISERICORDE"), "MISÉRICORDE");
        TYPOS.put(Pattern.compile("HERITAGE"), "HÉRITAGE");
        TYPOS.put(Pattern.compile("MYSTERE"), "MYSTÈRE");
        TYPOS.put(Pattern.compile("VERITE"), "VÉRITÉ");
        TYPOS.put(Pattern.compile("IMPLANTERENT"), "IMPLANTÈRENT");
        TYPOS.put(Pattern.compile("Etre"), "Être");
        TYPOS.put(Pattern.compile("educateurs"), "éducateurs");
        TYPOS.put(Pattern.compile("GENERATION"), "GÉNÉRATION");
        TYPOS.put(Pattern.compile("HUMANAE"), "HUMANÆ");
        TYPOS.put(Pattern.compile("VITAE"), "VITÆ");
        TYPOS.put(Pattern.compile("CAELIBATUS"), "CÆLIBATUS");
        TYPOS.put(Pattern.compile("OEUVRE"), "ŒUVRE");

        TYPOS.put(Pattern.compile("Incarnation ed la"), "Incarnation de la");
        TYPOS.put(Pattern.compile("\\baue\\b"), "que");
        TYPOS.put(Pattern.compile("\\bI'histoire"), "l'histoire");
        TYPOS.put(Pattern.compile("\\bI'Eucharistie"), "l'Eucharistie");
        TYPOS.put(Pattern.compile("\\bI'[e\u00e9]ducation"), "l'éducation");
        TYPOS.put(Pattern.compile("\\bI'[E\u00c9]glise"), "l'Église");
        TYPOS.put(Pattern.compile("\\bA "), "À ");
        TYPOS.put(Pattern.compile("([\\.\u00ab]) A "), "$1 À ");
        TYPOS.put(Pattern.compile("\\bO "), "Ô ");
        TYPOS.put(Pattern.compile("([\\.\u00ab]) O "), "$1 Ô ");
        TYPOS.put(Pattern.compile("suismoi"), "suis-moi");
        TYPOS.put(Pattern.compile("L '"), "L'");

        TYPOS.put(Pattern.compile("p. MANNA"), "P. MANNA");
        TYPOS.put(Pattern.compile("transperc</i>é"), "transpercé</i>");

        TYPOS.put(Pattern.compile("\\.\\.\\."), "…");

        // Split words
        TYPOS.put(Pattern.compile("mul- tiples"), "multiples");
        TYPOS.put(Pattern.compile("té- nèbres"), "ténèbres");
        TYPOS.put(Pattern.compile("sup- primés"), "supprimés");
        TYPOS.put(Pattern.compile("per- sonnes"), "personnes");
        TYPOS.put(Pattern.compile("histo- rique"), "historique");
        TYPOS.put(Pattern.compile("con- formes"), "conformes");
        TYPOS.put(Pattern.compile("con- naître"), "connaître");
        TYPOS.put(Pattern.compile("soi- même"), "soi-même");
        TYPOS.put(Pattern.compile("pro- clament"), "proclament");
        TYPOS.put(Pattern.compile("pers- pectives"), "perspectives");
        TYPOS.put(Pattern.compile("rassasiez- vous"), "rassasiez-vous");
        TYPOS.put(Pattern.compile("re- cherches"), "recherches");
        TYPOS.put(Pattern.compile("fa- milles"), "familles");
        TYPOS.put(Pattern.compile("poli- tiques"), "politiques");
        TYPOS.put(Pattern.compile("déve- loppent"), "développent");
        TYPOS.put(Pattern.compile("incompa- rable"), "incomparable");
        TYPOS.put(Pattern.compile("dia- logue"), "dialogue");
        TYPOS.put(Pattern.compile("affermissez- vous"), "affermissez-vous");
        TYPOS.put(Pattern.compile("non- guerre"), "non-guerre");
        TYPOS.put(Pattern.compile("léni- nisme"), "léninisme");
        TYPOS.put(Pattern.compile("Tiers- Monde"), "Tiers-Monde");
        TYPOS.put(Pattern.compile("Quart- Monde"), "Quart-Monde");
        TYPOS.put(Pattern.compile("lui- même"), "lui-même");
        TYPOS.put(Pattern.compile("bien- être"), "bien-être");
        TYPOS.put(Pattern.compile("est- ce"), "est-ce");
        TYPOS.put(Pattern.compile("par- lantes"), "parlantes");
        TYPOS.put(Pattern.compile("hom- me"), "homme");
        TYPOS.put(Pattern.compile("elle- même"), "elle-même");
        TYPOS.put(Pattern.compile("eux- même"), "eux-même");
        TYPOS.put(Pattern.compile("vigou- reuse"), "vigoureuse");
        TYPOS.put(Pattern.compile("par- ticipant"), "participant");
        TYPOS.put(Pattern.compile("par- tout"), "partout");
        TYPOS.put(Pattern.compile("lui- même"), "lui-même");
        TYPOS.put(Pattern.compile("avant- goût"), "avant-goût");
        TYPOS.put(Pattern.compile("ren- contres"), "rencontres");
        TYPOS.put(Pattern.compile("pourrait- on"), "pourrait-on");
        TYPOS.put(Pattern.compile("Moyen- Âge"), "Moyen-Âge");
        TYPOS.put(Pattern.compile("y a- t[ -]il"), "y a-t-il");
        TYPOS.put(Pattern.compile("subis- sent"), "subissent");
        TYPOS.put(Pattern.compile("tech- niques"), "techniques");
        TYPOS.put(Pattern.compile("dia- logue"), "dialogue");
        TYPOS.put(Pattern.compile("Jésus- Christ"), "Jésus-Christ");
        TYPOS.put(Pattern.compile("ré- flexion"), "réflexion");
        TYPOS.put(Pattern.compile("sup- porteront"), "supporteront");
        TYPOS.put(Pattern.compile("est-à- dire"), "est-à-dire");
        TYPOS.put(Pattern.compile("com- portements"), "comportements");
        TYPOS.put(Pattern.compile("Ephè- se"), "Éphèse");
        TYPOS.put(Pattern.compile("ré- pondre"), "répondre");
        TYPOS.put(Pattern.compile("œcumé- nique"), "œcuménique");

        TYPOS.put(Pattern.compile("10, 16- 17"), "10, 16-17");
    }

    private static String fixTypos(String document) {
        for (Map.Entry<Pattern, String> typo : TYPOS.entrySet()) {
            document = typo.getKey().matcher(document).replaceAll(typo.getValue());
        }
        return document;
    }

    private static String fixWhitespaces(String document) {
        Matcher paragraph = Pattern.compile("<p[\\s\\S]*?</p>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(fixWhitespacesInParagraph(paragraph.group()));
            start = paragraph.end();
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    private static String fixWhitespacesInParagraph(String text) {
        text = text.replaceAll("\n", " ");
        text = text.replaceAll("(\\s+)(</[^>]*>)", "$2$1");
        text = text.replaceAll("«\\s*", "«\u00A0");
        text = text.replaceAll("\\s*»", "\u00A0»");
        text = text.replaceAll("“\\s*", "“");
        text = text.replaceAll("\\s*”", "”");
        text = text.replaceAll("\\s*:\\s*", "\u00A0: ");
        text = text.replaceAll("\\s*;\\s*", "\u00A0; ");
        text = text.replaceAll("\\s*!\\s*", "\u00A0! ");
        text = text.replaceAll("\\s*\\?\\s*", "\u00A0? ");
        text = text.replaceAll("\\(\\s*", "(");
        text = text.replaceAll("\\s*\\)", ")");
        text = text.replaceAll("\\s*\\.([^<])", ". $1");
        text = text.replaceAll("\\s*,\\s*", ", ");
        text = text.replaceAll("( *\u00A0 *)( *\u00A0 *)*", "\u00A0");
        text = text.replaceAll(" +", " ");
        text = text.replaceAll("([\\s\u00A0])-([\\s\u00a0,])", "$1\u2014$2");

        // Fix back broken entities
        text = text.replaceAll("&([A-Za-z]+)\u00a0;", "&$1;");
        return text;
    }

    private static void copy(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[4096];
        int len;
        while ((len = reader.read(buf)) > 0) {
            writer.write(buf, 0, len);
        }
        reader.close();
        writer.close();
    }

    private static String getFull(String name) {
        if ("Benoît XVI".equals(name)) {
            return "benedict_xvi";
        } else if ("Jean-Paul II".equals(name)) {
            return "john_paul_ii";
        } else if ("Paul VI".equals(name)) {
            return "paul_vi";
        }
        throw new IllegalStateException("Unknown: " + name);
    }

}
