package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ResolveVars implements Action {

    private final Path baseDir;

    private final Map<String, String> var2val;

    @Override
    public String getName() {
        return "Resolving template variables";
    }

    @Override
    public Result perform() {
        try {
            Files.walk(baseDir).filter(Files::isRegularFile).forEach(this::resolveVars);
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
                source.lines().map(this::replaceAll).forEach(line -> writeLine(destination, line));
            }
            Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Problem with accessing file");
        } finally {
            cleanUp(tmpFile);
        }
    }

    private void writeLine(BufferedWriter writer, String line) {
        try {
            writer.write(line);
            writer.newLine();
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
