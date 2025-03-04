package oscars;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/** The score and rank standings - Immutable */
public final class Standings {
    private final long elapsedTime;

    private final boolean showEnded;

    private final ImmutableMap<Column, ImmutableSet<String>> winners;

    private final ImmutableMap<Player, BigDecimal> scoreMap;

    private final ImmutableMap<Player, ImmutableSet<Player>> lostToMap;

    public Standings(Collection<Player> inPlayers, Results inResults) {
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(inResults.elapsedTimeMillis());
        showEnded = inResults.showEnded();
        winners = Column.CATEGORIES.stream()
                .collect(ImmutableMap.toImmutableMap(category -> category, inResults::winners));
        scoreMap = inPlayers.stream()
                .collect(ImmutableMap.toImmutableMap(player -> player, this::score));
        lostToMap = scoreMap.keySet().stream().collect(ImmutableMap.toImmutableMap(player -> player,
                player -> lostToStream(player, possibleScores(player, true), elapsedTime, showEnded)
                        .map(Entry::getKey).collect(ImmutableSet.toImmutableSet())));
    }

    private BigDecimal score(Player inPlayer) {
        return Column.CATEGORIES.stream()
                .filter(category -> winners.get(category).contains(inPlayer.answer(category)))
                .map(Column::value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** The elapsed time in seconds since the start of the broadcast */
    public long elapsedTime() {
        return elapsedTime;
    }

    /** Get the DOM Element for these Standings */
    public Element toDOM() {
        int scale = Column.CATEGORIES.stream().map(Column::value).mapToInt(BigDecimal::scale).max()
                .orElse(0);
        return scoreMap.keySet().stream()
                .map(player -> player.toDOM()
                        .setAttribute("score", scoreMap.get(player).setScale(scale).toString())
                        .setAttribute("rank", String.valueOf(
                                lostToStream(player, scoreMap, elapsedTime, true).count() + 1))
                        .setAttribute("bpr", String.valueOf(lostToMap.get(player).size() + 1))
                        .setAttribute("wpr", String.valueOf(worstPossibleRank(player)))
                        .setAttribute("decided", decided(player)))
                .reduce(new Element("standings"), Element::addContent)
                .setAttribute("time", String.valueOf(elapsedTime));
    }

    private static Stream<Entry<Player, BigDecimal>> lostToStream(Player inPlayer,
            ImmutableMap<Player, BigDecimal> inScoreMap, long inElapsedTime,
            boolean inCheckOverTime) {
        return inScoreMap.entrySet().stream()
                .filter(scoreEntry -> inScoreMap.get(inPlayer).compareTo(scoreEntry.getValue()) < 0
                        || inScoreMap.get(inPlayer).equals(scoreEntry.getValue())
                                && scoreEntry.getKey().time() <= inElapsedTime
                                && (inPlayer.time() < scoreEntry.getKey().time()
                                        || inCheckOverTime && inPlayer.time() > inElapsedTime));
    }

    private ImmutableMap<Player, BigDecimal> possibleScores(Player inPlayer, boolean inBest) {
        return scoreMap.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey,
                scoreEntry -> Column.CATEGORIES.stream()
                        .filter(category -> winners.get(category).isEmpty() && inBest == scoreEntry
                                .getKey().answer(category).equals(inPlayer.answer(category)))
                        .map(Column::value).reduce(scoreEntry.getValue(), BigDecimal::add)));
    }

    private long worstPossibleRank(Player inPlayer) {
        ImmutableMap<Player, BigDecimal> worstPossibleScores = possibleScores(inPlayer, false);
        return 1 + Math.max(
                lostToStream(inPlayer, worstPossibleScores,
                        showEnded ? elapsedTime : Long.MAX_VALUE, showEnded).count(),
                showEnded || inPlayer.time() <= elapsedTime ? 0
                        : lostToStream(inPlayer, worstPossibleScores, inPlayer.time() - 1, true)
                                .count());
    }

    private String decided(Player inPlayer) {
        return scoreMap.keySet().stream()
                .map(opponent -> opponent == inPlayer ? "-"
                        : lostToMap.get(opponent).contains(inPlayer) ? "W"
                                : lostToMap.get(inPlayer).contains(opponent) ? "L" : "?")
                .collect(Collectors.joining());
    }
}