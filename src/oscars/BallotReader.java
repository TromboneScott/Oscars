package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.opencsv.CSVReader;

/** A reader that gets the ballots and header info from the survey */
public class BallotReader {
    private static final String URL_FILE = "ResponsesURL.txt";

    /** The header info for the categories from the survey */
    public final List<String> headers;

    private final List<String[]> ballots;

    /** Create a reader and read the ballots and header info from the survey */
    public BallotReader() throws IOException {
        try (Stream<String> lines = Files
                .lines(Paths.get(Ballot.class.getClassLoader().getResource(URL_FILE).toURI()))) {
            URL url = new URL(lines.iterator().next());
            url.openConnection().setDefaultUseCaches(false);
            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                headers = Collections.unmodifiableList(Arrays.asList(reader.readNext()));
                if (headers.size() != Category.ALL.size())
                    throw new IOException("Ballot headers size: " + headers.size()
                            + " does not match defined categories: " + Category.ALL.size());
                ballots = Collections.unmodifiableList(reader.readAll());
            }
        } catch (Exception e) {
            throw new IOException("Error reading ballots using URL from: " + URL_FILE, e);
        }
    }

    /** Get a Stream of the ballots from the survey */
    public Stream<Ballot> stream() {
        return ballots.stream().map(Ballot::new);
    }
}