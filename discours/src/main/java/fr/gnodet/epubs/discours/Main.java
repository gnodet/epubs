package fr.gnodet.epubs.discours;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import fr.gnodet.epubs.core.Cover;
import fr.gnodet.epubs.core.Processors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static fr.gnodet.epubs.core.EPub.createEpub;
import static fr.gnodet.epubs.core.IOUtil.copy;
import static fr.gnodet.epubs.core.IOUtil.loadTextContent;
import static fr.gnodet.epubs.core.IOUtil.readFully;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Main {

    static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String[] args) throws Exception {

        int nbColumns = 5;

        dbf.setNamespaceAware(true);

        StringBuilder indexHtml = new StringBuilder();
        indexHtml.append("<h3>Discours</h3>");
        indexHtml.append("<table>");

        Document enclist = dbf.newDocumentBuilder().parse(Main.class.getResourceAsStream("discours-list.xml"));
        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {

            Element book = (Element) books.item(i);
            String file = book.getAttribute("file");
            String title = book.getAttribute("title");
            String creator = book.getAttribute("creator");
            String date = book.getAttribute("date");
            String full = getFull(creator);
            String output = "discours_" + date + "_hf_" + full + "_" + title.toLowerCase().replaceAll("\\s", "-")
                    .replaceAll("æ", "ae")
                    .replaceAll("'", "-");

            byte[] coverPng = Cover.generateCoverPng(
                    (i * 1.0 / books.getLength()),
                    title,
                    new Object[] {
                            new Cover.Text(creator.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                            new Cover.Break(),
                            new Cover.Text(title, new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                            new Cover.Break(),
                    },
                    Main.class.getResource("coa/" + full + ".svg"));
            System.out.println("Copying cover to: " + System.getProperty("basedir") + "/target/site/images/");
            writeToFile(coverPng, System.getProperty("basedir") + "/target/site/images/" + output + ".png");
            if (i % nbColumns == 0) {
                indexHtml.append("<tr>");
            }
            indexHtml.append("<td><center>")
                    .append("<a href='epub/").append(output).append(".epub'><img src='images/").append(output).append(".png'/></a>")
                    .append("<br/><a href='readium-js-viewer/index.html?epub=../library/").append(output).append("'>Lecture</a>")
                    .append("</center></td>");
            if ((i + 1) % nbColumns == 0 || i == books.getLength() - 1) {
                indexHtml.append("</tr>");
            }
        }

        indexHtml.append("</table>");
        String template = new String(readFully(Main.class.getResource("template.html")));
        template = template.replace("${TITLE}", "Discours");
        template = template.replace("${CONTENT}", indexHtml.toString());
        writeToFile(template, "target/site/discours.html");
    }

    private static String getFull(String name) {
        if ("Benoît XVI".equals(name)) {
            return "benedict-xvi";
        } else if ("Jean-Paul II".equals(name)) {
            return "john-paul-ii";
        } else if ("Paul VI".equals(name)) {
            return "paul-vi";
        } else if ("François".equals(name)) {
            return "francesco";
        } else if ("Léon XIII".equals(name)) {
            return "leo-xiii";
        } else if ("Pie XI".equals(name)) {
            return "pius-xi";
        }
        throw new IllegalStateException("Unknown: " + name);
    }

}
