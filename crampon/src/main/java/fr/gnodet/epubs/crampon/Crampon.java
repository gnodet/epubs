package fr.gnodet.epubs.crampon;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.reporting.HtmlReport;
import fr.gnodet.epubs.core.EPub;

import java.io.File;

public class Crampon {

    public static void main(String[] args) throws Exception {
        File output = new File(args[0]);
        // Checking
        Report report = new HtmlReport(output.toString(),
                output.toString().replace(".epub", "-report.html"),
                EPub.class.getResource("template-report.html"));
        report.initialize();
        EpubCheck checker = new EpubCheck(output, report);
        checker.validate();
        report.generate();
    }
}
