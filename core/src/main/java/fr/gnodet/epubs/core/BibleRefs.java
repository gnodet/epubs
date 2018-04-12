package fr.gnodet.epubs.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BibleRefs {

    public enum Style { DocBook, Html }

    public static String fixBibleRefs(String document) {
        return fixBibleRefs(document, Style.DocBook);
    }

    public static String fixBibleRefs(String document, Style style) {
        Matcher paragraph = Pattern.compile("<p>([^<]+|<(?!p>)|<footnote><p>[\\s\\S]*?</p></footnote>)*?</p>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document, start, paragraph.start());
            newDoc.append(fixBibleRefsInParagraph(paragraph.group(), style));
            start = paragraph.end();
        }
        if (start == 0) {
            paragraph = Pattern.compile("<div[\\s\\S]*?</div>").matcher(document);
            while (paragraph.find(start)) {
                newDoc.append(document, start, paragraph.start());
                newDoc.append(fixBibleRefsInParagraph(paragraph.group(), style));
                start = paragraph.end();
            }
        }
        newDoc.append(document, start, document.length());
        return newDoc.toString();
    }

    private static String or(String... patterns) {
        return Arrays.stream(patterns)
                .collect(Collectors.joining("|", "(", ")"));
    }
    private static String repeat(String pat, String sep) {
        return "(" + pat + "(" + sep + pat + ")*)";
    }
    private static String opt(String pat) {
        return "((" + pat + ")?)";
    }
    private static String named(String name, String pat) {
        return "(?<" + name + ">" + pat + ")";
    }

    private static final String SPACE = "\\p{IsWhitespace}";
    private static final String SPACES = SPACE + "*";
    private static final String RSPACES = SPACE + "+";

    private static final String PAT_BOOK_PRE = "(\\b([1-4]" + SPACES + "|[IV]+" + RSPACES + "|S\\." + RSPACES + "))?";
    private static final String PAT_BOOK_MAIN = "\\p{IsAlphabetic}+\\.?";
    private static final String PAT_BOOK =
                    or("\\b" + PAT_BOOK_PRE + PAT_BOOK_MAIN,
                       "<i>" + PAT_BOOK_PRE + PAT_BOOK_MAIN + "</i>",
                       PAT_BOOK_PRE + "<i>" + PAT_BOOK_MAIN + "</i>")
                  + opt("\\.");

    private static final String PAT_ARABIC = "([1-9][0-9]*)";
    private static final String PAT_ROMAN = or("[IVXLCM]+", "[ivxlcm]+");
    private static final String PAT_ARABIC_OR_ROMAN = or(PAT_ARABIC, PAT_ROMAN);

    private static final String PAT_CHAP = or(PAT_ARABIC, PAT_ROMAN, "ibid\\.", "Ibid\\.");

    private static final String PAT_CHAP_PS = or(SPACES + "\\[" + PAT_ARABIC_OR_ROMAN + "\\]",
                                                 SPACES + "\\(" + PAT_ARABIC_OR_ROMAN + "\\)");

    private static final String PAT_VRS = PAT_ARABIC;
    private static final String PAT_VRS_SEP = or("[-.,]" + SPACES, RSPACES + "et" + RSPACES, RSPACES + "and" + RSPACES);
    private static final String PAT_VERS = repeat(PAT_VRS, PAT_VRS_SEP) + opt(SPACES + "(s|ss|f|ff)\\.");

    private static final String PAT_CHAP_VERS_SEP = SPACES + "[,:]" + SPACES;
    private static final String PAT_BOOK_CHAP_SEP = or(SPACES + "," + SPACES, RSPACES);
    private static final String PAT_CHAP_CHAP_SEP = SPACES + ";" + SPACES;

    private static final String PAT_CHAP_VERS = PAT_CHAP + opt(PAT_CHAP_PS) + PAT_CHAP_VERS_SEP + PAT_VERS;

    private static final String BR_FULL_BOOK = "book";
    private static final String BR_FULL_REFS = "refs";
    private static final String BR_FULL_NC_REFS = "ncrefs";
    private static final String BR_FULL =
                    named(BR_FULL_BOOK, PAT_BOOK)
                  + PAT_BOOK_CHAP_SEP
                  + or (named(BR_FULL_REFS, repeat(PAT_CHAP_VERS, PAT_CHAP_CHAP_SEP)),
                        named(BR_FULL_NC_REFS, PAT_VERS))
                  ;

    private static final Pattern BR_FULL_PATTERN = Pattern.compile(BR_FULL);

    private static final String BR_REF_CHAPTER = "chapter";
    private static final String BR_REF_CHAPTER_PS = "chapterps";
    private static final String BR_REF_VERSES = "verses";
    private static final String BR_REF =
                    named(BR_REF_CHAPTER, PAT_CHAP)
                  + opt(named(BR_REF_CHAPTER_PS, PAT_CHAP_PS))
                  + PAT_CHAP_VERS_SEP
                  + named(BR_REF_VERSES, PAT_VERS);

    private static final Pattern BR_REF_PATTERN = Pattern.compile(BR_REF);

    private static String fixBibleRefsInParagraph(String para, Style style) {
        Matcher ref = BR_FULL_PATTERN.matcher(para);
        StringBuilder newPara = new StringBuilder();
        int start = 0;
        while (ref.find(start)) {
            newPara.append(para, start, ref.start());
            String bref = createBibleRef(ref, style);
            if (bref != null) {
                newPara.append(bref);
                start = ref.end();
            } else {
                newPara.append(para, ref.start(), ref.start() + 1);
                start = ref.start() + 1;
            }
        }
        newPara.append(para, start, para.length());
        return newPara.toString();
    }

    static String lastBook;
    static String lastChapter;

    private static String createBibleRef(Matcher ref, Style style) {
        switch (style) {
            case DocBook:
                return createBibleRefDocBook(ref);
            case Html:
                return createBibleRefHtml(ref);
        }
        throw new UnsupportedOperationException();
    }

    private static String createBibleRefDocBook(Matcher ref) {
        String book = getBibleBook(ref.group(BR_FULL_BOOK));
        if (book != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<bible><ss>").append(book).append("</ss>");
            String refs = ref.group(BR_FULL_REFS);
            if (refs == null) {
                if (SINGLE_CHAP_BOOKS.contains(book)) {
                    String verses = ref.group(BR_FULL_NC_REFS);
                    sb.append("\u00a0");
                    sb.append(verses.replaceAll("(" + PAT_VRS + ")", "<sv>$1</sv>")
                            .replaceAll("\\." + SPACES, "."));
                } else {
                    return ref.group();
                }

            } else {
                String[] fullrefs = refs.split(";");
                for (int i = 0; i < fullrefs.length; i++) {
                    String fullref = fullrefs[i];
                    Matcher chvr = BR_REF_PATTERN.matcher(fullref);
                    chvr.find();
                    if (i > 0) {
                        sb.append("\u00a0; ");
                    } else {
                        sb.append("\u00a0");
                    }
                    String chapter = chvr.group(BR_REF_CHAPTER);
                    sb.append("<sc>").append(parseChapter(chapter)).append("</sc>");
                    if (chvr.group(BR_REF_CHAPTER_PS) != null) {
                        if (!"Ps".equals(book)) {
                            System.err.println("WARN: Illegal Psaume syntax found for " + ref.group());
                        }
                        sb.append(chvr.group(BR_REF_CHAPTER_PS));
                    }
                    sb.append(", ");
                    String verses = chvr.group(BR_REF_VERSES);
                    sb.append(verses.replaceAll("(" + PAT_VRS + ")", "<sv>$1</sv>")
                            .replaceAll("\\." + SPACES, ".")
                            .replaceAll("and", "et")
                            .replaceAll(">" + SPACES + "(f|s)\\.", ">\u00a0s.")
                            .replaceAll(">" + SPACES + "(ff|ss)\\.", ">\u00a0ss.")
                    );
                }
            }
            sb.append("</bible>");
            return sb.toString();
        }
        return null;
    }

    private static String createBibleRefHtml(Matcher ref) {
        String book = getBibleBook(ref.group(BR_FULL_BOOK));
        if (book != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<span class=\"bible\"><span class=\"ss\">").append(book).append("</span>");
            String refs = ref.group(BR_FULL_REFS);
            if (refs == null) {
                if (SINGLE_CHAP_BOOKS.contains(book)) {
                    String verses = ref.group(BR_FULL_NC_REFS);
                    sb.append("\u00a0");
                    sb.append(verses.replaceAll("(" + PAT_VRS + ")", "<span class=\"ssv\">$1</span>")
                            .replaceAll("\\." + SPACES, "."));
                } else {
                    return ref.group();
                }

            } else {
                String[] fullrefs = refs.split(";");
                for (int i = 0; i < fullrefs.length; i++) {
                    String fullref = fullrefs[i];
                    Matcher chvr = BR_REF_PATTERN.matcher(fullref);
                    chvr.find();
                    if (i > 0) {
                        sb.append("\u00a0; ");
                    } else {
                        sb.append("\u00a0");
                    }
                    String chapter = chvr.group(BR_REF_CHAPTER);
                    sb.append("<span class=\"ssc\">").append(decimalToRoman(parseChapter(chapter))).append("</span>");
                    if (chvr.group(BR_REF_CHAPTER_PS) != null) {
                        if (!"Ps".equals(book)) {
                            System.err.println("WARN: Illegal Psaume syntax found for " + ref.group());
                        }
                        sb.append(chvr.group(BR_REF_CHAPTER_PS));
                    }
                    sb.append(", ");
                    String verses = chvr.group(BR_REF_VERSES);
                    sb.append(verses.replaceAll("(" + PAT_VRS + ")", "<span class=\"ssv\">$1</span>")
                            .replaceAll("\\." + SPACES, ".")
                            .replaceAll("and", "et")
                            .replaceAll(">" + SPACES + "(f|s)\\.", ">\u00a0s.")
                            .replaceAll(">" + SPACES + "(ff|ss)\\.", ">\u00a0ss.")
                    );
                }
            }
            sb.append("</span>");
            return sb.toString();
        }
        return null;
    }

    private static String getBibleBook(String s) {
        s = s.replaceAll("\\.|<i>|</i>", "");
        s = s.replaceAll("III" + SPACE, "3");
        s = s.replaceAll("II" + SPACE, "2");
        s = s.replaceAll("I" + SPACE, "1");
        s = s.replaceAll("3" + SPACE, "3");
        s = s.replaceAll("2" + SPACE, "2");
        s = s.replaceAll("1" + SPACE, "1");
        s = s.replaceAll("S" + SPACE, "");
        String book = BOOKS.get(s);
        if (book == null && s.equalsIgnoreCase("id")) {
            book = lastBook;
        } else if (book != null) {
            lastBook = book;
        } else {
            System.err.println("WARN: unrecognized bible book: " + s);
        }
        return book;
    }

    static final Map<String, String> BOOKS;
    static final Set<String> SINGLE_CHAP_BOOKS;
    static {
        SINGLE_CHAP_BOOKS = new HashSet<>(Arrays.asList("1Jn", "2Jn", "3Jn", "Jd"));
        BOOKS = new HashMap<>();
        BOOKS.put("Actes", "Ac");
        BOOKS.put("Marc", "Mc");
        BOOKS.put("Jean", "Jn");
        BOOKS.put("1Pierre", "1P");
        BOOKS.put("2Pierre", "2P");
        BOOKS.put("1Jean", "1Jn");
        BOOKS.put("2Jean", "2Jn");
        BOOKS.put("3Jean", "3Jn");
        BOOKS.put("Luc", "Lc");
        BOOKS.put("Lucam", "Lc");
        BOOKS.put("Joannem", "Lc");
        BOOKS.put("Proverbes", "Pv");
        BOOKS.put("Sagesse", "Sg");
        BOOKS.put("Psaume", "Ps");
        BOOKS.put("Jacques", "Jc");
        BOOKS.put("Deutéronome", "Dt");
        BOOKS.put("Exode", "Ex");
        BOOKS.put("Matthieu", "Mt");
        BOOKS.put("Genèse", "Gn");
        BOOKS.put("Ecclésiaste", "Ec");
        BOOKS.put("Romain", "Rm");
        BOOKS.put("Acts", "Ac");
        BOOKS.put("Roi", "R");
        BOOKS.put("Kings", "R");
        BOOKS.put("John", "Jn");
        BOOKS.put("Psalm", "Ps");
        BOOKS.put("Isaïe", "Is");
        BOOKS.put("Ezéch", "Ez");
        BOOKS.put("Ezéchiel", "Ez");
        BOOKS.put("Habac", "Ha");

        BOOKS.put("ISAÏE", "Is");
        BOOKS.put("MARC", "Mc");
        BOOKS.put("MATTH", "Mt");
        BOOKS.put("LUC", "Lc");
        BOOKS.put("JEAN", "Jn");
        BOOKS.put("1JEAN", "1Jn");
        BOOKS.put("2JEAN", "2Jn");
        BOOKS.put("3JEAN", "3Jn");
        BOOKS.put("1PIERRE", "1P");
        BOOKS.put("2PIERRE", "2P");
        BOOKS.put("MALACHIE", "Ml");

        BOOKS.put("Jér", "Jr");
        BOOKS.put("Ezk", "Ez");
        BOOKS.put("Coloss", "Col");
        BOOKS.put("Heb", "Hb");
        BOOKS.put("Hébr", "Hb");
        BOOKS.put("Phil", "Ph");
        BOOKS.put("Agg", "Ag");
        BOOKS.put("Ephes", "Ep");
        BOOKS.put("Eccle", "Ec");
        BOOKS.put("Exod", "Ex");
        BOOKS.put("Tit", "Tt");
        BOOKS.put("Eph", "Ep");
        BOOKS.put("Matth", "Mt");
        BOOKS.put("Io", "Jn");
        BOOKS.put("Ioan", "Jn");
        BOOKS.put("Ioann", "Jn");
        BOOKS.put("Joan", "Jn");
        BOOKS.put("Rom", "Rm");
        BOOKS.put("1Ioan", "1Jn");
        BOOKS.put("2Ioan", "2Jn");
        BOOKS.put("3Ioan", "3Jn");
        BOOKS.put("1Tim", "1Tm");
        BOOKS.put("2Tim", "2Tm");
        BOOKS.put("Hebr", "Hb");
        BOOKS.put("1Cor", "1Co");
        BOOKS.put("2Cor", "2Co");
        BOOKS.put("1Petr", "1P");
        BOOKS.put("2Petr", "2P");
        BOOKS.put("Job", "Jb");
        BOOKS.put("Prov", "Pv");
        BOOKS.put("Isai", "Is");
        BOOKS.put("Act", "Ac");
        BOOKS.put("Ierem", "Jr");
        BOOKS.put("Apoc", "Ap");
        BOOKS.put("Gen", "Gn");
        BOOKS.put("Gal", "Ga");
        BOOKS.put("1Thess", "1Th");
        BOOKS.put("2Thess", "2Th");
        BOOKS.put("Apoc", "Ap");
        BOOKS.put("1Sam", "1S");
        BOOKS.put("2Sam", "2S");
        BOOKS.put("1Macc", "1M");
        BOOKS.put("2Macc", "2M");
        BOOKS.put("Dan", "Dn");
        BOOKS.put("Lam", "Lm");
        BOOKS.put("Mal", "Ml");
        BOOKS.put("Zac", "Za");
        BOOKS.put("Tob", "Tb");
        BOOKS.put("Sir", "Si");
        BOOKS.put("Iob", "Jb");
        BOOKS.put("Ier", "Jr");
        BOOKS.put("Jac", "Jc");
        BOOKS.put("Kgs", "R");
        BOOKS.put("Sag", "Sg");
        BOOKS.put("Wis", "Sg");
        BOOKS.put("Jer", "Jr");
        BOOKS.put("Jos", "Js");

        BOOKS.put("Rt", "Rt");
        BOOKS.put("Ba", "Ba");
        BOOKS.put("Esd", "Esd");
        BOOKS.put("Est", "Est");
        BOOKS.put("Js", "Js");
        BOOKS.put("Jb", "Jb");
        BOOKS.put("Ne", "Ne");
        BOOKS.put("Nm", "Nb");
        BOOKS.put("Nb", "Nb");
        BOOKS.put("Phm", "Phm");
        BOOKS.put("Mk", "Mc");
        BOOKS.put("Lk", "Lc");
        BOOKS.put("Ez", "Ez");
        BOOKS.put("Am", "Am");
        BOOKS.put("Ga", "Ga");
        BOOKS.put("Col", "Col");
        BOOKS.put("Is", "Is");
        BOOKS.put("Mt", "Mt");
        BOOKS.put("Za", "Za");
        BOOKS.put("Lm", "Lm");
        BOOKS.put("La", "Lm");
        BOOKS.put("Gn", "Gn");
        BOOKS.put("Hb", "Hb");
        BOOKS.put("Da", "Dn");
        BOOKS.put("Dn", "Dn");
        BOOKS.put("1Co", "1Co");
        BOOKS.put("2Co", "2Co");
        BOOKS.put("Jn", "Jn");
        BOOKS.put("1Jn", "1Jn");
        BOOKS.put("2Jn", "2Jn");
        BOOKS.put("3Jn", "3Jn");
        BOOKS.put("Ex", "Ex");
        BOOKS.put("Jc", "Jc");
        BOOKS.put("Jr", "Jr");
        BOOKS.put("Ap", "Ap");
        BOOKS.put("He", "Hb");
        BOOKS.put("Jg", "Jg");
        BOOKS.put("Lc", "Lc");
        BOOKS.put("Ps", "Ps");
        BOOKS.put("Ml", "Ml");
        BOOKS.put("Mc", "Mc");
        BOOKS.put("Ac", "Ac");
        BOOKS.put("Lv", "Lv");
        BOOKS.put("Dt", "Dt");
        BOOKS.put("Tb", "Tb");
        BOOKS.put("Ph", "Ph");
        BOOKS.put("Tt", "Tt");
        BOOKS.put("Os", "Os");
        BOOKS.put("Jl", "Jl");
        BOOKS.put("Ct", "Ct");
        BOOKS.put("Pv", "Pv");
        BOOKS.put("Si", "Si");
        BOOKS.put("Rm", "Rm");
        BOOKS.put("Ep", "Ep");
        BOOKS.put("1Tm", "1Tm");
        BOOKS.put("2Tm", "2Tm");
        BOOKS.put("1Th", "1Th");
        BOOKS.put("2Th", "2Th");
        BOOKS.put("1P", "1P");
        BOOKS.put("2P", "2P");
        BOOKS.put("1M", "1M");
        BOOKS.put("2M", "2M");
        BOOKS.put("1S", "1S");
        BOOKS.put("2S", "2S");
        BOOKS.put("2Sm", "2S");
        BOOKS.put("1R", "1R");
        BOOKS.put("2R", "2R");
        BOOKS.put("Sg", "Sg");
        BOOKS.put("Mi", "Mi");
        BOOKS.put("Ag", "Ag");
        BOOKS.put("Ec", "Ec");
        BOOKS.put("1Ch", "1Ch");
        BOOKS.put("2Ch", "2Ch");
        BOOKS.put("Jon", "Jon");

        BOOKS.put("3R", "3R");
        BOOKS.put("4R", "4R");
        BOOKS.put("Pr", "Pr");
        BOOKS.put("Na", "Na");
        BOOKS.put("Qo", "Qo");
        BOOKS.put("Zc", "Zc");
    }

    public static int parseChapter(String str) {
        if (str.equalsIgnoreCase("ibid.")) {
            str = lastChapter;
        } else {
            lastChapter = str;
        }
        if (str.matches("[ivxclIVXCL]+")) {
            return romanToDecimal(str);
        } else if (str.matches("[0-9]+")) {
            return Integer.parseInt(str);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static int romanToDecimal(String romanNumber) {
        int decimal = 0;
        int lastNumber = 0;
        for (int i = romanNumber.length() - 1; i >= 0; --i) {
            char convertToDecimal = romanNumber.charAt(i);
            int base;
            switch (Character.toUpperCase(convertToDecimal)) {
                case 'M': base = 1000; break;
                case 'D': base = 500; break;
                case 'C': base = 100; break;
                case 'L': base = 50; break;
                case 'X': base = 10; break;
                case 'V': base = 5; break;
                case 'I': base = 1; break;
                default: throw new NumberFormatException();
            }
            decimal = lastNumber > base ? decimal - base : decimal + base;
            lastNumber = base;
        }
        return decimal;
    }

    private static String[] romanThousands = new String[]{"", "m", "mm", "mmm", "mmmm", "mmmmm", "mmmmmm", "mmmmmmm", "mmmmmmmm", "mmmmmmmmm"};
    private static String[] romanHundreds = new String[]{"", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm"};
    private static String[] romanTens = new String[]{"", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc"};
    private static String[] romanUnits = new String[]{"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"};

    public static String decimalToRoman(long n) {
        return n > 0L && n <= 9999L ? romanThousands[(int)n / 1000] + romanHundreds[(int)n / 100 % 10] + romanTens[(int)n / 10 % 10] + romanUnits[(int)n % 10] : "" + n;
    }

}
