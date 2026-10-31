package org.daar.automata;

import org.daar.regex.RegexNode;

public class ThompsonBuilder {

    // Chaque morceau d'automate a un début et une fin
    private record Fragment(int start, int end) {}

    public NFA build(RegexNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Arbre regex null");
        }

        NFA nfa = new NFA();
        Fragment fragment = buildFragment(root, nfa);

        nfa.setStart(fragment.start());
        nfa.addAccepting(fragment.end());

        return nfa;
    }

    private Fragment buildFragment(RegexNode node, NFA nfa) {

        return switch (node.getType()) {

            case LITERAL -> {
                int start = nfa.newState();
                int end = nfa.newState();

                nfa.addTransition(start, node.getSymbol(), end);

                yield new Fragment(start, end);
            }

            case WILDCARD -> {
                int start = nfa.newState();
                int end = nfa.newState();

                nfa.addAny(start, end);

                yield new Fragment(start, end);
            }

            case CONCAT -> {
                Fragment left = buildFragment(node.getLeft(), nfa);
                Fragment right = buildFragment(node.getRight(), nfa);

                // On relie les deux morceaux
                nfa.addEpsilon(left.end(), right.start());

                yield new Fragment(left.start(), right.end());
            }

            case ALTERNATION -> {
                Fragment left = buildFragment(node.getLeft(), nfa);
                Fragment right = buildFragment(node.getRight(), nfa);

                int start = nfa.newState();
                int end = nfa.newState();

                // Deux chemins possibles
                nfa.addEpsilon(start, left.start());
                nfa.addEpsilon(start, right.start());

                nfa.addEpsilon(left.end(), end);
                nfa.addEpsilon(right.end(), end);

                yield new Fragment(start, end);
            }

            case STAR -> {
                Fragment child = buildFragment(node.getLeft(), nfa);

                int start = nfa.newState();
                int end = nfa.newState();

                // On peut passer directement à la fin (zéro répétition)
                nfa.addEpsilon(start, end);
                nfa.addEpsilon(start, child.start());

                // On peut recommencer ou s'arrêter
                nfa.addEpsilon(child.end(), child.start());
                nfa.addEpsilon(child.end(), end);

                yield new Fragment(start, end);
            }
        };
    }
}