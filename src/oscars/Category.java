package oscars;

/** Category information - Immutable */
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public final class Category {
    private static final Pattern TIE_BREAKER_PATTERN = Pattern.compile(" *\\((\\d+)\\)");

    public static final Category FIRST_NAME = new Category("First");

    public static final Category LAST_NAME = new Category("Last");

    public static final Category TIME = new Category("Time");

    public static final Color BAR_GRAY = Color.GRAY;

    public static final Color BAR_GREEN = Color.getColor("", 0x28A428);

    public static final Color BAR_RED = Color.getColor("", 0xCC0000);

    public static final Color BACKGROUND_COLOR = Color.getColor("", 0xB0C4DE);

    /** Category name without tieBreaker */
    public final String name;

    /** TieBreaker value */
    public final String tieBreakerValue;

    /** Scoring value */
    public final BigDecimal value;

    /** Players' guesses in this category */
    public final Map<String, Long> guesses;

    private Category(String inName) {
        name = inName;
        tieBreakerValue = null;
        value = null;
        guesses = null;
    }

    public Category(String inName, Map<String, Long> inGuesses) {
        name = baseName(inName);
        tieBreakerValue = tieBreakerValue(inName);
        value = value(tieBreakerValue);
        guesses = Collections.unmodifiableMap(new HashMap<>(inGuesses));
    }

    private static String baseName(String inName) {
        return TIE_BREAKER_PATTERN.matcher(inName).replaceFirst("");
    }

    private static String tieBreakerValue(String inName) {
        Matcher tieBreakerMatcher = TIE_BREAKER_PATTERN.matcher(inName);
        return tieBreakerMatcher.find() ? tieBreakerMatcher.group(1) : "";
    }

    private static BigDecimal value(String inTieBreakerValue) {
        return inTieBreakerValue.isEmpty() ? BigDecimal.ONE
                : BigDecimal.ONE
                        .add(BigDecimal.ONE.movePointLeft(Integer.parseInt(inTieBreakerValue)));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object inOther) {
        return this == inOther || inOther != null && getClass() == inOther.getClass()
                && name.equals(((Category) inOther).name);
    }

    public String chartName(Results inResults) {
        return name + "_"
                + guesses.keySet().stream().sorted()
                        .map(guess -> inResults.winners(this).contains(guess) ? "1" : "0")
                        .collect(Collectors.joining())
                + ".png";
    }

    public void writeChart(Results inResults) throws IOException {
        Collection<String> winners = inResults.winners(this);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Color> guessColor = guesses.keySet().stream().sorted()
                .peek(guess -> dataset.addValue(guesses.get(guess), "nominee", guess))
                .map(guess -> winners.isEmpty() ? BAR_GRAY
                        : winners.contains(guess) ? BAR_GREEN : BAR_RED)
                .collect(Collectors.toList());

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        long totalGuesses = guesses.values().stream().mapToLong(Long::longValue).sum();
        if (totalGuesses > 0)
            plot.getRangeAxis().setRange(0, totalGuesses * 1.15);// Room for counts on top
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setRenderer(new GuessRenderer(guessColor));

        ChartUtilities.saveChartAsPNG(new File("category/" + chartName(inResults)), chart, 500,
                300);
    }

    public Element toCoreDOM() {
        Element categoryDOM = new Element("category");
        categoryDOM.addContent(new Element("name").addContent(name));
        categoryDOM.addContent(new Element("tieBreaker").addContent(tieBreakerValue));
        categoryDOM.addContent(new Element("value").addContent(value.toString()));
        return categoryDOM;
    }

    public Element toDOM(Collection<Player> inPlayers) {
        Element categoryDOM = toCoreDOM();
        categoryDOM.addContent(guesses.keySet().stream().sorted()
                .map(guess -> new Element("guess").addContent(guess))
                .reduce(new Element("guesses"), Element::addContent));
        categoryDOM.addContent(inPlayers.stream()
                .map(player -> player.toCoreDOM()
                        .addContent(new Element("guess").addContent(player.picks.get(this))))
                .reduce(new Element("players"), Element::addContent));
        return categoryDOM;
    }
}