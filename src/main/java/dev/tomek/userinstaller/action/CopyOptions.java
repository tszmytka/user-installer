package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class CopyOptions implements Action {
    private final Path from;
    private final Path to;
    private final List<String> optionNames;


    @Override
    public String getName() {
        return "Copying options";
    }

    @Override
    public boolean perform() {
        try {
            final List<String> targetAlreadyContaining = Files.lines(to, UTF_8).filter(l -> optionNames.stream().anyMatch(l::contains)).collect(toList());
            if (!targetAlreadyContaining.isEmpty()) {
                throw new IllegalStateException("Target file already contains option names: " + targetAlreadyContaining);
            }
            try (BufferedWriter target = Files.newBufferedWriter(to, UTF_8, StandardOpenOption.APPEND)) {
                target.newLine();
                target.write(Files.lines(from, UTF_8).filter(l -> optionNames.stream().anyMatch(l::contains)).collect(joining("\n")));
                target.newLine();
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot copy options", e);
        }
        return false;
    }
}
