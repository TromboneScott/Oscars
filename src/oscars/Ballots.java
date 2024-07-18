package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.opencsv.CSVReader;

/** The data from the ballot survey - Immutable */
public final class Ballots {
    private static final String URL_FILE = "ResponsesURL.txt";

    /** The category names from the header row of the survey */
    public final List<String> categories;

    /** All of the ballots that were received */
    private final List<Player> all;

    /** The data for the players using the latest ballot for each Player */
    public final Collection<Player> players;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        System.out.println("Downloading ballots...");
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new Ballots().all.stream().filter(player -> !player.picks.get(Category.EMAIL).isEmpty())
                    .forEach(player -> System.out
                            .println(getName(player) + " = " + player.picks.get(Category.EMAIL)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    /** Download the data from the ballot survey */
    public Ballots() throws IOException {
        try (Stream<String> lines = Files.lines(
                Paths.get(Optional.ofNullable(getClass().getClassLoader().getResource(URL_FILE))
                        .orElseThrow(() -> new IOException("File not found")).toURI()))) {
            URL url = new URI(lines.findFirst().orElseThrow(() -> new IOException("File is empty")))
                    .toURL();
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                categories = Collections.unmodifiableList(Arrays.asList(reader.readNext()));
                if (categories.size() != Category.ALL.size())
                    throw new IOException("Number of categories in ballots: " + categories.size()
                            + " does not match defined categories: " + Category.ALL.size());
                all = Collections.unmodifiableList(reader.readAll().stream().map(Ballots::toPlayer)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from file: " + URL_FILE, e);
        }
        players = Collections.unmodifiableCollection(all.stream()
                .collect(Collectors.toMap(Ballots::getName, player -> player,
                        BinaryOperator.maxBy(Comparator.comparing(Player::getTimestamp))))
                .values());
    }

    private static Player toPlayer(String[] inEntries) {
        if (inEntries.length != Category.ALL.size())
            throw new RuntimeException("Number of ballot entries: " + inEntries.length
                    + " does not match category definitions: " + Category.ALL.size());
        return new Player(IntStream.range(0, inEntries.length).boxed().collect(Collectors.toMap(
                column -> Category.ALL.get(column).name, column -> inEntries[column].trim())));
    }

    /** Get the name (last, first) for the Player */
    private static String getName(Player inPlayer) {
        return Stream.of(Category.LAST_NAME, Category.FIRST_NAME).map(inPlayer.picks::get)
                .filter(name -> !name.isEmpty()).collect(Collectors.joining(", "));
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Player> players = new Ballots().players;
                LocalDateTime maxTimestamp = players.stream().map(Player::getTimestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    System.err.println(LocalDateTime.now() + " - Downloaded: " + players.size()
                            + " ballots - After: "
                            + Duration.between(maxTimestamp, LocalDateTime.now()).toString()
                                    .substring(2));
                    Results.write(ZonedDateTime.now(), players.stream()
                            .map(player -> new Element("ballot")
                                    .setAttribute("name", getName(player))
                                    .setAttribute("timestamp", player.getTimestamp().toString()))
                            .reduce(new Element("ballots"), Element::addContent));
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }
}