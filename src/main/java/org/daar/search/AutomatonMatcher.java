package org.daar.search;

import org.daar.automata.DFA;

public class AutomatonMatcher implements LineMatcher {

    private final DFA dfa;

    public AutomatonMatcher(DFA dfa) {

        if (dfa == null) {
            throw new IllegalArgumentException("L'AFD ne peut pas être null");
        }

        this.dfa = dfa;
    }

    @Override
    public boolean containsMatch(String line) {

        if (line == null) {
            throw new IllegalArgumentException("La ligne ne peut pas être null");
        }

        int start = dfa.getStart();

        // Si le motif accepte le mot vide, il correspond à toutes les lignes
        if (dfa.isAccepting(start)) {
            return true;
        }

        // On essaie de trouver le motif à partir de chaque position
        for (int i = 0; i < line.length(); i++) {

            int state = start;

            // On parcourt la ligne à partir de la position i
            for (int j = i; j < line.length(); j++) {

                char c = line.charAt(j);
                state = dfa.next(state, c);

                // Pas de transition : on essaie à la position suivante
                if (state == -1) {
                    break;
                }

                // Une partie de la ligne correspond au motif
                if (dfa.isAccepting(state)) {
                    return true;
                }
            }
        }

        // Aucune correspondance trouvée
        return false;
    }
}