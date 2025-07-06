package oscars;

import java.io.IOException;
import java.util.stream.IntStream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;

import oscars.ballot.MappedBallots;
import oscars.ballot.Player;
import oscars.column.Category;
import oscars.column.DataColumn;
import oscars.file.Directory;
import oscars.file.XMLFile;

/**
 * Create and update the Oscars website with the winners that are entered by the user. The players'
 * guesses are downloaded from the survey in comma-delimited format. The columns are defined in an
 * XML file that defines each category and its nominees. The order of the categories on the website
 * will be the same as the order on the ballot.
 * 
 * @author Scott McDonald
 * @version 7.0
 */
public class Oscars {
    public static final ImmutableList<Player> PLAYERS;

    public static final Results RESULTS;

    static {
        System.out.println("Downloading ballots...");
        try {
            MappedBallots ballots = new MappedBallots();
            PLAYERS = ballots.players();
            RESULTS = new Results(ballots.nomineeMap());
            writeStaticFiles();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Prompt for Oscars results, store them and create output files */
    public static void main(String[] inArgs) throws Exception {
        do
            System.out.println();
        while (prompt());

        System.out.print("\nWriting final results...");
        ResultsUpdater.writeResults(); // In case thread was interrupted
        System.out.println(" DONE");
    }

    private static boolean prompt() throws Exception {
        Thread thread = new ResultsUpdater();
        try {
            thread.start();
            return RESULTS.prompt();
        } finally {
            thread.interrupt(); // Stop file I/O thread
            thread.join(); // Wait for it to finish
        }
    }

    private static void writeStaticFiles() throws IOException {
        new XMLFile(Directory.DATA, "ballots.xml").write(IntStream.range(0, PLAYERS.size())
                .mapToObj(playerNum -> Category.ALL.stream()
                        .map(category -> category.toDOM().setAttribute("nominee",
                                PLAYERS.get(playerNum).answer(category)))
                        .reduce(PLAYERS.get(playerNum).toDOM(), Element::addContent)
                        .setAttribute("id", String.valueOf(playerNum + 1))
                        .setAttribute("time", String.valueOf(PLAYERS.get(playerNum).time())))
                .reduce(new Element("ballots"), Element::addContent));

        for (Player player : PLAYERS)
            new XMLFile(Directory.PLAYER, player.answer(DataColumn.FIRST_NAME) + "_"
                    + player.answer(DataColumn.LAST_NAME) + ".xml").write(player.toDOM());

        for (Category category : Category.ALL) {
            category.writeChart();
            new XMLFile(Directory.CATEGORY, category.name() + ".xml").write(category.toDOM());
        }
    }
}