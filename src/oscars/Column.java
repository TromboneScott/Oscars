package oscars;

import java.awt.Paint;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
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
import com.google.common.collect.ImmutableSet;

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

    /** Use a unique filename for each generated chart in case any browsers cache images */
    private String chartName(ImmutableSet<String> inWinners) {
        return nominees().stream().map(nominee -> inWinners.contains(nominee) ? "1" : "0")
                .collect(Collectors.joining("", name, ".png"));
    }

    /** Write the chart for this category using the current results */
    public void writeChart() throws IOException {
        ImmutableSet<String> winners = Oscars.RESULTS.winners(this);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees().forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        Oscars.PLAYERS.forEach(player -> dataset.incrementValue(1, "nominee", player.answer(this)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, Oscars.PLAYERS.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(ChartPaint.BACKGROUND);

        @SuppressWarnings("serial")
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(final int inRow, final int column) {
                return winners.isEmpty() ? ChartPaint.GRAY
                        : winners.contains(nominees().get(column)) ? ChartPaint.GREEN
                                : ChartPaint.RED;
            }
        };
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        plot.setRenderer(renderer);

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORY, chartName(winners)), chart, 500,
                300);
    }

    /** Delete all charts we no longer use */
    public static void deleteUnusedCharts() {
        ImmutableSet<String> usedCharts = CATEGORIES.stream()
                .map(category -> category.chartName(Oscars.RESULTS.winners(category)))
                .collect(ImmutableSet.toImmutableSet());
        for (File file : Directory.CATEGORY.listFiles(
                (directory, name) -> name.endsWith(".png") && !usedCharts.contains(name)))
            file.delete();
    }
}