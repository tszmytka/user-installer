package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("reg delete %s /f".formatted(key));
            final boolean result = process.waitFor(1, TimeUnit.SECONDS);
            LOGGER.debug("Registry key {} deletion result: {}", key, result);
            return result;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Problem while deleting registry key {}", key, e);
        } finally {
            Optional.ofNullable(process).ifPresent(Process::destroy);
        }
        return false;
    }
}
