package oscars;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;

/** Player information - Immutable */
public final class Player {
    private final Map<Column, String> answers;

    private final int time;

    private final LocalDateTime timestamp;

    /** Create a Player with the specified answer in each Column */
    public Player(String[] inAnswers) {
        answers = Collections.unmodifiableMap(IntStream.range(0, inAnswers.length).boxed()
                .collect(Collectors.toMap(Column.ALL::get, column -> inAnswers[column].trim())));
        try {
            time = LocalTime.parse(answer(Column.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException(name() + " has invalid time: " + answer(Column.TIME), e);
        }
        timestamp = LocalDateTime.parse(answer(Column.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    /** Get the name (last, first) of the Player */
    public String name() {
        return Stream.of(Column.LAST_NAME, Column.FIRST_NAME).map(this::answer)
                .filter(name -> !name.isEmpty()).collect(Collectors.joining(", "));
    }

    /** Get the Player's answer in the given Column */
    public String answer(Column inColumn) {
        return answers.get(inColumn);
    }

    /** Get the Player's guessed time in seconds */
    public int time() {
        return time;
    }

    /** Get the timestamp of the ballot for this Player */
    public LocalDateTime timestamp() {
        return timestamp;
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("firstName", answer(Column.FIRST_NAME))
                .setAttribute("lastName", answer(Column.LAST_NAME));
    }
}