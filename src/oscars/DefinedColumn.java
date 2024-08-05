package oscars;

/**
 * Columns from the survey that aren't categories with nominees. NOTE: The definitions file must
 * exactly match these headers.
 */
public enum DefinedColumn implements Column {
    TIMESTAMP("Timestamp"),
    FIRST_NAME("First Name"),
    LAST_NAME("Last Name"),
    TIME("Time"),
    EMAIL("EMail");

    private final String header;

    private DefinedColumn(String inHeader) {
        header = inHeader;
    }

    /** The header of this Column */
    public String header() {
        return header;
    }
}