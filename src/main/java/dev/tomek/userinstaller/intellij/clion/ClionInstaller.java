package dev.tomek.userinstaller.intellij.clion;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.CopyFiles;
import dev.tomek.userinstaller.action.InstallPlugins;
import dev.tomek.userinstaller.intellij.IntellijInstaller;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static dev.tomek.userinstaller.action.InstallPlugins.*;

/**
 * Example usage:
 * <p>
 * <code>java -jar user-installer.jar -d D:/AppData -u ts32023 -a D:/Applications -s git@gitlab.com:tszmytka/rust-settings.git</code>
 */
@Slf4j
public class ClionInstaller extends IntellijInstaller {
    private static final String CLION = "clion";

    public ClionInstaller(String userHome, String settingsRepoUrl, String appsDir) {
        super(CLION, userHome, settingsRepoUrl, appsDir);
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of(
            new CopyFiles(
                homeDir.resolve(Paths.get("idea", "config", "settingsRepository", "repository", "external", "templates")), homeDir.resolve(Paths.get(CLION))
            ),
            new InstallPlugins(homeDir.resolve(Paths.get(CLION)), List.of(
                TOML,
                RUST,
                EXTRA_ICONS,
                EDITOR_CONFIG
            ))
        );
    }
}
