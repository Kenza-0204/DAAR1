package org.daar;

import org.daar.regex.RegexParser;
import org.daar.regex.RegexNode;

import org.daar.automata.NFA;
import org.daar.automata.DFA;
import org.daar.automata.ThompsonBuilder;
import org.daar.automata.Determinizer;
import org.daar.automata.DfaMinimizer;

import org.daar.search.AutomatonMatcher;
import org.daar.search.FileSearcher;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Le programme attend :
        // java ... <motif> <fichier>
        if (args.length != 2) {
            System.err.println(
                    "Usage : egrep <motif> <fichier>"
            );
            System.exit(2);
        }

        String pattern = args[0];
        String filePath = args[1];

        try {
            FileSearcher searcher = new FileSearcher();
            List<String> results;

            // Motif litteral : on utilise KMP de Nadine
            // avec le CarryOverBuilder de Kenza.
            if (pattern.matches("[a-zA-Z]+")) {

                results = searcher.search(
                        filePath, pattern
                );

            } else {

                // 1. Regex -> AST
                RegexNode ast =
                        new RegexParser().parse(pattern);

                // 2. AST -> NFA
                NFA nfa =
                        new ThompsonBuilder().build(ast);

                // 3. NFA -> DFA
                DFA dfa =
                        new Determinizer().determinize(nfa);

                // 4. Minimisation du DFA
                DFA minimized =
                        new DfaMinimizer().minimize(dfa);

                // 5. Recherche avec DFA
                AutomatonMatcher matcher =
                        new AutomatonMatcher(minimized);

                // 6. Lecture du fichier
                results = searcher.search(
                        filePath, matcher
                );
            }

            // Afficher uniquement les lignes trouvees.
            for (String line : results) {
                System.out.println(line);
            }

        } catch (Exception e) {
            System.err.println(
                    "Erreur : " + e.getMessage()
            );
            System.exit(2);
        }
    }
}