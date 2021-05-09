package com.intellij.ide.startup;

import lombok.Value;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Predicate;

public interface StartupActionScriptManager {
    interface ActionCommand {
    }

    @Value
    class CopyCommand implements Serializable, ActionCommand {
        @Serial
        private static final long serialVersionUID = 201708031943L;
        String mySource;
        String myDestination;
    }

    @Value
    class UnzipCommand implements Serializable, ActionCommand {
        @Serial
        private static final long serialVersionUID = 201708031943L;
        String mySource;
        String myDestination;
        Predicate<? super String> myFilenameFilter;
    }

    @Value
    class DeleteCommand implements Serializable, ActionCommand {
        @Serial
        private static final long serialVersionUID = 201708031943L;
        String mySource;
    }
}
