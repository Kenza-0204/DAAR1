package org.daar.automata;

import java.util.*;

public class DFA {

    // Transitions : etat -> caractere -> etat suivant
    private final List<Map<Character, Integer>> onchar =
            new ArrayList<>();

    // Transition par defaut (pour gerer le joker '.')
    // -1 signifie qu'il n'y a pas de transition
    private final List<Integer> onAny = new ArrayList<>();

    // Ensemble des etats acceptants
    private final Set<Integer> accepting = new HashSet<>();

    // Etat initial
    private int start = -1;

    // 1. Creer un nouvel etat
    public int newState() {
        onchar.add(new HashMap<>());
        onAny.add(-1);

        return onchar.size() - 1;
    }

    // 2. Definir l'etat initial
    public void setStart(int state) {
        checkState(state);
        start = state;
    }

    // 3. Recuperer l'etat initial
    public int getStart() {
        if (start < 0) {
            throw new IllegalStateException(
                    "Etat initial non defini"
            );
        }
        return start;
    }

    // 4. Ajouter un etat acceptant
    public void addAccepting(int state) {
        checkState(state);
        accepting.add(state);
    }

    // 5. Verifier si un etat est acceptant
    public boolean isAccepting(int state) {
        checkState(state);
        return accepting.contains(state);
    }

    // 6. Recuperer les etats acceptants
    public Set<Integer> getAccepting() {
        return Set.copyOf(accepting);
    }

    // 7. Ajouter une transition sur un caractere
    public void addTransition(int from, char c, int to) {
        checkState(from);
        checkState(to);

        onchar.get(from).put(c, to);
    }

    // 8. Definir la transition par defaut
    public void addAny(int from, int to) {
        checkState(from);
        checkState(to);

        onAny.set(from, to);
    }

    // 9. Recuperer les transitions explicites d'un etat
    public Map<Character, Integer> getTransitions(int state) {
        checkState(state);

        return Map.copyOf(onchar.get(state));
    }

    // 10. Recuperer la transition par defaut
    public int getAny(int state) {
        checkState(state);
        return onAny.get(state);
    }

    // 11. Obtenir l'etat suivant pour un caractere
    public int next(int state, char c) {
        checkState(state);

        // D'abord la transition explicite
        Integer target = onchar.get(state).get(c);

        if (target != null) {
            return target;
        }

        // Sinon, la transition par defaut
        return onAny.get(state);
    }

    // 12. Recuperer l'alphabet
    public Set<Character> getAlphabet() {
        Set<Character> alphabet = new HashSet<>();

        for (Map<Character, Integer> transitions : onchar) {
            alphabet.addAll(transitions.keySet());
        }

        return Set.copyOf(alphabet);
    }

    // 13. Nombre total d'etats
    public int size() {
        return onchar.size();
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