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

    /** The Standings based on the current Results */
    public Standings() {
        elapsedTime = Oscars.RESULTS.elapsedTimeSeconds();
        showEnded = Oscars.RESULTS.showEnded();
        winners = Category.ALL.stream().collect(
                ImmutableMap.toImmutableMap(category -> category, Oscars.RESULTS::winners));
        scoreMap = Oscars.PLAYERS.stream()
                .collect(ImmutableMap.toImmutableMap(player -> player, this::score));
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
        ImmutableMap<Player, ImmutableSet<Player>> lostToMap = scoreMap.keySet().stream()
                .collect(ImmutableMap.toImmutableMap(player -> player, this::lostTo));
        return scoreMap.keySet().stream()
                .map(player -> player.toDOM()
                        .setAttribute("score", scoreMap.get(player).setScale(scale).toString())
                        .setAttribute("decided", decided(player, lostToMap)))
                .reduce(new Element("standings"), Element::addContent)
                .setAttribute("time", String.valueOf(elapsedTime));
    }

    private ImmutableSet<Player> lostTo(Player inPlayer) {
        ImmutableMap<Player, BigDecimal> possibleScoreMap = scoreMap.entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> Category.ALL.stream()
                        .filter(category -> winners.get(category).isEmpty()
                                && entry.getKey().answer(category) == inPlayer.answer(category))
                        .map(Category::value).reduce(entry.getValue(), BigDecimal::add)));
        return scoreMap.keySet().stream().filter(opponent -> possibleScoreMap.get(opponent)
                .compareTo(possibleScoreMap.get(inPlayer)) > 0
                || possibleScoreMap.get(opponent).compareTo(possibleScoreMap.get(inPlayer)) == 0
                        && opponent.time() <= elapsedTime
                        && (inPlayer.time() < opponent.time()
                                || showEnded && inPlayer.time() > elapsedTime))
                .collect(ImmutableSet.toImmutableSet());
    }

    /** Values: - = This Player, W = Won, L = Lost, T = Tied, X = Score could tie, ? = Undecided */
    private String decided(Player inPlayer,
            ImmutableMap<Player, ImmutableSet<Player>> inLostToMap) {
        return scoreMap.keySet().stream().map(opponent -> opponent == inPlayer ? "-"
                : inLostToMap.get(opponent).contains(inPlayer) ? "W"
                        : inLostToMap.get(inPlayer).contains(opponent) ? "L"
                                : !disagreementStream(inPlayer, opponent).findAny().isPresent()
                                        && (showEnded || opponent.time() == inPlayer.time()) ? "T"
                                                : couldTieScore(inPlayer, opponent) ? "X" : "?")
                .collect(Collectors.joining());
    }

    private boolean couldTieScore(Player inPlayer, Player inOpponent) {
        return scoreMap.get(inPlayer).subtract(scoreMap.get(inOpponent)).abs()
                .compareTo(disagreementStream(inPlayer, inOpponent).map(Category::value)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)) == 0;
    }

    private Stream<Category> disagreementStream(Player inPlayer, Player inOpponent) {
        return Category.ALL.stream().filter(category -> winners.get(category).isEmpty()
                && !inPlayer.answer(category).equals(inOpponent.answer(category)));
    }
}