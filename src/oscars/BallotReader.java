package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVReader;

/** A reader that gets the ballots and header info from the survey */
public class BallotReader {
    private static final String URL_FILE = "ResponsesURL.txt";

    /** The category names from the header row of the survey */
    public final List<String> categories;

    /** The ballots that were received */
    public final List<Ballot> ballots;

    /** Create a reader and read the ballots and header info from the survey */
    public BallotReader() throws IOException {
        try (Stream<String> lines = Files
                .lines(Paths.get(Ballot.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URI(lines.iterator().next()).toURL();
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                categories = Collections.unmodifiableList(Arrays.asList(reader.readNext()));
                if (categories.size() != Category.ALL.size())
                    throw new IOException("Number of categories in ballots: " + categories.size()
                            + " does not match defined categories: " + Category.ALL.size());
                ballots = Collections.unmodifiableList(
                        reader.readAll().stream().map(Ballot::new).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from: " + URL_FILE, e);
        }
    }

    /** Get the latest Ballot for each player */
    public Collection<Ballot> latest() {
        return ballots.stream().collect(Collectors.toMap(Ballot::getName, ballot -> ballot,
                BinaryOperator.maxBy(Comparator.comparing(Ballot::getTimestamp)))).values();
    }
}