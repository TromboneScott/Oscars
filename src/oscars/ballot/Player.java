package oscars.ballot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

import oscars.column.Column;
import oscars.column.DataColumn;

/** Player answers converted from ballot values to website values - Immutable */
public final class Player extends Ballot {
    private final int time;

    /** Create a Player with their answers mapped to website values */
    Player(Function<Column, String> inAnswerMapper) {
        super(Column.ALL.stream().map(inAnswerMapper).toArray(String[]::new));
        try {
            time = LocalTime.parse(answer(DataColumn.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time: " + answer(DataColumn.TIME), e);
        }
    }

    /** Get the Player's guessed time in seconds */
    public int time() {
        return time;
    }
}