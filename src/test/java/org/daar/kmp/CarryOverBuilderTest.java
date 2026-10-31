package org.daar.kmp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarryOverBuilderTest {

    // Objet utilise pour construire les tables CarryOver.
    private final CarryOverBuilder builder =
            new CarryOverBuilder();

    /**
     * TEST 1 : Motif "mami".
     *
     * Exemple du cours DAAR 2.
     *
     * Resultat attendu :
     * [-1, 0, -1, 1, 0]
     *
     * On verifie que la table contient
     * exactement les bonnes valeurs.
     */
    @Test
    void testMami() {

        int[] expected = {-1, 0, -1, 1, 0};

        int[] actual = builder.build("mami");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 2 : Motif "mamamia".
     *
     * Exemple du cours DAAR 2.
     *
     * Resultat attendu :
     * [-1, 0, -1, 0, -1, 3, 0, 0]
     *
     * Ce motif contient plusieurs repetitions.
     * Il permet de verifier les replis successifs.
     */
    @Test
    void testMamamia() {

        int[] expected = {
                -1, 0, -1, 0, -1, 3, 0, 0
        };

        int[] actual = builder.build("mamamia");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 3 : Motif "chicha".
     *
     * Exemple du cours DAAR 2.
     *
     * Resultat attendu :
     * [-1, 0, 0, -1, 0, 2, 0]
     *
     * On verifie le fonctionnement sur
     * un autre motif contenant des repetitions.
     */
    @Test
    void testChicha() {

        int[] expected = {
                -1, 0, 0, -1, 0, 2, 0
        };

        int[] actual = builder.build("chicha");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 4 : Motif "Sargon".
     *
     * Exemple du cours DAAR 2.
     *
     * Resultat attendu :
     * [-1, 0, 0, 0, 0, 0, 0]
     *
     * Comme le motif ne possede pas de
     * repetitions utiles, les replis sont nuls.
     */
    @Test
    void testSargon() {

        int[] expected = {
                -1, 0, 0, 0, 0, 0, 0
        };

        int[] actual = builder.build("Sargon");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 5 : Motif avec un seul caractere.
     *
     * Pour "a", la table doit avoir
     * deux cases : [-1, 0].
     */
    @Test
    void testSingleCharacter() {

        int[] expected = {-1, 0};

        int[] actual = builder.build("a");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 6 : Motif sans repetitions.
     *
     * Pour "abc", aucun bord non vide
     * n'est possible.
     *
     * Resultat attendu :
     * [-1, 0, 0, 0]
     */
    @Test
    void testNoRepetition() {

        int[] expected = {-1, 0, 0, 0};

        int[] actual = builder.build("abc");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 7 : Motif avec caracteres identiques.
     *
     * Motif : "aaaa"
     *
     * Les repetitions permettent plusieurs
     * replis optimises.
     *
     * Resultat attendu :
     * [-1, -1, -1, -1, 3]
     */
    @Test
    void testRepeatedCharacters() {

        int[] expected = {
                -1, -1, -1, -1, 3
        };

        int[] actual = builder.build("aaaa");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 8 : Motif vide.
     *
     * La longueur du motif est zero.
     *
     * La table contient une seule case : [-1].
     */
    @Test
    void testEmptyPattern() {

        int[] expected = {-1};

        int[] actual = builder.build("");

        assertArrayEquals(expected, actual);
    }

    /**
     * TEST 9 : Motif null.
     *
     * On verifie que la methode refuse
     * une valeur null en levant une exception.
     */
    @Test
    void testNullPattern() {

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(null)
        );
    }
}