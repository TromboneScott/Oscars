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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.opencsv.CSVReader;

/** The data from the ballot survey - Immutable */
public final class Ballots {
    private static final String URL_FILE = "ResponsesURL.txt";

    private final List<String> headers;

    private final List<Player> all;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        System.out.println("Downloading ballots...");
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new Ballots().all.stream().filter(player -> !player.answer(Column.EMAIL).isEmpty())
                    .forEach(player -> System.out
                            .println(player.name() + " = " + player.answer(Column.EMAIL)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    /** Download the data from the ballot survey */
    public Ballots() throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(Objects
                .requireNonNull(getClass().getClassLoader().getResource(URL_FILE), "File not found")
                .toURI()))) {
            URL url = new URI(lines.findFirst().orElseThrow(() -> new IOException("File is empty")))
                    .toURL();
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                headers = Collections.unmodifiableList(Arrays.asList(reader.readNext()));
                if (headers.size() != Column.ALL.size())
                    throw new IOException("Number of columns on ballots: " + headers.size()
                            + " does not match number of defined columns: " + Column.ALL.size());
                all = Collections.unmodifiableList(
                        reader.readAll().stream().map(Player::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from file: " + URL_FILE, e);
        }
    }

    /** The column headers of the survey */
    public List<String> headers() {
        return headers;
    }

    /** The players from the survey using the latest ballot for each Player */
    public Collection<Player> players() {
        return Collections
                .unmodifiableCollection(all.stream()
                        .collect(Collectors.toMap(Player::name, player -> player,
                                BinaryOperator.maxBy(Comparator.comparing(Player::timestamp))))
                        .values());
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Player> players = new Ballots().players();
                LocalDateTime maxTimestamp = players.stream().map(Player::timestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    System.err.println(LocalDateTime.now() + " - Downloaded: " + players.size()
                            + " ballots - After: "
                            + Duration.between(maxTimestamp, LocalDateTime.now()).toString()
                                    .substring(2));
                    Results.write(ZonedDateTime.now(), players.stream()
                            .map(player -> new Element("ballot").setAttribute("name", player.name())
                                    .setAttribute("timestamp", player.timestamp().toString()))
                            .reduce(new Element("ballots"), Element::addContent));
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }
}