package oscars.ballot;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jdom2.Element;

/** Player with answers converted from ballot values to website values - Immutable */
public final class Player {
    private static final AtomicInteger nextId = new AtomicInteger();

    private final String id = String.valueOf(nextId.getAndIncrement());

    private final Ballot ballot;

    /** Create a Player with their answers mapped to website values */
    Player(Function<Column, String> inAnswerMapper) {
        ballot = new Ballot(Column.ALL.stream().map(inAnswerMapper).toArray(String[]::new));
    }

    /** Get the id of this Player */
    public String id() {
        return id;
    }

    /** Get the answer in the given Column */
    public final String answer(Column inColumn) {
        return ballot.answer(inColumn);
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("id", id);
    }
}