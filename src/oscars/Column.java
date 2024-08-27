package oscars;

import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** A column from the survey - Immutable */
public final class Column {
    /** All the columns in survey order */
    public static final ImmutableList<Column> ALL = read("definitions.xml");

    /** All the award categories in order */
    public static final ImmutableList<Column> CATEGORIES = ALL.stream()
            .filter(category -> !category.nominees().isEmpty())
            .collect(ImmutableList.toImmutableList());

    private static final ImmutableMap<String, Column> INSTANCES = ALL.stream()
            .collect(ImmutableMap.toImmutableMap(Column::header, column -> column, (a, b) -> {
                throw new RuntimeException("Duplicate definitions found for column: " + a.header);
            }));

    public static final Column TIMESTAMP = of("Timestamp");

    public static final Column FIRST_NAME = of("First Name");

    public static final Column LAST_NAME = of("Last Name");

    public static final Column TIME = of("Time");

    public static final Column EMAIL = of("EMail");

    private final String header;

    private final BigDecimal value;

    private final ImmutableList<String> nominees;

    private Column(Element inCategory) {
        header = Objects.requireNonNull(inCategory.getAttributeValue("name"),
                "Category is missing required attribute: name");
        try {
            value = BigDecimal.ONE.add(Optional
                    .ofNullable(inCategory.getAttributeValue("tieBreaker"))
                    .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                    .orElse(BigDecimal.ZERO));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid tieBreaker value in category: " + header, e);
        }
        nominees = inCategory.getChildren("nominee").stream()
                .map(nominee -> Objects.requireNonNull(nominee.getAttributeValue("name"),
                        header + " category has nominee without required attribute: name"))
                .collect(ImmutableList.toImmutableList());
    }

    /** The header of this Column */
    public final String header() {
        return header;
    }

    /** The scoring value of this Column */
    public BigDecimal value() {
        return value;
    }

    /** The nominees in display order of this Column */
    public ImmutableList<String> nominees() {
        return nominees;
    }

    /** The String representation of this Column which will be just the header */
    @Override
    public final String toString() {
        return header;
    }

    /** Get the Column instance that has the given header */
    public static Column of(String inHeader) {
        return Objects.requireNonNull(INSTANCES.get(inHeader), "Column not defined: " + inHeader);
    }

    private static ImmutableList<Column> read(String inDefinitionsFile) {
        try {
            return Directory.DATA.getRootElement(inDefinitionsFile)
                    .orElseThrow(() -> new RuntimeException("File not found")).getChildren("column")
                    .stream().map(Column::new).collect(ImmutableList.toImmutableList());
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + inDefinitionsFile, e);
        }
    }

    /** Use a unique filename for each generated chart in case any browsers cache images */
    private String chartName(Results inResults) {
        return header + nominees.stream().map(inResults.winners(this)::contains)
                .map(winner -> winner ? "1" : "0").collect(Collectors.joining()) + ".png";
    }

    /** Write the chart for this Category given these players and these Results */
    public void writeChart(List<Player> inPlayers, Results inResults) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees.forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        inPlayers.forEach(player -> dataset.incrementValue(1, "nominee", player.answer(this)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, inPlayers.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(ChartPaint.BACKGROUND);
        plot.setRenderer(new NomineeRenderer(inResults.winners(this)));

        ChartUtils.saveChartAsPNG(Directory.CATEGORY.file(chartName(inResults)), chart, 500, 300);
    }

    @SuppressWarnings("serial")
    private class NomineeRenderer extends BarRenderer {
        private final ImmutableList<String> winners;

        public NomineeRenderer(ImmutableList<String> inWinners) {
            winners = inWinners;
            setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            setDefaultItemLabelsVisible(true);
        }

        @Override
        public Paint getItemPaint(final int inRow, final int inColumn) {
            return winners.isEmpty() ? ChartPaint.GRAY
                    : winners.contains(nominees.get(inColumn)) ? ChartPaint.GREEN : ChartPaint.RED;
        }
    }

    /** Delete all charts we don't need to keep */
    public static void cleanUpCharts(Results inResults) {
        Set<String> chartsToKeep = CATEGORIES.stream()
                .map(category -> category.chartName(inResults)).collect(Collectors.toSet());
        for (File file : Directory.CATEGORY.listFiles(
                (directory, name) -> name.endsWith(".png") && !chartsToKeep.contains(name)))
            file.delete();
    }
}