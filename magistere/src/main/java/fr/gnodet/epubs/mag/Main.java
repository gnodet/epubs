package fr.gnodet.epubs.mag;

import fr.gnodet.epubs.core.Cover;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");
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
            byte[] coverPngData = Cover.generateCoverPng(
                    (i * 1.0 / books.getLength()),
                    title,
                    new Object[] {
                            new Cover.Text(creator.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                            new Cover.Break(),
                            new Cover.Text(title.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                            new Cover.Break(),
                            new Cover.Text(titlefr, new Font(Font.SERIF, Font.ITALIC, 58), 1.0, 0.0)
                    },
                    Main.class.getResource("coa/" + full + ".svg"));
            Files.createDirectories(Paths.get("target/site/images"));
            writeToFile(coverPngData, "target/site/images/" + file + ".png");
            Files.createDirectories(Paths.get("target/site/svgs"));
            Files.copy(Main.class.getResourceAsStream("coa/" + full + "-bw.svg"), Paths.get("target/site/svgs/" + file + ".svg"));
            int nbColumns = 5;
//            if (i % nbColumns == 0) {
//                indexHtml.append("<tr>");
//            }
//            indexHtml.append("<td><center>")
//                    .append("<a href='epub/").append(output).append(".epub'><img src='images/").append(output).append(".png'/></a>")
//                    .append("<br/><a href='readium-js-viewer/index.html?epub=../library/").append(output).append("'>Lecture</a>")
//                    .append("</center></td>");
//            if ((i + 1) % nbColumns == 0 || i == books.getLength() - 1) {
//                indexHtml.append("</tr>");
//            }
//            URL url = new URL("http://www.vatican.va/holy_father/" + full + "/encyclicals/documents/" + file);
//            URL url = new URL("http://w2.vatican.va/content/" + full + "/encyclicals/documents/" + file);
//            URL url = new URL("http://w2.vatican.va/content/" + full + "/fr/encyclicals/documents/" + file);
//            try {
//                process(url, "target/cache/" + file, "target/html/" + output + ".html", title, full, creator);
//                Map<String, byte[]> resources = new HashMap<String, byte[]>();
//                resources.put("OEBPS/img/" + full + "-bw.svg",
//                              readFully(Main.class.getResource("coa/" + full + "-bw.svg")));
//                resources.put("OEBPS/img/cover.png", coverPngData);
//                resources.put("OEBPS/cover.html", Cover.generateCoverHtml(creator, titlefr, title, full).getBytes());
//                createEpub(new File[] { new File("target/html/" + output + ".html") },
//                           resources,
//                           new File("target/site/epub/" + output + ".epub"),
//                           title, creator, null);
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
        }
//        indexHtml.append("</table>");
//        String template = new String(readFully(Main.class.getResource("template.html")));
//        template = template.replace("${TITLE}", "Lettres encycliques");
//        template = template.replace("${CONTENT}", indexHtml.toString());
//        writeToFile(template, "target/site/encycliques.html");
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
