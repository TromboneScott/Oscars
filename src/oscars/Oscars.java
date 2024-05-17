package oscars;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jdom2.Element;

/**
 * This program will allow Oscars winners to be entered and it will update the results on the web
 * site. Ballots with each player's guesses are downloaded from the web. A comma delimited file
 * indicates the categories on the ballot and all nominees for each category as they should be
 * displayed on the web site. The column name can include a tie breaker number inside parentheses
 * like this: Director(1) to indicate that Director is the first tie breaker. The players' names
 * must be in the columns named "First" and "Last" and their time estimate must be in the column
 * named "Time" in the format "H:MM" or "H:MM:SS.D".
 *
 * @author Scott McDonald
 * @version 5.0
 */
public class Oscars implements Runnable {
    private final List<Player> players;

    private final Results results;

    private Standings standings;

    private long validTimes = 0;

    private ZonedDateTime updated;

    /**
     * Prompt for Oscars results, store them and create output files
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] inArgs) throws Exception {
        new Oscars().process();
    }

    private Oscars() throws IOException {
        System.out.print("Step 1 of 4: Downloading ballots... ");
        CategoryMapper categoryMapper = new CategoryMapper();
        System.out.println("DONE");

        System.out.print("Step 2 of 4: Reading any existing results... ");
        players = categoryMapper.getPlayers();
        results = new Results(categoryMapper.getNomineeDescriptions());
        System.out.println("DONE");

        System.out.print("Step 3 of 4: Writing rank images... ");
        RankChart.writeAll(players.size());
        System.out.println("DONE");

        System.out.print("Step 4 of 4: Writing web pages... ");
        writeCategoryPages();
        writePlayerPages();
        System.out.println("DONE");
    }

    private void process() throws Exception {
        do
            System.out.println();
        while (prompt());

        System.out.print("\nWriting final results... ");
        writeResults(); // In case it was interrupted in the thread
        Directory.CATEGORY.cleanUpCharts(Category.stream()
                .map(category -> category.chartName(results.winners(category.name))));
        Directory.RANK.cleanUpCharts(
                players.stream().mapToLong(standings::rank).mapToObj(RankChart::name));
        System.out.println("DONE");
    }

    private boolean prompt() throws Exception {
        updated = ZonedDateTime.now();
        Thread thread = new Thread(this);
        try {
            thread.start();
            return results.prompt(players);
        } finally {
            thread.interrupt(); // Stop file I/O thread
            thread.join(); // Wait for it to finish
        }
    }

    /**
     * Run in a separate thread: Process the results and wait until we need to update the times
     * again. Continue until the main thread kills this thread or the show ends.
     */
    @Override
    public void run() {
        try {
            do {
                writeResults();
                Thread.sleep(waitTime(TimeUnit.MINUTES.toMillis(1)));
            } while (results.elapsedTimeMillis() > 0 && !standings.showEnded());
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private long waitTime(long inMaxWait) {
        long nextPlayerTime = players.stream().mapToLong(player -> player.time)
                .filter(playerTime -> playerTime > standings.elapsedTime)
                .map(TimeUnit.SECONDS::toMillis).min().orElse(Long.MAX_VALUE);
        long elapsedTimeMillis = results.elapsedTimeMillis();
        return Math.min(Math.max(nextPlayerTime - elapsedTimeMillis, 0),
                inMaxWait - elapsedTimeMillis % inMaxWait);
    }

    private void writeResults() throws IOException {
        standings = new Standings(players, results);
        long currentTimes = players.stream().filter(player -> player.time <= standings.elapsedTime)
                .count();
        if (validTimes != currentTimes)
            updated = ZonedDateTime.now();
        validTimes = currentTimes;
        Results.write(updated, standings.resultsCategoryDOM(), standings.resultsPlayerDOM(players),
                standings.resultsShowTimeDOM());
    }

    private void writeCategoryPages() throws IOException {
        Element all = new Element("categories");
        for (Category category : Category.stream().collect(Collectors.toList())) {
            category.writeChart(results, players);
            Directory.CATEGORY.write(category.toDOM(), category.webPage(), "category.xsl");
            all.addContent(category.toDOM(players));
        }
        Directory.CATEGORY.write(all, "all.xml", "categoryGraphs.xsl");
        Directory.CATEGORY.cleanUp();
    }

    private void writePlayerPages() throws IOException {
        for (Player player : players)
            Directory.PLAYER.write(player.toDOM(), player.webPage, "player.xsl");
        Directory.PLAYER.cleanUp();
    }
}