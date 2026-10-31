package org.daar.kmp;

/**
 * Construction de la table CarryOver de KMP
 * selon la convention du cours DAAR 2.
 *
 * Pour un motif de longueur m, la table contient m + 1 cases.
 *
 * C[0] = -1
 *
 * Pour chaque position i :
 *
 * f(i) represente la longueur du plus long bord propre
 * du prefixe pattern[0...i).
 *
 * Un bord est un prefixe qui est aussi un suffixe.
 *
 * Si pattern[i] == pattern[f(i)], reprendre en f(i)
 * echouerait sur le meme caractere.
 *
 * On herite alors de :
 * C[i] = C[f(i)]
 *
 * Sinon :
 * C[i] = f(i)
 *
 * Enfin :
 * C[m] = f(m)
 *
 * Exemples du cours :
 *
 * mami     -> [-1, 0, -1, 1, 0]
 * mamamia  -> [-1, 0, -1, 0, -1, 3, 0, 0]
 */
public class CarryOverBuilder {

    /**
     * Construit la table CarryOver d'un motif.
     *
     * @param pattern motif recherche
     * @return tableau CarryOver de taille m + 1
     */
    public int[] build(String pattern) {

        // Le motif ne peut pas etre null.
        if (pattern == null) {
            throw new IllegalArgumentException(
                    "Le motif ne peut pas etre null"
            );
        }

        // Calculer d'abord la table des bords.
        int[] f = borders(pattern);

        // Longueur du motif.
        int m = pattern.length();

        // CarryOver contient m + 1 cases.
        int[] carry = new int[m + 1];

        // Convention du cours :
        // -1 signifie qu'aucun repli n'est possible.
        carry[0] = -1;

        /*
         * Calcul des valeurs CarryOver.
         *
         * Si le caractere courant est identique
         * au caractere de la position de repli,
         * on saute directement au repli suivant.
         *
         * Sinon, on conserve la valeur du bord.
         */
        for (int i = 1; i < m; i++) {

            carry[i] =
                    pattern.charAt(i) == pattern.charAt(f[i])
                            ? carry[f[i]]
                            : f[i];
        }

        /*
         * La derniere case sert apres
         * une occurrence complete du motif.
         */
        if (m > 0) {
            carry[m] = f[m];
        }

        return carry;
    }

    /**
     * Construit la table des bords du motif.
     *
     * f[i] est la longueur du plus long bord propre
     * de pattern[0...i).
     *
     * f[0] = -1 par convention.
     *
     * Calcul en O(m), avec m la longueur du motif.
     */
    static int[] borders(String pattern) {

        int m = pattern.length();

        // Tableau des bords.
        int[] f = new int[m + 1];

        // Valeur initiale.
        f[0] = -1;

        // Longueur du bord courant.
        int k = -1;

        /*
         * Parcourir les caracteres du motif
         * pour calculer les longueurs des bords.
         */
        for (int i = 0; i < m; i++) {

            /*
             * Tant que les caracteres ne correspondent pas,
             * revenir au bord precedent.
             */
            while (k >= 0 &&
                    pattern.charAt(k) != pattern.charAt(i)) {

                k = f[k];
            }

            // On a trouve un caractere correspondant,
            // ou on recommence depuis le debut.
            k++;

            // Enregistrer la longueur du bord.
            f[i + 1] = k;
        }

        return f;
    }
}