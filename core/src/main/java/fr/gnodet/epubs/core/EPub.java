package fr.gnodet.epubs.core;

import com.adobe.epubcheck.api.EpubCheck;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EPub {

    public static void createEpub(File input, File output, String title, String creator) throws Exception {
        createEpub(new File[] { input }, new HashMap<String, byte[]>(), output, title, creator, null);
    }

    public static void createEpub(File[] inputs, File output, String title, String creator, String tocNcx) throws Exception {
        createEpub(inputs, new HashMap<String, byte[]>(), output, title, creator, tocNcx);
    }

    public static void createEpub(File[] inputs, Map<String, byte[]> resources, File output, String title, String creator, String tocNcx) throws Exception {
        createEpub(inputs, resources, output, title, creator, tocNcx, null);
    }

    public static void createEpub(File[] inputs, Map<String, byte[]> resources, File output, String title, String creator, String tocNcx, String uuid) throws Exception {
        output.getParentFile().mkdirs();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
        // mimetype
        ZipEntry ze = new ZipEntry("mimetype");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize("application/epub+zip".getBytes().length);
        ze.setCrc(0x2cab616f);
        zos.putNextEntry(ze);
        zos.write("application/epub+zip".getBytes());
        zos.closeEntry();
        // META-INF/
        zos.putNextEntry(new ZipEntry("META-INF/"));
        // META-INF/container.xml
        zos.putNextEntry(new ZipEntry("META-INF/container.xml"));
        zos.write(("<?xml version=\"1.0\"?>\n" +
                "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n" +
                "  <rootfiles>\n" +
                "    <rootfile full-path=\"content.opf\" media-type=\"application/oebps-package+xml\"/>\n" +
                "  </rootfiles>\n" +
                "</container>\n").getBytes("UTF-8"));
        zos.closeEntry();
        // content.opf
        zos.putNextEntry(new ZipEntry("content.opf"));
        String contentOpf =
                "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"2.0\" unique-identifier=\"uuid_id\">\n" +
                "  <metadata xmlns:opf=\"http://www.idpf.org/2007/opf\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "    <dc:title>" + title + "</dc:title>\n" +
                "    <dc:language>fr</dc:language>\n" +
                "    <dc:creator opf:file-as=\"" + creator + "\" opf:role=\"aut\">" + creator + "</dc:creator>\n" +
                "    <meta name=\"cover\" content=\"cover-image\"/>\n" +
                "    <dc:identifier id=\"uuid_id\" opf:scheme=\"uuid\">" + (uuid != null ? uuid : UUID.randomUUID()) + "</dc:identifier>\n" +
                "  </metadata>\n" +
                "  <manifest>\n";
        for (int i = 0; i < inputs.length; i++) {
            contentOpf +=
                    "    <item id=\"s" + Integer.toString(i) + "\" media-type=\"application/xhtml+xml\" href=\"OEBPS/" + inputs[i].getName() + "\" />\n";
        }
        boolean includeCover = false;
        boolean hasCover = false;
        {
            int i = 0;
            for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
                String type;
                if (resource.getKey().endsWith(".svg")) {
                    type = "image/svg+xml";
                } else if (resource.getKey().endsWith(".jpg")) {
                    type = "image/jpeg";
                } else if (resource.getKey().endsWith(".png")) {
                    type = "image/png";
                } else if (resource.getKey().endsWith(".html")) {
                    type = "application/xhtml+xml";
                } else {
                    type = "binary";
                }
                String id;
                if (resource.getKey().endsWith("cover.jpg") || resource.getKey().endsWith("cover.png")) {
                    id = "cover-image";
                } else if (resource.getKey().endsWith("cover.html")) {
                    id = "cover";
                    hasCover = true;
                } else {
                    id = "r" + Integer.toString(i++);
                }
                contentOpf +=
                        "   <item id=\"" + id + "\" media-type=\"" + type + "\" href=\"" + resource.getKey() + "\" />\n";
            }
        }
        contentOpf +=
                "    <item href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\" id=\"ncx\"/>\n" +
                "  </manifest>\n" +
                "  <spine toc=\"ncx\"> \n";
        if (hasCover && includeCover) {
            contentOpf +=
                "    <itemref idref=\"cover\"/>\n";
        }
        for (int i = 0; i < inputs.length; i++) {
            contentOpf +=
                "    <itemref idref=\"s" + Integer.toString(i) + "\"/>\n";
        }
        contentOpf +=
                "  </spine>\n" +
                "</package>\n";
        zos.write(contentOpf.getBytes());
        zos.closeEntry();
        // toc.ncx
        zos.putNextEntry(new ZipEntry("toc.ncx"));
        if (tocNcx == null) {
            tocNcx =
                    "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\" xml:lang=\"eng\">\n" +
                    "  <head>\n" +
                    "    <meta content=\"1\" name=\"dtb:depth\"/>\n" +
                    "  </head>\n" +
                    "  <docTitle>\n" +
                    "    <text>" + title + "</text>\n" +
                    "  </docTitle>\n" +
                    "  <navMap>\n" +
                    "    <navPoint id=\"id-1\" playOrder=\"1\">\n" +
                    "      <navLabel>\n" +
                    "        <text>" + title + "</text>\n" +
                    "      </navLabel>\n";
            if (hasCover && includeCover) {
                tocNcx +=
                        "      <content src=\"OEBPS/cover.html\"/>\n";
            }
            else {
                tocNcx +=
                        "      <content src=\"OEBPS/" + inputs[0].getName() + "\"/>\n";
            }
//            for (int i = 0; i < 1; i++) {
//                tocNcx +=
//                        "      <content src=\"OEBPS/" + inputs[i].getName() + "\"/>\n";
//            }
            tocNcx +=
                    "    </navPoint>\n" +
                    "  </navMap>\n" +
                    "</ncx>\n";
        }
        zos.write(tocNcx.getBytes());
        zos.closeEntry();
        // OEBPS/
        zos.putNextEntry(new ZipEntry("OEBPS/"));
        // OEBPS/ main file
        for (int i = 0; i < inputs.length; i++) {
            zos.putNextEntry(new ZipEntry("OEBPS/" + inputs[i].getName()));
            {
                InputStream fis = new BufferedInputStream(new FileInputStream(inputs[i]));
                byte[] buf = new byte[4096];
                int len;
                while ((len = fis.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                fis.close();
            }
            zos.closeEntry();
        }
        // Additional resources
        for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
            zos.putNextEntry(new ZipEntry(resource.getKey()));
            zos.write(resource.getValue());
            zos.closeEntry();
        }
        // close zip
        zos.close();

        // Checking
        PrintWriter writer = new PrintWriter(System.err);
        EpubCheck checker = new EpubCheck(output, writer);
        checker.validate();
        writer.flush();
    }

}
