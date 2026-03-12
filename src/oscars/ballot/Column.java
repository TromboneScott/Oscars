package oscars.ballot;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

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
import oscars.file.XMLFile;

/** A column from the ballot - Immutable */
public final class Column {
    /** All the columns in ballot order */
    static final ImmutableList<Column> ALL = XMLFile.readDefinitionsFile().getChildren("column")
            .stream().map(Column::new).collect(ImmutableList.toImmutableList());

    /** All the categories (columns with nominees) in ballot order */
    public static final ImmutableList<Column> CATEGORIES = ALL.stream()
            .filter(column -> !column.nominees().isEmpty())
            .collect(ImmutableList.toImmutableList());

    public static final Column TIMESTAMP = of("Timestamp");

    public static final Column FIRST_NAME = of("First Name");

    public static final Column LAST_NAME = of("Last Name");

    public static final Column TIME = of("Time");

    public static final Column EMAIL = of("EMail");

    private static final int WIDTH = 500;

    private static final int HEIGHT = 300;

    private static final Paint BACKGROUND = new Color(0xB0C4DE);

    private static final Paint GRAY = Color.GRAY;

    private static final Paint GREEN = new Color(0x28A428);

    private static final Paint RED = new Color(0xCC0000);

    private final String name;

    private final ImmutableList<String> nominees;

    private Column(Element inColumn) {
        name = Objects.requireNonNull(inColumn.getAttributeValue("name"),
                "Definitions file missing column attribute: name");
        nominees = inColumn.getChildren("nominee").stream()
                .map(nominee -> nominee.getAttributeValue("name"))
                .collect(ImmutableList.toImmutableList());
    }

    /** The name of this Column */
    public final String name() {
        return name;
    }

    public ImmutableList<String> nominees() {
        return nominees;
    }

    /** Get the Column instance that has the given header from the Column list */
    public static Column of(String inHeader) {
        return ALL.stream().filter(column -> column.name().equals(inHeader)).findAny()
                .orElseThrow(() -> new NullPointerException("Column not defined: " + inHeader));
    }

    public Element toDOM() {
        return new Element("category").setAttribute("name", name());
    }

    /** Write the chart with the current winners for this Category */
    public void writeChart(ImmutableSet<String> inWinners) throws IOException {
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

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(final int inRow, final int column) {
                return inWinners.isEmpty() ? GRAY
                        : inWinners.contains(nominees().get(column)) ? GREEN : RED;
            }
        };
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        plot.setRenderer(renderer);

        ChartUtils.saveChartAsPNG(new File(Directory.CATEGORIES, name() + ".png"), chart, WIDTH,
                HEIGHT);
    }
}