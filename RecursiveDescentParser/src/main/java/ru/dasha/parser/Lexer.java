package ru.dasha.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    public static String[] lex(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int state = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (state) {
                case 0:
                    if (isLetter(c)) {
                        currentToken.append(c);
                        state = 1;
                    } else if (isOperator(c)) {
                        tokens.add(Character.toString(c));
                    } else if (isWhitespace(c)) {
                    } else {
                        throw new RuntimeException("Invalid character: " + c);
                    }
                    break;
                case 1:
                    if (isLetterOrDigit(c)) {
                        currentToken.append(c);
                    } else {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                        state = 0;
                        i--;
                    }
                    break;
            }
        }
        if (state != 0) {
            tokens.add(currentToken.toString());
        }
        return tokens.toArray(new String[0]);
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isLetterOrDigit(char c) {
        return isLetter(c) || isDigit(c);
    }

    private static boolean isOperator(char c) {
        return c == ';' ||c == '=' || c == ':' || c == ',' || c == '(' || c == ')' || c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    public static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        br.close();
        return sb.toString();
    }
}
