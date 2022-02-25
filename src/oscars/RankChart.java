package oscars;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public final class RankChart {
    private final long rank;

    public RankChart(long inRank) {
        rank = inRank;
    }

    public String chartName() {
        return "rank/rank_" + rank + ".png";
    }

    public void writeChart(int playerCount) throws IOException {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        data.addValue(rank, "A", "");
        data.addValue(playerCount, "B", "");

        JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, data);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(1, Math.max(2, playerCount));
        plot.getRangeAxis().setInverted(true);
        plot.getRenderer().setSeriesPaint(0, Category.BAR_GRAY);
        plot.getRenderer().setSeriesPaint(1, Category.BAR_GREEN);

        ChartUtilities.saveChartAsPNG(new File(chartName()), chart, 80, 180);
    }
}
