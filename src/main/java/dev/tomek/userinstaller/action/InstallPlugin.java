package dev.tomek.userinstaller.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.startup.StartupActionScriptManager;
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

@Slf4j
@RequiredArgsConstructor
public class InstallPlugin implements Action {
    private static final String API_BASE = "https://plugins.jetbrains.com/api/plugins/";
    private static final String DOWNLOAD_BASE = "https://plugins.jetbrains.com/plugin/download?updateId=";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String pluginName;
    private final int pluginId;
    private final Path userHomeDir;

    @Override
    public String getName() {
        return "Installing plugin " + pluginName;
    }

    @Override
    public Result perform() {
        try {
            final Update[] updates = MAPPER.readValue(buildUrlUpdates(), Update[].class);
            if (updates.length <= 0) {
                LOGGER.error("No updates for plugin {}", pluginId);
                return Result.ERROR;
            }
            final Update newest = updates[0];
            final Path systemPlugins = userHomeDir.resolve(Paths.get("system", "plugins"));
            Files.createDirectories(systemPlugins);

            final URL pluginUrl = new URL(DOWNLOAD_BASE + newest.id);
            final Path target = systemPlugins.resolve(pluginName + ".zip");
            Files.copy(pluginUrl.openStream(), target, StandardCopyOption.REPLACE_EXISTING);
            final Path installTarget = userHomeDir.resolve(Paths.get("plugins"));
            final StartupActionScriptManager.ActionCommand[] actionCommands = {
                new StartupActionScriptManager.DeleteCommand(installTarget.resolve(pluginName).toAbsolutePath().toString()),
                new StartupActionScriptManager.UnzipCommand(target.toAbsolutePath().toString(), installTarget.toAbsolutePath().toString(), null),
            };
            final Path actionScript = systemPlugins.resolve("action.script");
            try (ObjectOutput oos = new ObjectOutputStream(Files.newOutputStream(actionScript))) {
                oos.writeObject(actionCommands);
            } catch (Exception e) {
                Files.deleteIfExists(actionScript);
                LOGGER.error("Could not build action script file", e);
                return Result.ERROR;
            }
            return Result.OK;
        } catch (IOException e) {
            LOGGER.warn("Problem while installing plugin {}", pluginName, e);
        }
        return Result.ERROR;
    }

    private URL buildUrlUpdates() throws MalformedURLException {
        return new URL(API_BASE + "%s/updates".formatted(pluginId));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Update(int id, String version, String file) {
    }
}
