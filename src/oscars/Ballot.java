package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;

/** The values on a player's ballot - Immutable */
public final class Ballot {
    private static final String URL_FILE = "ResponsesURL.txt";

    public static final Collector<Ballot, ?, Collection<Ballot>> LATEST = Collectors
            .collectingAndThen(
                    Collectors.toMap(ballot -> ballot.getName().toUpperCase(), ballot -> ballot,
                            BinaryOperator.maxBy(Comparator.comparing(Ballot::getTimestamp))),
                    Map::values);

    private final String[] values;

    public static void main(String[] inArgs) throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Ballot> ballots = stream().collect(LATEST);
                LocalDateTime maxTimestamp = ballots.stream().map(Ballot::getTimestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    Results.write(LocalDateTime.now(), ballots.stream().map(Ballot::toDOM)
                            .reduce(new Element("ballots"), Element::addContent));
                    System.err.println(
                            LocalDateTime.now() + " - Wrote " + ballots.size() + " ballots");
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }

    public static Stream<Ballot> stream() throws Exception {
        try (Stream<String> lines = Files
                .lines(Paths.get(Ballot.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URL(lines.iterator().next());
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReaderHeaderAware(
                    new InputStreamReader(url.openStream()))) {
                return reader.readAll().stream().map(Ballot::new);
            }
        }
    }

    private Ballot(String[] inValues) {
        values = inValues;
    }

    public String get(int inColumn) {
        return values[inColumn];
    }

    public String getName() {
        return values[2] + ", " + values[1];
    }

    public String getEmail() {
        return values[values.length - 1];
    }

    private LocalDateTime getTimestamp() {
        return LocalDateTime.parse(values[0], DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private Element toDOM() {
        return new Element("ballot").addContent(new Element("name").addContent(getName()))
                .addContent(new Element("timestamp").setAttribute("raw", getTimestamp().toString())
                        .addContent(getTimestamp()
                                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"))));
    }
}