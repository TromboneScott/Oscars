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
    public static final ImmutableList<Player> PLAYERS;

    public static final Results RESULTS;

    private long elapsedTime;

    private long validTimes = 0;

    private ZonedDateTime updated;

    static {
        System.out.print("Downloading ballots...");
        try {
            Mapper mapper = new Mapper();
            PLAYERS = mapper.players();
            RESULTS = new Results(mapper::nomineeMapping);
            writeStaticFiles();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(" DONE");
    }

    /** Prompt for Oscars results, store them and create output files */
    public static void main(String[] inArgs) throws Exception {
        new Oscars().process();
    }

    private void process() throws Exception {
        do
            System.out.println();
        while (prompt());

        System.out.print("\nWriting final results...");
        writeResults(); // In case it was interrupted in the thread
        Column.deleteUnusedCharts();
        System.out.println(" DONE");
    }

    private boolean prompt() throws Exception {
        updated = ZonedDateTime.now();
        Thread thread = new Thread(this);
        try {
            thread.start();
            return RESULTS.prompt();
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
            } while (RESULTS.elapsedTimeMillis() > 0 && !RESULTS.showEnded());
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long waitTime(long inMaxWait) {
        long nextPlayerTime = PLAYERS.stream().mapToLong(player -> player.time())
                .filter(playerTime -> playerTime > elapsedTime).map(TimeUnit.SECONDS::toMillis)
                .min().orElse(Long.MAX_VALUE);
        long elapsedTimeMillis = RESULTS.elapsedTimeMillis();
        return Math.min(Math.max(nextPlayerTime - elapsedTimeMillis, 0),
                inMaxWait - elapsedTimeMillis % inMaxWait);
    }

    private void writeResults() throws IOException {
        elapsedTime = RESULTS.elapsedTimeSeconds();
        long currentTimes = PLAYERS.stream().filter(player -> player.time() <= elapsedTime).count();
        if (validTimes != currentTimes)
            updated = ZonedDateTime.now();
        validTimes = currentTimes;
        Results.write(updated, RESULTS.toDOM(), new Standings().toDOM());
    }

    private static void writeStaticFiles() throws IOException {
        new XMLFile(Directory.DATA, "ballots.xml").write(IntStream.range(0, PLAYERS.size())
                .mapToObj(playerNum -> Column.CATEGORIES.stream()
                        .map(category -> new Element("category")
                                .setAttribute("name", category.name())
                                .setAttribute("nominee", PLAYERS.get(playerNum).answer(category)))
                        .reduce(PLAYERS.get(playerNum).toDOM(), Element::addContent)
                        .setAttribute("id", String.valueOf(playerNum + 1))
                        .setAttribute("time", String.valueOf(PLAYERS.get(playerNum).time())))
                .reduce(new Element("ballots"), Element::addContent));

        for (Player player : PLAYERS)
            new XMLFile(Directory.PLAYER, player.answer(Column.FIRST_NAME) + "_"
                    + player.answer(Column.LAST_NAME) + ".xml").write(player.toDOM());

        for (Column category : Column.CATEGORIES) {
            category.writeChart();
            new XMLFile(Directory.CATEGORY, category.name() + ".xml")
                    .write(new Element("category").setAttribute("name", category.name()));
        }
    }
}