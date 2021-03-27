package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class CopyFiles implements Action {
    private final Path from;
    private final Path to;

    @Override
    public String getName() {
        return "Copying files";
    }

    @Override
    public boolean perform() {
        try {
            Files.copy(from, to);
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot copy", e);
        }
        return false;
    }
}
