package oscars.ballot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

import org.jdom2.Element;

import oscars.column.Column;
import oscars.column.DataColumn;

/** Player answers converted from ballot values to website values - Immutable */
public final class Player extends Ballot {
    private static int nextId = 0;

    private final String id;

    private final int time;

    /** Create a Player with their answers mapped to website values */
    Player(Function<Column, String> inAnswerMapper) {
        super(Column.ALL.stream().map(inAnswerMapper).toArray(String[]::new));
        id = String.valueOf(nextId++);
        try {
            time = LocalTime.parse(answer(DataColumn.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time: " + answer(DataColumn.TIME), e);
        }
    }

    /** Get the id of this Player */
    public String id() {
        return id;
    }

    /** Get the Player's guessed time in seconds */
    public int time() {
        return time;
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("id", id);
    }
}