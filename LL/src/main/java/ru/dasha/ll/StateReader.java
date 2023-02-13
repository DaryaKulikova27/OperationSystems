package ru.dasha.ll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StateReader {

    public static List<State> readStates(String filePath) {
        List<String[]> table = readCSV(filePath);

        List<State> states = new ArrayList<>(table.size());

        for (String[] row : table) {
            State state = new State(
                    row[0],
                    getSymbols(row[1]),
                    stringToBool(row[2]),
                    stringToBool(row[3]),
                    stringToInt(row[4]),
                    stringToBool(row[5]),
                    stringToBool(row[6])
            );

            states.add(state);
        }

        return states;
    }

    private static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                records.add(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    private static int stringToInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new RuntimeException("can not convert " + str + " to int");
        }
    }

    private static boolean stringToBool(String str) {
        return Boolean.parseBoolean(str);
    }

    private static String[] getSymbols(String str) {
        String argsString = str.substring(1, str.length() - 1);

        return argsString.split(", ");
    }

}
