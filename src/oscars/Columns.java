package oscars;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Reads the columns from the definitions file - Immutable */
public final class Columns {
    private static final String DEFINITIONS_FILE = "definitions.xml";

    private static final List<Category> DEFINED = Collections.unmodifiableList(read());

    /** All the columns in survey order */
    public static final List<? extends Column> ALL = DEFINED;

    /** All the award categories in order */
    public static final List<Category> CATEGORIES = Collections.unmodifiableList(DEFINED.stream()
            .filter(category -> !category.nominees().isEmpty()).collect(Collectors.toList()));

    public static final Column TIMESTAMP = requiredColumn("Timestamp");

    public static final Column FIRST_NAME = requiredColumn("First Name");

    public static final Column LAST_NAME = requiredColumn("Last Name");

    public static final Column TIME = requiredColumn("Time");

    public static final Column EMAIL = requiredColumn("EMail");

    private Columns() {
    }

    private static List<Category> read() {
        try {
            return Directory.DATA.getRootElement(DEFINITIONS_FILE)
                    .orElseThrow(() -> new RuntimeException("File not found"))
                    .getChildren("category").stream().map(Category::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + DEFINITIONS_FILE, e);
        }
    }

    /** Create a column that's guaranteed to be defined */
    private static final Column requiredColumn(String inHeader) {
        return Optional.ofNullable(inHeader).map(Column::new).filter(ALL::contains)
                .orElseThrow(() -> new RuntimeException(
                        inHeader + " - category not defined in: " + DEFINITIONS_FILE));
    }
}