import java.awt.Paint;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

@SuppressWarnings("serial")
public class GuessRenderer extends BarRenderer {
    private final boolean[] isWinner;

    public GuessRenderer(boolean[] inIsWinner) {
        super();
        isWinner = inIsWinner;
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setBaseItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return isWinner == null ? Oscars.BAR_GRAY : isWinner[inColumn] ? Oscars.BAR_GREEN
                : Oscars.BAR_RED;
    }
}
