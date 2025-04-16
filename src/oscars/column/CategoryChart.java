package oscars.column;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;

import com.google.common.collect.ImmutableSet;

import oscars.Oscars;
import oscars.file.Directory;

/** A chart of the nominees in the given Category using the current Results - Immutable */
public final class CategoryChart {
    private static final int WIDTH = 500;

    private static final int HEIGHT = 300;

    private static final Paint BACKGROUND = new Color(0xB0C4DE);

    private static final Paint GRAY = Color.GRAY;

    private static final Paint GREEN = new Color(0x28A428);

    private static final Paint RED = new Color(0xCC0000);

    private final Category category;

    private final ImmutableSet<String> winners;

    public CategoryChart(Category inCategory) {
        category = inCategory;
        winners = Oscars.RESULTS.winners(inCategory);
    }

    /** Write this chart */
    public void write() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        category.nominees().forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        Oscars.PLAYERS
                .forEach(player -> dataset.incrementValue(1, "nominee", player.answer(category)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, Oscars.PLAYERS.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND);

        @SuppressWarnings("serial")
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(final int inRow, final int column) {
                return winners.isEmpty() ? GRAY
                        : winners.contains(category.nominees().get(column)) ? GREEN : RED;
            }
        };
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        plot.setRenderer(renderer);

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORY, name()), chart, WIDTH, HEIGHT);
    }

    /** Use a unique filename for each generated chart for browsers that cache images */
    private String name() {
        return category.nominees().stream().map(nominee -> winners.contains(nominee) ? "1" : "0")
                .collect(Collectors.joining("", category.name(), ".png"));
    }

    /** Delete all charts we no longer use */
    public static void deleteUnusedCharts() {
        ImmutableSet<String> usedCharts = Category.ALL.stream().map(CategoryChart::new)
                .map(CategoryChart::name).collect(ImmutableSet.toImmutableSet());
        for (File file : Directory.CATEGORY.listFiles(
                (directory, name) -> name.endsWith(".png") && !usedCharts.contains(name)))
            file.delete();
    }
}