package org.daar.search;

import org.daar.kmp.KmpMatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {

    // Recherche avec KMP
    public List<String> search(String filePath, String word) throws IOException {
        KmpMatcher kmp = new KmpMatcher();

        return search(filePath, line -> kmp.contains(line, word));
    }

    // Recherche avec un algorithme fourni
    public List<String> search(String filePath, LineMatcher matcher) throws IOException {

        if (matcher == null) {
            throw new IllegalArgumentException("Matcher null");
        }

        List<String> results = new ArrayList<>();

        // Lecture du fichier ligne par ligne
        try (var lines = Files.lines(Path.of(filePath))) {
            lines.forEach(line -> {
                if (matcher.containsMatch(line)) {
                    results.add(line);
                }
            });
        }

        return results;
    }
}