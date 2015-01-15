package fr.gnodet.epubs.fillion;

import java.awt.*;

import fr.gnodet.epubs.core.Cover;

import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Main {

    public static void main(String[] args) throws Exception {

        String[] evangelists = new String[] {
                "Matthieu", "Marc", "Luc", "Jean"
        };

        String title = "Évangile selon saint ";
        String creator = "Louis-Claude FILLION";

        for (String evang : evangelists) {
            byte[] coverPng = Cover.generateCoverPng(Math.random(),
                    title,
                    new Object[]{
                            new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                            new Cover.Break(),
                            new Cover.Text(title + evang, new Font(Font.SERIF, Font.PLAIN, 118), 1.2, 0.25),
                            new Cover.Break(),
                    },
                    null);
            System.out.println("Copying cover to: " + System.getProperty("basedir") + "/target/docbkx/epub3/" + evang.toLowerCase() + "/xml/");
                    writeToFile(coverPng, System.getProperty("basedir") + "/target/docbkx/epub3/" + evang.toLowerCase() + "/xml/cover.png");
        }
    }

}
