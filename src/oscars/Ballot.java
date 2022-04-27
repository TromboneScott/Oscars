package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

/** The values on a player's ballot - Immutable */
public final class Ballot {
    private final List<String> values;

    public Ballot(String[] inValues) {
        values = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(inValues)));
    }

    public String get(int inColumn) {
        return values.get(inColumn);
    }

    public String name() {
        return get(2) + ", " + get(1);
    }

    public String email() {
        return get(values.size() - 1);
    }

    private LocalDateTime timestamp() {
        return LocalDateTime.parse(get(0), DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    public static Stream<Ballot> stream(URL inURL) throws IOException, CsvException {
        try (InputStreamReader inputReader = new InputStreamReader(inURL.openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            return csvReader.readAll().stream().map(Ballot::new);
        }
    }

    public static Collection<Ballot> latest(Stream<Ballot> inStream) {
        return inStream.collect(Collectors.toMap(ballot -> ballot.name().toUpperCase(),
                ballot -> ballot, BinaryOperator.maxBy(Comparator.comparing(Ballot::timestamp))))
                .values();
    }

    public static void main(String[] inArgs) throws Exception {
        Oscars.validateArgs(inArgs);
        for (LocalDateTime lastTimestamp = LocalDateTime.MIN;; Thread.sleep(10000))
            try {
                URL url = new URL(inArgs[0]);
                url.openConnection().setDefaultUseCaches(false);
                Collection<Ballot> ballots = latest(stream(url));
                if (ballots.stream().map(Ballot::timestamp).anyMatch(lastTimestamp::isBefore)) {
                    Results.write(LocalDateTime.now(), ballots.stream().map(Ballot::toDOM)
                            .reduce(new Element("ballots"), Element::addContent));
                    System.err.println(
                            LocalDateTime.now() + " - Wrote " + ballots.size() + " ballots");
                    lastTimestamp = ballots.stream().map(Ballot::timestamp)
                            .max(LocalDateTime::compareTo).get();
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }

    private Element toDOM() {
        return new Element("ballot").addContent(new Element("name").addContent(name())).addContent(
                new Element("timestamp").setAttribute("raw", timestamp().toString()).addContent(
                        timestamp().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"))));
    }
}