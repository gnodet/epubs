package fr.gnodet.epubs.thonnard.histoire;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import fr.gnodet.epubs.core.Cover;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.IOUtil.loadTextContent;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;
import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Tidy.tidyHtml;
import static fr.gnodet.epubs.core.Tidy.translateEntities;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespaces;

public class Main {

    public static void main(String[] args) throws Exception {

        String epub = "target/epub/precis_d_histoire_de_la_philosophie.epub";
        String title = "Précis d’histoire de la philosophie";
        String creator = "François-Joseph THONNARD";
        String burl = "http://inquisition.ca/fr/livre/thonnard/histo/";
        String firstFile = "h01_index.htm";

        // Create toc
        /*
        String tdm = "h02_TDM.htm";
        String toc = loadTextContent(new URL(burl + tdm), "target/cache/" + tdm);
        toc = toc.replaceAll("[\r\n]", " ");
        toc = toc.substring(toc.indexOf("</h1>"), toc.indexOf("Table alphabétique des philosophes"));
        LinkedHashMap<String, String> hrefs = new LinkedHashMap<String, String>();
        Matcher m = Pattern.compile("<a href=\"([^\"]*)\">(.*?)</a>").matcher(toc);
        while (m.find()) {
            String ref = m.group(1);
            String name = m.group(2);
            if ("#s1p2".equals(ref)) {
                ref = "h04_p0001a0025.htm#s1p2";
                name = "1. " + name;
            } else if ("h04_p0001a0025.htm#s3p1".equals(ref)) {
                name = "2. " + name;
            }
            if (name.equals("1)L'UN.")) {
                name = "1) L'UN.";
            }
            hrefs.put(ref, name);
        }
        String[] patterns = { "(Première|Deuxième|Troisième|Quatrième)\\s+partie.*",
                "Conclusion générale\\.",
                "(Première|Deuxième|Troisième|Quatrième|2e|3e)\\s+(période|PÉRIODE).*",
                "(Première|Deuxième|Troisième|Quatrième)\\s+étape.*",
                "(1er|2e|3e)\\s+degré\\).*",
                "Chapitre [1-9][\\.:] .*",
                "Section [1-9][\\.:] .*",
                "Article [1-9][\\.:] .*",
                "[A-E]\\) .*",
                "([1-9]|1[0-9])\\. .*",
                "([1-9]|1[0-9])\\) .*",
                "[1-9]\\.[1-9]\\) .*",
                "(1er|2e|3e)\\).*",
                "°[1-9].*|b[1-9].*",
                "[A-Z][a-z].*"
                };
        int last = 0;
        List<String> parents = new ArrayList<String>();
        for (Map.Entry<String, String> entry : hrefs.entrySet()) {
            String ref = entry.getKey();
            String name = entry.getValue();
            String pattern = null;
            for (String p : patterns) {
                if (Pattern.matches(p, name)) {
                    pattern = p;
                    break;
                }
            }
            if (pattern == null) {
                pattern = ".*";
            }
            if (patterns[0].equals(pattern) && !name.equals("Première Partie: Volontarisme.") && !name.equals("Deuxième partie: Formalisme.")
                    || patterns[1].equals(pattern)) {
                parents.clear();
            }
            if (name.equals("Deuxième partie: Formalisme.")) {
                parents.remove(parents.size() - 1);
            } else if (!parents.isEmpty()) {
                for (int i = 0; i < parents.size(); i++) {
                    if (Pattern.matches(parents.get(i), name)) {
                        parents.removeAll(parents.subList(i, parents.size()));
                        break;
                    }
                }
            }
            if (last >= parents.size()) {
                System.out.println("/>");
            } else {
                System.out.println(">");
            }
            while (last-- > parents.size()) {
                for (int i = 0; i < last; i++) {
                    System.out.print("  ");
                }
                System.out.println("</item>");
            }
            last = parents.size();
            for (int i = 0; i < last; i++) {
                System.out.print("  ");
            }
            System.out.print("<item text=\"" + name + "\" ref=\"" + ref + "\"");
            parents.add(pattern);
        }
        */
        String tocNcx = createToc(epub);
        byte[] coverPng = Cover.generateCoverPng(Math.random(),
                title,
                new Object[] {
                        new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                        new Cover.Break(),
                        new Cover.Text("Précis d’histoire", new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                        new Cover.Text("de la philosophie", new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                        new Cover.Break(),
                },
                null);


        Set<String> toDownload = new LinkedHashSet<String>();
        Set<String> downloaded = new LinkedHashSet<String>();
        toDownload.add(firstFile);
        while (!toDownload.isEmpty()) {
            String name = toDownload.iterator().next();
            toDownload.remove(name);

            try {
                String document = loadTextContent(new URL(burl + name), "target/cache/" + name);
                downloaded.add(name);
                int index = document.indexOf("<a href=\"");
                while (index >= 0) {
                    int start = index + "<a href=\"".length();
                    int stop = document.indexOf("\"", start + 1);
                    String rel = document.substring(start, stop);

                    // TODO: download images

                    if (!rel.startsWith("../") && !rel.startsWith("http")) {
                        start = rel.indexOf('#');
                        if (start >= 0) {
                            rel = rel.substring(0, start);
                        }
                        if (rel.length() > 0 && !downloaded.contains(rel) && !toDownload.contains(rel)) {
                            toDownload.add(rel);
                        }
                    }
                    index = document.indexOf("<a href=\"", stop);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Ignoring file " + e.getMessage());
            }
        }


        List<File> files = new ArrayList<File>();
        List<String> docs = new ArrayList<String>();

        for (String file : downloaded) {
            String document = loadTextContent(new URL(burl + file), "target/cache/" + file);
            String output = "target/html/" + file;

            // Fix encoding
            document = document.replaceAll("<meta[^>]*http-equiv=\"Content-Type\"[^>]*/>", "");
            document = document.replace("<head>", "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
            // Delete scripts tags
            document = document.replaceAll("<script[\\S\\s]*?</script>", "");
            document = document.replaceAll("<SCRIPT[\\S\\s]*?</SCRIPT>", "");
            // Delete image tags
            document = document.replaceAll("<img[^>]*>", "");
            document = document.replaceAll("<IMG[^>]*>", "");
            // Fix id and href attributes
            document = process(document, "href=\"#([^\"]*)\"", 1, new URIProcessor());
            document = process(document, "name=\"([^\"]*)\"", 1, new URIProcessor());
            document = process(document, "href=\"(\\.\\./[^\"]*)\"", 1, new RelativeURIProcessor(burl));
            // Delete empty elements
            document = document.replaceAll("<font[^>]*>\\s*</font>", "");
            document = document.replaceAll("<b[^>]*>\\s*</b>", "");
            document = document.replaceAll("<p[^>]*>\\s*</p>", "");
            // Fix style
            document = document.replaceAll("<link[^>]*type=\"text/css\"[^>]*>", "");
            document = document.replaceAll("<style>", "<style type=\"text/css\">");

            // Tidy html
            document = tidyHtml(document);

            // Use xhtml 1.1
            document = document.replaceAll(
                    "<!DOCTYPE[^>]*>",
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");

            document = document.replace("&agrave; ('infini", "&agrave; l'infini");

            // Remove new lines to simplify
            {
                int index = document.indexOf("<pre>");
                while (index > 0) {
                    int stop = document.indexOf("</pre>", index) + "</pre>".length();
                    String pre = document.substring(index, stop);
                    pre = pre.replaceAll("\n", "<br/>");
                    document = document.substring(0, index) + pre + document.substring(stop, document.length());
                    index = document.indexOf("<pre>", stop);
                }
            }
            document = document.replaceAll("[\r\n]+", " ");
            // Fix encoding
            document = document.replaceAll("<meta[^>]*http-equiv=\"Content-Type\"[^>]*/>", "");
            document = document.replace("<head>", "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
            // Translate entities
            document = translateEntities(document);

            // Delete header and footer
            document = document.replaceAll("<p class=\"poucethaut\">.*?</p>\\s*<p[^>]*>\\s*\\[.*?\\]\\s*</p>", "");
            document = document.replaceAll("<p[^>]*>\\s*\\[.*?\\]\\s*</p>\\s*<p class=\"poucetbas\">.*?</p>", "");
            document = document.replaceAll("<p[^>]*>Note: Si le grec classique.*?</p>", "");
            document = document.replaceAll("<p[^>]*>Table alphabétique.*?</p>\\s*<p[^>]*>Je pense que Google.*?</p>.*?<form.*?</form>.*?-->", "");
            // Delete unwanted attributes
            document = document.replaceAll(" \\b(width|topmargin|border|marginwidth|marginheight|cellspacing|" +
                    "cellpadding|hspacing|lang|xml:lang|alink|vlink|link|background|" +
                    "text|height|hspace|alt|bgcolor|rowspan|valign)=[^\\s>]*", "");
            document = document.replaceAll(" style=\"[^\"]*\"", "");
            document = document.replaceAll(" style='[^']*'", "");
            document = document.replaceAll(" clear=\"all\"| align=\"left\"| align=\"justify\"| class=\"[^\"]*\"| name=\"[^\"]*\"", "");
            // Remove unwanted meta tags
            document = document.replaceAll("<meta name=\"generator\".*?/>", "");

            //
            // Clean things a bit
            //
            document = document.replaceAll("<i><br />", "<br /><i>");
            document = document.replaceAll(" color=\"#663300\"| face=\"Times New Roman\"| size=\"3\"", "");
            document = document.replaceAll("align=\"center\"", "class=\"center\"");
            document = document.replaceAll("align=\"right\"", "class=\"right\"");

            // Fix stuff
            document = document.replaceAll("\\.\\.\\.", "…");
            document = document.replaceAll(":\\s*</i>", "</i> : ");
            document = fixQuotes(document);
            document = fixFootNotes(document);
            document = fixWhitespaces(document);

            document = document.replaceAll("<font\\s*>(.*?)</font>", "$1");
            document = document.replaceAll("<a[^>]*></a>", "");

            // Add our style
            document = document.replaceAll("</head>",
                    "<style type=\"text/css\">\n" +
                            " #title { color: #663300; text-align: center; }\n" +
                            " #title .title { font-style:italic; font-size: larger; font-weight:bold; }\n" +
                            " #title .author { font-weight:bold; }\n" +
                            " #bened { font-style:italic; } \n" +
                            " #main .numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                            " .footnote { vertical-align: super; font-size: 70%; line-height: 80%; }\n" +
                            " .center { text-align: center; }\n" +
                            " .right { text-align: right; }\n" +
                            " p .ref { margin: 0; padding: 0; font-size: smaller; }\n" +
                            " p a .ref { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                            "</style>" +
                            "</head>");

            // Write file
            writeToFile(document, output);
            files.add(new File(output));
            docs.add(document);
        }

        // Create epub
        Map<String, byte[]> resources = new HashMap<String, byte[]>();
        resources.put("OEBPS/img/cover.png", coverPng);
        resources.put("OEBPS/cover.html", Cover.generateCoverHtml(creator, title, "", creator).getBytes());
        createEpub(files.toArray(new File[files.size()]), resources, new File(epub), title, creator, tocNcx);
    }

    private static String createToc(String fileBase) throws Exception {
        Document tocDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("histoire-philo.xml"));
        StringBuilder tocNcx = new StringBuilder();
        tocNcx.append("<?xml version='1.0' encoding='utf-8'?>\n" +
                      "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\" xml:lang=\"eng\">\n" +
                      "  <head>\n" +
                      "    <meta content=\"1\" name=\"dtb:depth\"/>\n" +
                      "  </head>\n" +
                      "  <docTitle>\n" +
                      "    <text>Précis d'histoire de la philosophie</text>\n" +
                      "  </docTitle>\n" +
                      "  <navMap>\n");

        String baseName = new File(fileBase).getName();
        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        buildToc(tocDoc.getDocumentElement(), tocNcx, new AtomicInteger(0), new AtomicReference<String>(""), baseName);
        tocNcx.append("  </navMap>\n" +
                      "</ncx>\n");
        return tocNcx.toString();
    }

    private static void buildToc(Node node, StringBuilder tocNcx, AtomicInteger counter, AtomicReference<String> lastHref, String baseName) {
        if (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("item")) {
                    String text = ((Element) node).getAttribute("text");
                    String href = ((Element) node).getAttribute("ref");
                    tocNcx.append("<navPoint id=\"").append(counter.get()).append("\" playOrder=\"").append(counter.get()).append("\">\n");
                    tocNcx.append("<navLabel><text>").append(text).append("</text></navLabel>\n");
                    counter.incrementAndGet();
                    if (href != null) {
                        tocNcx.append("<content src=\"OEBPS/" + href + "\"/>\n");
                        lastHref.set(href);
                        buildToc(node.getFirstChild(), tocNcx, counter, lastHref, baseName);
                    } else {
                        tocNcx.append("<content src=\"OEBPS/" + lastHref.get() + "\"/>\n");
                        buildToc(node.getFirstChild(), tocNcx, counter, lastHref, baseName);
                    }
                    tocNcx.append("</navPoint>\n");
                } else {
                    buildToc(node.getFirstChild(), tocNcx, counter, lastHref, baseName);
                }
            }
            buildToc(node.getNextSibling(), tocNcx, counter, lastHref, baseName);
        }
    }

    private static String toDigits(int nb) {
        if (nb < 10) {
            return "0" + Integer.toString(nb);
        } else {
            return Integer.toString(nb);
        }
    }

    private static String fixFootNotes(String document) {
        document = document.replaceAll("<p><sup>([0-9]+)</sup>", "<p id=\"fn$1\" class=\"ref\"><a href=\"#fnr$1\">[$1]</a> ");
        document = document.replaceAll("<br /></sup>", "</sup><br />");
        document = document.replaceAll("([.,;:?!]\\s*)(<sup>([0-9]+)</sup>)", "$2$1");
        document = document.replaceAll("<sup>([0-9]+)</sup>", "<a id=\"fnr$1\" class=\"footnote\" href=\"#fn$1\">$1</a>");
        return document;
    }

    interface Processor {
        String process(String text);
    }

    private static String process(String document, String regexp, int group, Processor processor) {
        Matcher paragraph = Pattern.compile(regexp).matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start(group)));
            newDoc.append(processor.process(paragraph.group(group)));
            start = paragraph.end(group);
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    static class RelativeURIProcessor implements Processor {
        final URI buri;
        RelativeURIProcessor(String buri) {
            this.buri = URI.create(buri);
        }
        @Override
        public String process(String text) {
            String r = buri.resolve(text).toString();
            return r;
        }
    }

    static class URIProcessor implements Processor {
        @Override
        public String process(String text) {
            try {
                text = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                text = text.replaceAll("[^\\p{Alnum}\\p{Space}]", "");
                String encoded = URLEncoder.encode(text, "UTF-8");
                if (encoded.equals("b+eglise+catholique+et+communaute+politique")) {
                    encoded = "eglise+catholique+et+communaute+politique";
                }
                encoded = encoded.replace('+', '-');
                encoded = encoded.replaceAll("--", "-");
                if (encoded.startsWith("-")) {
                    encoded = encoded.substring(1);
                }
                if (encoded.endsWith("-")) {
                    encoded = encoded.substring(0, encoded.length() - 1);
                }
                return encoded;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
