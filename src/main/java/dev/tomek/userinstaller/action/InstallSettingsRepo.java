package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class InstallSettingsRepo implements Action {
    private final Path homeDir;
    private final String applicationName;
    private final String repoUrl;

    @Override
    public String getName() {
        return "Installing settings repository";
    }

    @Override
    public Result perform() {
        final Path destination = homeDir.resolve(Paths.get(applicationName, "config", "settingsRepository", "repository"));
        Process process = null;
        try {
            Files.createDirectories(destination);
            if (!Files.isDirectory(destination)) {
                LOGGER.error("Destination is not a directory");
                return Result.ERROR;
            }
            if (Files.walk(destination).anyMatch(path -> path != destination)) {
                LOGGER.info("Skipping installing settings repository. Destination directory is not empty.");
                return Result.SKIPPED;
            }
            process = Runtime.getRuntime().exec("git clone %s %s".formatted(repoUrl, destination));
            if (process.waitFor(1, TimeUnit.MINUTES)) {
                final int exit = process.exitValue();
                if (exit == 0) {
                    return Result.OK;
                } else {
                    LOGGER.warn("Git clone failed with exit value: {} and error: {}", exit, new String(process.getErrorStream().readAllBytes(), UTF_8));
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Cannot install settings repository into {}", destination, e);
        } finally {
            Optional.ofNullable(process).ifPresent(Process::destroy);
        }
        return Result.ERROR;
    }
}
