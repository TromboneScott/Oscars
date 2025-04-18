package oscars;

import java.util.regex.Pattern;

/** Apply ANSI escape codes for text colors and graphics modes - Immutable */
public enum Font {
    BOLD("1"),
    UNDERLINE("4"),
    INVERSE("7"),
    GREEN("32"),
    BROWN("33"),
    CYAN("36"),
    WHITE("37"),
    YELLOW("38;5;226");

    private static final String RESET = "\033[m";

    private final String tag;

    private Font(String inCode) {
        tag = new StringBuilder(RESET).insert(RESET.length() - 1, inCode).toString();
    }

    /** Apply this Font to the given text */
    public String apply(String inText) {
        return tag + inText.replaceAll(Pattern.quote(RESET), RESET + tag) + RESET;
    }

    /** Format the title using BOLD and UNDERLINE */
    public static String title(String inTitle) {
        return BOLD.apply(UNDERLINE.apply(inTitle));
    }

    /** Create the formatted menu number for the zero-based index (adding one to it) */
    public static String menuNumber(int inIndex) {
        return BROWN.apply(String.format("%2d: ", inIndex + 1));
    }
}