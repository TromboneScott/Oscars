package oscars;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Reads the columns from the definitions file - Immutable */
public final class Columns {
    private static final String DEFINITIONS_FILE = "definitions.xml";

    private static final List<Category> ALL = Collections.unmodifiableList(read());

    private static final List<Category> CATEGORIES = Collections.unmodifiableList(ALL.stream()
            .filter(category -> !category.nominees().isEmpty()).collect(Collectors.toList()));

    private Columns() {
    }

    private static List<Category> read() {
        try {
            List<Category> all = Directory.DATA.getRootElement(DEFINITIONS_FILE)
                    .orElseThrow(() -> new RuntimeException("File not found"))
                    .getChildren("category").stream().map(Category::new)
                    .collect(Collectors.toList());
            Stream.of(DefinedColumn.values()).map(DefinedColumn::header).forEach(header -> all
                    .stream().filter(category -> category.header().equals(header)).findAny()
                    .orElseThrow(() -> new RuntimeException("Category not defined: " + header)));
            return all;
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + DEFINITIONS_FILE, e);
        }
    }

    /** All the columns in survey order */
    public static List<? extends Column> all() {
        return ALL;
    }

    /** All the award categories in order */
    public static List<Category> categories() {
        return CATEGORIES;
    }
}