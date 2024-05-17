package oscars;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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

    private final Map<ShowTimeType, String> showTimes;

    public final long elapsedTime;

    public Standings(Collection<Player> inPlayers, Results inResults) {
        winners = Collections.unmodifiableMap(Category.stream().map(category -> category.name)
                .collect(Collectors.toMap(category -> category, category -> Collections
                        .unmodifiableSet(new HashSet<>(inResults.winners(category))))));
        showTimes = Collections.unmodifiableMap(Stream.of(ShowTimeType.values())
                .collect(Collectors.toMap(showTimeType -> showTimeType, inResults::get)));
        scoreMap = Collections.unmodifiableMap(
                inPlayers.stream().collect(Collectors.toMap(player -> player, this::score)));
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(inResults.elapsedTimeMillis());
        lostToMap = Collections
                .unmodifiableMap(inPlayers.stream().collect(Collectors.toMap(player -> player,
                        player -> Collections.unmodifiableSet(lostTo(player, scoreMap)))));
    }

    public boolean showEnded() {
        return !showTimes.get(ShowTimeType.END).isEmpty();
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
                showEnded()).map(Entry::getKey).collect(Collectors.toSet());
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

    public Element resultsCategoryDOM() {
        return Category.stream()
                .map(category -> new Element("category").setAttribute("name", category.name)
                        .setAttribute("webPage", category.webPage())
                        .setAttribute("chart", category.chartName(winners.get(category.name)))
                        .addContent(winners.get(category.name).stream()
                                .map(winner -> new Element("nominee").setAttribute("name", winner))
                                .reduce(new Element("winners"), Element::addContent)))
                .reduce(new Element("categories"), Element::addContent);
    }

    public Element resultsPlayerDOM(List<Player> inPlayers) {
        int tieBreakers = (int) Category.stream().filter(category -> !category.tieBreaker.isEmpty())
                .count();
        return IntStream.range(0, inPlayers.size()).mapToObj(playerNum -> inPlayers.get(playerNum)
                .toDOM().setAttribute("id", String.valueOf(playerNum + 1))
                .addContent(rankDOM(rank(inPlayers.get(playerNum))))
                .addContent(new Element("bpr").addContent(
                        String.valueOf(lostToMap.get(inPlayers.get(playerNum)).size() + 1)))
                .addContent(new Element("wpr").addContent(
                        String.valueOf(worstPossibleRank(inPlayers.get(playerNum), scoreMap))))
                .addContent(new Element("score").addContent(scoreMap.get(inPlayers.get(playerNum))
                        .setScale(tieBreakers).toString()))
                .addContent(
                        new Element("time")
                                .setAttribute("delta", inPlayers.get(playerNum).time <= elapsedTime
                                        ? formatTime(elapsedTime - inPlayers.get(playerNum).time)
                                        : showEnded() ? "OVER"
                                                : "-" + formatTime(inPlayers.get(playerNum).time
                                                        - elapsedTime))
                                .setAttribute("status",
                                        inPlayers.get(playerNum).time <= elapsedTime ? "correct"
                                                : showEnded() ? "incorrect" : "unannounced")
                                .addContent(formatTime(inPlayers.get(playerNum).time)))
                .addContent(new Element("opponents").setAttribute("decided", inPlayers.stream()
                        .map(opponent -> lostToMap.get(inPlayers.get(playerNum)).contains(opponent)
                                || lostToMap.get(opponent).contains(inPlayers.get(playerNum))
                                || tied(inPlayers.get(playerNum), opponent) ? "Y" : "N")
                        .collect(Collectors.joining()))))
                .reduce(new Element("players"), Element::addContent);
    }

    private Element rankDOM(long inRank) {
        return new Element("rank").setAttribute("chart", RankChart.name(inRank))
                .addContent(String.valueOf(inRank));
    }

    /** Determine if the player and opponent will be tied at the end of the game */
    private boolean tied(Player inPlayer, Player inOpponent) {
        return (inPlayer.time == inOpponent.time
                || inPlayer.time > elapsedTime && inOpponent.time > elapsedTime && showEnded())
                && scoreMap.get(inPlayer).equals(scoreMap.get(inOpponent))
                && Category.stream().map(category -> category.name)
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
        return inTime < 0 ? "" : LocalTime.ofSecondOfDay(inTime).format(Player.TIME_FORMAT);
    }

    public static Map<String, Set<String>> winners(Element inResultsDOM) {
        return Optional.ofNullable(inResultsDOM.getChild("categories"))
                .map(element -> element.getChildren("category").stream()).orElseGet(Stream::empty)
                .collect(
                        Collectors.toMap(categoryDOM -> categoryDOM.getAttributeValue("name"),
                                categoryDOM -> Collections.unmodifiableSet(categoryDOM
                                        .getChild("winners").getChildren("nominee").stream()
                                        .map(element -> element.getAttributeValue("name"))
                                        .collect(Collectors.toSet()))));
    }

    public static Map<ShowTimeType, ZonedDateTime> showTimes(Element inResultsDOM) {
        return Stream.of(ShowTimeType.values())
                .map(showTimeType -> new SimpleEntry<>(showTimeType,
                        Optional.ofNullable(inResultsDOM.getChild("showTime")).map(
                                element -> element.getChildText(showTimeType.name().toLowerCase()))
                                .orElse("")))
                .filter(entry -> !entry.getValue().isEmpty()).collect(Collectors.toMap(
                        entry -> entry.getKey(), entry -> ZonedDateTime.parse(entry.getValue())));
    }
}