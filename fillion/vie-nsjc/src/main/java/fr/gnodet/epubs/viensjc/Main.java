package fr.gnodet.epubs.viensjc;


import java.awt.*;

import fr.gnodet.epubs.core.Cover;

import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Main {

    public static void main(String[] args) throws Exception {

        String filename = "fillion-vie-nsjc";
        String title = "Vie de Notre-Seigneur Jésus-Christ";
        String creator = "Louis-Claude FILLION";

        for (int i = 1; i <= 3; i++) {
            String tome = "Tome ";
            for (int j = 0; j < i; j++) {
                tome += "I";
            }
            byte[] coverPng = Cover.generateCoverPng(Math.random(),
                    title,
                    new Object[]{
                            new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                            new Cover.Break(),
                            new Cover.Break(),
                            new Cover.Text(title, new Font(Font.SERIF, Font.PLAIN, 118), 1.2, 0.25),
                            new Cover.Break(),
                            new Cover.Text(tome, new Font(Font.SERIF, Font.PLAIN, 80), 1.0, 0.25),
                            new Cover.Break(),
                            new Cover.Break(),
                    },
                    null);
            System.out.println("Copying cover to: " + System.getProperty("basedir") + "/target/site/images/");
            writeToFile(coverPng, System.getProperty("basedir") + "/target/site/images/" + filename + "-t" + i + ".png");
        }
    }

}
