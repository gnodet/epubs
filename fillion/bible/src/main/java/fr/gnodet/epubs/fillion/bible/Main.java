package fr.gnodet.epubs.fillion.bible;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rtfparserkit.parser.IRtfSource;
import com.rtfparserkit.parser.RtfListenerAdaptor;
import com.rtfparserkit.parser.RtfStreamSource;
import com.rtfparserkit.parser.standard.StandardRtfParser;
import com.rtfparserkit.rtf.Command;
import fr.gnodet.epubs.core.Cover;
import fr.gnodet.epubs.core.EPub;

import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Main {

    public static void main(String[] args) throws Exception {

        Properties livres = new Properties();
        livres.load(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("livres.properties")));

        File inputDir = new File(System.getProperty("workingDirectory", "."), "src/main/rtf");
        File outputDir = new File(System.getProperty("workingDirectory", "."), "target/html");
        outputDir.mkdirs();

        String[] files = inputDir.list();
        Arrays.sort(files);
        for (String name : files) {
            StandardRtfParser parser = new StandardRtfParser();
            IRtfSource source = new RtfStreamSource(new FileInputStream(new File(inputDir, name)));

            String abbr = name.substring(5, name.length() - 4);

            XhtmlRtfListener listener = new XhtmlRtfListener(livres.getProperty(abbr));
            parser.parse(source, listener);
            Writer writer = new FileWriter(new File(outputDir, name.substring(0, name.length() - 3) + "xhtml"));
            writer.write(listener.getOutput());
            writer.close();
        }

        StringBuilder index = new StringBuilder();
        StringBuilder toc = new StringBuilder();
        index.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        index.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
        index.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        index.append("<head>\n" +
                     "  <meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\" />\n" +
                     "  <style type=\"text/css\">\n" +
                     "    .numero_verset { vertical-align: baseline; position: relative; top: -0.4em; font-size: 75%; color: #2554C7; font-weight: bold; margin-right: 0.2em; }\n" +
                     "    a, h1, h2 { color: #2554C7; }\n" +
                     "  </style>\n" +
                     "  <title>Bible</title>\n" +
                     "</head>\n" +
                     "<body>\n" +
                     "  <h1>Bible</h1>\n");
        index.append("<div id='ancien_testament'>\n");
        index.append("  <h2 class='titre'> <a href='#ancien_testament'>Ancien testament</a> </h2>\n");
        index.append("  <ul class='livres'>\n");

        toc.append("<items>\n" +
                   "  <item text=\"Table des matières\" ref=\"index.xhtml\" />\n" +
                   "  <item text=\"Ancien testament\">\n");
        for (String name : files) {
            if (name.startsWith("at")) {
                String abbr = name.substring(5, name.length() - 4);
                String info = livres.getProperty(abbr);
                String title = info.split(",")[1];
                String gen = name.replace(".rtf", ".xhtml");
                index.append("<li><a href='").append(gen).append("'>").append(title).append("</a></li>\n");
                toc.append("    <item text='").append(title).append("' ref='").append(gen).append("'/>\n");
            }
        }
        index.append("  </ul>\n");
        index.append("</div>\n");
        index.append("<div id='nouveau_testament'>\n");
        index.append("  <h2 class='titre'> <a href='#nouveau_testament'>Nouveau testament</a> </h2>\n");
        index.append("  <ul class='livres'>\n");
        toc.append("  </item>\n" +
                   "  <item text=\"Nouveau testament\">\n");
        for (String name : files) {
            if (name.startsWith("nt")) {
                String abbr = name.substring(5, name.length() - 4);
                String info = livres.getProperty(abbr);
                String title = info.split(",")[1];
                String gen = name.replace(".rtf", ".xhtml");
                index.append("<li><a href='").append(gen).append("'>").append(title).append("</a></li>\n");
                toc.append("    <item text='").append(title).append("' ref='").append(gen).append("'/>\n");
            }
        }
        index.append("  </ul>\n");
        index.append("</div>\n");
        index.append("</body>\n");
        index.append("</html>\n");
        toc.append("  </item>\n" +
                   "</items>\n");

        Writer writer = new FileWriter(new File(outputDir, "index.xhtml"));
        writer.write(index.toString());
        writer.close();

        // Create epub
        String title = "Bible";
        String creator = "Louis-Claude FILLION";
        String creator2 = "Louis-Claude Fillion";
        String epub = "fillion-bible.epub";
        String tocNcx = EPub.createToc(title, toc.toString());
        byte[] coverPng = Cover.generateCoverPng(Math.random(),
                title,
                new Object[] {
                        new Cover.Text(creator, new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                        new Cover.Break(),
                        new Cover.Text("Bible", new Font(Font.SERIF, Font.PLAIN, 96), 1.1, 0.25),
                        new Cover.Text("Catholique", new Font(Font.SERIF, Font.PLAIN, 96), 1.1, 0.25),
                        new Cover.Break(),
                },
                null);
        writeToFile(coverPng, "target/site/images/" + "fillion-bible" + ".png");

        Map<String, byte[]> resources = new HashMap<String, byte[]>();
        resources.put("OEBPS/img/cover.png", coverPng);
        resources.put("OEBPS/cover.html",
                Cover.generateCoverHtml(creator, title, "", creator).getBytes());
        List<File> generated = new ArrayList<File>();
        generated.add(new File(outputDir, "index.xhtml"));
        for (String name : files) {
            generated.add(new File(outputDir, name.replace(".rtf", ".xhtml")));
        }
        EPub.createEpub(generated.toArray(new File[generated.size()]), resources, new File("target/site/epub/" + epub), title, creator2, tocNcx);
    }

    public static class XhtmlRtfListener extends RtfListenerAdaptor {

        final String info;
        final String abbr;
        final String title;

        final StringBuilder output = new StringBuilder();
        final Pattern pattern = Pattern.compile("([A-Za-zé ]*) ([1-9][0-9]*),([1-9][0-9, ]*)\\. (.*)");

        final List<List<Command>> commands = new ArrayList<List<Command>>();
        final List<Map<String, String>> chapters = new ArrayList<Map<String, String>>();
        int lastChapter = 0;
        boolean ignoreAll;

        public XhtmlRtfListener(String info) {
            this.info = info;
            if (info != null) {
                this.abbr = info.substring(0, info.indexOf(','));
                this.title = info.substring(info.indexOf(',') + 1);
            } else {
                abbr = null;
                title = null;
            }
        }

        @Override
        public void processDocumentStart() {
        }

        @Override
        public void processDocumentEnd() {
            StringBuilder head = new StringBuilder();
            head.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            head.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
            head.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
            head.append("<head>\n");
            head.append("  <meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\" />\n");
            head.append("  <style type=\"text/css\">\n");
            head.append("    .numero_verset { vertical-align: baseline; position: relative; top: -0.4em; font-size: 75%; color: #2554C7; font-weight: bold; margin-right: 0.2em; }\n");
            head.append("    a, h1, h2 { color: #2554C7; }\n");
            head.append("  </style>\n");
            head.append("  <title>").append(title).append("</title>\n");
            head.append("</head>\n");
            head.append("<body>\n");
            head.append("  <h1>").append(title).append("</h1>\n");

            head.append("  <div>\n");
            for (int i = 0; i < lastChapter; i++) {
                head.append("    <a href=\"#c-")
                        .append(abbr.toLowerCase())
                        .append("-")
                        .append(i + 1)
                        .append("\">[")
                        .append(i + 1)
                        .append("]</a>\n");
            }
            head.append("  </div>\n");

            output.insert(0, head.toString());
            output.append("</body>\n");
            output.append("</html>\n");

        }

        @Override
        public void processGroupStart() {
            commands.add(new ArrayList<Command>());
        }

        @Override
        public void processGroupEnd() {
            List<Command> group = commands.remove(commands.size() - 1);
        }

        @Override
        public void processCommand(Command command, int parameter, boolean hasParameter, boolean optional) {
            commands.get(commands.size() - 1).add(command);
        }

        @Override
        public void processString(String string) {
            List<Command> group = commands.get(commands.size() - 1);
            if (group.get(0) != Command.rtf) {
                return;
            }

            Matcher matcher = pattern.matcher(string);
            if (matcher.matches()) {
                String book = matcher.group(1);
                int chapter = Integer.parseInt(matcher.group(2));
                String verse = matcher.group(3);
                String text = matcher.group(4);

                if (chapter != lastChapter) {
                    chapters.add(new LinkedHashMap<String, String>());
                    lastChapter = chapter;
                }

                text = fixTypos(text);

                if (verse.equals("16, 17, 18")) {
                    verse = "18";
                }

                output.append("<div id=\"v-")
                        .append(abbr.toLowerCase())
                        .append("-")
                        .append(chapter)
                        .append("-")
                        .append(verse)
                        .append("\">")
                        .append("<span class=\"numero_verset\">")
                        .append(verse.length() == 1 ? "0" + verse : verse)
                        .append("</span> ")
                        .append("<span class=\"content_verset\">")
                        .append(text)
                        .append("</span>")
                        .append("</div>\n");
            } else {
                string = string.trim();
                if (string.toLowerCase().startsWith("chapitre")
                        || string.toLowerCase().startsWith("psaume")) {
                    output.append("<h2 id=\"c-")
                            .append(abbr.toLowerCase())
                            .append("-")
                            .append(lastChapter + 1)
                            .append("\">")
                            .append(string)
                            .append("</h2>\n");
                } else {
                    String s1 = string.replaceAll("[^\\p{L}]", "");
                    String s2 = title.replaceAll("[^\\p{L}]", "");
                    if (s1.equalsIgnoreCase(s2)) {
                        // ignore
                    } else if (string.contains("Fillion")) {
                        ignoreAll = true;
                    } else if (ignoreAll) {
                        // ignore
                    } else {
                        string = fixTypos(string);
                        output.append("<div><span>")
                                .append(string)
                                .append("</span></div>\n");
                    }
                }
            }
        }

        public String getOutput() {
            return output.toString();
        }
    }

    private static String fixTypos(String text) {
        text = text.replaceAll("P R O L O G U E", "PROLOGUE");
        text = text.replaceAll("\\s*([!?:;])", " $1");
        text = text.replaceAll("'", "’");

        text = text.replaceAll("([.?,;!:])(\\p{Lu})", "$1 $2");

        String[] words = { "Je", "Tu", "Il", "Elle", "Nous", "Vous", "Ils",
                            "Mon", "Ton", "Son", "Notre", "Votre", "Leur",
                            "Ma", "Ta", "Sa",
                            "Mes", "Tes", "Ses", "Nos", "Vos", "Leurs",
                            "Moi", "Toi", "Soi", "Lui", "Eux",
                            "Me", "M’", "Te", "T’", "Se", "S’", "Le", "La", "Les", "L’", "J’",
                            "Enfant", "Mère", "Ne", "Honore" };
        for (String word : words) {
            text = text.replaceAll("([\\p{L},;][ ’\\-])" + word + "\\b", "$1" + word.toLowerCase());
        }

        text = text.replaceAll("\\bEve\\b", "Ève");
        text = text.replaceAll("\\bEre\\b", "Ère");
        text = text.replaceAll("\\bEtes\\b", "Êtes");
        text = text.replaceAll("\\bEtre\\b", "Être");
        text = text.replaceAll("\\bEtres\\b", "Êtres");
        text = text.replaceAll("\\bE([bcdfgjklmnpqrstvwz][aeoiuyéh])", "É$1");
        text = text.replaceAll("\\bE([cdg][rl])", "É$1");

        text = text.replaceAll("\\bA\\b", "À");
        text = text.replaceAll("\\bO\\b", "Ô");
        text = text.replaceAll("\\bOte\\b", "Ôte");

        text = text.replaceAll("oe", "œ");
        return text;
    }
}
