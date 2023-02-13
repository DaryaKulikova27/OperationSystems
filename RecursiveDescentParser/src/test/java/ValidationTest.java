import org.junit.jupiter.api.Test;
import ru.dasha.parser.Parser;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.dasha.parser.Lexer.lex;

public class ValidationTest {

    public ValidationTest() throws IOException {
    }

    public boolean checkStringValidity(String text) {
        String[] tokens = lex(text);
        return new Parser(Arrays.asList(tokens)).parseProg();
    }

    private void l(String... str) {
//        System.out.println(Arrays.toString(str));
    }
    @Test
    public void testSingleLine() {
        String s = "prog id var id : int begin read(id, id); id := id + ((--num)); end";
        l(s);
        assertTrue(checkStringValidity(s));
    }

    @Test
    public void testSingleLineCaseInsensitive() {
        String s = "PROG id vAr id : int bEgIn reAD(Id, Id); ID := id + ((--nUm)); eNd";
        l(s);
        assertTrue(checkStringValidity(s));
    }

    @Test
    public void testMultiLineCaseInsensitive() {
        String s = "PROG id\n" +
                "vAr\n" +
                "  id: int\n" +
                "bEgIn\n" +
                "  reAD(  Id,   Id  );reAD(Id);\n" +
                "\n" +
                "  write(  Id,   Id  );\n" +
                "  write(Id);\n" +
                "\n" +
                "  ID :=  id    +   (  (  -  -  nUm  )  );\n" +
                "  ID :=  id    *   (  (  -  -  nUm  )  );\n" +
                "\n" +
                "  write(  Id,   Id  );\n" +
                "\n" +
                "eNd\n";
        l(s);
        assertTrue(checkStringValidity(s));
    }

    @Test
    public void testMultiplicationOperator() {
        String s = "prog id\n" +
                "var\n" +
                "  id: int\n" +
                "begin\n" +
                "	id := id * id;\n" +
                "	id := id * (-id);\n" +
                "	id := id*(-id);\n" +
                "    id := id*id;\n" +
                "end\n";
        l(s);
        assertTrue(checkStringValidity(s));
    }
}
