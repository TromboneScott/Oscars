package oscars;

/** Columns from the survey that have to be identified by specific names */
public enum Column {
    TIMESTAMP("Timestamp"),
    FIRST_NAME("First Name"),
    LAST_NAME("Last Name"),
    TIME("Time"),
    EMAIL("EMail");

    private final String header;

    private Column(String inHeader) {
        header = inHeader;
    }

    public String getHeader() {
        return header;
    }
}