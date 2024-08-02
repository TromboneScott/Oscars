package oscars;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

/** Player information - Immutable */
public final class Player {
    private final Map<String, String> picks;

    private final int time;

    /** Create a Player with the specified picks */
    public Player(Map<String, String> inPicks) {
        picks = Collections.unmodifiableMap(new HashMap<>(inPicks));
        try {
            time = LocalTime.parse(getPick(Column.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException(getPick(Column.FIRST_NAME) + " " + getPick(Column.LAST_NAME)
                    + " has invalid time guess: " + getPick(Column.TIME), e);
        }
    }

    /** Get the Player's pick for the given Category */
    public String getPick(Category inCategory) {
        return picks.get(inCategory.getName());
    }

    /** Get the Player's pick for the given Column */
    public String getPick(Column inColumn) {
        return picks.get(inColumn.getHeader());
    }

    /** Get the Player's guessed time in seconds */
    public int getTime() {
        return time;
    }

    /** Get the timestamp of the ballot for this Player */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(getPick(Column.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("firstName", getPick(Column.FIRST_NAME))
                .setAttribute("lastName", getPick(Column.LAST_NAME));
    }
}