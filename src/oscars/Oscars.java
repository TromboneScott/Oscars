package oscars;

import java.io.IOException;

import org.jdom2.Element;

import com.google.common.collect.ImmutableList;

import oscars.ballot.MappedBallots;
import oscars.ballot.Player;
import oscars.column.Category;
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
        do {
            Results.write(RESULTS.toDOM(), new Standings().toDOM());
            System.out.println();
        } while (RESULTS.prompt());
    }

    private static void writeStaticFiles() throws IOException {
        new XMLFile(Directory.DATA, "ballots.xml").write(PLAYERS.stream().map(player -> Category.ALL
                .stream()
                .map(category -> category.toDOM().setAttribute("nominee", player.answer(category)))
                .reduce(player.toDOM(), Element::addContent)
                .setAttribute("time", String.valueOf(player.time())))
                .reduce(new Element("ballots"), Element::addContent));

        for (Player player : PLAYERS)
            new XMLFile(Directory.PLAYERS, player.id() + ".xml").write(player.toDOM());

        for (Category category : Category.ALL) {
            category.writeChart();
            new XMLFile(Directory.CATEGORIES, category.name() + ".xml").write(category.toDOM());
        }
    }
}