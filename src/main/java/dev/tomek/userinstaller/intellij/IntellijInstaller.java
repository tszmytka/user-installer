package dev.tomek.userinstaller.intellij;

import dev.tomek.userinstaller.AnsiConsole;
import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.DeleteDir;
import dev.tomek.userinstaller.action.InstallSettingsRepo;
import dev.tomek.userinstaller.action.SetVmOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public abstract class IntellijInstaller implements Runnable {
    private final String applicationName;
    private final String userHome;
    private final String settingsRepoUrl;
    private final String appsDir;

    protected abstract List<Action> buildCustomActions(Path homeDir, Path newInstallation);

    @Override
    public void run() {
        final Path homeDir = Paths.get(userHome);

        final Optional<Path> foundInstallation = findNewInstallDir();
        if (foundInstallation.isEmpty()) {
            throw new IllegalStateException("Could not find new installation dir");
        }
        final Path newInstallation = foundInstallation.get();
        System.out.println("New installation: " + newInstallation);
        System.out.println("User home dir: " + homeDir);
        if (AnsiConsole.should("Proceed with installation?")) {
            final Stream<Action> actions = Stream.concat(
                Stream.of(
                    new DeleteDir(homeDir.resolve(Paths.get(applicationName))),
                    new InstallSettingsRepo(homeDir, applicationName, settingsRepoUrl),
                    new SetVmOptions(homeDir, applicationName, newInstallation.resolve(Paths.get("bin", applicationName + "64.exe.vmoptions")))
                ),
                buildCustomActions(homeDir, newInstallation).stream()
            );
            actions.forEach(a -> {
                System.out.print(a.getName() + " ... ");
                AnsiConsole.printResult(a.perform());
            });
        } else {
            System.out.println("Installation interrupted.");
        }
        System.out.println();
    }

    private Optional<Path> findNewInstallDir() {
        try {
            return Files.find(Paths.get(appsDir), 1, (p, a) -> a.isDirectory() && p.getFileName().toString().toLowerCase(Locale.ROOT).startsWith(applicationName))
                .map(p -> {
                    FileTime fileTime;
                    try {
                        fileTime = Files.readAttributes(p, BasicFileAttributes.class).lastModifiedTime();
                    } catch (IOException e) {
                        fileTime = FileTime.from(Instant.now());
                    }
                    return Map.entry(p, fileTime);
                }).max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        } catch (IOException e) {
            LOGGER.error("Cannot find install dirs", e);
        }
        return Optional.empty();
    }
}
