package com.adobe.epubcheck.reporting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import com.adobe.epubcheck.util.JsonWriter;
import com.adobe.epubcheck.util.PathUtil;

import static fr.gnodet.epubs.core.IOUtil.readFully;
import static fr.gnodet.epubs.core.IOUtil.writeToFile;


public class HtmlReport extends CheckingReport
{

    URL templateUrl;

    public HtmlReport(String ePubName, String out, URL templateUrl)
    {
        super(ePubName, out);
        this.templateUrl = templateUrl;
    }

    public int generate()
    {
        setStopDate();
        setParameters();
        sortCollections();

        int returnCode;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonWriter jw = JsonWriter.createJsonWriter(true);
            jw.writeJson(this, baos);

            int fatals = 0, errors = 0, warnings = 0;
            StringBuilder table = new StringBuilder();
            table.append("<table><tr><th>Niveau</th><th>Message</th></tr>");
            for (CheckMessage msg : messages) {
                switch (msg.getSeverity()) {
                case FATAL:   fatals++; break;
                case ERROR:   errors++; break;
                case WARNING: warnings++; break;
                }
                String str = msg.toString();
                str = str.replaceFirst("[\\s\\S]*DESCRIPTION \\(long\\): ([^\n]*)\n[\\s\\S]*", "$1");
                table.append("<tr><td>").append(msg.getSeverity()).append("</td><td>").append(str).append("</td></tr>");
            }
            table.append("</table>");

            String epub = new File(getEpubFileName()).getName();
            String details = "";
            if (fatals > 0) {
                details = fatals + " fatales";
            }
            if (errors > 0) {
                if (details.length() > 0) {
                    details += ", ";
                }
                details += errors + " erreurs";
            }
            if (warnings > 0) {
                if (details.length() > 0) {
                    details += ", ";
                }
                details += warnings + " warnings";
            }
            String content =
                    "<h2>" + "Rapport EpubCheck&nbsp;: " + epub + "</h2>\n" +
                    "<h3>Résumé</h3>\n" +
                    "<ul><li><b>Statut</b>&nbsp;: <b>" +  (fatals + errors > 0 ? "Invalide" : "Valide") + "</b>" +
                            (details.isEmpty() ? "" : "(" + details + ")") +
                            "</li></ul>\n" +
                    (fatals + errors + warnings > 0 ? "<h3>Messages</h3>\n" + table : "") +
                    "<h3>Rapport complet</h3>\n" +
                    "<div id=\"divreport\"></div>";

            String template = new String(readFully(templateUrl));
            template = template.replace("${TITLE}", "EpubCheck Report for " + epub);
            template = template.replace("${REPORT}", baos.toString());
            template = template.replace("${IDREPORT}", "divreport");
            template = template.replace("${CONTENT}", content);
            writeToFile(template, outputFile);

            returnCode = 0;
        }
        catch (IOException e)
        {
            System.err.println("IOException error: " + e.getMessage());
            returnCode = 1;
        }
        return returnCode;
    }

}
