package dev.tomek.userinstaller.intellij.phpstorm;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.intellij.IntellijInstaller;

import java.nio.file.Path;
import java.util.List;

public class PhpStormInstaller extends IntellijInstaller {
    public PhpStormInstaller(String userHome, String settingsRepoUrl, String appsDir) {
        super("phpstorm", userHome, settingsRepoUrl, appsDir);
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of();
    }
}
