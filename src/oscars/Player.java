package oscars;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

/** Player information - Immutable */
public final class Player {
    /** Player's picks for each category - Immutable */
    public final Map<String, String> picks;

    /** Player's guessed time in seconds */
    public final int time;

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

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("firstName", picks.get(Category.FIRST_NAME))
                .setAttribute("lastName", picks.get(Category.LAST_NAME));
    }
}