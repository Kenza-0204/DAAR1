package org.daar.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSearcherTest {

    private final FileSearcher searcher = new FileSearcher();

    @TempDir
    Path folder;

    @Test
    void wordFound() throws IOException {
        Path file = folder.resolve("test.txt");
        Files.writeString(file, "bonjour\nprojet DAAR\nau revoir");

        List<String> results = searcher.search(file.toString(), "DAAR");

        assertEquals(List.of("projet DAAR"), results);
    }

    @Test
    void severalLinesFound() throws IOException {
        Path file = folder.resolve("test.txt");
        Files.writeString(file, "DAAR projet\nbonjour\ncours DAAR");

        List<String> results = searcher.search(file.toString(), "DAAR");

        assertEquals(List.of("DAAR projet", "cours DAAR"), results);
    }

    @Test
    void wordNotFound() throws IOException {
        Path file = folder.resolve("test.txt");
        Files.writeString(file, "bonjour\nau revoir");

        List<String> results = searcher.search(file.toString(), "DAAR");

        assertTrue(results.isEmpty());
    }

    @Test
    void emptyFile() throws IOException {
        Path file = folder.resolve("test.txt");
        Files.writeString(file, "");

        List<String> results = searcher.search(file.toString(), "DAAR");

        assertTrue(results.isEmpty());
    }

    @Test
    void fileNotFound() {
        Path file = folder.resolve("inexistant.txt");

        assertThrows(IOException.class,
                () -> searcher.search(file.toString(), "DAAR"));
    }
}
