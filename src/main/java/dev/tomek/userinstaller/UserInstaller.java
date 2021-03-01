package dev.tomek.userinstaller;

import dev.tomek.userinstaller.action.Action;
import dev.tomek.userinstaller.action.DeleteDir;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Command(mixinStandardHelpOptions = true)
public class UserInstaller implements Runnable {

    @Option(names = {"-d", "--home-dir"}, description = "User home directory", required = true)
    private String userHome;

    @Option(names = {"-u", "--user"}, description = "User name", required = true)
    private String userName;

    public static void main(String[] args) {
        System.exit(new CommandLine(new UserInstaller()).execute(args));
    }

    @Override
    public void run() {
        final Path homeDir = Paths.get(userHome);
        final Path jb1 = Paths.get("C:", "users", userName, "AppData", "Roaming", "JetBrains");
        final Path jb2 = Paths.get("C:", "users", userName, "AppData", "Local", "JetBrains");
        final List<Action> actions = List.of(
            new DeleteDir(homeDir.resolve(Paths.get("idea"))),
            new DeleteDir(jb1),
            new DeleteDir(jb2)
        );
        actions.forEach(a -> {
            System.out.print(a.getName() + " ... ");
            if (a.perform()) {
                AnsiConsole.printOk();
            }
        });
    }

    static class AnsiConsole {
        private static final String ESC = "\033";
        private static final String OFF = sgr(0);

        public static void printOk() {
            System.out.printf("%sOK%s", sgr(32), OFF);
        }

        public static void printError() {
            System.out.printf("%sERROR%s", sgr(31), OFF);
        }

        private static String sgr(int... sgrParam) {
            return ESC + "[" + Arrays.stream(sgrParam).boxed().map(String::valueOf).collect(Collectors.joining(";")) + "m";
        }
    }
}
