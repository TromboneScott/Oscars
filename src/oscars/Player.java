package oscars;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.IntStream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableMap;

/** Player information - Immutable */
public final class Player {
    private final ImmutableMap<Column, String> answers;

    private final int time;

    /** Create a Player with the specified answer in each Column */
    public Player(String[] inAnswers) {
        answers = IntStream.range(0, inAnswers.length).boxed().collect(
                ImmutableMap.toImmutableMap(Column.ALL::get, column -> inAnswers[column].trim()));
        try {
            time = LocalTime.parse(answer(Column.TIME), DateTimeFormatter.ofPattern("H:mm:ss"))
                    .toSecondOfDay();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time in: " + answers, e);
        }
    }

    /** Get the Player's answer in the given Column */
    public String answer(Column inColumn) {
        return answers.get(inColumn);
    }

    /** Get the Player's guessed time in seconds */
    public int time() {
        return time;
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("firstName", answer(Column.FIRST_NAME))
                .setAttribute("lastName", answer(Column.LAST_NAME));
    }
}