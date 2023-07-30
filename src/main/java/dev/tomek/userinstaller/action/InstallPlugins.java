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
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class InstallPlugins implements Action {
    public static final Plugin EXTRA_ICONS = new Plugin(11058, "Extra Icons", 154299);
    public static final Plugin TEST_ME = new Plugin(9471, "TestMe");
    public static final Plugin TOML = new Plugin(8195, "Toml");
    public static final Plugin RUST = new Plugin(8182, "Rust");
    public static final Plugin EDITOR_CONFIG = new Plugin(7294, "EditorConfig");
    public static final Plugin SETTINGS_REPOSITORY = new Plugin(7294, "Settings Repository");
    private static final String API_BASE = "https://plugins.jetbrains.com/api/plugins/";
    private static final String DOWNLOAD_BASE = "https://plugins.jetbrains.com/plugin/download?updateId=";
    private static final ObjectMapper MAPPER = new ObjectMapper();
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
            final String task = "installing plugins: %s".formatted(pluginsToInstall.stream().map(Plugin::name).toList());
            LOGGER.info("Begin {}", task);
            final List<ActionCommand> actionCommands = pluginsToInstall.stream().flatMap(p -> preInstall(p, systemPlugins, installTarget)).toList();
            if (!actionCommands.isEmpty()) {
                final Path actionScript = systemPlugins.resolve("action.script");
                try (ObjectOutput oos = new ObjectOutputStream(Files.newOutputStream(actionScript))) {
                    oos.writeObject(actionCommands.toArray(new ActionCommand[]{}));
                    LOGGER.info("Finished {}", task);
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
                final URL pluginUrl = URI.create(DOWNLOAD_BASE + Optional.ofNullable(plugin.releaseId).orElse(updates[0].id)).toURL();
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
        return URI.create(API_BASE + "%s/updates".formatted(pluginId)).toURL();
    }

    public record Plugin(int id, String name, Integer releaseId) {
        public Plugin(int id, String name) {
            this(id, name, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Update(int id, String version, String file) {}
}
