package org.gnodet.epubs.fixer;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import dk.dren.hunspell.Hunspell;
import dk.dren.hunspell.Hunspell.Dictionary;
import org.mozilla.universalchardet.UniversalDetector;

import static fr.gnodet.epubs.core.Quotes.fixQuotes;
import static fr.gnodet.epubs.core.Tidy.tidyHtml;
import static fr.gnodet.epubs.core.Tidy.translateEntities;
import static fr.gnodet.epubs.core.Whitespaces.fixWhitespaces;
import static fr.gnodet.epubs.core.BibleRefs.fixBibleRefs;

public class Main {

    final Options options;
    final Detector detector;
    final Map<String, Dictionary> dictionaries;

    static {
        try {
            initLanguages();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Main(String[] args) throws Exception {
        this.options = Options.compile(usage()).parse(args);
        detector = DetectorFactory.create();
        detector.setVerbose();
        dictionaries = new HashMap<>();
    }

    public void run() throws Exception {
        if (options.isSet("help")) {
            options.usage(System.err);
            return;
        }
        for (String file : options.args()) {
            try {
                String doc = process(file);
                System.out.println(doc);
            } catch (Exception e) {
                System.err.println("Error processing " + file + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void initLanguages() throws IOException, LangDetectException {
        List<String> profiles = new ArrayList<>();
        for (String lang : Arrays.asList("fr", /*"la",*/ "el", "he")) {
            try (InputStream is = Main.class.getResourceAsStream("/profiles/" + lang)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copy(is, baos);
                byte[] data = baos.toByteArray();
                String detected = detectCharset(data);
                profiles.add(new String(data, detected != null ? detected : "UTF-8"));
            }
        }
        DetectorFactory.loadProfile(profiles);
    }

    private static long copy(InputStream source, OutputStream sink) throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[8192];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    String[] usage() {
        return new String[] {
                "fixer -  fix document typography",
                "Usage: fixer [OPTIONS] [FILES]",
                "  -? --help                    Show help",
                "  -i --input-charset=IC        Input charset",
                "  -o --output-charset=OC       Output charset"
        };
    }

    void loadDictionnaries() throws IOException {
        dictionaries.put("fr", loadDictionary("fr/dictionaries/fr-classique"));
        dictionaries.put("el", loadDictionary("el/dictionaries/grc_GR"));
        dictionaries.put("la", loadDictionary("la/la/la"));
        dictionaries.put("he", loadDictionary("he/he_IL"));
    }

    Hunspell.Dictionary loadFrenchDictionary() throws IOException {
        Path tmpDir = Files.createTempDirectory("dic.fr.");
        extract(tmpDir, "fr/dictionaries/", "fr-classique.aff");
        extract(tmpDir, "fr/dictionaries/", "fr-classique.dic");
        return Hunspell.getInstance().getDictionary(tmpDir+"/fr-classique");
    }

    Hunspell.Dictionary loadDictionary(String name) throws IOException {
        Path tmpDir = Files.createTempDirectory("dic-");
        extract(tmpDir, name + ".aff");
        extract(tmpDir, name + ".dic");
        Path out = Paths.get(name).getFileName();
        String dir = tmpDir.resolve(out).toString();
        return Hunspell.getInstance().getDictionary(dir);
    }

    public static Path extract(Path tmpDir, String resource) throws IOException {
        Path res = Paths.get(resource);
        Path output = tmpDir.resolve(res.getFileName());
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(res.toString())) {
            Files.copy(is, output);
        }
        return output;
    }

    public static Path extract(Path tmpDir, String s, String f) throws IOException {
        Path output = tmpDir.resolve(f);

        URI c = URI.create(s).resolve(f);
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(c.toString())) {
            Files.copy(is, output);
        }
        return output;
    }

    public String process(String file) throws Exception {

//        String doc = tika(new File(file).toURI().toURL());
//        if (doc != null) {
//            return doc;
//        }

        byte[] data = Files.readAllBytes(Paths.get(file));
        String detected = detectCharset(data);
        String inputCharset = options.isSet("input-charset") ? options.get("input-charset") : null;
        if (inputCharset != null && detected != null && !inputCharset.equals(detected)) {
            System.err.println("WARN: detected charset (" + detected + ") " +
                    "is different the specified input charset (" + inputCharset + ")");
        }
        String charset = inputCharset != null ? inputCharset :
                        detected != null ? detected : Charset.defaultCharset().name();
        String document = new String(data, charset);

        String prev;
        do {
            prev = document;
            document = document.replaceAll("<para", "<p")
                    .replaceAll("</para>", "</p>");
            document = document.replaceAll("</?bible>|</?ss>|</?sc>|</?sv>", "")
                    .replaceAll("<pa>", "(").replaceAll("</pa>", ")");
            document = tidyHtml(document);
            document = translateEntities(document);

            document = fixQuotes(document);
            document = fixBibleRefs(document);
            document = fixTypos(document);
            document = fixFootNotes(document);
            document = fixWhitespaces(document);

            break;
        } while (!document.equals(prev));

        return document;
    }

    private static String detectCharset(byte[] data) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(data, 0, data.length);
        detector.dataEnd();

        return detector.getDetectedCharset();
    }

    public static String tidyHtml(String document) throws IOException {
        Writer writer;
        writer = new StringWriter();
        org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
//        tidy.setXHTML(true);
//        tidy.setMakeBare(true);
//        tidy.setXmlOut(true);
//        tidy.setOutputEncoding("UTF-8");
        tidy.getConfiguration().printConfigOptions(new OutputStreamWriter(System.err), true);
        tidy.parse(new StringReader(document), writer);
        writer.close();
        if (!writer.toString().isEmpty()) {
            document = writer.toString();
        }
        return document;
    }

    /*
    private String tika(URL url) throws Exception {

        ContentHandler handler = new SafeContentHandler(new ToXMLContentHandler()) {

            Stack<String> element = new Stack<>();
            StringBuilder buffer = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                if (localName.equals("p") || localName.equals("para")) {
                    flush();
                    element.push(localName);
                    super.startElement(uri, localName, name, atts);
                }
                else if (element.size() > 0) {
                    buffer.append("<").append(localName);
                    for (int i = 0; i < atts.getLength(); i++) {
                        String key = atts.getLocalName(i);
                        String val = atts.getValue(i);
                        buffer.append(" ").append(key).append("=\"").append(val).append("\"");
                    }
                    buffer.append(">");
                } else {
                    super.startElement(uri, localName, name, atts);
                }
            }

            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {
                if (localName.equals("p") || localName.equals("para")) {
                    flush();
                    super.endElement(uri, localName, name);
                    element.pop();
                } else if (element.size() > 0){
                    buffer.append("</").append(localName).append(">");
                } else {
                    super.endElement(uri, localName, name);
                }
            }

            protected void flush() throws SAXException {
                if (buffer.length() > 0) {
                    String org = buffer.toString();
                    if (!org.matches("\\p{IsWhiteSpace}+")) {
                        String mod = fixQuotes("<p>" + org + "</p>");
                        mod = fixWhitespaces(mod);
                        mod = mod.replace("<p>", "").replace("</p>", "");
                        org = mod;
                    }
                    char[] ch = org.toCharArray();
                    super.characters(ch, 0, ch.length);
                    buffer.setLength(0);
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (element.size() > 0) {
                    buffer.append(ch, start, length);
                } else {
                    super.characters(ch, start, length);
                }
            }
        };

        TikaConfig config = TikaConfig.getDefaultConfig();
        Parser parser = new AutoDetectParser(config);
        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        context.set(HtmlMapper.class, IdentityHtmlMapper.INSTANCE);
        Metadata metadata = new Metadata();
        try (TikaInputStream stream = TikaInputStream.get(url, metadata)) {
            parser.parse(stream, handler, metadata, context);
            String parsed = handler.toString();
            System.err.println("TIKA");
            System.err.println("====");
            System.err.println(parsed);

            return parsed;
        }
    }
    */

    /*
    public static String fixBibleRefs(String document) {
        Matcher paragraph = Pattern.compile("<(p|h[1-9])[\\s\\S]*?</\\1>").matcher(document);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (paragraph.find(start)) {
            newDoc.append(document.substring(start, paragraph.start()));
            newDoc.append(fixBibleRefsInParagraph(paragraph.group()));
            start = paragraph.end();
        }
        if (start == 0) {
            paragraph = Pattern.compile("<div[\\s\\S]*?</div>").matcher(document);
            while (paragraph.find(start)) {
                newDoc.append(document.substring(start, paragraph.start()));
                newDoc.append(fixBibleRefsInParagraph(paragraph.group()));
                start = paragraph.end();
            }
        }
        newDoc.append(document.substring(start, document.length()));
        return newDoc.toString();
    }

    private static String fixBibleRefsInParagraph(String para) {
        String PAT_BOOK = "(?:[12]\\s*)?(?:[IV]+ )?[A-Z][a-z]+";
        String PAT_CHAP = "(?:[0-9]+|[IXVLCixvlc]+)";
        String PAT_VERS = "[0-9]+(?:[-.][0-9]+)*";
        Matcher ref = Pattern.compile(
                "(?:<i>)?" +
                "(?<book>" + PAT_BOOK + ")" +
                "(?:\\.)?" +
                "(?:</i>\\.?)? " +
                "(?<refs>" + PAT_CHAP + "[,:]\\p{IsWhitespace}*" + PAT_VERS +
                        "(\\p{IsWhitespace}*;\\p{IsWhitespace}*" + PAT_CHAP + "[,:]\\p{IsWhitespace}*" + PAT_VERS + ")*)"
            ).matcher(para);
        StringBuilder newPara = new StringBuilder();
        int start = 0;
        while (ref.find(start)) {
            newPara.append(para.substring(start, ref.start()));
            newPara.append(createBibleRef(ref));
            start = ref.end();
        }
        newPara.append(para.substring(start, para.length()));
        return newPara.toString();
    }

    private static String createBibleRef(Matcher ref) {
        String book = getBibleBook(ref.group("book"));
        if (book != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<bible><ss>").append(book).append("</ss>");
            String refs = ref.group("refs");
            String[] fullrefs = refs.split(";");
            for (int i = 0; i < fullrefs.length; i++) {
                String fullref = fullrefs[i];
                Matcher chvr = Pattern.compile(
                        "(?<chapter>[0-9]+|[IXVLCixvlc]+)[,:]\\s*" +
                                "(?<verses>[0-9]+(?:[-.][0-9]+)*)").matcher(fullref);
                chvr.find();
                if (i > 0) {
                    sb.append("\u00a0; ");
                }
                sb.append(" <sc>").append(parseChapter(chvr.group("chapter"))).append("</sc>, ");
                sb.append(chvr.group("verses").replaceAll("([0-9]+)", "<sv>$1</sv>"));
            }
            sb.append("</bible>");
            return sb.toString();
        }
        return ref.group();
    }

    private static String getBibleBook(String s) {
        s = s.replace("I ", "1");
        s = s.replace("II ", "2");
        s = s.replace("1 ", "1");
        s = s.replace("2 ", "2");
        return BOOKS.get(s);
    }

    static final Map<String, String> BOOKS;
    static {
        BOOKS = new HashMap<>();
        BOOKS.put("Mt", "Mt");
        BOOKS.put("Gn", "Gn");
        BOOKS.put("Hb", "Hb");
        BOOKS.put("Da", "Da");
        BOOKS.put("1Co", "1Co");
        BOOKS.put("2Co", "2Co");
        BOOKS.put("Jn", "Jn");
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
        BOOKS.put("Ct", "Ct");
        BOOKS.put("Pv", "Pv");
        BOOKS.put("Si", "Si");
        BOOKS.put("Rm", "Rm");
        BOOKS.put("Ep", "Ep");
        BOOKS.put("1Tm", "1Tm");
        BOOKS.put("2Tm", "2Tm");
        BOOKS.put("1Th", "1Th");
    }

    public static int parseChapter(String str) {
        try {
            return romanToDecimal(str);
        } catch (NumberFormatException e) {
            return Integer.parseInt(str);
        }
    }

    public static int romanToDecimal(String romanNumber) {
        int decimal = 0;
        int lastNumber = 0;
        for (char convertToDecimal : romanNumber.toCharArray()) {
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
    */

    private static final String BIBLE_REF = "<bible>([\\p{IsAlphabetic}0-9;,–\\-. \u00a0]|</?ss>|</?sc>|</?sv>|</?pa>)+</bible>";

    public String fixFootNotes(String document) {
        document = document.replaceAll("[:;.,]</i>", "</i>.");

        document = document.replaceAll("(\\S)\\(", "$1 (");
        document = document.replaceAll("([.,])\\s*(\\((Saint (Paul|Matthieu|Luc|Jacques|Marc|Jean), )?" + BIBLE_REF + "\\))", "$2$1");
        document = document.replaceAll("(\u00a0[:;»!?])\\s*(\\((Saint (Paul|Matthieu|Luc|Jacques|Marc|Jean), )?" + BIBLE_REF + "\\))", " $2$1");

        document = document.replaceAll("([.,])\\s*(<footnote>.*?</footnote>)", "$2$1");
        document = document.replaceAll("(\u00a0[:;»!?])\\s*(<footnote>.*?</footnote>)", "$2$1");
        document = document.replaceAll("<footnote><p>(" + "(Saint (Paul|Matthieu|Luc|Jacques|Marc|Jean), )?" + BIBLE_REF + "(\\p{IsWhitespace}*;\\p{IsWhitespace}*" + BIBLE_REF + ")*)\\.?</p></footnote>", " ($1)");
        document = document.replaceAll("<footnote><p>Cf\\.?[\u00a0 ](" + "(Saint (Paul|Matthieu|Luc|Jacques|Marc|Jean), )?" + BIBLE_REF + "(\\p{IsWhitespace}*;\\p{IsWhitespace}*" + BIBLE_REF + ")*)\\.?</p></footnote>",
                                       " (cf.\u00a0$1)");
        return document;
    }

    public String fixTypos(String document) throws IOException {
        for (Map.Entry<Pattern, String> typo : TYPOS.entrySet()) {
            document = typo.getKey().matcher(document).replaceAll(typo.getValue());
        }
        return document;
//        Matcher paragraph = Pattern.compile(">[^<]*<").matcher(document);
//        StringBuilder newDoc = new StringBuilder();
//        int start = 0;
//        while (paragraph.find(start)) {
//            newDoc.append(document.substring(start, paragraph.start()));
//            newDoc.append(">");
//            String para = paragraph.group();
//            para = para.substring(1, para.length() - 1);
//            newDoc.append(fixTyposInParagraph(para));
//            newDoc.append("<");
//            start = paragraph.end();
//        }
//        newDoc.append(document.substring(start, document.length()));
//        document = newDoc.toString();
//        return document;
    }

    /*
    class WordFixer {

        private String lang = "fr";

        public String checkWord(String w) {
            Map<String, List<String>> suggs = null;
            List<String> reps = null;
            if (lang != null) {
                List<String> sugg = dictionaries.get(lang).suggest(w);
                if (sugg.size() > 0) {
                    reps = getBestSuggestion(w, lang, sugg);
                }
            }
//            if (reps == null || reps.isEmpty()) {
//                suggs = new HashMap<>();
//                for (Map.Entry<String, Dictionary> entry : dictionaries.entrySet()) {
//                    if (!lang.equals(entry.getKey())) {
//                        List<String> sugg = entry.getValue().suggest(w);
//                        if (!sugg.isEmpty()) {
//                            suggs.put(entry.getKey(), sugg);
//                        }
//                    }
//                }
//                if (suggs.size() > 1) {
//                    reps = getBestSuggestion(w, suggs);
//                } else if (suggs.size() == 1) {
//                    Map.Entry<String, List<String>> entry = suggs.entrySet().iterator().next();
//                    lang = entry.getKey();
//                    System.err.println("Changing language to " + lang);
//                    reps = getBestSuggestion(w, lang, entry.getValue());
//                }
//            }
            if (reps != null && !reps.isEmpty()) {
                if (reps.size() == 1) {
                    String rep = reps.get(0);
                    if (!rep.equals(w)) {
                        System.err.println("INFO: using " + rep + " for " + w);
                        return rep;
                    }
                } else if (reps.size() > 1) {
                    System.err.println("Ambiguous: " + w + " -> " + reps);
                }
            } else {
                System.err.println("Ambiguous language: " + suggs);
            }
            return w;
        }

        private List<String> getBestSuggestion(String w, Map<String, List<String>> suggs) {
            for (int colidx = MAX_COLLATORS-1; colidx >= 0; colidx--) {
                for (String lang : new ArrayList<>(suggs.keySet())) {
                    Collator coll = getCollators(lang)[colidx];
                    CollationKey kw = coll.getCollationKey(w);
                    List<String> sugg = suggs.get(lang);
                    sugg.removeIf(s -> kw.compareTo(coll.getCollationKey(s)) != 0);
                    if (sugg.isEmpty()) {
                        suggs.remove(lang);
                    }
                }
                if (suggs.size() == 1) {
                    lang = suggs.keySet().iterator().next();
                    System.err.println("Changing language to " + lang);
                    return getBestSuggestion(w, lang, suggs.get(lang));
                }
            }
            return null;
        }

        private List<String> getBestSuggestion(String w, String lang, List<String> cursugg) {
            Collator[] collators = getCollators(lang);
            for (int colidx = collators.length - 1; colidx >= 0; colidx--) {
                Collator coll = collators[colidx];
                List<String> reps = new ArrayList<>();
                CollationKey kw = coll.getCollationKey(w);
                for (String s : cursugg) {
                    if (kw.compareTo(coll.getCollationKey(s)) == 0) {
                        reps.add(s);
                    }
                }
                if (reps.size() == 1) {
                    return reps;
                } else if (reps.size() == 0) {
                    return cursugg;
                } else {
                    cursugg = reps;
                }
            }
            return cursugg;
        }
    }
    */

    static final Map<Pattern, String> TYPOS;
    static {
        TYPOS = new LinkedHashMap<>();
        TYPOS.put(Pattern.compile("\\bEzéchias\\b"), "Ézéchias");
        TYPOS.put(Pattern.compile("\\bEzéchiel\\b"), "Ézéchiel");
        TYPOS.put(Pattern.compile("\\bEsaü\\b"), "Esaü");
        TYPOS.put(Pattern.compile("\\bEphraïm\\b"), "Éphraïm");
        TYPOS.put(Pattern.compile("\\bEve\\b"), "Ève");
        TYPOS.put(Pattern.compile("\\bEden\\b"), "Éden");
        TYPOS.put(Pattern.compile("Ecout"), "Écout");
        TYPOS.put(Pattern.compile("Epiphanie"), "Épiphanie");
        TYPOS.put(Pattern.compile("CHRETIEN"), "CHRÉTIEN");
        TYPOS.put(Pattern.compile("EGLISE"), "ÉGLISE");
        TYPOS.put(Pattern.compile("Egyptien"), "Égyptien");
        TYPOS.put(Pattern.compile("Eglise"), "Église");
        TYPOS.put(Pattern.compile("Ecriture"), "Écriture");
        TYPOS.put(Pattern.compile("Egypte"), "Égypte");
        TYPOS.put(Pattern.compile("Etant"), "Étant");
        TYPOS.put(Pattern.compile("Etat"), "État");
        TYPOS.put(Pattern.compile("Epoux"), "Époux");
        TYPOS.put(Pattern.compile("Epouse"), "Épouse");
        TYPOS.put(Pattern.compile("Eternel"), "Éternel");
        TYPOS.put(Pattern.compile("Etendant"), "Étendant");
        TYPOS.put(Pattern.compile("Evêque"), "Évêque");
        TYPOS.put(Pattern.compile("Elisabeth"), "Élisabeth");
        TYPOS.put(Pattern.compile("EVANGILE"), "ÉVANGILE");
        TYPOS.put(Pattern.compile("Evangile"), "Évangile");
        TYPOS.put(Pattern.compile("EVEQUE"), "ÉVÊQUE");
        TYPOS.put(Pattern.compile("PRETRES"), "PRÊTRES");
        TYPOS.put(Pattern.compile("ONZIEME"), "ONZIÈME");
        TYPOS.put(Pattern.compile("EVANGELISATION"), "ÉVANGÉLISATION");
        TYPOS.put(Pattern.compile("METHODE"), "MÉTHODE");
        TYPOS.put(Pattern.compile("PORTEE"), "PORTÉE");
        TYPOS.put(Pattern.compile("MILLENAIRE"), "MILLÉNAIRE");
        TYPOS.put(Pattern.compile("HERAUTS"), "HÉRAUTS");
        TYPOS.put(Pattern.compile("MERE"), "MÈRE");
        TYPOS.put(Pattern.compile("PERE"), "PÈRE");
        TYPOS.put(Pattern.compile("MEDIATION"), "MÉDIATION");
        TYPOS.put(Pattern.compile("MISERICORDE"), "MISÉRICORDE");
        TYPOS.put(Pattern.compile("HERITAGE"), "HÉRITAGE");
        TYPOS.put(Pattern.compile("MYSTERE"), "MYSTÈRE");
        TYPOS.put(Pattern.compile("VERITE"), "VÉRITÉ");
        TYPOS.put(Pattern.compile("IMPLANTERENT"), "IMPLANTÈRENT");
        TYPOS.put(Pattern.compile("Etre"), "Être");
        TYPOS.put(Pattern.compile("educateurs"), "éducateurs");
        TYPOS.put(Pattern.compile("GENERATION"), "GÉNÉRATION");
        TYPOS.put(Pattern.compile("EVANGELI"), "ÉVANGÉLI");
        TYPOS.put(Pattern.compile("Evangéli"), "Évangéli");
        TYPOS.put(Pattern.compile("OE"), "Œ");
        TYPOS.put(Pattern.compile("AE"), "Æ");
        TYPOS.put(Pattern.compile("oe"), "œ");
        TYPOS.put(Pattern.compile("ae"), "æ");
        TYPOS.put(Pattern.compile("\\bA "), "À ");
        TYPOS.put(Pattern.compile("([\\.«]) A "), "$1 À ");
        TYPOS.put(Pattern.compile("\\bO "), "Ô ");
        TYPOS.put(Pattern.compile("([\\.«]) O "), "$1 Ô ");
        TYPOS.put(Pattern.compile("\\.\\.\\."), "…");
        TYPOS.put(Pattern.compile("Cf\\. "), "Cf.\u00a0");
        TYPOS.put(Pattern.compile("cf\\. "), "cf.\u00a0");
    }

    private String fixTyposInParagraph(String paragraph) {
        Matcher word = Pattern.compile("[\\p{IsAlphabetic}’-]+").matcher(paragraph);
        StringBuilder newDoc = new StringBuilder();
        int start = 0;
        while (word.find(start)) {
            String prev = paragraph.substring(start, word.start());
            newDoc.append(prev);
            String w = word.group();
            if (!prev.endsWith("<ss>")) {
                w = fixTyposInWord(w, dictionaries.get("fr"));
            }
            newDoc.append(w);
            start = word.end();
        }
        newDoc.append(paragraph.substring(start, paragraph.length()));
        paragraph = newDoc.toString();
        String paragraph2 = paragraph.replaceAll("(^|\\.\\s+)A(\\s)", "$1À$2");
        paragraph = paragraph2;
        return paragraph;
    }

    private static final int MAX_COLLATORS = 3;

    private Map<String, Collator[]> collators = new HashMap<>();

    private Collator[] getCollators(String key) {
        return collators.computeIfAbsent(key, this::createCollators);
    }

    private Collator[] createCollators(String key) {
        Collator[] collators = new Collator[MAX_COLLATORS];
        for (int i = 0; i < MAX_COLLATORS; i++) {
            Collator collator = Collator.getInstance(new Locale(key));
            collator.setStrength(i + 4 - MAX_COLLATORS);
            collators[MAX_COLLATORS-1-i] = collator;
        }
        return collators;
    }

    private String fixLigatures(String w) {
        return w.replaceAll("ae", "æ")
                .replaceAll("Ae", "Æ")
                .replaceAll("AE", "Æ")
                .replaceAll("oe", "œ")
                .replaceAll("Oe", "Œ")
                .replaceAll("OE", "Œ")
                ;
    }

    private String fixTyposInWord(String word, Hunspell.Dictionary dictionary) {
        if (dictionary.misspelled(word)) {
            List<String> strings = dictionary.suggest(word);
            for (String sug : strings) {
                String usug = Normalizer.normalize(sug, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                usug = usug.replaceAll("Æ", "AE").replaceAll("æ", "ae")
                        .replaceAll("Œ", "OE").replaceAll("œ", "oe");
                if (usug.equals(word)) {
                    System.err.println("INFO: fixing mispelled word '" + word + "' to '" + sug + "'");
                    return sug;
                }
            }
//            System.err.println("WARN: misspelled word: '" + word
//                    + "' (suggestions: " + String.join(", ", strings) + ")");
            return word;
        } else {
            return word;
        }
    }

    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }

}
