package dev.tomek.userinstaller.intellij.idea;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.CopyFiles;
import dev.tomek.userinstaller.action.ResolveVars;
import dev.tomek.userinstaller.intellij.IntellijInstaller;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
@Command(mixinStandardHelpOptions = true)
public class IdeaInstaller extends IntellijInstaller {
    @CommandLine.Option(names = {"-m", "--maven-home"}, description = "Maven home directory.", required = true)
    private String mavenHome;

    @CommandLine.Option(names = {"-j", "--jdk-home"}, description = "Main JDK home directory.", required = true)
    private String jdkHome;

    @CommandLine.Option(names = {"-j8", "--jdk8-home"}, description = "JDK 8 home directory.", required = true)
    private String jdk8Home;

    public IdeaInstaller() {
        super("idea");
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new IdeaInstaller()).execute(args));
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
            ))
        );
    }
}
