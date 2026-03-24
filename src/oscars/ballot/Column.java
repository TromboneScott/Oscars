package oscars.ballot;

import java.util.Objects;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import oscars.file.XMLFile;

/** A column from the ballot - Immutable */
public final class Column {
    private static final ImmutableMap<String, Column> MAP = XMLFile.readDefinitionsFile()
            .getChildren("column").stream().map(Column::new)
            .collect(ImmutableMap.toImmutableMap(Column::name, column -> column));

    /** All the columns in ballot order */
    static final ImmutableList<Column> ALL = ImmutableList.copyOf(MAP.values());

    /** All the categories (columns with nominees) in ballot order */
    public static final ImmutableList<Column> CATEGORIES = ALL.stream()
            .filter(column -> !column.nominees().isEmpty())
            .collect(ImmutableList.toImmutableList());

    public static final Column FIRST_NAME = of("First Name");

    public static final Column LAST_NAME = of("Last Name");

    public static final Column TIME = of("Time");

    static final Column TIMESTAMP = of("Timestamp");

    static final Column EMAIL = of("EMail");

    private final String name;

    private final ImmutableList<String> nominees;

    private Column(Element inColumn) {
        name = Objects.requireNonNull(inColumn.getAttributeValue("name"),
                "Definitions file missing column attribute: name");
        nominees = inColumn.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name"))
                .collect(ImmutableList.toImmutableList());
    }

    /** The name of this Column */
    public final String name() {
        return name;
    }

    public ImmutableList<String> nominees() {
        return nominees;
    }

    /** Get the Column instance for the given header */
    public static Column of(String inHeader) {
        return Objects.requireNonNull(MAP.get(inHeader), "Column not defined: " + inHeader);
    }

    public Element toDOM() {
        return new Element("category").setAttribute("name", name());
    }
}