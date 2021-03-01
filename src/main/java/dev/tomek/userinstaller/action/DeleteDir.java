package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
