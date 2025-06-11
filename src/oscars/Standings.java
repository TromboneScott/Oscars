package oscars;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import oscars.ballot.Player;
import oscars.column.Category;

/** The current score and rank standings - Immutable */
final class Standings {
    private final long elapsedTime;

    private final boolean showEnded;

    private final ImmutableMap<Category, ImmutableSet<String>> winners;

    private final ImmutableMap<Player, BigDecimal> scoreMap;

    private final ImmutableMap<Player, ImmutableSet<Player>> lostToMap;

    /** The Standings based on the current Results */
    public Standings() {
        elapsedTime = Oscars.RESULTS.elapsedTimeSeconds();
        showEnded = Oscars.RESULTS.showEnded();
        winners = Category.ALL.stream().collect(
                ImmutableMap.toImmutableMap(category -> category, Oscars.RESULTS::winners));
        scoreMap = Oscars.PLAYERS.stream()
                .collect(ImmutableMap.toImmutableMap(player -> player, this::score));
        lostToMap = scoreMap.keySet().stream().collect(ImmutableMap.toImmutableMap(player -> player,
                player -> lostToStream(player, possibleScores(player, true), elapsedTime, showEnded)
                        .map(Entry::getKey).collect(ImmutableSet.toImmutableSet())));
    }

    private BigDecimal score(Player inPlayer) {
        return Category.ALL.stream()
                .filter(category -> winners.get(category).contains(inPlayer.answer(category)))
                .map(Category::value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Get the DOM Element for these Standings */
    public Element toDOM() {
        int scale = Category.ALL.stream().map(Category::value).mapToInt(BigDecimal::scale).max()
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
                scoreEntry -> Category.ALL.stream()
                        .filter(category -> winners.get(category).isEmpty() && inBest == scoreEntry
                                .getKey().answer(category).equals(inPlayer.answer(category)))
                        .map(Category::value).reduce(scoreEntry.getValue(), BigDecimal::add)));
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
        return scoreMap.keySet().stream().map(opponent -> opponent == inPlayer ? "-"
                : lostToMap.get(opponent).contains(inPlayer) ? "W"
                        : lostToMap.get(inPlayer).contains(opponent) ? "L"
                                : scoreMap.get(opponent).equals(scoreMap.get(inPlayer))
                                        && Category.ALL.stream().allMatch(
                                                category -> !winners.get(category).isEmpty()
                                                        || opponent.answer(category) == inPlayer
                                                                .answer(category)) ? opponent
                                                                        .time() == inPlayer.time()
                                                                                ? "T"
                                                                                : "X"
                                                                        : "?")
                .collect(Collectors.joining());
    }
}