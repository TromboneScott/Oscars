package oscars;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;

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

import oscars.ballot.Column;
import oscars.file.Directory;

/** Creates bar graphs of the nominees of a given category - Immutable */
final class CategoryChart {
    private static final int WIDTH = 500;

    private static final int HEIGHT = 300;

    private static final Paint BACKGROUND = new Color(0xB0C4DE);

    private static final Paint GRAY = Color.GRAY;

    private static final Paint GREEN = new Color(0x28A428);

    private static final Paint RED = new Color(0xCC0000);

    /** Write a bar graph for the given category indicating any current winners and losers */
    static void write(Column inCategory) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        inCategory.nominees().forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        Oscars.PLAYERS
                .forEach(player -> dataset.incrementValue(1, "nominee", player.answer(inCategory)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, Oscars.PLAYERS.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND);

        BarRenderer renderer = new BarRenderer() {
            private final ImmutableSet<String> winners = Oscars.RESULTS.winners(inCategory);

            @Override
            public Paint getItemPaint(final int inRowNum, final int inColumnNum) {
                return winners.isEmpty() ? GRAY
                        : winners.contains(inCategory.nominees().get(inColumnNum)) ? GREEN : RED;
            }
        };
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        plot.setRenderer(renderer);

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORIES, inCategory.name() + ".png"), chart,
                WIDTH, HEIGHT);
    }
}