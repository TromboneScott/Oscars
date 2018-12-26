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
    private final List<Color> guessColor;

    public GuessRenderer(List<Color> inGuessColor) {
        super();
        guessColor = Collections.unmodifiableList(new ArrayList<>(inGuessColor));
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setBaseItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return guessColor.get(inColumn);
    }
}