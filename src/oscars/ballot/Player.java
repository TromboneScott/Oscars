package oscars.ballot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import oscars.column.DataColumn;

/** Player answers converted from ballot values to website values - Immutable */
public final class Player extends Ballot {
    private final int time;

    /** Create a Player with the specified website answer in each Column */
    Player(String[] inAnswers) {
        super(inAnswers);
        try {
            time = LocalTime.parse(answer(DataColumn.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time in: " + Arrays.asList(inAnswers), e);
        }
    }

    /** Get the Player's guessed time in seconds */
    public int time() {
        return time;
    }
}