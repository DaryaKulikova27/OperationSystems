package ru.dasha.lexer.token;

public class Token {
    TokenType type;
    int line;
    int column;
    String text;

    public Token(TokenType type, int line, int column, String text) {
        this.type = type;
        this.line = line;
        this.column = column;
        this.text = text;
    }

    @Override
    public String toString() {
        return type.name() + " (" + line + ", " + column + ") \"" + text + "\"";
    }
}