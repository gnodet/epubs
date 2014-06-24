package fr.gnodet.epubs.bible;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import fr.gnodet.epubs.core.IOUtil;
import fr.gnodet.epubs.core.Processors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.EPub.createToc;
import static fr.gnodet.epubs.core.IOUtil.loadTextContent;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;
import static fr.gnodet.epubs.core.Processors.process;
import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Tidy.tidyHtml;
import static fr.gnodet.epubs.core.Tidy.translateEntities;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespaces;

public class Main {

    public static void main(String[] args) throws Exception {

        String filename = "bible_liturgie_catholique";
        String epub = "target/site/epub/" + filename + ".epub";
        String title = "Bible de la Liturgique Catholique";
        String creator = "AELF";
        String burl = "http://www.aelf.org";
        final String firstFile = "/bible-liturgie";

        Set<String> toDownload = new LinkedHashSet<String>();
        Set<String> downloaded = new LinkedHashSet<String>();
        toDownload.add(firstFile);
        while (!toDownload.isEmpty()) {
            String name = toDownload.iterator().next();
            toDownload.remove(name);

            try {
                String document = loadTextContent(new URL(burl + name), "target/cache/" + name + ".html");
                downloaded.add(name);

                document = document.substring(document.indexOf("<div id=\"texte\""));
                document = document.replaceAll("(<div id=\"texte\"[\\s\\S]*?<div class=\"print_only\">[\\s\\S]*?</div>\\s*</div>)[\\s\\S]*", "$1");
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
                            if (!rel.contains("chapitre")) {
                                rel += "/chapitre/1";
                            }
                            toDownload.add(rel);
                        }
                    }
                    index = document.indexOf("<a href=\"", stop);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Ignoring file " + e.getMessage());
            }
        }

        String order = new String(IOUtil.readFully(Main.class.getResource("bible-toc.xml")));
        final List<String> booksOrder = new ArrayList<String>();
        Matcher matcher = Pattern.compile("ref=\"(\\S*)/index.html\"").matcher(order);
        while (matcher.find()) {
            booksOrder.add(matcher.group(1));
        }

        List<File> files = new ArrayList<File>();
        List<String> docs = new ArrayList<String>();

        for (final String file : downloaded) {
            String document = loadTextContent(new URL(burl + file + ".html"), "target/cache/" + file + ".html");
            String output = URLDecoder.decode(file + ".html", "UTF-8");
            output = output.replaceAll("bible-liturgie/([^/]*)/[^/]*/chapitre/([^/]*.html)", "$1/$2");
            if (output.equals("/bible-liturgie.html")) {
                output = "/index.html";
            }
            output = "target/html" + output;

            String h1 = document.replaceAll("[\\s\\S]*<h1[^>]*>([\\s\\S]*?)</h1>[\\s\\S]*", "$1");
            document = document.substring(document.indexOf("<div id=\"texte\""));
//            document = document.replaceAll("(<div id=\"texte\"[\\s\\S]*?<div class=\"print_only\">[\\s\\S]*?</div>\\s*</div>)[\\s\\S]*", "$1");
            if (file.equals(firstFile)) {
                document = document.replaceAll("(<div id=\"texte\"[\\s\\S]*?)<div class=\"print_only\"[\\s\\S]*", "$1");
            } else {
                document = document.replaceAll("(<div id=\"texte\"[\\s\\S]*?)<div class=\"print_only\"[\\s\\S]*", "$1</div>");
            }
            document = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>" +
                    "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />" +
                    "<style type=\"text/css\">\n" +
                    " .numero_verset { vertical-align: super; font-size: 70%; line-height: 80%; }\n" +
                    "</style>\n" +
                    "<title>" + h1 + "</title></head>\n" +
                    "<body>\n" +
                    "<h1>" + h1 + "</h1>\n" + document + "</body></html>";

            document = document.replaceAll("<div class=\"clr\">&nbsp;</div>", "");
            document = document.replaceAll("<div class=\"clr\">\\s*</div>", "");
            document = document.replaceAll("<div class=\"clr\" />", "");
            document = document.replaceAll("<div class=\"print_only\">[\\s\\S]*?</div>", "");
            document = document.replaceAll("<div[^>]*>\\s*<select[\\s\\S]*</select>\\s*</div>", "");
            document = document.replaceAll("<div class=\"f_left\">[\\s\\S]*?</div>", "");
            document = document.replaceAll("<div class=\"pager\">[\\s\\S]*?</div>", "");
            document = document.replaceAll("<form[^>]*>[\\s\\S]*</form>", "");
            document = document.replaceAll("<div class=\"verset\" id=\"([0-9]+)\">", "<div class=\"verset\" id=\"v$1\">");
//            document = document.replace("</body>", "</div></body>");

            document = document.replace("\u001e", "-");

            document = document.replaceAll("<div[^>]*id=\"livre_psaumes\">[\\s\\S]*?</div>", "");

            document = document.replaceAll("</ul>\\s*<ul[^>]*>", "");
            document = document.replaceAll("class=\"[^\"]* testament \" ", "");
            document = document.replaceAll("<span class=\"gris\">[\\s\\S]*?</span>", "");
            document = document.replaceAll("Lire la bible", "Bible");
            document = document.replaceAll("(&gt;|>) <a", "<a");

            // Tidy html
//            document = tidyHtml(document);
//            document = document.replaceAll("<meta name=\"generator[^>]*/>", "");

            document = process(document, "href=\"([^\"]*)\"", 1, new Processors.Processor() {
                @Override
                public String process(String text) {
                    if (text.startsWith("/bible-liturgie#")) {
                        return text.substring("/bible-liturgie".length());
                    } else if (text.startsWith("/bible-liturgie/")) {
                        try {
                            text = URLDecoder.decode(text, "UTF-8");
                            text = text.replaceAll("/bible-liturgie/([^/]*)/[^/]*", "$1/index.html");
                            return text;
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return text;
                }
            });

            document = document.replaceAll("Evangile", "Évangile");
            document = document.replaceAll("REVELATION", "RÉVÉLATION");
            document = document.replaceAll("JESUS", "JÉSUS");
            document = document.replaceAll("ETAIT", "ÉTAIT");
            document = document.replaceAll("SYMEON", "SYMÉON");
            document = document.replaceAll("APOTRE", "APÔTRE");
            document = document.replaceAll("TIMOTHEE", "TIMOTHÉE");
            document = document.replaceAll("APPELE", "APPELÉ");
            document = document.replaceAll("THEOPHILE", "THÉOPHILE");
            document = document.replaceAll("GENEALOGIE", "GÉNÉALOGIE");
            document = document.replaceAll("PREMIERE", "PREMIÈRE");
            document = document.replaceAll("DEUXIEME", "DEUXIÈME");
            document = document.replaceAll("TROISIEME", "TROISIÈME");
            document = document.replaceAll("TRENTIEME", "TRENTIÈME");
            document = document.replaceAll("ANNEE", "ANNÉE");
            document = document.replaceAll("JEREMIE", "JÉRÉMIE");
            document = document.replaceAll("NEHEMIE", "NÉHÉMIE");
            document = document.replaceAll("APRES", "APRÈS");
            document = document.replaceAll("EPOQUE", "ÉPOQUE");
            document = document.replaceAll("DESERT", "DÉSERT");

            document = translateEntities(document);
            document = fixQuotes(document);
            document = fixWhitespaces(document);

            // Write file
            writeToFile(document, output);
            files.add(new File(output));
            docs.add(document);
        }

        // Create index per book
        Map<String, List<String>> books = new HashMap<String, List<String>>();
        for (String file : downloaded) {
            if (!file.startsWith("/bible-liturgie/")) {
                continue;
            }
            String book = file.substring(0, file.indexOf("chapitre/"));
            List<String> chapters = books.get(book);
            if (chapters == null) {
                chapters = new ArrayList<String>();
                books.put(book, chapters);
            }
            chapters.add(file);
        }
        for (String book : books.keySet()) {
            List<String> chapters = books.get(book);

            String booktitle = URLDecoder.decode(book, "UTF-8");
            if (booktitle.endsWith("/")) {
                booktitle = booktitle.substring(0, booktitle.length() - 1);
            }
            if (booktitle.contains("/")) {
                booktitle = booktitle.substring(booktitle.lastIndexOf("/") + 1);
            }
            booktitle = booktitle.replaceAll("Evangile", "Évangile");

            String output = URLDecoder.decode(book + "index.html", "UTF-8");
            output = output.replaceAll("bible-liturgie/([^/]*)/[^/]*/([^/]*.html)", "$1/$2");
            output = "target/html" + output;

            String document = "";
            int off = chapters.contains(book + "chapitre/0") ? 0 : 1;
            for (int i = 0; i < chapters.size(); i++) {
                document = document + "<a href=\"" + (i+off) + ".html\">" + (i+off) + "</a>\n";
            }

            document = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>" +
                    "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />" +
                    "<style type=\"text/css\">\n" +

                    "</style>\n" +
                    "<title>" + booktitle + "</title></head>\n" +
                    "<body>\n" +
                    "<h1>" + booktitle + "</h1>\n<div>\n" + document + "</div>\n</body></html>";


            // Write file
            writeToFile(document, output);
            files.add(new File(output));
            docs.add(document);
        }

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String s1 = o1.toString().substring("target/html/".length());
                String s2 = o2.toString().substring("target/html/".length());
                if (s1.equals("index.html")) {
                    return -1;
                } else if (s2.equals("index.html")) {
                    return 1;
                } else {
                    String b1 = s1.substring(0, s1.indexOf("/"));
                    String b2 = s2.substring(0, s2.indexOf("/"));
                    int i1 = booksOrder.indexOf(b1);
                    int i2 = booksOrder.indexOf(b2);
                    if (i1 != i2) {
                        return i1 - i2;
                    } else {
                        if (s1.contains("index.html")) {
                            return -1;
                        } else if (s2.contains("index.html")) {
                            return 1;
                        } else {
                            s1 = s1.substring(b1.length() + 1, s1.length() - ".html".length());
                            s2 = s2.substring(b2.length() + 1, s2.length() - ".html".length());
                            i1 = Integer.parseInt(s1);
                            i2 = Integer.parseInt(s2);
                            return i1 - i2;
                        }
                    }
                }
            }
        });

        // Create epub

        String tocNcx = createToc(title, IOUtil.readUrl(Main.class.getResource("bible-toc.xml"), "UTF-8"));
        byte[] coverPng = Cover.generateCoverPng(Math.random(),
                title,
                new Object[] {
                        new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                        new Cover.Break(),
                        new Cover.Text("Bible", new Font(Font.SERIF, Font.PLAIN, 96), 1.1, 0.25),
                        new Cover.Text("Catholique", new Font(Font.SERIF, Font.PLAIN, 96), 1.1, 0.25),
                        new Cover.Break(),
                },
                null);
        writeToFile(coverPng, "target/site/images/" + filename + ".png");

        Map<String, byte[]> resources = new HashMap<String, byte[]>();
        resources.put("OEBPS/img/cover.png", coverPng);
        resources.put("OEBPS/cover.html",
                Cover.generateCoverHtml(creator, title, "", creator).getBytes());
        createEpub(files.toArray(new File[files.size()]), resources, new File(epub), title, creator, tocNcx);
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
