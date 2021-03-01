package dev.tomek.userinstaller;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AnsiConsole {
    private static final String ESC = "\033";
    private static final String OFF = sgr(0);

    public static void printOk() {
        System.out.printf("%sOK%s", sgr(32), OFF);
    }

    public static void printError() {
        System.out.printf("%sERROR%s", sgr(31), OFF);
    }

    private static String sgr(int... sgrParam) {
        return ESC + "[" + Arrays.stream(sgrParam).boxed().map(String::valueOf).collect(Collectors.joining(";")) + "m";
    }
}
