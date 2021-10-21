package dev.tomek.userinstaller.intellij.clion;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.InstallPlugins;
import dev.tomek.userinstaller.intellij.IntellijInstaller;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Example usage:
 * <p>
 * <code>java -jar user-installer.jar -d D:/AppData -u ts32023 -a D:/Applications -s git@gitlab.com:tszmytka/rust-settings.git</code>
 */
@Slf4j
@Command(mixinStandardHelpOptions = true)
public class ClionInstaller extends IntellijInstaller {

    public ClionInstaller() {
        super("clion");
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        System.exit(new CommandLine(new ClionInstaller()).execute(args));
        AnsiConsole.systemUninstall();
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of(
            new InstallPlugins(homeDir.resolve(Paths.get("idea")), List.of(
                InstallPlugins.TOML,
                InstallPlugins.RUST,
                InstallPlugins.EXTRA_ICONS
            ))
        );
    }
}
