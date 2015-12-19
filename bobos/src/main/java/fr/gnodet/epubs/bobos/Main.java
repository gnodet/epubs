package fr.gnodet.epubs.bobos;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

import com.google.common.io.Files;
import fr.gnodet.epubs.core.Cover;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Main {

    public static void main(String[] args) throws Exception {

        /*
        File[] pdfs = new File("res/").listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("LES BOBOS EN VÉRITÉ.pdf");
            }
        });
        for (File file : pdfs) {
            PDDocument pdf = PDDocument.load(file, "");
            PDFText2HTML stripper = new PDFText2HTML();
            String txt = stripper.getText(pdf);
            Files.write(txt, new File(file.toString() + ".html"), Charset.defaultCharset());
        }
        */

        String title = "Les Bobos en Vérité";
        String subtitle = "Le nouveau paganisme mondial de l’Antéchrist";
        String creator = "Philippe ARIÑO";

            byte[] coverPng = Cover.generateCoverPng(Math.random(),
                    title,
                    new Object[]{
                            new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                            new Cover.Break(),
                            new Cover.Text(title, new Font(Font.SERIF, Font.PLAIN, 98), 1.2, 0.25),
                            new Cover.Break(),
                            new Cover.Text(subtitle, new Font(Font.SERIF, Font.PLAIN, 48), 1.2, 0.25),
                            new Cover.Break(),
                    },
                    null,
                    Main.class.getResource("/arino-bobos.jpg"));
            System.out.println("Copying cover to: " + System.getProperty("basedir") + "/target/docbkx/epub3/");
            writeToFile(coverPng, System.getProperty("basedir") + "/target/docbkx/epub3/cover.png");
    }

}
