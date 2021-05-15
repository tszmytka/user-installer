package dev.tomek.userinstaller.intellij;

import dev.tomek.userinstaller.AnsiConsole;
import dev.tomek.userinstaller.action.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalTime;
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

    @CommandLine.Option(names = {"-d", "--home-dir"}, description = "User home directory - where application config dir will be located.", required = true)
    private String userHome;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User name. Used to locate additional settings needing cleaning up.", required = true)
    private String userName;

    @CommandLine.Option(names = {"-a", "--apps-dir"}, description = "Applications directory. Where the old and new Intellij installs reside.", required = true)
    private String appsDir;

    @CommandLine.Option(names = {"-s", "--settings-repo"}, description = "Url to settings repository. This will be cloned and installed.", required = true)
    private String settingsRepoUrl;

    protected abstract List<Action> buildCustomActions(Path homeDir, Path newInstallation);


    @Override
    public void run() {
        System.out.printf("User Installer for an Intellij Application (%s)%n", applicationName);
        final String job = "installing %s".formatted(applicationName);
        LOGGER.info("Start " + job);
        final long t0 = System.nanoTime();
        final Path homeDir = Paths.get(userHome);
        final Path jb1 = Paths.get("C:", "users", userName, "AppData", "Roaming", "JetBrains");
        final Path jb2 = Paths.get("C:", "users", userName, "AppData", "Local", "JetBrains");

        final Optional<Path> foundInstallation = findNewInstallDir();
        if (foundInstallation.isEmpty()) {
            throw new IllegalStateException("Could not find new installation dir");
        }
        final Path newInstallation = foundInstallation.get();
        System.out.println("New installation: " + newInstallation);
        System.out.println("User home dir: " + homeDir);
        confirmInstallation();

        final Stream<Action> actions = Stream.concat(
            Stream.of(
                new DeleteDir(homeDir.resolve(Paths.get(applicationName))),
                new DeleteDir(jb1),
                new DeleteDir(jb2),
                new DeleteRegKey("HKCU\\Software\\JavaSoft\\Prefs\\jetbrains"),
                new InstallSettingsRepo(homeDir, applicationName, settingsRepoUrl),
                new SetVmOptions(homeDir, applicationName, newInstallation.resolve(Paths.get("bin", applicationName + "64.exe.vmoptions")))
            ),
            buildCustomActions(homeDir, newInstallation).stream()
        );
        actions.forEach(a -> {
            System.out.print(a.getName() + " ... ");
            AnsiConsole.printResult(a.perform());
        });
        System.out.println("Installation finished.");
        final LocalTime elapsed = LocalTime.ofNanoOfDay(System.nanoTime() - t0);
        System.out.println("Elapsed time: " + elapsed);
        LOGGER.info("Finished %s. Elapsed: %s".formatted(job, elapsed));
    }


    private void confirmInstallation() {
        try {
            System.out.println("Proceed with installation? (yes, no)");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            if ("yes".equals(reader.readLine())) {
                return;
            }
        } catch (IOException e) {
            LOGGER.error("Error while reading user input", e);
        }
        System.out.println("Installation interrupted. Exiting.");
        System.exit(0);
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
                }).min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        } catch (IOException e) {
            LOGGER.error("Cannot find install dirs", e);
        }
        return Optional.empty();
    }
}
