package org.daar.search;

import org.daar.regex.RegexParser;
import org.daar.regex.RegexNode;

import org.daar.automata.NFA;
import org.daar.automata.DFA;
import org.daar.automata.ThompsonBuilder;
import org.daar.automata.Determinizer;
import org.daar.automata.DfaMinimizer;

import org.daar.kmp.CarryOverBuilder;
import org.daar.kmp.KmpMatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTS D'INTEGRATION DU PROJET DAAR.
 */
class RegexIntegrationTest {

    // JUnit cree automatiquement un dossier temporaire
    // pour les tests utilisant de vrais fichiers.
    @TempDir
    Path tempDir;

    /**
     * Construire le DFA a partir d'une expression reguliere.
     */
    private DFA buildDfa(String regex) {

        RegexNode ast = new RegexParser().parse(regex);

        NFA nfa = new ThompsonBuilder().build(ast);

        return new Determinizer().determinize(nfa);
    }

    /**
     * avec un DFA construit a partir de la vraie regex.
     */
    private AutomatonMatcher createMatcher(String regex) {
        return new AutomatonMatcher(buildDfa(regex));
    }

    /**
     * TEST 1 : Concatenation.
     *
     * Verifie que le motif "abc" est reconnu
     * au debut, au milieu et a la fin d'une ligne.
     *
     * Modules : Parser + Thompson + Determinizer
     *           + AutomatonMatcher.
     */
    @Test
    void testConcatenation() {
        AutomatonMatcher matcher = createMatcher("abc");

        assertTrue(matcher.containsMatch("abc"));
        assertTrue(matcher.containsMatch("xxabcxx"));
        assertTrue(matcher.containsMatch("bonjourabc"));

        assertFalse(matcher.containsMatch("ab"));
        assertFalse(matcher.containsMatch("acb"));
        assertFalse(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 2 : Alternation.
     *
     * Verifie que "a|b" reconnait 'a' OU 'b'.
     * Thompson construit les deux branches.
     * Determinizer les transforme en DFA.
     */
    @Test
    void testAlternation() {
        AutomatonMatcher matcher = createMatcher("a|b");

        assertTrue(matcher.containsMatch("xxayy"));
        assertTrue(matcher.containsMatch("xxbyy"));

        assertFalse(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 3 : Etoile.
     *
     * "ab*c" reconnait :
     * ac, abc, abbc, abbbc, etc.
     *
     * Teste les repetitions et les transitions
     * epsilon du NFA de Thompson.
     */
    @Test
    void testStar() {
        AutomatonMatcher matcher = createMatcher("ab*c");

        assertTrue(matcher.containsMatch("ac"));
        assertTrue(matcher.containsMatch("abc"));
        assertTrue(matcher.containsMatch("abbbc"));
        assertTrue(matcher.containsMatch("xxabbbcxx"));

        assertFalse(matcher.containsMatch("abb"));
        assertFalse(matcher.containsMatch("axc"));
    }

    /**
     * TEST 4 : Joker '.'.
     *
     * "a.b" reconnait exactement un caractere
     * quelconque entre a et b.
     *
     * Verifie la compatibilite des transitions
     * joker entre Thompson et Determinizer.
     */
    @Test
    void testWildcard() {
        AutomatonMatcher matcher = createMatcher("a.b");

        assertTrue(matcher.containsMatch("acb"));
        assertTrue(matcher.containsMatch("a7b"));
        assertTrue(matcher.containsMatch("xxa!byy"));

        assertFalse(matcher.containsMatch("ab"));
        assertFalse(matcher.containsMatch("axxb"));
    }

    /**
     * TEST 5 : Parentheses.
     *
     * "(a|b)c" reconnait "ac" ou "bc".
     *
     * Verifie la priorite des operations
     * depuis le Parser jusqu'au Matcher.
     */
    @Test
    void testParentheses() {
        AutomatonMatcher matcher = createMatcher("(a|b)c");

        assertTrue(matcher.containsMatch("ac"));
        assertTrue(matcher.containsMatch("bc"));
        assertTrue(matcher.containsMatch("xxacyy"));

        // "abc" contient la sous-chaine "bc".
        assertTrue(matcher.containsMatch("abc"));

        assertFalse(matcher.containsMatch("abd"));
        assertFalse(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 6 : Repetition d'un groupe.
     *
     * "(ab)*c" reconnait zero ou plusieurs
     * repetitions de "ab" suivies de "c".
     */
    @Test
    void testGroupedStar() {
        AutomatonMatcher matcher = createMatcher("(ab)*c");

        assertTrue(matcher.containsMatch("c"));
        assertTrue(matcher.containsMatch("abc"));
        assertTrue(matcher.containsMatch("ababc"));
        assertTrue(matcher.containsMatch("xxababcxx"));

        assertFalse(matcher.containsMatch("abab"));
        assertFalse(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 7 : Mot vide.
     *
     * "a*" accepte zero caractere.
     * Il doit correspondre meme a une ligne vide.
     */
    @Test
    void testEmptyMatch() {
        AutomatonMatcher matcher = createMatcher("a*");

        assertTrue(matcher.containsMatch(""));
        assertTrue(matcher.containsMatch("aaa"));
        assertTrue(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 8 : Minimisation simple.
     * La minimisation ne doit pas changer
     * les lignes acceptees.
     */
    @Test
    void testMinimizationSimple() {
        DFA original = buildDfa("ab*c");

        DFA minimized = new DfaMinimizer().minimize(original);

        AutomatonMatcher before = new AutomatonMatcher(original);
        AutomatonMatcher after = new AutomatonMatcher(minimized);

        String[] lines = {
                "", "ac", "abc", "abbbc",
                "xxabbbcxx", "abb", "axc", "xyz"
        };

        for (String line : lines) {
            assertEquals(
                    before.containsMatch(line),
                    after.containsMatch(line),
                    "Difference apres minimisation : " + line
            );
        }
    }

    /**
     * TEST 9 : Minimisation avec plusieurs regex.
     */
    @Test
    void testMinimizationMultipleRegexes() {
        String[] regexes = {
                "abc", "a|b", "ab*c", "a.b",
                "(a|b)c", "(ab)*c", "a*", "(a|b)*c"
        };

        String[] lines = {
                "", "abc", "xxabcxx", "ac", "bc",
                "axb", "a7b", "abbbc", "xyz",
                "ababc", "c", "aaabc", "bonjour"
        };

        for (String regex : regexes) {
            DFA original = buildDfa(regex);
            DFA minimized = new DfaMinimizer().minimize(original);

            AutomatonMatcher before = new AutomatonMatcher(original);
            AutomatonMatcher after = new AutomatonMatcher(minimized);

            for (String line : lines) {
                assertEquals(
                        before.containsMatch(line),
                        after.containsMatch(line),
                        "Difference pour regex=" + regex
                                + ", ligne=" + line
                );
            }
        }
    }

    /**
     * TEST 10 : Joker et alternation.
     *
     * Pour "a|.", une transition litterale
     * et une transition joker peuvent s'appliquer.
     * On verifie aussi le DFA minimise.
     */
    @Test
    void testWildcardWithAlternation() {
        DFA original = buildDfa("a|.");
        DFA minimized = new DfaMinimizer().minimize(original);

        AutomatonMatcher before = new AutomatonMatcher(original);
        AutomatonMatcher after = new AutomatonMatcher(minimized);

        String[] matching = {"a", "b", "!", "xyz"};

        for (String line : matching) {
            assertTrue(before.containsMatch(line));
            assertTrue(after.containsMatch(line));
        }

        assertFalse(before.containsMatch(""));
        assertFalse(after.containsMatch(""));
    }

    /**
     * TEST 11 : Integration CarryOver + KMP.
     * On utilise "mami", exemple du cours DAAR.
     */
    @Test
    void testKmpWithCarryOverMami() {
        CarryOverBuilder builder = new CarryOverBuilder();
        KmpMatcher kmp = new KmpMatcher();

        // Verifier la table attendue du cours.
        assertArrayEquals(
                new int[]{-1, 0, -1, 1, 0},
                builder.build("mami")
        );

        // Verifier la recherche utilisant cette table.
        assertTrue(kmp.contains("xxmamiyy", "mami"));
        assertTrue(kmp.contains("mamami", "mami"));

        assertFalse(kmp.contains("mamx", "mami"));
        assertFalse(kmp.contains("xyz", "mami"));
    }

    /**
     * TEST 12 : KMP avec repetitions et replis.
     *
     * Verifie que l'algorithme de Nadine
     * sait reprendre apres une comparaison ratee,
     * grace aux valeurs CarryOver de Kenza.
     *
     * On teste aussi un motif sans repetition
     * et un motif vide.
     */
    @Test
    void testKmpFallbackAndEdgeCases() {
        CarryOverBuilder builder = new CarryOverBuilder();
        KmpMatcher kmp = new KmpMatcher();

        assertArrayEquals(
                new int[]{-1, 0, -1, 0, -1, 3, 0, 0},
                builder.build("mamamia")
        );

        assertTrue(kmp.contains("xxmamamiayy", "mamamia"));
        assertFalse(kmp.contains("mamamix", "mamamia"));

        assertTrue(kmp.contains("AAAAAB", "AAAB"));
        assertFalse(kmp.contains("AAAAAC", "AAAB"));

        assertTrue(kmp.contains("bonjour", ""));
        assertFalse(kmp.contains("", "bonjour"));
    }

    /**
     * TEST 13 : FileSearcher + vrai AutomatonMatcher.
     *
     * On cree un vrai fichier temporaire contenant
     * plusieurs lignes de texte.
     * Le motif "(a|b)c" doit trouver les lignes
     * contenant "ac" ou "bc".
     */
    @Test
    void testFileSearcherWithRealAutomaton()
            throws IOException {

        Path file = tempDir.resolve("regex.txt");

        Files.writeString(
                file,
                "bonjour\nabc\nac\nbc\nabd\nxyz\nxxacyy\n"
        );

        FileSearcher searcher = new FileSearcher();

        AutomatonMatcher matcher =
                createMatcher("(a|b)c");

        List<String> results = searcher.search(
                file.toString(),
                matcher
        );

        // "abc" est accepte car il contient "bc".
        assertEquals(
                List.of("abc", "ac", "bc", "xxacyy"),
                results
        );
    }

    /**
     * TEST 14 : FileSearcher + KMP + CarryOver.
     * On verifie que seules les bonnes lignes
     * sont retournees, dans le bon ordre.
     */
    @Test
    void testFileSearcherWithKmpAndCarryOver()
            throws IOException {

        Path file = tempDir.resolve("kmp.txt");

        Files.writeString(
                file,
                "bonjour\nxxmamiyy\nmami\nmamx\nmamami\n"
        );

        FileSearcher searcher = new FileSearcher();

        List<String> results = searcher.search(
                file.toString(),
                "mami"
        );

        assertEquals(
                List.of("xxmamiyy", "mami", "mamami"),
                results
        );
    }
}
