package oscars;

import java.util.Optional;

/** A nomineee of a category - Immutable */
public final class Nominee {
    /** Name of nominee for website */
    public final String name;

    /** Description of nominee from the ballot or nominee name if not found on any ballots */
    public final String description;

    /** Number of players that guessed this nominee */
    public final long count;

    public Nominee(String inName, String inDescription, long inCount) {
        name = inName;
        description = Optional.ofNullable(inDescription).orElse(inName + " (not on any ballots)");
        count = inCount;
    }
}