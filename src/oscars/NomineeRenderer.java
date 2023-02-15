package oscars;

import java.awt.Paint;
import java.util.stream.Stream;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

/** Set bar colors for each Nominee in a Category */
@SuppressWarnings("serial")
public class NomineeRenderer extends BarRenderer {
    private final Paint[] nomineeColors;

    public NomineeRenderer(Stream<Paint> inNomineeColors) {
        super();
        nomineeColors = inNomineeColors.toArray(Paint[]::new);
        setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        setDefaultItemLabelsVisible(true);
    }

    @Override
    public Paint getItemPaint(final int inRow, final int inColumn) {
        return nomineeColors[inColumn];
    }
}