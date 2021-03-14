package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class CopyOptions implements Action {

    private final Path baseDir;
    private final Path optionsFile;
    private final List<String> optionNames;


    @Override
    public String getName() {
        return "Copying options";
    }

    @Override
    public boolean perform() {
        try {
            final List<Map.Entry<Path, FileTime>> collect = Files.find(baseDir, 3, (p, a) -> a.isRegularFile() && p.getFileName().endsWith(optionsFile))
                .map(p -> {
                    FileTime fileTime;
                    try {
                        fileTime = Files.readAttributes(p, BasicFileAttributes.class).lastModifiedTime();
                    } catch (IOException e) {
                        fileTime = FileTime.from(Instant.now());
                    }
                    return Map.entry(p, fileTime);
                })
                .sorted(Map.Entry.comparingByKey())
                .limit(2)
                .collect(toList());
            if (collect.size() != 2) {
                throw new IllegalStateException("Unexpected amount of options files found:" + collect.size());
            }
            final Path targetPath = collect.get(1).getKey();
            final List<String> targetAlreadyContaining = Files.lines(targetPath, UTF_8).filter(l -> optionNames.stream().anyMatch(l::contains)).collect(toList());
            if (!targetAlreadyContaining.isEmpty()) {
                throw new IllegalStateException("Target file already contains option names: " + targetAlreadyContaining);
            }
            try (BufferedWriter target = Files.newBufferedWriter(targetPath, UTF_8, StandardOpenOption.APPEND)) {
                target.newLine();
                target.write(Files.lines(collect.get(0).getKey(), UTF_8).filter(l -> optionNames.stream().anyMatch(l::contains)).collect(joining("\n")));
                target.newLine();
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot copy options", e);
        }
        return false;
    }
}
