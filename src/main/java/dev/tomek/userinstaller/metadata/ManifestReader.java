package dev.tomek.userinstaller.metadata;

import picocli.CommandLine;

import java.io.IOException;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestReader implements CommandLine.IVersionProvider {
    private static final String KEY_VERSION = "Application-Version";
    private static final String KEY_COMMIT = "Application-Commit";
    private static final String APPLICATION_NAME = "User Installer";
    private static final String WRITTEN_BY = "Written by Tomek Szmytka";
    private static final String UNSPECIFIED = "<unspecified>";

    private Manifest manifest;

    @Override
    public String[] getVersion() throws Exception {
        final Manifest manifest = getManifest();
        final Attributes mainAttributes = manifest.getMainAttributes();
        return new String[]{buildAppName(mainAttributes), buildAppCommit(mainAttributes), WRITTEN_BY};
    }

    private String buildAppName(Attributes attributes) {
        return APPLICATION_NAME + " " + readAttribute(attributes, KEY_VERSION);
    }

    private String buildAppCommit(Attributes attributes) {
        return "Commit: " + readAttribute(attributes, KEY_COMMIT);
    }

    private String readAttribute(Attributes attributes, String attr) {
        return Optional.ofNullable(attributes.getValue(attr)).orElse(UNSPECIFIED);
    }

    private Manifest getManifest() throws IOException {
        if (manifest == null) {
            manifest = new Manifest(getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
        }
        return manifest;
    }
}
