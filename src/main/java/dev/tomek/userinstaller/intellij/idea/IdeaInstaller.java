package dev.tomek.userinstaller.intellij.idea;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.CopyFiles;
import dev.tomek.userinstaller.action.InstallPlugins;
import dev.tomek.userinstaller.action.ResolveVars;
import dev.tomek.userinstaller.intellij.IntellijInstaller;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
public class IdeaInstaller extends IntellijInstaller {
    private final String mavenHome;
    private final String jdkHome;
    private final String jdk8Home;

    public IdeaInstaller(String userHome, String settingsRepoUrl, String appsDir, String mavenHome, String jdkHome, String jdk8Home) {
        super("idea", userHome, settingsRepoUrl, appsDir);
        this.mavenHome = mavenHome;
        this.jdkHome = jdkHome;
        this.jdk8Home = jdk8Home;
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of(
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
            )),
            new InstallPlugins(homeDir.resolve(Paths.get("idea")), List.of(
                InstallPlugins.EXTRA_ICONS,
                InstallPlugins.TEST_ME
            ))
        );
    }
}
