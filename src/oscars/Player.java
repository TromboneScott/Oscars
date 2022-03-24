package oscars;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

/** Player information - Immutable */
public final class Player {
    /** Player's first name */
    public final String firstName;

    /** Player's last name */
    public final String lastName;

    /** Player's picks - immutable */
    public final Map<Category, String> picks;

    /** Player's guessed time in seconds */
    public final long time;

    /**
     * Constructs a new Player with specified picks
     *
     * @param inEntries
     *            All the entries for this Player
     */
    public Player(Map<Category, String> inEntries) {
        String tempFirstName = inEntries.get(Category.FIRST_NAME);
        String tempLastName = inEntries.get(Category.LAST_NAME);
        if (tempLastName.isEmpty()) {
            tempLastName = tempFirstName;
            tempFirstName = "";
        }
        firstName = tempFirstName.trim();
        lastName = tempLastName.trim();
        picks = Collections.unmodifiableMap(
                inEntries.entrySet().stream().filter(entry -> !entry.getKey().guesses.isEmpty())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        time = Stream.of(inEntries.get(Category.TIME).split(":", 3)).mapToLong(Long::parseLong)
                .reduce(0, (subtotal, element) -> subtotal * 60 + element);
    }

    public Element toDOM() {
        return new Element("player").addContent(new Element("firstName").addContent(firstName))
                .addContent(new Element("lastName").addContent(lastName));
    }

    @Override
    public int hashCode() {
        return (lastName + ", " + firstName).hashCode();
    }

    @Override
    public boolean equals(Object inPlayer) {
        return firstName.equals(((Player) inPlayer).firstName)
                && lastName.equals(((Player) inPlayer).lastName);
    }
}