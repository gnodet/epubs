package fr.gnodet.epubs.core;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static fr.gnodet.epubs.core.IOUtil.writeToFile;

public class Cover {

    public static String generateCoverHtml(String creator, String titlefr, String title, String full) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "  <head>\n" +
                "    <title>Cover</title>\n" +
                "    <style type=\"text/css\"> img { max-width: 100%; } </style>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <div id=\"cover-image\">\n" +
                "      <img src=\"img/cover.png\" alt=\"" + titlefr + "\"/>\n" +
                "    </div>\n" +
                "  </body>\n" +
                "</html>";
    }

    public static class Text {
        String text;
        Font font;
        double scale;
        double color;

        public Text(String text, Font font, double scale, double color) {
            this.text = text;
            this.font = font;
            this.scale = scale;
            this.color = color;
        }
    }

    public static class Break {
        double factor = 1.0;

        public Break() {
        }

        public Break(double factor) {
            this.factor = factor;
        }
    }

    public static byte[] generateCoverPng(double hue,
                                          String creator,
                                          String title,
                                          String subtitle,
                                          String full,
                                          URL svg) throws IOException, TranscoderException {
        Object[] textParts = new Object[] {
                new Text(creator.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 58), 1.0, 0.0),
                new Break(),
                new Text(title.toUpperCase(), new Font(Font.SERIF, Font.PLAIN, 96), 1.2, 0.25),
                new Break(),
                new Text(subtitle, new Font(Font.SERIF, Font.ITALIC, 58), 1.0, 0.0),
        };
        return generateCoverPng(hue, title, textParts, svg);
    }

    public static byte[] generateCoverPng(double hue,
                                          String title,
                                          Object[] textParts,
                                          URL svg) throws IOException, TranscoderException {
        return generateCoverPng(hue, title, textParts, svg, null);
    }

    public static byte[] generateCoverPng(double hue,
                                          String title,
                                          Object[] textParts,
                                          URL svg,
                                          URL photo) throws IOException, TranscoderException {

        int width = 711;
        int height = 1084;
        int borderOut = 40;
        int borderIn = 55;

        Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, height);
        Rectangle2D borderOutRect = new Rectangle2D.Double(bounds.getX() + borderOut, bounds.getY() + borderOut,
                                                           bounds.getWidth() - borderOut * 2, bounds.getHeight() - borderOut * 2);
        Rectangle2D borderInRect = new Rectangle2D.Double(bounds.getX() + borderIn, bounds.getY() + borderIn,
                                                          bounds.getWidth() - borderIn * 2, bounds.getHeight() - borderIn * 2);

        String backColor =   hsl2rgb(hue, 0.9, 0.5);
        String borderColor = hsl2rgb(hue, 0.9, 0.85);
        String titleColor =  hsl2rgb(hue, 0.9, 0.25);

        double radius = 35.0;

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" preserveAspectRatio=\"xMidYMid meet\" " +
                        "viewBox=\"" + bounds.getX() + " " + bounds.getY() + " " + bounds.getWidth() + " " + bounds.getHeight() + "\">\n");
        sb.append("<rect x=\"" + bounds.getX() + "\" y=\"" + bounds.getY() + "\" " +
                         "width=\"" + bounds.getWidth() + "\" height=\"" + bounds.getHeight() + "\" " +
                         "fill=\"").append(backColor).append("\"/>\n");

        sb.append("<rect x=\"" + borderOutRect.getX() + "\" y=\"" + borderOutRect.getY() + "\" " +
                         "width=\"" + borderOutRect.getWidth() + "\" height=\"" + borderOutRect.getHeight() + "\" " +
                         "fill=\"").append(borderColor).append("\"/>\n");

        sb.append("<rect x=\"" + borderInRect.getX() + "\" y=\"" + borderInRect.getY() + "\" " +
                         "width=\"" + borderInRect.getWidth() + "\" height=\"" + borderInRect.getHeight() + "\" " +
                         "fill=\"").append("white").append("\"/>\n");

        if (radius > 0.0) {
            sb.append("<g fill=\"").append(borderColor).append("\">");
            sb.append("<circle cx=\"" + borderOutRect.getX() + "\" cy=\"" + borderOutRect.getY() + "\" r=\"" + (radius + borderIn - borderOut) + "\" />");
            sb.append("<circle cx=\"" + borderOutRect.getMaxX() + "\" cy=\"" + borderOutRect.getY() + "\" r=\"" + (radius + borderIn - borderOut) + "\" />");
            sb.append("<circle cx=\"" + borderOutRect.getMaxX() + "\" cy=\"" + borderOutRect.getMaxY() + "\" r=\"" + (radius + borderIn - borderOut) + "\" />");
            sb.append("<circle cx=\"" + borderOutRect.getX() + "\" cy=\"" + borderOutRect.getMaxY() + "\" r=\"" + (radius + borderIn - borderOut) + "\" />");
            sb.append("</g>");
            sb.append("<g fill=\"").append(backColor).append("\">");
            sb.append("<circle cx=\"" + borderOutRect.getX() + "\" cy=\"" + borderOutRect.getY() + "\" r=\"" + (radius) + "\" />");
            sb.append("<circle cx=\"" + borderOutRect.getMaxX() + "\" cy=\"" + borderOutRect.getY() + "\" r=\"" + (radius) + "\" />");
            sb.append("<circle cx=\"" + borderOutRect.getMaxX() + "\" cy=\"" + borderOutRect.getMaxY() + "\" r=\"" + (radius) + "\" />");
            sb.append("<circle cx=\"" + borderOutRect.getX() + "\" cy=\"" + borderOutRect.getMaxY() + "\" r=\"" + (radius) + "\" />");
            sb.append("</g>");

            sb.append("<g fill=\"").append(backColor).append("\">");
            sb.append("<rect x=\"" + bounds.getX() + "\" y=\"" + bounds.getY() + "\" " + "width=\"" + bounds.getWidth() + "\" height=\"" + (borderOutRect.getY() - bounds.getY()) + "\" />\n");
            sb.append("<rect x=\"" + bounds.getX() + "\" y=\"" + borderOutRect.getMaxY() + "\" " + "width=\"" + bounds.getWidth() + "\" height=\"" + (bounds.getMaxY() - borderOutRect.getMaxY()) + "\" />\n");
            sb.append("<rect x=\"" + bounds.getX() + "\" y=\"" + borderOutRect.getY() + "\" " + "width=\"" + (borderOutRect.getX() - bounds.getX()) + "\" height=\"" + borderOutRect.getHeight() + "\" />\n");
            sb.append("<rect x=\"" + borderOutRect.getMaxX() + "\" y=\"" + borderOutRect.getY() + "\" " + "width=\"" + (bounds.getMaxX() - borderOutRect.getMaxX()) + "\" height=\"" + borderOutRect.getHeight() + "\" />\n");
            sb.append("</g>");
        }

        double svgMargin = 47.0;
        double svgHeight = svg != null || photo != null ? 324.0 : 0.0;
        Rectangle2D svgBox = new Rectangle2D.Double(borderInRect.getMinX() + svgMargin,
                borderInRect.getMaxY() - svgMargin - svgHeight,
                borderInRect.getWidth() - svgMargin * 2,
                svgHeight);

        double textMargin = 25.0;
        Rectangle2D textBox = new Rectangle2D.Double(borderInRect.getMinX() + textMargin,
                borderInRect.getMinY() + textMargin,
                borderInRect.getWidth() - 2 * textMargin,
                svgBox.getMinY() - borderInRect.getMinY() - 2 * textMargin);

        Graphics graphics = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB).getGraphics();
        {
            class TextInfo {
                int height;
                List<String> parts;
                double scale;
            }
            TextInfo[] infos = new TextInfo[textParts.length];
            int curUsed = 0;
            double totBreak = 0.0;
            for (int i = 0; i < textParts.length; i++) {
                if (textParts[i] instanceof Text) {
                    Text text = (Text) textParts[i];
                    FontMetrics fm = graphics.getFontMetrics(text.font);
                    TextInfo info = new TextInfo();
                    info.scale = text.scale;
                    List<String> p0 = wrap(text.text, fm, (int) ((textBox.getWidth() - textMargin * 0) * info.scale));
                    List<String> p1 = wrap(text.text, fm, (int) ((textBox.getWidth() - textMargin * 2) * info.scale));
                    boolean useMargin = p1.size() == 2;
                    info.parts = useMargin ? p1 : p0;
                    for (String str : info.parts) {
                        int tw = fm.stringWidth(str);
                        if (tw / text.scale > (textBox.getWidth() - textMargin * 2)) {
                            double s = tw / (textBox.getWidth() - textMargin * 2);
                            info.scale = Math.max(info.scale, s);
                        }
                    }
                    info.parts = wrap(text.text, fm, (int) ((textBox.getWidth() - textMargin * (useMargin ? 2 : 0)) * info.scale));
                    info.height = info.parts.size() * text.font.getSize();
                    infos[i] = info;
                    curUsed += info.height;
                } else if (textParts[i] instanceof Break) {
                    totBreak += ((Break) textParts[i]).factor;
                }
            }
            for (int i = 0; i < textParts.length; i++) {
                if (textParts[i] instanceof Break) {
                    TextInfo info = new TextInfo();
                    info.height = (int) Math.round((textBox.getHeight() - curUsed) / totBreak * ((Break) textParts[i]).factor);
                    infos[i] = info;
                }
            }
            double minY = textBox.getMinY();
            for (int i = 0; i < textParts.length; i++) {
                if (textParts[i] instanceof Text) {
                    Text text = (Text) textParts[i];
                    FontMetrics fm = graphics.getFontMetrics(text.font);
                    sb.append("<g font-family=\"serif\" font-size=\"" + text.font.getSize() + "\" fill=\"" + hsl2rgb(hue, 0.9, text.color) + "\" text-anchor=\"middle\" " +
                            ((text.font.getStyle() & Font.ITALIC) == Font.ITALIC ? "font-style=\"italic\" " : "") +
                            "transform=\"scale(" + (1.0/infos[i].scale) + " 1.0) translate(" + (textBox.getCenterX() * infos[i].scale) + ", " + (minY + text.font.getSize()) + ")\">");
                    for (int j = 0; j < infos[i].parts.size(); j++) {
                        sb.append("<text x=\"0\" y=\"").append(j * text.font.getSize()).append("\">")
                                .append(infos[i].parts.get(j))
                                .append("</text>\n");
                    }
                    sb.append("</g>");
                }
                minY += infos[i].height;
            }
        }

        if (svg != null) {
            String innerSvg = new String(IOUtil.readFully(svg));
            if (innerSvg.startsWith("<?")) {
                innerSvg = innerSvg.substring(innerSvg.indexOf("?>") + 2);
            }
            int vbi = innerSvg.indexOf("viewBox=");
            String[] viewBox = innerSvg.substring(vbi + 9, innerSvg.indexOf("\"", vbi+9)).split(" ");
            double iw = new Double(viewBox[2]);
            double ih = new Double(viewBox[3]);
            double rw = svgHeight * iw / ih;
            double x = (width - rw) / 2;
            sb.append("<svg y=\"").append(svgBox.getMinY()).append("\"")
                    .append(" x=\"").append(x).append("\"")
                    .append(" height=\"").append(svgHeight).append("\" >")
                    .append(innerSvg)
                    .append("</svg>");
        } else if (photo != null) {
            sb.append("<image y=\"")
                    .append(svgBox.getMinY())
                    .append("\" x=\"")
                    .append(svgBox.getMinX())
                    .append("\" height=\"")
                    .append(svgBox.getHeight())
                    .append("\" width=\"")
                    .append(svgBox.getWidth())
                    .append("\" ")
                    .append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
                    .append("xlink:href=\"")
                    .append(photo)
                    .append("\"/>");
        }
        sb.append("</svg>");


        new File("target/svgs").mkdirs();
        writeToFile(sb.toString(), "target/svgs/" + pad(Long.toString((long) (hue * 100)), 2, "0") + " - " + title + ".svg");

        // Create a JPEG transcoder
        PNGTranscoder t = new PNGTranscoder();
        // Set the transcoding hints.
        t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
        t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        t.addTranscodingHint(PNGTranscoder.KEY_AOI, new Rectangle(width, height));
        // Create the transcoder input.
        TranscoderInput input = new TranscoderInput(new StringReader(sb.toString()));
        // Create the transcoder output.
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();

        writeToFile(ostream.toByteArray(), "target/pngs/" + pad(Long.toString((long) (hue * 100)), 2, "0") + " - " + title + ".png");

        return ostream.toByteArray();
    }

    private static String pad(String str, int length, String chr) {
        while (str.length() < length) {
            str = chr + str;
        }
        return str;
    }

    private static double hue2rgb(double p, double q, double t) {
        if(t < 0.0) t += 1.0;
        if(t > 1.0) t -= 1.0;
        if(t < 1.0/6.0) return p + (q - p) * 6.0 * t;
        if(t < 1.0/2.0) return q;
        if(t < 2.0/3.0) return p + (q - p) * (2.0/3.0 - t) * 6.0;
        return p;
    }
    private static String hsl2rgb(double h, double s, double l) {
        double r, g, b;
        if (s == 0.0) {
            r = g = b = l; // achromatic
        } else {
            double q = l < 0.5 ? l * (1.0 + s) : l + s - l * s;
            double p = 2.0 * l - q;
            r = hue2rgb(p, q, h + 1.0/3.0);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1.0/3.0);
        }

        return toRGB(r, g, b);
    }

    private static String toRGB(double r, double g, double b) {
        int lr = (int)(r * 255.0);
        int lg = (int)(g * 255.0);
        int lb = (int)(b * 255.0);
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        if (lr < 16) {
            sb.append("0");
        }
        sb.append(Integer.toHexString(lr));
        if (lg < 16) {
            sb.append("0");
        }
        sb.append(Integer.toHexString(lg));
        if (lb < 16) {
            sb.append("0");
        }
        sb.append(Integer.toHexString(lb));
        return sb.toString();
    }

    private static List<String> wrap(String phrase, FontMetrics fm, int width) {
        String[] words = phrase.split(" ");
        int hi = fm.stringWidth(phrase);
        int lo = Integer.MAX_VALUE;
        int lines = hi / width + 1;
        for (String word : words) {
            lo = Math.min(lo, fm.stringWidth(word));
        }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            List<String> v = wrapMinWidth(words, fm, mid);
            if (v.size() > lines) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return wrapMinWidth(words, fm, lo);
    }

    private static List<String> wrapMinWidth(String[] words, FontMetrics fm, int n) {
        List<String> r = new ArrayList<String>();
        String l = "";
        for (String w : words) {
            if (l.length() > 0 && fm.stringWidth(l + " " + w) > n) {
                r.add(l);
                l = "";
            }
            if (l.length() > 0) {
                l = l + " " + w;
            } else {
                l = w;
            }
        }
        if (l.length() > 0) {
            r.add(l);
        }
        return r;
    }

    public static List<String> minLines(String phrase, int lines) {
        String[] words = phrase.split(" ");
        int hi = 0;
        int lo = Integer.MAX_VALUE;
        for (String word : words) {
            hi += word.length();
            lo = Math.min(lo, word.length());
        }
        for (;;) {
            int mid = lo + (hi - lo) / 2;
            List<String> v = wrapMinWidth(words, mid);
            if (lo == mid) {
                return v;
            }
            if (v.size() > lines) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
    }

    private static List<String> wrapMinWidth(String[] words, int n) {
        List<String> r = new ArrayList<String>();
        String l = "";
        for (String w : words) {
            if (l.length() > 0 && w.length() + l.length() > n) {
                r.add(l);
                l = "";
            }
            if (l.length() > 0) {
                l = l + " " + w;
            } else {
                l = w;
            }
        }
        if (l.length() > 0) {
            r.add(l);
        }
        return r;
    }

}
