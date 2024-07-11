package oscars;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;

/** Entries on a player's ballot - Immutable */
public final class Ballot {
    /** The entries for each category on this ballot */
    public final Map<String, String> entries;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        System.out.println("Downloading ballots...");
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new BallotReader().ballots.stream()
                    .filter(ballot -> !ballot.entries.get(Category.EMAIL).isEmpty())
                    .forEach(ballot -> System.out.println(
                            ballot.getName() + " = " + ballot.entries.get(Category.EMAIL)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    /** Create a Ballot with the given entries */
    public Ballot(String[] inEntries) {
        if (inEntries.length != Category.ALL.size())
            throw new RuntimeException("Number of ballot entries: " + inEntries.length
                    + " does not match category definitions: " + Category.ALL.size());
        entries = Collections.unmodifiableMap(IntStream.range(0, inEntries.length).boxed()
                .collect(Collectors.toMap(column -> Category.ALL.get(column).name,
                        column -> inEntries[column].trim())));
    }

    /** Get the name (last, first) on the Ballot */
    public String getName() {
        return Stream.of(Category.LAST_NAME, Category.FIRST_NAME).map(entries::get)
                .filter(name -> !name.isEmpty()).collect(Collectors.joining(", "));
    }

    /** Get the timestamp of this Ballot */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(entries.get(Category.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
            try {
                Collection<Ballot> ballots = new BallotReader().latest();
                LocalDateTime maxTimestamp = ballots.stream().map(Ballot::getTimestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    System.err.println(LocalDateTime.now() + " - Downloaded: " + ballots.size()
                            + " ballots - After: "
                            + Duration.between(maxTimestamp, LocalDateTime.now()).toString()
                                    .substring(2));
                    Results.write(ZonedDateTime.now(), ballots.stream()
                            .map(ballot -> new Element("ballot")
                                    .setAttribute("name", ballot.getName())
                                    .setAttribute("timestamp", ballot.getTimestamp().toString()))
                            .reduce(new Element("ballots"), Element::addContent));
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }
}