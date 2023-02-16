package oscars;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public abstract class RankChart {
    public static String name(long inRank) {
        return "rank_" + inRank + ".png";
    }

    public static void writeAll(int inTotal) throws IOException {
        for (int rank = 1; rank <= inTotal; rank++) {
            DefaultCategoryDataset data = new DefaultCategoryDataset();
            data.addValue(rank, "A", "");
            data.addValue(inTotal, "B", "");

            JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, data);
            chart.removeLegend();

            CategoryPlot plot = chart.getCategoryPlot();
            plot.getRangeAxis().setRange(1, Math.max(2, inTotal));
            plot.getRangeAxis().setInverted(true);
            plot.getRenderer().setSeriesPaint(0, ChartColor.GRAY);
            plot.getRenderer().setSeriesPaint(1, ChartColor.GREEN);

            ChartUtils.saveChartAsPNG(new File(Directory.RANK, name(rank)), chart, 80, 180);
        }
        Directory.RANK.cleanUp();
    }
}