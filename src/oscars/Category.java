package oscars;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

/** Category information - Immutable */
public final class Category implements ChartColor {
    private static final Map<String, Category> INSTANCES = new HashMap<>();

    public static final Category TIMESTAMP = new Category("Timestamp", null, null);

    public static final Category FIRST_NAME = new Category("First", null, null);

    public static final Category LAST_NAME = new Category("Last", null, null);

    public static final Category TIME = new Category("Time", null, null);

    public static final Category EMAIL = new Category("EMail", null, null);

    /** Category name */
    public final String name;

    /** TieBreaker value */
    public final String tieBreaker;

    /** Scoring value */
    public final BigDecimal value;

    /** Nominees in display order */
    public final List<Nominee> nominees;

    public Category(String inName, String inTieBreakerValue, Stream<Nominee> inNominees) {
        name = inName;
        tieBreaker = Optional.ofNullable(inTieBreakerValue).orElse("");
        value = BigDecimal.ONE.add(tieBreaker.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)));
        nominees = inNominees == null ? Collections.emptyList()
                : Collections.unmodifiableList(inNominees.collect(Collectors.toList()));
        INSTANCES.put(inName, this);
    }

    public static Category of(Element inCategory) {
        String name = inCategory.getAttributeValue("name");
        return Optional.ofNullable(of(name)).orElseGet(() -> new Category(name,
                inCategory.getAttributeValue("tieBreaker"),
                inCategory.getChildren("nominee").stream()
                        .map(nominee -> new Nominee(nominee.getAttributeValue("name"), null, 0))));
    }

    public static Category of(String inName) {
        return INSTANCES.get(inName);
    }

    public String webPage() {
        return name + ".xml";
    }

    public String chartName(Set<String> inWinners) {
        return name + nominees.stream().map(nominee -> inWinners.contains(nominee.name) ? "1" : "0")
                .collect(Collectors.joining()) + ".png";
    }

    public void writeChart(Results inResults) throws IOException {
        Set<String> winners = inResults.winners(this);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees.forEach(nominee -> dataset.addValue(nominee.count, "nominee", nominee.name));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0,
                nominees.stream().mapToLong(nominee -> nominee.count).sum() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND);
        plot.setRenderer(
                new NomineeRenderer(nominees.stream().map(nominee -> winners.isEmpty() ? GRAY
                        : winners.contains(nominee.name) ? GREEN : RED)));

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORY, chartName(winners)), chart, 500,
                300);
    }

    public Element toDOM() {
        return new Element("category").setAttribute("name", name);
    }

    public Element toDOM(Collection<Player> inPlayers) {
        return toDOM().setAttribute("tieBreaker", tieBreaker)
                .setAttribute("value", value.toString())
                .addContent(inPlayers.stream()
                        .map(player -> player.toDOM().setAttribute("guess", player.picks.get(this)))
                        .reduce(new Element("players"), Element::addContent));
    }
}