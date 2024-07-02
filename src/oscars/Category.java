package oscars;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;

/** Category information - Immutable */
public final class Category {
    private static final String DEFINITIONS_FILE = "definitions.xml";

    /** All categories in display order */
    public static final List<Category> ALL = Collections.unmodifiableList(all());

    public static final String TIMESTAMP = "Timestamp";

    public static final String FIRST_NAME = "First Name";

    public static final String LAST_NAME = "Last Name";

    public static final String TIME = "Time";

    public static final String EMAIL = "EMail";

    /** Category name */
    public final String name;

    /** Scoring value */
    public final BigDecimal value;

    /** Nominees in display order */
    public final List<String> nominees;

    private Category(Element inCategory) {
        name = inCategory.getAttributeValue("name");
        value = BigDecimal.ONE.add(Optional.ofNullable(inCategory.getAttributeValue("tieBreaker"))
                .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                .orElse(BigDecimal.ZERO));
        nominees = Collections.unmodifiableList(inCategory.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name")).collect(Collectors.toList()));
    }

    private static List<Category> all() {
        try {
            return Optional.ofNullable(Directory.DATA.getRootElement(DEFINITIONS_FILE))
                    .orElseThrow(() -> new RuntimeException("File not found: " + DEFINITIONS_FILE))
                    .getChildren("category").stream().map(Category::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading definitions file: " + DEFINITIONS_FILE, e);
        }
    }

    /** Stream of categories in display order that have nominees */
    public static Stream<Category> stream() {
        return ALL.stream().filter(category -> !category.nominees.isEmpty());
    }

    /** Use a unique filename for each generated chart in case any browsers cache images */
    public String chartName(Results inResults) {
        return name + nominees.stream()
                .map(nominee -> inResults.winners(name).contains(nominee) ? "1" : "0")
                .collect(Collectors.joining()) + ".png";
    }

    public void writeChart(Results inResults, List<Player> inPlayers) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees.forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        inPlayers.forEach(player -> dataset.incrementValue(1, "nominee", player.picks.get(name)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, inPlayers.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(ChartPaint.BACKGROUND);
        plot.setRenderer(
                new NomineeRenderer(nominee -> inResults.winners(name).isEmpty() ? ChartPaint.GRAY
                        : inResults.winners(name).contains(nominees.get(nominee)) ? ChartPaint.GREEN
                                : ChartPaint.RED));

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORY, chartName(inResults)), chart, 500,
                300);
    }
}