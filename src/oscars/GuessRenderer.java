package oscars;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

@SuppressWarnings("serial")
public class GuessRenderer extends BarRenderer {
    private final Color[] guessColor;

    public GuessRenderer(Color[] inGuessColor) {
        super();
        guessColor = inGuessColor;
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setBaseItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return guessColor[inColumn];
    }
}