package oscars;

/** Players' guess in a category - Immutable */
public final class Guess {
    /** Name of guess for website */
    public final String name;

    /** Number of players that made this guess */
    public final Long count;

    /** Description from the ballot */
    public final String description;

    public Guess(String inName, Long inCount, String inDescription) {
        name = inName;
        count = inCount;
        description = inDescription;
    }
}