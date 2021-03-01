package dev.tomek.userinstaller.idea;

import dev.tomek.userinstaller.AnsiConsole;
import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.DeleteDir;
import dev.tomek.userinstaller.action.DeleteRegKey;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Command(mixinStandardHelpOptions = true)
public class IdeaInstaller implements Runnable {
    @CommandLine.Option(names = {"-d", "--home-dir"}, description = "User home directory", required = true)
    private String userHome;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User name", required = true)
    private String userName;

    public static void main(String[] args) {
        System.exit(new CommandLine(new IdeaInstaller()).execute(args));
    }

    @Override
    public void run() {
        final Path homeDir = Paths.get(userHome);
        final Path jb1 = Paths.get("C:", "users", userName, "AppData", "Roaming", "JetBrains");
        final Path jb2 = Paths.get("C:", "users", userName, "AppData", "Local", "JetBrains");
        final List<Action> actions = List.of(
            new DeleteDir(homeDir.resolve(Paths.get("idea"))),
            new DeleteDir(jb1),
            new DeleteDir(jb2),
            new DeleteRegKey("HKEY_CURRENT_USER\\Software\\JavaSoft\\Prefs\\jetbrains")
        );
        actions.forEach(a -> {
            System.out.print(a.getName() + " ... ");
            if (a.perform()) {
                AnsiConsole.printOk();
            }
        });
    }
}
