package org.daar;

public class RegexNode {
    public enum Type {
        LITERAL,
        WILDCARD,
        CONCAT,
        ALTERNATION,
        STAR
    }
   public Type type; // nature du noeud
  public char symbol; //signification uniquement pour literal
   public RegexNode left; // premier enfant pour concat, alternation, star
    public RegexNode right;// second enfant pour concat, alternation

//constructeur
private RegexNode(Type type, char symbol, RegexNode left, RegexNode right) {
    this.type = type;
    this.symbol = symbol;
    this.left = left;
    this.right = right;
}
public static RegexNode literal(char symbol) {
    return new RegexNode(Type.LITERAL, symbol, null, null);
}
public static RegexNode wildcard(char symbol) {
    return new RegexNode(Type.WILDCARD, symbol, null, null);
}
public static RegexNode alternation(RegexNode left, RegexNode right) {
   return new RegexNode(Type.ALTERNATION, '\0', left, right);
}
public static RegexNode star(RegexNode child) {
    return new RegexNode(Type.STAR, '\0', child, null);
}
public static RegexNode concatenation(RegexNode left, RegexNode right) {
    return new RegexNode(Type.CONCAT, '\0', left, right);
}

    public Type getType() {
        return type;
    }

    public char getSymbol() {
        return symbol;
    }

    public RegexNode getLeft() {
        return left;
    }

    public RegexNode getRight() {
        return right;
    }

}
