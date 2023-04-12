package oscars;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;

/** Reader for ballots and the ballot definition - Immutable */
public final class BallotReader {
    /** Provides the latest ballot for each player */
    public static final Collector<Map<String, String>, ?, Collection<Map<String, String>>> LATEST = Collectors
            .collectingAndThen(
                    Collectors.toMap(ballot -> getName(ballot), ballot -> ballot,
                            BinaryOperator
                                    .maxBy(Comparator.comparing(ballot -> getTimestamp(ballot)))),
                    Map::values);

    private static final String URL_FILE = "ResponsesURL.txt";

    private static final String CATEGORY_VALUES_FILE = "categoryValues.csv";

    /** An ordered Map that defines each category name and any nominees for that category */
    public final Map<String, List<String>> categoryValues;

    /** Output either the name and timestamp or the email for each ballot */
    public static void main(String[] inArgs) throws Exception {
        BallotReader ballotReader = new BallotReader();
        if (inArgs.length == 0)
            for (LocalDateTime lastTimestamp = null;; Thread.sleep(TimeUnit.SECONDS.toMillis(10)))
                lastTimestamp = ballotReader.writeNewBallots(lastTimestamp);
        else if ("emails".equalsIgnoreCase(inArgs[0]))
            ballotReader.readBallots().filter(ballot -> !ballot.get(Category.EMAIL.name).isEmpty())
                    .forEach(ballot -> System.out
                            .println(getName(ballot) + " = " + ballot.get(Category.EMAIL.name)));
        else
            throw new Exception("Unknown action: " + inArgs[0]);
    }

    /** Prepare to read ballots by reading the ballot definition */
    public BallotReader() throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(CATEGORY_VALUES_FILE))) {
            List<String[]> lines = reader.readAll();
            categoryValues = Collections.unmodifiableMap(IntStream.range(0, lines.get(0).length)
                    .boxed()
                    .collect(Collectors.toMap(column -> lines.get(0)[column],
                            column -> Collections.unmodifiableList(lines.stream().skip(1)
                                    .map(entries -> entries[column]).filter(StringUtils::isNotEmpty)
                                    .collect(Collectors.toList())),
                            (list1, list2) -> list1, LinkedHashMap::new)));
        }
    }

    public Stream<Map<String, String>> readBallots() throws Exception {
        List<String> categoryNames = new ArrayList<>(categoryValues.keySet());
        try (Stream<String> lines = Files.lines(
                Paths.get(BallotReader.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URL(lines.iterator().next());
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReaderHeaderAware(
                    new InputStreamReader(url.openStream()))) {
                return reader.readAll().stream().map(ballot -> Collections
                        .unmodifiableMap(IntStream.range(0, categoryNames.size()).boxed().collect(
                                Collectors.toMap(categoryNames::get, column -> ballot[column]))));
            }
        }
    }

    private LocalDateTime writeNewBallots(LocalDateTime inLastTimestamp) throws Exception {
        try {
            Collection<Map<String, String>> ballots = readBallots().collect(LATEST);
            LocalDateTime maxTimestamp = ballots.stream().map(BallotReader::getTimestamp)
                    .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
            if (inLastTimestamp == null || inLastTimestamp.isBefore(maxTimestamp)) {
                System.err.println(LocalDateTime.now() + " - Downloaded: " + ballots.size()
                        + " ballots - After: " + Duration.between(maxTimestamp, LocalDateTime.now())
                                .toString().substring(2));
                Results.write(ZonedDateTime.now(), ballots.stream().map(BallotReader::toDOM)
                        .reduce(new Element("ballots"), Element::addContent));
                return maxTimestamp;
            }
        } catch (IOException e) {
            System.err.println(LocalDateTime.now() + " - Error downloading ballots: " + e);
        }
        return inLastTimestamp;
    }

    private static String getName(Map<String, String> inBallot) {
        return inBallot.get(Category.LAST_NAME.name) + ", "
                + inBallot.get(Category.FIRST_NAME.name);
    }

    private static LocalDateTime getTimestamp(Map<String, String> inBallot) {
        return LocalDateTime.parse(inBallot.get(Category.TIMESTAMP.name),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private static Element toDOM(Map<String, String> inBallot) {
        LocalDateTime timestamp = getTimestamp(inBallot);
        return new Element("ballot").addContent(new Element("name").addContent(getName(inBallot)))
                .addContent(new Element("timestamp").setAttribute("raw", timestamp.toString())
                        .addContent(timestamp
                                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"))));
    }
}