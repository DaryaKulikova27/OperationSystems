package ru.dasha;

import ru.dasha.lexer.Lexer;
import ru.dasha.lexer.token.Token;

import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer(new BufferedReader(new FileReader("pascal/test0.pas")));
        ArrayList<Token> lex = lexer.lex();

        PrintWriter writer = new PrintWriter("output.txt", "UTF-8");


        for (Token token : lex) {
            writer.println(token);
            System.out.println(token);

        }
        writer.close();
    }

}