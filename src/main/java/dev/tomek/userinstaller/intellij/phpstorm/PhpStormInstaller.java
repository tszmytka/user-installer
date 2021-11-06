package dev.tomek.userinstaller.intellij.phpstorm;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.intellij.IntellijInstaller;
import dev.tomek.userinstaller.metadata.ManifestReader;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("unused")
@CommandLine.Command(mixinStandardHelpOptions = true, versionProvider = ManifestReader.class)
public class PhpStormInstaller extends IntellijInstaller {

    public PhpStormInstaller() {
        super("phpstorm");
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new PhpStormInstaller()).execute(args));
    }

    @Override
    protected List<Action> buildCustomActions(Path homeDir, Path newInstallation) {
        return List.of();
    }
}
