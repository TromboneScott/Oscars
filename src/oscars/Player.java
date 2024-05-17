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
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm:ss");

    /** Player's picks for each category - Immutable */
    public final Map<String, String> picks;

    /** Player's first name */
    private final String firstName;

    /** Player's last name */
    private final String lastName;

    /** The XML web page for this player */
    public final String webPage;

    /** Player's guessed time in seconds */
    public final int time;

    /**
     * Constructs a new Player with specified picks
     *
     * @param inPicks
     *            This Player's picks for each category
     */
    public Player(Map<String, String> inPicks) {
        picks = Collections.unmodifiableMap(new HashMap<>(inPicks));
        firstName = picks.get(Category.LAST_NAME).isEmpty() ? "" : picks.get(Category.FIRST_NAME);
        lastName = picks.get(Category.LAST_NAME).isEmpty() ? picks.get(Category.FIRST_NAME)
                : picks.get(Category.LAST_NAME);
        webPage = firstName + "_" + lastName + ".xml";
        try {
            time = LocalTime.parse(picks.get(Category.TIME), TIME_FORMAT).toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException(firstName + " " + lastName + " has invalid time guess: "
                    + picks.get(Category.TIME), e);
        }
    }

    public Element toDOM() {
        return new Element("player").setAttribute("firstName", firstName)
                .setAttribute("lastName", lastName).setAttribute("webPage", webPage);
    }
}