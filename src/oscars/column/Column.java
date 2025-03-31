package oscars.column;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;

import oscars.file.Directory;
import oscars.file.XMLFile;

/** A column from the survey - Immutable */
public class Column {
    private static final XMLFile DEFINITIONS_FILE = new XMLFile(Directory.DATA, "definitions.xml");

    /** All the columns in survey order */
    public static final ImmutableList<Column> ALL = readFile().map(Column::new)
            .collect(ImmutableList.toImmutableList());

    private final String name;

    Column(Element inColumn) {
        name = Objects.requireNonNull(inColumn.getAttributeValue("name"),
                DEFINITIONS_FILE + " - Missing column attribute: name");
    }

    /** The name of this Column */
    public final String name() {
        return name;
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(Object inOther) {
        return this == inOther || inOther instanceof Column && name.equals(((Column) inOther).name);
    }

    /** Get the Column instance that has the given header from the Column list */
    static <T extends Column> T of(String inHeader, ImmutableList<T> inColumns) {
        return inColumns.stream().filter(column -> column.name().equals(inHeader)).findAny()
                .orElseThrow(() -> new NullPointerException("Column not defined: " + inHeader));
    }

    static Stream<Element> readFile() {
        try {
            return DEFINITIONS_FILE.read().orElseThrow(FileNotFoundException::new)
                    .getChildren("column").stream();
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + DEFINITIONS_FILE, e);
        }
    }
}