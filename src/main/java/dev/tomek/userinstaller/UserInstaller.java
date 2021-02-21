package dev.tomek.userinstaller;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(mixinStandardHelpOptions = true)
public class UserInstaller implements Runnable {

    @Option(names = {"-d", "--home-dir"}, description = "User home directory", required = true)
    private String userHome;

    public static void main(String[] args) {
        System.exit(new CommandLine(new UserInstaller()).execute(args));
    }

    @Override
    public void run() {
        try {
            System.out.print("Deleting: " + userHome + " ");
            final Path homeDir = Paths.get(userHome);
            final boolean delResult = Files.deleteIfExists(homeDir);
            System.out.println(delResult);
        } catch (IOException e) {
            System.out.println("Can't delete: " + e.getMessage());
        }
    }
}
