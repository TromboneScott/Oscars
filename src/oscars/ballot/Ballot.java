package oscars.ballot;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;

/** Answers from a ballot - Immutable */
final class Ballot {
    private final ImmutableMap<Column, String> answers;

    /** Create a Ballot with the answers for each Column */
    Ballot(String[] inAnswers) {
        if (inAnswers.length != Column.ALL.size())
            throw new InvalidParameterException("Number of columns on ballot: " + inAnswers.length
                    + " does not match number of defined columns: " + Column.ALL.size());
        answers = IntStream.range(0, inAnswers.length).boxed().collect(
                ImmutableMap.toImmutableMap(Column.ALL::get, column -> inAnswers[column].trim()));
    }

    /** Get the answer in the given Column */
    final String answer(Column inColumn) {
        return answers.get(inColumn);
    }

    /** Get the timestamp of this Ballot */
    final LocalDateTime timestamp() {
        return LocalDateTime.parse(answer(Column.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }
}