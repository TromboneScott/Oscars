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
            time = LocalTime.parse(picks.get(Category.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException(
                    picks.get(Category.FIRST_NAME) + " " + picks.get(Category.LAST_NAME)
                            + " has invalid time guess: " + picks.get(Category.TIME),
                    e);
        }
    }

    /** Get the Player's picks for the given category */
    public String getPick(String inCategory) {
        return picks.get(inCategory);
    }

    /** Get the Player's guessed time in seconds */
    public int getTime() {
        return time;
    }

    /** Get the timestamp of the ballot for this Player */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(getPick(Category.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("firstName", getPick(Category.FIRST_NAME))
                .setAttribute("lastName", getPick(Category.LAST_NAME));
    }
}