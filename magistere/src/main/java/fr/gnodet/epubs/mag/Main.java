package fr.gnodet.epubs.mag;

import fr.gnodet.epubs.core.Cover;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static fr.gnodet.epubs.core.IOUtil.readFully;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Main {

    static final javax.xml.parsers.DocumentBuilderFactory dbf;

    static {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    public static void main(String[] args) throws Exception {
        generateCovers("rc-list.xml");
        generateCovers("dis-list.xml");
        generateCovers("enc-list.xml");
        generateCovers("exh-list.xml");
        generateCovers("let-list.xml");
        generateCovers("vatican-ii-list.xml");
        generateCovers("var-list.xml");
    }

    private static void generateCovers(String resource) throws Exception {
        Document enclist = dbf.newDocumentBuilder().parse(Main.class.getResourceAsStream(resource));
        String type = enclist.getDocumentElement().getAttribute("type");
        String ltitle = enclist.getDocumentElement().getAttribute("title");
        String lfile = enclist.getDocumentElement().getAttribute("file");
        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");

        int nbColumns = 5;
        StringBuilder indexHtml = new StringBuilder();
        indexHtml.append("        <h3>").append(ltitle).append("</h3>\n");
        indexHtml.append("        <table>\n");

        for (int i = 0; i < books.getLength(); i++) {
            Element book = (Element) books.item(i);
            String title = book.getAttribute("title");
            String titlefr = book.getAttribute("title-fr");
            String creator = book.getAttribute("creator");
            String date = book.getAttribute("date");
            String full = getFull(creator);
            String file = book.getAttribute("file");
            if (file == null || file.isEmpty()) {
                file = type + "_" + date + "_hf_" + full + "_" + title.toLowerCase().replaceAll("\\s", "-").replaceAll("æ", "ae");
            }

            System.err.println("     Generating " + file + ".png");
            int height = 1084;
            int width = (height * 297) / (210 * 2); // A5 format
            String coverSvgData = Cover.createCoverSvg(
                    (i * 1.0 / books.getLength()),
                    new Object[] {
                            new Cover.Text(creator.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                            new Cover.Break(),
                            new Cover.Text(title.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                            new Cover.Break(),
                            new Cover.Text(titlefr, new Font(Font.SERIF, Font.ITALIC, 58), 1.0, 0.0)
                    },
                    Main.class.getResource("coa/" + full + ".svg"), null,
                    height, width);
            writeToFile(coverSvgData, "target/site/svgs/" + file + ".svg");
            byte[] coverPngData = Cover.createCoverPng(
                    coverSvgData,
                    height, width);
            writeToFile(coverPngData, "target/site/images/" + file + ".png");
            Files.copy(Main.class.getResourceAsStream("coa/" + full + "-bw.svg"),
                       Paths.get("target/site/svgs/" + file + "_coa-bw.svg"),
                       StandardCopyOption.REPLACE_EXISTING);

            if (i % nbColumns == 0) {
                indexHtml.append("          <tr>\n");
            }
            indexHtml.append("            <td><center>")
                    .append("<a href='epub/").append(file).append(".epub'><img src='images/").append(file).append(".png'/></a>")
                    .append("<br/><a href='readium-js-viewer/index.html?epub=../library/").append(file).append("'>Lecture</a>")
                    .append(" <a href='pdf/").append(file).append(".pdf'>(PDF)</a>")
                    .append("</center></td>\n");
            if ((i + 1) % nbColumns == 0 || i == books.getLength() - 1) {
                indexHtml.append("          </tr>\n");
            }
        }
        indexHtml.append("        </table>\n");
        String template = new String(readFully(Main.class.getResource("template.html")));
        template = template.replace("${TITLE}", ltitle);
        template = template.replace("${CONTENT}", indexHtml.toString());
        writeToFile(template, "target/site/" + lfile + ".html");
    }

    private static String getFull(String name) {
        if ("Benoît XVI".equals(name)) {
            return "benedict-xvi";
        } else if ("Benoît XV".equals(name)) {
             return "benedict-xv";
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
        } else if ("Pie XII".equals(name)) {
            return "pius-xii";
        } else {
            return "papacy";
        }
    }

}
