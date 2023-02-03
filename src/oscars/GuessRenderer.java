package oscars;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

/** Set bar colors for Category guesses */
@SuppressWarnings("serial")
public class GuessRenderer extends BarRenderer {
    private final Color[] guessColors;

    public GuessRenderer(Color[] inGuessColors) {
        super();
        guessColors = inGuessColors.clone();
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setBaseItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return guessColors[inColumn];
    }
}