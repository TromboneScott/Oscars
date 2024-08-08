package oscars.column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;

/** A column in the survey - Immutable but can be extended */
public class Column {
    private static final Map<String, Column> INSTANCES = new HashMap<>();

    /** All the columns in survey order */
    public static final List<? extends Column> ALL = Category.DEFINED;

    private final String header;

    Column(Element inElement) {
        header = Optional.ofNullable(inElement.getAttributeValue("name")).orElseThrow(
                () -> new RuntimeException("Category is missing required attribute: name"));
        if (INSTANCES.containsKey(header))
            throw new RuntimeException("Duplicate definitions found for category: " + header);
        INSTANCES.put(header, this);
    }

    /** Get the instance of this Column that has the given header */
    public static Column of(String inHeader) {
        return Optional.ofNullable(inHeader).map(INSTANCES::get)
                .orElseThrow(() -> new RuntimeException("Column not defined: " + inHeader));
    }

    /** The header of this Column */
    public final String header() {
        return header;
    }

    @Override
    public final String toString() {
        return header;
    }
}