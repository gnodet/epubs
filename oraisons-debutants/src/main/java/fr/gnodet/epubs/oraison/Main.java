package fr.gnodet.epubs.oraison;

import fr.gnodet.epubs.core.Cover;
import org.mozilla.universalchardet.UniversalDetector;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.IOUtil.loadTextContent;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;
import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Quotes.fixQuotesInParagraph;
import static fr.gnodet.epubs.core.Tidy.translateEntities;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespaces;

public class Main {

    static final javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String[] args) throws Exception {

        dbf.setNamespaceAware(true);

//        Document enclist = dbf.newDocumentBuilder().parse(Main.class.getResourceAsStream("list.xml"));
//        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");
//        for (int i = 0; i < books.getLength(); i++) {
//        for (int i = 17; i < books.getLength(); i++) {
//        int i = 19; {
//            Element book = (Element) books.item(i);
            String file = "les-oraisons-des-debutants.html";
            String filename = "les-oraisons-des-débutants";
            String title = "Les oraisons des débutants";
            String subtitle = "Initiation à la pratique de l’Oraison";
            String creator = "P. Marie-Eugène de l'Enfant-Jésus O.C.D.";
//            String date = book.getAttribute("date");
//            String type = book.getAttribute("type");
            URL url = Main.class.getResource("/les-oraisons-des-debutants.html");
            try {
                process(url, "target/cache/" + file, "target/html/" + file);
                Map<String, byte[]> resources = new HashMap<String, byte[]>();
                byte[] coverData = Cover.generateCoverPng(0.3,
                        creator,
                        title,
                        subtitle,
                        null,
                        Main.class.getResource("papacy.svg"));
                writeToFile(coverData, "target/site/images/" + filename + ".png");
                resources.put("OEBPS/img/cover.png", coverData);
                resources.put("OEBPS/cover.html",
                        Cover.generateCoverHtml(creator, title, title, "").getBytes());
                createEpub(new File[] { new File("target/html/" + file) },
                           resources,
                           new File("target/site/epub/" + filename + ".epub"),
                           title,
                           creator,
                           null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
//        }
    }

    private static void process(URL url, String cache, String output) throws Exception {
        System.out.println("Processing: " + url);

        // Load URL text content
        String document = loadTextContent(url, cache, "UTF-8");
        FileInputStream fis = new FileInputStream(cache);
        byte[] buf = new byte[32 * 1024];
        UniversalDetector detector = new UniversalDetector(null);
        for (;;) {
            int l = fis.read(buf);
            if (l < 0) {
                break;
            }
            detector.handleData(buf, 0, l);
        }
        detector.dataEnd();
        document = loadTextContent(url, cache, detector.getDetectedCharset());

        // Add doctype if not present
        if (!document.startsWith("<!DOCTYPE")) {
            document = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + document;
        }

        // Fix bad html
        document = document.replaceAll("<p align=\"left\"<a", "<p align=\"left\"><a");
        // Delete office elements
        document = document.replaceAll("<o:p>[\\S\\s]*?</o:p>", "");
        // Fix encoding
        document = document.replaceAll("<meta[^>]*http-equiv=\"Content-Type\"[^>]*/?>", "");
        document = document.replace("<head>", "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
        // Delete bad attributes
        document = document.replaceAll("<a title ", "<a ");
        // Fix footnotes with missing paragraph and missing closing div tag
        // Tidy Html
//        document = tidyHtml(document);
        // Fix encoding
        document = document.replaceAll("<meta[^>]*/?>", "");
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

        // Fix stuff
        document = document.replaceAll("\\.\\.\\.", "…");
        document = document.replaceAll(":\\s*</i>", "</i> : ");

        // Inline second table, first row, second column
        document = document.replaceFirst("<body>\\s*<table>.*?</table>\\s*<table>\\s*<tr>\\s*<td>\\s*</td>\\s*<td>(.*?)</td>.*</body>", "<body>$1</body>");

        String head = extract(document, "<head>.*?</head>", 0);
        String title = extract(document, "<body>.*?(<h1.*?</h1>)", 1);
        String main = document.substring(document.indexOf(title) + title.length(), document.indexOf("</body>"));


        document = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\">" +
                cleanHead(head) + "<body>" +
                "<div id=\"title\">" + cleanTitle(title) + "</div>" +
                "<div id=\"main\">" + cleanMain(main) + "</div>" +
                "</body></html>";

        document = document.replaceAll("<font\\s*>(.*?)</font>", "$1");
        document = document.replaceAll("<a[^>]*></a>", "");

        // Write file
        writeToFile(document, output);
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
        document = document.replaceAll("<p>\\s*</p>", "<br />");
        document = document.replaceAll("(<br />)+</p>", "<p>");
        document = document.replaceAll("([^>])<br />SUR", "$1<br /><br />SUR");
        document = document.replaceAll("VOLONTÉ<br />", "VOLONTÉ<br /><br />");
        document = document.replaceAll("<br />À L’OCCASION", "<br /><br />À L’OCCASION");
        document = document.replaceAll("PAUL VI", "<br />PAUL VI");
        document = document.replaceAll("(BENOÎT XVI|JEAN-PAUL II|PAUL VI|FRANÇOIS)", "<span class=\"author\">$1</span><br />");
        document = document.replaceAll("<p>(.*)<br />(.*?)</p>", "<p><span class=\"title\">$2</span><br /><br />$1</p>");
        document = document.replaceAll("</p>\\s*</p>", "</p>");
        document = document.replaceAll("<br />\\s*(<br /><br />|</p>)", "$1");
        while (document.endsWith("<p>")) {
            document = document.substring(0, document.length() - 3);
        }
        while (document.startsWith("<br />")) {
            document = document.substring(6).trim();
        }
        while (document.endsWith("<br />")) {
            document = document.substring(0, document.length() - 6).trim();
        }
        if (!document.endsWith("</p>")) {
            document = document + "</p>";
        }
        if (!document.startsWith("<p>")) {
            document = "<p>" + document;
        }
        return document;
    }

    private static String cleanMain(String document) {
        int i1 = document.indexOf("</center>");
        int i2 = document.indexOf("<center>");
        if (i1 >= 0 && (i1 < i2 || i2 < 0)) {
            document = document.substring(i1 + "</center>".length());
        }
        document = fixQuotes(document);
        document = fixBibleRefs(document);
        document = fixFootNotes(document);
        document = fixWhitespaces(document);
        document = fixTypos(document);
        return document;
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
                        " #notes .ref a { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        " #notes .author { font-style: italic; }\n" +
                        " #notes .title { font-style: italic; }\n" +
                        " #copyright { color: #663300; text-align: center; font-size: smaller; }\n" +
                        "  .numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        "  .footnote { vertical-align: super; font-size: 70%; line-height: 80%; }\n" +
                        "  .center { text-align: center; }\n" +
                        "  p .ref { margin: 0; padding: 0; font-size: smaller; }\n" +
                        "  p a .ref { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        "</style>" +
                        "</head>");
        return document;
    }

    private static String fixBibleRefs(String document) {
        document = document.replaceAll("\\((cf\\. )?(([12] )?[A-Za-z]+\\.?)((\\s*[1-9][0-9]*[,.;-]?)+)\\s*\\)", "($1<i>$2</i>$4)");
        document = document.replaceAll("<i>(([12] )?[A-Za-zé]+)</i>\\.", "<i>$1.</i>");
        document = document.replaceAll("([12] )<i>([A-Za-zé]+\\.?)</i>", "<i>$1$2</i>");
        document = document.replaceAll("([12])<i>([A-Za-zé]+\\.?)</i>", "<i>$1 $2</i>");
        document = document.replaceAll("(\\((cf\\. )?<i>[^<]*?</i>,? [0-9]+, [0-9]+)\\.\\)", "$1).");
        return document;
    }

    private static String fixFootNotes(String document) {
        document = document.replaceAll("<p>\\[<a[^>]*>([0-9]+)</a>\\]", "<fn>$1</fn> ");
        // Check the kind of foot notes we have so far
        boolean parenthesis = document.indexOf("(1)") >= 0 && document.indexOf("(2)") >= 0 && document.indexOf("(3)") >= 0;
        boolean brackets    = document.indexOf("[1]") >= 0 && document.indexOf("[2]") >= 0 && document.indexOf("[3]") >= 0;
        boolean link        = document.indexOf(">1</a>") >= 0 && document.indexOf(">2</a>") >= 0 && document.indexOf(">3</a>") >= 0;
        boolean linkBracket = document.indexOf(">[1]</a>") >= 0 && document.indexOf(">[2]</a>") >= 0 && document.indexOf(">[3]</a>") >= 0;
        boolean outBracket  = document.indexOf(">1</a>]") >= 0 && document.indexOf(">2</a>]") >= 0 && document.indexOf(">3</a>]") >= 0;
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
        document = document.replaceAll("(<a[^>]*>)\\[([1-9][0-9]*)\\](</a>)", "$1$2$3");
        document = document.replaceAll("<font[^>]*><sup>(<a[^>]*>[1-9][0-9]*</a>)</sup></font>", "$1");
        document = document.replaceAll("<a[^>]*>\\(</a><a[^>]*>([1-9][0-9]*)</a>\\)", "<a href=\"#fn$1\">$1</a>");
        document = document.replaceAll("[\\[\\(]?<a[^>]*>([1-9][0-9]*)</a>[\\]\\)]?", "<span class=\"footnote\" id=\"fnr$1\"><a href=\"#fn$1\">$1</a></span>");
        document = document.replaceAll("\\s+(<span class=\"footnote\">.*?</span>)", "$1");
        document = document.replaceAll("([\\.,;]\\s*)(<span class=\"footnote\".*?</span>)", "$2$1");
        // Fix refs
        document = document.replaceAll("<p><fn>([0-9]+)</fn>", "<p id=\"fn$1\" class=\"ref\"><a href=\"#fnr$1\">[$1]</a> ");
        document = document.replaceAll("<author>(.*?)</author>", "<span class=\"author\">$1</span>");
        document = document.replaceAll("<title>(.*?)</title>", "<span class=\"title\">$1</span>");
        return document;
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

    static final Map<Pattern, String> TYPOS;

    static {
        TYPOS = new LinkedHashMap<Pattern, String>();

        TYPOS.put(Pattern.compile("AETATE"), "ÆTATE");
        TYPOS.put(Pattern.compile("HUMANAE"), "HUMANÆ");
        TYPOS.put(Pattern.compile("PERFECTAE"), "PERFECTÆ");
    }

    private static String fixTypos(String document) {
        for (Map.Entry<Pattern, String> typo : TYPOS.entrySet()) {
            document = typo.getKey().matcher(document).replaceAll(typo.getValue());
        }
        return document;
    }

    private static String extract(String document, String pattern, int group) {
        return extract(document, pattern, group, 0);
    }

    private static String extract(String document, String pattern, int group, int start) {
        Matcher matcher = Pattern.compile(pattern).matcher(document);
        return matcher.find(start) ? matcher.group(group) : null;
    }

}
