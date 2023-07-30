package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class CopyFiles implements Action {
    private final Path from;
    private final Path to;

    @Override
    public String getName() {
        return "Copying files";
    }

    @Override
    public Result perform() {
        try (Stream<Path> files = Files.walk(from)) {
            if (files.map(file -> Map.entry(file, to.resolve(from.relativize(file)))).filter(pair -> !copyAll(pair)).findAny().isEmpty()) {
                return Result.OK;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot copy {}", from, e);
        }
        return Result.ERROR;
    }

    private boolean copyAll(Entry<Path, Path> copyPair) {
        final Path source = copyPair.getKey();
        final Path dest = copyPair.getValue();
        try {
            if (Files.isDirectory(source)) {
                Files.createDirectories(dest);
            } else {
                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            LOGGER.warn("Problem while copying {}", source, e);
        }
        return false;
    }
}
