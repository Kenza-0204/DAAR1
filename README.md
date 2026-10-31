# DAAR – Projet 1 : clone de `egrep` avec support partiel des ERE

Projet de l'UE **DAAR** (Sorbonne Université, M1 Informatique), encadré par BM Bui-Xuan.

Ce programme Java reproduit le comportement de la commande `egrep` sur un sous-ensemble de la norme
[POSIX ERE](http://pubs.opengroup.org/onlinepubs/7908799/xbd/re.html). Il reçoit un motif et un fichier texte,
puis affiche, dans l'ordre, chaque ligne du fichier qui contient au moins une occurrence du motif.

Deux stratégies de recherche sont implémentées :

| Stratégie | Utilisée quand | Algorithmes |
|---|---|---|
| **Automate** (séance 1) | le motif contient un opérateur (`\|`, `*`, `.`, `(`, `)`) | arbre syntaxique → ε-NFA (Aho-Ullman / Thompson) → DFA (sous-ensembles) → DFA minimal → recherche |
| **KMP** (séance 2) | le motif ne contient que des lettres (`[a-zA-Z]+`) | table CarryOver + Knuth-Morris-Pratt |

---

## Sommaire

1. [Syntaxe supportée](#1-syntaxe-supportée)
2. [Prérequis](#2-prérequis)
3. [Compilation](#3-compilation)
4. [Exécution](#4-exécution)
5. [Exécution avec Docker](#5-exécution-avec-docker)
6. [Tests](#6-tests)
7. [Intégration continue](#7-intégration-continue)
8. [Architecture du code](#8-architecture-du-code)
9. [Validation par rapport à egrep](#9-validation-par-rapport-à-egrep)
10. [Limites connues](#10-limites-connues)
11. [Arborescence](#11-arborescence)
12. [Auteurs](#12-auteurs)

---

## 1. Syntaxe supportée

| Élément | Syntaxe | Exemple | Reconnaît |
|---|---|---|---|
| Lettre ASCII | `a`–`z`, `A`–`Z` | `Sargon` | le mot « Sargon » |
| Concaténation | `ab` | `th(e\|a)t` | « thet », « that » |
| Alternative | `a\|b` | `Babylon\|Assyria` | l'un ou l'autre |
| Étoile de Kleene | `a*` | `ab*c` | « ac », « abc », « abbc », … |
| Caractère universel | `.` | `Sarg.n` | « Sargon », « Sargan », … |
| Parenthèses | `( … )` | `S(a\|g\|r)*on` | « Son », « Sargon », … |

Priorités, de la plus forte à la plus faible : `*`, puis concaténation, puis `|`.
Par exemple, `a|bc*` se lit `a|(b(c*))`.

Les éléments hors de ce périmètre (`+`, `?`, `{n,m}`, `[...]`, `^`, `$`, `\`, chiffres, espaces, ponctuation) sont
**refusés** avec un message d'erreur qui indique la position fautive.

> `X+` peut toujours s'écrire `XX*`. Par exemple, `S(a|g|r)+on` équivaut à `S(a|g|r)(a|g|r)*on`.

---

## 2. Prérequis

| Outil | Version | Utilisation |
|---|---|---|
| JDK | **21** ou plus récent | compilation et exécution |
| Maven | 3.9+ (ou le wrapper `./mvnw` fourni) | compilation et tests |
| Docker + Docker Compose | facultatif | exécution sans installer Java |

Vérifier l'installation :

```bash
java -version
mvn -v
```

---

## 3. Compilation

Depuis la racine du projet (le dossier qui contient `pom.xml`) :

```bash
mvn -B clean package
```

Cette commande compile le code, **exécute les 71 tests** et produit les classes dans `target/classes`.

Pour compiler sans lancer les tests :

```bash
mvn -B clean package -DskipTests
```

Sans Maven installé, utilisez le wrapper : `./mvnw clean package` (Linux/macOS) ou `mvnw.cmd clean package` (Windows).

---

## 4. Exécution

```text
java -cp target/classes org.daar.Main <motif> <fichier>
```

Le motif doit être **entre guillemets**, sinon le shell interprète `|`, `*` et les parenthèses.

### Exemples sur le fichier fourni (`data/56667-0.txt`, *A History of Babylon*, Project Gutenberg)

```bash
# Motif littéral : recherche par KMP
java -cp target/classes org.daar.Main "Sargon" data/56667-0.txt

# Expression régulière : recherche par automate
java -cp target/classes org.daar.Main "S(a|g|r)*on" data/56667-0.txt
java -cp target/classes org.daar.Main "(Babylon|Assyria)" data/56667-0.txt
java -cp target/classes org.daar.Main "Sarg.n" data/56667-0.txt

# Compter les lignes trouvées
java -cp target/classes org.daar.Main "th(e|a)t" data/56667-0.txt | wc -l
```

Extrait de sortie pour `"S(a|g|r)*on"` :

```text
under the Sargonids--The policies of encouragement and
that empire's expansion, and the vacillating policy of the Sargonids
to Sargon of Akkad; but that marked the extreme limit of Babylonian
...
```

### Codes de retour

| Code | Cause |
|---|---|
| `0` | exécution normale (même si aucune ligne n'a été trouvée) |
| `2` | mauvais nombre d'arguments, motif invalide ou fichier illisible |

```bash
$ java -cp target/classes org.daar.Main "a+" data/56667-0.txt
Erreur : Caractere inattendu à la position 1
```

---

## 5. Exécution avec Docker

Le `Dockerfile` est **multi-étapes** :

1. l'image `maven:3.9-eclipse-temurin-21` compile le projet et **exécute les tests** : si un test échoue, l'image n'est pas construite ;
2. seules les classes compilées sont copiées dans une image légère `eclipse-temurin:21-jre`.

`compose.yaml` monte le dossier `data/` **en lecture seule** dans `/data` à l'intérieur du conteneur.

```bash
# Construire l'image
docker compose build

# Lancer une recherche (les arguments sont passés au programme)
docker compose run --rm egrep "S(a|g|r)*on" /data/56667-0.txt
docker compose run --rm egrep "Sargon" /data/56667-0.txt
```

Sans Compose :

```bash
docker build -t daar-egrep .
docker run --rm -v "$(pwd)/data:/data:ro" daar-egrep "Sargon" /data/56667-0.txt
```

Pour chercher dans un autre fichier, placez-le dans `data/` et utilisez son chemin sous `/data/`.

---

## 6. Tests

```bash
mvn -B clean test
```

Résultat attendu : `Tests run: 71, Failures: 0, Errors: 0, Skipped: 0`.

Les tests utilisent **JUnit 5**. Ceux qui ont besoin de fichiers les créent dans un dossier temporaire (`@TempDir`) :
ils ne dépendent ni de la machine ni du dossier `data/`.

| Classe de test | Nb | Ce qui est vérifié |
|---|---:|---|
| `RegexParserTest` | 9 | lettres, concaténation, alternative, étoile, joker, priorités, parenthèses, motifs invalides, position de l'erreur |
| `ThompsonBuilderTest` | 6 | nombre d'états et de transitions produits par chaque construction |
| `DeterminizerTest` | 4 | ε-fermeture, cycle d'ε-transitions, déterminisation, combinaison lettre + joker |
| `DfaMinimizerTest` | 5 | fusion des états équivalents, états inaccessibles, joker, entrée nulle |
| `AutomatonMatcherTest` | 10 | occurrence en début, milieu ou fin de ligne, absence, étoile et mot vide, joker, parenthèses |
| `CarryOverBuilderTest` | 9 | tables du cours (`mami`, `mamamia`, `chicha`, `Sargon`) et cas limites |
| `KmpMatcherTest` | 8 | présence, absence, motif vide, motif plus long que le texte, répétitions |
| `FileSearcherTest` | 6 | une ou plusieurs lignes, ordre, fichier vide, fichier inexistant |
| `RegexIntegrationTest` | 14 | chaîne complète de bout en bout, même résultat avec et sans minimisation, KMP + CarryOver, vrais fichiers |
| **Total** | **71** | |

Lancer une seule classe ou un seul test :

```bash
mvn test -Dtest=RegexIntegrationTest
mvn test -Dtest=CarryOverBuilderTest#testMamamia
```

---

## 7. Intégration continue

Le workflow GitHub Actions [`.github/workflows/tests.yml`](.github/workflows/tests.yml) s'exécute à chaque **push**
et à chaque **pull request** sur les branches `dev` et `main`. Il :

1. récupère le dépôt (`actions/checkout@v4`) ;
2. installe Java 21 Temurin, avec cache Maven (`actions/setup-java@v4`) ;
3. lance `mvn -B clean test`.

Le jeton GitHub du workflow n'a que le droit de lecture (`permissions: contents: read`).
Une pull request dont un test échoue est signalée en rouge avant toute fusion.

---

## 8. Architecture du code

```text
                  motif ∈ [a-zA-Z]+ ?
        ┌───────────── oui ─────────────┐
        │                               ▼
        │                    CarryOverBuilder → KmpMatcher ─┐
 Main ──┤                                                   ├─► FileSearcher → lignes trouvées
        │ non                                               │
        ▼                                                   │
 RegexParser → ThompsonBuilder → Determinizer → DfaMinimizer → AutomatonMatcher
   (AST)          (ε-NFA)           (DFA)        (DFA minimal)
```

| Paquetage | Classe | Rôle | Complexité |
|---|---|---|---|
| `org.daar` | `Main` | ligne de commande, choix de la stratégie | — |
| `org.daar.regex` | `RegexParser` | analyse descendante récursive → arbre syntaxique | O(\|r\|) |
| | `RegexNode` | nœud de l'arbre : `LITERAL`, `WILDCARD`, `CONCAT`, `ALTERNATION`, `STAR` | — |
| | `RegexException` | erreur de syntaxe avec sa position | — |
| `org.daar.automata` | `NFA` | automate non déterministe : ε-transitions, transitions par lettre, joker | — |
| | `ThompsonBuilder` | arbre → ε-NFA (méthode Aho-Ullman), au plus 2\|r\| états | O(\|r\|) |
| | `Determinizer` | ε-fermeture, `move`, construction des sous-ensembles | O(2^m) au pire |
| | `DFA` | automate déterministe avec transition par défaut pour `.` | — |
| | `DfaMinimizer` | états accessibles, état puits, raffinement de partitions (Moore) | O(n²·\|Σ\|) |
| `org.daar.kmp` | `CarryOverBuilder` | table des bords puis table CarryOver (convention du cours) | O(m) |
| | `KmpMatcher` | recherche d'un facteur sans revenir en arrière dans le texte | O(n + m) |
| `org.daar.search` | `LineMatcher` | interface : « cette ligne contient-elle le motif ? » | — |
| | `AutomatonMatcher` | lance le DFA depuis chaque position de la ligne | O(n²) au pire par ligne |
| | `FileSearcher` | lit le fichier ligne par ligne (`Files.lines`) et filtre | O(nombre de lignes) |

**Le joker `.`** n'est pas copié sur chaque lettre de l'alphabet. Chaque état du DFA a une **transition par défaut**
(`onAny`), utilisée quand aucune transition explicite ne correspond au caractère lu.

---

## 9. Validation par rapport à egrep

Les sorties ont été comparées ligne à ligne avec GNU `grep -E` 3.0 sur `data/56667-0.txt`
(13 309 lignes, 735 377 caractères) :

| Motif | Stratégie | Lignes egrep | Lignes projet | Identiques |
|---|---|---:|---:|:---:|
| `Sargon` | KMP | 29 | 29 | ✅ |
| `the` | KMP | 7952 | 7952 | ✅ |
| `Babylon` | KMP | 1178 | 1178 | ✅ |
| `Nebuchadnezzar` | KMP | 121 | 121 | ✅ |
| `xyzzyq` | KMP | 0 | 0 | ✅ |
| `S(a\|g\|r)(a\|g\|r)*on` | Automate | 29 | 29 | ✅ |
| `Sarg.n` | Automate | 29 | 29 | ✅ |
| `(Babylon\|Assyria)` | Automate | 1359 | 1359 | ✅ |
| `th(e\|a)t` | Automate | 1420 | 1420 | ✅ |
| `(ab)*c` | Automate | 8535 | 8535 | ✅ |
| `B.b.l.n` | Automate | 1178 | 1178 | ✅ |
| `S(a\|g\|r)*on` | Automate | 30 | 30 | ✅ |

Refaire la comparaison (Linux/macOS, ou Git Bash sous Windows) :

```bash
P="S(a|g|r)*on"
diff <(egrep "$P" data/56667-0.txt) \
     <(java -cp target/classes org.daar.Main "$P" data/56667-0.txt) \
  && echo "Sorties identiques"
```

L'analyse des performances (KMP, DFA, DFA minimisé, egrep) se trouve dans le rapport.

---

## 10. Limites connues

- **Périmètre ERE réduit** : `+`, `?`, intervalles, classes de caractères et ancres ne sont pas gérés ; ils sont refusés.
- **Pas d'options** comme egrep (`-i`, `-c`, `-n`, `-v`…) ni de lecture sur l'entrée standard.
- **Performance du DFA minimisé** : la minimisation ajoute un état puits. Comme `AutomatonMatcher` s'arrête seulement
  quand il n'y a plus de transition (`-1`), il lit alors chaque ligne jusqu'au bout depuis chaque position. Le résultat
  reste juste, mais la recherche est environ 50 fois plus lente qu'avec le DFA non minimisé (voir le rapport).
- **KMP** : la table CarryOver est recalculée pour chaque ligne au lieu d'une seule fois par motif.
- **Démarrage de la JVM** : de bout en bout, le programme met environ 1 s, contre environ 80 ms pour egrep.

---

## 11. Arborescence

```text
daar/
├── .github/workflows/tests.yml   # Intégration continue (GitHub Actions)
├── data/56667-0.txt              # Fichier de test (Project Gutenberg n° 56667)
├── src/
│   ├── main/java/org/daar/
│   │   ├── Main.java
│   │   ├── regex/                # RegexParser, RegexNode, RegexException
│   │   ├── automata/             # NFA, ThompsonBuilder, Determinizer, DFA, DfaMinimizer
│   │   ├── kmp/                  # CarryOverBuilder, KmpMatcher
│   │   └── search/               # LineMatcher, AutomatonMatcher, FileSearcher
│   └── test/java/org/daar/       # 71 tests JUnit 5 (même organisation)
├── Dockerfile                    # Image multi-étapes : compilation + tests, puis JRE
├── compose.yaml                  # Service "egrep", data/ monté en lecture seule
├── pom.xml                       # Maven : Java 21, JUnit 5.11, Surefire
├── mvnw, mvnw.cmd, .mvn/         # Maven Wrapper
└── README.md
```

---

## 12. Auteurs

- **BELBEY Nadine**
- **MANSOURI Kenza**


Projet réalisé en binôme pour l'UE DAAR, Sorbonne Université, 2026.
