import org.junit.jupiter.api.Test;
import ru.dasha.ll.State;
import ru.dasha.ll.StateReader;
import ru.dasha.ll.Validator;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationTest {
    private final List<State> stateList = StateReader.readStates("states2.csv");
    private final Validator validator = new Validator(stateList);

    public ValidationTest() throws IOException {
    }

    @Test
    void testValidLine() {
        String line = "-8*--(---3+a*b*--8*(a+--3))";
        System.out.println(line);
        assertTrue(validator.validate(line));
    }

    @Test
    void testLineWithMismatchedParentheses() {
        String line = "-8*-)(-3+a*b*-8*(a+-3))";
        System.out.println(line);
        assertFalse(validator.validate(line));
    }

    @Test
    void testLineWithExtraPlus() {
        String line = "-8*-(-3++a*b*-8*(a+++-3))";
        System.out.println(line);
        assertFalse(validator.validate(line));
    }

    @Test
    void testLineWithExtraAsterisks() {
        String line = "-8**-(-3+a***b****-8**(a+-3))";
        System.out.println(line);
        assertFalse(validator.validate(line));
    }

    @Test
    void testLineWithParenthesesAndMultiplication() {
        String line = "-8*-(-3+a*b*-8*(a+-3))";
        System.out.println(line);
        assertTrue(validator.validate(line));
    }

    @Test
    void testLineWithNestedParenthesesAndMultiplication() {
        String line = "-8*-(-3+(a*b*-8)*(a+-3))";
        System.out.println(line);
        assertTrue(validator.validate(line));
    }
}
