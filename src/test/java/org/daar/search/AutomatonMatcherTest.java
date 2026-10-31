
package org.daar.search;

import org.daar.automata.DFA;
import org.daar.automata.NFA;
import org.daar.automata.Determinizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutomatonMatcherTest {

    /**
     * Methode utilitaire :
     * Transforme un NFA en DFA puis cree le matcher.
     *
     * Cela evite de repeter le meme code dans chaque test.
     */
    private AutomatonMatcher createMatcher(NFA nfa) {
        DFA dfa = new Determinizer().determinize(nfa);
        return new AutomatonMatcher(dfa);
    }

    /**
     * Construit un NFA reconnaissant le motif "ab".
     *
     * q0 --a--> q1 --b--> q2
     *
     * q0 : etat initial
     * q2 : etat acceptant
     */
    private NFA buildAB() {
        NFA nfa = new NFA();

        int q0 = nfa.newState();
        int q1 = nfa.newState();
        int q2 = nfa.newState();

        nfa.setStart(q0);
        nfa.addAccepting(q2);

        nfa.addTransition(q0, 'a', q1);
        nfa.addTransition(q1, 'b', q2);

        return nfa;
    }

    /**
     * TEST 1 : Le motif "ab" est au debut de la ligne.
     *
     * Ligne : "abxyz"
     * Resultat attendu : true
     */
    @Test
    void testMatchAtBeginning() {
        AutomatonMatcher matcher = createMatcher(buildAB());

        assertTrue(matcher.containsMatch("abxyz"));
        assertTrue(matcher.containsMatch("ab"));
    }

    /**
     * TEST 2 : Le motif "ab" est au milieu de la ligne.
     *
     * Ligne : "xxabyy"
     * Resultat attendu : true
     */
    @Test
    void testMatchInMiddle() {
        AutomatonMatcher matcher = createMatcher(buildAB());

        assertTrue(matcher.containsMatch("xxabyy"));
        assertTrue(matcher.containsMatch("bonjourabmonde"));
    }

    /**
     * TEST 3 : Le motif "ab" est a la fin de la ligne.
     *
     * Ligne : "xyzab"
     * Resultat attendu : true
     */
    @Test
    void testMatchAtEnd() {
        AutomatonMatcher matcher = createMatcher(buildAB());

        assertTrue(matcher.containsMatch("xyzab"));
        assertTrue(matcher.containsMatch("xxab"));
    }

    /**
     * TEST 4 : Le motif "ab" n'apparait pas dans la ligne.
     *
     * Une correspondance partielle ne doit pas
     * etre consideree comme un resultat valide.
     */
    @Test
    void testNoMatch() {
        AutomatonMatcher matcher = createMatcher(buildAB());

        assertFalse(matcher.containsMatch("xyz"));
        assertFalse(matcher.containsMatch("acb"));
        assertFalse(matcher.containsMatch("a"));
        assertFalse(matcher.containsMatch(""));
    }

    /**
     * TEST 5 : Alternation "a|b".
     *
     * L'automate doit reconnaitre soit 'a', soit 'b'.
     *
     * q0 --epsilon--> q1 --a--> q3
     * q0 --epsilon--> q2 --b--> q3
     */
    @Test
    void testAlternation() {
        NFA nfa = new NFA();

        int q0 = nfa.newState();
        int q1 = nfa.newState();
        int q2 = nfa.newState();
        int q3 = nfa.newState();

        nfa.setStart(q0);
        nfa.addAccepting(q3);

        nfa.addEpsilon(q0, q1);
        nfa.addEpsilon(q0, q2);

        nfa.addTransition(q1, 'a', q3);
        nfa.addTransition(q2, 'b', q3);

        AutomatonMatcher matcher = createMatcher(nfa);

        assertTrue(matcher.containsMatch("xxayy"));
        assertTrue(matcher.containsMatch("xxbyy"));
        assertFalse(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 6 : Etoile "a*".
     *
     * Le motif reconnait zero ou plusieurs 'a'.
     *
     * q0 est initial et acceptant.
     * q0 --a--> q0
     *
     * Comme le mot vide est accepte,
     * toutes les lignes correspondent.
     */
    @Test
    void testStar() {
        NFA nfa = new NFA();

        int q0 = nfa.newState();

        nfa.setStart(q0);
        nfa.addAccepting(q0);
        nfa.addTransition(q0, 'a', q0);

        AutomatonMatcher matcher = createMatcher(nfa);

        assertTrue(matcher.containsMatch(""));
        assertTrue(matcher.containsMatch("aaa"));
        assertTrue(matcher.containsMatch("xyz"));
    }

    /**
     * TEST 7 : Joker "a.b".
     *
     * Le point correspond a un caractere quelconque.
     *
     * q0 --a--> q1 --ANY--> q2 --b--> q3
     */
    @Test
    void testWildcard() {
        NFA nfa = new NFA();

        int q0 = nfa.newState();
        int q1 = nfa.newState();
        int q2 = nfa.newState();
        int q3 = nfa.newState();

        nfa.setStart(q0);
        nfa.addAccepting(q3);

        nfa.addTransition(q0, 'a', q1);
        nfa.addAny(q1, q2);
        nfa.addTransition(q2, 'b', q3);

        AutomatonMatcher matcher = createMatcher(nfa);

        assertTrue(matcher.containsMatch("xxacbyy"));
        assertTrue(matcher.containsMatch("a7b"));
        assertTrue(matcher.containsMatch("a!b"));

        assertFalse(matcher.containsMatch("ab"));
        assertFalse(matcher.containsMatch("axxc"));
    }

    /**
     * TEST 8 : Parentheses "(a|b)c".
     *
     * L'automate doit reconnaitre :
     * - "ac"
     * - "bc"
     *
     * Mais pas "a" ou "b" seuls.
     *
     * Les parentheses sont representees ici
     * par la structure du NFA.
     */
    @Test
    void testParentheses() {
        NFA nfa = new NFA();

        int q0 = nfa.newState();
        int q1 = nfa.newState();
        int q2 = nfa.newState();
        int q3 = nfa.newState();
        int q4 = nfa.newState();

        nfa.setStart(q0);
        nfa.addAccepting(q4);

        // Deux branches : a ou b.
        nfa.addEpsilon(q0, q1);
        nfa.addEpsilon(q0, q2);

        nfa.addTransition(q1, 'a', q3);
        nfa.addTransition(q2, 'b', q3);

        // Puis obligatoirement le caractere c.
        nfa.addTransition(q3, 'c', q4);

        AutomatonMatcher matcher = createMatcher(nfa);

        assertTrue(matcher.containsMatch("xxacyy"));
        assertTrue(matcher.containsMatch("xxbcyy"));
        assertTrue(matcher.containsMatch("abc"));

        assertFalse(matcher.containsMatch("xxay"));
        assertFalse(matcher.containsMatch("xxby"));
        assertFalse(matcher.containsMatch("abd"));
    }

    /**
     * TEST 9 : Verifier le rejet d'une ligne null.
     *
     * Le matcher doit lever une exception
     * et non provoquer une erreur inattendue.
     */
    @Test
    void testNullLine() {
        AutomatonMatcher matcher = createMatcher(buildAB());

        assertThrows(
                IllegalArgumentException.class,
                () -> matcher.containsMatch(null)
        );
    }

    /**
     * TEST 10 : Verifier qu'un DFA null est refuse
     * par le constructeur du matcher.
     */
    @Test
    void testNullDfa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AutomatonMatcher(null)
        );
    }
}
