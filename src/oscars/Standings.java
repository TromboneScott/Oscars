package oscars;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;

/** The score and rank standings - Immutable */
public final class Standings {
    private final Map<Player, BigDecimal> scoreMap;

    private final Map<Player, Set<Player>> lostToMap;

    private final Map<String, Set<String>> winners;

    public final boolean showEnded;

    public final long elapsedTime;

    public Standings(Collection<Player> inPlayers, Results inResults) {
        winners = Collections.unmodifiableMap(Category.stream().map(category -> category.name)
                .collect(Collectors.toMap(category -> category, category -> Collections
                        .unmodifiableSet(new HashSet<>(inResults.winners(category))))));
        showEnded = !inResults.get(ShowTimeType.END).isEmpty();
        scoreMap = Collections.unmodifiableMap(
                inPlayers.stream().collect(Collectors.toMap(player -> player, this::score)));
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(inResults.elapsedTimeMillis());
        lostToMap = Collections
                .unmodifiableMap(inPlayers.stream().collect(Collectors.toMap(player -> player,
                        player -> Collections.unmodifiableSet(lostTo(player, scoreMap)))));
    }

    private BigDecimal score(Player inPlayer) {
        return Category.stream().filter(
                category -> winners.get(category.name).contains(inPlayer.picks.get(category.name)))
                .map(category -> category.value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long rank(Player inPlayer) {
        return lostToStream(inPlayer, scoreMap, elapsedTime, true).count() + 1;
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
                        scoreEntry -> Category.stream()
                                .filter(category -> winners.get(category.name).isEmpty()
                                        && inBest == scoreEntry.getKey().picks.get(category.name)
                                                .equals(inPlayer.picks.get(category.name)))
                                .map(category -> category.value)
                                .reduce(scoreEntry.getValue(), BigDecimal::add)));
    }

    public Element toDOM(List<Player> inPlayers) {
        int tieBreakers = (int) Category.stream()
                .filter(category -> !category.value.equals(BigDecimal.ONE)).count();
        return IntStream.range(0, inPlayers.size()).mapToObj(playerNum -> inPlayers.get(playerNum)
                .toDOM()
                .setAttribute("score",
                        scoreMap.get(inPlayers.get(playerNum)).setScale(tieBreakers).toString())
                .setAttribute("rank", String.valueOf(rank(inPlayers.get(playerNum))))
                .setAttribute("bpr",
                        String.valueOf(lostToMap.get(inPlayers.get(playerNum)).size() + 1))
                .setAttribute("wpr",
                        String.valueOf(worstPossibleRank(inPlayers.get(playerNum), scoreMap)))
                .setAttribute("time", String.valueOf(inPlayers.get(playerNum).time))
                .setAttribute("decided", inPlayers.stream()
                        .map(opponent -> lostToMap.get(inPlayers.get(playerNum)).contains(opponent)
                                || lostToMap.get(opponent).contains(inPlayers.get(playerNum))
                                || tied(inPlayers.get(playerNum), opponent) ? "Y" : "N")
                        .collect(Collectors.joining())))
                .reduce(new Element("standings"), Element::addContent);
    }

    /** Determine if the player and opponent will be tied at the end of the game */
    private boolean tied(Player inPlayer, Player inOpponent) {
        return (inPlayer.time == inOpponent.time
                || inPlayer.time > elapsedTime && inOpponent.time > elapsedTime && showEnded)
                && scoreMap.get(inPlayer).equals(scoreMap.get(inOpponent))
                && Category.stream().map(category -> category.name)
                        .allMatch(category -> !winners.get(category).isEmpty() || inPlayer.picks
                                .get(category).equals(inOpponent.picks.get(category)));
    }
}