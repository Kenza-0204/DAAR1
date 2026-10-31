package org.daar.automata;

import java.util.*;

public class NFA {

    // Transitions epsilon : sans lire de caractere
    private final List<Set<Integer>> epsilon = new ArrayList<>();

    // Transitions sur un caractere precis
    private final List<Map<Character, Set<Integer>>> onchar =
            new ArrayList<>();

    // Transitions avec le joker '.'
    private final List<Set<Integer>> onAny = new ArrayList<>();

    // Ensemble des etats acceptants
    private final Set<Integer> accepting = new HashSet<>();

    // Etat initial
    private int start = -1;

    // 1. Creer un nouvel etat
    public int newState() {
        epsilon.add(new HashSet<>());
        onchar.add(new HashMap<>());
        onAny.add(new HashSet<>());

        return epsilon.size() - 1;
    }

    // 2. Definir l'etat initial
    public void setStart(int state) {
        checkState(state);
        start = state;
    }

    // 3. Recuperer l'etat initial
    public int getStart() {
        return start;
    }

    // 4. Ajouter un etat final
    public void addAccepting(int state) {
        checkState(state);
        accepting.add(state);
    }

    // 5. Verifier si un etat est final
    public boolean isAccepting(int state) {
        checkState(state);
        return accepting.contains(state);
    }

    // 6. Recuperer les etats finaux
    public Set<Integer> getAccepting() {
        return Set.copyOf(accepting);
    }

    // 7. Ajouter une transition epsilon
    public void addEpsilon(int from, int to) {
        checkState(from);
        checkState(to);
        epsilon.get(from).add(to);
    }

    // 8. Ajouter une transition sur un caractere
    public void addTransition(int from, char c, int to) {
        checkState(from);
        checkState(to);
        onchar.get(from)
                .computeIfAbsent(c, k -> new HashSet<>())
                .add(to);
    }

    // 9. Ajouter une transition joker
    public void addAny(int from, int to) {
        checkState(from);
        checkState(to);
        onAny.get(from).add(to);
    }

    // 10. Recuperer les transitions epsilon
    public Set<Integer> getEpsilon(int state) {
        checkState(state);
        return Set.copyOf(epsilon.get(state));
    }

    // 11. Recuperer les transitions d'un caractere
    public Set<Integer> getTransitions(int state, char c) {
        checkState(state);

        return Set.copyOf(
                onchar.get(state).getOrDefault(c, Set.of())
        );
    }

    // 12. Recuperer les transitions joker
    public Set<Integer> getAny(int state) {
        checkState(state);
        return Set.copyOf(onAny.get(state));
    }

    // 13. Recuperer l'alphabet des transitions litterales
    public Set<Character> getAlphabet() {
        Set<Character> alphabet = new HashSet<>();

        for (Map<Character, Set<Integer>> transitions : onchar) {
            alphabet.addAll(transitions.keySet());
        }

        return alphabet;
    }

    // 14. Nombre total d'etats
    public int size() {
        return epsilon.size();
    }

    // Verifier qu'un etat existe
    private void checkState(int state) {
        if (state < 0 || state >= size()) {
            throw new IllegalArgumentException(
                    "Etat inexistant : " + state
            );
        }
    }
}
