package oscars;

/** Player information */
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

public class Player implements Cloneable {
    /** Player's first name */
    public final String firstName;

    /** Player's last name */
    public final String lastName;

    /** Player's picks - immutable */
    public final Map<Category, String> picks;

    /** Player's guessed time in seconds */
    public final long time;

    /** Player's current score */
    private BigDecimal score;

    /** Player's current rank */
    private long rank;

    /** Player's worst possible rank */
    private long worstPossibleRank;

    /** Players that this player has lost to */
    private Set<Player> lostTo;

    /**
     * Constructs a new Player with specified picks
     *
     * @param inEntries
     *            All the entries for this Player
     */
    public Player(Map<Category, String> inEntries) {
        String tempFirstName = inEntries.get(Category.FIRST_NAME);
        String tempLastName = inEntries.get(Category.LAST_NAME);
        if (tempLastName.isEmpty()) {
            tempLastName = tempFirstName;
            tempFirstName = "";
        }
        firstName = tempFirstName.trim();
        lastName = tempLastName.trim();
        picks = Collections.unmodifiableMap(
                inEntries.entrySet().stream().filter(entry -> !entry.getKey().guesses.isEmpty())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        time = time(inEntries.get(Category.TIME));
    }

    private long time(String inTimeString) {
        return Stream.of(inTimeString.split(":", 3)).mapToLong(Long::parseLong).reduce(0,
                (subtotal, element) -> subtotal * 60 + element);
    }

    /**
     * Set this Players's score based on the given Results
     *
     * @param inResults
     *            Results to base the score on
     */
    public void setScore(Results inResults) {
        score = picks.entrySet().stream()
                .filter(entry -> inResults.winners(entry.getKey()).contains(entry.getValue()))
                .map(entry -> entry.getKey().value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get this Player's score
     *
     * @return This Player's score
     */
    public BigDecimal getScore() {
        return score;
    }

    /**
     * Set the Current, Best Possible and Worst Possible Rank based on the scores of all players
     * which must already be set
     *
     * @param inResults
     *            The Results to base the ranks on
     * @param inPlayers
     *            The list of Players (including this one)
     * @param inRunningTime
     *            The running time of the show or -1 if not finished
     * @param inElapsedTime
     *            The elapsed time of the show so far
     */
    public void setRanks(Results inResults, Collection<Player> inPlayers, long inRunningTime,
            long inElapsedTime) {
        rank = 1 + lostToStream(inPlayers, inRunningTime, inElapsedTime).count();
        lostTo = getUpdatedPlayer(this, inResults, true)
                .lostToStream(getUpdatedPlayers(inPlayers, inResults, true), inRunningTime,
                        time > inElapsedTime ? time : inElapsedTime)
                .collect(Collectors.toSet());
        Collection<Player> worstCasePlayers = getUpdatedPlayers(inPlayers, inResults, false);
        worstPossibleRank = 1 + Math.max(time > inElapsedTime
                ? lostToStream(worstCasePlayers, inRunningTime, time - 1).count()
                : 0, lostToStream(worstCasePlayers, inRunningTime, Long.MAX_VALUE).count());
    }

    /**
     * Get the rank of this Player
     *
     * @return The rank of this Player
     */
    public long getRank() {
        return rank;
    }

    /**
     * Determine this Player's best possible rank if all remaining guesses turn out to be correct
     *
     * @return This Player's best possible rank
     */
    public long getBestPossibleRank() {
        return lostTo.size() + 1;
    }

    /**
     * Determine this Player's worst possible rank if all remaining guesses turn out to be incorrect
     *
     * @return This Player's worst possible rank
     */
    public long getWorstPossibleRank() {
        return worstPossibleRank;
    }

    /**
     * Determine if this Player and the opponent will be tied at the end of the contest
     * 
     * @param inOpponent
     * @return Whether or not they have all same guesses including the time
     */
    public boolean tiedWith(Player inOpponent, Results inResults, long inRunningTime) {
        return (time == inOpponent.time
                || inRunningTime >= 0 && time > inRunningTime && inOpponent.time > inRunningTime)
                && getUpdatedPlayer(this, inResults, true).score
                        .equals(getUpdatedPlayer(inOpponent, inResults, true).score);
    }

    public boolean lostTo(Player inOpponent) {
        return lostTo.contains(inOpponent);
    }

    private Stream<Player> lostToStream(Collection<Player> inPlayers, long inRunningTime,
            double inElapsedTime) {
        double runningTime = inRunningTime < 0 ? inElapsedTime : inRunningTime;
        return inPlayers.stream()
                .filter(opponent -> opponent.score.compareTo(score) > 0
                        || opponent.score.equals(score) && opponent.time <= runningTime
                                && (time < opponent.time || time > runningTime));
    }

    private Collection<Player> getUpdatedPlayers(Collection<Player> inPlayers, Results inResults,
            boolean inBest) {
        return inPlayers.stream().map(player -> getUpdatedPlayer(player, inResults, inBest))
                .collect(Collectors.toList());
    }

    private Player getUpdatedPlayer(Player inPlayer, Results inResults, boolean inBest) {
        try {
            Player clonedPlayer = (Player) inPlayer.clone();
            clonedPlayer.score = picks.entrySet().stream().filter(entry -> inResults
                    .winners(entry.getKey()).isEmpty()
                    && inBest == clonedPlayer.picks.get(entry.getKey()).equals(entry.getValue()))
                    .map(entry -> entry.getKey().value).reduce(clonedPlayer.score, BigDecimal::add);
            return clonedPlayer;
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(getClass() + " needs to implement Cloneable",
                    e);
        }
    }

    public Element toDOM() {
        return new Element("player").addContent(new Element("firstName").addContent(firstName))
                .addContent(new Element("lastName").addContent(lastName));
    }

    @Override
    public int hashCode() {
        return (lastName + ", " + firstName).hashCode();
    }

    @Override
    public boolean equals(Object inPlayer) {
        return firstName.equals(((Player) inPlayer).firstName)
                && lastName.equals(((Player) inPlayer).lastName);
    }
}