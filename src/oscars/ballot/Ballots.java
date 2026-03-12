package oscars.ballot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.opencsv.CSVReader;

import oscars.Results;

/** The data from the ballot survey - Immutable */
class Ballots {
    private static final URL RESPONSES_URL = responsesUrl("ResponsesURL.txt");

    private final ImmutableList<String> headers;

    private final ImmutableCollection<Ballot> all;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new Ballots().all.stream().filter(ballot -> !ballot.answer(Column.EMAIL).isEmpty())
                    .forEach(ballot -> System.out.println(ballot.answer(Column.LAST_NAME) + ", "
                            + ballot.answer(Column.FIRST_NAME) + " = "
                            + ballot.answer(Column.EMAIL)));
        else
            throw new IllegalArgumentException("Unknown action: " + inArgs[0]);
    }

    /** Get the URL for the ballot responses */
    private static URL responsesUrl(String urlFile) {
        System.out.println("Downloading ballots with URL from file: " + urlFile);
        try (Stream<String> lines = Files.lines(Paths
                .get(Objects.requireNonNull(Ballots.class.getClassLoader().getResource(urlFile),
                        "File not found: " + urlFile).toURI()))) {
            return new URL(lines.findFirst()
                    .orElseThrow(() -> new Exception("File is empty: " + urlFile)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Download the ballots */
    public Ballots() throws Exception {
        URLConnection connection = RESPONSES_URL.openConnection();
        connection.setUseCaches(false);
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            headers = ImmutableList.copyOf(reader.readNext());
            if (headers.size() != Column.ALL.size())
                throw new Exception("Number of columns on ballots: " + headers.size()
                        + " does not match number of defined columns: " + Column.ALL.size());
            all = reader.readAll().stream().map(Ballot::new)
                    .sorted(Ordering.from(String.CASE_INSENSITIVE_ORDER)
                            .onResultOf((Ballot ballot) -> ballot.answer(Column.LAST_NAME))
                            .compound(Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(
                                    (Ballot ballot) -> ballot.answer(Column.FIRST_NAME)))
                            .compound(Ordering.natural().onResultOf(Ballot::timestamp)))
                    .collect(ImmutableList.toImmutableList());
        }
    }

    /** The column headers of the survey */
    protected final ImmutableList<String> headers() {
        return headers;
    }

    /** The latest ballot for each Player */
    protected final ImmutableCollection<Ballot> latest() {
        return all.stream()
                .collect(ImmutableMap.toImmutableMap(
                        ballot -> ImmutableList.of(ballot.answer(Column.FIRST_NAME),
                                ballot.answer(Column.LAST_NAME)),
                        ballot -> ballot,
                        BinaryOperator.maxBy(Comparator.comparing(Ballot::timestamp))))
                .values();
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(Results.UPDATE_TIME))
            try {
                Results.writeCountdown();
                ImmutableCollection<Ballot> ballots = new Ballots().latest();
                LocalDateTime maxTimestamp = ballots.stream().map(Ballot::timestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    Results.write(ballots.stream()
                            .map(ballot -> new Element("player")
                                    .setAttribute("firstName", ballot.answer(Column.FIRST_NAME))
                                    .setAttribute("lastName", ballot.answer(Column.LAST_NAME))
                                    .setAttribute("timestamp", ballot.timestamp().toString()))
                            .reduce(new Element("ballots"), Element::addContent));
                    System.err.println(LocalDateTime.now() + " - Downloaded: " + ballots.size()
                            + " ballots - After: "
                            + Duration.between(maxTimestamp, LocalDateTime.now()).toString()
                                    .substring(2));
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }
}