package oscars.column;

/** The required columns in the survey - Immutable */
public final class DataColumn {
    public static final Column TIMESTAMP = of("Timestamp");

    public static final Column FIRST_NAME = of("First Name");

    public static final Column LAST_NAME = of("Last Name");

    public static final Column TIME = of("Time");

    public static final Column EMAIL = of("EMail");

    private static Column of(String inHeader) {
        return Column.of(inHeader, Column.ALL);
    }

    private DataColumn() {
    }
}