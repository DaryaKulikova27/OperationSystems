package ru.dasha.ll;

import java.util.LinkedList;
import java.util.List;

public class Validator {
    private List<State> table;

    public Validator(List<State> table) {
        this.table = table;
    }

    public boolean validate(String line) {
        LinkedList<Integer> stack = new LinkedList<>();
        int index = 0;
        int ch = 0;

        while (true) {
            String symbol = getSymbol(line, ch);

            if (symbol.equals(".")) {
                return true;
            }

            State state = table.get(index);
            System.out.println(index + " " + state.getName() + " " + symbol + " " + state.getNextStateID());
            if (!hasSymbol(symbol, state)) {
                if (state.isError()) {
                    if (stack.size() > 0) {
                        int back = stack.removeLast();
                        index = back;
                        continue;
                    }

                    return false;
                } else {
                    index++;
                    continue;
                }
            }
            if (state.isPushToStack())
                stack.addLast(index + 1);
            if (state.isShift())
                symbol = getSymbol(line, ++ch);
            if (state.isEnd())
                break;
            if (state.getNextStateID() != -1)
                index = state.getNextStateID();
            else
                index = stack.removeLast();
        }

        return true;
    }

    private static String getSymbol(String line, int ch) {
        if (ch >= line.length()) {
            return ".";
        } else {
            return Character.toString(line.charAt(ch));
        }
    }

    private static boolean hasSymbol(String symbol, State state) {
        for (String ch : state.getGuideCharacters()) {
            if (ch.equals(symbol)) {
                return true;
            }
        }

        return false;
    }
}

