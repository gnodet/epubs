package fr.gnodet.epubs.core;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Tidy {

    public static String tidyHtml(String document) throws IOException {
        Writer writer;
        writer = new StringWriter();
        org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
        tidy.setXHTML(true);
//        tidy.getConfiguration().printConfigOptions(new OutputStreamWriter(System.err), true);
        tidy.parse(new StringReader(document), writer);
        writer.close();
        document = writer.toString();
        return document;
    }

    public static String translateEntities(String document) {
        for (Map.Entry<Pattern, String> typo : ENTITIES.entrySet()) {
            document = typo.getKey().matcher(document).replaceAll(typo.getValue());
        }
        return document;
    }

    static final Map<Pattern, String> ENTITIES;

    static {
        // Init map
        ENTITIES = new LinkedHashMap<Pattern, String>();
        //  C0 Controls and Basic Latin
        ENTITIES.put(Pattern.compile("&#34;"), "&quot;");
        ENTITIES.put(Pattern.compile("&#38;"), "&amp;");
        ENTITIES.put(Pattern.compile("&#60;"), "&lt;");
        ENTITIES.put(Pattern.compile("&#62;"), "&gt;");
        ENTITIES.put(Pattern.compile("&#133;"), "&hellip;");
        ENTITIES.put(Pattern.compile("&#134;"), "&dagger;");
        ENTITIES.put(Pattern.compile("&#140;"), "&OElig;");
        ENTITIES.put(Pattern.compile("&#145;"), "&lsquo;");
        ENTITIES.put(Pattern.compile("&#146;"), "&rsquo;");
        ENTITIES.put(Pattern.compile("&#147;"), "&ldquo;");
        ENTITIES.put(Pattern.compile("&#148;"), "&rdquo;");
        ENTITIES.put(Pattern.compile("&#150;"), "&ndash;");
        ENTITIES.put(Pattern.compile("&#151;"), "&mdash;");
        ENTITIES.put(Pattern.compile("&#156;"), "&oelig;");
        // Standard
        ENTITIES.put(Pattern.compile("&#160;"), "&nbsp;"); //  no-break space
        ENTITIES.put(Pattern.compile("&#161;"), "&iexcl;"); //  inverted exclamation mark
        ENTITIES.put(Pattern.compile("&#162;"), "&cent;"); //  cent sign
        ENTITIES.put(Pattern.compile("&#163;"), "&pound;"); //  pound sterling sign
        ENTITIES.put(Pattern.compile("&#164;"), "&curren;"); //  general currency sign
        ENTITIES.put(Pattern.compile("&#165;"), "&yen;"); //  yen sign
        ENTITIES.put(Pattern.compile("&#166;"), "&brvbar;"); //  broken (vertical) bar
        ENTITIES.put(Pattern.compile("&#167;"), "&sect;"); //  section sign
        ENTITIES.put(Pattern.compile("&#168;"), "&uml;"); //  umlaut (dieresis)
        ENTITIES.put(Pattern.compile("&#169;"), "&copy;"); //  copyright sign
        ENTITIES.put(Pattern.compile("&#170;"), "&ordf;"); //  ordinal indicator, feminine
        ENTITIES.put(Pattern.compile("&#171;"), "&laquo;"); //  angle quotation mark, left
        ENTITIES.put(Pattern.compile("&#172;"), "&not;"); //  not sign
        ENTITIES.put(Pattern.compile("&#173;"), "&shy;"); //  soft hyphen
        ENTITIES.put(Pattern.compile("&#174;"), "&reg;"); //  registered sign
        ENTITIES.put(Pattern.compile("&#175;"), "&macr;"); //  macron
        ENTITIES.put(Pattern.compile("&#176;"), "&deg;"); //  degree sign
        ENTITIES.put(Pattern.compile("&#177;"), "&plusmn;"); //  plus-or-minus sign
        ENTITIES.put(Pattern.compile("&#178;"), "&sup2;"); //  superscript two
        ENTITIES.put(Pattern.compile("&#179;"), "&sup3;"); //  superscript three
        ENTITIES.put(Pattern.compile("&#180;"), "&acute;"); //  acute accent
        ENTITIES.put(Pattern.compile("&#181;"), "&micro;"); //  micro sign
        ENTITIES.put(Pattern.compile("&#182;"), "&para;"); //  pilcrow (paragraph sign)
        ENTITIES.put(Pattern.compile("&#183;"), "&middot;"); //  middle dot
        ENTITIES.put(Pattern.compile("&#184;"), "&cedil;"); //  cedilla
        ENTITIES.put(Pattern.compile("&#185;"), "&sup1;"); //  superscript one
        ENTITIES.put(Pattern.compile("&#186;"), "&ordm;"); //  ordinal indicator, masculine
        ENTITIES.put(Pattern.compile("&#187;"), "&raquo;"); //  angle quotation mark, right
        ENTITIES.put(Pattern.compile("&#188;"), "&frac14;"); //  fraction one-quarter
        ENTITIES.put(Pattern.compile("&#189;"), "&frac12;"); //  fraction one-half
        ENTITIES.put(Pattern.compile("&#190;"), "&frac34;"); //  fraction three-quarters
        ENTITIES.put(Pattern.compile("&#191;"), "&iquest;"); //  inverted question mark
        ENTITIES.put(Pattern.compile("&#192;"), "&Agrave;"); //  capital A, grave accent
        ENTITIES.put(Pattern.compile("&#193;"), "&Aacute;"); //  capital A, acute accent
        ENTITIES.put(Pattern.compile("&#194;"), "&Acirc;"); //  capital A, circumflex accent
        ENTITIES.put(Pattern.compile("&#195;"), "&Atilde;"); //  capital A, tilde
        ENTITIES.put(Pattern.compile("&#196;"), "&Auml;"); //  capital A, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#197;"), "&Aring;"); //  capital A, ring
        ENTITIES.put(Pattern.compile("&#198;"), "&AElig;"); //  capital AE diphthong (ligature)
        ENTITIES.put(Pattern.compile("&#199;"), "&Ccedil;"); //  capital C, cedilla
        ENTITIES.put(Pattern.compile("&#200;"), "&Egrave;"); //  capital E, grave accent
        ENTITIES.put(Pattern.compile("&#201;"), "&Eacute;"); //  capital E, acute accent
        ENTITIES.put(Pattern.compile("&#202;"), "&Ecirc;"); //  capital E, circumflex accent
        ENTITIES.put(Pattern.compile("&#203;"), "&Euml;"); //  capital E, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#204;"), "&Igrave;"); //  capital I, grave accent
        ENTITIES.put(Pattern.compile("&#205;"), "&Iacute;"); //  capital I, acute accent
        ENTITIES.put(Pattern.compile("&#206;"), "&Icirc;"); //  capital I, circumflex accent
        ENTITIES.put(Pattern.compile("&#207;"), "&Iuml;"); //  capital I, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#208;"), "&ETH;"); //  capital Eth, Icelandic
        ENTITIES.put(Pattern.compile("&#209;"), "&Ntilde;"); //  capital N, tilde
        ENTITIES.put(Pattern.compile("&#210;"), "&Ograve;"); //  capital O, grave accent
        ENTITIES.put(Pattern.compile("&#211;"), "&Oacute;"); //  capital O, acute accent
        ENTITIES.put(Pattern.compile("&#212;"), "&Ocirc;"); //  capital O, circumflex accent
        ENTITIES.put(Pattern.compile("&#213;"), "&Otilde;"); //  capital O, tilde
        ENTITIES.put(Pattern.compile("&#214;"), "&Ouml;"); //  capital O, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#215;"), "&times;"); //  multiply sign
        ENTITIES.put(Pattern.compile("&#216;"), "&Oslash;"); //  capital O, slash
        ENTITIES.put(Pattern.compile("&#217;"), "&Ugrave;"); //  capital U, grave accent
        ENTITIES.put(Pattern.compile("&#218;"), "&Uacute;"); //  capital U, acute accent
        ENTITIES.put(Pattern.compile("&#219;"), "&Ucirc;"); //  capital U, circumflex accent
        ENTITIES.put(Pattern.compile("&#220;"), "&Uuml;"); //  capital U, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#221;"), "&Yacute;"); //  capital Y, acute accent
        ENTITIES.put(Pattern.compile("&#222;"), "&THORN;"); //  capital THORN, Icelandic
        ENTITIES.put(Pattern.compile("&#223;"), "&szlig;"); //  small sharp s, German (sz ligature)
        ENTITIES.put(Pattern.compile("&#224;"), "&agrave;"); //  small a, grave accent
        ENTITIES.put(Pattern.compile("&#225;"), "&aacute;"); //  small a, acute accent
        ENTITIES.put(Pattern.compile("&#226;"), "&acirc;"); //  small a, circumflex accent
        ENTITIES.put(Pattern.compile("&#227;"), "&atilde;"); //  small a, tilde
        ENTITIES.put(Pattern.compile("&#228;"), "&auml;"); //  small a, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#229;"), "&aring;"); //  small a, ring
        ENTITIES.put(Pattern.compile("&#230;"), "&aelig;"); //  small ae diphthong (ligature)
        ENTITIES.put(Pattern.compile("&#231;"), "&ccedil;"); //  small c, cedilla
        ENTITIES.put(Pattern.compile("&#232;"), "&egrave;"); //  small e, grave accent
        ENTITIES.put(Pattern.compile("&#233;"), "&eacute;"); //  small e, acute accent
        ENTITIES.put(Pattern.compile("&#234;"), "&ecirc;"); //  small e, circumflex accent
        ENTITIES.put(Pattern.compile("&#235;"), "&euml;"); //  small e, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#236;"), "&igrave;"); //  small i, grave accent
        ENTITIES.put(Pattern.compile("&#237;"), "&iacute;"); //  small i, acute accent
        ENTITIES.put(Pattern.compile("&#238;"), "&icirc;"); //  small i, circumflex accent
        ENTITIES.put(Pattern.compile("&#239;"), "&iuml;"); //  small i, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#240;"), "&eth;"); //  small eth, Icelandic
        ENTITIES.put(Pattern.compile("&#241;"), "&ntilde;"); //  small n, tilde
        ENTITIES.put(Pattern.compile("&#242;"), "&ograve;"); //  small o, grave accent
        ENTITIES.put(Pattern.compile("&#243;"), "&oacute;"); //  small o, acute accent
        ENTITIES.put(Pattern.compile("&#244;"), "&ocirc;"); //  small o, circumflex accent
        ENTITIES.put(Pattern.compile("&#245;"), "&otilde;"); //  small o, tilde
        ENTITIES.put(Pattern.compile("&#246;"), "&ouml;"); //  small o, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#247;"), "&divide;"); //  divide sign
        ENTITIES.put(Pattern.compile("&#248;"), "&oslash;"); //  small o, slash
        ENTITIES.put(Pattern.compile("&#249;"), "&ugrave;"); //  small u, grave accent
        ENTITIES.put(Pattern.compile("&#250;"), "&uacute;"); //  small u, acute accent
        ENTITIES.put(Pattern.compile("&#251;"), "&ucirc;"); //  small u, circumflex accent
        ENTITIES.put(Pattern.compile("&#252;"), "&uuml;"); //  small u, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#253;"), "&yacute;"); //  small y, acute accent
        ENTITIES.put(Pattern.compile("&#254;"), "&thorn;"); //  small thorn, Icelandic
        ENTITIES.put(Pattern.compile("&#255;"), "&yuml;"); //  small y, dieresis or umlaut mark
        ENTITIES.put(Pattern.compile("&#263;"), "&cacute;");
        //  Latin Extended-A
        ENTITIES.put(Pattern.compile("&#338;"), "&OElig;"); //  latin capital ligature oe, u+0152 ISOlat2
        ENTITIES.put(Pattern.compile("&#339;"), "&oelig;"); //  latin small ligature oe, u+0153 ISOlat2
        ENTITIES.put(Pattern.compile("&#352;"), "&Scaron;"); //  latin capital letter s with caron, u+0160 ISOlat2
        ENTITIES.put(Pattern.compile("&#353;"), "&scaron;"); //  latin small letter s with caron, u+0161 ISOlat2
        ENTITIES.put(Pattern.compile("&#376;"), "&Yuml;"); //  latin capital letter y with diaeresis, u+0178 ISOlat2
        //  Latin Extended-B
        ENTITIES.put(Pattern.compile("&#402;"), "&fnof;"); //  latin small f with hook, =function, =florin, u+0192 ISOtech
        //  Spacing Modifier Letters
        ENTITIES.put(Pattern.compile("&#710;"), "&circ;"); //  modifier letter circumflex accent, u+02C6 ISOpub
        ENTITIES.put(Pattern.compile("&#732;"), "&tilde;"); //  small tilde, u+02DC ISOdia
        //  Greek
        ENTITIES.put(Pattern.compile("&#913;"), "&Alpha;"); //  greek capital letter alpha,  u+0391
        ENTITIES.put(Pattern.compile("&#914;"), "&Beta;"); //  greek capital letter beta,  u+0392
        ENTITIES.put(Pattern.compile("&#915;"), "&Gamma;"); //  greek capital letter gamma,  u+0393 ISOgrk3
        ENTITIES.put(Pattern.compile("&#916;"), "&Delta;"); //  greek capital letter delta,  u+0394 ISOgrk3
        ENTITIES.put(Pattern.compile("&#917;"), "&Epsilon;"); //  greek capital letter epsilon,  u+0395
        ENTITIES.put(Pattern.compile("&#918;"), "&Zeta;"); //  greek capital letter zeta,  u+0396
        ENTITIES.put(Pattern.compile("&#919;"), "&Eta;"); //  greek capital letter eta,  u+0397
        ENTITIES.put(Pattern.compile("&#920;"), "&Theta;"); //  greek capital letter theta,  u+0398 ISOgrk3
        ENTITIES.put(Pattern.compile("&#921;"), "&Iota;"); //  greek capital letter iota,  u+0399
        ENTITIES.put(Pattern.compile("&#922;"), "&Kappa;"); //  greek capital letter kappa,  u+039A
        ENTITIES.put(Pattern.compile("&#923;"), "&Lambda;"); //  greek capital letter lambda,  u+039B ISOgrk3
        ENTITIES.put(Pattern.compile("&#924;"), "&Mu;"); //  greek capital letter mu,  u+039C
        ENTITIES.put(Pattern.compile("&#925;"), "&Nu;"); //  greek capital letter nu,  u+039D
        ENTITIES.put(Pattern.compile("&#926;"), "&Xi;"); //  greek capital letter xi,  u+039E ISOgrk3
        ENTITIES.put(Pattern.compile("&#927;"), "&Omicron;"); //  greek capital letter omicron,  u+039F
        ENTITIES.put(Pattern.compile("&#928;"), "&Pi;"); //  greek capital letter pi,  u+03A0 ISOgrk3
        ENTITIES.put(Pattern.compile("&#929;"), "&Rho;"); //  greek capital letter rho,  u+03A1
        ENTITIES.put(Pattern.compile("&#931;"), "&Sigma;"); //  greek capital letter sigma,  u+03A3 ISOgrk3
        ENTITIES.put(Pattern.compile("&#932;"), "&Tau;"); //  greek capital letter tau,  u+03A4
        ENTITIES.put(Pattern.compile("&#933;"), "&Upsilon;"); //  greek capital letter upsilon,  u+03A5 ISOgrk3
        ENTITIES.put(Pattern.compile("&#934;"), "&Phi;"); //  greek capital letter phi,  u+03A6 ISOgrk3
        ENTITIES.put(Pattern.compile("&#935;"), "&Chi;"); //  greek capital letter chi,  u+03A7
        ENTITIES.put(Pattern.compile("&#936;"), "&Psi;"); //  greek capital letter psi,  u+03A8 ISOgrk3
        ENTITIES.put(Pattern.compile("&#937;"), "&Omega;"); //  greek capital letter omega,  u+03A9 ISOgrk3
        ENTITIES.put(Pattern.compile("&#945;"), "&alpha;"); //  greek small letter alpha, u+03B1 ISOgrk3
        ENTITIES.put(Pattern.compile("&#946;"), "&beta;"); //  greek small letter beta,  u+03B2 ISOgrk3
        ENTITIES.put(Pattern.compile("&#947;"), "&gamma;"); //  greek small letter gamma,  u+03B3 ISOgrk3
        ENTITIES.put(Pattern.compile("&#948;"), "&delta;"); //  greek small letter delta,  u+03B4 ISOgrk3
        ENTITIES.put(Pattern.compile("&#949;"), "&epsilon;"); //  greek small letter epsilon,  u+03B5 ISOgrk3
        ENTITIES.put(Pattern.compile("&#950;"), "&zeta;"); //  greek small letter zeta,  u+03B6 ISOgrk3
        ENTITIES.put(Pattern.compile("&#951;"), "&eta;"); //  greek small letter eta,  u+03B7 ISOgrk3
        ENTITIES.put(Pattern.compile("&#952;"), "&theta;"); //  greek small letter theta,  u+03B8 ISOgrk3
        ENTITIES.put(Pattern.compile("&#953;"), "&iota;"); //  greek small letter iota,  u+03B9 ISOgrk3
        ENTITIES.put(Pattern.compile("&#954;"), "&kappa;"); //  greek small letter kappa,  u+03BA ISOgrk3
        ENTITIES.put(Pattern.compile("&#955;"), "&lambda;"); //  greek small letter lambda,  u+03BB ISOgrk3
        ENTITIES.put(Pattern.compile("&#956;"), "&mu;"); //  greek small letter mu,  u+03BC ISOgrk3
        ENTITIES.put(Pattern.compile("&#957;"), "&nu;"); //  greek small letter nu,  u+03BD ISOgrk3
        ENTITIES.put(Pattern.compile("&#958;"), "&xi;"); //  greek small letter xi,  u+03BE ISOgrk3
        ENTITIES.put(Pattern.compile("&#959;"), "&omicron;"); //  greek small letter omicron,  u+03BF NEW
        ENTITIES.put(Pattern.compile("&#960;"), "&pi;"); //  greek small letter pi,  u+03C0 ISOgrk3
        ENTITIES.put(Pattern.compile("&#961;"), "&rho;"); //  greek small letter rho,  u+03C1 ISOgrk3
        ENTITIES.put(Pattern.compile("&#962;"), "&sigmaf;"); //  greek small letter final sigma,  u+03C2 ISOgrk3
        ENTITIES.put(Pattern.compile("&#963;"), "&sigma;"); //  greek small letter sigma,  u+03C3 ISOgrk3
        ENTITIES.put(Pattern.compile("&#964;"), "&tau;"); //  greek small letter tau,  u+03C4 ISOgrk3
        ENTITIES.put(Pattern.compile("&#965;"), "&upsilon;"); //  greek small letter upsilon,  u+03C5 ISOgrk3
        ENTITIES.put(Pattern.compile("&#966;"), "&phi;"); //  greek small letter phi,  u+03C6 ISOgrk3
        ENTITIES.put(Pattern.compile("&#967;"), "&chi;"); //  greek small letter chi,  u+03C7 ISOgrk3
        ENTITIES.put(Pattern.compile("&#968;"), "&psi;"); //  greek small letter psi,  u+03C8 ISOgrk3
        ENTITIES.put(Pattern.compile("&#969;"), "&omega;"); //  greek small letter omega,  u+03C9 ISOgrk3
        ENTITIES.put(Pattern.compile("&#977;"), "&thetasym;"); //  greek small letter theta symbol,  u+03D1 NEW
        ENTITIES.put(Pattern.compile("&#978;"), "&upsih;"); //  greek upsilon with hook symbol,  u+03D2 NEW
        ENTITIES.put(Pattern.compile("&#982;"), "&piv;"); //  greek pi symbol,  u+03D6 ISOgrk3
        // General punctuation
        ENTITIES.put(Pattern.compile("&#8194;"), "&ensp;"); //  en space, u+2002 ISOpub
        ENTITIES.put(Pattern.compile("&#8195;"), "&emsp;"); //  em space, u+2003 ISOpub
        ENTITIES.put(Pattern.compile("&#8201;"), "&thinsp;"); //  thin space, u+2009 ISOpub
        ENTITIES.put(Pattern.compile("&#8204;"), "&zwnj;"); //  zero width non-joiner, u+200C NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8205;"), "&zwj;"); //  zero width joiner, u+200D NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8206;"), "&lrm;"); //  left-to-right mark, u+200E NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8207;"), "&rlm;"); //  right-to-left mark, u+200F NEW RFC 2070
        ENTITIES.put(Pattern.compile("&#8211;"), "&ndash;"); //  en dash, u+2013 ISOpub
        ENTITIES.put(Pattern.compile("&#8212;"), "&mdash;"); //  em dash, u+2014 ISOpub
        ENTITIES.put(Pattern.compile("&#8216;"), "&lsquo;"); //  left single quotation mark, u+2018 ISOnum
        ENTITIES.put(Pattern.compile("&#8217;"), "&rsquo;"); //  right single quotation mark, u+2019 ISOnum
        ENTITIES.put(Pattern.compile("&#8218;"), "&sbquo;"); //  single low-9 quotation mark, u+201A NEW
        ENTITIES.put(Pattern.compile("&#8220;"), "&ldquo;"); //  left double quotation mark, u+201C ISOnum
        ENTITIES.put(Pattern.compile("&#8221;"), "&rdquo;"); //  right double quotation mark, u+201D ISOnum
        ENTITIES.put(Pattern.compile("&#8222;"), "&bdquo;"); //  double low-9 quotation mark, u+201E NEW
        ENTITIES.put(Pattern.compile("&#8224;"), "&dagger;"); //  dagger, u+2020 ISOpub
        ENTITIES.put(Pattern.compile("&#8225;"), "&Dagger;"); //  double dagger, u+2021 ISOpub
        ENTITIES.put(Pattern.compile("&#8226;"), "&bull;"); //  bullet, =black small circle, u+2022 ISOpub
        ENTITIES.put(Pattern.compile("&#8230;"), "&hellip;");
        ENTITIES.put(Pattern.compile("&#8240;"), "&permil;"); //  per mille sign, u+2030 ISOtech
        ENTITIES.put(Pattern.compile("&#8242;"), "&prime;"); //  prime, =minutes, =feet, u+2032 ISOtech
        ENTITIES.put(Pattern.compile("&#8243;"), "&Prime;"); //  double prime, =seconds, =inches, u+2033 ISOtech
        ENTITIES.put(Pattern.compile("&#8249;"), "&lsaquo;"); //  single left-pointing angle quotation mark, u+2039 ISO proposed
        ENTITIES.put(Pattern.compile("&#8250;"), "&rsaquo;"); //  single right-pointing angle quotation mark, u+203A ISO proposed
        ENTITIES.put(Pattern.compile("&#8254;"), "&oline;"); //  overline, =spacing overscore, u+203E NEW
        ENTITIES.put(Pattern.compile("&#8260;"), "&frasl;"); //  fraction slash, u+2044 NEW
        ENTITIES.put(Pattern.compile("&#8364;"), "&euro;");
        //  Letterlike Symbols
        ENTITIES.put(Pattern.compile("&#8472;"), "&weierp;"); //  script capital P, =power set, =Weierstrass p, u+2118 ISOamso
        ENTITIES.put(Pattern.compile("&#8465;"), "&image;"); //  blackletter capital I, =imaginary part, u+2111 ISOamso
        ENTITIES.put(Pattern.compile("&#8476;"), "&real;"); //  blackletter capital R, =real part symbol, u+211C ISOamso
        ENTITIES.put(Pattern.compile("&#8482;"), "&trade;"); //  trade mark sign, u+2122 ISOnum
        ENTITIES.put(Pattern.compile("&#8501;"), "&alefsym;"); //  alef symbol, =first transfinite cardinal, u+2135 NEW
        //  Arrows
        ENTITIES.put(Pattern.compile("&#8592;"), "&larr;"); //  leftwards arrow, u+2190 ISOnum
        ENTITIES.put(Pattern.compile("&#8593;"), "&uarr;"); //  upwards arrow, u+2191 ISOnum
        ENTITIES.put(Pattern.compile("&#8594;"), "&rarr;"); //  rightwards arrow, u+2192 ISOnum
        ENTITIES.put(Pattern.compile("&#8595;"), "&darr;"); //  downwards arrow, u+2193 ISOnum
        ENTITIES.put(Pattern.compile("&#8596;"), "&harr;"); //  left right arrow, u+2194 ISOamsa
        ENTITIES.put(Pattern.compile("&#8629;"), "&crarr;"); //  downwards arrow with corner leftwards, =carriage return, u+21B5 NEW
        ENTITIES.put(Pattern.compile("&#8656;"), "&lArr;"); //  leftwards double arrow, u+21D0 ISOtech
        ENTITIES.put(Pattern.compile("&#8657;"), "&uArr;"); //  upwards double arrow, u+21D1 ISOamsa
        ENTITIES.put(Pattern.compile("&#8658;"), "&rArr;"); //  rightwards double arrow, u+21D2 ISOtech
        ENTITIES.put(Pattern.compile("&#8659;"), "&dArr;"); //  downwards double arrow, u+21D3 ISOamsa
        ENTITIES.put(Pattern.compile("&#8660;"), "&hArr;"); //  left right double arrow, u+21D4 ISOamsa
        //  Mathematical Operators
        ENTITIES.put(Pattern.compile("&#8704;"), "&forall;"); //  for all, u+2200 ISOtech
        ENTITIES.put(Pattern.compile("&#8706;"), "&part;"); //  partial differential, u+2202 ISOtech
        ENTITIES.put(Pattern.compile("&#8707;"), "&exist;"); //  there exists, u+2203 ISOtech
        ENTITIES.put(Pattern.compile("&#8709;"), "&empty;"); //  empty set, =null set, =diameter, u+2205 ISOamso
        ENTITIES.put(Pattern.compile("&#8711;"), "&nabla;"); //  nabla, =backward difference, u+2207 ISOtech
        ENTITIES.put(Pattern.compile("&#8712;"), "&isin;"); //  element of, u+2208 ISOtech
        ENTITIES.put(Pattern.compile("&#8713;"), "&notin;"); //  not an element of, u+2209 ISOtech
        ENTITIES.put(Pattern.compile("&#8715;"), "&ni;"); //  contains as member, u+220B ISOtech
        ENTITIES.put(Pattern.compile("&#8719;"), "&prod;"); //  n-ary product, =product sign, u+220F ISOamsb
        ENTITIES.put(Pattern.compile("&#8721;"), "&sum;"); //  n-ary sumation, u+2211 ISOamsb
        ENTITIES.put(Pattern.compile("&#8722;"), "&minus;"); //  minus sign, u+2212 ISOtech
        ENTITIES.put(Pattern.compile("&#8727;"), "&lowast;"); //  asterisk operator, u+2217 ISOtech
        ENTITIES.put(Pattern.compile("&#8730;"), "&radic;"); //  square root, =radical sign, u+221A ISOtech
        ENTITIES.put(Pattern.compile("&#8733;"), "&prop;"); //  proportional to, u+221D ISOtech
        ENTITIES.put(Pattern.compile("&#8734;"), "&infin;"); //  infinity, u+221E ISOtech
        ENTITIES.put(Pattern.compile("&#8736;"), "&ang;"); //  angle, u+2220 ISOamso
        ENTITIES.put(Pattern.compile("&#8869;"), "&and;"); //  logical and, =wedge, u+2227 ISOtech
        ENTITIES.put(Pattern.compile("&#8870;"), "&or;"); //  logical or, =vee, u+2228 ISOtech
        ENTITIES.put(Pattern.compile("&#8745;"), "&cap;"); //  intersection, =cap, u+2229 ISOtech
        ENTITIES.put(Pattern.compile("&#8746;"), "&cup;"); //  union, =cup, u+222A ISOtech
        ENTITIES.put(Pattern.compile("&#8747;"), "&int;"); //  integral, u+222B ISOtech
        ENTITIES.put(Pattern.compile("&#8756;"), "&there4;"); //  therefore, u+2234 ISOtech
        ENTITIES.put(Pattern.compile("&#8764;"), "&sim;"); //  tilde operator, =varies with, =similar to, u+223C ISOtech
        ENTITIES.put(Pattern.compile("&#8773;"), "&cong;"); //  approximately equal to, u+2245 ISOtech
        ENTITIES.put(Pattern.compile("&#8776;"), "&asymp;"); //  almost equal to, =asymptotic to, u+2248 ISOamsr
        ENTITIES.put(Pattern.compile("&#8800;"), "&ne;"); //  not equal to, u+2260 ISOtech
        ENTITIES.put(Pattern.compile("&#8801;"), "&equiv;"); //  identical to, u+2261 ISOtech
        ENTITIES.put(Pattern.compile("&#8804;"), "&le;"); //  less-than or equal to, u+2264 ISOtech
        ENTITIES.put(Pattern.compile("&#8805;"), "&ge;"); //  greater-than or equal to, u+2265 ISOtech
        ENTITIES.put(Pattern.compile("&#8834;"), "&sub;"); //  subset of, u+2282 ISOtech
        ENTITIES.put(Pattern.compile("&#8835;"), "&sup;"); //  superset of, u+2283 ISOtech
        ENTITIES.put(Pattern.compile("&#8836;"), "&nsub;"); //  not a subset of, u+2284 ISOamsn
        ENTITIES.put(Pattern.compile("&#8838;"), "&sube;"); //  subset of or equal to, u+2286 ISOtech
        ENTITIES.put(Pattern.compile("&#8839;"), "&supe;"); //  superset of or equal to, u+2287 ISOtech
        ENTITIES.put(Pattern.compile("&#8853;"), "&oplus;"); //  circled plus, =direct sum, u+2295 ISOamsb
        ENTITIES.put(Pattern.compile("&#8855;"), "&otimes;"); //  circled times, =vector product, u+2297 ISOamsb
        ENTITIES.put(Pattern.compile("&#8869;"), "&perp;"); //  up tack, =orthogonal to, =perpendicular, u+22A5 ISOtech
        ENTITIES.put(Pattern.compile("&#8901;"), "&sdot;"); //  dot operator, u+22C5 ISOamsb
        //  Miscellaneous Technical
        ENTITIES.put(Pattern.compile("&#8968;"), "&lceil;"); //  left ceiling, =apl upstile, u+2308, ISOamsc
        ENTITIES.put(Pattern.compile("&#8969;"), "&rceil;"); //  right ceiling, u+2309, ISOamsc
        ENTITIES.put(Pattern.compile("&#8970;"), "&lfloor;"); //  left floor, =apl downstile, u+230A, ISOamsc
        ENTITIES.put(Pattern.compile("&#8971;"), "&rfloor;"); //  right floor, u+230B, ISOamsc
        ENTITIES.put(Pattern.compile("&#9001;"), "&lang;"); //  left-pointing angle bracket, =bra, u+2329 ISOtech
        ENTITIES.put(Pattern.compile("&#9002;"), "&rang;"); //  right-pointing angle bracket, =ket, u+232A ISOtech
        //  Geometric Shapes
        ENTITIES.put(Pattern.compile("&#9674;"), "&loz;"); //  lozenge, u+25CA ISOpub
        //  Miscellaneous Symbols
        ENTITIES.put(Pattern.compile("&#9824;"), "&spades;"); //  black spade suit, u+2660 ISOpub
        ENTITIES.put(Pattern.compile("&#9827;"), "&clubs;"); //  black club suit, =shamrock, u+2663 ISOpub
        ENTITIES.put(Pattern.compile("&#9829;"), "&hearts;"); //  black heart suit, =valentine, u+2665 ISOpub
        ENTITIES.put(Pattern.compile("&#9830;"), "&diams;"); //  black diamond suit, u+2666 ISOpub
        // Other entities
        ENTITIES.put(Pattern.compile("&Aacute;"), "Á");
        ENTITIES.put(Pattern.compile("&Agrave;"), "À");
        ENTITIES.put(Pattern.compile("&Acirc;"), "Â");
        ENTITIES.put(Pattern.compile("&Auml;"), "Ä");
        ENTITIES.put(Pattern.compile("&Aring;"), "Å");
        ENTITIES.put(Pattern.compile("&AElig;"), "Æ");
        ENTITIES.put(Pattern.compile("&aacute;"), "á");
        ENTITIES.put(Pattern.compile("&agrave;"), "à");
        ENTITIES.put(Pattern.compile("&acirc;"), "â");
        ENTITIES.put(Pattern.compile("&auml;"), "ä");
        ENTITIES.put(Pattern.compile("&aring;"), "å");
        ENTITIES.put(Pattern.compile("&aelig;"), "æ");
        ENTITIES.put(Pattern.compile("&Eacute;"), "É");
        ENTITIES.put(Pattern.compile("&Egrave;"), "È");
        ENTITIES.put(Pattern.compile("&Ecirc;"), "Ê");
        ENTITIES.put(Pattern.compile("&Euml;"), "Ë");
        ENTITIES.put(Pattern.compile("&eacute;"), "é");
        ENTITIES.put(Pattern.compile("&egrave;"), "è");
        ENTITIES.put(Pattern.compile("&ecirc;"), "ê");
        ENTITIES.put(Pattern.compile("&euml;"), "ë");
        ENTITIES.put(Pattern.compile("&Iacute;"), "Í");
        ENTITIES.put(Pattern.compile("&Igrave;"), "Ì");
        ENTITIES.put(Pattern.compile("&Icirc;"), "Î");
        ENTITIES.put(Pattern.compile("&Iuml;"), "Ï");
        ENTITIES.put(Pattern.compile("&iacute;"), "í");
        ENTITIES.put(Pattern.compile("&igrave;"), "ì");
        ENTITIES.put(Pattern.compile("&icirc;"), "î");
        ENTITIES.put(Pattern.compile("&iuml;"), "ï");
        ENTITIES.put(Pattern.compile("&Oacute;"), "Ó");
        ENTITIES.put(Pattern.compile("&Ograve;"), "Ò");
        ENTITIES.put(Pattern.compile("&Ocirc;"), "Ô");
        ENTITIES.put(Pattern.compile("&Ouml;"), "Ö");
        ENTITIES.put(Pattern.compile("&OElig;"), "Œ");
        ENTITIES.put(Pattern.compile("&oacute;"), "ó");
        ENTITIES.put(Pattern.compile("&ograve;"), "ò");
        ENTITIES.put(Pattern.compile("&ocirc;"), "ô");
        ENTITIES.put(Pattern.compile("&ouml;"), "ö");
        ENTITIES.put(Pattern.compile("&oelig;"), "œ");
        ENTITIES.put(Pattern.compile("&Uacute;"), "Ú");
        ENTITIES.put(Pattern.compile("&Ugrave;"), "Ù");
        ENTITIES.put(Pattern.compile("&Ucirc;"), "Û");
        ENTITIES.put(Pattern.compile("&Uuml;"), "Ü");
        ENTITIES.put(Pattern.compile("&uacute;"), "ú");
        ENTITIES.put(Pattern.compile("&ugrave;"), "ù");
        ENTITIES.put(Pattern.compile("&ucirc;"), "û");
        ENTITIES.put(Pattern.compile("&uuml;"), "ü");
        ENTITIES.put(Pattern.compile("&Ccedil;"), "Ç");
        ENTITIES.put(Pattern.compile("&Cacute;"), "Ć");
        ENTITIES.put(Pattern.compile("&ccedil;"), "ç");
        ENTITIES.put(Pattern.compile("&cacute;"), "ć");
        ENTITIES.put(Pattern.compile("&quot;"), "\"");
        ENTITIES.put(Pattern.compile("&nbsp;"), "\u00a0");
        ENTITIES.put(Pattern.compile("&rsquo;"), "’");
        ENTITIES.put(Pattern.compile("&lsquo;"), "‘");
        ENTITIES.put(Pattern.compile("&laquo;"), "«");
        ENTITIES.put(Pattern.compile("&raquo;"), "»");
        ENTITIES.put(Pattern.compile("&ldquo;"), "“");
        ENTITIES.put(Pattern.compile("&rdquo;"), "”");
        ENTITIES.put(Pattern.compile("&ndash;"), "\u2013");
        ENTITIES.put(Pattern.compile("&mdash;"), "\u2014");
        ENTITIES.put(Pattern.compile("&dagger;"), "†");
        ENTITIES.put(Pattern.compile("&hellip;"), "…");
        ENTITIES.put(Pattern.compile("&copy;"), "©");
        ENTITIES.put(Pattern.compile("&beta;"), "β");
        ENTITIES.put(Pattern.compile("&sect;"), "§");
        ENTITIES.put(Pattern.compile("&para;"), "¶");
        ENTITIES.put(Pattern.compile("&deg;"), "°");
        ENTITIES.put(Pattern.compile("&ordm;"), "º");
        ENTITIES.put(Pattern.compile("&Scaron;"), "Š");
        ENTITIES.put(Pattern.compile("&scaron;"), "š");
    }

}
