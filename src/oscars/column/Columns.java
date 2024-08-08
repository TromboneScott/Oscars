package oscars.column;

/** Specific columns that must exist on the survey */
public final class Columns {
    public static final Column TIMESTAMP = Column.of("Timestamp");

    public static final Column FIRST_NAME = Column.of("First Name");

    public static final Column LAST_NAME = Column.of("Last Name");

    public static final Column TIME = Column.of("Time");

    public static final Column EMAIL = Column.of("EMail");

    private Columns() {
    }
}