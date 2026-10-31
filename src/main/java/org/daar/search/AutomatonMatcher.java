package org.daar.search;

import org.daar.automata.DFA;

public class AutomatonMatcher implements LineMatcher {

    // Automate déterministe utilisé pour rechercher le motif.
    private final DFA dfa;

    public AutomatonMatcher(DFA dfa) {

        // Vérifier que l'automate existe.
        if (dfa == null) {
            throw new IllegalArgumentException(
                    "L'AFD ne peut pas être null"
            );
        }

        this.dfa = dfa;
    }

    @Override
    public boolean containsMatch(String line) {

        // Une ligne null n'est pas valide.
        if (line == null) {
            throw new IllegalArgumentException(
                    "La ligne ne peut pas être null"
            );
        }

        // Récupérer l'état initial du DFA.
        int start = dfa.getStart();
        if (dfa.isAccepting(start)) {
            return true;
        }

        for (int i = 0; i < line.length(); i++) {

            // Chaque tentative commence à l'état initial.
            int state = start;

            for (int j = i; j < line.length(); j++) {

                // Lire le caractère actuel.
                char c = line.charAt(j);

                // Suivre la transition du DFA.
                state = dfa.next(state, c);

                if (state == -1) {
                    break;
                }

                if (dfa.isAccepting(state)) {
                    return true;
                }
            }
        }

        return false;
    }
}