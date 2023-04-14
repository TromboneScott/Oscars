package oscars;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;

/** Reader for ballots and the ballot definition - Immutable */
public final class BallotReader {
    private static final String URL_FILE = "ResponsesURL.txt";

    private static final String CATEGORY_VALUES_FILE = "categoryValues.csv";

    /** Categories (in order) mapped to their nominees (also in order) */
    public final Map<String, List<String>> categoryValues;

    /** Prepare to read ballots by reading the ballot definition */
    public BallotReader() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(CATEGORY_VALUES_FILE))) {
            List<String[]> lines = reader.readAll();
            for (int row = 1; row < lines.size(); row++)
                if (lines.get(row).length != lines.get(0).length)
                    throw new Exception("Number of columns is inconsistent");
            categoryValues = Collections.unmodifiableMap(IntStream.range(0, lines.get(0).length)
                    .boxed()
                    .collect(Collectors.toMap(column -> lines.get(0)[column],
                            column -> Collections.unmodifiableList(lines.stream().skip(1)
                                    .map(entries -> entries[column]).filter(StringUtils::isNotEmpty)
                                    .collect(Collectors.toList())),
                            (list1, list2) -> list1, LinkedHashMap::new)));
        } catch (Exception e) {
            throw new IOException("Error reading file: " + CATEGORY_VALUES_FILE, e);
        }
    }

    public Stream<Ballot> readBallots() throws IOException {
        List<String> categoryNames = new ArrayList<>(categoryValues.keySet());
        try (Stream<String> lines = Files.lines(
                Paths.get(BallotReader.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URL(lines.iterator().next());
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReaderHeaderAware(
                    new InputStreamReader(url.openStream()))) {
                return reader.readAll().stream().peek(ballot -> {
                    if (ballot.length != categoryNames.size())
                        throw new RuntimeException("Ballot length: " + ballot.length
                                + " does not match category values: " + categoryNames.size());
                }).map(ballot -> IntStream.range(0, ballot.length).boxed()
                        .collect(Ballot.toBallot(categoryNames::get, column -> ballot[column])));
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from: " + URL_FILE, e);
        }
    }
}