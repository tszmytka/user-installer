package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

@Slf4j
@RequiredArgsConstructor
public class SetVmOptions implements Action {
    private final Path homeDir;
    private final String applicationName;

    private final Path optionsFile;

    @Override
    public String getName() {
        return "Setting startup vm options";
    }

    @Override
    public Result perform() {
        try {
            final List<String> optIds = Arrays.stream(Opt.values()).map(opt -> opt.optId).toList();
            final List<String> configAlreadyContaining = Files.lines(optionsFile, UTF_8).filter(l -> optIds.stream().anyMatch(l::contains)).toList();
            if (!configAlreadyContaining.isEmpty()) {
                if (configAlreadyContaining.size() == optIds.size()) {
                    LOGGER.info("All options are already in the target file");
                    return Result.SKIPPED;
                } else {
                    throw new IllegalStateException("Some (but not all) options are already in the target file: " + configAlreadyContaining);
                }
            }
            try (BufferedWriter target = Files.newBufferedWriter(optionsFile, UTF_8, StandardOpenOption.APPEND)) {
                target.newLine();
                final String collect = Arrays.stream(Opt.values())
                    .map(opt -> opt.optId + "=" + (opt.dirSuffix.isBlank() ? homeDir : homeDir.resolve(Paths.get(applicationName, opt.dirSuffix))))
                    .collect(joining("\n"));
                target.write(collect);
                target.newLine();
            }
            return Result.OK;
        } catch (IOException e) {
            LOGGER.error("Cannot set options", e);
        }
        return Result.ERROR;
    }

    @RequiredArgsConstructor
    private enum Opt {
        USER_HOME("-Duser.home", ""),
        CONFIG_PATH("-Didea.config.path", "config"),
        SYSTEM_PATH("-Didea.system.path", "system"),
        PLUGINS_PATH("-Didea.plugins.path", "plugins"),
        LOG_PATH("-Didea.log.path", "log");

        private final String optId;
        private final String dirSuffix;
    }
}
