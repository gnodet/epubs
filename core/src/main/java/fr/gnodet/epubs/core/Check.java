package fr.gnodet.epubs.core;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.reporting.HtmlReport;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class Check {

    static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String[] args) throws Exception {
        dbf.setNamespaceAware(true);

        for (String arg : args) {
            File output = new File(arg);
            Report report = new HtmlReport(output.toString(),
                    output.toString().replace(".epub", "-report.html"),
                    EPub.class.getResource("template-report.html"));
            report.initialize();
            EpubCheck checker = new EpubCheck(output, report);
            checker.validate();
            report.generate();
        }
   }

}
