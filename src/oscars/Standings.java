package oscars;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The score and rank standings - Immutable */
public final class Standings {
    private final Map<Player, PlayerStanding> standings;

    public Standings(Collection<Player> inPlayers, Results inResults, long inElapsedTime) {
        Map<Player, BigDecimal> scoreMap = inPlayers.parallelStream()
                .collect(Collectors.toMap(player -> player, player -> score(player, inResults)));
        standings = Collections.unmodifiableMap(inPlayers.parallelStream()
                .collect(Collectors.toMap(player -> player,
                        player -> new PlayerStanding(scoreMap.get(player),
                                lostToStream(player, scoreMap, inElapsedTime, true).count() + 1,
                                worstPossibleRank(player, scoreMap, inResults, inElapsedTime),
                                lostTo(player, scoreMap, inResults, inElapsedTime)))));
    }

    public PlayerStanding get(Player inPlayer) {
        return standings.get(inPlayer);
    }

    /** Determine if the player and opponent will be tied at the end of the game */
    public boolean tied(Player inPlayer, Player inOpponent, Results inResults, long inElapsedTime) {
        return (inPlayer.time == inOpponent.time || inResults.showEnded()
                && inPlayer.time > inElapsedTime && inOpponent.time > inElapsedTime)
                && get(inPlayer).score.equals(get(inOpponent).score)
                && inPlayer.picks.keySet().stream().allMatch(
                        category -> !inResults.winners(category).isEmpty() || inPlayer.picks
                                .get(category).equals(inOpponent.picks.get(category)));
    }

    private BigDecimal score(Player inPlayer, Results inResults) {
        return inPlayer.picks.entrySet().stream()
                .filter(pickEntry -> inResults.winners(pickEntry.getKey())
                        .contains(pickEntry.getValue()))
                .map(pickEntry -> pickEntry.getKey().value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Stream<Entry<Player, BigDecimal>> lostToStream(Player inPlayer,
            Map<Player, BigDecimal> inScoreMap, long inElapsedTime, boolean inCheckOverTime) {
        return inScoreMap.entrySet().stream()
                .filter(scoreEntry -> inScoreMap.get(inPlayer).compareTo(scoreEntry.getValue()) < 0
                        || inScoreMap.get(inPlayer).equals(scoreEntry.getValue())
                                && scoreEntry.getKey().time <= inElapsedTime
                                && (inPlayer.time < scoreEntry.getKey().time
                                        || inCheckOverTime && inPlayer.time > inElapsedTime));
    }

    private long worstPossibleRank(Player inPlayer, Map<Player, BigDecimal> inScoreMap,
            Results inResults, long inElapsedTime) {
        Map<Player, BigDecimal> worstPossibleScores = possibleScores(inPlayer, inScoreMap,
                inResults, false);
        return 1 + Math.max(inPlayer.time > inElapsedTime
                ? lostToStream(inPlayer, worstPossibleScores, inPlayer.time - 1, true).count()
                : 0, lostToStream(inPlayer, worstPossibleScores, Long.MAX_VALUE, false).count());
    }

    private Set<Player> lostTo(Player inPlayer, Map<Player, BigDecimal> inScoreMap,
            Results inResults, long inElapsedTime) {
        return lostToStream(inPlayer, possibleScores(inPlayer, inScoreMap, inResults, true),
                inElapsedTime, inResults.showEnded()).map(Entry::getKey)
                        .collect(Collectors.toSet());
    }

    private Map<Player, BigDecimal> possibleScores(Player inPlayer,
            Map<Player, BigDecimal> inScoreMap, Results inResults, boolean inBest) {
        return inScoreMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey,
                        scoreEntry -> inPlayer.picks.entrySet().stream().map(Entry::getKey)
                                .filter(category -> inResults.winners(category).isEmpty()
                                        && inBest == scoreEntry.getKey().picks.get(category)
                                                .equals(inPlayer.picks.get(category)))
                                .map(category -> category.value)
                                .reduce(scoreEntry.getValue(), BigDecimal::add)));
    }
}