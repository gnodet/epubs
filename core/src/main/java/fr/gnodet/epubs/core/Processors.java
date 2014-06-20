package fr.gnodet.epubs.core;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processors {

    public interface Processor {
        String process(String text);
    }

    public static String process(String document, String regexp, int group, Processor processor) {
        Matcher paragraph = Pattern.compile(regexp).matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start(group)));
            newDoc.append(processor.process(paragraph.group(group)));
            start = paragraph.end(group);
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    public static class RelativeURIProcessor implements Processor {
        final URI buri;
        public RelativeURIProcessor(String buri) {
            this.buri = URI.create(buri);
        }
        public RelativeURIProcessor(URI buri) {
            this.buri = buri;
        }
        @Override
        public String process(String text) {
            String r = buri.resolve(text).toString();
            r = buri.relativize(URI.create(r)).toString();
            return r;
        }
    }

}
