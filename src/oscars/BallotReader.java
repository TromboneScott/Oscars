package oscars;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;

/** Reader for ballots and the ballot definition - Immutable */
public final class BallotReader {
    private static final File CATEGORY_DEFINITIONS_FILE = new File("categoryDefinitions.xml");

    private static final String URL_FILE = "ResponsesURL.txt";

    /** Categories (in order) mapped to their nominees (also in order) */
    public final Map<String, Category> categoryDefinitions;

    /** Prepare to read ballots by reading the ballot definition */
    public BallotReader() throws IOException {
        try {
            categoryDefinitions = Collections.unmodifiableMap(new SAXBuilder()
                    .build(CATEGORY_DEFINITIONS_FILE).getRootElement().getChildren("category")
                    .stream().map(Category::of).collect(Collectors.toMap(category -> category.name,
                            category -> category, (list1, list2) -> list1, LinkedHashMap::new)));
        } catch (JDOMException e) {
            throw new IOException(
                    "ERROR: Unable to read category definitions file: " + CATEGORY_DEFINITIONS_FILE,
                    e);
        }
        SortTypes.writePages();
    }

    public Stream<Ballot> readBallots() throws IOException {
        List<String> categoryNames = new ArrayList<>(categoryDefinitions.keySet());
        try (Stream<String> lines = Files.lines(
                Paths.get(BallotReader.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URL(lines.iterator().next());
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReaderHeaderAware(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                return reader.readAll().stream().peek(ballot -> {
                    if (ballot.length != categoryNames.size())
                        throw new RuntimeException("Ballot length: " + ballot.length
                                + " does not match category definitions: " + categoryNames.size());
                }).map(ballot -> IntStream.range(0, ballot.length).boxed().collect(
                        Ballot.toBallot(categoryNames::get, column -> ballot[column].trim())));
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from: " + URL_FILE, e);
        }
    }
}