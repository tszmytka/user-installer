package dev.tomek.userinstaller.action;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DeleteDirTest {

    private static Path workingDir;

    @BeforeAll
    static void beforeAll() throws IOException {
        workingDir = Files.createTempDirectory(DeleteDirTest.class.getSimpleName());
    }

    @AfterAll
    static void afterAll() {
        try {
            if (workingDir != null) {
                Files.deleteIfExists(workingDir);
            }
        } catch (IOException ignored) {
        }
    }

    @Test
    void canDeleteDirectory() throws IOException {
        final Path dir1 = Files.createDirectory(workingDir.resolve("dir1"));
        Files.createFile(dir1.resolve("file1.dat"));
        Files.createFile(dir1.resolve("file2.txt"));
        assumeTrue(Files.list(dir1).count() > 1);
        final DeleteDir deleteDir = new DeleteDir(dir1);
        deleteDir.perform();

        assertThat(Files.exists(dir1)).isFalse();
    }
}
