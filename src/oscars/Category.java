package oscars;

import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

/** Category information - Immutable */
public final class Category {
    private static final String DEFINITIONS_FILE = "definitions.xml";

    /** All the defined categories in display order */
    public static final List<Category> DEFINED = Collections.unmodifiableList(all());

    /** All the defined categories in display order that have nominees */
    public static final List<Category> ALL = Collections.unmodifiableList(DEFINED.stream()
            .filter(category -> !category.getNominees().isEmpty()).collect(Collectors.toList()));

    private final String name;

    private final BigDecimal value;

    private final List<String> nominees;

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
            List<Category> all = Directory.DATA.getRootElement(DEFINITIONS_FILE)
                    .orElseThrow(() -> new RuntimeException("File not found"))
                    .getChildren("category").stream().map(Category::new)
                    .collect(Collectors.toList());
            Stream.of(Column.values()).map(Column::getHeader).forEach(header -> all.stream()
                    .filter(category -> category.getName().equals(header)).findAny()
                    .orElseThrow(() -> new RuntimeException("Category not defined: " + header)));
            return all;
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + DEFINITIONS_FILE, e);
        }
    }

    /** Get the name of this Category */
    public String getName() {
        return name;
    }

    /** Get the scoring value of this Category */
    public BigDecimal getValue() {
        return value;
    }

    /** Get the nominees in display order of this Category */
    public List<String> getNominees() {
        return nominees;
    }

    /** Use a unique filename for each generated chart in case any browsers cache images */
    private String chartName(Results inResults) {
        return name + nominees.stream()
                .map(nominee -> inResults.winners(name).contains(nominee) ? "1" : "0")
                .collect(Collectors.joining()) + ".png";
    }

    /** Write the chart for this Category given these players and these Results */
    public void writeChart(List<Player> inPlayers, Results inResults) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees.forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        inPlayers.forEach(player -> dataset.incrementValue(1, "nominee", player.getPick(this)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, inPlayers.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(ChartPaint.BACKGROUND);
        plot.setRenderer(new NomineeRenderer(inResults));

        ChartUtils.saveChartAsPNG(Directory.CATEGORY.file(chartName(inResults)), chart, 500, 300);
    }

    @SuppressWarnings("serial")
    private class NomineeRenderer extends BarRenderer {
        private final Collection<String> winners;

        public NomineeRenderer(Results inResults) {
            winners = inResults.winners(name);
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
        Set<String> chartsToKeep = ALL.stream().map(category -> category.chartName(inResults))
                .collect(Collectors.toSet());
        for (File file : Directory.CATEGORY.listFiles(
                (directory, name) -> name.endsWith(".png") && !chartsToKeep.contains(name)))
            file.delete();
    }
}