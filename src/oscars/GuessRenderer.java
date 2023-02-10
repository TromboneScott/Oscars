package oscars;

import java.awt.Paint;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

/** Set bar colors for Category guesses */
@SuppressWarnings("serial")
public class GuessRenderer extends BarRenderer {
    private final Paint[] guessColors;

    public GuessRenderer(Paint[] inGuessColors) {
        super();
        guessColors = inGuessColors.clone();
        setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setDefaultItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return guessColors[inColumn];
    }
}