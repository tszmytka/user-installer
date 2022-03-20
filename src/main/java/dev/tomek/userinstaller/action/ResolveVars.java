package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class ResolveVars implements Action {
    private static final String[] IGNORED_PATHS = new String[]{".git", "external"};
    private static final String[] ALLOWED_EXT = new String[]{".xml", ".json"};

    private final Path baseDir;

    private final Map<String, String> var2val;

    @Override
    public String getName() {
        return "Resolving template variables";
    }

    @Override
    public Result perform() {
        try {
            Files.walk(baseDir)
                .filter(Files::isRegularFile)
                .filter(path -> Arrays.stream(IGNORED_PATHS).noneMatch(p -> path.toString().contains(p)))
                .filter(path -> Arrays.stream(ALLOWED_EXT).anyMatch(ext -> path.getFileName().toString().endsWith(ext)))
                .forEach(this::resolveVars);
            return Result.OK;
        } catch (IOException e) {
            LOGGER.error("Cannot resolve variables in {}", baseDir, e);
        }
        return Result.ERROR;
    }

    private void resolveVars(Path file) {
        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile(getClass().getSimpleName(), "tmp");
            try (
                final BufferedReader source = Files.newBufferedReader(file);
                final BufferedWriter destination = Files.newBufferedWriter(tmpFile)
            ) {
                final AtomicBoolean newLine = new AtomicBoolean(false);
                source.lines().map(this::replaceAll).forEach(line -> writeLine(destination, line, newLine.getAndSet(true)));
            }
            Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Problem with accessing file", e);
        } finally {
            cleanUp(tmpFile);
        }
    }

    private void writeLine(BufferedWriter writer, String line, boolean newLine) {
        try {
            writer.write((newLine ? "\n" : "") + line);
        } catch (IOException e) {
            LOGGER.error("Problem while writing to file", e);
        }
    }

    private String replaceAll(String line) {
        for (Map.Entry<String, String> entries : var2val.entrySet()) {
            line = line.replace(entries.getKey(), entries.getValue());
        }
        return line;
    }

    private void cleanUp(Path tmpFile) {
        try {
            if (tmpFile != null) {
                Files.deleteIfExists(tmpFile);
            }
        } catch (IOException e) {
            LOGGER.error("Problem while deleting tmp file {}", tmpFile, e);
        }
    }
}
