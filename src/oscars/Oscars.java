package oscars;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;

/**
 * Create and update the Oscars website with the winners that are entered by the user. The players'
 * guesses are downloaded from the survey in comma-delimited format. The columns are defined in an
 * XML file that defines each category and its nominees. The order of the categories on the website
 * will be the same as the order on the ballot.
 * 
 * @author Scott McDonald
 * @version 6.1
 */
public class Oscars implements Runnable {
    private final ImmutableList<Player> players;

    private final Results results;

    private Standings standings;

    private long validTimes = 0;

    private ZonedDateTime updated;

    /** Prompt for Oscars results, store them and create output files */
    public static void main(String[] inArgs) throws Exception {
        new Oscars().process();
    }

    private Oscars() throws Exception {
        System.out.print("Step 1 of 2: Downloading ballots... ");
        Mapper mapper = new Mapper();
        System.out.println("DONE");

        System.out.print("Step 2 of 2: Creating web pages... ");
        players = mapper.players();
        results = new Results(players, mapper::nomineeMapping);
        writeStaticXMLFiles();
        System.out.println("DONE");
    }

    private void process() throws Exception {
        do
            System.out.println();
        while (prompt());

        System.out.print("\nWriting final results... ");
        writeResults(); // In case it was interrupted in the thread
        results.cleanUpCharts();
        System.out.println("DONE");
    }

    private boolean prompt() throws Exception {
        updated = ZonedDateTime.now();
        Thread thread = new Thread(this);
        try {
            thread.start();
            return results.prompt();
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
            e.printStackTrace();
        }
    }

    private long waitTime(long inMaxWait) {
        long nextPlayerTime = players.stream().mapToLong(player -> player.time())
                .filter(playerTime -> playerTime > standings.elapsedTime())
                .map(TimeUnit.SECONDS::toMillis).min().orElse(Long.MAX_VALUE);
        long elapsedTimeMillis = results.elapsedTimeMillis();
        return Math.min(Math.max(nextPlayerTime - elapsedTimeMillis, 0),
                inMaxWait - elapsedTimeMillis % inMaxWait);
    }

    private void writeResults() throws IOException {
        standings = new Standings(players, results);
        long currentTimes = players.stream()
                .filter(player -> player.time() <= standings.elapsedTime()).count();
        if (validTimes != currentTimes)
            updated = ZonedDateTime.now();
        validTimes = currentTimes;
        results.write(updated, standings);
    }

    private void writeStaticXMLFiles() throws IOException {
        new XMLFile(Directory.DATA, "ballots.xml").write(IntStream.range(0, players.size())
                .mapToObj(playerNum -> Column.CATEGORIES.stream()
                        .map(category -> new Element("category")
                                .setAttribute("name", category.name())
                                .setAttribute("nominee", players.get(playerNum).answer(category)))
                        .reduce(players.get(playerNum).toDOM(), Element::addContent)
                        .setAttribute("id", String.valueOf(playerNum + 1))
                        .setAttribute("time", String.valueOf(players.get(playerNum).time())))
                .reduce(new Element("ballots"), Element::addContent));

        for (Column column : Column.CATEGORIES)
            new XMLFile(Directory.CATEGORY, column + ".xml")
                    .write(new Element("category").setAttribute("name", column.name()));

        for (Player player : players)
            new XMLFile(Directory.PLAYER, player.answer(Column.FIRST_NAME) + "_"
                    + player.answer(Column.LAST_NAME) + ".xml").write(player.toDOM());
    }
}