
package org.daar.regex;

public class RegexException
        extends IllegalArgumentException {

    private final int position;

    public RegexException(
            String message,
            int position
    ) {
        super(message + " à la position " + position);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
