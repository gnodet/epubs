package fr.gnodet.epubs.discours;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.reporting.HtmlReport;
import fr.gnodet.epubs.core.Cover;
import fr.gnodet.epubs.core.EPub;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;

import static fr.gnodet.epubs.core.IOUtil.readFully;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Check {

    static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String[] args) throws Exception {
        dbf.setNamespaceAware(true);

        Document enclist = dbf.newDocumentBuilder().parse(Check.class.getResourceAsStream("discours-list.xml"));
        NodeList books = enclist.getDocumentElement().getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {

            Element book = (Element) books.item(i);
            String file = book.getAttribute("file");
            String title = book.getAttribute("title");
            String creator = book.getAttribute("creator");
            String date = book.getAttribute("date");
            String full = getFull(creator);
            String out = "discours_" + date + "_hf_" + full + "_" + title.toLowerCase().replaceAll("\\s", "-")
                    .replaceAll("æ", "ae")
                    .replaceAll("'", "-");
            File output = new File("target/site/epub/" + out + ".epub");

            Report report = new HtmlReport(output.toString(),
                                           output.toString().replace(".epub", "-report.html"),
                                           EPub.class.getResource("template-report.html"));
            report.initialize();
            EpubCheck checker = new EpubCheck(output, report);
            checker.validate();
            report.generate();
        }

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
