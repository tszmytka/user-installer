package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class InstallSettingsRepo implements Action {
    private final Path homeDir;
    private final String repoUrl;

    @Override
    public String getName() {
        return "Installing settings repository";
    }

    @Override
    public boolean perform() {
        final Path destination = homeDir.resolve(Paths.get("idea", "config", "settingsRepository", "repository"));
        Process process = null;
        try {
            Files.createDirectories(destination);
            process = Runtime.getRuntime().exec("git clone %s %s".formatted(repoUrl, destination));
            final CopyFiles copyConfig = new CopyFiles(
                homeDir.resolve(Paths.get("idea", "config", "settingsRepository", "repository", "external", "settingsRepository", "config.json")),
                homeDir.resolve(Paths.get("idea", "config", "settingsRepository", "config.json"))
            );
            return process.waitFor(1, TimeUnit.MINUTES) && copyConfig.perform();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Cannot install settings repository into {}", destination, e);
        } finally {
            Optional.ofNullable(process).ifPresent(Process::destroy);
        }
        return false;
    }
}
