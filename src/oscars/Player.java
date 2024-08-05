package oscars;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

/** Player information - Immutable */
public final class Player {
    private final Map<String, String> answers;

    private final int time;

    /** Create a Player with the specified answers */
    public Player(Map<Column, String> inAnswers) {
        answers = Collections.unmodifiableMap(inAnswers.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().header(), Entry::getValue)));
        try {
            time = LocalTime
                    .parse(answer(DefinedColumn.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException(
                    name() + " has invalid time guess: " + answer(DefinedColumn.TIME), e);
        }
    }

    /** Get the name (last, first) for the Player */
    public String name() {
        return Stream.of(DefinedColumn.LAST_NAME, DefinedColumn.FIRST_NAME).map(this::answer)
                .filter(name -> !name.isEmpty()).collect(Collectors.joining(", "));
    }

    /** Get the Player's answer in the given Column */
    public String answer(Column inColumn) {
        return answers.get(inColumn.header());
    }

    /** Get the Player's guessed time in seconds */
    public int time() {
        return time;
    }

    /** Get the timestamp of the ballot for this Player */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(answer(DefinedColumn.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("firstName", answer(DefinedColumn.FIRST_NAME))
                .setAttribute("lastName", answer(DefinedColumn.LAST_NAME));
    }
}