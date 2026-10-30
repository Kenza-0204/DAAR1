package org.daar.kmp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KmpMatcherTest {

    private final KmpMatcher kmp = new KmpMatcher();

    @Test
    void wordFound() {
        assertTrue(kmp.contains("bonjour monde", "jour"));
    }

    @Test
    void wordNotFound() {
        assertFalse(kmp.contains("bonjour monde", "salut"));
    }

    @Test
    void wordAtBeginning() {
        assertTrue(kmp.contains("bonjour monde", "bonjour"));
    }

    @Test
    void wordAtEnd() {
        assertTrue(kmp.contains("bonjour monde", "monde"));
    }

    @Test
    void wordLongerThanText() {
        assertFalse(kmp.contains("abc", "abcdef"));
    }

    @Test
    void emptyWord() {
        assertTrue(kmp.contains("bonjour", ""));
    }

    @Test
    void repeatedCharacters() {
        assertTrue(kmp.contains("AAAAAB", "AAAB"));
    }

    @Test
    void nullInput() {
        assertThrows(IllegalArgumentException.class,
                () -> kmp.contains(null, "abc"));
    }
}