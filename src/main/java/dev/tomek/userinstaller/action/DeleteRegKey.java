package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class DeleteRegKey implements Action {

    private final String key;

    @Override
    public String getName() {
        return "Removing registry key: " + key;
    }

    @Override
    public boolean perform() {
        try {
            final Process process = Runtime.getRuntime().exec("reg delete " + key);
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Problem while deleting registry key ", e);
        }
        return false;
    }
}
