
package org.daar.automata;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeterminizerTest {

    /**
     * TEST 1 : Verifier le calcul de la fermeture epsilon.
     *
     * On construit le NFA suivant :
     *
     * q0 --epsilon--> q1 --epsilon--> q2
     *
     * Une transition epsilon permet de changer d'etat
     * sans lire de caractere.
     *
     * Resultat attendu :
     * epsilonClosure({q0}) = {q0, q1, q2}
     *
     * On doit donc retrouver les trois etats.
     */
    @Test
    void testEpsilonClosure() {

        // Creer un automate non deterministe vide.
        NFA nfa = new NFA();

        // Creer trois etats : q0, q1 et q2.
        int q0 = nfa.newState();
        int q1 = nfa.newState();
        int q2 = nfa.newState();

        // Ajouter une transition epsilon de q0 vers q1.
        nfa.addEpsilon(q0, q1);

        // Ajouter une transition epsilon de q1 vers q2.
        nfa.addEpsilon(q1, q2);

        // Creer l'objet qui calcule la determinisation.
        Determinizer determinizer = new Determinizer();

        // Verifier qu'en partant de q0, on peut atteindre
        // q0 (sans bouger), q1 et q2 par transitions epsilon.
        assertEquals(
                Set.of(q0, q1, q2),
                determinizer.epsilonClosure(nfa, Set.of(q0))
        );
    }

    /**
     * TEST 2 : Verifier la gestion d'un cycle epsilon.
     *
     * On construit le NFA suivant :
     *
     * q0 --epsilon--> q1
     * q1 --epsilon--> q0
     *
     * Les deux etats forment un cycle.
     *
     * Resultat attendu :
     * epsilonClosure({q0}) = {q0, q1}
     *
     * L'algorithme ne doit pas tourner indefiniment.
     * Chaque etat doit etre visite au maximum une fois.
     */
    @Test
    void testEpsilonCycle() {

        // Creer un NFA vide.
        NFA nfa = new NFA();

        // Creer deux etats.
        int q0 = nfa.newState();
        int q1 = nfa.newState();

        // Transition epsilon de q0 vers q1.
        nfa.addEpsilon(q0, q1);

        // Transition epsilon de q1 vers q0.
        // Cela cree un cycle entre les deux etats.
        nfa.addEpsilon(q1, q0);

        Determinizer determinizer = new Determinizer();

        // Verifier que la fermeture contient seulement
        // q0 et q1, sans repetition ni boucle infinie.
        assertEquals(
                Set.of(q0, q1),
                determinizer.epsilonClosure(nfa, Set.of(q0))
        );
    }

    /**
     * TEST 3 : Verifier la determinisation d'un NFA simple.
     *
     * On construit le NFA suivant :
     *
     * -> q0 --a--> ((q1))
     *
     * q0 est l'etat initial.
     * q1 est l'etat acceptant (final).
     *
     * Ce NFA reconnait uniquement le caractere 'a'.
     *
     * On verifie apres determinisation :
     * 1. L'etat initial du DFA n'est pas acceptant.
     * 2. La lecture de 'a' mene a un etat acceptant.
     * 3. La lecture de 'b' ne possede aucune transition.
     */
    @Test
    void testSimpleCharacter() {

        // Creer le NFA.
        NFA nfa = new NFA();

        // Creer les deux etats.
        int q0 = nfa.newState();
        int q1 = nfa.newState();

        // Definir q0 comme etat initial.
        nfa.setStart(q0);

        // Definir q1 comme etat acceptant.
        nfa.addAccepting(q1);

        // Ajouter la transition : q0 --a--> q1.
        nfa.addTransition(q0, 'a', q1);

        // Transformer le NFA en DFA.
        DFA dfa = new Determinizer().determinize(nfa);

        // Recuperer l'etat initial du DFA.
        int start = dfa.getStart();

        // Lire le caractere 'a' depuis l'etat initial.
        int afterA = dfa.next(start, 'a');

        // L'etat initial ne doit pas etre acceptant.
        assertFalse(dfa.isAccepting(start));

        // Apres avoir lu 'a', le DFA doit etre
        // dans un etat acceptant.
        assertTrue(dfa.isAccepting(afterA));

        // Le caractere 'b' n'existe pas dans les transitions.
        // Dans notre DFA, -1 signifie aucune transition.
        assertEquals(-1, dfa.next(start, 'b'));
    }

    /**
     * TEST 4 : Verifier la combinaison d'une transition
     * litterale et d'une transition joker '.'.
     *
     * On construit le NFA suivant :
     *
     * q0 --epsilon--> q1
     *
     * q1 --a--> q2 (final)
     * q1 --.--> q3 (final)
     *
     * Le joker '.' reconnait n'importe quel caractere.
     *
     * Si on lit 'a' :
     * - la transition litterale 'a' est possible ;
     * - la transition joker '.' est aussi possible.
     * Le DFA doit donc representer {q2, q3}.
     *
     * Si on lit 'b' :
     * - seule la transition joker '.' est possible.
     * Le DFA doit donc representer {q3}.
     *
     * On verifie que ces deux resultats sont
     * acceptants mais correspondent a des etats
     * DFA differents.
     */
    @Test
    void testWildcardAndLiteral() {

        // Creer le NFA.
        NFA nfa = new NFA();

        // Creer quatre etats.
        int q0 = nfa.newState();
        int q1 = nfa.newState();
        int q2 = nfa.newState();
        int q3 = nfa.newState();

        // q0 est l'etat initial.
        nfa.setStart(q0);

        // Depuis q0, on atteint q1 sans lire
        // de caractere.
        nfa.addEpsilon(q0, q1);

        // Depuis q1, le caractere 'a' mene vers q2.
        nfa.addTransition(q1, 'a', q2);

        // Depuis q1, le joker '.' mene vers q3.
        // Il reconnait notamment 'a', 'b', 'z', etc.
        nfa.addAny(q1, q3);

        // Les deux destinations sont acceptantes.
        nfa.addAccepting(q2);
        nfa.addAccepting(q3);

        // Convertir le NFA en DFA.
        DFA dfa = new Determinizer().determinize(nfa);

        // Recuperer l'etat initial du DFA.
        int start = dfa.getStart();

        // Lire 'a' : active la transition litterale
        // et la transition joker.
        int afterA = dfa.next(start, 'a');

        // Lire 'b' : active uniquement le joker.
        int afterB = dfa.next(start, 'b');

        // La lecture de 'a' doit etre acceptee.
        assertTrue(dfa.isAccepting(afterA));

        // La lecture de 'b' doit aussi etre acceptee.
        assertTrue(dfa.isAccepting(afterB));

        // Les sous-ensembles NFA obtenus sont differents :
        // apres 'a' : {q2, q3}
        // apres 'b' : {q3}
        // Ils doivent donc correspondre a deux
        // etats DFA distincts.
        assertNotEquals(afterA, afterB);

        // Apres avoir lu 'a', aucune transition
        // supplementaire sur 'z' n'existe.
        assertEquals(-1, dfa.next(afterA, 'z'));
    }
}
