package oscars;

import java.util.regex.Pattern;

/** Apply ANSI escape codes for text colors and graphics modes - Immutable */
public final class Font {
    private static final String RESET = new Font("").tag;

    public static final Font BOLD = new Font("1");

    public static final Font UNDERLINE = new Font("4");

    public static final Font GREEN = new Font("32");

    public static final Font BROWN = new Font("33");

    public static final Font CYAN = new Font("36");

    public static final Font YELLOW = new Font("38;5;226");

    private final String tag;

    private Font(String inCode) {
        tag = "\033[" + inCode + "m";
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