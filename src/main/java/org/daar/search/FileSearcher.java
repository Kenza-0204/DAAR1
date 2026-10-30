package org.daar.search;

import org.daar.kmp.KmpMatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {

    public List<String> search(String filePath, String word) throws IOException {

        List<String> results = new ArrayList<>();
        KmpMatcher kmp = new KmpMatcher();

        // Lecture du fichier ligne par ligne
        try (var lines = Files.lines(Path.of(filePath))) {

            lines.forEach(line -> {
                if (kmp.contains(line, word)) {
                    results.add(line);
                }
            });
        }

        return results;
    }
}