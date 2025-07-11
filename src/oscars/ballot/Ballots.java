package oscars.ballot;

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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.jdom2.Element;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opencsv.CSVReader;

import oscars.Results;
import oscars.column.Column;
import oscars.column.DataColumn;

/** The data from the ballot survey - Immutable */
class Ballots {
    private static final String URL_FILE = "ResponsesURL.txt";

    private final ImmutableList<String> headers;

    private final ImmutableCollection<Ballot> all;

    /** Output either the name and timestamp or the email for each Ballot */
    public static void main(String[] inArgs) throws Exception {
        System.out.println("Downloading ballots with URL from file: " + URL_FILE);
        if (inArgs.length == 0)
            writeNewBallots();
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            new Ballots().all.stream().filter(player -> !player.answer(DataColumn.EMAIL).isEmpty())
                    .forEach(player -> System.out.println(player.answer(DataColumn.LAST_NAME) + ", "
                            + player.answer(DataColumn.FIRST_NAME) + " = "
                            + player.answer(DataColumn.EMAIL)));
        else
            throw new IllegalArgumentException("Unknown action: " + inArgs[0]);
    }

    /** Download the ballots from the survey */
    public Ballots() throws Exception {
        try (Stream<String> lines = Files.lines(
                Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(URL_FILE),
                        "File not found: " + URL_FILE).toURI()))) {
            URL url = new URI(lines.findFirst()
                    .orElseThrow(() -> new Exception("File is empty: " + URL_FILE))).toURL();
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                headers = ImmutableList.copyOf(reader.readNext());
                if (headers.size() != Column.ALL.size())
                    throw new Exception("Number of columns on ballots: " + headers.size()
                            + " does not match number of defined columns: " + Column.ALL.size());
                all = reader.readAll().stream().map(Ballot::new)
                        .collect(ImmutableList.toImmutableList());
            }
        }
    }

    /** The column headers of the survey */
    protected final ImmutableList<String> headers() {
        return headers;
    }

    /** The answers from the survey using the latest ballot for each Player */
    protected final ImmutableCollection<Ballot> answers() {
        return all.stream()
                .collect(ImmutableMap.toImmutableMap(
                        answers -> answers.answer(DataColumn.LAST_NAME) + "|"
                                + answers.answer(DataColumn.FIRST_NAME),
                        answers -> answers,
                        BinaryOperator.maxBy(Comparator.comparing(Ballots::timestamp))))
                .values();
    }

    private static void writeNewBallots() throws Exception {
        for (LocalDateTime lastTimestamp = null;; Thread.sleep(Results.UPDATE_TIME))
            try {
                Results.writeElapsed(Duration.between(Results.CURTAIN, ZonedDateTime.now()));
                ImmutableCollection<Ballot> answers = new Ballots().answers();
                LocalDateTime maxTimestamp = answers.stream().map(Ballots::timestamp)
                        .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                if (lastTimestamp == null || lastTimestamp.isBefore(maxTimestamp)) {
                    Results.write(answers.stream()
                            .map(player -> player.toDOM().setAttribute("timestamp",
                                    timestamp(player).toString()))
                            .reduce(new Element("ballots"), Element::addContent));
                    System.err.println(LocalDateTime.now() + " - Downloaded: " + answers.size()
                            + " ballots - After: "
                            + Duration.between(maxTimestamp, LocalDateTime.now()).toString()
                                    .substring(2));
                    lastTimestamp = maxTimestamp;
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
            }
    }

    private static LocalDateTime timestamp(Ballot inAnswers) {
        return LocalDateTime.parse(inAnswers.answer(DataColumn.TIMESTAMP),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }
}