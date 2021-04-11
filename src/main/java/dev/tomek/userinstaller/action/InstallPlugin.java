package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class InstallPlugin implements Action {
    private static final String BASE_URL = "https://plugins.jetbrains.com/api/plugins/";

    private final int pluginId;

    @Override
    public String getName() {
        return "Install plugin";
    }

    @Override
    public Result perform() {

        try {
            final URLConnection connection = buildUrlUpdates().openConnection();
            try (BufferedInputStream is = new BufferedInputStream(connection.getInputStream())) {
                final String updatesList = new String(is.readAllBytes(), UTF_8);

            }
        } catch (IOException e) {

        }


        return null;
    }

    private URL buildUrlUpdates() throws MalformedURLException {
        return new URL(BASE_URL + "%s/updates".formatted(pluginId));
    }
}
