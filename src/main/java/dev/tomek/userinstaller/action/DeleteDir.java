package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;

@Slf4j
@RequiredArgsConstructor
public class DeleteDir implements Action {

    private final Path dir;

    @Override
    public String getName() {
        return "Deleting: " + dir;
    }

    @Override
    public boolean perform() {
        try {
            if (Files.exists(dir)) {
                final boolean dosAttrsSupported = Files.getFileStore(dir).supportsFileAttributeView(DosFileAttributeView.class);
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ensureWritability(file, dosAttrsSupported);
                        Files.deleteIfExists(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        ensureWritability(dir, dosAttrsSupported);
                        Files.deleteIfExists(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } else {
                LOGGER.info("Directory for deletion doesn't exist: {}", dir);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot delete directory: {}", dir, e);
        }
        return false;
    }

    private void ensureWritability(Path path, boolean dosAttrsSupported) throws IOException {
        if (!Files.isWritable(path)) {
            if (!dosAttrsSupported) {
                throw new IllegalStateException("Only DosFileAttributeView is currently supported");
            }
            final DosFileAttributeView attributes = Files.getFileAttributeView(path, DosFileAttributeView.class);
            attributes.setSystem(false);
            attributes.setHidden(false);
            attributes.setReadOnly(false);
        }
    }
}
