package oscars;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

/** Chart for a given rank - Immutable */
public final class RankChart {
    private final long rank;

    /** Create a rank chart for the given rank */
    public RankChart(long inRank) {
        rank = inRank;
    }

    /** Get the file name for this chart */
    public String getName() {
        return "rank_" + rank + ".png";
    }

    /** Write this chart using the given total number of players */
    public void write(int inTotal) throws IOException {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        data.addValue(rank, "rank", "");
        data.addValue(inTotal, "total", "");

        JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, data);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(1, Math.max(2, inTotal));
        plot.getRangeAxis().setInverted(true);
        plot.getRenderer().setSeriesPaint(0, ChartPaint.GRAY);
        plot.getRenderer().setSeriesPaint(1, ChartPaint.GREEN);

        ChartUtils.saveChartAsPNG(new File(Directory.RANK, getName()), chart, 80, 180);
    }
}