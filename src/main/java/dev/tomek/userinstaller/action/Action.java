package dev.tomek.userinstaller.action;

public interface Action {

    String getName();

    Result perform();

    enum Result {
        OK,
        ERROR,
        SKIPPED;
    }
}
