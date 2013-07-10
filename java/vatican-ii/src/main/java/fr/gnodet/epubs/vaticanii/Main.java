package fr.gnodet.epubs.vaticanii;

import fr.gnodet.epubs.core.Cover;
import fr.gnodet.epubs.core.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.IOUtil.loadTextContent;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;
import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Tidy.tidyHtml;
import static fr.gnodet.epubs.core.Tidy.translateEntities;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespaces;

public class Main {

    static final javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String[] args) throws Exception {

        dbf.setNamespaceAware(true);

        Document enclist = dbf.newDocumentBuilder().parse(Main.class.getResourceAsStream("list.xml"));
        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {
//        for (int i = 17; i < books.getLength(); i++) {
//        int i = 19; {
            Element book = (Element) books.item(i);
            String file = book.getAttribute("file");
            String title = book.getAttribute("title");
            String creator = book.getAttribute("creator");
            String date = book.getAttribute("date");
            String type = book.getAttribute("type");
            URL url = new URL("http://www.vatican.va/archive/hist_councils/ii_vatican_council/documents/" + file);
            try {
                process(url, "target/cache/" + file, "target/html/" + file);
                Map<String, byte[]> resources = new HashMap<String, byte[]>();
                resources.put("OEBPS/img/cover.png",
                        Cover.generateCoverPng((i * 1.0 / books.getLength()),
                                "Concile Vatican II",
                                title,
                                type,
                                null,
                                Main.class.getResource("papacy.svg")));
                resources.put("OEBPS/cover.html",
                        Cover.generateCoverHtml(creator, title, type, "Concile Vatican II").getBytes());
                createEpub(new File[] { new File("target/html/" + file) },
                           resources,
                           new File("target/epub/" + file.substring(0, file.lastIndexOf('.')) + ".epub"),
                           title,
                           creator,
                           null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void process(URL url, String cache, String output) throws Exception {
        System.out.println("Processing: " + url);

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

        // Fix stuff
        document = document.replaceAll("\\.\\.\\.", "…");
        document = document.replaceAll(":\\s*</i>", "</i> : ");

        document = cleanHead(document);
        document = fixQuotes(document);
        document = fixBibleRefs(document);
        document = fixFootNotes(document);
        document = fixWhitespaces(document);

        document = document.replaceAll("<font\\s*>(.*?)</font>", "$1");
        document = document.replaceAll("<a[^>]*></a>", "");

        // Write file
        writeToFile(document, output);
    }

    private static String cleanHead(String document) {
        // Remove meta tags with spaces
        document = document.replaceAll("<meta[^>]*name=\"[^\"]* [^\"]*\"[^>]*/>", "");
        // Remove style elements
        document = document.replaceAll("<style.*?</style>", "");
        // Add our style
        document = document.replaceAll("</head>",
                "<style type=\"text/css\">\n" +
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
        document = document.replaceAll("(<a [^>]*>)\\[([1-9][0-9]*)\\](</a>)", "$1$2$3");
        document = document.replaceAll("<font[^>]*><sup>(<a[^>]*>[1-9][0-9]*</a>)</sup></font>", "$1");
        document = document.replaceAll("<a[^>]*>\\(</a><a[^>]*>([1-9][0-9]*)</a>\\)", "<a href=\"#fn$1\">$1</a>");
        document = document.replaceAll("[\\[\\(]?<a[^>]*>([1-9][0-9]*)</a>[\\]\\)]?", "<span class=\"footnote\" id=\"fnr$1\"><a href=\"#fn$1\">$1</a></span>");
        document = document.replaceAll("\\s+(<span class=\"footnote\">.*?</span>)", "$1");
        document = document.replaceAll("([\\.,;]\\s*)(<span class=\"footnote\".*?</span>)", "$2$1");
        // Fix refs
        document = document.replaceAll("<fn>([0-9]+)</fn>", "<p id=\"fn$1\" class=\"ref\"><a href=\"#fnr$1\">[$1]</a> ");
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

}
