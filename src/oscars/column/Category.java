package oscars.column;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

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
import com.google.common.collect.ImmutableSet;

import oscars.Oscars;
import oscars.file.Directory;

/** An award category column from the survey - Immutable */
public final class Category extends Column {
    private static final int WIDTH = 500;

    private static final int HEIGHT = 300;

    private static final Paint BACKGROUND = new Color(0xB0C4DE);

    private static final Paint GRAY = Color.GRAY;

    private static final Paint GREEN = new Color(0x28A428);

    private static final Paint RED = new Color(0xCC0000);

    /** All the award categories in order */
    public static final ImmutableList<Category> ALL = readFile()
            .filter(column -> !column.getChildren("nominee").isEmpty()).map(Category::new)
            .collect(ImmutableList.toImmutableList());

    private final ImmutableList<String> nominees;

    private final BigDecimal value;

    private Category(Element inColumn) {
        super(inColumn);
        nominees = inColumn.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name"))
                .collect(ImmutableList.toImmutableList());
        try {
            value = BigDecimal.ONE.add(Optional.ofNullable(inColumn.getAttributeValue("tieBreaker"))
                    .map(tieBreaker -> BigDecimal.ONE.movePointLeft(Integer.parseInt(tieBreaker)))
                    .orElse(BigDecimal.ZERO));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid tieBreaker value in column: " + name(), e);
        }
    }

    /** The nominees of this Column in display order */
    public ImmutableList<String> nominees() {
        return nominees;
    }

    /** The scoring value of this Column */
    public BigDecimal value() {
        return value;
    }

    public Element toDOM() {
        return new Element("category").setAttribute("name", name());
    }

    /** Get the Column instance that has the given header */
    public static Category of(String inHeader) {
        return of(inHeader, ALL);
    }

    /** Write the chart with the current winners for this Category */
    public void writeChart() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        nominees().forEach(nominee -> dataset.setValue(0, "nominee", nominee));
        Oscars.PLAYERS.forEach(player -> dataset.incrementValue(1, "nominee", player.answer(this)));

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        chart.removeLegend();
        chart.setPadding(new RectangleInsets(10, 0, 0, 25));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0, Oscars.PLAYERS.size() * 1.15);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        plot.setBackgroundPaint(BACKGROUND);

        ImmutableSet<String> winners = Oscars.RESULTS.winners(this);
        @SuppressWarnings("serial")
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(final int inRow, final int column) {
                return winners.isEmpty() ? GRAY
                        : winners.contains(nominees().get(column)) ? GREEN : RED;
            }
        };
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        plot.setRenderer(renderer);

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORIES, name() + ".png"), chart, WIDTH,
                HEIGHT);
    }
}