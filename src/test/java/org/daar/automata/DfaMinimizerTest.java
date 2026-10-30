package org.daar.automata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DfaMinimizerTest {

    private final DfaMinimizer minimizer = new DfaMinimizer();

    @Test
    void equivalentStates() {
        DFA dfa = new DFA();

        int start = dfa.newState();
        int state1 = dfa.newState();
        int state2 = dfa.newState();

        dfa.setStart(start);
        dfa.addAccepting(state1);
        dfa.addAccepting(state2);

        dfa.addTransition(start, 'a', state1);
        dfa.addTransition(start, 'b', state2);

        DFA result = minimizer.minimize(dfa);

        // Les deux états finaux doivent être regroupés
        assertEquals(result.next(result.getStart(), 'a'),
                result.next(result.getStart(), 'b'));
    }

    @Test
    void differentStates() {
        DFA dfa = new DFA();

        int start = dfa.newState();
        int finalState = dfa.newState();

        dfa.setStart(start);
        dfa.addAccepting(finalState);
        dfa.addTransition(start, 'a', finalState);

        DFA result = minimizer.minimize(dfa);

        // L'état initial et l'état final restent différents
        assertNotEquals(result.getStart(),
                result.next(result.getStart(), 'a'));
    }

    @Test
    void unreachableState() {
        DFA dfa = new DFA();

        int start = dfa.newState();
        int unused = dfa.newState();

        dfa.setStart(start);
        dfa.addAccepting(start);
        dfa.addAccepting(unused);

        DFA result = minimizer.minimize(dfa);

        // L'état inaccessible n'a pas besoin d'être conservé
        assertTrue(result.size() < dfa.size() + 1);
    }

    @Test
    void wildcard() {
        DFA dfa = new DFA();

        int start = dfa.newState();
        int end = dfa.newState();

        dfa.setStart(start);
        dfa.addAccepting(end);
        dfa.addAny(start, end);

        DFA result = minimizer.minimize(dfa);

        int next = result.next(result.getStart(), 'x');

        assertTrue(result.isAccepting(next));
    }

    @Test
    void nullDfa() {
        assertThrows(IllegalArgumentException.class,
                () -> minimizer.minimize(null));
    }
}
