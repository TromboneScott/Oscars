package oscars;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

@SuppressWarnings("serial")
public class GuessRenderer extends BarRenderer {
    private final List<Color> guessColors;

    public GuessRenderer(List<Color> inGuessColors) {
        super();
        guessColors = Collections.unmodifiableList(new ArrayList<>(inGuessColors));
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setBaseItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return guessColors.get(inColumn);
    }
}