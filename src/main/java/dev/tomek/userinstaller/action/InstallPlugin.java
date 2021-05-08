package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
public class InstallPlugin implements Action {
    private static final String API_BASE = "https://plugins.jetbrains.com/api/plugins/";
    private static final String DOWNLOAD_BASE = "https://plugins.jetbrains.com/plugin/download?rel=true&updateId=";
    private static final Path TMP_DIR = Paths.get("D:", "tmp", "downloads");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final int pluginId = 11058;
    private final Path userHomeDir;

    @Override
    public String getName() {
        return "Installing plugin ";
    }

    @Override
    public Result perform() {

        // todo find newest updateId
        final int updateId = 118941;
        final String pluginUrl = DOWNLOAD_BASE + updateId;
        // todo download pluginUrl

        final Path source = TMP_DIR.resolve(Paths.get("Extra_Icons-1.52.0.201.zip"));
        final Path target = userHomeDir.resolve(Paths.get("system", "plugins"));

        try {
//            final Update[] updates = MAPPER.readValue(buildUrlUpdates(), Update[].class);

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);


        } catch (IOException e) {
            LOGGER.warn("Problem while copying {}", source, e);
        }


        return Result.ERROR;
    }

    private URL buildUrlUpdates() throws MalformedURLException {
        return new URL(API_BASE + "%s/updates".formatted(pluginId));
    }

//    @JsonIgnoreProperties(ignoreUnknown = true)
//    private record Update(int id, String version, String file) {
//    }
}
