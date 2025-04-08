package oscars;

public final class Font {
    public static final String NONE = format("");

    public static final String BOLD = format("1");

    public static final String UNDERLINE = format("4");

    public static final String TITLE = BOLD + UNDERLINE;

    public static final String RED = format("31");

    public static final String GREEN = format("32");

    public static final String BROWN = format("33");

    public static final String CYAN = format("36");

    public static final String YELLOW = format("4;38;5;226");

    private static String format(String inCode) {
        return "\033[" + inCode + "m";
    }

    /** Format the title using bold and underline */
    public static String title(String inTitle) {
        return Font.BOLD + Font.UNDERLINE + inTitle + Font.NONE;
    }

    /** Create the formatted menu number for the zero-based index (add one to it) */
    public static String menuNumber(int inIndex) {
        return String.format("%s%2d: %s", Font.BROWN, inIndex + 1, Font.NONE);
    }
}