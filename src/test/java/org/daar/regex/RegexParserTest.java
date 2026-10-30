
package org.daar.regex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegexParserTest {

    private final RegexParser parser = new RegexParser();

    // TEST 1 : Une seule lettre
    @Test
    void testLiteral() {
        RegexNode node = parser.parse("a");

        assertEquals(RegexNode.Type.LITERAL, node.getType());
        assertEquals('a', node.getSymbol());
    }

    // TEST 2 : Concatenation ab
    @Test
    void testConcatenation() {
        RegexNode node = parser.parse("ab");

        assertEquals(RegexNode.Type.CONCAT, node.getType());
        assertEquals('a', node.getLeft().getSymbol());
        assertEquals('b', node.getRight().getSymbol());
    }

    // TEST 3 : Alternative a|b
    @Test
    void testAlternation() {
        RegexNode node = parser.parse("a|b");

        assertEquals(RegexNode.Type.ALTERNATION, node.getType());
        assertEquals('a', node.getLeft().getSymbol());
        assertEquals('b', node.getRight().getSymbol());
    }

    // TEST 4 : Etoile de Kleene a*
    @Test
    void testStar() {
        RegexNode node = parser.parse("a*");

        assertEquals(RegexNode.Type.STAR, node.getType());
        assertEquals('a', node.getLeft().getSymbol());
    }

    // TEST 5 : Joker .
    @Test
    void testWildcard() {
        RegexNode node = parser.parse(".");

        assertEquals(RegexNode.Type.WILDCARD, node.getType());
    }

    // TEST 6 : Priorite des operateurs
    @Test
    void testPriority() {
        RegexNode node = parser.parse("a|bc*");

        // La racine doit etre ALTERNATION
        assertEquals(
                RegexNode.Type.ALTERNATION,
                node.getType()
        );

        // A gauche : a
        assertEquals('a', node.getLeft().getSymbol());

        // A droite : b suivi de c*
        assertEquals(
                RegexNode.Type.CONCAT,
                node.getRight().getType()
        );

        assertEquals(
                'b',
                node.getRight().getLeft().getSymbol()
        );

        assertEquals(
                RegexNode.Type.STAR,
                node.getRight().getRight().getType()
        );

        assertEquals(
                'c',
                node.getRight().getRight().getLeft().getSymbol()
        );
    }

    // TEST 7 : Parentheses et repetition
    @Test
    void testParentheses() {
        RegexNode node = parser.parse("(a|b)*c");

        assertEquals(RegexNode.Type.CONCAT, node.getType());

        // Partie gauche : (a|b)*
        assertEquals(
                RegexNode.Type.STAR,
                node.getLeft().getType()
        );

        // Sous STAR : a|b
        assertEquals(
                RegexNode.Type.ALTERNATION,
                node.getLeft().getLeft().getType()
        );

        // Partie droite : c
        assertEquals('c', node.getRight().getSymbol());
    }

    // TEST 8 : Expression invalide
    @Test
    void testInvalidPatterns() {
        assertThrows(
                RegexException.class,
                () -> parser.parse("a|")
        );

        assertThrows(
                RegexException.class,
                () -> parser.parse("*a")
        );

        assertThrows(
                RegexException.class,
                () -> parser.parse("(ab")
        );

        assertThrows(
                RegexException.class,
                () -> parser.parse("()")
        );

        assertThrows(
                RegexException.class,
                () -> parser.parse("a+")
        );

        assertThrows(
                RegexException.class,
                () -> parser.parse("")
        );
    }

    // TEST 9 : Position de l'erreur
    @Test
    void testExceptionPosition() {
        RegexException exception = assertThrows(
                RegexException.class,
                () -> parser.parse("a|")
        );

        assertEquals(2, exception.getPosition());
    }
}
