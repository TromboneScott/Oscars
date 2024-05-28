package oscars;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

/** Category information - Immutable */
public final class Category implements ChartColor {
    private static final File CATEGORY_DEFINITIONS_FILE = new File("categoryDefinitions.xml");

    /** Categories in display order */
    public static final List<Category> ALL = Collections.unmodifiableList(all());

    public static final String TIMESTAMP = "Timestamp";

    public static final String FIRST_NAME = "First";

    public static final String LAST_NAME = "Last";

    public static final String TIME = "Time";

    public static final String EMAIL = "EMail";

    /** Category name */
    public final String name;

    /** Scoring value */
    public final BigDecimal value;

    /** Nominees in display order */
    public final List<String> nominees;

    /** The XML web page for this category */
    public final String webPage;

    private Category(Element inCategory) {
        name = inCategory.getAttributeValue("name");
        value = Optional.ofNullable(inCategory.getAttributeValue("tieBreaker"))
                .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                .orElse(BigDecimal.ZERO).add(BigDecimal.ONE);
        nominees = Collections.unmodifiableList(inCategory.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name")).collect(Collectors.toList()));
        webPage = name + ".xml";
    }

    private static List<Category> all() {
        try {
            return new SAXBuilder().build(CATEGORY_DEFINITIONS_FILE).getRootElement()
                    .getChildren("category").stream().map(Category::new)
                    .collect(Collectors.toList());
        } catch (IOException | JDOMException e) {
            throw new RuntimeException(
                    "Error reading category definitions file: " + CATEGORY_DEFINITIONS_FILE, e);
        }
    }

    /** Stream of categories (in order) that have nominees */
    public static Stream<Category> stream() {
        return ALL.stream().filter(category -> !category.nominees.isEmpty());
    }

    /** Use a unique filename for each generated chart in case any browsers cache images */
    public String chartName(Set<String> inWinners) {
        return name + nominees.stream().map(nominee -> inWinners.contains(nominee) ? "1" : "0")
                .collect(Collectors.joining()) + ".png";
    }

    public void writeChart(Results inResults, List<Player> inPlayers) throws IOException {
        Set<String> winners = inResults.winners(name);

        Map<String, Long> counts = nominees.stream()
                .collect(Collectors.toMap(nominee -> nominee, nominee -> inPlayers.stream()
                        .filter(player -> nominee.equals(player.picks.get(name))).count()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees.forEach(nominee -> dataset.addValue(counts.get(nominee), "nominee", nominee));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0,
                counts.values().stream().mapToLong(Long::longValue).sum() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND);
        plot.setRenderer(new NomineeRenderer(winners.isEmpty() ? nominee -> GRAY
                : nominee -> winners.contains(nominees.get(nominee)) ? GREEN : RED));

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORY, chartName(winners)), chart, 500,
                300);
    }

    public Element toDOM() {
        return new Element("category").setAttribute("name", name);
    }

    public Element toDOM(Collection<Player> inPlayers) {
        return inPlayers.stream()
                .map(player -> player.toDOM().setAttribute("guess", player.picks.get(name)))
                .reduce(toDOM().setAttribute("value", value.toString()), Element::addContent);
    }
}