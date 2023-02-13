package ru.dasha.parser;

import java.util.Arrays;
import java.util.List;

public class Parser {
    private List<String> tokens;
    private int pos;

    public Parser(List<String> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    private boolean match(String expected) {
        l("    match", expected, tokens.get(pos), pos < tokens.size() && tokens.get(pos).equalsIgnoreCase(expected) ? "ok" : "err");
        if (pos < tokens.size() && tokens.get(pos).equalsIgnoreCase(expected)) {
            pos++;
            return true;
        }
        return false;
    }

    private void l(String... str) {
//        System.out.println(Arrays.toString(str));
    }

    public boolean parseProg() {
        l("parseProg");
        if (match("PROG") && match("id") && parseVar() && match("begin") && parseListSt() && match("end")) {
            return true;
        }
        return false;
    }

    private boolean parseVar() {
        l("parseVar");
        int prevPos = pos;
        if (match("VAR") && parseIdList() && match(":") && parseType())
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseIdList() {
        l("parseIdList");
        int prevPos = pos;
        if (match("id") && parseIdListA())
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseIdListA() {
        l("parseIdListA");
        int prevPos = pos;
        if (match(",")) {
            if (match("id") && parseIdListA())
                return true;
            pos = prevPos;
            return false;
        }
        return true;
    }

    private boolean parseListSt() {
        l("parseListSt");
        int prevPos = pos;
        if (parseSt() && parseListStA())
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseListStA() {
        l("parseListStA");
        int prevPos = pos;
        if (parseSt() && parseListStA())
            return true;
        pos = prevPos;
        return true;
    }

    private boolean parseType() {
        l("parseType");
        int prevPos = pos;
        if (match("int") || match("float") || match("bool") || match("string"))
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseSt() {
        l("parseSt");
        int prevPos = pos;
        if (parseRead() || parseWrite() || parseAssign())
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseRead() {
        l("parseRead");
        int prevPos = pos;
        if (match("READ") && match("(") && parseIdList() && match(")") && match(";"))
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseWrite() {
        l("parseWrite");
        int prevPos = pos;
        if (match("WRITE") && match("(") && parseIdList() && match(")") && match(";"))
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseAssign() {
        l("parseAssign");
        int prevPos = pos;
        if (match("id") && match(":") && match("=") && parseExp() && match(";"))
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseExp() {
        l("parseExp");
        int prevPos = pos;
        if (parseT() && parseExpA())
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseExpA() {
        l("parseExpA");
        int prevPos = pos;
        if (match("+") && parseT() && parseExpA())
            return true;
        pos = prevPos;
        return true;
    }

    private boolean parseT() {
        l("parseT");
        int prevPos = pos;
        if (parseF() && parseTA())
            return true;
        pos = prevPos;
        return false;
    }

    private boolean parseTA() {
        l("parseTA");
        int prevPos = pos;
        if (match("*") && parseF() && parseTA())
            return true;
        pos = prevPos;
        return true;
    }

    private boolean parseF() {
        l("parseF");
        int prevPos = pos;
        if (match("-")) {
            if (parseF())
                return true;
            pos = prevPos;
            return false;
        }

        if (match("id") || match("num") ||
                match("(") && parseExp() && match(")"))
            return true;
        pos = prevPos;
        return false;
    }
}

