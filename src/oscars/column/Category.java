package oscars.column;

import java.math.BigDecimal;
import java.util.Optional;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;

/** An award category column from the survey - Immutable */
public final class Category extends Column {
    /** All the award categories in order */
    public static final ImmutableList<Category> ALL = readFile()
            .filter(column -> !column.getChildren("nominee").isEmpty()).map(Category::new)
            .collect(ImmutableList.toImmutableList());

    private final ImmutableList<String> nominees;

    private final BigDecimal value;

    private Category(Element inColumn) {
        super(inColumn);
        nominees = inColumn.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name"))
                .collect(ImmutableList.toImmutableList());
        try {
            value = BigDecimal.ONE.add(Optional.ofNullable(inColumn.getAttributeValue("tieBreaker"))
                    .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                    .orElse(BigDecimal.ZERO));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid tieBreaker value in column: " + name(), e);
        }
    }

    /** The nominees of this Column in display order */
    public ImmutableList<String> nominees() {
        return nominees;
    }

    /** The scoring value of this Column */
    public BigDecimal value() {
        return value;
    }

    public Element toDOM() {
        return new Element("category").setAttribute("name", name());
    }

    /** Get the Column instance that has the given header */
    public static Category of(String inHeader) {
        return of(inHeader, ALL);
    }
}