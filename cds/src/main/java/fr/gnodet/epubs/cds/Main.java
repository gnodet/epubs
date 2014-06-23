package fr.gnodet.epubs.cds;

import java.awt.*;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import fr.gnodet.epubs.core.Cover;
import fr.gnodet.epubs.core.Processors;
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

        String file = "rc_pc_justpeace_doc_20060526_compendio-dott-soc_fr.html";
        String filename = file.substring(0, file.lastIndexOf('.'));
        String burl = "http://www.vatican.va/roman_curia/pontifical_councils/justpeace/documents/";
        String cache = "target/cache/" + file;
        String output = "target/html/" + file;
        String epub = "target/site/epub/" + filename + ".epub";
        String title = "Compendium de la Doctrine Sociale de l’Église";
        String creator = "Conseil Pontifical « Justice et Paix »";

        byte[] coverPng = Cover.generateCoverPng(Math.random(),
                title,
                new Object[] {
                        new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                        new Cover.Break(),
                        new Cover.Text("Compendium de la", new Font(Font.SERIF, Font.ITALIC, 58), 1.2, 0.25),
                        new Cover.Text("DOCTRINE SOCIALE DE L’ÉGLISE", new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                        new Cover.Break(),
                },
                Main.class.getResource("papacy.svg"));
        writeToFile(coverPng, "target/site/images/" + filename + ".png");

        // Load URL text content
        String document = loadTextContent(new URL(burl + file), cache);

        // Delete office elements
        document = document.replaceAll("<o:p>[\\S\\s]*?</o:p>", "");
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
        document = Processors.process(document, "href=\"#([^\"]*)\"", 1, new URIProcessor());
        document = Processors.process(document, "name=\"([^\"]*)\"", 1, new URIProcessor());
        document = Processors.process(document, "href=\"(\\.\\./[^\"]*)\"", 1, new Processors.RelativeURIProcessor(burl));
        // Delete empty elements
        document = document.replaceAll("<font[^>]*>\\s*</font>", "");
        document = document.replaceAll("<b[^>]*>\\s*</b>", "");
        document = document.replaceAll("<p[^>]*>\\s*</p>", "");
        // Fix style
        document = document.replaceAll("<style>", "<style type=\"text/css\">");

        // Tidy html
        document = tidyHtml(document);

        // Remove new lines to simplify
        document = document.replaceAll("[\r\n]+", " ");
        // Fix encoding
        document = document.replaceAll("<meta[^>]*http-equiv=\"Content-Type\"[^>]*/>", "");
        document = document.replace("<head>", "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
        // Translate entities
        document = translateEntities(document);

        // Delete first table
        document = document.replaceFirst("<table[\\s\\S]*?</table>", "");
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
        document = document.replaceAll(" color=\"#663300\"| face=\"Times New Roman\"", "");
        document = document.replaceAll("align=\"center\"", "class=\"center\"");
        document = document.replaceAll("align=\"right\"", "class=\"right\"");
        document = document.replaceAll("<sup>([0-9]+)<br\\s*/>\\s*</sup>", "<sup>$1</sup><br/>");

        // Fix stuff
        document = document.replaceAll("\\.\\.\\.", "…");
        document = document.replaceAll("come", "comme");
        document = document.replaceAll("la pratique\\s*</i>\\s*</p>\\s*<p>\\s*<i>\\s*du pouvoir", "la pratique du pouvoir");
        document = document.replaceAll(":\\s*</i>", "</i> : ");
//        document = document.replaceAll("<a href=\"#autonomie-et-independance\">", "<a href=\"#autonomie-et-independances\">");
        document = fixQuotes(document);
        document = fixFootNotes(document);
        document = fixWhitespaces(document);

        document = document.replaceAll("<font\\s+>", "<font>");
        int index = 0;
        while ((index = document.indexOf("<font>", index)) > 0) {
            int index2 = document.indexOf("</font>", index);
            document = document.substring(0, index) + document.substring(index + "<font>".length(), index2)
                    + document.substring(index2 + "</font>".length());
        }
        document = document.replaceAll("<font size=\"3\">", "<span class=\"fmedium\">");
        document = document.replaceAll("<font size=\"4\">", "<span class=\"flarge\">");
        document = document.replaceAll("<font size=\"5\">", "<span class=\"fxlarge\">");
        document = document.replaceAll("</font>", "</span>");

        document = document.replaceAll("<a href=\"rc_pc_justpeace_doc_20060526_analytical-index_fr.html\">Index analytique</a>", "");

        document = document.replaceAll("<a[^>]*></a>", "");

        // Add our style
        document = document.replaceAll("<style type=\"text/css\">.*</head>",
                        "<style type=\"text/css\">\n" +
                        " #title { color: #663300; text-align: center; }\n" +
                        " #title .title { font-style:italic; font-size: larger; font-weight:bold; }\n" +
                        " #title .author { font-weight:bold; }\n" +
                        " #bened { font-style:italic; } \n" +
                        " #main .numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        " .footnote { vertical-align: super; font-size: 70%; line-height: 80%; }\n" +
                        " .center { text-align: center; }\n" +
                        " .right { text-align: right; }\n" +
                        " .fmedium { font-size: 120%; }\n" +
                        " .flarge { font-size: 140%; }\n" +
                        " .fxlarge { font-size: 160%; }\n" +
                        " h6 { font-size: 100% }\n" +
                        " p .ref { margin: 0; padding: 0; font-size: smaller; }\n" +
                        " p a .ref { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                        "</style>" +
                        "</head>");

        document = document.replaceAll("<table>\\s*<tr>\\s*<td>\\s*</td>\\s*<td>", "");
        document = document.replaceAll("</td>\\s*</tr>\\s*<tr>\\s*<td.*?>\\s*</td>\\s*</tr>\\s*</table>", "");

        // Possible breaks
        String[] docs = split(document,
                              "<h2|" +
                              "<p align=\"center\"><font color=\"#663300\"><b><a id=\"[^\"]*-chapitre\"|" +
                               "<p align=\"center\"><font face=\"Times New Roman\" color=\"#663300\"><b><a id=\"conclusion\">CONCLUSION</a></b></font></p>|" +
                                      "<hr",
                               output);
        File[] files = new File[docs.length];
        for (int i = 0; i < docs.length; i++) {
            String name = output.substring(0, output.lastIndexOf('.')) + ".p" + toDigits(i) + ".html";
            files[i] = new File(name);
            writeToFile(docs[i], name);
        }

        // Write file
        writeToFile(document, output);

        String uuid = UUID.randomUUID().toString();

        String tocNcx = createToc(title, docs, output, uuid);
        tocNcx = formatXml(tocNcx);
        writeToFile(tocNcx, "target/toc.ncx");

        // Create epub
        Map<String, byte[]> resources = new HashMap<String, byte[]>();
        resources.put("OEBPS/img/cover.png", coverPng);
        resources.put("OEBPS/cover.html",
                Cover.generateCoverHtml(creator, title, "", creator).getBytes());
        createEpub(files, resources, new File(epub), title, creator, tocNcx, uuid);
    }

    private static String formatXml(String input) throws Exception {
        Source xmlInput = new StreamSource(new StringReader(input));
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        // Configure transformer
        Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity transformer
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(xmlInput, xmlOutput);

        return xmlOutput.getWriter().toString();
    }

    private static String createToc(String title, String[] docs, String fileBase, String uid) throws Exception {
        Map<String, Integer> refs = new HashMap<String, Integer>();
        for (int i = 0; i < docs.length; i++) {
            String doc = docs[i];
            Matcher idMatcher = Pattern.compile(" id=\"([^\"]*)\"").matcher(doc);
            int idStart = 0;
            while (idMatcher.find(idStart)) {
                String name = idMatcher.group(1);
                refs.put(name, i);
                idStart = idMatcher.end();
            }
        }
        Document tocDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("cds-toc.xml"));
        StringBuilder tocNcx = new StringBuilder();
        tocNcx.append("<?xml version='1.0' encoding='utf-8'?>\n" +
                      "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\" xml:lang=\"eng\">\n" +
                      "  <head>\n" +
                      "    <meta content=\"1\" name=\"dtb:depth\"/>\n" +
                      "    <meta content=\"" + uid + "\" name=\"dtb:uid\"/>\n" +
                      "  </head>\n" +
                      "  <docTitle>\n" +
                      "    <text>" + title + "</text>\n" +
                      "  </docTitle>\n" +
                      "  <navMap>\n");

        String baseName = new File(fileBase).getName();
        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        buildToc(tocDoc.getDocumentElement(), tocNcx, refs, new AtomicInteger(0), new AtomicInteger(0), new AtomicReference<String>(""), baseName);
        tocNcx.append("  </navMap>\n" +
                      "</ncx>\n");
        return tocNcx.toString();
    }

    private static void buildToc(Node node, StringBuilder tocNcx, Map<String, Integer> refs, AtomicInteger id, AtomicInteger po, AtomicReference<String> lastHref, String baseName) {
        if (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("item")) {
                    String text = ((Element) node).getAttribute("text");
                    String href = ((Element) node).getAttribute("href");
                    if (href == null || href.isEmpty()) {
                        href = getHref(refs, text);
                    }
                    Integer docIndex = refs.get(href);
                    if (docIndex == null) {
                        if (node.getFirstChild() != null) {
                            Node ch = node.getFirstChild();
                            while (ch != null && ch.getNodeType() != Node.ELEMENT_NODE) {
                                ch = ch.getNextSibling();
                            }
                            if (ch != null) {
                                href = ((Element) ch).getAttribute("href");
                                if (href == null || href.isEmpty()) {
                                    href = getHref(refs, ((Element) ch).getAttribute("text"));
                                }
                                if (href != null) {
                                    docIndex = refs.get(href);
                                }
                            }
                        }
                    }
                    String sid = "id-" + Integer.toString(id.incrementAndGet());
                    String shr = (docIndex != null) ? "OEBPS/" + baseName + ".p" + toDigits(docIndex) + ".html#" + href : lastHref.get();
                    int    spo = lastHref.get().equals(shr) ? po.get() : po.incrementAndGet();
                    lastHref.set(shr);

                    tocNcx.append("<navPoint id=\"id-").append(sid).append("\" playOrder=\"").append(spo).append("\">");
                    tocNcx.append("<navLabel><text>").append(text).append("</text></navLabel>");
                    tocNcx.append("<content src=\"" + shr + "\"/>");
                    buildToc(node.getFirstChild(), tocNcx, refs, id, po, lastHref, baseName);
                    tocNcx.append("</navPoint>");
                } else {
                    buildToc(node.getFirstChild(), tocNcx,  refs, id, po, lastHref, baseName);
                }
            }
            buildToc(node.getNextSibling(), tocNcx,  refs, id, po, lastHref, baseName);
        }
    }

    private static String getHref(Map<String, Integer> refs, String text) {
        String href = new URIProcessor().process(text);
        if (href.startsWith("introduction")) {
            href = "introduction";
        } else if (href.indexOf("-chapitre") > 0) {
            href = href.substring(0, href.indexOf("-chapitre") + "-chapitre".length());
        } else if (href.indexOf("-partie") > 0) {
            href = href.substring(0, href.indexOf("-partie") + "-partie".length());
        }
        href = href.replaceAll("-uvre-", "-339uvre-");
        Integer index = refs.get(href);
        List<String> prefixes = Arrays.asList(new String[]{"a", "b", "c", "d", "e", "f", "i", "ii", "iii", "iv"});
        for (String prefix : prefixes) {
            if (index == null) {
                index = refs.get(prefix + "-" + href);
                if (index != null) {
                    href = prefix + "-" + href;
                }
            }
        }
        return href;
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

    private static String[] split(String document, String regex, String fileName) {
        int min = 50 * 1024;
        int max = 50 * 1024;
        int bodyStart = document.indexOf("<body>") + "<body>".length();
        int bodyEnd = document.indexOf("</body>");
        List<Integer> breaks = new ArrayList<Integer>();
        Matcher matcher = Pattern.compile(regex).matcher(document);
        int start = bodyStart;
        breaks.add(bodyStart);
        while (matcher.find(start)) {
            int prev = breaks.get(breaks.size() - 1);
            int cur = matcher.start();
            if (cur - prev > min) {
                breaks.add(matcher.start());
            }
            start = matcher.end();
        }
        breaks.add(bodyEnd);
        List<String> docs = new ArrayList<String>();
        int currStart = bodyStart;
        int numBreak = 0;
        List<String> opened = new ArrayList<String>();
        while (numBreak < breaks.size()) {
            int nextBreak = breaks.get(numBreak);
            if (nextBreak - currStart >= max) {
                String doc = document.substring(currStart, nextBreak);
                if (doc.startsWith("<hr />")) {
                    doc = doc.substring("<hr />".length());
                }
                String open = "";
                for (String tag : opened) {
                    open += "<" + tag + ">";
                }
                doc = open + doc;
                opened.clear();
                computeTags(doc, opened);
                List<String> rev = new ArrayList<String>(opened);
                Collections.reverse(rev);
                for (String tag : rev) {
                    doc += "</" + tag + ">";
                }
                doc = document.substring(0, bodyStart) + doc + "</body></html>";
                docs.add(doc);
                currStart = nextBreak;
            }
            numBreak++;
        }
        Map<String, Integer> refs = new HashMap<String, Integer>();
        for (int i = 0; i < docs.size(); i++) {
            String doc = docs.get(i);
            Matcher idMatcher = Pattern.compile(" id=\"([^\"]*)\"").matcher(doc);
            int idStart = 0;
            while (idMatcher.find(idStart)) {
                String name = idMatcher.group(1);
                refs.put(name, i);
                idStart = idMatcher.end();
            }
        }
        String baseName = new File(fileName).getName();
        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        for (int i = 0; i < docs.size(); i++) {
            String doc = docs.get(i);
            StringBuilder newDoc = new StringBuilder();
            Matcher idMatcher = Pattern.compile(" href=\"(#([^\"]*))\"").matcher(doc);
            int idStart = 0;
            while (idMatcher.find(idStart)) {
                newDoc.append(doc.substring(idStart, idMatcher.start(1)));
                Integer docIndex = refs.get(idMatcher.group(2));
                if (docIndex != null && docIndex.intValue() != i) {
                    newDoc.append(baseName + ".p" + toDigits(docIndex) + ".html#" + idMatcher.group(2));
                } else {
                    newDoc.append("#" + idMatcher.group(2));
                }
                idStart = idMatcher.end(1);
            }
            newDoc.append(doc.substring(idStart, doc.length()));
            docs.set(i, newDoc.toString());
        }

        return docs.toArray(new String[docs.size()]);
    }

    private static void computeTags(String document, List<String> opened) {
        Matcher matcher = Pattern.compile("</?([a-z]+)\\b[^>]*>").matcher(document);
        int start = 0;
        while (matcher.find(start)) {
            String tag = matcher.group();
            String name = matcher.group(1);
            if (tag.startsWith("</")) {
                String old = opened.remove(opened.size() - 1);
                if (!name.equals(old)) {
                    System.err.println("Tag mismatch: found </" + name + "> but expected </" + old + ">");
                }
            } else if (!tag.endsWith("/>")) {
                opened.add(name);
            }
            start = matcher.end();
        }
    }

    static class URIProcessor implements Processors.Processor {
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
