
package org.daar.automata;

import org.daar.regex.RegexNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThompsonBuilderTest {

    private final ThompsonBuilder thompson = new ThompsonBuilder();

    @Test
    void literal() {
        NFA nfa = thompson.build(RegexNode.literal('a'));

        assertEquals(2, nfa.size());
        assertEquals(1, nfa.getTransitions(nfa.getStart(), 'a').size());
    }

    @Test
    void wildcard() {
        NFA nfa = thompson.build(RegexNode.wildcard('.'));

        assertEquals(2, nfa.size());
        assertEquals(1, nfa.getAny(nfa.getStart()).size());
    }

    @Test
    void concatenation() {
        RegexNode regex = RegexNode.concatenation(
                RegexNode.literal('a'),
                RegexNode.literal('b')
        );

        NFA nfa = thompson.build(regex);

        assertEquals(4, nfa.size());
        assertEquals(1, nfa.getAccepting().size());
    }

    @Test
    void alternation() {
        RegexNode regex = RegexNode.alternation(
                RegexNode.literal('a'),
                RegexNode.literal('b')
        );

        NFA nfa = thompson.build(regex);

        assertEquals(6, nfa.size());
        assertEquals(2, nfa.getEpsilon(nfa.getStart()).size());
    }

    @Test
    void star() {
        RegexNode regex = RegexNode.star(RegexNode.literal('a'));

        NFA nfa = thompson.build(regex);

        assertEquals(4, nfa.size());
        assertEquals(2, nfa.getEpsilon(nfa.getStart()).size());
    }

    @Test
    void nullRegex() {
        assertThrows(IllegalArgumentException.class,
                () -> thompson.build(null));
    }
}
