package org.daar.search;

import org.daar.automata.DFA;

/**
 * Recherche d'une occurrence d'un motif n'importe où dans une ligne,
 * à l'aide d'un automate fini déterministe (DFA).
 *
 * L'automate reconnaît exactement le langage du motif.
 *
 * Méthode (cours DAAR 1) :
 * On lance l'automate depuis chaque position de départ i.
 *
 * Dès qu'un état acceptant est atteint, la sous-chaîne
 * line[i...j] correspond au motif recherché.
 *
 * Complexité :
 * Dans le pire cas, O(n²) transitions pour une ligne
 * de longueur n, en considérant l'automate fixé.
 */
public class AutomatonMatcher implements LineMatcher {

    // Automate déterministe utilisé pour rechercher le motif.
    private final DFA dfa;

    /**
     * Constructeur.
     *
     * @param dfa automate déterministe du motif recherché
     */
    public AutomatonMatcher(DFA dfa) {

        // Vérifier que l'automate existe.
        if (dfa == null) {
            throw new IllegalArgumentException(
                    "L'AFD ne peut pas être null"
            );
        }

        this.dfa = dfa;
    }

    /**
     * Vérifie si une ligne contient au moins une occurrence du motif.
     *
     * @param line ligne dans laquelle effectuer la recherche
     * @return true si une sous-chaîne est reconnue par le DFA,
     *         false sinon
     */
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

        /*
         * Si l'état initial est acceptant, cela signifie
         * que le motif reconnaît le mot vide.
         *
         * Le mot vide est présent dans toutes les lignes,
         * même dans une ligne vide.
         */
        if (dfa.isAccepting(start)) {
            return true;
        }

        /*
         * Essayer de reconnaître le motif depuis chaque
         * position i de la ligne.
         *
         * Exemple :
         * Motif : "ab"
         * Ligne : "xxabyy"
         *
         * i = 0 : on commence au premier 'x'
         * i = 1 : on commence au deuxième 'x'
         * i = 2 : on commence au 'a' -> correspondance !
         */
        for (int i = 0; i < line.length(); i++) {

            // Chaque tentative commence à l'état initial.
            int state = start;

            /*
             * Parcourir les caractères à partir
             * de la position i.
             */
            for (int j = i; j < line.length(); j++) {

                // Lire le caractère actuel.
                char c = line.charAt(j);

                // Suivre la transition du DFA.
                state = dfa.next(state, c);

                /*
                 * -1 signifie qu'aucune transition n'existe.
                 *
                 * La recherche depuis cette position i
                 * a échoué : on passe à la position suivante.
                 */
                if (state == -1) {
                    break;
                }

                /*
                 * Si l'état atteint est acceptant,
                 * on a trouvé une sous-chaîne correspondant
                 * à l'expression régulière.
                 */
                if (dfa.isAccepting(state)) {
                    return true;
                }
            }
        }

        /*
         * On a essayé toutes les positions de départ,
         * sans trouver de correspondance.
         */
        return false;
    }
}