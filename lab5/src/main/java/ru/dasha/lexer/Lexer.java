package ru.dasha.lexer;

import ru.dasha.lexer.token.Token;
import ru.dasha.lexer.token.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;
import static ru.dasha.lexer.token.TokenType.*;

public class Lexer {
    private final static Map<Character, TokenType> MONO_TERMINALS_MAP = Map.ofEntries(
            entry('*', MULTIPLICATION),
            entry('+', PLUS),
            entry('-', MINUS),
            entry('/', DIVIDE),
            entry(';', SEMICOLON),
            entry(',', COMMA),
            entry('(', LEFT_PAREN),
            entry(')', RIGHT_PAREN),
            entry('[', LEFT_BRACKET),
            entry(']', RIGHT_PAREN),
            entry('=', ASSIGN),
            entry('.', DOT)
    );
    private final static Map<String, TokenType> KEYWORDS_LEXEMES_MAP = Map.ofEntries(
            entry("ARRAY", ARRAY),
            entry("BEGIN", BEGIN),
            entry("ELSE", ELSE),
            entry("END", END),
            entry("IF", IF),
            entry("OF", OF),
            entry("OR", OR),
            entry("PROGRAM", PROGRAM),
            entry("PROCEDURE", PROCEDURE),
            entry("THEN", THEN),
            entry("TYPE", TYPE),
            entry("VAR", VAR)
    );
    StringBuffer currentBuffer = new StringBuffer();
    BufferedReader reader;
    State state = State.INIT;
    char current;
    int currLine = 1;
    int currColumn = 1;
    int lexLine = currLine;
    int lexColumn = currColumn;
    boolean skipReading = false;
    boolean afterSpace = false;
    ArrayList<Token> tokens = new ArrayList<>();

    public Lexer(BufferedReader reader) {
        this.reader = reader;
    }

    public ArrayList<Token> lex() throws IOException {
        while (state != State.END) {
            if (skipReading)
                skipReading = false;
            else
                readNext();
            l("State", state, "current", current);
            switch (state) {
                case INIT: setState(inStateInit()); break;
                case PRE_DIV: setState(inStatePreDiv()); break;
                case LINE_COMMENT:setState(inStateLineComment()); break;
                case BLOCK_COMMENT: setState(inStateBlockComment()); break;
                case STRING: setState(inStateString()); break;
                case INTEGER: setState(inStateInteger());  break;
                case FLOAT: setState(inStateFloat()); break;
                case COLON: setState(inStateColon()); break;
                case GT: setState(inStateGT()); break;
                case LT: setState(inStateLT()); break;
                case IDENTIFIER: setState(inStateIdentifier()); break;
                case END:
                    break;
            }
            if (state == State.DEBUG__BREAK) {
                l("State", state, "current", current);
                break;
            }
        }
        return tokens;
    }

    private State inStateFloat() {
        if (isDigit())
            return State.FLOAT;
        dropCurrent();
        makeToken(FLOAT);
        return State.INIT;
    }

    private State inStateInteger() {
        if (isDigit())
            return State.INTEGER;
        if (current == '.')
            return State.FLOAT;
        else {
            dropCurrent();
            makeToken(INTEGER);
        }
        return State.INIT;
    }

    private State inStateIdentifier() {
        if (isDigit() || isAlpha() || current == '_')
            return State.IDENTIFIER;

        dropCurrent();
        makeIdentifierOrKeyword();
        return State.INIT;
    }

    private void makeIdentifierOrKeyword() {
        String key = currentBuffer.toString().toUpperCase();
        makeToken(KEYWORDS_LEXEMES_MAP.getOrDefault(key, IDENTIFIER));
    }

    private State inStateLT() {
        if (current != '=')
        {
            dropCurrent();
            makeToken(LESS);
        }
        else
            makeToken(LESS_EQ);
        return State.INIT;
    }

    private State inStateGT() {
        if (current != '=')
        {
            dropCurrent();
            makeToken(GREATER);
        }
        else
            makeToken(GREATER_EQ);
        return State.INIT;
    }

    private State inStateColon() {
        if (current != '=')
        {
            dropCurrent();
            makeToken(COLON);
        }
        else
            makeToken(ASSIGN);
        return State.INIT;
    }

    private State inStateString() {
        if (current != '\'')
            return State.STRING;
        makeToken(STRING);
        return State.INIT;
    }

    private State inStateBlockComment() {
        if (current != '}')
            return State.BLOCK_COMMENT;
        makeToken(BLOCK_COMMENT);
        return State.INIT;
    }

    private State inStateLineComment() {
        if (current != '\n')
            return State.LINE_COMMENT;
        makeToken(LINE_COMMENT);
        return State.INIT;
    }

    private State inStatePreDiv() {
        if (current == '/')
            return State.LINE_COMMENT;

        dropCurrent();
        makeToken(DIVIDE);
        return State.INIT;
    }

    private State inStateInit() throws IOException {
        if (isSpace()) {
            l("space", currentBuffer, current);
            dropBuffer();
            afterSpace = true;
        }
        else if (MONO_TERMINALS_MAP.containsKey(current))
            makeToken(MONO_TERMINALS_MAP.get(current));
        else if (isAlpha() || current == '_')
            return State.IDENTIFIER;
        else if (isDigit())
            return State.INTEGER;
        else
            switch (current) {
                case '>': return State.GT;
                case '<': return State.LT;
                case '/': return State.PRE_DIV;
                case '{': return State.BLOCK_COMMENT;
                case '\'': return State.STRING;
                case ':': return State.COLON;
                default: makeToken(BAD);
            }
        return State.INIT;
    }

    boolean isSpace() {
        return Character.isWhitespace(current);
    }

    boolean isDigit() {
        return Character.isDigit(current);
    }

    boolean isAlpha() {
        return Character.isAlphabetic(current);
    }

    void readNext() throws IOException {
        currColumn++;
        if (current == '\n') {
            currLine++;
            currColumn = 1;
        }
        int read = reader.read();
        if (read == -1) {
            switch (state)
            {
                case INTEGER: makeToken(INTEGER); break;
                case FLOAT: makeToken(FLOAT); break;
                case IDENTIFIER: makeIdentifierOrKeyword(); break;
                default: if (currentBuffer.length() != 0) makeToken(BAD);
            }
            setState(State.END);
        }
        current = (char) read;
        if (afterSpace) {
            afterSpace = false;
            lexLine = currLine;
            lexColumn = currColumn;
        }
        currentBuffer.append(current);
    }

    void makeToken(TokenType type) {
        l("makeToken ", type, "drop", skipReading, "buf", currentBuffer);
        tokens.add(l(new Token(type, lexLine, lexColumn, currentBuffer.toString())));
        dropBuffer();
        if (skipReading)
            currentBuffer.append(current);
    }

    private void dropCurrent() {
        currentBuffer.deleteCharAt(currentBuffer.length() - 1);
        skipReading = true;
    }

    void dropBuffer() {
        currentBuffer.setLength(0);
    }

    void setState(State state) {
        this.state = state;
        if (skipReading)
        {
            lexLine = currLine;
            lexColumn = currColumn;
        }
    }

    private <T> T l(T... t) {
//        System.out.println(Arrays.toString(t));
        return t[0];
    }

    enum State {
        INIT,
        PRE_DIV,
        LINE_COMMENT,
        BLOCK_COMMENT,
        STRING,
        INTEGER,
        FLOAT,
        COLON,
        GT,
        LT,
        IDENTIFIER,
        DEBUG__BREAK, END
    }
}