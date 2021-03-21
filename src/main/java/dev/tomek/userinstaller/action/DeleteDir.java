package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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
            // todo internal git files have the read-only attribute set - this causes "delete" calls to throw AccessDenied
            if (Files.exists(dir)) {
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
}
