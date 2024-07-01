package oscars;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class RankChart {
    private final long rank;

    public RankChart(long inRank) {
        rank = inRank;
    }

    public String getName() {
        return "rank_" + rank + ".png";
    }

    public void write(int inTotal) throws IOException {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        data.addValue(rank, "A", "");
        data.addValue(inTotal, "B", "");

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