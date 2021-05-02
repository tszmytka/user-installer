package dev.tomek.userinstaller.idea;

import dev.tomek.userinstaller.AnsiConsole;
import dev.tomek.userinstaller.action.*;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

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
import java.util.Map;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
@Slf4j
@Command(mixinStandardHelpOptions = true)
public class IdeaInstaller implements Runnable {
    @CommandLine.Option(names = {"-d", "--home-dir"}, description = "User home directory - where idea's config dir will be located.", required = true)
    private String userHome;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User name. Used to locate additional settings needing cleaning up.", required = true)
    private String userName;

    @CommandLine.Option(names = {"-a", "--apps-dir"}, description = "Applications directory. Where the old and new Intellij installs reside.", required = true)
    private String appsDir;

    @CommandLine.Option(names = {"-s", "--settings-repo"}, description = "Url to settings repository. This will be cloned and installed.", required = true)
    private String settingsRepoUrl;

    @CommandLine.Option(names = {"-m", "--maven-home"}, description = "Maven home directory.", required = true)
    private String mavenHome;

    @CommandLine.Option(names = {"-j", "--jdk-home"}, description = "Main JDK home directory.", required = true)
    private String jdkHome;

    @CommandLine.Option(names = {"-j8", "--jdk8-home"}, description = "JDK 8 home directory.", required = true)
    private String jdk8Home;

    public static void main(String[] args) {
        System.exit(new CommandLine(new IdeaInstaller()).execute(args));
    }

    @Override
    public void run() {
        System.out.println("User Installer for Intellij Idea");
        final long t0 = System.nanoTime();
        final Path homeDir = Paths.get(userHome);
        final Path jb1 = Paths.get("C:", "users", userName, "AppData", "Roaming", "JetBrains");
        final Path jb2 = Paths.get("C:", "users", userName, "AppData", "Local", "JetBrains");

        final List<Path> installDirs = findInstallDirs();
        if (installDirs.size() != 2) {
            throw new IllegalStateException("Unexpected amount of installation dirs (expected 2): " + installDirs);
        }
        final Path oldInstallation = installDirs.get(0);
        final Path newInstallation = installDirs.get(1);
        System.out.println("Old Intellij installation: " + oldInstallation);
        System.out.println("New Intellij installation: " + newInstallation);
        System.out.println("User home dir: " + homeDir);
        confirmInstallation();

        final List<Action> actions = List.of(
            new DeleteDir(homeDir.resolve(Paths.get("idea"))),
            new DeleteDir(jb1),
            new DeleteDir(jb2),
            new DeleteRegKey("HKCU\\Software\\JavaSoft\\Prefs\\jetbrains"),
            new CopyOptions(
                oldInstallation.resolve(Paths.get("bin", "idea64.exe.vmoptions")),
                newInstallation.resolve(Paths.get("bin", "idea64.exe.vmoptions")),
                List.of("user.home", "idea.config.path", "idea.system.path", "idea.plugins.path", "idea.log.path")
            ),

            new InstallSettingsRepo(homeDir, settingsRepoUrl),
            new CopyFiles(
                homeDir.resolve(Paths.get("idea", "config", "settingsRepository", "repository", "external", "templates")), homeDir.resolve(Paths.get("idea"))
            ),
            new ResolveVars(homeDir.resolve(Paths.get("idea", "config", "options")), Map.of(
                "$GRADLE_CACHES", homeDir.resolve(Paths.get(".gradle", "caches")).toString(),
                "$KOTLIN_BUNDLED", newInstallation.resolve(Paths.get("plugins", "Kotlin", "kotlinc")).toString(),
                "$MAVEN_REPOSITORY", homeDir.resolve(Paths.get(".m2", "repository")).toString(),
                "$mavenHome", mavenHome,
                "$homePathJdk8", jdk8Home,
                "$homePathJdk15", jdkHome
            ))
        );
        actions.forEach(a -> {
            System.out.print(a.getName() + " ... ");
            AnsiConsole.printResult(a.perform());
        });
        System.out.println("Installation finished.");
        System.out.println("Elapsed time: " + LocalTime.ofNanoOfDay(System.nanoTime() - t0));
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

    private List<Path> findInstallDirs() {
        try {
            return Files.find(Paths.get(appsDir), 1, (p, a) -> a.isDirectory() && p.getFileName().toString().startsWith("ideaIU"))
                .map(p -> {
                    FileTime fileTime;
                    try {
                        fileTime = Files.readAttributes(p, BasicFileAttributes.class).lastModifiedTime();
                    } catch (IOException e) {
                        fileTime = FileTime.from(Instant.now());
                    }
                    return Map.entry(p, fileTime);
                })
                .sorted(Map.Entry.comparingByValue())
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(toList());
        } catch (IOException e) {
            LOGGER.error("Cannot find install dirs", e);
        }
        return List.of();
    }
}
