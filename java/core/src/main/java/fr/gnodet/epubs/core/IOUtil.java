package fr.gnodet.epubs.core;

import org.w3c.tidy.Tidy;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class IOUtil {

    public static String loadTextContent(URL url, String cache) throws IOException {
        File file = new File(cache);
        file.getParentFile().mkdirs();

        if (!file.exists()) {
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            if (encoding == null) {
                String type = connection.getContentType();
                int idx = type.indexOf("charset=");
                if (idx > 0) {
                    encoding = type.substring(idx + "charset=".length());
                }
            }
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }
            Reader reader;
            {
                BufferedInputStream bufIn = new BufferedInputStream(is);
                bufIn.mark(3);
                boolean utf8 = bufIn.read() == 0xEF && bufIn.read() == 0xBB && bufIn.read() == 0xBF;
                if (utf8) {
                    reader = new BufferedReader(new InputStreamReader(bufIn, "UTF-8"));
                } else {
                    bufIn.reset();
                    reader = new BufferedReader(new InputStreamReader(bufIn, encoding));
                }
            }
            Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
            copy(reader, writer);
        }
        Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8");
        Writer writer = new StringWriter();
        copy(reader, writer);
        return writer.toString();
    }

    public static void writeToFile(String document, String file) throws IOException {
        document = document.replaceAll("(</tr>|</head>|</p>|</div>|<br />)\\s*", "$1\n");
        Reader reader = new StringReader(document);
        new File(file).getParentFile().mkdirs();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        copy(reader, writer);
    }

    public static void copy(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[4096];
        int len;
        while ((len = reader.read(buf)) > 0) {
            writer.write(buf, 0, len);
        }
        reader.close();
        writer.close();
    }

}
