package fr.gnodet.epubs.core;

import com.adobe.epubcheck.api.EpubCheck;

import java.io.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EPub {

    public static void createEpub(File input, File output, String title, String creator) throws Exception {
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
        zos.write(("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"2.0\" unique-identifier=\"uuid_id\">\n" +
                "  <metadata xmlns:opf=\"http://www.idpf.org/2007/opf\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "    <dc:title>" + title + "</dc:title>\n" +
                "    <dc:language>fr</dc:language>\n" +
                "    <dc:creator opf:file-as=\"" + creator + "\" opf:role=\"aut\">" + creator + "</dc:creator>\n" +
                "    <meta name=\"cover\" content=\"cover\"/>\n" +
                "    <dc:identifier id=\"uuid_id\" opf:scheme=\"uuid\">" + UUID.randomUUID() + "</dc:identifier>\n" +
                "  </metadata>\n" +
                "  <manifest>\n" +
                "    <item id=\"s01\" media-type=\"application/xhtml+xml\" href=\"OEBPS/" + input.getName() + "\" />\n" +
                "    <item href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\" id=\"ncx\"/>\n" +
                "  </manifest>\n" +
                "  <spine toc=\"ncx\"> \n" +
                "    <itemref idref=\"s01\"/>\n" +
                "  </spine>\n" +
                "</package>\n").getBytes());
        zos.closeEntry();
        // toc.ncx
        zos.putNextEntry(new ZipEntry("toc.ncx"));
        zos.write(("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\" xml:lang=\"eng\">\n" +
                "  <head>\n" +
                "    <meta content=\"1\" name=\"dtb:depth\"/>\n" +
                "  </head>\n" +
                "  <docTitle>\n" +
                "    <text>" + title + "</text>\n" +
                "  </docTitle>\n" +
                "  <navMap>\n" +
                "    <navPoint id=\"1\" playOrder=\"1\">\n" +
                "      <navLabel>\n" +
                "        <text>" + title + "</text>\n" +
                "      </navLabel>\n" +
                "      <content src=\"OEBPS/" + input.getName() + "\"/>\n" +
                "    </navPoint>\n" +
                "  </navMap>\n" +
                "</ncx>\n").getBytes());
        zos.closeEntry();
        // OEBPS/
        zos.putNextEntry(new ZipEntry("OEBPS/"));
        // OEBPS/ main file
        zos.putNextEntry(new ZipEntry("OEBPS/" + input.getName()));
        {
            InputStream fis = new BufferedInputStream(new FileInputStream(input));
            byte[] buf = new byte[4096];
            int len;
            while ((len = fis.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            fis.close();
        }
        zos.closeEntry();
        // close zip
        zos.close();

        // Checking
        PrintWriter writer = new PrintWriter(System.err);
        EpubCheck checker = new EpubCheck(output, writer);
        checker.validate();
        writer.flush();
    }

}
