package oscars;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

/** The score and rank standings - Immutable */
public final class Standings {
    private final long elapsedTime;

    private final boolean showEnded;

    private final Map<String, Set<String>> winners;

    private final Map<Player, BigDecimal> scoreMap;

    public Standings(List<Player> inPlayers, Results inResults) {
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(inResults.elapsedTimeMillis());
        showEnded = inResults.showEnded();
        winners = Collections
                .unmodifiableMap(Category.ALL.stream().map(category -> category.getName())
                        .collect(Collectors.toMap(category -> category, category -> Collections
                                .unmodifiableSet(new HashSet<>(inResults.winners(category))))));
        scoreMap = Collections.unmodifiableMap(inPlayers.stream().collect(Collectors
                .toMap(player -> player, this::score, BigDecimal::min, LinkedHashMap::new)));
    }

    private BigDecimal score(Player inPlayer) {
        return Category.ALL.stream()
                .filter(category -> winners.get(category.getName())
                        .contains(inPlayer.getPick(category.getName())))
                .map(Category::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Get the elapsed time in seconds since the start of the broadcast */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /** Get the DOM Element for these Standings */
    public Element toDOM() {
        int scale = Category.ALL.stream().map(Category::getValue).mapToInt(BigDecimal::scale).max()
                .orElse(0);
        Map<Player, Set<Player>> lostToMap = scoreMap.keySet().stream().collect(Collectors.toMap(
                player -> player,
                player -> lostToStream(player, possibleScores(player, true), elapsedTime, showEnded)
                        .map(Entry::getKey).collect(Collectors.toSet())));
        return scoreMap.keySet().stream()
                .map(player -> player.toDOM()
                        .setAttribute("score", scoreMap.get(player).setScale(scale).toString())
                        .setAttribute("rank", String.valueOf(
                                lostToStream(player, scoreMap, elapsedTime, true).count() + 1))
                        .setAttribute("bpr", String.valueOf(lostToMap.get(player).size() + 1))
                        .setAttribute("wpr", String.valueOf(worstPossibleRank(player)))
                        .setAttribute("decided",
                                scoreMap.keySet().stream()
                                        .map(opponent -> lostToMap.get(player).contains(opponent)
                                                || lostToMap.get(opponent).contains(player)
                                                || tied(player, opponent) ? "Y" : "N")
                                        .collect(Collectors.joining())))
                .reduce(new Element("standings"), Element::addContent)
                .setAttribute("time", String.valueOf(elapsedTime));
    }

    private Stream<Entry<Player, BigDecimal>> lostToStream(Player inPlayer,
            Map<Player, BigDecimal> inScoreMap, long inElapsedTime, boolean inCheckOverTime) {
        return inScoreMap.entrySet().stream()
                .filter(scoreEntry -> inScoreMap.get(inPlayer).compareTo(scoreEntry.getValue()) < 0
                        || inScoreMap.get(inPlayer).equals(scoreEntry.getValue())
                                && scoreEntry.getKey().getTime() <= inElapsedTime
                                && (inPlayer.getTime() < scoreEntry.getKey().getTime()
                                        || inCheckOverTime && inPlayer.getTime() > inElapsedTime));
    }

    private Map<Player, BigDecimal> possibleScores(Player inPlayer, boolean inBest) {
        return scoreMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, scoreEntry -> Category.ALL.stream()
                        .filter(category -> winners.get(category.getName()).isEmpty()
                                && inBest == scoreEntry.getKey().getPick(category.getName())
                                        .equals(inPlayer.getPick(category.getName())))
                        .map(Category::getValue).reduce(scoreEntry.getValue(), BigDecimal::add)));
    }

    private long worstPossibleRank(Player inPlayer) {
        Map<Player, BigDecimal> worstPossibleScores = possibleScores(inPlayer, false);
        return 1 + Math.max(
                lostToStream(inPlayer, worstPossibleScores,
                        showEnded ? elapsedTime : Long.MAX_VALUE, showEnded).count(),
                showEnded || inPlayer.getTime() <= elapsedTime ? 0
                        : lostToStream(inPlayer, worstPossibleScores, inPlayer.getTime() - 1, true)
                                .count());
    }

    /** Determine if the player and their opponent will be tied at the end of the game */
    private boolean tied(Player inPlayer, Player inOpponent) {
        return (inPlayer.getTime() == inOpponent.getTime() || showEnded
                && inPlayer.getTime() > elapsedTime && inOpponent.getTime() > elapsedTime)
                && scoreMap.get(inPlayer).equals(scoreMap.get(inOpponent))
                && winners.keySet().stream().allMatch(category -> !winners.get(category).isEmpty()
                        || inPlayer.getPick(category).equals(inOpponent.getPick(category)));
    }
}