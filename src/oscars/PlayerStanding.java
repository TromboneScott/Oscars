package oscars;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jdom2.Content;
import org.jdom2.Element;

/** A Player's score and rank standings - Immutable */
public final class PlayerStanding {
    public final BigDecimal score;

    public final long rank;

    private final long worstPossibleRank;

    private final Set<Player> lostTo;

    public PlayerStanding(BigDecimal inScore, long inRank, long inWorstPossibleRank,
            Set<Player> inLostTo) {
        score = inScore;
        rank = inRank;
        worstPossibleRank = inWorstPossibleRank;
        lostTo = Collections.unmodifiableSet(inLostTo);
    }

    public boolean lostTo(Player inOpponent) {
        return lostTo.contains(inOpponent);
    }

    public Collection<Content> toContent(String inScoreFormat) {
        return Arrays.asList(new Element("rank").addContent(String.valueOf(rank)),
                new Element("bpr").addContent(String.valueOf(lostTo.size() + 1)),
                new Element("wpr").addContent(String.valueOf(worstPossibleRank)),
                new Element("score").addContent(String.format(inScoreFormat, score)));
    }
}