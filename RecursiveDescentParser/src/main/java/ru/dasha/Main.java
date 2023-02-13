package ru.dasha;

import ru.dasha.parser.Parser;

import java.io.IOException;
import java.util.Arrays;

import static ru.dasha.parser.Lexer.lex;
import static ru.dasha.parser.Lexer.readFile;

public class Main {
    public static void main(String[] args) throws IOException {
        String[] tokens = lex(readFile("input.txt"));
        boolean success = new Parser(Arrays.asList(tokens)).parseProg();
        System.out.println("Parsing successful? " + success);
    }

}