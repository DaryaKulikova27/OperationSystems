package ru.dasha;

import ru.dasha.ll.State;
import ru.dasha.ll.StateReader;
import ru.dasha.ll.Validator;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<State> states = StateReader.readStates("states.csv");

        Validator validator = new Validator(states);

        String line = "-8*--(---3+a*b*--8*(a+--3))";
        System.out.println(validator.validate(line));
    }

}