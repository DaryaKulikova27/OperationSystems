package ru.dasha;

import ru.dasha.lexer.Lexer;
import ru.dasha.lexer.token.Token;

import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer(new BufferedReader(new FileReader("pascal/test0.pas")));
        ArrayList<Token> lex = lexer.lex();

        for (Token token : lex)
            System.out.println(token);
    }

}