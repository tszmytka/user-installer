package dev.tomek.userinstaller.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.startup.StartupActionScriptManager.ActionCommand;
import com.intellij.ide.startup.StartupActionScriptManager.DeleteCommand;
import com.intellij.ide.startup.StartupActionScriptManager.UnzipCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class InstallPlugins implements Action {
    private static final String API_BASE = "https://plugins.jetbrains.com/api/plugins/";
    private static final String DOWNLOAD_BASE = "https://plugins.jetbrains.com/plugin/download?updateId=";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final Plugin EXTRA_ICONS = new Plugin(11058, "Extra Icons");
    public static final Plugin TEST_ME = new Plugin(9471, "TestMe");
    public static final Plugin TOML = new Plugin(8195, "Toml");
    public static final Plugin RUST = new Plugin(8182, "Rust");

    private final Path userHomeDir;
    private final List<Plugin> pluginsToInstall;

    @Override
    public String getName() {
        return "Installing plugins (%s)".formatted(pluginsToInstall.size());
    }

    @Override
    public Result perform() {
        try {
            final Path systemPlugins = userHomeDir.resolve(Paths.get("system", "plugins"));
            Files.createDirectories(systemPlugins);
            final Path installTarget = userHomeDir.resolve(Paths.get("plugins"));
            final List<ActionCommand> actionCommands = pluginsToInstall.stream().flatMap(p -> preInstall(p, systemPlugins, installTarget)).collect(toList());
            if (actionCommands.size() > 0) {
                final Path actionScript = systemPlugins.resolve("action.script");
                try (ObjectOutput oos = new ObjectOutputStream(Files.newOutputStream(actionScript))) {
                    oos.writeObject(actionCommands.toArray(new ActionCommand[]{}));
                } catch (Exception e) {
                    Files.deleteIfExists(actionScript);
                    LOGGER.error("Could not build action script file", e);
                    return Result.ERROR;
                }
                return Result.OK;
            }
        } catch (IOException e) {
            LOGGER.error("Problem while installing plugins", e);
        }
        return Result.ERROR;
    }

    private Stream<ActionCommand> preInstall(Plugin plugin, Path systemPlugins, Path installTarget) {
        try {
            final Update[] updates = MAPPER.readValue(buildUrlUpdates(plugin.id), Update[].class);
            if (updates.length <= 0) {
                LOGGER.error("No updates for plugin {}", plugin.id);
            } else {
                final Update newest = updates[0];
                final URL pluginUrl = new URL(DOWNLOAD_BASE + newest.id);
                final Path target = systemPlugins.resolve(plugin.name + ".zip");
                Files.copy(pluginUrl.openStream(), target, StandardCopyOption.REPLACE_EXISTING);
                return Stream.of(
                    new DeleteCommand(installTarget.resolve(plugin.name).toAbsolutePath().toString()),
                    new UnzipCommand(target.toAbsolutePath().toString(), installTarget.toAbsolutePath().toString(), null)
                );
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't install plugin: '{}'", plugin.name, e);
        }
        return Stream.empty();
    }

    private URL buildUrlUpdates(int pluginId) throws MalformedURLException {
        return new URL(API_BASE + "%s/updates".formatted(pluginId));
    }

    public record Plugin(int id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Update(int id, String version, String file) {
    }
}
