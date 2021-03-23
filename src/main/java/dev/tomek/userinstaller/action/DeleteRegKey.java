package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
        try {
            final Process process = Runtime.getRuntime().exec("reg delete %s /f".formatted(key));
            final boolean result = process.waitFor(1, TimeUnit.SECONDS);
            LOGGER.debug("Registry key {} deletion result: {}", key, result);
            process.destroy();
            return result;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Problem while deleting registry key ", e);
        }
        return false;
    }
}
