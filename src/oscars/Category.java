package oscars;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public final class Category {
    private static final Pattern TIE_BREAKER_PATTERN = Pattern.compile(" *\\((\\d+)\\)$");

    private static final Map<String, Category> INSTANCES = new HashMap<>();

    public static final Category TIMESTAMP = of("Timestamp", Stream.empty());

    public static final Category FIRST_NAME = of("First", Stream.empty());

    public static final Category LAST_NAME = of("Last", Stream.empty());

    public static final Category TIME = of("Time", Stream.empty());

    public static final Category EMAIL = of("EMail", Stream.empty());

    /** Category name */
    public final String name;

    /** TieBreaker value */
    public final String tieBreakerValue;

    /** Scoring value */
    public final BigDecimal value;

    /** Nominees in display order */
    public final List<Nominee> nominees;

    private Category(String inName, Stream<Nominee> inNominees) {
        Matcher tieBreakerMatcher = TIE_BREAKER_PATTERN.matcher(inName);
        name = tieBreakerMatcher.replaceFirst("");
        tieBreakerValue = tieBreakerMatcher.find(0) ? tieBreakerMatcher.group(1) : "";
        value = BigDecimal.ONE.add(tieBreakerValue.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreakerValue)));
        nominees = Collections.unmodifiableList(
                inNominees.sorted(Comparator.comparing(nominee -> nominee.name.toUpperCase()))
                        .collect(Collectors.toList()));
    }

    public static Category of(String inName, Stream<Nominee> inNominees) {
        return INSTANCES.computeIfAbsent(inName, k -> new Category(inName, inNominees));
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
        plot.setBackgroundPaint(ChartColor.BACKGROUND);
        plot.setRenderer(new NomineeRenderer(
                nominees.stream().map(nominee -> winners.isEmpty() ? ChartColor.GRAY
                        : winners.contains(nominee.name) ? ChartColor.GREEN : ChartColor.RED)));

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORY, chartName(winners)), chart, 500,
                300);
    }

    public Element toDOM() {
        return new Element("category").addContent(new Element("name").addContent(name));
    }

    public Element toDOM(Collection<Player> inPlayers) {
        return toDOM().addContent(new Element("tieBreaker").addContent(tieBreakerValue))
                .addContent(new Element("value").addContent(value.toString()))
                .addContent(inPlayers.stream()
                        .map(player -> player.toDOM().addContent(
                                new Element("guess").addContent(player.picks.get(this))))
                        .reduce(new Element("players"), Element::addContent));
    }
}