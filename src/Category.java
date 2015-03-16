/** Category information - Immutable */
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public final class Category {
    private static final Color BACKGROUND_COLOR = Color.getColor("", 0xB0C4DE);

    private static final Pattern TIE_BREAKER_PATTERN = Pattern.compile(" *\\((\\d+)\\)");

    public static final Category FIRST_NAME = new Category("First");

    public static final Category LAST_NAME = new Category("Last");

    public static final Category TIME = new Category("Time");

    /** Category name without tieBreaker */
    public final String name;

    /** TieBreaker value */
    public final String tieBreakerValue;

    /** Scoring value */
    public final BigDecimal value;

    /** Players' guesses in this category */
    public final Map<String, Integer> guesses;

    private Category(String inName) {
        name = inName;
        tieBreakerValue = null;
        value = null;
        guesses = null;
    }

    public Category(String inName, Map<String, Integer> inGuesses) {
        name = baseName(inName);
        tieBreakerValue = tieBreakerValue(inName);
        value = value(tieBreakerValue);
        guesses = Collections.unmodifiableMap(new HashMap<String, Integer>(inGuesses));
    }

    private static String baseName(String inName) {
        return TIE_BREAKER_PATTERN.matcher(inName).replaceFirst("");
    }

    private static String tieBreakerValue(String inName) {
        Matcher tieBreakerMatcher = TIE_BREAKER_PATTERN.matcher(inName);
        return tieBreakerMatcher.find() ? tieBreakerMatcher.group(1) : "";
    }

    private static BigDecimal value(String inTieBreakerValue) {
        return inTieBreakerValue.isEmpty() ? BigDecimal.ONE : BigDecimal.ONE.add(BigDecimal.ONE
                .divide(new BigDecimal(Math.pow(10, Integer.parseInt(inTieBreakerValue)))));
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

    public void writeChart(final Collection<String> inWinners) throws IOException {
        final boolean[] isWinner = new boolean[guesses.size()];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<String> guessList = new ArrayList<String>(guesses.keySet());
        Collections.sort(guessList);
        for (int guessNum = 0; guessNum < guessList.size(); guessNum++) {
            String guess = guessList.get(guessNum);
            dataset.addValue(guesses.get(guess), "nominee", guess);
            isWinner[guessNum] = inWinners.contains(guess);
        }

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setUpperMargin(.15);// Room for counts on top
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setRenderer(new GuessRenderer(inWinners.isEmpty() ? null : isWinner));

        ChartUtilities.saveChartAsPNG(new File("category/" + name + ".png"), chart, 500, 300);
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
        categoryDOM.addContent(toGuessesDOM());
        categoryDOM.addContent(toPlayersDOM(inPlayers));
        return categoryDOM;
    }

    private Element toGuessesDOM() {
        Element guessesDOM = new Element("guesses");
        List<String> guessList = new ArrayList<String>(guesses.keySet());
        Collections.sort(guessList);
        for (String guess : guessList) {
            Element guessDOM = new Element("guess");
            guessDOM.addContent(new Element("name").addContent(guess));
            guessDOM.addContent(new Element("count").addContent(String.valueOf(guesses.get(guess))));
            guessesDOM.addContent(guessDOM);
        }
        return guessesDOM;
    }

    private Element toPlayersDOM(Collection<Player> inPlayers) {
        Element playersDOM = new Element("players");
        for (Player player : inPlayers) {
            Element playerDOM = player.toCoreDOM();
            playerDOM.addContent(new Element("guess").addContent(player.picks.get(this)));
            playersDOM.addContent(playerDOM);
        }
        return playersDOM;
    }
}