package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.opencsv.CSVReader;

/** Entries on a player's ballot - Immutable */
public final class Ballot {
    private static final String URL_FILE = "ResponsesURL.txt";

    /** Provides the latest Ballot for each player */
    public static final Collector<Ballot, ?, Collection<Ballot>> LATEST = Collectors
            .collectingAndThen(
                    Collectors.toMap(Ballot::getName, ballot -> ballot,
                            BinaryOperator.maxBy(Comparator.comparing(Ballot::getTimestamp))),
                    Map::values);

    public final Map<String, String> values;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            readBallots().filter(ballot -> !ballot.values.get(Category.EMAIL).isEmpty())
                    .forEach(ballot -> System.out
                            .println(ballot.getName() + " = " + ballot.values.get(Category.EMAIL)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    private Ballot(String[] inValues) {
        if (inValues.length != Category.ALL.size())
            throw new RuntimeException("Ballot length: " + inValues.length
                    + " does not match category definitions: " + Category.ALL.size());
        values = Collections.unmodifiableMap(
                IntStream.range(0, inValues.length).boxed().collect(Collectors.toMap(
                        column -> Category.ALL.get(column).name, column -> inValues[column])));
    }

    public static Stream<Ballot> readBallots() throws IOException {
        try (Stream<String> lines = Files
                .lines(Paths.get(Ballot.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URL(lines.iterator().next());
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                CategoryMapper.setHeaders(reader.readNext());
                return reader.readAll().stream().map(Ballot::new);
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from: " + URL_FILE, e);
        }
    }

    private String getName() {
        return values.get(Category.LAST_NAME) + ", " + values.get(Category.FIRST_NAME);
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(values.get(Category.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Ballot> ballots = readBallots().collect(LATEST);
                LocalDateTime maxTimestamp = ballots.stream().map(Ballot::getTimestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    System.err.println(LocalDateTime.now() + " - Downloaded: " + ballots.size()
                            + " ballots - After: "
                            + Duration.between(maxTimestamp, LocalDateTime.now()).toString()
                                    .substring(2));
                    Results.write(ZonedDateTime.now(), ballots.stream().map(Ballot::toDOM)
                            .reduce(new Element("ballots"), Element::addContent));
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }

    private Element toDOM() {
        LocalDateTime timestamp = getTimestamp();
        return new Element("ballot").setAttribute("name", getName()).addContent(
                new Element("timestamp").setAttribute("raw", timestamp.toString()).addContent(
                        timestamp.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"))));
    }
}