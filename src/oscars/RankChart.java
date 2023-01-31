package oscars;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public final class RankChart {
    public static final String DIRECTORY = "rank/";

    public static String name(long inRank) {
        return "rank_" + inRank + ".png";
    }

    public static void writeAll(int inPlayerCount) throws IOException {
        IOUtils.mkdir(DIRECTORY);
        for (int inRank = 1; inRank <= inPlayerCount; inRank++) {
            DefaultCategoryDataset data = new DefaultCategoryDataset();
            data.addValue(inRank, "A", "");
            data.addValue(inPlayerCount, "B", "");

            JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, data);
            chart.removeLegend();

            CategoryPlot plot = chart.getCategoryPlot();
            plot.getRangeAxis().setRange(1, Math.max(2, inPlayerCount));
            plot.getRangeAxis().setInverted(true);
            plot.getRenderer().setSeriesPaint(0, Category.BAR_GRAY);
            plot.getRenderer().setSeriesPaint(1, Category.BAR_GREEN);

            ChartUtilities.saveChartAsPNG(new File(DIRECTORY + name(inRank)), chart, 80, 180);
        }
    }
}