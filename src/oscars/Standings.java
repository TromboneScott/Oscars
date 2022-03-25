package oscars;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jdom2.Element;

/** The score and rank standings - Immutable */
public final class Standings {
    private final Map<Player, BigDecimal> scoreMap;

    private final Map<Player, Set<Player>> lostToMap;

    private final Map<Category, Set<String>> winners;

    private final Map<ShowTimeType, String> showTimes;

    public final long elapsedTime;

    public Standings(Collection<Player> inPlayers, Results inResults) {
        winners = Collections.unmodifiableMap(inPlayers.iterator().next().picks.keySet().stream()
                .collect(Collectors.toMap(category -> category, category -> Collections
                        .unmodifiableSet(new HashSet<>(inResults.winners(category))))));
        showTimes = Collections.unmodifiableMap(Stream.of(ShowTimeType.values())
                .collect(Collectors.toMap(showTimeType -> showTimeType, inResults::getShowTime)));
        scoreMap = Collections.unmodifiableMap(inPlayers.parallelStream()
                .collect(Collectors.toMap(player -> player, this::score)));
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(inResults.elapsedTimeMillis());
        lostToMap = Collections.unmodifiableMap(inPlayers.parallelStream()
                .collect(Collectors.toMap(player -> player, player -> lostTo(player, scoreMap))));
    }

    public boolean showEnded() {
        return !showTimes.get(ShowTimeType.END).isEmpty();
    }

    private BigDecimal score(Player inPlayer) {
        return inPlayer.picks.entrySet().stream()
                .filter(pickEntry -> winners.get(pickEntry.getKey()).contains(pickEntry.getValue()))
                .map(pickEntry -> pickEntry.getKey().value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long rank(Player inPlayer) {
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
                showEnded()).map(Entry::getKey).collect(Collectors.toSet());
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

    public Element resultsCategoryDOM(Category inCategory) {
        return new Element("category").addContent(new Element("name").addContent(inCategory.name))
                .addContent(inCategory.guesses.keySet().stream().sorted()
                        .map(guess -> new Element("nominee").addContent(guess).setAttribute(
                                "status",
                                winners.get(inCategory).isEmpty() ? "unannounced"
                                        : winners.get(inCategory).contains(guess) ? "correct"
                                                : "incorrect"))
                        .reduce(new Element("nominees"), Element::addContent));
    }

    public Element resultsPlayerDom(List<Player> inPlayers, int inPlayerNum, String inScoreFormat) {
        Player player = inPlayers.get(inPlayerNum);
        return player.toDOM().setAttribute("id", String.valueOf(inPlayerNum + 1))
                .addContent(new Element("rank").addContent(String.valueOf(rank(player))))
                .addContent(new Element("bpr")
                        .addContent(String.valueOf(lostToMap.get(player).size() + 1)))
                .addContent(new Element("wpr")
                        .addContent(String.valueOf(worstPossibleRank(player, scoreMap))))
                .addContent(new Element("score")
                        .addContent(String.format(inScoreFormat, scoreMap.get(player))))
                .addContent(new Element("time")
                        .setAttribute("delta",
                                player.time <= elapsedTime ? formatTime(elapsedTime - player.time)
                                        : showEnded() ? "OVER"
                                                : "-" + formatTime(player.time - elapsedTime))
                        .setAttribute("status",
                                player.time <= elapsedTime ? "correct"
                                        : showEnded() ? "incorrect" : "unannounced")
                        .addContent(formatTime(player.time)))
                .addContent(inPlayers.stream()
                        .map(opponent -> new Element("player")
                                .addContent(lostToMap.get(player).contains(opponent) ? "BETTER"
                                        : lostToMap.get(opponent).contains(player)
                                                || tied(player, opponent) ? "WORSE" : "TBD"))
                        .reduce(new Element("opponents"), Element::addContent));
    }

    /** Determine if the player and opponent will be tied at the end of the game */
    private boolean tied(Player inPlayer, Player inOpponent) {
        return (inPlayer.time == inOpponent.time
                || showEnded() && inPlayer.time > elapsedTime && inOpponent.time > elapsedTime)
                && scoreMap.get(inPlayer).equals(scoreMap.get(inOpponent))
                && inPlayer.picks.keySet().stream()
                        .allMatch(category -> !winners.get(category).isEmpty() || inPlayer.picks
                                .get(category).equals(inOpponent.picks.get(category)));
    }

    public Element resultsShowTimeDOM() {
        String timeString = formatTime(elapsedTime);
        return Arrays.stream(ShowTimeType.values())
                .map(showTimeType -> new Element(showTimeType.name().toLowerCase())
                        .addContent(showTimes.get(showTimeType)))
                .reduce(new Element("showTime"), Element::addContent)
                .addContent(new Element("length").addContent(timeString))
                .addContent(new Element("header")
                        .addContent("Time" + (showEnded() ? "=" : ">") + timeString));
    }

    private String formatTime(long inTime) {
        return inTime < 0 ? ""
                : String.format("%d:%02d:%02d", TimeUnit.SECONDS.toHours(inTime),
                        TimeUnit.SECONDS.toMinutes(inTime) % 60, inTime % 60);
    }

    /** Get a stream of the current ranks of all the players */
    public LongStream rankStream() {
        return scoreMap.keySet().stream().mapToLong(this::rank);
    }
}