package dev.tomek.userinstaller.intellij.rustrover;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.CopyFiles;
import dev.tomek.userinstaller.action.InstallPlugins;
import dev.tomek.userinstaller.action.ResolveVars;
import dev.tomek.userinstaller.intellij.IntellijInstaller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static dev.tomek.userinstaller.action.InstallPlugins.*;

public class RustRoverInstaller extends IntellijInstaller {
    private static final String RUST_ROVER = "rustrover";
    private static final Path RUST_ROVER_PATH = Paths.get(RUST_ROVER);

    public RustRoverInstaller(String userHome, String settingsRepoUrl, String appsDir) {
        super(RUST_ROVER, userHome, settingsRepoUrl, appsDir);
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of(
            new CopyFiles(
                homeDir.resolve(Paths.get(RUST_ROVER, "config", "settingsRepository", "repository", "external", "templates")), homeDir.resolve(RUST_ROVER_PATH)
            ),
            new ResolveVars(homeDir.resolve(Paths.get(RUST_ROVER, "config", "settingsRepository", "repository")), Map.of(
                "$applicationConfig", homeDir.resolve(Paths.get(RUST_ROVER, "config")).toString()
            )),
            new InstallPlugins(homeDir.resolve(RUST_ROVER_PATH), List.of(
                EXTRA_ICONS,
                EDITOR_CONFIG,
                SETTINGS_REPOSITORY
            ))
        );
    }
}
