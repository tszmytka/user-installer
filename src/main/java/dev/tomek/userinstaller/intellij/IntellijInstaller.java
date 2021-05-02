package dev.tomek.userinstaller.intellij;

import dev.tomek.userinstaller.AnsiConsole;
import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.DeleteDir;
import dev.tomek.userinstaller.action.DeleteRegKey;
import dev.tomek.userinstaller.action.InstallSettingsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
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

    protected abstract List<Action> getCustomActions();

    @Override
    public void run() {
        System.out.printf("User Installer for an Intellij Application (%s)%n", applicationName);
        final long t0 = System.nanoTime();
        final Path homeDir = Paths.get(userHome);
        final Path jb1 = Paths.get("C:", "users", userName, "AppData", "Roaming", "JetBrains");
        final Path jb2 = Paths.get("C:", "users", userName, "AppData", "Local", "JetBrains");
        confirmInstallation();

        final Stream<Action> actions = Stream.concat(
            Stream.of(
                new DeleteDir(homeDir.resolve(Paths.get(applicationName))),
                new DeleteDir(jb1),
                new DeleteDir(jb2),
                new DeleteRegKey("HKCU\\Software\\JavaSoft\\Prefs\\jetbrains"),
                new InstallSettingsRepo(homeDir, applicationName, settingsRepoUrl)
            ),
            getCustomActions().stream()
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
}
