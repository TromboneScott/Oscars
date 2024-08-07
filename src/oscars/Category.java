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
public final class Category extends Column {
    private final BigDecimal value;

    private final List<String> nominees;

    public Category(Element inCategory) {
        super(inCategory.getAttributeValue("name"));
        value = BigDecimal.ONE.add(Optional.ofNullable(inCategory.getAttributeValue("tieBreaker"))
                .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                .orElse(BigDecimal.ZERO));
        nominees = Collections.unmodifiableList(inCategory.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name")).collect(Collectors.toList()));
    }

    /** The scoring value of this Category */
    public BigDecimal value() {
        return value;
    }

    /** The nominees in display order of this Category */
    public List<String> nominees() {
        return nominees;
    }

    /** Use a unique filename for each generated chart in case any browsers cache images */
    private String chartName(Results inResults) {
        return header() + nominees.stream()
                .map(nominee -> inResults.winners(this).contains(nominee) ? "1" : "0")
                .collect(Collectors.joining()) + ".png";
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
        private final Collection<String> winners;

        public NomineeRenderer(Collection<String> inWinners) {
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
        Set<String> chartsToKeep = Columns.CATEGORIES.stream()
                .map(category -> category.chartName(inResults)).collect(Collectors.toSet());
        for (File file : Directory.CATEGORY.listFiles(
                (directory, name) -> name.endsWith(".png") && !chartsToKeep.contains(name)))
            file.delete();
    }
}