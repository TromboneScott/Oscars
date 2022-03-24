package oscars;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

/** A Player's score and rank standings - Immutable */
public final class PlayerStanding {
    public final BigDecimal score;

    public final long rank;

    public final long worstPossibleRank;

    private final Set<Player> lostTo;

    public PlayerStanding(BigDecimal inScore, long inRank, long inWorstPossibleRank,
            Set<Player> inLostTo) {
        score = inScore;
        rank = inRank;
        worstPossibleRank = inWorstPossibleRank;
        lostTo = Collections.unmodifiableSet(inLostTo);
    }

    public long getBestPossibleRank() {
        return lostTo.size() + 1;
    }

    public boolean lostTo(Player inOpponent) {
        return lostTo.contains(inOpponent);
    }
}