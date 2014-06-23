package fr.gnodet.epubs.cec;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.xml.sax.InputSource;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.EPub.createToc;
import static fr.gnodet.epubs.core.IOUtil.loadTextContent;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;
import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Quotes.fixQuotesInList;
import static fr.gnodet.epubs.core.Tidy.tidyHtml;
import static fr.gnodet.epubs.core.Tidy.translateEntities;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespaces;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespacesInList;

public class Main {

    public static void main(String[] args) throws Exception {

        String filename = "catechisme_de_l_eglise_catholique";
        String epub = "target/site/epub/" + filename + ".epub";
        String title = "Catéchisme de l'Église Catholique";
        String creator = "Église Catholique";
        String burl = "http://www.vatican.va/archive/FRA0013/";
        String firstFile = "_INDEX.HTM";

        String tocNcx = null; //createToc(epub);
        byte[] coverPng = Cover.generateCoverPng(Math.random(),
                title,
                new Object[] {
                        new Cover.Break(),
                        new Cover.Text(title.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                        new Cover.Break(),
                },
                Main.class.getResource("papacy.svg"));
        writeToFile(coverPng, "target/site/images/" + filename + ".png");

        Set<String> toDownload = new LinkedHashSet<String>();
        Set<String> downloaded = new LinkedHashSet<String>();
        toDownload.add(firstFile);
        while (!toDownload.isEmpty()) {
            String name = toDownload.iterator().next();
            toDownload.remove(name);

            if (!name.equals(firstFile) && !name.startsWith("__P")) {
                continue;
            }
            try {
                String document = loadTextContent(new URL(burl + name), "target/cache/" + name);
                document = tidyHtml(document);
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

            if (file.equals(firstFile)) {

                String str1 = "<font face=Arial size=1><ul type=square><li><font size=3><a href=__P1.HTM>LISTE DES SIGLES</a>";
                String str2 = "<hr noshade><center><font size=+1><a name=fonte>Cr&eacute;dits</a>";
                int idx1 = document.indexOf(str1);
                int idx2 = document.indexOf(str2);
                if (0 < idx1 && idx1 < idx2) {
                    document = "<html><head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" /></meta><body>" + document.substring(idx1, idx2) + "</body></html>";
                }

                document = document.replaceAll("<font[^>]*>", "");
                document = document.replaceAll("</font>", "");
                document = document.replaceAll("<div[^>]*>", "");
                document = document.replaceAll("</div>", "");
                document = document.replaceAll("<ul type=square>", "<ul>");
                document = tidyHtml(document);

                // Use xhtml 1.1
                document = document.replaceAll(
                        "<!DOCTYPE[^>]*>",
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");

                document = translateEntities(document);
                document = document.replaceAll("\\.\\.\\.", "…");
                document = fixQuotesInList(document);
                document = fixWhitespacesInList(document);

                document = document.replaceAll("<div[^>]*>", "");
                document = document.replaceAll("</div>", "");

            } else {

                String str1 = "<hr size=1 noshade>";
                String str2 = "<center><br><br><hr size=1 width=70%>";
                int idx1 = document.indexOf(str1);
                int idx2 = document.indexOf(str2);
                if (0 < idx1 && idx1 < idx2) {
                    document = "<html>\n" +
                               "  <head>\n" +
                               "    <style type=\"text/css\">\n" +
                               "      table tbody tr td p { margin: 0 1em 0 1em }\n" +
                               "      .center { text-align: center; }\n" +
                               "      .numpara { font-family: Verdana; font-size: smaller; font-weight: bold; }\n" +
                               "    </style>\n" +
                               "    <meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n" +
                               "  </head>\n" +
                               "  <body>\n" +
                               document.substring(idx1 + str1.length(), idx2) +
                               "  </body>\n" +
                               "</html>\n";
                }

                document = document.replaceAll("\\sstyle='mso-bidi-font-weight\\s*:\\s*normal'", "");
                document = document.replaceAll("\\sclass=MsoNormal", "");
                document = document.replaceAll("<p style='margin-left:72.0pt'>([\\s\\S]*?)</p>", "<blockquote><p>$1</p></blockquote>");
                document = document.replaceAll("align=center style='text-align:center'", "class=\"center\"");

                document = document.replaceAll("<table[^>]*>", "<table>");
                document = document.replaceAll("<tr[^>]*>", "<tr>");
                document = document.replaceAll("<td[^>]*>", "<td>");
                document = document.replaceAll("<i>\\s+</i>", " ");

                document = tidyHtml(document);

                // Fix head section
                document = document.replaceAll("<meta\\s+name=\"generator\"\\s+content=\".*?\"\\s*/>", "");
                document = document.replaceAll("<title></title>", "<title>Catéchisme de l'Église Catholique</title>");

                // Use xhtml 1.1
                document = document.replaceAll(
                        "<!DOCTYPE[^>]*>",
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");

                document = translateEntities(document);
                document = document.replaceAll("\\.\\.\\.", "…");
                document = fixQuotes(document);
                document = fixNumberedParagraphs(document);
                document = fixWhitespaces(document);

            }

            document = document.replaceAll("PREMIERE", "PREMIÈRE");
            document = document.replaceAll("DEUXIEME", "DEUXIÈME");
            document = document.replaceAll("TROISIEME", "TROISIÈME");
            document = document.replaceAll("QUATRIEME", "QUATRIÈME");
            document = document.replaceAll("CINQUIEME", "CINQUIÈME");
            document = document.replaceAll("SIXIEME", "SIXIÈME");
            document = document.replaceAll("SEPTIEME", "SEPTIÈME");
            document = document.replaceAll("HUITIEME", "HUITIÈME");
            document = document.replaceAll("NEUVIEME", "NEUVIÈME");
            document = document.replaceAll("DIXIEME", "DIXIÈME");
            document = document.replaceAll("GUERISON", "GUÉRISON");
            document = document.replaceAll("CELEBRATIONS", "CÉLÉBRATIONS");
            document = document.replaceAll("CHRETIEN", "CHRÉTIEN");
            document = document.replaceAll("FUNERAILLES", "FUNÉRAILLES");
            document = document.replaceAll("DIGNITE", "DIGNITÉ");
            document = document.replaceAll("COMMUNAUTE", "COMMUNAUTÉ");
            document = document.replaceAll("GRACE", "GRÂCE");
            document = document.replaceAll("MERE", "MÈRE");
            document = document.replaceAll("PERE", "PÈRE");
            document = document.replaceAll("CREATEUR", "CRÉATEUR");
            document = document.replaceAll("JESÚS", "JÉSUS");
            document = document.replaceAll("JESUS", "JÉSUS");
            document = document.replaceAll("CRUCIFIE", "CRUCIFIÉ");
            document = document.replaceAll("NE DE LA", "NÉ DE LA");
            document = document.replaceAll("MYSTERE", "MYSTÈRE");
            document = document.replaceAll("A ETE", "A ÉTÉ");
            document = document.replaceAll("RESSUSCITE", "RESSUSCITÉ");
            document = document.replaceAll("MONTE", "MONTÉ");
            document = document.replaceAll("SIEGE", "SIÈGE");
            document = document.replaceAll("D’OU", "D’OÙ");
            document = document.replaceAll("FIDELE", "FIDÈLE");
            document = document.replaceAll("PECHE", "PÉCHÉ");
            document = document.replaceAll("HIERARCHIE", "HIÉRARCHIE");
            document = document.replaceAll("CONSACRE", "CONSACRÉ");
            document = document.replaceAll("RESURRECTION", "RÉSURRECTION");
            document = document.replaceAll("ETERNEL", "ÉTERNEL");
            document = document.replaceAll("CELEBRATION", "CÉLÉBRATION");
            document = document.replaceAll("TRINITE", "TRINITÉ");
            document = document.replaceAll("DE ÉGLISE", "DE L’ÉGLISE");
            document = document.replaceAll("CELEBRER", "CÉLÉBRER");
            document = document.replaceAll("DIVERSITE", "DIVERSITÉ");
            document = document.replaceAll("UNITE", "UNITÉ");
            document = document.replaceAll("BAPTEME", "BAPTÊME");
            document = document.replaceAll("a la beatitude", "à la béatitude");
            document = document.replaceAll("Egalité", "Égalité");
            document = document.replaceAll("EDUCATRICE", "ÉDUCATRICE");
            document = document.replaceAll("AME", "ÂME");
            document = document.replaceAll("MEME", "MÊME");
            document = document.replaceAll("PRIERE", "PRIÈRE");
            document = document.replaceAll("A LA", "À LA");
            document = document.replaceAll("TESTÂMENT", "TESTAMENT");
            document = document.replaceAll("PLENITUDE", "PLÉNITUDE");
            document = document.replaceAll("EVANGILE", "ÉVANGILE");
            document = document.replaceAll("RESUME", "RÉSUMÉ");
            document = document.replaceAll("REPONSE", "RÉPONSE");
            document = document.replaceAll("REVELATION", "RÉVÉLATION");
            document = document.replaceAll("SACRÂMENTELLE", "SACRAMENTELLE");

            if (file.equals(firstFile)) {
                String toc = document.substring(document.indexOf("<body>") + 6, document.indexOf("</body>"));
                toc = toc.replaceAll("<ul[^>]*>", "").replaceAll("</ul>", "");
                toc = toc.replaceAll("<br ?/>", "");
                toc = toc.replaceAll("<li>", "\n<li>");
                toc = toc.replaceAll("\n\n", "\n");
                toc = toc.replaceAll("\n\n", "\n");
                toc = toc.replaceAll("\n\n", "\n");

                toc = toc.replaceAll("<li style=\"list-style : none\">\\s*(<li>.*</li>)\\s*</li>", "$1");

                toc = toc.replaceAll("<li><a href=\"([^\"]*)\">\\s*([^<]*)\\s*</a>\\s*</li>", "<item text=\"$2\" ref=\"$1\"/>");
                toc = toc.replaceAll("<li><a href=\"([^\"]*)\">\\s*([^<]*?)\\s*</a>", "<item text=\"$2\" ref=\"$1\">");
                toc = toc.replaceAll("<li>\\s*([^<]*)\\s*", "<item text=\"$1\">");
                toc = toc.replaceAll("</li>", "</item>");

                toc = "<items>" + toc + "</items>";

                tocNcx = createToc(title, toc);
            }

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

    private static String fixNumberedParagraphs(String document) {
        document = document.replaceAll("<p>([1-9][0-9]*) ", "<p><a class=\"numpara\" id=\"p$1\">$1.</a> ");
        document = document.replaceAll("<p>([1-9][0-9]*)<i>", "<p><a class=\"numpara\" id=\"p$1\">$1.</a> <i>");
        return document;
    }

}
