package oscars;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The score and rank standings - Immutable */
public final class Standings {
    private final Map<Player, PlayerStanding> standings;

    private final Map<Category, Set<String>> winners;

    public final boolean showEnded;

    private final long elapsedTime;

    public Standings(Collection<Player> inPlayers, Results inResults, long inElapsedTime) {
        winners = Collections.unmodifiableMap(inPlayers.iterator().next().picks.keySet().stream()
                .collect(Collectors.toMap(category -> category, category -> Collections
                        .unmodifiableSet(new HashSet<>(inResults.winners(category))))));
        showEnded = !inResults.getShowTime(ShowTimeType.END).isEmpty();
        elapsedTime = inElapsedTime;
        Map<Player, BigDecimal> scoreMap = inPlayers.parallelStream()
                .collect(Collectors.toMap(player -> player, this::score));
        standings = Collections.unmodifiableMap(inPlayers.parallelStream()
                .collect(Collectors.toMap(player -> player,
                        player -> new PlayerStanding(scoreMap.get(player),
                                lostToStream(player, scoreMap, elapsedTime, true).count() + 1,
                                worstPossibleRank(player, scoreMap), lostTo(player, scoreMap)))));
    }

    public PlayerStanding get(Player inPlayer) {
        return standings.get(inPlayer);
    }

    /** Determine if the player and opponent will be tied at the end of the game */
    public boolean tied(Player inPlayer, Player inOpponent) {
        return (inPlayer.time == inOpponent.time
                || showEnded && inPlayer.time > elapsedTime && inOpponent.time > elapsedTime)
                && get(inPlayer).score.equals(get(inOpponent).score)
                && inPlayer.picks.keySet().stream()
                        .allMatch(category -> !winners.get(category).isEmpty() || inPlayer.picks
                                .get(category).equals(inOpponent.picks.get(category)));
    }

    private BigDecimal score(Player inPlayer) {
        return inPlayer.picks.entrySet().stream()
                .filter(pickEntry -> winners.get(pickEntry.getKey()).contains(pickEntry.getValue()))
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

    private long worstPossibleRank(Player inPlayer, Map<Player, BigDecimal> inScoreMap) {
        Map<Player, BigDecimal> worstPossibleScores = possibleScores(inPlayer, inScoreMap, false);
        return 1 + Math.max(inPlayer.time > elapsedTime
                ? lostToStream(inPlayer, worstPossibleScores, inPlayer.time - 1, true).count()
                : 0, lostToStream(inPlayer, worstPossibleScores, Long.MAX_VALUE, false).count());
    }

    private Set<Player> lostTo(Player inPlayer, Map<Player, BigDecimal> inScoreMap) {
        return lostToStream(inPlayer, possibleScores(inPlayer, inScoreMap, true), elapsedTime,
                showEnded).map(Entry::getKey).collect(Collectors.toSet());
    }

    private Map<Player, BigDecimal> possibleScores(Player inPlayer,
            Map<Player, BigDecimal> inScoreMap, boolean inBest) {
        return inScoreMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey,
                        scoreEntry -> inPlayer.picks.entrySet().stream().map(Entry::getKey)
                                .filter(category -> winners.get(category).isEmpty()
                                        && inBest == scoreEntry.getKey().picks.get(category)
                                                .equals(inPlayer.picks.get(category)))
                                .map(category -> category.value)
                                .reduce(scoreEntry.getValue(), BigDecimal::add)));
    }
}