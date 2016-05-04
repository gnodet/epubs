package fr.gnodet.epubs.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Whitespaces {

    public static String fixWhitespaces(String document) {
        Matcher paragraph = Pattern.compile("<p>([^<]+|<(?!p>)|<footnote><p>[\\s\\S]*?</p></footnote>)*?</p>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(fixWhitespacesInParagraph(paragraph.group()));
            start = paragraph.end();
        }
        newDoc.append(document.substring(start, document.length()));
        document = newDoc.toString();
        // Clean hrefs
        int len;
        do {
            len = document.length();
            document = document.replaceAll("(href=\"[^\"\\s\u00a0]*)[\\s\u00a0]*", "$1");
        } while (document.length() != len);
        return document;
    }

    public static String fixWhitespacesInList(String document) {
        Matcher paragraph = Pattern.compile("<li[\\s\\S]*?</li>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(fixWhitespacesInParagraph(paragraph.group()));
            start = paragraph.end();
        }
        newDoc.append(document.substring(start, document.length()));
        document = newDoc.toString();
        // Clean hrefs
        int len;
        do {
            len = document.length();
            document = document.replaceAll("(href=\"[^\"\\s\u00a0]*)[\\s\u00a0]*", "$1");
        } while (document.length() != len);
        return document;
    }

    public static String fixWhitespacesInParagraph(String text) {
        text = text.replaceAll("\\bCf\\. ", "Cf.\u00A0");
        text = text.replaceAll("\\bcf\\. ", "cf.\u00A0");
        text = text.replaceAll("\\bp\\. ([0-9])", "p.\u00A0$1");
        text = text.replaceAll("\\bpp\\. ([0-9])", "pp.\u00A0$1");
        text = text.replaceAll("\\bq\\. ([0-9])", "q.\u00A0$1");
        text = text.replaceAll("\\p{IsWhitespace}+<footnote>", "<footnote>");

        text = text.replaceAll("\n", " ");
        text = text.replaceAll("(\\s+)(</[^>]*>)", "$2$1");
        text = text.replaceAll("«\\s*", "«\u00A0");
        text = text.replaceAll("\\s*»", "\u00A0»");
        text = text.replaceAll("“\\s*", "“");
        text = text.replaceAll("\\s*”", "”");
        text = text.replaceAll("\\s*:\\s*", "\u00A0: ");
        text = text.replaceAll("\\s*(?<!&#[0-9]{1,4}|&[A-Za-z0-9]{1,10});\\s*", "\u00A0; ");
        text = text.replaceAll("\\s*!\\s*", "\u00A0! ");
        text = text.replaceAll("\\s*\\?\\s*", "\u00A0? ");
        text = text.replaceAll("\\(\\s*", "(");
        text = text.replaceAll("\\s*\\)", ")");
        text = text.replaceAll("\\s*\\.([^<\\)])", ". $1");
        text = text.replaceAll("\\s*,\\s*", ", ");
        text = text.replaceAll("\\s*…\\s*", "… ");
        text = text.replaceAll("… ([\\)\\]])", "…$1");
        text = text.replaceAll("( *\u00A0 *)( *\u00A0 *)*", "\u00A0");
        text = text.replaceAll(" +", " ");
        text = text.replaceAll("([\\s\u00A0])-([\\s\u00a0,])", "$1\u2014$2");

        // Fix back broken entities
        text = text.replaceAll("&([A-Za-z]+)\u00a0;", "&$1;");
        // Fix back broken paragraph numeration
        text = text.replaceAll("([0-9])\\. ([0-9])\\)", "$1.$2)");
        // Fix back broken xml attributes
        text = text.replaceAll("(<[^>]+?)\u00a0: ([^>]+>)", "$1:$2");
        return text;
    }

}
