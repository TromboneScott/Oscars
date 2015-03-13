import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

@SuppressWarnings("serial")
public class GuessRenderer extends BarRenderer {
    private static final Color BAR_GRAY = Color.GRAY;

    private static final Color BAR_GREEN = Color.getColor("", 0x28A428);

    private static final Color BAR_RED = Color.getColor("", 0xCC0000);

    private final boolean[] isWinner;

    public GuessRenderer(boolean[] inIsWinner) {
        super();
        isWinner = inIsWinner;
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setBaseItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return isWinner == null ? BAR_GRAY : isWinner[inColumn] ? BAR_GREEN : BAR_RED;
    }
}
