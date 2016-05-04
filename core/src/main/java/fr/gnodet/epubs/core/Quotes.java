package fr.gnodet.epubs.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Quotes {

    static final int UNKNOWN = 0;
    static final int OPEN = 1;
    static final int CLOSE = 2;

    public static String fixQuotes(String document) {
        Matcher paragraph = Pattern.compile("<p>([^<]+|<(?!p>)|<footnote><p>[\\s\\S]*?</p></footnote>)*?</p>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(fixQuotesInParagraph(paragraph.group()));
            start = paragraph.end();
        }
        if (start == 0) {
            paragraph = Pattern.compile("<div[\\s\\S]*?</div>").matcher(document);
            while (paragraph.find(start)) {
                newDoc.append(document.substring(start, paragraph.start()));
                newDoc.append(fixQuotesInParagraph(paragraph.group()));
                start = paragraph.end();
            }
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    public static String fixQuotesInList(String document) {
        Matcher paragraph = Pattern.compile("<li[\\s\\S]*?</li>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(fixQuotesInParagraph(paragraph.group()));
            start = paragraph.end();
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    public static String fixQuotesInParagraph(String text) {
        // Double quotes
        if (text.contains("\"")) {
            text = text.replaceAll("°", "&deg;");
            text = text.replaceAll("=\"([^\"]*)\"", "=°$1°");

            int count = count(text, "\"«“»”");
            if (count > 0) {
                if (count % 2 == 1) {
                    System.err.print("Odd number of quotes in paragraph:\n" + text + "\n\n");
                } else {
                    char[] buffer = text.toCharArray();
                    int[] pos = new int[count];
                    int[] typ = new int[count];
                    int nb = 0;
                    for (int i = 0; i < buffer.length; i++) {
                        if (buffer[i] == '«' || buffer[i] == '“') {
                            pos[nb] = i;
                            typ[nb] = OPEN;
                            nb++;
                        } else if (buffer[i] == '»' || buffer[i] == '”') {
                            pos[nb] = i;
                            typ[nb] = CLOSE;
                            nb++;
                        } else if (buffer[i] == '"') {
                            pos[nb] = i;
                            for (int j = i - 1; j >= 0; j--) {
                                char c = buffer[j];
                                if (Character.isWhitespace((int) c)) {
                                    continue;
                                }
                                if (c == ':' || c == ',' || c == ';' || c == '(' || c == '\u2014' || c == '«') {
                                    typ[nb] = OPEN;
                                } else if (j >= 2 && buffer[j - 2] == '<' && buffer[j - 1] == 'i' && buffer[j] == '>') {
                                    typ[nb] = OPEN;
                                }
                                break;
                            }
                            for (int j = i + 1; j < buffer.length; j++) {
                                char c = buffer[j];
                                if (Character.isWhitespace((int) c)) {
                                    continue;
                                }
                                if (c == ':' || c == ',' || c == ';' || c == ')' || c == '\u2014' || c == '»') {
                                    typ[nb] = CLOSE;
                                }
                                break;
                            }
                            nb++;
                        }
                    }
                    propagate(typ);
                    nb = 0;
                    for (int i = 0; i < pos.length; i++) {
                        if (typ[i] == UNKNOWN) {
                            typ[i] = (nb == 0) ? OPEN : CLOSE;
                            propagate(typ);
                        }
                        if (typ[i] == OPEN) {
                            buffer[pos[i]] = nb == 0 ? '«' : '“';
                            nb++;
                        } else if (typ[i] == CLOSE) {
                            nb--;
                            buffer[pos[i]] = nb == 0 ? '»' : '”';
                        }
                    }

                    text = new String(buffer);
                }
            }
            text = text.replaceAll("°", "\"");
            text = text.replaceAll("&deg;", "°");
        }

        // Single quotes
        if (text.contains("'")) {
            text = text.replaceAll("°", "&deg;");
            text = text.replaceAll("='([^']*)'", "=°$1°");
            text = text.replaceAll("([A-Za-z])'", "$1’");
            text = text.replaceAll("'([A-Za-z])", "‘$1");
            text = text.replaceAll("([^ ])' ", "$1’");
            text = text.replaceAll("([^ ])'$", "$1’");
            text = text.replaceAll(" '([^ ])", " ‘$1");
            text = text.replaceAll("^'([^ ])", "‘$1");
            text = text.replaceAll("([0-9])'([;,:.])", "$1’$2");
            text = text.replaceAll("°", "'");
            text = text.replaceAll("&deg;", "°");
        }

        return text;
    }

    private static void propagate(int[] typ) {
        boolean changed = true;
        while (changed) {
            changed = false;
            int nb;
            // Forward detection
            nb = 0;
            int min = 0;
            for (int i = 0; i < typ.length; i++) {
                if (typ[i] == UNKNOWN) {
                    if (nb == 0) {
                        typ[i] = OPEN;
                        changed = true;
                    } else if (min >= 1 && (i >= typ.length - 1 || typ[i + 1] != CLOSE)) {
                        typ[i] = CLOSE;
                        changed = true;
                    }
                }
                if (typ[i] == OPEN) {
                    min++;
                    nb++;
                } else if (typ[i] == CLOSE) {
                    min--;
                    nb--;
                } else {
                    min = Math.max(0, min - 1);
                }
            }
            // Backward detection
            nb = 0;
            min = 0;
            for (int i = typ.length - 1; i >= 0; i--) {
                if (typ[i] == UNKNOWN) {
                    if (nb == 0) {
                        typ[i] = CLOSE;
                        changed = true;
                    } else if (min == 1 && i > 0 && typ[i - 1] == OPEN) {
                        typ[i] = CLOSE;
                        changed = true;
                    } else if (min > 1) {
                        typ[i] = OPEN;
                        changed = true;
                    }
                }
                if (typ[i] == CLOSE) {
                    min++;
                    nb++;
                } else if (typ[i] == OPEN) {
                    min--;
                    nb--;
                } else {
                    min = Math.max(0, min - 1);
                }
            }
        }
    }

    static private int count(String text, String chars) {
        int nb = 0;
        for (char ch : text.toCharArray()) {
            if (chars.indexOf(ch) >= 0) {
                nb++;
            }
        }
        return nb;
    }

}
