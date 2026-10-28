
package org.daar;

public class RegexParser {

    // Expression reguliere a analyser
    private String regex;

    // Position actuelle dans l'expression
    private int position;

    // Methode principale du parseur
    public RegexNode parse(String input) {

        if (input == null || input.isEmpty()) {
            throw new RegexException(
                    "Expression reguliere vide", 0
            );
        }

        regex = input;
        position = 0;

        RegexNode result = parseAlternation();

        // Verifier que toute l'expression a ete lue
        if (position != regex.length()) {
            throw error("Caractere inattendu");
        }

        return result;
    }

    // ALTERNATION : a|b
    // Priorite la plus faible
    private RegexNode parseAlternation() {

        RegexNode left = parseConcat();

        while (verify('|')) {
            position++;

            RegexNode right = parseConcat();

            // Utilisation de TA methode statique
            left = RegexNode.alternation(left, right);
        }

        return left;
    }

    // CONCATENATION : ab
    // Priorite intermediaire
    private RegexNode parseConcat() {

        if (!startsAtom()) {
            throw error("Expression attendue");
        }

        RegexNode left = parseRepeat();

        while (startsAtom()) {

            RegexNode right = parseRepeat();

            // Utilisation de TA methode statique
            left = RegexNode.concatenation(left, right);
        }

        return left;
    }

    // REPETITION : a*
    // Priorite la plus forte
    private RegexNode parseRepeat() {

        RegexNode node = parseAtom();

        while (verify('*')) {
            position++;

            // Utilisation de TA methode statique
            node = RegexNode.star(node);
        }

        return node;
    }

    // Lettre, joker ou expression entre parentheses
    private RegexNode parseAtom() {

        if (position >= regex.length()) {
            throw error("Fin inattendue");
        }

        char c = regex.charAt(position);

        // Cas 1 : lettre ASCII
        if (isAsciiLetter(c)) {
            position++;

            return RegexNode.literal(c);
        }

        // Cas 2 : joker .
        if (c == '.') {
            position++;

            return RegexNode.wildcard('.');
        }

        // Cas 3 : parentheses
        if (c == '(') {
            position++;

            RegexNode inside = parseAlternation();

            if (!verify(')')) {
                throw error(
                        "Parenthese fermante manquante"
                );
            }

            position++;

            return inside;
        }

        throw error("Symbole invalide");
    }

    // Verifie si une nouvelle sous-expression commence
    private boolean startsAtom() {

        if (position >= regex.length()) {
            return false;
        }

        char c = regex.charAt(position);

        return isAsciiLetter(c)
                || c == '.'
                || c == '(';
    }

    // Verifie le caractere courant
    private boolean verify(char expected) {

        return position < regex.length()
                && regex.charAt(position) == expected;
    }

    // Reconnaissance des lettres ASCII
    private boolean isAsciiLetter(char c) {

        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }

    // Construit une exception avec la position
    private RegexException error(String message) {

        return new RegexException(message, position);
    }
}
