package dev.tomek.userinstaller;

import dev.tomek.userinstaller.action.Action.Result;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class AnsiConsole {
    private static final String ESC = "\033";
    private static final String OFF = sgr(0);

    public static void printResult(Result result) {
        switch (result) {
            case OK -> printOk();
            case ERROR -> printError();
            case SKIPPED -> printSkipped();
        }
    }

    public static void printOk() {
        System.out.printf("%sOK%s\n", sgr(32), OFF);
    }

    public static void printError() {
        System.out.printf("%sERROR%s\n", sgr(31), OFF);
    }

    public static void printSkipped() {
        System.out.printf("%sSKIPPED%s\n", sgr(33), OFF);
    }

    public static boolean should(String question) {
        try {
            System.out.println(question + " (yes, no)");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            if ("yes".equals(reader.readLine())) {
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Error while reading user input", e);
        }
        return false;
    }

    private static String sgr(int... sgrParam) {
        return ESC + "[" + Arrays.stream(sgrParam).boxed().map(String::valueOf).collect(Collectors.joining(";")) + "m";
    }
}
