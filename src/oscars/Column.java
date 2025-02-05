package oscars;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** A column from the survey - Immutable */
public final class Column {
    private static final ImmutableMap<String, Column> INSTANCES = read(
            new XMLFile(Directory.DATA, "definitions.xml"));

    /** All the columns in survey order */
    public static final ImmutableList<Column> ALL = ImmutableList.copyOf(INSTANCES.values());

    /** All the award categories in order */
    public static final ImmutableList<Column> CATEGORIES = ALL.stream()
            .filter(column -> !column.nominees().isEmpty())
            .collect(ImmutableList.toImmutableList());

    public static final Column TIMESTAMP = of("Timestamp");

    public static final Column FIRST_NAME = of("First Name");

    public static final Column LAST_NAME = of("Last Name");

    public static final Column TIME = of("Time");

    public static final Column EMAIL = of("EMail");

    private final String name;

    private final ImmutableList<String> nominees;

    private final BigDecimal value;

    private Column(Element inColumn) {
        name = inColumn.getAttributeValue("name");
        nominees = inColumn.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name"))
                .collect(ImmutableList.toImmutableList());
        try {
            value = BigDecimal.ONE.add(Optional.ofNullable(inColumn.getAttributeValue("tieBreaker"))
                    .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                    .orElse(BigDecimal.ZERO));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid tieBreaker value in column: " + name, e);
        }
    }

    /** The name of this Column */
    public String name() {
        return name;
    }

    /** The nominees of this Column in display order */
    public ImmutableList<String> nominees() {
        return nominees;
    }

    /** The scoring value of this Column */
    public BigDecimal value() {
        return value;
    }

    /** The String representation of this Column which is just the name */
    @Override
    public String toString() {
        return name;
    }

    /** Get the Column instance that has the given header */
    public static Column of(String inHeader) {
        return Objects.requireNonNull(INSTANCES.get(inHeader), "Column not defined: " + inHeader);
    }

    private static ImmutableMap<String, Column> read(XMLFile inDefinitionsFile) {
        try {
            return inDefinitionsFile.read().orElseThrow(FileNotFoundException::new)
                    .getChildren("column").stream().map(Column::new)
                    .collect(ImmutableMap.toImmutableMap(Column::name, column -> column));
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + inDefinitionsFile, e);
        }
    }
}