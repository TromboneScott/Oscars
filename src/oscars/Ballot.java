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
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jdom2.Element;

/** Entries on a player's ballot - Immutable */
public final class Ballot {
    /** Provides the latest Ballot for each player */
    public static final Collector<Ballot, ?, Collection<Ballot>> LATEST = Collectors
            .collectingAndThen(
                    Collectors.toMap(Ballot::getName, ballot -> ballot,
                            BinaryOperator.maxBy(Comparator.comparing(Ballot::getTimestamp))),
                    Map::values);

    private final Map<String, String> values;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new BallotReader().readBallots()
                    .filter(ballot -> !ballot.get(Category.EMAIL.name).isEmpty())
                    .forEach(ballot -> System.out
                            .println(ballot.getName() + " = " + ballot.get(Category.EMAIL.name)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    /** Build a Ballot */
    public static <T> Collector<T, ?, Ballot> toBallot(Function<T, String> inCategoryMapper,
            Function<T, String> inValueMapper) {
        return Collectors.collectingAndThen(Collectors.toMap(inCategoryMapper, inValueMapper),
                Ballot::new);
    }

    private Ballot(Map<String, String> inValues) {
        values = Collections.unmodifiableMap(inValues);
    }

    /** Get value from Ballot for given category */
    public String get(String inCategory) {
        return values.get(inCategory);
    }

    private String getName() {
        return get(Category.LAST_NAME.name) + ", " + get(Category.FIRST_NAME.name);
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(get(Category.TIMESTAMP.name),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private static void writeNewBallots() throws Exception {
        BallotReader ballotReader = new BallotReader();
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Ballot> ballots = ballotReader.readBallots().collect(LATEST);
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