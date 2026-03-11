package oscars.ballot;

import java.util.function.Function;

import org.jdom2.Element;

import oscars.column.Column;

/** Player answers converted from ballot values to website values - Immutable */
public final class Player extends Ballot {
    private static int nextId = 0;

    private final String id = String.valueOf(nextId++);

    /** Create a Player with their answers mapped to website values */
    Player(Function<Column, String> inAnswerMapper) {
        super(Column.ALL.stream().map(inAnswerMapper).toArray(String[]::new));
    }

    /** Get the id of this Player */
    public String id() {
        return id;
    }

    /** Get the DOM Element for this Player */
    public Element toDOM() {
        return new Element("player").setAttribute("id", id);
    }
}