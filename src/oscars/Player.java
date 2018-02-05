package oscars;

/** Player information */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

public class Player implements Cloneable {
	private static final String PSEUDO_TAG = "PSEUDO-";

	/** Player's first name */
	public final String firstName;

	/** Player's last name */
	public final String lastName;

	/** Player's picks - immutable */
	public final Map<Category, String> picks;

	public final boolean isPseudo;

	/** Player's guessed time in seconds, -1 if invalid */
	public final long time;

	/** Player's current score */
	private BigDecimal score;

	/** Player's current rank */
	private int rank;

	/** Player's current best possible rank */
	private int bestPossibleRank;

	/** Player's current worst possible rank */
	private int worstPossibleRank;

	/**
	 * Constructs a new Player with specified picks
	 * 
	 * @param inEntries
	 *            All the entries for this Player
	 */
	public Player(Map<Category, String> inEntries) {
		String tempFirstName = inEntries.get(Category.FIRST_NAME);
		String tempLastName = inEntries.get(Category.LAST_NAME);
		isPseudo = tempFirstName.startsWith(PSEUDO_TAG);
		if (isPseudo)
			tempFirstName = tempFirstName.substring(PSEUDO_TAG.length());
		if (tempLastName.isEmpty()) {
			tempLastName = tempFirstName;
			tempFirstName = "";
		}
		firstName = tempFirstName.trim();
		lastName = tempLastName.trim();
		picks = Collections.unmodifiableMap(picks(inEntries));
		time = time(inEntries.get(Category.TIME));
	}

	private Map<Category, String> picks(Map<Category, String> inEntries) {
		Map<Category, String> picks = new HashMap<Category, String>(inEntries.size());
		for (Category category : inEntries.keySet())
			if (!category.guesses.isEmpty())
				picks.put(category, inEntries.get(category));
		return picks;
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
		score = BigDecimal.ZERO;
		for (Category category : picks.keySet())
			if (inResults.winners(category).contains(picks.get(category)))
				score = score.add(category.value);
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
	 * Set this Player's rank based on the scores of all the players which must
	 * already be set
	 * 
	 * @param inPlayers
	 *            The list of the Players (including this Player)
	 * @param inRunningTime
	 *            The running time of the show or -1 if not finished
	 * @param inElapsedTime
	 *            The elapsed time of the show so far
	 */
	public void setRank(Collection<Player> inPlayers, long inRunningTime, long inElapsedTime) {
		rank = calculateRank(inPlayers, inRunningTime, inElapsedTime, false);
	}

	/**
	 * Get the rank of this Player
	 * 
	 * @return The rank of this Player
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Set the Best Possible Rank based on the scores of all players which must
	 * already be set
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
	public void setBestPossibleRank(Results inResults, Collection<Player> inPlayers, long inRunningTime,
			long inElapsedTime) {
		bestPossibleRank = getUpdatedPlayer(this, inResults, true).calculateRank(
				getUpdatedPlayers(inPlayers, inResults, true), inRunningTime,
				time > inElapsedTime ? time : inElapsedTime, false);
	}

	/**
	 * Determine this Player's best possible rank if all remaining guesses turn out
	 * to be correct
	 * 
	 * @return This Player's best possible rank
	 */
	public int getBestPossibleRank() {
		return bestPossibleRank;
	}

	/**
	 * Set the Worst Possible Rank based on the scores of all players which must
	 * already be set
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
	public void setWorstPossibleRank(Results inResults, Collection<Player> inPlayers, long inRunningTime,
			long inElapsedTime) {
		worstPossibleRank = calculateRank(getUpdatedPlayers(inPlayers, inResults, false), inRunningTime, inElapsedTime,
				inRunningTime < 0);
	}

	/**
	 * Determine this Player's worst possible rank if all remaining guesses turn out
	 * to be incorrect
	 * 
	 * @return This Player's worst possible rank
	 */
	public int getWorstPossibleRank() {
		return worstPossibleRank;
	}

	private int calculateRank(Collection<Player> inPlayers, long inRunningTime, double inElapsedTime, boolean inWorst) {
		double runningTime = inRunningTime < 0 ? inElapsedTime : inRunningTime;

		int tempRank = 1;
		for (Player opponent : inPlayers)
			if (!opponent.isPseudo && (opponent.score.compareTo(score) > 0 || opponent.score.equals(score)
					&& time != opponent.time && opponent.time >= 0 && (inWorst || opponent.time <= runningTime)
					&& (time < opponent.time || time > runningTime)))
				tempRank++;
		return tempRank;
	}

	private Collection<Player> getUpdatedPlayers(Collection<Player> inPlayers, Results inResults, boolean inBest) {
		Collection<Player> clonedPlayers = new ArrayList<Player>(inPlayers.size());
		for (Player player : inPlayers)
			clonedPlayers.add(getUpdatedPlayer(player, inResults, inBest));
		return clonedPlayers;
	}

	private Player getUpdatedPlayer(Player inPlayer, Results inResults, boolean inBest) {
		try {
			Player clonedPlayer = (Player) inPlayer.clone();
			for (Category category : picks.keySet())
				if (inResults.winners(category).isEmpty()
						&& inBest == clonedPlayer.picks.get(category).equals(picks.get(category)))
					clonedPlayer.score = clonedPlayer.score.add(category.value);
			return clonedPlayer;
		} catch (CloneNotSupportedException e) {
			throw new UnsupportedOperationException(getClass() + " needs to implement Cloneable", e);
		}
	}

	/**
	 * Player's guessed time in seconds, -1 if over the actual running time or
	 * invalid
	 * 
	 * @param inRunningTime
	 *            Actual running time of broadcast in seconds
	 */
	public double getTime(long inRunningTime) {
		return inRunningTime >= 0 && time > inRunningTime ? -1 : time;
	}

	public Element toCoreDOM() {
		Element playerDOM = new Element("player");
		playerDOM.addContent(new Element("firstName").addContent(firstName));
		playerDOM.addContent(new Element("lastName").addContent(lastName));
		return playerDOM;
	}

	public Element toDOM(Collection<Category> inCategories) {
		Element playerDOM = toCoreDOM();
		playerDOM.addContent(playerCategoriesDOM(inCategories));
		return playerDOM;
	}

	private Element playerCategoriesDOM(Collection<Category> inCategories) {
		Element categoriesDOM = new Element("categories");
		for (Category category : inCategories) {
			Element categoryDOM = category.toCoreDOM();
			categoryDOM.addContent(new Element("guess").addContent(picks.get(category)));
			categoriesDOM.addContent(categoryDOM);
		}
		return categoriesDOM;
	}
}