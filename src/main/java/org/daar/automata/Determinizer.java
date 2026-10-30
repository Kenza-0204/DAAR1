package org.daar.automata;

import java.util.*;

public class Determinizer {

    /*
     * 1. FERMETURE EPSILON
     *
     * A partir d'un ensemble d'etats, trouver tous les
     * etats accessibles en suivant uniquement des
     * transitions epsilon.
     *
     * Les etats de depart sont inclus dans le resultat.
     */
    public Set<Integer> epsilonClosure(
            NFA nfa,
            Set<Integer> states
    ) {
        Set<Integer> closure = new HashSet<>(states);
        Deque<Integer> stack = new ArrayDeque<>(states);

        while (!stack.isEmpty()) {
            int current = stack.pop();

            for (int next : nfa.getEpsilon(current)) {
                // add retourne false si next est deja present
                if (closure.add(next)) {
                    stack.push(next);
                }
            }
        }

        return Set.copyOf(closure);
    }
    /*
     * 2. MOVE
     *
     * Trouver tous les etats accessibles en lisant
     * le caractere c depuis les etats de "states".
     *
     * On tient compte :
     * - des transitions sur c
     * - des transitions joker '.'
     */
    public Set<Integer> move(
            NFA nfa,
            Set<Integer> states,
            char c
    ) {
        Set<Integer> result = new HashSet<>();

        for (int state : states) {
            result.addAll(nfa.getTransitions(state, c));
            result.addAll(nfa.getAny(state));
        }

        return Set.copyOf(result);
    }

    /*
     * Trouver les destinations des transitions joker
     * uniquement.
     *
     * Elles serviront de transition par defaut du DFA.
     */
    private Set<Integer> moveAny(
            NFA nfa,
            Set<Integer> states
    ) {
        Set<Integer> result = new HashSet<>();

        for (int state : states) {
            result.addAll(nfa.getAny(state));
        }

        return Set.copyOf(result);
    }

    /*
     * Enregistrer un nouvel etat DFA si l'ensemble
     * d'etats NFA n'a pas encore ete rencontre.
     *
     * Retourne le numero de l'etat DFA correspondant.
     */
    private int registerState(
            NFA nfa,
            DFA dfa,
            Set<Integer> subset,
            Map<Set<Integer>, Integer> ids,
            Queue<Set<Integer>> pending
    ) {
        Integer existing = ids.get(subset);

        if (existing != null) {
            return existing;
        }

        int id = dfa.newState();
        ids.put(subset, id);
        pending.add(subset);

        // Un etat DFA est final si son ensemble
        // contient au moins un etat final du NFA.
        for (int state : subset) {
            if (nfa.isAccepting(state)) {
                dfa.addAccepting(id);
                break;
            }
        }

        return id;
    }

    /*
     * 3. DETERMINISATION
     *
     * Transformer le NFA en DFA en utilisant
     * la construction par sous-ensembles.
     */
    public DFA determinize(NFA nfa) {

        DFA dfa = new DFA();

        // Associe chaque ensemble NFA a un etat DFA.
        Map<Set<Integer>, Integer> ids = new HashMap<>();

        // Etats DFA qu'il reste a explorer.
        Queue<Set<Integer>> pending = new ArrayDeque<>();

        // Etape A : calcul de l'etat initial DFA
        Set<Integer> initialSubset = epsilonClosure(nfa, Set.of(nfa.getStart())
        );

        int initialId = registerState(
                nfa, dfa, initialSubset, ids, pending
        );

        dfa.setStart(initialId);

        // Etape B : explorer tous les etats DFA
        while (!pending.isEmpty()) {

            Set<Integer> currentSubset = pending.remove();
            int currentId = ids.get(currentSubset);

            // Etape C : traiter les transitions joker.
            // Elles forment la transition par defaut.
            Set<Integer> anySubset = epsilonClosure(
                    nfa,
                    moveAny(nfa, currentSubset)
            );

            if (!anySubset.isEmpty()) {
                int targetId = registerState(
                        nfa, dfa, anySubset, ids, pending
                );

                dfa.addAny(currentId, targetId);
            }
            // Etape D : traiter chaque caractere litteral
            // present dans l'alphabet du NFA.
            for (char c : nfa.getAlphabet()) {

                Set<Integer> nextSubset = epsilonClosure(nfa, move(nfa, currentSubset, c));

                // Sans destination, aucune transition.
                if (nextSubset.isEmpty()) {
                    continue;
                }

                int targetId = registerState(
                        nfa, dfa, nextSubset, ids, pending
                );

                dfa.addTransition(currentId, c, targetId);
            }
        }

        // Etape E : retourner l'automate deterministe
        return dfa;
    }
}
