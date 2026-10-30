package org.daar.kmp;

public class KmpMatcher {

    public boolean contains(String text, String word) {

        if (text == null || word == null) {
            throw new IllegalArgumentException("Le texte et le mot ne peuvent pas être null");
        }

        if (word.isEmpty()) {
            return true;
        }

        // On calcule le tableau LPS pour éviter les comparaisons inutiles
        int[] lps = calculateLps(word);

        int i = 0; // Position dans le texte
        int j = 0; // Position dans le mot

        while (i < text.length()) {

            if (text.charAt(i) == word.charAt(j)) {
                i++;
                j++;

                // On a trouvé le mot entier
                if (j == word.length()) {
                    return true;
                }

            } else {
                // On utilise LPS pour savoir où reprendre dans le mot
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return false;
    }

    private int[] calculateLps(String word) {

        int[] lps = new int[word.length()];

        int len = 0;
        int i = 1;

        while (i < word.length()) {

            if (word.charAt(i) == word.charAt(len)) {
                len++;
                lps[i] = len;
                i++;

            } else {
                // On cherche un préfixe plus court si les caractères sont différents
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }
}