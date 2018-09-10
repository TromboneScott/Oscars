package oscars;

/** Player information */
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jdom2.Element;

public class Player implements Cloneable {
    /** Player's first name */
    public final String firstName;

    /** Player's last name */
    public final String lastName;

    /** Player's picks - immutable */
    public final Map<Category, String> picks;

    /** Player's guessed time in seconds, -1 if invalid */
    public final long time;

    /** Player's current score */
    private BigDecimal score;

    /** Player's current rank */
    private long rank;

    /** Players that this player will lose to */
    private Set<Player> bestPossibleRankPlayers;

    /** Players that will lose to this player */
    private Set<Player> worstPossibleRankPlayers;

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
        String[] timeArray = inTimeString.split(":", 3);
        return timeArray.length < 2 ? -1
                : TimeUnit.HOURS.toSeconds(Long.parseLong(timeArray[0]))
                        + TimeUnit.MINUTES.toSeconds(Long.parseLong(timeArray[1]))
                        + (timeArray.length > 2 ? Long.parseLong(timeArray[2]) : 0);
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
     *            The Results to base the status on
     * @param inPlayers
     *            The list of Players (including this one)
     * @param inRunningTime
     *            The running time of the show or -1 if not finished
     * @param inElapsedTime
     *            The elapsed time of the show so far
     */
    public void setRanks(Results inResults, Collection<Player> inPlayers, long inRunningTime,
            long inElapsedTime) {
        rank = filter(inPlayers, inRunningTime, inElapsedTime, false).size() + 1;
        bestPossibleRankPlayers = getUpdatedPlayer(this, inResults, true).filter(
                getUpdatedPlayers(inPlayers, inResults, true), inRunningTime,
                time > inElapsedTime ? time : inElapsedTime, false);
        worstPossibleRankPlayers = filter(getUpdatedPlayers(inPlayers, inResults, false),
                inRunningTime, inElapsedTime, inRunningTime < 0);
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
        return bestPossibleRankPlayers.size() + 1;
    }

    /**
     * Determine this Player's worst possible rank if all remaining guesses turn out to be incorrect
     * 
     * @return This Player's worst possible rank
     */
    public long getWorstPossibleRank() {
        return worstPossibleRankPlayers.size() + 1;
    }

    public boolean isBetterThan(Player inPlayer) {
        return !worstPossibleRankPlayers.contains(inPlayer);
    }

    public boolean isWorseThan(Player inPlayer) {
        return bestPossibleRankPlayers.contains(inPlayer);
    }

    private Set<Player> filter(Collection<Player> inPlayers, long inRunningTime,
            double inElapsedTime, boolean inWorst) {
        double runningTime = inRunningTime < 0 ? inElapsedTime : inRunningTime;
        return inPlayers.stream()
                .filter(opponent -> opponent.score.compareTo(score) > 0
                        || opponent.score.equals(score) && time != opponent.time
                                && opponent.time >= 0 && (inWorst || opponent.time <= runningTime)
                                && (time < opponent.time || time > runningTime))
                .collect(Collectors.toSet());
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

    /**
     * Player's guessed time in seconds, -1 if over the actual running time or invalid
     * 
     * @param inRunningTime
     *            Actual running time of broadcast in seconds
     */
    public long getTime(long inRunningTime) {
        return inRunningTime >= 0 && time > inRunningTime ? -1 : time;
    }

    public Element toCoreDOM() {
        return new Element("player").addContent(new Element("firstName").addContent(firstName))
                .addContent(new Element("lastName").addContent(lastName));
    }

    public Element toDOM(Collection<Category> inCategories) {
        return toCoreDOM().addContent(inCategories.stream()
                .map(category -> category.toCoreDOM()
                        .addContent(new Element("guess").addContent(picks.get(category))))
                .reduce(new Element("categories"), Element::addContent));
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