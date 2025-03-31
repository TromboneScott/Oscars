package oscars.ballot;

import java.util.stream.IntStream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableMap;

import oscars.column.Column;
import oscars.column.DataColumn;

/** Answers from a ballot - Immutable */
class Ballot {
    private final ImmutableMap<Column, String> answers;

    /** Create a Ballot with the answers for each Column */
    protected Ballot(String[] inAnswers) {
        answers = IntStream.range(0, inAnswers.length).boxed().collect(
                ImmutableMap.toImmutableMap(Column.ALL::get, column -> inAnswers[column].trim()));
    }

    /** Get the answer in the given Column */
    public final String answer(Column inColumn) {
        return answers.get(inColumn);
    }

    /** Get the DOM Element for this Ballot */
    public final Element toDOM() {
        return new Element("player").setAttribute("firstName", answer(DataColumn.FIRST_NAME))
                .setAttribute("lastName", answer(DataColumn.LAST_NAME));
    }
}