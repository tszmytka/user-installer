package dev.tomek.userinstaller.intellij.phpstorm;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.ResolveVars;
import dev.tomek.userinstaller.intellij.IntellijInstaller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PhpStormInstaller extends IntellijInstaller {
    private static final String PHPSTORM = "phpstorm";

    public PhpStormInstaller(String userHome, String settingsRepoUrl, String appsDir) {
        super(PHPSTORM, userHome, settingsRepoUrl, appsDir);
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of(
            new ResolveVars(homeDir.resolve(Paths.get(PHPSTORM, "config", "settingsRepository", "repository")), Map.of(
                "$applicationConfig", homeDir.resolve(Paths.get(PHPSTORM, "config")).toString()
            ))
        );
    }
}
