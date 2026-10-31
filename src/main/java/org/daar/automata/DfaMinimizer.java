
package org.daar.automata;

import java.util.*;

public class DfaMinimizer {

    private record Signature(int group, List<Integer> transitions) {}

    public DFA minimize(DFA dfa) {
        if (dfa == null) {
            throw new IllegalArgumentException("DFA null");
        }

        int sink = dfa.size(); // Etat puits pour les transitions manquantes
        int start = dfa.getStart();

        List<Character> alphabet = new ArrayList<>(dfa.getAlphabet());
        Collections.sort(alphabet);

        // On garde uniquement les états accessibles
        Set<Integer> reachable = new TreeSet<>();
        Queue<Integer> queue = new ArrayDeque<>();

        reachable.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            int state = queue.remove();

            for (char c : alphabet) {
                int next = nextState(dfa, state, c, sink);

                if (reachable.add(next)) {
                    queue.add(next);
                }
            }

            int next = otherState(dfa, state, sink);

            if (reachable.add(next)) {
                queue.add(next);
            }
        }

        int[] groups = new int[dfa.size() + 1];
        Arrays.fill(groups, -1);

        // Au départ, on sépare les états finaux des autres
        for (int state : reachable) {
            groups[state] = state != sink && dfa.isAccepting(state) ? 1 : 0;
        }

        boolean changed;

        do {
            changed = false;

            Map<Signature, Integer> partitions = new HashMap<>();
            int[] newGroups = new int[groups.length];
            Arrays.fill(newGroups, -1);

            for (int state : reachable) {
                List<Integer> transitions = new ArrayList<>();

                for (char c : alphabet) {
                    int next = nextState(dfa, state, c, sink);
                    transitions.add(groups[next]);
                }

                // On prend aussi en compte les autres caractères
                int other = otherState(dfa, state, sink);
                transitions.add(groups[other]);

                Signature signature = new Signature(groups[state], transitions);

                if (!partitions.containsKey(signature)) {
                    partitions.put(signature, partitions.size());
                }

                newGroups[state] = partitions.get(signature);
            }

            if (!Arrays.equals(groups, newGroups)) {
                changed = true;
            }

            groups = newGroups;

        } while (changed);

        // On construit le nouvel automate avec un état par groupe
        DFA result = new DFA();

        int count = Arrays.stream(groups).max().orElse(-1) + 1;

        for (int i = 0; i < count; i++) {
            result.newState();
        }

        result.setStart(groups[start]);

        Set<Integer> finalGroups = new HashSet<>();

        for (int state : reachable) {
            if (state != sink && dfa.isAccepting(state)) {
                finalGroups.add(groups[state]);
            }
        }

        for (int group : finalGroups) {
            result.addAccepting(group);
        }

        // Un représentant suffit pour reconstruire chaque groupe
        Set<Integer> done = new HashSet<>();

        for (int state : reachable) {
            int group = groups[state];

            if (!done.add(group)) {
                continue;
            }

            for (char c : alphabet) {
                int next = nextState(dfa, state, c, sink);
                result.addTransition(group, c, groups[next]);
            }

            int other = otherState(dfa, state, sink);
            result.addAny(group, groups[other]);
        }

        return result;
    }

    private int nextState(DFA dfa, int state, char c, int sink) {
        if (state == sink) {
            return sink;
        }

        int next = dfa.next(state, c);
        return next == -1 ? sink : next;
    }

    private int otherState(DFA dfa, int state, int sink) {
        if (state == sink) {
            return sink;
        }

        int next = dfa.getAny(state);
        return next == -1 ? sink : next;
    }
}
