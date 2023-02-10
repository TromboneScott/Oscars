package oscars;

/** Category information - Immutable */
import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

public final class Category {
    public static final String DIRECTORY = "category/";

    private static final Pattern TIE_BREAKER_PATTERN = Pattern.compile(" *\\((\\d+)\\)$");

    public static final Category FIRST_NAME = new Category("First", null);

    public static final Category LAST_NAME = new Category("Last", null);

    public static final Category TIME = new Category("Time", null);

    public static final Paint BAR_GRAY = Color.GRAY;

    public static final Paint BAR_GREEN = Color.getColor("", 0x28A428);

    private static final Paint BAR_RED = Color.getColor("", 0xCC0000);

    private static final Paint BACKGROUND_COLOR = Color.getColor("", 0xB0C4DE);

    /** Category name without tieBreaker */
    public final String name;

    /** TieBreaker value */
    public final String tieBreakerValue;

    /** Scoring value */
    public final BigDecimal value;

    /** Players' guesses in this category */
    public final List<Guess> guesses;

    private Category(String inName, Stream<Guess> inGuesses) {
        Matcher tieBreakerMatcher = TIE_BREAKER_PATTERN.matcher(inName);
        name = tieBreakerMatcher.replaceFirst("");
        tieBreakerValue = tieBreakerMatcher.find(0) ? tieBreakerMatcher.group(1) : "";
        value = BigDecimal.ONE.add(tieBreakerValue.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreakerValue)));
        guesses = inGuesses == null ? null
                : Collections.unmodifiableList(
                        inGuesses.sorted(Comparator.comparing(guess -> guess.name.toUpperCase()))
                                .collect(Collectors.toList()));
    }

    public static Category of(String inName, Stream<Guess> inGuesses) {
        return Stream.of(FIRST_NAME, LAST_NAME, TIME)
                .filter(category -> category.name.equals(inName)).findAny()
                .orElseGet(() -> new Category(inName, inGuesses));
    }

    @Override
    public String toString() {
        return name;
    }

    public String chartName(Results inResults) {
        return name + "_"
                + guesses.stream()
                        .map(guess -> inResults.winners(this).contains(guess.name) ? "1" : "0")
                        .collect(Collectors.joining())
                + ".png";
    }

    public void writeChart(Results inResults) throws IOException {
        Collection<String> winners = inResults.winners(this);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        guesses.forEach(guess -> dataset.addValue(guess.count, "nominee", guess.name));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0,
                guesses.stream().mapToLong(guess -> guess.count).sum() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setRenderer(new GuessRenderer(guesses.stream()
                .map(guess -> winners.isEmpty() ? BAR_GRAY
                        : winners.contains(guess.name) ? BAR_GREEN : BAR_RED)
                .toArray(Paint[]::new)));

        ChartUtils.saveChartAsPNG(new File(DIRECTORY + chartName(inResults)), chart, 500, 300);
    }

    public Element toDOM(Collection<Player> inPlayers) {
        return new Element("category").addContent(new Element("name").addContent(name))
                .addContent(new Element("tieBreaker").addContent(tieBreakerValue))
                .addContent(new Element("value").addContent(value.toString()))
                .addContent(inPlayers.stream()
                        .map(player -> player.toDOM().addContent(
                                new Element("guess").addContent(player.picks.get(this))))
                        .reduce(new Element("players"), Element::addContent));
    }
}