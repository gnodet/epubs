package fr.gnodet.epubs.fixer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestFixer {

    @Test
    public void testFootnotes() throws Exception {
        String fixed = new Main(new String[0]).fixFootNotes(
            "<footnote><p>Cf. <bible><ss>Ep</ss> <sc>2</sc>, <sv>21</sv>-<sv>22</sv></bible> ; <bible><ss>1P</ss> <sc>2</sc>, <sv>5</sv></bible>.</p></footnote>"
        );
        Assert.assertEquals(" (cf. <bible><ss>Ep</ss> <sc>2</sc>, <sv>21</sv>-<sv>22</sv></bible> ; <bible><ss>1P</ss> <sc>2</sc>, <sv>5</sv></bible>)", fixed);
    }

    @Test
    public void test() throws Exception {
        String file = Paths.get(getClass().getClassLoader().getResource("test1.html").toURI()).toString();
        String doc = new Main(new String[0]).process(file);
        write("test1.html", doc);
    }

    @Test
    public void test2() throws Exception {
        // ./fillion/marc/target/docbkx/epub3/pr01s05.xhtml
        String file = Paths.get(getClass().getClassLoader().getResource("pr01s05.xhtml").toURI()).toString();
        String doc = new Main(new String[0]).process(file);
        write("pr01s05.xhtml", doc);
    }

    @Test
    public void test3() throws Exception {
        // http://catho.org/9.php?d=cam
        String file = Paths.get(getClass().getClassLoader().getResource("9.php?d=cam").toURI()).toString();
        String doc = new Main(new String[0]).process(file);
        write("9_d_cam.html", doc);
    }

    private void write(String file, String doc) throws IOException {
        Path p = Paths.get("target/fixer/", file);
        Files.createDirectories(p.toAbsolutePath().getParent());
        Files.write(p, doc.getBytes());
//        System.out.println(doc);
    }

}
