package dev.tomek.userinstaller.intellij.phpstorm;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.intellij.IntellijInstaller;
import picocli.CommandLine;

import java.util.List;

@SuppressWarnings("unused")
@CommandLine.Command(mixinStandardHelpOptions = true)
public class PhpStormInstaller extends IntellijInstaller {

    public static void main(String[] args) {
        System.exit(new CommandLine(new PhpStormInstaller()).execute(args));
    }

    public PhpStormInstaller() {
        super("phpstorm");
    }

    @Override
    protected List<Action> getCustomActions() {
        return List.of();
    }
}
