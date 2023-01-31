package oscars;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jdom2.Element;

/**
 * This program will allow Oscars winners to be selected and a new results file will be generated.
 * The Oscars picks for each player are in a comma delimited file which won't change during the
 * contest. A separate file indicates the column names of that file and all nominees for each
 * category the way they should be displayed on the web site. The column name can include a tie
 * breaker number inside parentheses like this: Director(1) to indicate that Director is the first
 * tie breaker. The contestants' names must be in the columns named "First" and "Last" and their
 * time estimate must be in the column named "Time" in the format "H:MM" or "H:MM:SS.D".
 *
 * @author Scott McDonald
 * @version 4.5
 */
public class Oscars implements Runnable {
    private final List<Player> players;

    private final List<Category> categories;

    private final Results results;

    private final String scoreFormat;

    private Standings standings;

    private long validTimes = 0;

    private LocalDateTime updated;

    /**
     * Prompt for Oscars results, store them and create output files
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] inArgs) throws Exception {
        new Oscars().process();
    }

    private Oscars() throws Exception {
        System.out.print("Step 1 of 6: Deleting any old data... ");
        IOUtils.deleteOldData();
        System.out.println("DONE");

        System.out.print("Step 2 of 6: Downloading ballots... ");
        Stream<Ballot> ballots = Ballot.stream();
        System.out.println("DONE");

        System.out.print("Step 3 of 6: Mapping the category data... ");
        CategoryMapper categoryMapper = new CategoryMapper(ballots);
        players = categoryMapper.getPlayers();
        categories = categoryMapper.getCategories();
        scoreFormat = "%." + categories.stream()
                .filter(category -> !category.tieBreakerValue.isEmpty()).count() + "f";
        System.out.println("DONE");

        System.out.print("Step 4 of 6: Reading any existing results... ");
        results = new Results(categories);
        System.out.println("DONE");

        System.out.print("Step 5 of 6: Writing rank images... ");
        RankChart.writeAll(players.size());
        System.out.println("DONE");

        System.out.print("Step 6 of 6: Writing web pages... ");
        writeCategoryPages();
        writePlayerPages();
        System.out.println("DONE");
    }

    private void process() throws IOException, InterruptedException {
        do
            System.out.println();
        while (prompt());

        System.out.print("\nWriting final results... ");
        writeResults(); // In case it was interrupted in the thread
        IOUtils.cleanUpCharts(Category.DIRECTORY,
                categories.stream().map(category -> category.chartName(results)));
        IOUtils.cleanUpCharts(RankChart.DIRECTORY,
                players.stream().mapToLong(standings::rank).mapToObj(RankChart::name));
        System.out.println("DONE");
    }

    private boolean prompt() throws IOException, InterruptedException {
        updated = LocalDateTime.now();
        Thread thread = new Thread(this);
        try {
            thread.start();
            return results.prompt(categories);
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
                Thread.sleep(waitTime(TimeUnit.SECONDS.toMillis(60)));
            } while (!standings.showEnded());
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private long waitTime(long inMaxWait) {
        long nextPlayerTime = TimeUnit.SECONDS
                .toMillis(players.stream().mapToLong(player -> player.time)
                        .filter(playerTime -> playerTime > standings.elapsedTime).min()
                        .orElseGet(() -> TimeUnit.MILLISECONDS.toSeconds(Long.MAX_VALUE)));
        long elapsedTimeMillis = results.elapsedTimeMillis();
        return Math.min(Math.max(nextPlayerTime - elapsedTimeMillis, 0),
                inMaxWait - elapsedTimeMillis % inMaxWait);
    }

    private void writeResults() throws IOException {
        standings = new Standings(players, results);
        long currentTimes = players.stream().filter(player -> player.time <= standings.elapsedTime)
                .count();
        if (validTimes != currentTimes)
            updated = LocalDateTime.now();
        validTimes = currentTimes;
        Results.write(updated, standings.resultsCategoryDOM(categories),
                standings.resultsPlayerDOM(players, scoreFormat), standings.resultsShowTimeDOM());
    }

    private void writeCategoryPages() throws IOException {
        IOUtils.mkdir(Category.DIRECTORY);
        IOUtils.writeDocument(
                categories.stream().map(category -> category.toDOM(players))
                        .reduce(new Element("categories"), Element::addContent),
                Category.DIRECTORY + "all.xml", "../xsl/categoryGraphs.xsl");
        for (Category category : categories) {
            category.writeChart(results);
            IOUtils.writeDocument(new Element("category").addContent(category.name),
                    Category.DIRECTORY + category.name + ".xml", "../xsl/category.xsl");
        }
    }

    private void writePlayerPages() throws IOException {
        IOUtils.mkdir(Player.DIRECTORY);
        for (Player player : players)
            IOUtils.writeDocument(player.toDOM(),
                    Player.DIRECTORY + (player.firstName + " " + player.lastName).trim() + ".xml",
                    "../xsl/player.xsl");
    }
}