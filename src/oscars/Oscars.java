package oscars;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.jdom2.Element;

/**
 * Create and update the Oscars website with the winners that are entered by the user. The players'
 * guesses are downloaded from the survey in comma-delimited format. The columns are defined in an
 * XML file that defines each category and its nominees. The order of the categories on the website
 * will be the same as the order on the ballot.
 * 
 * @author Scott McDonald
 * @version 6.0
 */
public class Oscars implements Runnable {
    private final List<Player> players;

    private final Results results;

    private Standings standings;

    private long validTimes = 0;

    private ZonedDateTime updated;

    /** Prompt for Oscars results, store them and create output files */
    public static void main(String[] inArgs) throws Exception {
        new Oscars().process();
    }

    private Oscars() throws IOException {
        System.out.print("Step 1 of 3: Downloading ballots... ");
        CategoryMapper categoryMapper = new CategoryMapper();
        System.out.println("DONE");

        System.out.print("Step 2 of 3: Reading any existing results... ");
        players = categoryMapper.getPlayers();
        results = new Results(categoryMapper.getNomineeDescriptions());
        System.out.println("DONE");

        System.out.print("Step 3 of 3: Writing web pages... ");
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
        Category.cleanUpCharts(results);
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
            } while (results.elapsedTimeMillis() > 0 && !results.showEnded());
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
        Results.write(updated, results.awardsDOM(), standings.toDOM());
    }

    private void writeCategoryPages() throws IOException {
        for (Category category : Category.ALL) {
            category.writeChart(results, players);
            writeCategoryPage(category.name);
        }
        writeCategoryPage("all");
    }

    private static void writeCategoryPage(String inCategory) throws IOException {
        Directory.CATEGORY.write(new Element("category").setAttribute("name", inCategory),
                inCategory + ".xml", "category.xsl");
    }

    private void writePlayerPages() throws IOException {
        Directory.DATA.write(IntStream.range(0, players.size()).mapToObj(playerNum -> Category.ALL
                .stream()
                .map(category -> new Element("category").setAttribute("name", category.name)
                        .setAttribute("nominee", players.get(playerNum).picks.get(category.name)))
                .reduce(players.get(playerNum).toDOM(), Element::addContent)
                .setAttribute("id", String.valueOf(playerNum + 1))
                .setAttribute("time", String.valueOf(players.get(playerNum).time)))
                .reduce(new Element("ballots"), Element::addContent), "ballots.xml", null);

        for (Player player : players)
            Directory.PLAYER.write(player.toDOM(), player.picks.get(Category.FIRST_NAME) + "_"
                    + player.picks.get(Category.LAST_NAME) + ".xml", "player.xsl");
    }
}