package oscars.ballot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

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

    /** Get the timestamp of this Ballot */
    LocalDateTime timestamp() {
        return LocalDateTime.parse(answer(DataColumn.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }
}