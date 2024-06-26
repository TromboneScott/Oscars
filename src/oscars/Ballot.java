package oscars;

import java.io.IOException;
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

/** Entries on a player's ballot - Immutable */
public final class Ballot {
    /** Provides the latest Ballot for each player */
    public static final Collector<Ballot, ?, Collection<Ballot>> LATEST = Collectors
            .collectingAndThen(
                    Collectors.toMap(Ballot::getName, ballot -> ballot,
                            BinaryOperator.maxBy(Comparator.comparing(Ballot::getTimestamp))),
                    Map::values);

    public final Map<String, String> values;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        System.out.println("Downloading ballots...");
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new BallotReader().stream()
                    .filter(ballot -> !ballot.values.get(Category.EMAIL).isEmpty())
                    .forEach(ballot -> System.out
                            .println(ballot.getName() + " = " + ballot.values.get(Category.EMAIL)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    public Ballot(String[] inValues) {
        if (inValues.length != Category.ALL.size())
            throw new RuntimeException("Ballot length: " + inValues.length
                    + " does not match category definitions: " + Category.ALL.size());
        values = Collections.unmodifiableMap(IntStream.range(0, inValues.length).boxed()
                .collect(Collectors.toMap(column -> Category.ALL.get(column).name,
                        column -> inValues[column].trim())));
    }

    private String getName() {
        return Stream.of(Category.LAST_NAME, Category.FIRST_NAME).map(values::get)
                .filter(name -> !name.isEmpty()).collect(Collectors.joining(", "));
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(values.get(Category.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Ballot> ballots = new BallotReader().stream().collect(LATEST);
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
        return new Element("ballot").setAttribute("name", getName()).setAttribute("timestamp",
                getTimestamp().toString());
    }
}