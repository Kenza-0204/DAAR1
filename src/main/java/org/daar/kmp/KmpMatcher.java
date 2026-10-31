package org.daar.kmp;

public class KmpMatcher {

    public boolean contains(String text, String word) {

        if (text == null || word == null) {
            throw new IllegalArgumentException("Le texte et le mot ne peuvent pas être null");
        }

        if (word.isEmpty()) {
            return true;
        }

        // On récupère la table CarryOver
        CarryOverBuilder builder = new CarryOverBuilder();
        int[] carryOver = builder.build(word);

        int i = 0; // Position dans le texte
        int j = 0; // Position dans le mot

        while (i < text.length()) {

            if (j == -1 || text.charAt(i) == word.charAt(j)) {
                i++;
                j++;

                // Le mot entier a été trouvé
                if (j == word.length()) {
                    return true;
                }

            } else {
                // On reprend à la position indiquée par CarryOver
                j = carryOver[j];
            }
        }

        return false;
    }
}