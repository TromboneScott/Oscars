package oscars;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Element;

/** Player information - Immutable */
public final class Player {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm:ss");

    /** Player's first name */
    public final String firstName;

    /** Player's last name */
    public final String lastName;

    /** Player's picks - Immutable */
    public final Map<Category, String> picks;

    /** Player's guessed time in seconds */
    public final int time;

    /**
     * Constructs a new Player with specified picks
     *
     * @param inEntries
     *            All the entries for this Player
     */
    public Player(Map<Category, String> inEntries) {
        firstName = inEntries.get(Category.LAST_NAME).isEmpty() ? ""
                : inEntries.get(Category.FIRST_NAME);
        lastName = inEntries.get(Category.LAST_NAME).isEmpty() ? inEntries.get(Category.FIRST_NAME)
                : inEntries.get(Category.LAST_NAME);
        picks = Collections.unmodifiableMap(
                inEntries.entrySet().stream().filter(entry -> !entry.getKey().nominees.isEmpty())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        try {
            time = LocalTime.parse(inEntries.get(Category.TIME), TIME_FORMAT).toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException(firstName + " " + lastName + " has invalid time guess: "
                    + inEntries.get(Category.TIME), e);
        }
    }

    public String webPage() {
        return firstName + "_" + lastName + ".xml";
    }

    public Element toDOM() {
        return new Element("player").setAttribute("firstName", firstName)
                .setAttribute("lastName", lastName).setAttribute("webPage", webPage());
    }
}