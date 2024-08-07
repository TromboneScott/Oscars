package oscars;

import java.util.Objects;

/** A column from the survey - Immutable but can be extended */
public class Column {
    private final String header;

    public Column(String inHeader) {
        header = inHeader;
    }

    /** The header of this Column */
    public final String header() {
        return header;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(header);
    }

    @Override
    public final boolean equals(Object inOther) {
        return this == inOther
                || inOther instanceof Column && Objects.equals(header, ((Column) inOther).header);
    }

    @Override
    public final String toString() {
        return header;
    }
}