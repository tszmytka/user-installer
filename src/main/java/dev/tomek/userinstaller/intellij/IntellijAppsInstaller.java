package dev.tomek.userinstaller.intellij;

import dev.tomek.userinstaller.AnsiConsole;
import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.DeleteDir;
import dev.tomek.userinstaller.intellij.clion.ClionInstaller;
import dev.tomek.userinstaller.intellij.idea.IdeaInstaller;
import dev.tomek.userinstaller.intellij.phpstorm.PhpStormInstaller;
import dev.tomek.userinstaller.intellij.rustrover.RustRoverInstaller;
import dev.tomek.userinstaller.metadata.ManifestReader;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.fusesource.jansi.AnsiConsole.systemInstall;
import static org.fusesource.jansi.AnsiConsole.systemUninstall;

@SuppressWarnings("unused")
@Slf4j
@CommandLine.Command(mixinStandardHelpOptions = true, versionProvider = ManifestReader.class)
public class IntellijAppsInstaller implements Runnable {
    @Option(names = {"-d", "--home-dir"}, description = "User home directory - where application config dir will be located.", required = true)
    private String userHome;
    @Option(names = {"-u", "--user"}, description = "User name. Used to locate additional settings needing cleaning up.", required = true)
    private String userName;
    @Option(names = {"-a", "--apps-dir"}, description = "Applications directory. Where the old and new Intellij installs reside.", required = true)
    private String appsDir;
    @Option(names = {"-si", "--settings-idea"}, description = "Url to Idea settings repository. This will be cloned and installed.", required = true)
    private String settingsIdea;
    @Option(names = {"-m", "--maven-home"}, description = "Maven home directory.", required = true)
    private String mavenHome;
    @Option(names = {"-j", "--jdk-home"}, description = "Main JDK home directory.", required = true)
    private String jdkHome;
    @Option(names = {"-j8", "--jdk8-home"}, description = "JDK 8 home directory.", required = true)
    private String jdk8Home;
    @Option(names = {"-sp", "--settings-phpstorm"}, description = "Url to Php Storm settings repository. This will be cloned and installed.")
    private String settingsPhpStorm;
    @Option(names = {"-sc", "--settings-clion"}, description = "Url to Clion settings repository. This will be cloned and installed.")
    private String settingsClion;
    @Option(names = {"-sr", "--settings-rust-rover"}, description = "Url to Rust Rover settings repository. This will be cloned and installed.")
    private String settingsRustRover;

    public static void main(String[] args) {
        systemInstall();
        System.exit(new CommandLine(new IntellijAppsInstaller()).execute(args));
        systemUninstall();
    }

    @Override
    public void run() {
        System.out.printf("User Installer for an Intellij Applications%n");
        final String job = "installing applications";
        LOGGER.info("Start " + job);
        final long t0 = System.nanoTime();
        
        Stream.of(
            Optional.of(new IdeaInstaller(userHome, settingsIdea, appsDir, mavenHome, jdkHome, jdk8Home)),
            Optional.ofNullable(settingsPhpStorm).map(s -> new PhpStormInstaller(userHome, s, appsDir)),
            Optional.ofNullable(settingsClion).map(s -> new ClionInstaller(userHome, s, appsDir)),
            Optional.ofNullable(settingsRustRover).map(s -> new RustRoverInstaller(userHome, s, appsDir))
        ).flatMap(Optional::stream).forEach(IntellijInstaller::run);

        if (AnsiConsole.should("Remove global settings (affects all installations)?")) {
            final Path jb1 = Paths.get("C:", "users", userName, "AppData", "Roaming", "JetBrains");
            final Path jb2 = Paths.get("C:", "users", userName, "AppData", "Local", "JetBrains");

            final List<Action> actions = List.of(
                new DeleteDir(jb1),
                new DeleteDir(jb2)
            );
            actions.forEach(a -> {
                System.out.print(a.getName() + " ... ");
                AnsiConsole.printResult(a.perform());
            });
        }

        System.out.println("Installation finished.");
        final LocalTime elapsed = LocalTime.ofNanoOfDay(System.nanoTime() - t0);
        System.out.println("Elapsed time: " + elapsed);
        LOGGER.info("Finished %s. Elapsed: %s".formatted(job, elapsed));
    }
}

